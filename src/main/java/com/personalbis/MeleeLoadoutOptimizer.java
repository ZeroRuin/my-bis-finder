package com.personalbis;

import java.util.*;
import java.util.stream.IntStream;
import javax.inject.Inject;

public class MeleeLoadoutOptimizer
{
    private static final int SLOT_CHOICES=4, BEAM_WIDTH=32;
    private final EquipmentIntelligence intelligence;
    private final MeleeCombatCalculator calculator;
    private static final class SearchContext
    {
        final EnumMap<EquipmentSlot,List<EquipmentCandidate>> choices=new EnumMap<>(EquipmentSlot.class);
        final Map<String,CombatResult> exact=new HashMap<>();
    }
    private static final class WeaponSearchResult
    {
        final EquipmentCandidate weapon; final OptimizedLoadout loadout;
        WeaponSearchResult(EquipmentCandidate w,OptimizedLoadout l){weapon=w;loadout=l;}
    }

    @Inject
    public MeleeLoadoutOptimizer(EquipmentIntelligence intelligence,MeleeCombatCalculator calculator)
    {
        this.intelligence=intelligence;
        this.calculator=calculator;
    }

    // alpha50.4.34 DEV synthetic melee harness: evaluate one explicitly fixed
    // equipment map without adding synthetic weapons to the production bank/optimizer.
    public CombatResult evaluateFixed(Map<EquipmentSlot,EquipmentCandidate> gear, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask)
    {
        return calculator.calculate(gear, style, monster, onSlayerTask);
    }

    public OptimizedLoadout optimize(List<BankItem> bank,AttackStyle style,MonsterDefinition monster,boolean onSlayerTask)
    {
        MeleeOptimizationReport report=optimizeWithReport(bank,style,monster,onSlayerTask);
        return report==null ? null : report.getBest();
    }

    public MeleeOptimizationReport optimizeWithReport(List<BankItem> bank,AttackStyle style,
        MonsterDefinition monster,boolean onSlayerTask)
    {
        if(style!=AttackStyle.MELEE_STAB&&style!=AttackStyle.MELEE_SLASH&&style!=AttackStyle.MELEE_CRUSH)
            return null;

        Map<EquipmentSlot,List<EquipmentCandidate>> ranked=intelligence.rank(bank,style,onSlayerTask);
        return optimizeWithReportRanked(ranked,style,monster,onSlayerTask);
    }

    public MeleeOptimizationReport optimizeWithReportRanked(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
        AttackStyle style,MonsterDefinition monster,boolean onSlayerTask)
    {
        if(style!=AttackStyle.MELEE_STAB&&style!=AttackStyle.MELEE_SLASH&&style!=AttackStyle.MELEE_CRUSH)return null;
        ranked=SlayerTargetRequirements.constrainRanked(monster,ranked);
        EquipmentSlot requiredSlot=SlayerTargetRequirements.requiredEquipmentSlot(monster);
        EquipmentCandidate requiredEquipment=requiredSlot==null?null:
            SlayerTargetRequirements.firstRequiredEquipment(monster,ranked.get(requiredSlot));
        List<EquipmentCandidate> weapons=shortlist(ranked.get(EquipmentSlot.WEAPON),monster);
        List<WeaponComparison> comparisons=new ArrayList<>();
        List<String> audit=new ArrayList<>();
        appendSlotAudit(audit,"BODY",ranked.get(EquipmentSlot.BODY));
        appendSlotAudit(audit,"LEGS",ranked.get(EquipmentSlot.LEGS));
        OptimizedLoadout best=null;
        List<EquipmentCandidate> searchable=new ArrayList<>();
        for(EquipmentCandidate weapon:weapons)
        {
            if(requiredSlot==EquipmentSlot.SHIELD&&weapon.isTwoHanded())continue;
            boolean supported=MeleeAttackStyleResolver.supports(weapon, style);
            String wn=weapon.getItem().getName();
            boolean focus=wn.toLowerCase().contains("keris");
            if (!supported)
            {
                if(focus) audit.add("KERIS id="+weapon.getItem().getItemId()+" "+wn+" cat="+weapon.getWeaponCategory()+" -> REJECT style");
                continue;
            }
            searchable.add(weapon);
        }
        final Map<EquipmentSlot,List<EquipmentCandidate>> searchRanked=ranked;
        List<WeaponSearchResult> results=IntStream.range(0,searchable.size()).parallel().mapToObj(i->{
            EquipmentCandidate weapon=searchable.get(i);
            return new WeaponSearchResult(weapon,optimizeForWeapon(weapon,searchRanked,style,monster,
                onSlayerTask,requiredEquipment,new SearchContext()));
        }).collect(java.util.stream.Collectors.toList());
        for(WeaponSearchResult result:results)
        {
            EquipmentCandidate weapon=result.weapon;
            OptimizedLoadout weaponBest=result.loadout;
            if(weaponBest!=null)
            {
                comparisons.add(new WeaponComparison(weapon.getItem().getName(),weaponBest));
                if(weapon.getItem().getName().toLowerCase().contains("keris"))
                    audit.add(weaponAudit("KERIS",weapon,weaponBest,monster,onSlayerTask));
                if(best==null||FinalLoadoutSelection.better(weaponBest.getResult().getDps(),best.getResult().getDps())) best=weaponBest;
            }
        }

        comparisons.sort((a,b)->Double.compare(b.getResult().getDps(),a.getResult().getDps()));
        for(int i=0;i<Math.min(6,comparisons.size());i++)
            audit.add(weaponAudit("TOP"+(i+1), findWeapon(weapons,comparisons.get(i).getWeaponName()), comparisons.get(i).getLoadout(),monster,onSlayerTask));
        return new MeleeOptimizationReport(best,comparisons,audit);
    }

