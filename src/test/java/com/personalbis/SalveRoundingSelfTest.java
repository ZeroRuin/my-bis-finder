package com.personalbis;

import java.util.*;

/** Salve's integer checkpoint and the screenshot's ordinary Fang DPS boundary. */
public final class SalveRoundingSelfTest
{
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static void close(double actual,double expected){if(Math.abs(actual-expected)>1e-9)throw new AssertionError(actual+" != "+expected);}
    private static Map<EquipmentSlot,EquipmentCandidate> neck(String name){
        Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.NECK,new EquipmentCandidate(new BankItem(12018,1,name),EquipmentSlot.NECK,0,0,0,0,0,0,0,0,0,false,"",RequirementResult.usable("fixture")));
        return gear;
    }
    public static void main(String[] args){
        MonsterDefinition undead=new MonsterDefinition(8061,"Vorkath",732,214,150,100,750,0,0,0,0,0,0,0,Arrays.asList("undead"),3,"",0);
        MonsterDefinition living=new MonsterDefinition(1,"Living",1,1,1,1,1);
        for(String name:new String[]{"Salve amulet (e)","Salve amulet(ei)","Salve amulet","Salve amulet(i)"}){
            Map<EquipmentSlot,EquipmentCandidate> gear=neck(name);
            int numerator=name.contains("(e)")||name.contains("(ei)")?6:7;
            int denominator=numerator==6?5:6;
            for(int max=0;max<=150;max++)check(MeleeLoadoutEffects.salveMaxHit(max,gear,undead)==max*numerator/denominator,"integer Salve "+name);
            check(MeleeLoadoutEffects.salveMaxHit(43,gear,living)==43,"non-undead control");
        }
        Map<EquipmentSlot,EquipmentCandidate> enhanced=neck("Salve amulet(ei)");
        int base=WikiIntegerStages.maxHitFromEffective(132,148+64);
        check(base==44,"base-hit truncation");
        check(MeleeLoadoutEffects.salveMaxHit(base,enhanced,undead)==52,"Salve cannot restore discarded fraction");
        check((int)(((132*(148+64)+320.0)/640)*1.2)==53,"old ordering counterexample");
        double chance=WikiAccuracyRoll.fang(29311,20070);
        close(chance,.8437002370446669);
        // Verified share ElysianBurningPool: Attack92, Strength99, Piety, Lunge;
        // the Wiki selected the uncharged Serp (0 Strength), total gear Strength143.
        int effectiveAttack=CombatEffectiveLevels.melee(92,1.20,0,false);
        int effectiveStrength=CombatEffectiveLevels.melee(99,1.23,3,false);
        check(MeleeLoadoutEffects.salveAttackRoll(effectiveAttack*(143+64),enhanced,undead)==29311,"share attack roll");
        int unchargedBase=WikiIntegerStages.maxHitFromEffective(effectiveStrength,143+64);
        int chargedBase=WikiIntegerStages.maxHitFromEffective(effectiveStrength,148+64);
        int faceguardBase=WikiIntegerStages.maxHitFromEffective(effectiveStrength,149+64);
        check(unchargedBase==43&&chargedBase==44&&faceguardBase==44,"share charge-state and Faceguard strength thresholds");
        HitDistribution serp=MeleeWeaponEffects.fangDistribution(MeleeLoadoutEffects.salveMaxHit(unchargedBase,enhanced,undead),chance);
        HitDistribution faceguard=MeleeWeaponEffects.fangDistribution(MeleeLoadoutEffects.salveMaxHit(faceguardBase,enhanced,undead),chance);
        check(serp.max()==44&&faceguard.max()==45,"Serp/Faceguard screenshot max boundaries");
        close(serp.expectedHit()/3,7.171452014879671);
        close(faceguard.expectedHit()/3,7.312068721053778);
        HitDistribution charged=MeleeWeaponEffects.fangDistribution(MeleeLoadoutEffects.salveMaxHit(chargedBase,enhanced,undead),chance);
        close(charged.expectedHit(),faceguard.expectedHit());
        check(charged.max()==45,"charged Serp ties Faceguard at this threshold");
        double unchargedTtk=ExpectedKillTime.seconds(new HitDistribution.AttackDistribution(Collections.singletonList(serp)),750,5);
        double chargedTtk=ExpectedKillTime.seconds(new HitDistribution.AttackDistribution(Collections.singletonList(charged)),750,5);
        check(Math.abs(unchargedTtk-106.6)<.05&&Math.abs(chargedTtk-104.6)<.05,"share finite TTK figures");
        DefenceFillPolicy.Offence better=new DefenceFillPolicy.Offence(chance,45,5,faceguard.expectedHit()/3,104.6,29311,52);
        check(!better.sameAs(new DefenceFillPolicy.Offence(chance,44,5,serp.expectedHit()/3,106.6,29311,51)),"Defence fill rejects lower-DPS Serp outcome");
        System.out.println("PASS staged Salve rounding, exact Wiki share rolls/DPS/TTK, charge states and defence-fill guard");
    }
}
