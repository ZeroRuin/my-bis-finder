package com.personalbis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.inject.Inject;
import net.runelite.api.Skill;

public class RangedLoadoutOptimizer
{
    // Wide limits on purpose: a narrower 6/48/8 search was faster, but real banks showed
    // it could discard complete Crystal/Bowfa and crossbow branches.
    private static final int SLOT_CHOICES = 12, BEAM_WIDTH = 128, EXACT_FINALISTS = 48;
    private static final String[] MECHANIC_ITEMS = {"crystal helm", "crystal body", "crystal legs", "void ranger helm",
        "void knight top", "elite void top", "void knight robe", "elite void robe", "void knight gloves", "salve amulet",
        "slayer helm", "slayer helmet", "black mask", "karil's", "amulet of the damned"};
    private final RangedCombatCalculator calc;
    private final AccountSnapshot account;

    @Inject
    public RangedLoadoutOptimizer(RangedCombatCalculator calc, AccountSnapshot account)
    {
        this.calc = calc;
        this.account = account;
    }

    /** The fastest ranged loadout, or null when the player owns no usable ranged weapon. */
    public OptimizedRangedLoadout optimize(Map<EquipmentSlot, List<EquipmentCandidate>> ranked, MonsterDefinition m, boolean task)
    {
        ranked = SlayerTargetRequirements.constrainRanked(m, ranked);
        EquipmentSlot requiredSlot = SlayerTargetRequirements.requiredEquipmentSlot(m);
        EquipmentCandidate requiredEquipment = requiredSlot == null ? null : SlayerTargetRequirements.firstRequiredEquipment(m, ranked.get(requiredSlot));
        OptimizedRangedLoadout best = null;
        // Every owned, usable ranged weapon gets a full search: raw rank never excludes a weapon up front.
        for (EquipmentCandidate weapon : LoadoutSearch.allUsable(ranked.get(EquipmentSlot.WEAPON)))
        {
            CalculationCancellation.check();
            if (CombatStyleLabels.rangedStances(weapon.getWeaponCategory()).isEmpty()) continue;
            if (requiredSlot == EquipmentSlot.SHIELD && RangedAmmoRules.occupiesBothHands(weapon)) continue;
            OptimizedRangedLoadout one = new WeaponSearch(ranked, weapon, m, task, requiredEquipment).run();
            if (one != null && (best == null || FinalLoadoutSelection.faster(one.getResult(), best.getResult()))) best = one;
        }
        return best;
    }

    /** The search for one weapon. Its caches are dropped with it, so memory stays bounded. */
    private final class WeaponSearch
    {
        final Map<EquipmentSlot, List<EquipmentCandidate>> ranked;
        final EquipmentCandidate weapon;
        final MonsterDefinition m;
        final boolean task;
        final EquipmentCandidate requiredEquipment;
        final Map<String, Double> scoreCache = new HashMap<>();
        final Map<String, RangedCombatResult> exactCache = new HashMap<>();
        final EnumMap<EquipmentSlot, List<EquipmentCandidate>> choiceCache = new EnumMap<>(EquipmentSlot.class);

        WeaponSearch(Map<EquipmentSlot, List<EquipmentCandidate>> ranked, EquipmentCandidate weapon, MonsterDefinition m,
            boolean task, EquipmentCandidate requiredEquipment)
        {
            this.ranked = ranked;
            this.weapon = weapon;
            this.m = m;
            this.task = task;
            this.requiredEquipment = requiredEquipment;
        }

