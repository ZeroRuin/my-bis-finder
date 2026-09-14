package com.personalbis;

import java.util.EnumSet;

/**
 * Maps Wiki DPS weapon categories to attack types the weapon can actually use.
 * Unknown categories are rejected rather than being granted fictional styles.
 */
public final class MeleeAttackStyleResolver
{
    private MeleeAttackStyleResolver() {}

    public static EnumSet<MeleeStance> legalStances(EquipmentCandidate weapon, AttackStyle style)
    {
        if (weapon == null) return EnumSet.allOf(MeleeStance.class);
        String c=weapon.getWeaponCategory()==null?"":weapon.getWeaponCategory().trim().toLowerCase();

        switch(c)
        {
            case "stab sword":
                // Dagger-style interface (including the original Keris):
                // Stab/Accurate, Lunge/Stab/Aggressive, Slash/Slash/Aggressive.
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "partisan":
                // Keris partisan interface: Stab/Accurate, Lunge/Stab/Aggressive,
                // Pound/Crush/Aggressive. It has no Slash attack option.
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "slash sword":
            case "claw":
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.CONTROLLED);
                return none();

            case "2h sword":
            case "axe":
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "blunt":
            case "bludgeon":
            case "staff":
            case "bulwark":
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                return none();

            case "spiked":
            case "flail":
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.CONTROLLED);
                return none();

            case "pickaxe":
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.ACCURATE);
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "whip":
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.ACCURATE,MeleeStance.CONTROLLED);
                return none();

            case "spear":
                if(style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH)
                    return set(MeleeStance.CONTROLLED);
                return none();

            case "polearm":
            case "scythe":
                // Halberd/polearm Slash is represented by the aggressive stance in
                // the Wiki attack interface; do not invent an Accurate option.
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.AGGRESSIVE);
                if(style==AttackStyle.MELEE_CRUSH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "polestaff":
            case "bladed staff":
                if(style==AttackStyle.MELEE_STAB)return set(MeleeStance.ACCURATE,MeleeStance.CONTROLLED);
                if(style==AttackStyle.MELEE_SLASH)return set(MeleeStance.AGGRESSIVE);
                return none();

            case "multi-melee":
                return style==AttackStyle.MELEE_STAB||style==AttackStyle.MELEE_SLASH||style==AttackStyle.MELEE_CRUSH
                    ? set(MeleeStance.ACCURATE,MeleeStance.AGGRESSIVE,MeleeStance.CONTROLLED) : none();

            // Ranged/magic/non-melee and unknown categories cannot be assumed to
            // possess a melee attack interface.
            default:
                return none();
        }
    }

    public static boolean supports(EquipmentCandidate weapon,AttackStyle style)
    {
        return !legalStances(weapon,style).isEmpty();
    }

    private static EnumSet<MeleeStance> set(MeleeStance first,MeleeStance... rest)
    {
        return EnumSet.of(first,rest);
    }

    private static EnumSet<MeleeStance> none()
    {
        return EnumSet.noneOf(MeleeStance.class);
    }
}
