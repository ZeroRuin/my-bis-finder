package com.personalbis;

/** Target/weapon attack-speed overrides ported from the OSRS Wiki DPS calculator. */
public final class WikiAttackSpeedRules
{
    private WikiAttackSpeedRules() {}

    public static boolean isScurriusGiantRat(MonsterDefinition monster)
    {
        return monster != null && monster.getId() == 7223;
    }

    public static boolean isRatBoneWeapon(String weaponName)
    {
        if (weaponName == null) return false;
        String n = weaponName.trim().toLowerCase();
        return n.equals("bone mace") || n.equals("bone shortbow") || n.equals("bone staff");
    }

    public static int attackTicks(int ordinaryTicks, String weaponName, MonsterDefinition monster)
    {
        if (isScurriusGiantRat(monster) && isRatBoneWeapon(weaponName)) return 1;
        return Math.max(1, ordinaryTicks);
    }

    public static int attackTicks(int ordinaryTicks, EquipmentCandidate weapon, MonsterDefinition monster)
    {
        String name = weapon == null || weapon.getItem() == null ? null : weapon.getItem().getName();
        return attackTicks(ordinaryTicks, name, monster);
    }
}
