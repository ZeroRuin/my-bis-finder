package com.personalbis;

public final class MagicOptimizationReport
{
    private final OptimizedMagicLoadout best;

    public MagicOptimizationReport(OptimizedMagicLoadout best)
    {
        this.best = best;
    }

    public OptimizedMagicLoadout getBest() { return best; }
}