        OptimizedRangedLoadout run()
        {
            if (!RangedAmmoRules.requiresEquippedAmmo(weapon) && !RangedAmmoRules.usesLoadedDarts(weapon)) return searchWithAmmo(null);
            // Ammo is chosen before any armour slot, so a strong partial Bowfa/crystal branch
            // cannot prune a crossbow's best supporting gear before its bolt effect exists.
            OptimizedRangedLoadout best = null;
            for (EquipmentCandidate rawAmmo : LoadoutSearch.allUsable(ranked.get(EquipmentSlot.AMMO)))
            {
                CalculationCancellation.check();
                boolean darts = RangedAmmoRules.usesLoadedDarts(weapon);
                if (darts && !BlowpipeAmmoCandidates.matches(weapon, rawAmmo)) continue;
                EquipmentCandidate ammo = darts ? asLoadedDart(rawAmmo) : rawAmmo;
                if (!RangedAmmoRules.compatible(weapon, ammo)) continue;
                // Ammo branches have different keys: retaining previous branches only grows memory.
                scoreCache.clear();
                exactCache.clear();
                OptimizedRangedLoadout one = searchWithAmmo(ammo);
                if (one != null && (best == null || FinalLoadoutSelection.preferred(one.getResult(), LoadoutSearch.prayerBonus(one.getItems()),
                    best.getResult(), LoadoutSearch.prayerBonus(best.getItems())))) best = one;
            }
            return best;
        }

        private OptimizedRangedLoadout searchWithAmmo(EquipmentCandidate ammo)
        {
            OptimizedRangedLoadout best = searchFamily(ammo, Collections.emptyMap());
            for (Map<EquipmentSlot, EquipmentCandidate> family : VoidLoadoutFamilies.families(ranked, requiredEquipment, "Void ranger helm"))
            {
                CalculationCancellation.check();
                OptimizedRangedLoadout candidate = searchFamily(ammo, family);
                if (candidate != null && (best == null || FinalLoadoutSelection.preferred(candidate.getResult(),
                    LoadoutSearch.prayerBonus(candidate.getItems()), best.getResult(), LoadoutSearch.prayerBonus(best.getItems())))) best = candidate;
            }
            return best;
        }

