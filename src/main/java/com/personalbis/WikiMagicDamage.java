package com.personalbis;

import java.util.Collection;

/** Source-ordered auto-attack port of PlayerVsNPCCalc.getPlayerMaxMagicHit(). */
public final class WikiMagicDamage
{
    private WikiMagicDamage() {}

    public static final class Result
    {
        public final int baseMax;
        public final int minHit;
        public final int maxHit;

        Result(int baseMax, int minHit, int maxHit)
        {
            this.baseMax = baseMax;
            this.minHit = minHit;
            this.maxHit = maxHit;
        }
    }

    public static Result standard(int magicLevel, double equipmentDamage, double prayerDamage,
        Collection<EquipmentCandidate> gear, MonsterDefinition monster, MagicSpell spell,
        boolean onSlayerTask)
    {
        int base = spell == null ? 0 : spell.scaledBaseMax(magicLevel);
        String weapon = weaponName(gear);
        if (spell != null && "Magic Dart".equals(spell.name))
        {
            boolean enhanced = equals(weapon, "Slayer's staff (e)") && onSlayerTask;
            base = enhanced ? 13 + magicLevel / 6 : 10 + magicLevel / 10;
        }
        if (spell != null && spell.name != null && spell.name.toLowerCase().contains("bolt")
            && has(gear, "Chaos gauntlets")) base += 3;
        // Charge is not assumed until its active buff state is exposed by PBIS.
        base += ElementalAmuletEffects.evaluate(gear, spell).flatMaxHit;
        return ordered(base, equipmentDamage, prayerDamage, gear, monster, spell, onSlayerTask, false);
    }

    public static Result powered(int base, double equipmentDamage, double prayerDamage,
        Collection<EquipmentCandidate> gear, MonsterDefinition monster, boolean onSlayerTask)
    {
        return ordered(base, equipmentDamage, prayerDamage, gear, monster, null, onSlayerTask, false);
    }

    static Result ordered(int base, double equipmentDamage, double prayerDamage,
        Collection<EquipmentCandidate> gear, MonsterDefinition monster, MagicSpell spell,
        boolean onSlayerTask, boolean sunfireRunes)
    {
        if (base <= 0) return new Result(0, 0, 0);
        int baseMax = base;
        int damageThousandths = (int)Math.round((equipmentDamage + prayerDamage) * 1000.0);
        String weapon = weaponName(gear);

        if ((MagicWeaponEffects.isSmokeStaff(weapon) || MagicWeaponEffects.isTwinflame(weapon))
            && spell != null && "standard".equals(spell.spellbook)) damageThousandths += 100;

        boolean undead = monster != null && monster.hasAttribute("undead");
        boolean salveEi = undead && has(gear, "Salve amulet(ei)");
        boolean salveI = undead && has(gear, "Salve amulet(i)");
        boolean blackMask = !salveEi && !salveI && onSlayerTask
            && (has(gear, "Slayer helmet (i)") || has(gear, "Slayer helm (i)") || has(gear, "Black mask (i)"));
        if (salveEi) damageThousandths += 200;
        else if (salveI) damageThousandths += 150;

        int max = base + WikiIntegerStages.factor(base, damageThousandths, 1000);
        if (blackMask) max = WikiIntegerStages.factor(max, 23, 20);
        if (equals(weapon, "Dawnbringer")) max = Math.max(2, max / 2);

        if (MagicWeaponEffects.isDraconic(monster))
        {
            if (equals(weapon, "Dragon hunter lance")) max = WikiIntegerStages.factor(max, 6, 5);
            else if (MagicWeaponEffects.isDragonHunterWand(weapon)) max = WikiIntegerStages.factor(max, 7, 5);
            else if (equals(weapon, "Dragon hunter crossbow")) max = WikiIntegerStages.factor(max, 5, 4);
        }

        if (monster != null && spell != null && monster.weakTo(spell.element()))
            max += WikiIntegerStages.factor(baseMax, monster.getWeaknessSeverity(), 100);

        int min = sunfireRunes && spell != null && "fire".equals(spell.element()) ? max / 10 : 0;
        if (chargedMatchingTome(gear, spell)) max = WikiIntegerStages.factor(max, 11, 10);
        return new Result(baseMax, Math.max(0, min), Math.max(0, max));
    }

    private static boolean chargedMatchingTome(Collection<EquipmentCandidate> gear, MagicSpell spell)
    {
        if (spell == null || spell.element().isEmpty()) return false;
        // Bank snapshots cannot expose charge count; PBIS retains its established
        // charged-form ownership assumption until item charge state is available.
        return has(gear, "Tome of " + spell.element());
    }

    private static String weaponName(Collection<EquipmentCandidate> gear)
    {
        if (gear != null) for (EquipmentCandidate c : gear)
            if (c != null && c.getSlot() == EquipmentSlot.WEAPON && c.getItem() != null) return c.getItem().getName();
        return "";
    }

    private static boolean has(Collection<EquipmentCandidate> gear, String name)
    {
        if (gear != null) for (EquipmentCandidate c : gear)
            if (c != null && c.getItem() != null && equals(c.getItem().getName(), name)) return true;
        return false;
    }

    private static boolean equals(String a, String b) { return a != null && a.equalsIgnoreCase(b); }
}
