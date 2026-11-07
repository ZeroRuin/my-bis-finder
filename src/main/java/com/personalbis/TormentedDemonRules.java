package com.personalbis;

/**
 * Tormented Demon phase rules, matching the OSRS Wiki DPS calculator
 * (PlayerVsNPCCalc and BaseCalc.tdUnshieldedBonusApplies).
 */
final class TormentedDemonRules
{
    private TormentedDemonRules() {}

    enum Phase
    {
        SHIELDED("Shielded"),
        DEFENCELESS("Shielded (Defenceless)"),
        UNSHIELDED("Unshielded");

        private final String label;
        Phase(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    static boolean isTormentedDemon(MonsterDefinition m)
    {
        return m != null && "Tormented Demon".equalsIgnoreCase(m.getName());
    }

    /** The selected phase is carried as the target's version, like Bork's phases. */
    static MonsterDefinition withPhase(MonsterDefinition m, Phase phase)
    {
        if (!isTormentedDemon(m) || phase == null) return m;
        return new MonsterDefinition(m.getId(), m.getName(), phase.toString(), m.getCombatLevel(),
            m.getDefenceLevel(), m.getMagicLevel(), m.getRangedLevel(), m.getHitpoints(),
            m.getStabDefence(), m.getSlashDefence(), m.getCrushDefence(), m.getMagicDefence(),
            m.getLightDefence(), m.getStandardDefence(), m.getHeavyDefence(), m.getAttributes(),
            m.getSize(), m.getWeaknessElement(), m.getWeaknessSeverity(), m.getMagicAttack());
    }

    /** Phase of a Tormented Demon target; Shielded when none was chosen. */
    static Phase phase(MonsterDefinition m)
    {
        if (!isTormentedDemon(m)) return null;
        for (Phase phase : Phase.values())
            if (phase.toString().equalsIgnoreCase(m.getVersion())) return phase;
        return Phase.SHIELDED;
    }

    /** Every attack hits unless the demon is Shielded. */
    static double accuracy(MonsterDefinition m, double ordinaryAccuracy)
    {
        Phase phase = phase(m);
        return phase != null && phase != Phase.SHIELDED ? 1.0 : ordinaryAccuracy;
    }

    /** 20% damage reduction while shielded, unless using demonbane or (melee) abyssal weapons. */
    static boolean damageReduced(MonsterDefinition m, AttackStyle style, String weaponName, boolean demonbaneSpell)
    {
        Phase phase = phase(m);
        if (phase == null || phase == Phase.UNSHIELDED) return false;
        return !usingDemonbane(style, weaponName, demonbaneSpell) && !usingAbyssal(style, weaponName);
    }

    /**
     * Unshielded bonus: spells, heavy ranged (crossbows and chinchompas) and crush
     * attacks deal ticks^2 - 16 extra damage on accurate hits and attack one tick faster.
     */
    static boolean unshieldedBonusApplies(MonsterDefinition m, AttackStyle style,
        EquipmentCandidate weapon, boolean usingSpell)
    {
        if (phase(m) != Phase.UNSHIELDED || style == null) return false;
        switch (style)
        {
            case MAGIC: return usingSpell;
            case MELEE_CRUSH: return true;
            case RANGED:
                String category = weapon == null ? "" : weapon.getWeaponCategory();
                return "Crossbow".equalsIgnoreCase(category) || "Chinchompas".equalsIgnoreCase(category);
            default: return false;
        }
    }

    static int bonusDamage(int attackTicks)
    {
        return Math.max(0, attackTicks * attackTicks - 16);
    }

    /**
     * Adds the bonus to accurate hits of a final distribution. Unshielded demons have
     * no other damage transforms, so adding it after them matches the Wiki order.
     */
    static HitDistribution.AttackDistribution withUnshieldedBonus(HitDistribution.AttackDistribution d, int attackTicks)
    {
        int bonus = bonusDamage(attackTicks);
        return bonus == 0 || d == null ? d : d.transform(HitDistribution.flatAdd(bonus, 0), false).flatten();
    }

    private static boolean usingDemonbane(AttackStyle style, String weaponName, boolean demonbaneSpell)
    {
        if (style == AttackStyle.MAGIC) return demonbaneSpell;
        if (style == AttackStyle.RANGED) return is(weaponName, "Scorching bow");
        return is(weaponName, "Silverlight", "Darklight", "Arclight", "Emberlight", "Bone claws", "Burning claws");
    }

    private static boolean usingAbyssal(AttackStyle style, String weaponName)
    {
        boolean melee = style == AttackStyle.MELEE_STAB || style == AttackStyle.MELEE_SLASH || style == AttackStyle.MELEE_CRUSH;
        return melee && is(weaponName, "Abyssal bludgeon", "Abyssal dagger", "Abyssal whip", "Abyssal tentacle");
    }

    private static boolean is(String weaponName, String... names)
    {
        if (weaponName == null) return false;
        for (String name : names) if (name.equalsIgnoreCase(weaponName)) return true;
        return false;
    }
}
