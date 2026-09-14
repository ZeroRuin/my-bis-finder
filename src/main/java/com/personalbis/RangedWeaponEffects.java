package com.personalbis;
public final class RangedWeaponEffects {
 private RangedWeaponEffects(){}
 public static boolean isDragonHunterCrossbow(EquipmentCandidate w){return w!=null&&name(w).contains("dragon hunter crossbow");}
 public static boolean isBowfa(EquipmentCandidate w){return w!=null&&(name(w).contains("bow of faerdhinen")||name(w).contains("bowfa"));}
 public static boolean isCrystalBow(EquipmentCandidate w){return w!=null&&name(w).contains("crystal bow");}
 public static boolean usesCrystalArmourBonus(EquipmentCandidate w){return isBowfa(w)||isCrystalBow(w);}
 public static boolean isScorchingBow(EquipmentCandidate w){return w!=null&&name(w).contains("scorching bow");}
 public static boolean isTwistedBow(EquipmentCandidate w){return w!=null&&name(w).contains("twisted bow");}
 public static boolean isDraconic(MonsterDefinition m){return m!=null&&(m.hasAttribute("dragon")||m.hasAttribute("draconic"));}
 public static double accuracyMultiplier(EquipmentCandidate w,MonsterDefinition m){
  if(isDragonHunterCrossbow(w)&&isDraconic(m))return 1.30;
  if(isScorchingBow(w)&&m!=null&&m.hasAttribute("demon"))return DemonbaneEffects.multiplier(m,30);
  if(isTwistedBow(w)&&m!=null)return twistedAccuracy(m);
  return 1.0;
 }
 public static double damageMultiplier(EquipmentCandidate w,MonsterDefinition m){
  if(isDragonHunterCrossbow(w)&&isDraconic(m))return 1.25;
  if(isScorchingBow(w)&&m!=null&&m.hasAttribute("demon"))return DemonbaneEffects.multiplier(m,30);
  if(isTwistedBow(w)&&m!=null)return twistedDamage(m);
  return 1.0;
 }
 // Outside-CoX Tbow model, target Magic level capped at 250. The bundled data does not yet expose NPC magic attack roll,
 // so Magic level is used; this is deliberately surfaced as a known limitation.
 public static double twistedAccuracy(MonsterDefinition m){
  double magic=twistedTargetMagic(m);
  double pct=140.0+((30.0*magic/10.0)-10.0)/100.0-Math.pow((3.0*magic/10.0)-100.0,2)/100.0;
  return Math.max(0,Math.min(140,pct))/100.0;
 }
 public static double twistedDamage(MonsterDefinition m){
  double magic=twistedTargetMagic(m);
  double pct=250.0+((30.0*magic/10.0)-14.0)/100.0-Math.pow((3.0*magic/10.0)-140.0,2)/100.0;
  return Math.max(0,Math.min(250,pct))/100.0;
 }
 public static double twistedTargetMagic(MonsterDefinition m){
  if(m==null)return 0;
  int cap=m.hasAttribute("xerician")?350:250;
  return Math.min(cap,Math.max(m.getMagicLevel(),m.getMagicAttack()));
 }
 private static String name(EquipmentCandidate w){return w.getItem().getName().toLowerCase();}
}