package com.personalbis;

public final class MeleeOptimizationReport
{
    private final OptimizedLoadout best;

    public MeleeOptimizationReport(OptimizedLoadout best)
    {
        this.best = best;
    }

    public OptimizedLoadout getBest() { return best; }
}
