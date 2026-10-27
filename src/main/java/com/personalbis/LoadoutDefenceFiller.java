package com.personalbis;

import java.util.*;
import java.util.function.Function;
import com.personalbis.wiki.WikiEquipment;

/** A post-search pass over the full owned candidate list, including occupied slots.
 * Weapon and ammo stay fixed; every accepted change preserves the original offence.
 */
final class LoadoutDefenceFiller
{
    private LoadoutDefenceFiller() {}
    private static DefenceFillPolicy.Offence offence(CombatResult r)
    {return r==null?null:new DefenceFillPolicy.Offence(r.getAccuracy(),r.getMaxHit(),r.getAttackSpeed(),r.getDps(),r.getExpectedTtkSeconds(),r.getAttackRoll(),r.getPreWeaponMaxHit());}
    private static DefenceFillPolicy.Offence offence(RangedCombatResult r)
    {return r==null?null:new DefenceFillPolicy.Offence(r.getAccuracy(),r.getMaxHit(),r.getAttackSpeed(),r.getDps(),r.getExpectedTtkSeconds(),r.getAttackRoll(),r.getPreTransformMax());}
    private static DefenceFillPolicy.Offence offence(MagicCombatResult r)
    {return r==null?null:new DefenceFillPolicy.Offence(r.getAccuracy(),r.getMaxHit(),r.getAttackSpeed(),r.getDps(),r.getExpectedTtkSeconds(),r.getAttackRoll(),r.getPreTransformMax());}

    private static int[] defence(EquipmentCandidate candidate)
    {
        if(candidate==null)return new int[5];
        WikiEquipment w=WikiEquipmentEngine.canonicalItem(candidate.getItem().getItemId());
        if(w==null||w.defensive==null)return null;
        return new int[]{w.defensive.stab,w.defensive.slash,w.defensive.crush,w.defensive.magic,w.defensive.ranged};
    }

    private static Map<EquipmentSlot,EquipmentCandidate> fill(Map<EquipmentSlot,EquipmentCandidate> original,
        Map<EquipmentSlot,List<EquipmentCandidate>> ranked,MonsterDefinition target,
        DefenceFillPolicy.Offence baseline,Function<Map<EquipmentSlot,EquipmentCandidate>,DefenceFillPolicy.Offence> calculate)
    {
        Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);gear.putAll(original);
        EquipmentCandidate weapon=gear.get(EquipmentSlot.WEAPON);
        EquipmentSlot required=SlayerTargetRequirements.requiredEquipmentSlot(target);
        for(EquipmentSlot slot:EquipmentSlot.values())
        {
            if(slot==EquipmentSlot.WEAPON||slot==EquipmentSlot.AMMO||slot==required)continue;
            if(slot==EquipmentSlot.SHIELD&&weapon!=null&&RangedAmmoRules.occupiesBothHands(weapon))continue;
            EquipmentCandidate current=gear.get(slot);
            int[] currentDefence=defence(current);
            // Do not invent defensive stats for an unrecognised item.
            if(currentDefence==null)continue;
            for(EquipmentCandidate candidate:ranked.getOrDefault(slot,Collections.emptyList()))
            {
                if(candidate==null||candidate.getRequirementResult().isBlocked()||candidate.getItem().getQuantity()<=0)continue;
                int[] nextDefence=defence(candidate);if(nextDefence==null)continue;
                if(DefenceFillPolicy.compare(nextDefence,candidate.getPrayer(),currentDefence,current==null?0:current.getPrayer(),DefenceFillPolicy.profile(target))<=0)continue;
                Map<EquipmentSlot,EquipmentCandidate> trial=new EnumMap<>(gear);trial.put(slot,candidate);
                if(!baseline.sameAs(calculate.apply(trial)))continue;
                gear=trial;current=candidate;currentDefence=nextDefence;
            }
        }
        return gear;
    }

    static OptimizedLoadout melee(OptimizedLoadout load,Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
        MonsterDefinition target,boolean task,AccountSnapshot account)
    {
        if(load==null)return null;
        CombatResult base=load.getResult();MeleeCombatCalculator calculator=new MeleeCombatCalculator(account);
        Function<Map<EquipmentSlot,EquipmentCandidate>,CombatResult> evaluate=gear->calculator.calculateForStance(gear,load.getStyle(),target,task,base.getStance(),base.getPrayer());
        Map<EquipmentSlot,EquipmentCandidate> gear=fill(load.getItems(),ranked,target,offence(base),trial->offence(evaluate.apply(trial)));
        CombatResult verified=evaluate.apply(gear);
        return offence(base).sameAs(offence(verified))?new OptimizedLoadout(load.getStyle(),gear,verified):load;
    }

    static OptimizedRangedLoadout ranged(OptimizedRangedLoadout load,Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
        MonsterDefinition target,boolean task,AccountSnapshot account)
    {
        if(load==null)return null;
        RangedCombatResult base=load.getResult();RangedCombatCalculator calculator=new RangedCombatCalculator(account);
        Function<Map<EquipmentSlot,EquipmentCandidate>,RangedCombatResult> evaluate=gear->{
            Map<EquipmentSlot,EquipmentCandidate> calculation=new EnumMap<>(gear);
            if(load.getLoadedAmmo()!=null)calculation.put(EquipmentSlot.AMMO,load.getLoadedAmmo());
            return calculator.calculate(calculation,target,task,base.getStance(),base.getPrayer());
        };
        Map<EquipmentSlot,EquipmentCandidate> gear=fill(load.getItems(),ranked,target,offence(base),trial->offence(evaluate.apply(trial)));
        RangedCombatResult verified=evaluate.apply(gear);
        return offence(base).sameAs(offence(verified))?new OptimizedRangedLoadout(gear,verified,load.getLoadedAmmo()):load;
    }

    static OptimizedMagicLoadout magic(OptimizedMagicLoadout load,Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
        MonsterDefinition target,boolean task,AccountSnapshot account,List<BankItem> owned)
    {
        if(load==null)return null;
        EquipmentCandidate weapon=load.getItems().get(EquipmentSlot.WEAPON);
        PoweredStaff staff=weapon==null?null:PoweredStaff.forWeapon(weapon.getItem().getName());
        if(staff==null&&load.getSpell()==null)return load;
        MagicCombatCalculator calculator=new MagicCombatCalculator(account);
        Function<Map<EquipmentSlot,EquipmentCandidate>,MagicCombatResult> evaluate=gear->{
            if(staff==null&&!MagicLoadoutOptimizer.runesAvailable(load.getSpell(),owned,weapon,gear.values()))return null;
            return staff==null?calculator.calculate(gear,target,load.getSpell(),task):calculator.calculatePowered(gear,target,staff,task);
        };
        Map<EquipmentSlot,EquipmentCandidate> gear=fill(load.getItems(),ranked,target,offence(load.getResult()),trial->offence(evaluate.apply(trial)));
        MagicCombatResult verified=evaluate.apply(gear);
        return offence(load.getResult()).sameAs(offence(verified))?new OptimizedMagicLoadout(gear,verified,load.getSpell()):load;
    }
}