    private OptimizedLoadout optimizeForWeapon(EquipmentCandidate weapon,
        Map<EquipmentSlot,List<EquipmentCandidate>> ranked,AttackStyle style,
        MonsterDefinition monster,boolean onSlayerTask,EquipmentCandidate requiredEquipment,SearchContext context)
    {
        Map<EquipmentSlot,EquipmentCandidate> seed=new EnumMap<>(EquipmentSlot.class);
        seed.put(EquipmentSlot.WEAPON,weapon);
        if(requiredEquipment!=null)seed.put(requiredEquipment.getSlot(),requiredEquipment);
        List<Map<EquipmentSlot,EquipmentCandidate>> beam=new ArrayList<>();
        beam.add(seed);
        Map<EquipmentSlot,EquipmentCandidate> verac=veracsBundle(ranked,weapon);
        if(verac!=null){if(requiredEquipment!=null)verac.put(requiredEquipment.getSlot(),requiredEquipment);beam.add(verac);}
        Map<EquipmentSlot,EquipmentCandidate> crystal=crystalBlessingBundle(ranked,weapon);
        if(crystal!=null){if(requiredEquipment!=null)crystal.put(requiredEquipment.getSlot(),requiredEquipment);beam.add(crystal);}

        for(EquipmentSlot slot:EquipmentSlot.values())
        {
            if(slot==EquipmentSlot.WEAPON) continue;
            if(SlayerTargetRequirements.requiredEquipmentSlot(monster)==slot)continue;
            if(weapon.isTwoHanded()&&slot==EquipmentSlot.SHIELD) continue;
            List<EquipmentCandidate> choices=context.choices.computeIfAbsent(slot,
                s->dominancePrune(usable(ranked.get(s),SLOT_CHOICES)));
            if(choices.isEmpty()) continue;

            List<Map<EquipmentSlot,EquipmentCandidate>> next=new ArrayList<>();
            for(Map<EquipmentSlot,EquipmentCandidate>b:beam)
            {
                // Empty optional slots are legal. This prevents a negative-bonus item
                // from being forced into an otherwise stronger complete loadout.
                next.add(new EnumMap<>(b));
                for(EquipmentCandidate c:choices)
                {
                    Map<EquipmentSlot,EquipmentCandidate> n=new EnumMap<>(b);
                    n.put(slot,c);
                    next.add(n);
                }
            }
            final Map<Map<EquipmentSlot,EquipmentCandidate>,Double> dpsCache=new IdentityHashMap<>();
            for(Map<EquipmentSlot,EquipmentCandidate> g:next)
                dpsCache.put(g,calculateCached(g,style,monster,onSlayerTask,context).getDps());
            next.sort((a,b)->{
                int d=Double.compare(dpsCache.get(b),dpsCache.get(a));
                return d!=0?d:Integer.compare(prayerBonus(b),prayerBonus(a));
            });
            beam=new ArrayList<>(next.subList(0,Math.min(BEAM_WIDTH,next.size())));
        }

        OptimizedLoadout best=null;
        for(Map<EquipmentSlot,EquipmentCandidate> g:beam)
        {
            CombatResult r=calculateCached(g,style,monster,onSlayerTask,context);
            if(best==null||FinalLoadoutSelection.better(r.getDps(),best.getResult().getDps())
                ||(Double.compare(r.getDps(),best.getResult().getDps())==0
                   && prayerBonus(g)>prayerBonus(best.getItems())))
                best=new OptimizedLoadout(style,g,r);
        }
        return completeNeutralSlots(best,ranked,style,monster,onSlayerTask,weapon,context);
    }

