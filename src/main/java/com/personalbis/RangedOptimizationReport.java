package com.personalbis;
import java.util.*;
public final class RangedOptimizationReport {
 private final OptimizedRangedLoadout best; private final List<RangedWeaponComparison> comparisons;
 public RangedOptimizationReport(OptimizedRangedLoadout b,List<RangedWeaponComparison> c){best=b;comparisons=Collections.unmodifiableList(c);}
 public OptimizedRangedLoadout getBest(){return best;} public List<RangedWeaponComparison> getComparisons(){return comparisons;}
}