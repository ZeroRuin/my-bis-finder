package com.personalbis;

import java.util.Collection;

/**
 * Source-ordered port of PlayerVsNPCCalc.getPlayerMaxMagicAttackRoll().
 * PBIS supplies account/loadout state; this class owns the Wiki integer stages.
 */
public final class WikiMagicAccuracy
{
    private WikiMagicAccuracy() {}

    public static final class Result
    {
        public final int effectiveLevel;
        public final int baseRoll;
        public final int attackRoll;

        Result(int effectiveLevel, int baseRoll, int attackRoll)
        {
            this.effectiveLevel = effectiveLevel;
            this.baseRoll = baseRoll;
            this.attackRoll = attackRoll;
        }
    }

    public static Result calculate(int magicLevel, double prayerFactor, int magicBonus,
        Collection<EquipmentCandidate> gear, MonsterDefinition monster, MagicSpell spell,
        boolean onSlayerTask)
    {
        int prayerNumerator = (int)Math.round(prayerFactor * 100.0);
        int effective = WikiIntegerStages.factor(Math.max(1, magicLevel), prayerNumerator, 100);
        // PBIS optimizes autocast loadouts. Accurate (+2) is reserved for a later
        // explicit manual-cast stance input, matching the upstream distinction.
        effective += 9;

        boolean magicVoid = has(gear, "Void mage helm")
            && has(gear, "Void knight gloves")
            && (has(gear, "Void knight top") || has(gear, "Elite void top"))
            && (has(gear, "Void knight robe") || has(gear, "Elite void robe"));
        if (magicVoid) effective = WikiIntegerStages.factor(effective, 29, 20);

        int baseRoll = effective * (magicBonus + 64);
        int additivePercent = 0;
        boolean undead = monster != null && monster.hasAttribute("undead");
        boolean salveEi = undead && has(gear, "Salve amulet(ei)");
        boolean salveI = undead && has(gear, "Salve amulet(i)");
        boolean blackMask = !salveEi && !salveI && onSlayerTask
            && (has(gear, "Slayer helmet (i)") || has(gear, "Slayer helm (i)") || has(gear, "Black mask (i)"));
        if (salveEi) additivePercent += 20;
        else if (salveI) additivePercent += 15;

        String weapon = weaponName(gear);
        if (MagicWeaponEffects.isSmokeStaff(weapon) || MagicWeaponEffects.isTwinflame(weapon))
        {
            if (spell != null && "standard".equals(spell.spellbook)) additivePercent += 10;
        }

        int roll = additivePercent == 0 ? baseRoll
            : WikiIntegerStages.factor(baseRoll, 100 + additivePercent, 100);

        if (MagicWeaponEffects.isDraconic(monster))
        {
            if (equals(weapon, "Dragon hunter crossbow")) roll = WikiIntegerStages.factor(roll, 13, 10);
            else if (equals(weapon, "Dragon hunter lance")) roll = WikiIntegerStages.factor(roll, 6, 5);
            else if (MagicWeaponEffects.isDragonHunterWand(weapon)) roll = WikiIntegerStages.factor(roll, 7, 4);
        }

        if (blackMask) roll = WikiIntegerStages.factor(roll, 23, 20);

        if (spell != null && spell.name != null && spell.name.contains("Demonbane")
            && monster != null && monster.hasAttribute("demon"))
        {
            int percent = spell.markOfDarkness ? 40 : 20;
            if (equals(weapon, "Purging staff")) percent *= 2;
            int scaled = percent * DemonbaneEffects.vulnerability(monster) / 100;
            roll += WikiIntegerStages.factor(roll, scaled, 100);
        }

        // Forinthry Surge is intentionally false until PBIS exposes that buff as account state.

        if (has(gear, "Tome of Water") && tomeWaterAccuracyApplies(spell))
            roll = WikiIntegerStages.factor(roll, 6, 5);

        if (monster != null && spell != null && monster.weakTo(spell.element()))
            roll += WikiIntegerStages.factor(baseRoll, monster.getWeaknessSeverity(), 100);

        return new Result(effective, baseRoll, roll);
    }

    static int orderedRoll(int baseRoll, int additivePercent, int dragonNumerator, int dragonDenominator,
        boolean blackMask, int demonbanePercent, int vulnerability, boolean revenant,
        boolean tomeWater, int weaknessPercent)
    {
        int roll = additivePercent == 0 ? baseRoll : WikiIntegerStages.factor(baseRoll, 100 + additivePercent, 100);
        if (dragonNumerator != dragonDenominator) roll = WikiIntegerStages.factor(roll, dragonNumerator, dragonDenominator);
        if (blackMask) roll = WikiIntegerStages.factor(roll, 23, 20);
        int scaledDemonbane = demonbanePercent * vulnerability / 100;
        if (scaledDemonbane != 0) roll += WikiIntegerStages.factor(roll, scaledDemonbane, 100);
        if (revenant) roll = WikiIntegerStages.factor(roll, 3, 2);
        if (tomeWater) roll = WikiIntegerStages.factor(roll, 6, 5);
        if (weaknessPercent != 0) roll += WikiIntegerStages.factor(baseRoll, weaknessPercent, 100);
        return roll;
    }

    private static boolean tomeWaterAccuracyApplies(MagicSpell spell)
    {
        if (spell == null) return false;
        if ("water".equals(spell.element())) return true;
        String n = spell.name == null ? "" : spell.name.toLowerCase();
        return n.contains("bind") || n.contains("snare") || n.contains("entangle");
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
