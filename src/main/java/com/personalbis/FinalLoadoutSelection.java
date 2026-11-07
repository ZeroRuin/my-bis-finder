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
}
