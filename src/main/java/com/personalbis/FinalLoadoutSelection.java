package com.personalbis;

public final class FinalLoadoutSelection
{
    private FinalLoadoutSelection(){}

    public static boolean better(double candidateDps, double currentDps)
    {
        return Double.isFinite(candidateDps) &&
            (!Double.isFinite(currentDps) || candidateDps > currentDps);
    }

    /** Finite-fight ordering: lower expected TTK wins, sustained DPS breaks ties. */
    public static boolean betterTtk(double candidateTtk, double candidateDps,
        double currentTtk, double currentDps)
    {
        if (!Double.isFinite(candidateTtk)) return !Double.isFinite(currentTtk) && candidateDps>0 && better(candidateDps,currentDps);
        if (!Double.isFinite(currentTtk)) return true;
        int ttk = Double.compare(candidateTtk,currentTtk);
        return ttk < 0 || (ttk == 0 && better(candidateDps,currentDps));
    }

    /** True when the candidate kills faster than the current result (or there is no current result). */
    public static boolean faster(CombatOutcome candidate, CombatOutcome current)
    {
        return current == null || betterTtk(candidate.getExpectedTtkSeconds(), candidate.getDps(),
            current.getExpectedTtkSeconds(), current.getDps());
    }

    /** Faster, or exactly as fast with a higher Prayer bonus. */
    public static boolean preferred(CombatOutcome candidate, int candidatePrayer, CombatOutcome current, int currentPrayer)
    {
        return faster(candidate, current)
            || (Double.compare(candidate.getExpectedTtkSeconds(), current.getExpectedTtkSeconds()) == 0 && candidatePrayer > currentPrayer);
    }
}