    /**
     * Beam search may intentionally keep an optional slot empty while comparing
     * offensive candidates. Once the DPS winner is known, fill any remaining
     * slot with the best owned item that does not reduce that winner's DPS.
     * This is deliberately post-search so it cannot distort weapon/style ranking.
     */
    private OptimizedLoadout completeNeutralSlots(OptimizedLoadout best,
        Map<EquipmentSlot,List<EquipmentCandidate>> ranked,AttackStyle style,
        MonsterDefinition monster,boolean onSlayerTask,EquipmentCandidate weapon,SearchContext context)
    {
        if(best==null)return null;
        Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(best.getItems());
        CombatResult current=best.getResult();

        for(EquipmentSlot slot:EquipmentSlot.values())
        {
            if(slot==EquipmentSlot.WEAPON||gear.containsKey(slot))continue;
            if(weapon.isTwoHanded()&&slot==EquipmentSlot.SHIELD)continue;

            EquipmentCandidate chosen=null;
            CombatResult chosenResult=current;
            List<EquipmentCandidate> all=ranked.get(slot);
            if(all==null)continue;

            int examined=0;
            for(EquipmentCandidate c:all)
            {
                if (examined++ >= 8) break;
                if(c.getRequirementResult().isBlocked())continue;
                Map<EquipmentSlot,EquipmentCandidate> trial=new EnumMap<>(gear);
                trial.put(slot,c);
                CombatResult r=calculateCached(trial,style,monster,onSlayerTask,context);

                // Never sacrifice the offensive result merely to make the grid full.
                if(Double.compare(r.getDps(),current.getDps())<0)continue;
                if(chosen==null
                    || FinalLoadoutSelection.better(r.getDps(),chosenResult.getDps())
                    || (Double.compare(r.getDps(),chosenResult.getDps())==0
                        && c.getPrayer()>chosen.getPrayer()))
                {
                    chosen=c;
                    chosenResult=r;
                }
            }

            if(chosen!=null)
            {
                gear.put(slot,chosen);
                current=chosenResult;
            }
        }
        return new OptimizedLoadout(style,gear,current);
    }

    private CombatResult calculateCached(Map<EquipmentSlot,EquipmentCandidate> gear,AttackStyle style,
        MonsterDefinition monster,boolean onSlayerTask,SearchContext context)
    {
        StringBuilder key=new StringBuilder();
        for(EquipmentSlot slot:EquipmentSlot.values())
        {
            EquipmentCandidate c=gear.get(slot);
            key.append(c==null?0:c.getItem().getItemId()).append(',');
        }
        String k=key.toString();
        CombatResult hit=context.exact.get(k);
        if(hit!=null)return hit;
        CombatResult calculated=calculator.calculate(gear,style,monster,onSlayerTask);
        context.exact.put(k,calculated);
        return calculated;
    }

    private List<EquipmentCandidate> dominancePrune(List<EquipmentCandidate> raw)
    {
        if(raw.size()<2)return raw;
        List<EquipmentCandidate> out=new ArrayList<>();
        for(int i=0;i<raw.size();i++)
        {
            EquipmentCandidate c=raw.get(i); boolean dominated=false;
            if(MeleeLoadoutEffects.isSynergyItem(c)){out.add(c);continue;}
            for(int j=0;j<raw.size();j++)
            {
                if(i==j)continue;
                EquipmentCandidate d=raw.get(j);
                if(MeleeLoadoutEffects.isSynergyItem(d))continue;
                boolean noWorse=d.getAttackBonus()>=c.getAttackBonus()
                    &&d.getStrengthBonus()>=c.getStrengthBonus()&&d.getPrayer()>=c.getPrayer();
                boolean strict=d.getAttackBonus()>c.getAttackBonus()
                    ||d.getStrengthBonus()>c.getStrengthBonus()||d.getPrayer()>c.getPrayer();
                boolean exact=d.getAttackBonus()==c.getAttackBonus()
                    &&d.getStrengthBonus()==c.getStrengthBonus()&&d.getPrayer()==c.getPrayer();
                if((noWorse&&strict)||(exact&&j<i)){dominated=true;break;}
            }
            if(!dominated)out.add(c);
        }
        return out;
    }

