package com.personalbis;

/**
 * OSRS Wiki DPS calculator accuracy-roll helpers.
 *
 * Parity source: src/lib/BaseCalc.ts getNormalAccuracyRoll/getFangAccuracyRoll.
 * Unlike the common positive-roll shortcut, these helpers deliberately preserve
 * signed attack/defence rolls produced by bonuses below -64.
 */
public final class WikiAccuracyRoll {
    private WikiAccuracyRoll() {}

    public static double normal(int attackRoll, int defenceRoll) {
        int atk = normalizeNegative(attackRoll);
        int def = normalizeNegative(defenceRoll);
        final double hit;
        if (atk >= 0 && def >= 0) hit = standard(atk, def);
        else if (atk >= 0) hit = 1.0 - 1.0 / (-def + 1.0) / (atk + 1.0);
        else if (def >= 0) hit = 0.0;
        else hit = standard(-def, -atk);
        return clamp(hit);
    }

    public static double fang(int attackRoll, int defenceRoll) {
        int atk = normalizeNegative(attackRoll);
        int def = normalizeNegative(defenceRoll);
        final double hit;
        if (atk >= 0 && def >= 0) hit = fangStandard(atk, def);
        else if (atk >= 0) hit = 1.0 - 1.0 / (-def + 1.0) / (atk + 1.0);
        else if (def >= 0) hit = 0.0;
        else hit = fangReverse(-def, -atk);
        return clamp(hit);
    }

    private static int normalizeNegative(int roll) {
        return roll < 0 ? Math.min(0, roll + 2) : roll;
    }

    private static double standard(double attack, double defence) {
        return attack > defence
            ? 1.0 - (defence + 2.0) / (2.0 * (attack + 1.0))
            : attack / (2.0 * (defence + 1.0));
    }

    private static double fangStandard(double attack, double defence) {
        return attack > defence
            ? 1.0 - (defence + 2.0) * (2.0 * defence + 3.0) / (6.0 * (attack + 1.0) * (attack + 1.0))
            : attack * (4.0 * attack + 5.0) / (6.0 * (attack + 1.0) * (defence + 1.0));
    }

    private static double fangReverse(double attack, double defence) {
        return attack < defence
            ? attack * (defence * 6.0 - 2.0 * attack + 5.0) / (6.0 * (defence + 1.0) * (defence + 1.0))
            : 1.0 - (defence + 2.0) * (2.0 * defence + 3.0) / (6.0 * (defence + 1.0) * (attack + 1.0));
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
