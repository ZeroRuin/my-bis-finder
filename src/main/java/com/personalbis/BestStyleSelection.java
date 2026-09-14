package com.personalbis;
import java.util.Map;
public final class BestStyleSelection {
 private BestStyleSelection(){}
 public static AttackStyle best(Map<AttackStyle,Double> scores){
  AttackStyle best=null; double score=Double.NEGATIVE_INFINITY;
  for(AttackStyle s:AttackStyle.values()){double v=scores.getOrDefault(s,Double.NEGATIVE_INFINITY);if(v>score){score=v;best=s;}}
  return best;
 }
}