    private void appendSlotAudit(List<String> audit,String label,List<EquipmentCandidate> all)
    {
        if(all==null)return;
        int rank=0;
        for(EquipmentCandidate c:all)
        {
            if(c==null||c.getRequirementResult().isBlocked())continue;
            rank++;
            String n=c.getItem().getName().toLowerCase();
            if(rank<=6||n.contains("bandos")||n.contains("mixed hide"))
                audit.add(label+" #"+rank+" "+c.getItem().getName()+" id="+c.getItem().getItemId()+" atk="+c.getAttackBonus()+" str="+c.getStrengthBonus()+" score="+c.getScore()+(rank<=SLOT_CHOICES?" IN":" OUT"));
        }
    }

    private EquipmentCandidate findWeapon(List<EquipmentCandidate> weapons,String name)
    {
        for(EquipmentCandidate w:weapons)if(w.getItem().getName().equals(name))return w;
        return null;
    }

    private String weaponAudit(String prefix,EquipmentCandidate w,OptimizedLoadout l,MonsterDefinition monster,boolean onSlayerTask)
    {
        if(w==null||l==null)return prefix+" unavailable";
        CombatResult r=l.getResult();
        EquipmentCandidate b=l.getItems().get(EquipmentSlot.BODY), g=l.getItems().get(EquipmentSlot.LEGS);
        String base=String.format(java.util.Locale.ROOT,"%s id=%d %s | cat=%s | keris=%s | dps=%.5f acc=%.2f%% max=%d speed=%d | body=%s | legs=%s",
            prefix,w.getItem().getItemId(),w.getItem().getName(),w.getWeaponCategory(),MeleeWeaponEffects.isKeris(w),r.getDps(),r.getAccuracy()*100.0,r.getMaxHit(),r.getAttackSpeed(),
            b==null?"—":b.getItem().getName(),g==null?"—":g.getItem().getName());
        if(MeleeWeaponEffects.isKeris(w) && r.getMaxHit()>0)
        {
            int proc=MeleeWeaponEffects.kerisProcMax(r.getMaxHit());
            double avg=MeleeWeaponEffects.kerisAverageHit(r.getMaxHit());
            double ev=r.getAccuracy()*avg;
            double accMult=MeleeWeaponEffects.accuracyMultiplier(w,monster,l.getStyle());
            String detail=base+String.format(java.util.Locale.ROOT,
                " | Keris detail: effAtk=%d effStr=%d stabAtk=%d str=%d preAtkRoll=%d accMult=%.3f finalAtkRoll=%d defRoll=%d | base=%d -> kalphite=%d -> proc=%d | successAvg=%.5f | EV/attack=%.5f",
                r.getEffectiveAttack(),r.getEffectiveStrength(),r.getAttackBonus(),r.getStrengthBonus(),
                r.getPreWeaponAttackRoll(),accMult,r.getAttackRoll(),r.getDefenceRoll(),
                r.getPreWeaponMaxHit(),r.getMaxHit(),proc,avg,ev);
            // Alpha50.4.33: expose each legal stance independently. Alternative weapons are
            // intentionally allowed to choose their own best stance; the selected Fang's
            // stance must not be assumed to apply to a Keris candidate.
            StringBuilder stanceAudit=new StringBuilder(detail);
            for(MeleeStance stance:MeleeAttackStyleResolver.legalStances(w,l.getStyle()))
            {
                CombatResult sr=calculator.calculateForStance(l.getItems(),l.getStyle(),monster,onSlayerTask,
                    stance,r.getPrayer());
                int sp=MeleeWeaponEffects.isKeris(w)&&monster!=null&&
                    (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite"))
                    ? MeleeWeaponEffects.kerisProcMax(sr.getMaxHit()) : sr.getMaxHit();
                double savg=MeleeWeaponEffects.isKeris(w)&&monster!=null&&
                    (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite"))
                    ? MeleeWeaponEffects.kerisAverageHit(sr.getMaxHit())
                    : MeleeWeaponEffects.successfulHitAverage(sr.getMaxHit());
                stanceAudit.append(String.format(java.util.Locale.ROOT,
                    " | %s: effAtk=%d effStr=%d roll=%d acc=%.2f%% base=%d max=%d proc=%d EV=%.5f dps=%.5f",
                    stance,sr.getEffectiveAttack(),sr.getEffectiveStrength(),sr.getAttackRoll(),
                    sr.getAccuracy()*100.0,sr.getPreWeaponMaxHit(),sr.getMaxHit(),sp,
                    sr.getAccuracy()*savg,sr.getDps()));
            }
            return stanceAudit.toString();
        }
        return base;
    }

    private List<EquipmentCandidate> shortlist(List<EquipmentCandidate> all,MonsterDefinition m)
    {
        // Alpha 50.4 completeness pass: a raw-stat weapon rank must never decide
        // whether an owned/equippable weapon gets an exact target DPS evaluation.
        // Attack-interface eligibility is still enforced in optimizeWithReportRanked.
        List<EquipmentCandidate> x=new ArrayList<>();
        if(all!=null)for(EquipmentCandidate c:all)
            if(c!=null&&!c.getRequirementResult().isBlocked())x.add(c);
        return x;
    }

    private Map<EquipmentSlot,EquipmentCandidate> veracsBundle(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
        EquipmentCandidate weapon)
    {
        if(weapon==null||weapon.getItem()==null||!weapon.getItem().getName().equalsIgnoreCase("Verac's flail"))return null;
        EquipmentCandidate head=findNamed(ranked.get(EquipmentSlot.HEAD),"Verac's helm");
        EquipmentCandidate body=findNamed(ranked.get(EquipmentSlot.BODY),"Verac's brassard");
        EquipmentCandidate legs=findNamed(ranked.get(EquipmentSlot.LEGS),"Verac's plateskirt");
        if(head==null||body==null||legs==null)return null;
        Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);g.put(EquipmentSlot.WEAPON,weapon);
        g.put(EquipmentSlot.HEAD,head);g.put(EquipmentSlot.BODY,body);g.put(EquipmentSlot.LEGS,legs);return g;
    }

    private EquipmentCandidate findNamed(List<EquipmentCandidate> items,String name)
    {
        if(items!=null)for(EquipmentCandidate c:items)if(c!=null&&!c.getRequirementResult().isBlocked()
            &&c.getItem()!=null&&c.getItem().getName().equalsIgnoreCase(name))return c;
        return null;
    }

    private Map<EquipmentSlot,EquipmentCandidate> crystalBlessingBundle(
        Map<EquipmentSlot,List<EquipmentCandidate>> ranked, EquipmentCandidate weapon)
    {
        if (weapon == null) return null;
        EquipmentCandidate blessing=findNamed(ranked.get(EquipmentSlot.NECK),"Crystal blessing");
        EquipmentCandidate helm=findNamed(ranked.get(EquipmentSlot.HEAD),"Crystal helm");
        EquipmentCandidate body=findNamed(ranked.get(EquipmentSlot.BODY),"Crystal body");
        EquipmentCandidate legs=findNamed(ranked.get(EquipmentSlot.LEGS),"Crystal legs");
        if (blessing==null||helm==null||body==null||legs==null) return null;
        Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);
        g.put(EquipmentSlot.WEAPON,weapon); g.put(EquipmentSlot.NECK,blessing);
        g.put(EquipmentSlot.HEAD,helm); g.put(EquipmentSlot.BODY,body); g.put(EquipmentSlot.LEGS,legs);
        return g;
    }

