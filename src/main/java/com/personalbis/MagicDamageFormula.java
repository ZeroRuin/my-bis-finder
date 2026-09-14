package com.personalbis;
public final class MagicDamageFormula {
 private MagicDamageFormula(){}
 public static int standardPrimary(int base,double equipment,double prayer,double weakness){
  int primary=(int)Math.floor(base*(1.0+equipment+prayer));
  int elemental=(int)Math.floor(base*weakness);
  return primary+elemental;
 }
 public static int elementalAttackRoll(int baseRoll,double weakness){
  return (int)Math.floor(baseRoll*(1.0+weakness));
 }
 public static double hitChance(int attackRoll,int defenceRoll){
  return WikiAccuracyRoll.normal(attackRoll,defenceRoll);
 }
 public static int poweredPrimary(int base,double equipment,double prayer){
  return (int)Math.floor(base*(1.0+equipment+prayer));
 }
}
