package com.personalbis;
import java.util.*;
public final class MagicOptimizationReport {
 private final OptimizedMagicLoadout best; private final List<MagicLoadoutComparison> comparisons; private final List<MagicAuditLine> audit;
 public MagicOptimizationReport(OptimizedMagicLoadout b,List<MagicLoadoutComparison> c){this(b,c,Collections.emptyList());}
 public MagicOptimizationReport(OptimizedMagicLoadout b,List<MagicLoadoutComparison> c,List<MagicAuditLine> a){
  best=b; comparisons=Collections.unmodifiableList(new ArrayList<>(c)); audit=Collections.unmodifiableList(new ArrayList<>(a));
 }
 public OptimizedMagicLoadout getBest(){return best;}
 public List<MagicLoadoutComparison> getComparisons(){return comparisons;}
 public List<MagicAuditLine> getAudit(){return audit;}
}
