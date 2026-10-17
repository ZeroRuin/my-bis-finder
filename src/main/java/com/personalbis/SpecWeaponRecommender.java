package com.personalbis;

import java.util.*;
import net.runelite.api.Skill;
import com.personalbis.wiki.WikiEquipment;

/** Scores a weapon/ammo switch in the displayed armour, separately from auto-attack optimization. */
final class SpecWeaponRecommender
{
    private static final List<String> RANGED=Arrays.asList("Magic shortbow","Magic shortbow (i)","Dark bow","Toxic blowpipe","Webweaver bow","Heavy ballista","Light ballista","Dragon knife","Zaryte crossbow");
    private static final List<String> MAGIC=Arrays.asList("Eye of Ayak","Volatile Nightmare staff","Eldritch Nightmare staff");
    private final AccountSnapshot account;
    SpecWeaponRecommender(AccountSnapshot account){this.account=account;}
    static String name(EquipmentCandidate c){WikiEquipment w=WikiEquipmentEngine.canonicalItem(c.getItem().getItemId());return w==null?c.getItem().getName():w.name;}
    SpecWeaponRecommendation best(Map<EquipmentSlot,EquipmentCandidate> worn,
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> owned,AttackStyle selected,MonsterDefinition target,boolean task)
    {
        if(worn.isEmpty()||TargetStyleImmunity.immune(target,selected))return null;
        SpecWeaponRecommendation best=null;
        Set<String> omitted=new TreeSet<>();
        AttackStyle[] styles=selected==AttackStyle.MAGIC||selected==AttackStyle.RANGED?new AttackStyle[]{selected}
            :new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH};
        for(AttackStyle style:styles){
            Map<EquipmentSlot,List<EquipmentCandidate>> ranked=owned.get(style);
            if(ranked==null)continue;
            for(EquipmentCandidate weapon:ranked.getOrDefault(EquipmentSlot.WEAPON,Collections.emptyList())){
                if(weapon.getItem().getQuantity()<=0||weapon.getRequirementResult().isBlocked())continue;
                String weaponName=name(weapon);
                boolean supported=style==AttackStyle.MAGIC?MAGIC.contains(weaponName):style==AttackStyle.RANGED?RANGED.contains(weaponName):SpecialAttackRules.MELEE.contains(weaponName);
                if(!supported){if(WikiEquipmentEngine.WEAPON_SPEC_COSTS.containsKey(weaponName))omitted.add(weaponName);continue;}
                Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);
                for(Map.Entry<EquipmentSlot,EquipmentCandidate> slot:worn.entrySet()){
                    for(EquipmentCandidate c:ranked.getOrDefault(slot.getKey(),Collections.emptyList()))
                        if(c.getItem().getItemId()==slot.getValue().getItem().getItemId()){gear.put(slot.getKey(),c);break;}
                }
                gear.put(EquipmentSlot.WEAPON,weapon);
                if(weapon.isTwoHanded())gear.remove(EquipmentSlot.SHIELD);
                List<EquipmentCandidate> ammo=new ArrayList<>();
                if(style==AttackStyle.RANGED&&(RangedAmmoRules.requiresEquippedAmmo(weapon)||RangedAmmoRules.usesLoadedDarts(weapon))){
                    for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.AMMO,EquipmentSlot.WEAPON})
                        for(EquipmentCandidate c:ranked.getOrDefault(slot,Collections.emptyList()))
                            if(!c.getRequirementResult().isBlocked()&&c.getItem().getQuantity()>0&&RangedAmmoRules.compatible(weapon,c))ammo.add(c);
                    if(ammo.isEmpty())continue;
                }else ammo.add(null);
                for(EquipmentCandidate a:ammo){
                    if(a!=null)gear.put(EquipmentSlot.AMMO,a);
                    SpecWeaponRecommendation r=style==AttackStyle.MAGIC?magic(gear,target,task)
                        :style==AttackStyle.RANGED?ranged(gear,target,task):melee(gear,style,target,task);
                    if(r!=null&&r.dps>0&&Double.isFinite(r.dps)&&(best==null||r.dps>best.dps))best=r;
                }
            }
        }
        return best==null?null:best.withCoverage(String.join(", ",omitted));
    }
    private SpecWeaponRecommendation result(EquipmentCandidate w,HitDistribution.AttackDistribution d,int ticks,AttackStyle style,
        Map<EquipmentSlot,EquipmentCandidate> gear,String note){
        String n=name(w);int cost=WikiEquipmentEngine.WEAPON_SPEC_COSTS.getOrDefault(n,50);
        EquipmentCandidate ammo=gear.get(EquipmentSlot.AMMO);
        if(ammo!=null&&style==AttackStyle.RANGED)note+=" | Ammo: "+ammo.getItem().getName();
        return new SpecWeaponRecommendation(w,ammo,d.expectedDamage(),d.max(),cost,ticks,style.toString(),note);
    }
    private MeleePrayer meleePrayer(){
        int p=account.real(Skill.PRAYER);
        if(p>=70&&account.isPietyUnlocked())return MeleePrayer.PIETY;
        if(p>=60&&account.isChivalryUnlocked())return MeleePrayer.CHIVALRY;
        return p>=44?MeleePrayer.HIGH:p>=31?MeleePrayer.MID:p>=13?MeleePrayer.LOW:MeleePrayer.NONE;
    }
    private RangedPrayer rangedPrayer(){
        int p=account.real(Skill.PRAYER);
        if(p>=74&&account.isRigourUnlocked())return RangedPrayer.RIGOUR;
        if(p>=62&&account.isDeadeyeUnlocked())return RangedPrayer.DEADEYE;
        return p>=44?RangedPrayer.EAGLE_EYE:p>=26?RangedPrayer.HAWK_EYE:p>=8?RangedPrayer.SHARP_EYE:RangedPrayer.NONE;
    }
    private SpecWeaponRecommendation melee(Map<EquipmentSlot,EquipmentCandidate> gear,AttackStyle style,MonsterDefinition m,boolean task){
        EquipmentCandidate w=gear.get(EquipmentSlot.WEAPON);String n=name(w);
        if(!SpecialAttackRules.MELEE.contains(n)||!MeleeAttackStyleResolver.supports(w,style)
            ||!WikiTargetEligibility.meleeCanDamage(m,w)||!LeafyTargetRules.meleeCanDamage(m,w)||!VampyreTargetRules.canDamageMelee(gear,w,m))return null;
        SpecWeaponRecommendation best=null;
        for(MeleeStance stance:MeleeAttackStyleResolver.legalStances(w,style)){
            CombatResult base=new MeleeCombatCalculator(account).calculateForStance(gear,style,m,task,stance,meleePrayer());
            int roll=base.getAttackRoll()*SpecialAttackRules.attackPercent(n)/100;
            double accuracy=WikiAccuracyRoll.normal(roll,base.getDefenceRoll());
            if(n.equals("Osmumten's fang")&&style==AttackStyle.MELEE_STAB)
                accuracy=RaidScaling.isToa(m)?1-Math.pow(1-accuracy,2):WikiAccuracyRoll.fang(roll,base.getDefenceRoll());
            if(RaidCombatRules.core(m))accuracy=1;
            int max=(int)Math.floor(base.getPreWeaponMaxHit()*MeleeLoadoutEffects.inquisitorMultiplier(gear,style));
            HitDistribution.AttackDistribution d=SpecialAttackRules.melee(n,max,accuracy,
                WikiAccuracyRoll.normal(roll*3/4,base.getDefenceRoll()),m==null?1:m.getSize());
            AttackStyle damageStyle=n.equals("Voidwaker")?AttackStyle.MAGIC:style;
            if(n.equals("Voidwaker")&&!WikiTargetEligibility.magicCanDamage(m))continue;
            if(RaidCombatRules.core(m))d=SpecialAttackRules.single(HitDistribution.single(d.max(),true));
            d=WikiPlayerVsNpcCalc.applyCorp(d,m,w,damageStyle);
            d=WikiPlayerVsNpcCalc.finalDistribution(d,m,damageStyle,w,null,false,gear.values());
            if(VampyreTargetRules.tier2HalfDamage(gear,w,m))d=d.transform(HitDistribution.divide(2,0),true);
            SpecWeaponRecommendation r=result(w,d,base.getAttackSpeed(),style,gear,"Special stance: "+style+" / "+stance+"; no utility/drain value");
            if(best==null||r.dps>best.dps)best=r;
        }
        return best;
    }
    private SpecWeaponRecommendation magic(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
        EquipmentCandidate w=gear.get(EquipmentSlot.WEAPON);String n=name(w);
        if(!MAGIC.contains(n)||!WikiTargetEligibility.magicCanDamage(m)||!LeafyTargetRules.poweredMagicCanDamage(m))return null;
        int level=Math.max(1,RaidScaling.boosted(account,Skill.MAGIC,m));
        int base=n.equals("Eye of Ayak")?Math.max(1,level/3-6)*13/10
            :n.startsWith("Volatile")?Math.max(1,Math.min(58,(99+58*level)/99)):Math.max(1,Math.min(44,(99+44*level)/99));
        int bonus=0;double damage=0;for(EquipmentCandidate c:gear.values()){bonus+=c.getMagicAttack();damage+=c.getMagicDamage();}
        WikiEquipmentEngine.Totals totals=WikiEquipmentEngine.transform(gear,m,null,0,0,bonus,damage,0);
        MagicPrayer prayer=MagicPrayer.best(account);
        int roll=WikiMagicAccuracy.calculate(level,prayer.accuracy,totals.magicAttack,gear.values(),m,null,task).attackRoll;
        roll=roll*(n.equals("Eye of Ayak")?200:n.startsWith("Volatile")?150:100)/100;
        int defence=RaidScaling.defenceRoll(m,m==null?640:(Math.max(1,m.getMagicLevel())+9)*(m.getMagicDefence()+64));
        int max=WikiMagicDamage.powered(base,totals.magicDamage,prayer.damage,gear.values(),m,task).maxHit;
        double accuracy=WikiAccuracyRoll.normal(roll,defence);int min=0;
        if(RaidCombatRules.wardenP2(m)){int[] bounds=RaidCombatRules.wardenBounds(max,roll,defence);min=bounds[0];max=bounds[1];accuracy=1;}
        Double brimstone=null;
        if(!RaidCombatRules.wardenP2(m))for(EquipmentCandidate c:gear.values())if(name(c).equals("Brimstone ring"))brimstone=WikiAccuracyRoll.normal(roll,defence*9/10);
        HitDistribution.AttackDistribution d=WikiPlayerVsNpcCalc.magicFinalDistribution(min,max,accuracy,brimstone,m,gear.values(),null);
        return result(w,d,5,AttackStyle.MAGIC,gear,"Built-in special spell; direct damage only");
    }
    private SpecWeaponRecommendation ranged(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
        EquipmentCandidate w=gear.get(EquipmentSlot.WEAPON),a=gear.get(EquipmentSlot.AMMO);String n=name(w);
        if(!RANGED.contains(n)
            ||!WikiTargetEligibility.rangedCanDamage(m)||!LeafyTargetRules.rangedCanDamage(m,a)||!VampyreTargetRules.canDamageRanged(gear,w,a,m))return null;
        SpecWeaponRecommendation best=null;
        for(RangedStance stance:RangedStance.values()){
            RangedCombatResult base=new RangedCombatCalculator(account).calculate(gear,m,task,stance,rangedPrayer());
            int roll=base.getAttackRoll();
            if(n.startsWith("Magic shortbow"))roll=roll*10/7;
            else if(n.contains("ballista"))roll=roll*5/4;
            else if(n.equals("Toxic blowpipe")||n.equals("Webweaver bow")||n.equals("Zaryte crossbow"))roll*=2;
            double accuracy=WikiAccuracyRoll.normal(roll,base.getDefenceRoll());int max=base.getPreTransformMax(),min=0,hits=1;
            if(n.startsWith("Magic shortbow")){
                if(a==null)continue;
                max=WikiIntegerStages.maxHitFromEffective(RaidScaling.boosted(account,Skill.RANGED,m)+10,a.getRangedStrength()+64);hits=2;
            }else if(n.equals("Toxic blowpipe"))max=max*3/2;
            else if(n.contains("ballista"))max=max*5/4;
            else if(n.equals("Webweaver bow")){max-=max*6/10;hits=4;}
            else if(n.equals("Dragon knife"))hits=2;
            else if(n.equals("Dark bow")){boolean dragon=a!=null&&a.getItem().getName().toLowerCase().contains("dragon arrow");min=dragon?8:5;max=Math.min(48,max*(dragon?15:13)/10);hits=2;}
            if(RaidCombatRules.wardenP2(m)){int[] bounds=RaidCombatRules.wardenBounds(max,roll,base.getDefenceRoll());min=bounds[0];max=bounds[1];accuracy=1;}
            HitDistribution hit=n.equals("Zaryte crossbow")
                ?SpecialBoltRules.distribution(a,m,RaidScaling.boosted(account,Skill.RANGED,m),min,max,accuracy,account.isKandarinHardCompleted(),CorporealBeastRules.isCorp(m))
                :HitDistribution.linear(accuracy,min,Math.max(min,max)).accurateZeroToOne();
            List<HitDistribution> splats=new ArrayList<>();for(int i=0;i<hits;i++)splats.add(hit);
            HitDistribution.AttackDistribution d=new HitDistribution.AttackDistribution(splats);
            if(!n.equals("Zaryte crossbow"))d=WikiPlayerVsNpcCalc.applyCorp(d,m,w,AttackStyle.RANGED);
            d=WikiPlayerVsNpcCalc.finalDistribution(d,m,AttackStyle.RANGED,w,null,false,gear.values());
            SpecWeaponRecommendation r=result(w,d,base.getAttackSpeed(),AttackStyle.RANGED,gear,"Special stance: "+stance+"; no healing/burn value; ruby uses full target HP");
            if(best==null||r.dps>best.dps)best=r;
        }
        return best;
    }
}