        private OptimizedRangedLoadout searchFamily(EquipmentCandidate anchoredAmmo, Map<EquipmentSlot, EquipmentCandidate> locked)
        {
            List<Map<EquipmentSlot, EquipmentCandidate>> beam = new ArrayList<>();
            Map<EquipmentSlot, EquipmentCandidate> seed = new EnumMap<>(EquipmentSlot.class);
            seed.put(EquipmentSlot.WEAPON, weapon);
            if (anchoredAmmo != null) seed.put(EquipmentSlot.AMMO, anchoredAmmo);
            if (requiredEquipment != null) seed.put(requiredEquipment.getSlot(), requiredEquipment);
            seed.putAll(locked);
            beam.add(seed);
            if (locked.isEmpty())
            {
                // Karil's and crystal armour only pay off as complete sets with their weapon, so the
                // owned set is seeded whole before per-slot trimming can drop its weaker pieces.
                if (ItemNames.is(weapon, "Karil's crossbow"))
                    addSet(beam, anchoredAmmo, LoadoutSearch.ownedSet(ranked, ItemNames::is, EquipmentSlot.HEAD, "Karil's coif",
                        EquipmentSlot.BODY, "Karil's leathertop", EquipmentSlot.LEGS, "Karil's leatherskirt", EquipmentSlot.NECK, "Amulet of the damned"));
                if (RangedWeaponEffects.usesCrystalArmourBonus(weapon))
                    addSet(beam, anchoredAmmo, LoadoutSearch.ownedSet(ranked, ItemNames::is, EquipmentSlot.HEAD, "Crystal helm",
                        EquipmentSlot.BODY, "Crystal body", EquipmentSlot.LEGS, "Crystal legs"));
            }
            for (EquipmentSlot slot : EquipmentSlot.values())
            {
                if (slot == EquipmentSlot.WEAPON || slot == EquipmentSlot.AMMO || locked.containsKey(slot)) continue;
                if (SlayerTargetRequirements.requiredEquipmentSlot(m) == slot) continue;
                List<EquipmentCandidate> choices = choicesFor(slot);
                List<Map<EquipmentSlot, EquipmentCandidate>> next = new ArrayList<>();
                for (Map<EquipmentSlot, EquipmentCandidate> g : beam)
                {
                    next.add(new EnumMap<>(g));
                    if (slot == EquipmentSlot.SHIELD && RangedAmmoRules.occupiesBothHands(weapon)) continue;
                    for (EquipmentCandidate c : choices)
                    {
                        Map<EquipmentSlot, EquipmentCandidate> n = new EnumMap<>(g);
                        n.put(slot, c);
                        next.add(n);
                    }
                }
                beam = trim(next);
                if (beam.isEmpty()) return null;
            }
            // Self/internal-ammo weapons may use the ammo slot for a blessing-like item.
            if (anchoredAmmo == null && RangedAmmoRules.ammoSlotFreeForNeutralGear(weapon))
            {
                List<Map<EquipmentSlot, EquipmentCandidate>> next = new ArrayList<>();
                for (Map<EquipmentSlot, EquipmentCandidate> g : beam)
                {
                    next.add(new EnumMap<>(g));
                    for (EquipmentCandidate c : LoadoutSearch.allUsable(ranked.get(EquipmentSlot.AMMO)))
                    {
                        if (RangedAmmoRules.ammoType(c) != RangedAmmoType.NONE) continue;
                        Map<EquipmentSlot, EquipmentCandidate> n = new EnumMap<>(g);
                        n.put(EquipmentSlot.AMMO, c);
                        next.add(n);
                    }
                }
                beam = trim(next);
            }
            if (VardorvisRules.supported(m))
                beam = VardorvisShortlist.select(beam, m.getHitpoints(), 2,
                    (g, hp) -> fastDps(g, VardorvisHpSettings.atHp(m, hp), task), LoadoutSearch::prayerBonus);
            Map<EquipmentSlot, EquipmentCandidate> bestGear = null;
            RangedCombatResult bestResult = null;
            for (Map<EquipmentSlot, EquipmentCandidate> g : beam.subList(0, Math.min(EXACT_FINALISTS, beam.size())))
            {
                RangedCombatResult r = exactCached(g, m, task);
                if (r != null && Double.isFinite(r.getDps()) && (bestGear == null
                    || FinalLoadoutSelection.preferred(r, LoadoutSearch.prayerBonus(g), bestResult, LoadoutSearch.prayerBonus(bestGear))))
                {
                    bestGear = g;
                    bestResult = r;
                }
            }
            if (bestGear == null) return null;

            Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(bestGear);
            RangedCombatResult result = LoadoutSearch.fillEmptySlots(gear, bestResult, ranked, this::mayFill, trial -> exactCached(trial, m, task));
            if (anchoredAmmo == null || !RangedAmmoRules.usesLoadedDarts(weapon)) return new OptimizedRangedLoadout(gear, result);
            // A blowpipe's darts are loaded, not worn: show the best Prayer item in the ammo slot instead.
            gear.remove(EquipmentSlot.AMMO);
            EquipmentCandidate neutral = null;
            for (EquipmentCandidate c : LoadoutSearch.allUsable(ranked.get(EquipmentSlot.AMMO)))
                if (RangedAmmoRules.ammoType(c) == RangedAmmoType.NONE && (neutral == null || c.getPrayer() > neutral.getPrayer())) neutral = c;
            if (neutral != null) gear.put(EquipmentSlot.AMMO, neutral);
            return new OptimizedRangedLoadout(gear, result, anchoredAmmo);
        }

        private void addSet(List<Map<EquipmentSlot, EquipmentCandidate>> beam, EquipmentCandidate ammo, Map<EquipmentSlot, EquipmentCandidate> set)
        {
            if (set == null) return;
            set.put(EquipmentSlot.WEAPON, weapon);
            if (ammo != null) set.put(EquipmentSlot.AMMO, ammo);
            if (requiredEquipment != null) set.put(requiredEquipment.getSlot(), requiredEquipment);
            beam.add(set);
        }

