package com.personalbis;

/** Exact integer checkpoints used by the OSRS Wiki DPS calculator. */
public final class WikiIntegerStages
{
    private WikiIntegerStages() {}

    /** Equivalent to Wiki BaseCalc.trackFactor(): truncate immediately after factor application. */
    public static int factor(int base, int numerator, int denominator)
    {
        if (denominator == 0) throw new IllegalArgumentException("denominator");
        return (int)(((long)base * numerator) / denominator);
    }

    /** Equivalent to Wiki BaseCalc.trackAdd(): truncate the completed addition. */
    public static int add(int base, double addend)
    {
        return (int)(base + addend);
    }

    /** Wiki max-hit checkpoint: trunc((effectiveLevel * gearBonus + 320) / 640). */
    public static int maxHitFromEffective(int effectiveLevel, int gearBonus)
    {
        return (int)(((long)effectiveLevel * gearBonus + 320L) / 640L);
    }
}
