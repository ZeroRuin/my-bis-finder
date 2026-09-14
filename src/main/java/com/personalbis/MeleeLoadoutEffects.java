package com.personalbis;

import java.util.Map;

/** Passive effects which depend on multiple equipped items rather than the weapon alone. */
public final class MeleeLoadoutEffects
{
    private MeleeLoadoutEffects() {}

    public static double salveMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition monster)
    {
        if (monster == null || !monster.hasAttribute("undead")) return 1.0;
        EquipmentCandidate neck=gear.get(EquipmentSlot.NECK);
        if (neck==null) return 1.0;
        String n=name(neck);
        if (!n.contains("salve amulet")) return 1.0;
        return (n.contains("(e)") || n.contains("(ei)")) ? 1.20 : (7.0/6.0);
    }

    public static boolean hasSalveEffect(Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition monster)
    {
        return salveMultiplier(gear,monster) > 1.0;
    }

    public static int salveAttackRoll(int baseAttackRoll, Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition monster)
    {
        return (int)Math.floor(baseAttackRoll * salveMultiplier(gear,monster));
    }

    public static int salveMaxHitFromNumerator(int effectiveStrength, int strengthBonus,
        Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition monster)
    {
        // OSRS/Wiki keeps the max-hit numerator exact through multiplicative
        // damage modifiers, then performs the /640 max-hit truncation.
        // This avoids losing a max hit by truncating before Salve.
        double salve = salveMultiplier(gear,monster);
        return (int)Math.floor((effectiveStrength * (strengthBonus + 64) + 320.0) * salve / 640.0);
    }

    public static boolean hasVoidMelee(Map<EquipmentSlot,EquipmentCandidate> gear)
    {
        return named(gear.get(EquipmentSlot.HEAD),"void melee helm")
            && (named(gear.get(EquipmentSlot.BODY),"void knight top") || named(gear.get(EquipmentSlot.BODY),"elite void top"))
            && (named(gear.get(EquipmentSlot.LEGS),"void knight robe") || named(gear.get(EquipmentSlot.LEGS),"elite void robe"))
            && named(gear.get(EquipmentSlot.HANDS),"void knight gloves");
    }

    public static double voidAccuracyMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear)
    {
        return hasVoidMelee(gear) ? 1.10 : 1.0;
    }

    public static double voidDamageMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear)
    {
        return hasVoidMelee(gear) ? 1.10 : 1.0;
    }

    /**
     * Current (22 July 2026+) Inquisitor crush bonus. The old full-set and
     * mace-amplified effects were removed: helm is +0.5%, hauberk +1.0%,
     * plateskirt +1.0%, independently, for +2.5% with all three. The mace
     * now carries its former synergy as raw +7 crush/+7 strength stats.
     */
    public static double inquisitorBonus(Map<EquipmentSlot,EquipmentCandidate> gear, AttackStyle style)
    {
        if (style != AttackStyle.MELEE_CRUSH || gear == null) return 0.0;
        double bonus=0.0;
        if (named(gear.get(EquipmentSlot.HEAD),"inquisitor's great helm")) bonus += 0.005;
        if (named(gear.get(EquipmentSlot.BODY),"inquisitor's hauberk")) bonus += 0.010;
        if (named(gear.get(EquipmentSlot.LEGS),"inquisitor's plateskirt")) bonus += 0.010;
        return bonus;
    }

    public static double inquisitorMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear, AttackStyle style)
    {
        return 1.0 + inquisitorBonus(gear,style);
    }


    /** Full obsidian armour (helm/body/legs) gives obsidian melee weapons +10% accuracy and damage. */
    public static boolean hasObsidianArmourSet(Map<EquipmentSlot,EquipmentCandidate> gear)
    {
        return gear!=null && named(gear.get(EquipmentSlot.HEAD),"obsidian helmet")
            && named(gear.get(EquipmentSlot.BODY),"obsidian platebody")
            && named(gear.get(EquipmentSlot.LEGS),"obsidian platelegs");
    }

    public static double obsidianAccuracyMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear, EquipmentCandidate weapon)
    {
        return hasObsidianArmourSet(gear) && MeleeWeaponEffects.isObsidianMeleeWeapon(weapon) ? 1.10 : 1.0;
    }

    /** Berserker necklace gives obsidian melee weapons +20% damage; stacks with full obsidian armour. */
    public static double obsidianDamageMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear, EquipmentCandidate weapon)
    {
        if (!MeleeWeaponEffects.isObsidianMeleeWeapon(weapon)) return 1.0;
        double mult=hasObsidianArmourSet(gear) ? 1.10 : 1.0;
        if (gear!=null && (named(gear.get(EquipmentSlot.NECK),"berserker necklace")
            || named(gear.get(EquipmentSlot.NECK),"berserker necklace (or)"))) mult*=1.20;
        return mult;
    }

    /**
     * Obsidian/Berserker-necklace damage is applied to each raw 0..baseMax
     * damage roll, with integer truncation on the modified roll. The Wiki
     * Vorkath checkpoint (base max 32, x1.32) averages 20.63636 rather than
     * treating the result as a fresh uniform 0..42 distribution.
     */
    public static double obsidianModifiedRollAverage(int baseMax, double multiplier)
    {
        if (baseMax<=0) return 0.0;
        if (multiplier<=1.0) return MeleeWeaponEffects.successfulHitAverage(baseMax);
        long total=0;
        for(int roll=0;roll<=baseMax;roll++) total += (int)Math.floor(roll*multiplier);
        return total/(double)(baseMax+1);
    }

    public static boolean isSynergyItem(EquipmentCandidate c)
    {
        if(c==null)return false;
        String n=name(c);
        return n.contains("salve amulet")
            || n.contains("slayer helmet") || n.contains("slayer helm") || n.contains("black mask")
            || n.contains("void melee helm") || n.contains("void knight top") || n.contains("elite void top")
            || n.contains("void knight robe") || n.contains("elite void robe") || n.contains("void knight gloves")
            || n.contains("inquisitor's great helm") || n.contains("inquisitor's hauberk")
            || n.contains("inquisitor's plateskirt") || n.contains("inquisitor's mace")
            || n.contains("obsidian helmet") || n.contains("obsidian platebody") || n.contains("obsidian platelegs")
            || n.contains("berserker necklace") || n.contains("verac's helm")
            || n.contains("verac's brassard") || n.contains("verac's plateskirt");
    }

    private static boolean named(EquipmentCandidate c,String text){return c!=null&&name(c).contains(text);}
    private static String name(EquipmentCandidate c){return c.getItem().getName().toLowerCase();}
}
