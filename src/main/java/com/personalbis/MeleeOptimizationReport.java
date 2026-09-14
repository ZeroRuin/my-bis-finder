package com.personalbis;

import java.util.Collections;
import java.util.List;

public final class MeleeOptimizationReport
{
    private final OptimizedLoadout best;
    private final List<WeaponComparison> comparisons;
    private final List<String> auditLines;

    public MeleeOptimizationReport(OptimizedLoadout best, List<WeaponComparison> comparisons)
    {
        this(best, comparisons, Collections.emptyList());
    }

    public MeleeOptimizationReport(OptimizedLoadout best, List<WeaponComparison> comparisons, List<String> auditLines)
    {
        this.best=best;
        this.comparisons=Collections.unmodifiableList(comparisons);
        this.auditLines=Collections.unmodifiableList(auditLines);
    }

    public OptimizedLoadout getBest(){return best;}
    public List<WeaponComparison> getComparisons(){return comparisons;}
    public List<String> getAuditLines(){return auditLines;}
}
