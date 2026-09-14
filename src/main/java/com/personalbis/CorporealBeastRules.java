package com.personalbis;
/** Wiki DPS parity rules for Corporeal Beast damage reduction. */
public final class CorporealBeastRules {
 private CorporealBeastRules(){}
 public static boolean isCorp(MonsterDefinition m){return m!=null&&"Corporeal Beast".equalsIgnoreCase(m.getName());}
 public static boolean meleeCorpbane(EquipmentCandidate w,AttackStyle style){
  if(w==null||style==null)return false; String n=w.getItem().getName().toLowerCase();
  if(MeleeWeaponEffects.isFang(w))return style==AttackStyle.MELEE_STAB;
  if(n.endsWith("halberd"))return style==AttackStyle.MELEE_STAB;
  if(n.contains("spear")&&!n.equals("blue moon spear"))return style==AttackStyle.MELEE_STAB;
  return false;
 }
 public static boolean reduced(MonsterDefinition m,EquipmentCandidate w,AttackStyle style){return isCorp(m)&&!meleeCorpbane(w,style);}
 /** Corp's divisionTransformer(2) floors each hitsplat after accurate-zero conversion. */
 public static double dividedSuccessfulAverage(int max){
  if(max<=0)return 0.0; return HitDistribution.linear(1.0,0,max).accurateZeroToOne().transform(HitDistribution.divide(2,0)).expectedHit();
 }
 public static double dividedUniformAverage(int min,int max){
  if(max<min)return 0.0; return HitDistribution.linear(1.0,min,max).transform(HitDistribution.divide(2,0)).expectedHit();
 }
}