    private List<EquipmentCandidate> usable(List<EquipmentCandidate> all,int limit)
    {
        List<EquipmentCandidate>x=new ArrayList<>();
        if(all!=null)
        {
            for(EquipmentCandidate c:all)
            {
                if(!c.getRequirementResult().isBlocked() && x.size()<limit) x.add(c);
            }
            EquipmentCandidate prayerBest=null;
            for(EquipmentCandidate c:all)if(!c.getRequirementResult().isBlocked()
                &&(prayerBest==null||c.getPrayer()>prayerBest.getPrayer()))prayerBest=c;
            if(prayerBest!=null&&prayerBest.getPrayer()>0&&!x.contains(prayerBest))x.add(prayerBest);
            // Set-effect pieces can look weak individually, so always preserve owned,
            // usable synergy pieces even when they fall outside the ordinary top-N.
            for(EquipmentCandidate c:all)
            {
                if(!c.getRequirementResult().isBlocked()
                    && MeleeLoadoutEffects.isSynergyItem(c) && !x.contains(c)) x.add(c);
            }
        }
        return x;
    }

    private int prayerBonus(Map<EquipmentSlot,EquipmentCandidate> g)
    {
        int p=0; for(EquipmentCandidate c:g.values())if(c!=null)p+=c.getPrayer(); return p;
    }

    private boolean isSpecial(String name,MonsterDefinition m)
    {
        if(name==null||m==null)return false;
        String n=name.toLowerCase();
        if(m.hasAttribute("demon")&&(n.contains("arclight")||n.contains("emberlight")||
            n.contains("silverlight")||n.contains("darklight")))return true;
        if(m.hasAttribute("undead")&&(n.contains("salve")||n.contains("blisterwood")))return true;
        if(n.contains("fang")||n.contains("scythe")||n.contains("keris")||n.contains("dragon hunter"))return true;
        return false;
    }
}