        /** Whether an item may fill a slot the search left empty. */
        private boolean mayFill(EquipmentSlot slot, EquipmentCandidate c)
        {
            if (slot == EquipmentSlot.SHIELD) return !RangedAmmoRules.occupiesBothHands(weapon);
            if (slot != EquipmentSlot.AMMO) return true;
            // For self/internal-ammo weapons the Ammo slot is free, but actual arrows, bolts and darts
            // are not neutral gear. Only blessing-like/non-ammo items may fill the free slot.
            return !RangedAmmoRules.requiresEquippedAmmo(weapon)
                && !(RangedAmmoRules.ammoSlotFreeForNeutralGear(weapon) && RangedAmmoRules.ammoType(c) != RangedAmmoType.NONE);
        }

        /**
         * The shortlist for one supporting slot, built once per weapon, with dominated candidates
         * removed. Candidates are only compared inside the same mechanic group, so Crystal, Void,
         * Salve and Slayer pieces are never pruned by ordinary raw-stat gear.
         */
        private List<EquipmentCandidate> choicesFor(EquipmentSlot slot)
        {
            return choiceCache.computeIfAbsent(slot, s -> EquipmentSetRules.strengthRanged(weapon)
                ? LoadoutSearch.dominancePrune(LoadoutSearch.allUsable(ranked.get(s)), RangedLoadoutOptimizer::mechanicGroup,
                    EquipmentCandidate::getRangedAttack, EquipmentCandidate::getStrengthBonus)
                : LoadoutSearch.dominancePrune(LoadoutSearch.shortlist(ranked.get(s), SLOT_CHOICES,
                        c -> EquipmentAuditPolicy.specialRangedWeapon(c.getItem().getName()) || RangedLoadoutEffects.synergy(c)),
                    RangedLoadoutOptimizer::mechanicGroup, EquipmentCandidate::getRangedAttack, EquipmentCandidate::getRangedStrength));
        }

        private RangedCombatResult exactCached(Map<EquipmentSlot, EquipmentCandidate> gear, MonsterDefinition m, boolean task)
        {
            CalculationCancellation.check();
            StringBuilder key = new StringBuilder();
            for (EquipmentSlot slot : EquipmentSlot.values())
            {
                EquipmentCandidate c = gear.get(slot);
                key.append(c == null ? 0 : c.getItem().getItemId()).append(',');
            }
            String k = key.toString();
            RangedCombatResult hit = exactCache.get(k);
            if (hit != null) return hit;
            RangedCombatResult r = calc.calculate(gear, m, task);
            if (exactCache.size() >= 1024) exactCache.clear();
            exactCache.put(k, r);
            return r;
        }

