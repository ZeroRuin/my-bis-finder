package com.personalbis;

public final class RangedOptimizationReport
{
    private final OptimizedRangedLoadout best;

    public RangedOptimizationReport(OptimizedRangedLoadout best)
    {
        this.best = best;
    }

    public OptimizedRangedLoadout getBest() { return best; }
}
