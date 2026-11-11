package com.personalbis;

import java.util.Locale;
import java.util.Map;

/** The text shown under a recommended loadout. */
final class LoadoutSummaryText
{
    private LoadoutSummaryText() {}

    static String ttk(double seconds)
    {
        if (!Double.isFinite(seconds)) return "—";
        if (seconds < 60.0) return String.format(Locale.ROOT, "%.1fs", seconds);
        return String.format(Locale.ROOT, "%dm %.1fs", (int) (seconds / 60.0), seconds % 60.0);
    }

    /** The stats block: DPS, time to kill, max hit, accuracy, attack style and Prayer bonus. */
    static String stats(CombatOutcome outcome, String styleLabel, int prayerBonus, AttackStyle style)
    {
        if (outcome == null)
            return style == AttackStyle.MAGIC
                ? "No castable combat spell found with your current Magic level and bank runes."
                : "No calculated loadout is available for this style.";
        return String.format("DPS: %.4f%n", outcome.getDps())
            + "Estimated TTK: " + ttk(outcome.getExpectedTtkSeconds()) + System.lineSeparator()
            + String.format("Max Hit: %d%n", outcome.getMaxHit())
            + String.format("Accuracy: %.2f%%%n", outcome.getAccuracy() * 100.0)
            + String.format("Style: %s%n", styleLabel)
            + String.format("Prayer Bonus: %+d", prayerBonus);
    }

    /** The assumptions behind the numbers, shown as the stats tooltip. */
    static String assumptions(MonsterDefinition monster, Map<EquipmentSlot, EquipmentCandidate> gear,
        OptimizedRangedLoadout ranged, FillPreference fill)
    {
        StringBuilder context = new StringBuilder("Normal attacks only; specials excluded.");
        EquipmentCandidate weapon = gear.get(EquipmentSlot.WEAPON);
        if (VardorvisRules.supported(monster)) context.append(" Accuracy/DPS use selected target HP; TTK scales Defence as HP falls.");
        if (SoulreaperAxeEffects.isSoulreaper(weapon)) context.append(" Soulreaper starts at zero stacks.");
        if (ranged != null && ranged.getLoadedAmmo() != null)
            context.append(" Assumed internal ammo: ").append(ranged.getLoadedAmmo().getItem().getName()).append("; quantity unknown.");
        String drain = ManualDefenceReduction.note(monster);
        if (!drain.isEmpty()) context.append(" ").append(drain);
        if (fill == FillPreference.DEFENCE) context.append(" Defence fill: ").append(DefenceFillPolicy.profile(monster).description).append("; equipment bonuses only.");
        if (EquipmentSetRules.dharok(gear)) context.append(" Dharok uses your Hitpoints snapshot at generation; regenerate after HP changes.");
        if (EquipmentSetRules.strengthRanged(weapon)) context.append(" Damage uses Strength level and melee Strength bonus.");
        if (WikiTargetEligibility.isGuardian(monster)) context.append(" Guardian damage uses Mining level and pickaxe tier.");
        String raid = RaidTargetVariants.note(monster);
        if (!raid.isEmpty()) context.append(" ").append(raid);
        return "<html><body style='width:240px'>" + context.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</body></html>";
    }
}