        /** Keeps the best BEAM_WIDTH partial loadouts: quick DPS first, Prayer on exact ties. */
        private List<Map<EquipmentSlot, EquipmentCandidate>> trim(List<Map<EquipmentSlot, EquipmentCandidate>> list)
        {
            // Collapse offensively equivalent partial states before the expensive combat
            // calculation. The key retains every identity that can change a later ranged
            // mechanic/set bonus; among equivalent states the higher-Prayer one is kept.
            LinkedHashMap<String, Map<EquipmentSlot, EquipmentCandidate>> unique = new LinkedHashMap<>();
            for (Map<EquipmentSlot, EquipmentCandidate> g : list)
            {
                String k = combatStateKey(g);
                Map<EquipmentSlot, EquipmentCandidate> prior = unique.get(k);
                if (prior == null || LoadoutSearch.prayerBonus(g) > LoadoutSearch.prayerBonus(prior)) unique.put(k, g);
            }
            if (VardorvisRules.supported(m))
                return VardorvisShortlist.select(new ArrayList<>(unique.values()), m.getHitpoints(), BEAM_WIDTH / 4,
                    (g, hp) -> fastDps(g, VardorvisHpSettings.atHp(m, hp), task), LoadoutSearch::prayerBonus);
            // Only the best BEAM_WIDTH states are needed, so score each once and keep a
            // min-heap of the best, then sort just those for a deterministic best-first beam.
            final class ScoredState
            {
                final Map<EquipmentSlot, EquipmentCandidate> gear;
                final double score;
                final int prayer;

                ScoredState(Map<EquipmentSlot, EquipmentCandidate> gear, double score)
                {
                    this.gear = gear;
                    this.score = score;
                    this.prayer = LoadoutSearch.prayerBonus(gear);
                }
            }
            Comparator<ScoredState> ascending = (a, z) -> {
                int d = Double.compare(a.score, z.score);
                return d != 0 ? d : Integer.compare(a.prayer, z.prayer);
            };
            PriorityQueue<ScoredState> top = new PriorityQueue<>(BEAM_WIDTH, ascending);
            for (Map.Entry<String, Map<EquipmentSlot, EquipmentCandidate>> entry : unique.entrySet())
            {
                Double cached = scoreCache.get(entry.getKey());
                if (cached == null)
                {
                    cached = fastDps(entry.getValue(), m, task);
                    if (scoreCache.size() >= 8192) scoreCache.clear();
                    scoreCache.put(entry.getKey(), cached);
                }
                ScoredState scored = new ScoredState(entry.getValue(), cached);
                if (top.size() < BEAM_WIDTH) top.offer(scored);
                else if (ascending.compare(scored, top.peek()) > 0)
                {
                    top.poll();
                    top.offer(scored);
                }
            }
            List<ScoredState> winners = new ArrayList<>(top);
            winners.sort(ascending.reversed());
            List<Map<EquipmentSlot, EquipmentCandidate>> result = new ArrayList<>(winners.size());
            for (ScoredState scored : winners) result.add(scored.gear);
            return result;
        }

