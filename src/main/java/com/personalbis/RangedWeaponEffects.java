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
 // Wiki tbowScaling: truncate both terms independently, then scale with integers.
 public static int twistedPercent(MonsterDefinition m,boolean accuracy){
  int magic=(int)twistedTargetMagic(m),factor=accuracy?10:14,base=accuracy?140:250;
  int term2=(3*magic-factor)/100;
  int difference=3*magic/10-10*factor;
  int term3=difference*difference/100;
  return Math.max(0,Math.min(base,base+term2-term3));
 }
 public static int scaleTwisted(int value,MonsterDefinition m,boolean accuracy){
  return (int)((long)value*twistedPercent(m,accuracy)/100);
 }
 public static double twistedAccuracy(MonsterDefinition m){return twistedPercent(m,true)/100.0;}
 public static double twistedDamage(MonsterDefinition m){return twistedPercent(m,false)/100.0;}
 public static double twistedTargetMagic(MonsterDefinition m){
  if(m==null)return 0;
  int cap=m.hasAttribute("xerician")?350:250;
  return Math.min(cap,Math.max(m.getMagicLevel(),m.getMagicAttack()));
 }
 private static String name(EquipmentCandidate w){return w.getItem().getName().toLowerCase();}
}