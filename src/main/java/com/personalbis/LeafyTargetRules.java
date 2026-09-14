package com.personalbis;

/**
 * Kurask/Turoth (leafy) damage eligibility.
 *
 * These targets still permit an accuracy roll to be calculated, but ordinary
 * damage is forced to zero unless the attack uses a leaf-bladed melee weapon,
 * broad ranged ammunition, or Magic Dart. This mirrors the Wiki calculator's
 * behaviour (e.g. Toktz-xil-ak can show accuracy against Kurask but max/DPS 0).
 */
public final class LeafyTargetRules
{
    private LeafyTargetRules() {}

    public static boolean restricted(MonsterDefinition monster)
    {
        return monster != null && monster.hasAttribute("leafy");
    }

    public static boolean meleeCanDamage(MonsterDefinition monster, EquipmentCandidate weapon)
    {
        if (!restricted(monster)) return true;
        if (weapon == null || weapon.getItem() == null || weapon.getItem().getName() == null) return false;
        String n=weapon.getItem().getName().toLowerCase();
        return n.contains("leaf-bladed spear") || n.contains("leaf-bladed sword") || n.contains("leaf-bladed battleaxe");
    }

    public static boolean rangedCanDamage(MonsterDefinition monster, EquipmentCandidate ammo)
    {
        if (!restricted(monster)) return true;
        if (ammo == null || ammo.getItem() == null || ammo.getItem().getName() == null) return false;
        String n=ammo.getItem().getName().toLowerCase();
        return n.contains("broad") && (n.contains("bolt") || n.contains("arrow"));
    }

    public static boolean magicCanDamage(MonsterDefinition monster, MagicSpell spell)
    {
        if (!restricted(monster)) return true;
        return spell != null && "Magic Dart".equalsIgnoreCase(spell.name);
    }

    /** Powered staves are not Magic Dart and therefore cannot damage leafy targets. */
    public static boolean poweredMagicCanDamage(MonsterDefinition monster)
    {
        return !restricted(monster);
    }
}