        /** A quick DPS estimate for ranking partial loadouts; the finalists get the exact calculation. */
        private double fastDps(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
          EquipmentCandidate scoredWeapon=gear.get(EquipmentSlot.WEAPON);
          if(VardorvisRules.supported(m)&&EquipmentSetRules.strengthRanged(scoredWeapon)) {
           double best=0;
           for(RangedStance stance:CombatStyleLabels.rangedStances(scoredWeapon.getWeaponCategory()))
            for(RangedPrayer prayer:fastPrayers())
             best=Math.max(best,calc.calculateSnapshot(gear,m,task,stance,prayer).getDps());
           return best;
          }
          if(RaidCombatRules.wardenP2(m)||EquipmentSetRules.strengthRanged(scoredWeapon))return exactCached(gear,m,task).getDps();
          int atk=0,str=0,speed=4; boolean slayer=false; EquipmentCandidate weapon=scoredWeapon;
          for(EquipmentCandidate c:gear.values()){if(c==null)continue;
           if(c.getSlot()==EquipmentSlot.AMMO&&!RangedAmmoRules.compatible(weapon,c))continue;
           atk+=c.getRangedAttack();str+=c.getRangedStrength();
           if(c.getSlot()==EquipmentSlot.WEAPON&&c.getAttackSpeed()>0)speed=c.getAttackSpeed();
           String n=ItemNames.lower(c);
           if(c.getSlot()==EquipmentSlot.HEAD&&(n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask"))&&(n.contains("(i)")||n.contains("imbued")))slayer=true;
          }
          int level=Math.max(1,RaidScaling.boosted(account,Skill.RANGED,m)); boolean voidRanged=RangedLoadoutEffects.voidRanged(gear),eliteVoid=RangedLoadoutEffects.eliteVoidRanged(gear);
          boolean salve=RangedLoadoutEffects.rangedSalveActive(gear,m); int def=m==null?1:Math.max(0,VardorvisRules.startingDefence(m)),bonus=RangedAmmoRules.targetDefence(m,weapon); int defRoll=RaidScaling.defenceRoll(m,(def+9)*(bonus+64));
          EquipmentCandidate ammo=gear.get(EquipmentSlot.AMMO); double best=-1;
          for(RangedStance st:CombatStyleLabels.rangedStances(scoredWeapon==null?"":scoredWeapon.getWeaponCategory()))for(RangedPrayer p:fastPrayers()){
           int eff=CombatEffectiveLevels.ranged(level,p.getRanged(),st.getRangedBoost(),voidRanged); int roll=eff*(atk+64);
           roll=(int)Math.floor(roll*RangedLoadoutEffects.crystalAccuracy(gear,weapon));
           roll=WikiRangedEffects.accuracyAfterExclusive(roll,gear.values(),m,task,slayer);
           roll=WikiRangedEffects.postAccuracy(roll,weapon,m,st,4);
           double acc=roll>defRoll?1-(defRoll+2.0)/(2*(roll+1.0)):roll/(2.0*(defRoll+1.0)); acc=Math.max(0,Math.min(1,acc));
           int dmgEff=CombatEffectiveLevels.rangedDamage(level,p.getRangedStrength(),st.getRangedBoost(),voidRanged,eliteVoid);
           int max=(int)Math.floor(0.5+dmgEff*(str+64)/640.0); max=(int)Math.floor(max*RangedLoadoutEffects.crystalDamage(gear,weapon));
           max=WikiRangedEffects.damageAfterExclusive(max,gear.values(),m,weapon,task,slayer);
           max=WikiRangedEffects.postDamage(max,weapon,m,task,slayer); int actualSpeed=Math.max(1,speed+st.getSpeedDelta());
           double expected;
           if(EnchantedBoltEffects.damageEffectModelled(ammo)&&!CorporealBeastRules.isCorp(m))
            expected=fastBoltExpected(ammo,m,level,max,acc,account.isKandarinHardCompleted());
           else if(requiresDistributionBeamScore(weapon,ammo,m))
            expected=WikiPlayerVsNpcCalc.rangedAttacker(weapon,ammo,m,level,max,acc,account.isKandarinHardCompleted(),gear.values()).expectedDamage();
           else
            // Exact expected value of Wiki's ordinary linear 0..max distribution after
            // accurate-zero becomes one. Avoid allocating/flattening a distribution for
            // the tens of thousands of ordinary partial beam states.
            expected=acc*((max*(max+1.0)/2.0+1.0)/(max+1.0));
           double d=expected/(actualSpeed*0.6); if(d>best)best=d;
          } return best;
         }
    }

    private static EquipmentCandidate asLoadedDart(EquipmentCandidate dart)
    {
        return new EquipmentCandidate(dart.getItem(), EquipmentSlot.AMMO, dart.getRangedStrength(),
            0, 0, 0, dart.getRangedStrength(), 0, 0f, 0, 0, false, "Loaded dart", dart.getRequirementResult());
    }

    private static boolean requiresDistributionBeamScore(EquipmentCandidate weapon,EquipmentCandidate ammo,MonsterDefinition monster){
     if(CorporealBeastRules.isCorp(monster))return true;
     String a=ammo==null||ammo.getItem()==null?"":ItemNames.lower(ammo);
     if(a.contains("seeking"))return true;
     String w=weapon==null||weapon.getItem()==null?"":ItemNames.lower(weapon);
     return w.equals("dark bow")||w.contains("tonalztics")||w.equals("karil's crossbow");
    }
    private static double fastBoltExpected(EquipmentCandidate ammo,MonsterDefinition m,int level,int max,double accuracy,boolean diary){
     String n=ItemNames.lower(ammo);double pScale=diary?1.10:1.0;
     double ordinary=successfulAverage(max),base=accuracy*ordinary;
     if(n.contains("ruby")){double p=.06*pScale,effect=Math.min(100.0,Math.floor(m.getHitpoints()*.20));return p*effect+(1.0-p)*base;}
     if(n.contains("diamond")){double p=.10*pScale,effect=successfulAverage((int)Math.floor(max*1.15));return p*effect+(1.0-p)*base;}
     if(n.contains("opal")){double p=.05*pScale;return base+p*Math.floor(level/10.0);}
     if(n.contains("pearl")){double p=.06*pScale;return base+p*Math.floor(level/(m.hasAttribute("fiery")?15.0:20.0));}
     if(n.contains("dragonstone")&&!dragonstoneBlocked(m)){double p=.06*pScale;return base+accuracy*p*Math.floor(level*.20);}
     if(n.contains("onyx")&&!m.hasAttribute("undead")){double p=.11*pScale,effect=successfulAverage((int)Math.floor(max*1.20));return accuracy*((1.0-p)*ordinary+p*effect);}
     return base;
    }
    private static double successfulAverage(int max){return (max*(max+1.0)/2.0+1.0)/(max+1.0);}
    private static boolean dragonstoneBlocked(MonsterDefinition m){return m!=null&&(m.hasAttribute("dragonfire immune")||m.hasAttribute("dragon")||m.hasAttribute("fiery"));}
    private RangedPrayer[] fastPrayers(){int x=account.real(Skill.PRAYER);
     // DPS is the sole search objective.  A stronger unlocked offensive prayer is
     // monotonic in both accuracy and max hit, so evaluating NONE as well cannot
     // change the winning gear state and merely doubles every beam-score pass.
     if(x>=74&&account.isRigourUnlocked())return new RangedPrayer[]{RangedPrayer.RIGOUR};
     if(x>=62&&account.isDeadeyeUnlocked())return new RangedPrayer[]{RangedPrayer.DEADEYE};
     if(x>=44)return new RangedPrayer[]{RangedPrayer.EAGLE_EYE}; if(x>=26)return new RangedPrayer[]{RangedPrayer.HAWK_EYE};
     if(x>=8)return new RangedPrayer[]{RangedPrayer.SHARP_EYE}; return new RangedPrayer[]{RangedPrayer.NONE};
    }

    private static String combatStateKey(Map<EquipmentSlot,EquipmentCandidate> g){
     int atk=0,str=0; StringBuilder flags=new StringBuilder();
     for(EquipmentCandidate c:g.values()){if(c==null)continue;atk+=c.getRangedAttack();str+=c.getRangedStrength();}
     flags.append(atk).append('/').append(str);
     if(EquipmentSetRules.strengthRanged(g.get(EquipmentSlot.WEAPON)))for(EquipmentSlot slot:EquipmentSlot.values()){
      EquipmentCandidate c=g.get(slot);flags.append('/').append(c==null?0:c.getStrengthBonus());}
     EquipmentCandidate cape=g.get(EquipmentSlot.CAPE);
     if(cape!=null&&ItemNames.lower(cape).contains("dizana"))flags.append("|quiver:").append(WikiEquipmentEngine.canonicalId(cape.getItem().getItemId()));
     // Weapon/ammo identity can change speed, projectile class and special effects.
     appendIdentity(flags,g.get(EquipmentSlot.WEAPON)); appendIdentity(flags,g.get(EquipmentSlot.AMMO));
     // Preserve slot-specific identities only where current or future set/mechanic
     // completion can alter DPS. Ordinary same-stat gear is safely equivalent.
     appendMechanic(flags,g.get(EquipmentSlot.HEAD)); appendMechanic(flags,g.get(EquipmentSlot.BODY));
     appendMechanic(flags,g.get(EquipmentSlot.LEGS)); appendMechanic(flags,g.get(EquipmentSlot.HANDS));
     appendMechanic(flags,g.get(EquipmentSlot.NECK));
     return flags.toString();
    }
    private static void appendIdentity(StringBuilder s,EquipmentCandidate c){s.append('|').append(c==null?"-":c.getItem().getItemId()+":"+ItemNames.lower(c));}

    private static void appendMechanic(StringBuilder s, EquipmentCandidate c)
    {
        s.append('|').append(mechanicGroup(c));
    }

    /** The item's own name when its identity can change a ranged set or effect bonus, otherwise a shared group. */
    private static String mechanicGroup(EquipmentCandidate c)
    {
        if (c == null) return "-";
        String n = ItemNames.lower(c);
        for (String item : MECHANIC_ITEMS) if (n.contains(item)) return n;
        return "_";
    }
}
