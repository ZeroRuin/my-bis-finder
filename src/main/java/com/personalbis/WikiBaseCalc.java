package com.personalbis;

import java.util.*;

/**
 * Structural Java parity layer for reusable mechanics in the OSRS Wiki DPS
 * calculator src/lib/BaseCalc.ts. Stateful UI sanitisation remains in PBIS,
 * while deterministic calculation helpers live here.
 *
 * Upstream: weirdgloop/osrs-dps-calc (GPL-3.0).
 */
public final class WikiBaseCalc {
    private WikiBaseCalc() {}

    public static int factor(int base,int numerator,int denominator){return WikiIntegerStages.factor(base,numerator,denominator);}
    public static int maxHitFromEffective(int effectiveLevel,int gearBonus){return WikiIntegerStages.maxHitFromEffective(effectiveLevel,gearBonus);}
    public static int add(int base,double addend){return WikiIntegerStages.add(base,addend);}
    public static int addFactor(int base,int numerator,int denominator){return base + factor(base,numerator,denominator);}

    public static double normalAccuracyRoll(int atk,int def){return WikiAccuracyRoll.normal(atk,def);}
    public static double fangAccuracyRoll(int atk,int def){return WikiAccuracyRoll.fang(atk,def);}

    /** BaseCalc.getConflictionGauntletsAccuracyRoll. */
    public static double conflictionGauntletsAccuracyRoll(int atk,int def){
        double single=normalAccuracyRoll(atk,def);
        double doubled=fangAccuracyRoll(atk,def);
        return doubled/(1.0+doubled-single);
    }

    /** BaseCalc.getFixedAttackHitChance: fixed player roll vs random NPC defence roll. */
    public static double fixedAttackHitChance(int attackRoll,int defenceRoll){
        int atk=normalizeNegative(attackRoll), def=normalizeNegative(defenceRoll);
        if(atk>=0&&def>=0)return atk>def?1.0:atk/(def+1.0);
        if(atk>=0)return 1.0;
        if(def>=0)return 0.0;
        int a=-def,d=-atk;
        return a>d?1.0:a/(d+1.0);
    }
    private static int normalizeNegative(int v){return v<0?Math.min(0,v+2):v;}

    public static boolean wearing(Collection<String> equipped,String... any){
        if(equipped==null)return false;
        for(String s:any)if(equipped.contains(s))return true;
        return false;
    }
    public static boolean wearingAll(Collection<String> equipped,String... all){
        if(equipped==null)return false;
        for(String s:all)if(!equipped.contains(s))return false;
        return true;
    }
    public static boolean isUsingMeleeStyle(AttackStyle style){
        return style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH;
    }
    public static boolean isWearingVoidRobes(Collection<String> e){
        return wearing(e,"Void knight top","Void knight top (or)","Elite void top","Elite void top (or)")
            && wearing(e,"Void knight robe","Void knight robe (or)","Elite void robe","Elite void robe (or)")
            && wearing(e,"Void knight gloves");
    }
    public static boolean isWearingEliteVoidRobes(Collection<String> e){
        return wearing(e,"Elite void top","Elite void top (or)")&&wearing(e,"Elite void robe","Elite void robe (or)")&&wearing(e,"Void knight gloves");
    }
    public static boolean isWearingSmokeStaff(Collection<String> e){return wearing(e,"Smoke battlestaff","Mystic smoke staff","Twinflame staff");}
    public static boolean isWearingTzhaarWeapon(Collection<String> e){return wearing(e,"Tzhaar-ket-em","Tzhaar-ket-om","Tzhaar-ket-om (t)","Toktz-xil-ak","Toktz-xil-ek","Toktz-mej-tal");}
    public static boolean isWearingObsidian(Collection<String> e){return wearingAll(e,"Obsidian helmet","Obsidian platelegs","Obsidian platebody");}
    public static boolean isWearingBerserkerNecklace(Collection<String> e){return wearing(e,"Berserker necklace","Berserker necklace (or)");}
    public static boolean isWearingCrystalBow(Collection<String> e){
        if(wearing(e,"Crystal bow"))return true;
        if(e!=null)for(String s:e)if(s!=null&&s.contains("Bow of Faerdhinen"))return true;
        return false;
    }
    public static boolean isWearingFang(Collection<String> e){return wearing(e,"Osmumten's fang","Osmumten's fang (or)");}
    public static boolean isWearingBlowpipe(Collection<String> e){return wearing(e,"Toxic blowpipe","Blazing blowpipe");}
    public static boolean isWearingGodsword(Collection<String> e){return wearing(e,"Ancient godsword","Armadyl godsword","Bandos godsword","Saradomin godsword","Zamorak godsword");}
    public static boolean isWearingScythe(Collection<String> e){
        if(wearing(e,"Scythe of Vitur"))return true;
        if(e!=null)for(String s:e)if(s!=null&&(s.contains("of vitur")||s.contains("of Vitur")))return true;
        return false;
    }
    public static boolean isWearingTwoHitWeapon(Collection<String> e){return wearing(e,"Torag's hammers","Sulphur blades","Glacial temotli","Earthbound tecpatl");}
    public static boolean isWearingKeris(Collection<String> e){if(e!=null)for(String s:e)if(s!=null&&s.contains("Keris"))return true;return false;}
    public static boolean isWearingRatBoneWeapon(Collection<String> e){return wearing(e,"Bone mace","Bone shortbow","Bone staff");}
    public static boolean isWearingJusticiarArmour(Collection<String> e){return wearingAll(e,"Justiciar faceguard","Justiciar chestguard","Justiciar legguards");}
    public static boolean isUsingAbyssal(Collection<String> e,AttackStyle style){return isUsingMeleeStyle(style)&&wearing(e,"Abyssal bludgeon","Abyssal dagger","Abyssal whip","Abyssal tentacle");}
    public static boolean isWearingOgreBow(Collection<String> e){return wearing(e,"Ogre bow","Comp ogre bow");}

    /** Exact BaseCalc Charge-spell cape gate. */
    public static boolean chargeSpellApplicable(boolean chargeBuff,String spell,Collection<String> e){
        if(!chargeBuff||spell==null)return false;
        switch(spell){
            case "Saradomin Strike": return wearing(e,"Saradomin cape","Imbued Saradomin cape","Saradomin max cape","Imbued Saradomin max cape");
            case "Claws of Guthix": return wearing(e,"Guthix cape","Imbued Guthix cape","Guthix max cape","Imbued Guthix max cape");
            case "Flames of Zamorak": return wearing(e,"Zamorak cape","Imbued Zamorak cape","Zamorak max cape","Imbued Zamorak max cape");
            default:return false;
        }
    }
    public static boolean matchingElementalAmulet(String element,Collection<String> e){
        if(element==null)return false;
        if(wearing(e,"Elemental amulet"))return true;
        switch(element.toLowerCase(Locale.ROOT)){
            case "air":return wearing(e,"Amulet of air"); case "water":return wearing(e,"Amulet of water");
            case "earth":return wearing(e,"Amulet of earth"); case "fire":return wearing(e,"Amulet of fire"); default:return false;
        }
    }
}
