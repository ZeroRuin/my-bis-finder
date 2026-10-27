package com.personalbis;
import java.util.*;
/** Source-ordered Void set recognition, effective levels and additive Magic damage. */
public final class VoidEquipmentSelfTest {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static EquipmentCandidate piece(String name,EquipmentSlot slot){return new EquipmentCandidate(new BankItem(1,1,name),slot,0,0,0,0,0,0,0,0,0,false,"",RequirementResult.usable("fixture"));}
 private static Map<EquipmentSlot,EquipmentCandidate> set(String helm,boolean elite,boolean ornament){
  Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);String suffix=ornament?" (or)":"";
  gear.put(EquipmentSlot.HEAD,piece(helm+suffix,EquipmentSlot.HEAD));
  gear.put(EquipmentSlot.BODY,piece((elite?"Elite void top":"Void knight top")+suffix,EquipmentSlot.BODY));
  gear.put(EquipmentSlot.LEGS,piece((elite?"Elite void robe":"Void knight robe")+suffix,EquipmentSlot.LEGS));
  gear.put(EquipmentSlot.HANDS,piece("Void knight gloves",EquipmentSlot.HANDS));return gear;
 }
 private static void near(double actual,double expected,String why){check(Math.abs(actual-expected)<1e-10,why+": "+actual);}
 public static void main(String[] args){
  check(CombatEffectiveLevels.melee(82,1.20,3,true)==119,"melee accuracy floor");
  check(CombatEffectiveLevels.melee(90,1.23,3,true)==133,"melee strength floor");
  check(CombatEffectiveLevels.ranged(66,1.15,3,true)==94,"ranged accuracy floor");
  check(CombatEffectiveLevels.rangedDamage(66,1.15,3,true,false)==94,"normal ranged damage floor");
  check(CombatEffectiveLevels.rangedDamage(66,1.15,3,true,true)==96,"elite ranged damage floor");
  check(CombatEffectiveLevels.magic(73,1.15,true,0)==133,"Magic +9 before Void");
  check(CombatEffectiveLevels.magic(73,1.15,true,2)==136,"powered +2 and +9 before Void");
  for(boolean elite:new boolean[]{false,true})for(boolean ornament:new boolean[]{false,true}){
   Map<EquipmentSlot,EquipmentCandidate> melee=set("Void melee helm",elite,ornament);
   check(MeleeLoadoutEffects.hasVoidMelee(melee),"melee full set");
   melee.remove(EquipmentSlot.HANDS);check(!MeleeLoadoutEffects.hasVoidMelee(melee),"melee missing gloves");
   Map<EquipmentSlot,EquipmentCandidate> ranged=set("Void ranger helm",elite,ornament);
   check(RangedLoadoutEffects.voidRanged(ranged)&&RangedLoadoutEffects.eliteVoidRanged(ranged)==elite,"ranged full set/elite distinction");
   ranged.put(EquipmentSlot.HEAD,piece("Void melee helm",EquipmentSlot.HEAD));check(!RangedLoadoutEffects.voidRanged(ranged),"wrong helmet");
   Map<EquipmentSlot,EquipmentCandidate> magic=set("Void mage helm",elite,ornament);
   check(WikiMagicAccuracy.calculate(73,1.15,100,magic.values(),null,null,false).effectiveLevel==133,"actual Magic accuracy normal/elite/ornaments");
   check(WikiMagicAccuracy.calculatePoweredAccurate(73,1.15,100,magic.values(),null,false).effectiveLevel==136,"actual powered Magic accuracy");
   double damage=WikiEquipmentEngine.transform(magic,null,null,0,0,100,.20,0).magicDamage;
   near(damage,elite?.25:.20,"elite adds five percentage points to equipment damage");
   check(WikiMagicDamage.powered(40,damage,.04,magic.values(),null,false).maxHit==(elite?51:49),"no extra multiplicative Void damage");
   MagicLoadoutEffects.Effect note=MagicLoadoutEffects.evaluate(magic.values(),null,false);
   near(note.accuracy,1.45,"ornamented Magic note");near(note.damage,elite?1.05:1.0,"elite Magic note");
   magic.remove(EquipmentSlot.HANDS);
   check(WikiMagicAccuracy.calculate(73,1.15,100,magic.values(),null,null,false).effectiveLevel==92,"incomplete Magic loses accuracy bonus");
   near(WikiEquipmentEngine.transform(magic,null,null,0,0,100,.20,0).magicDamage,.20,"incomplete elite loses damage bonus");
  }
  Map<EquipmentSlot,EquipmentCandidate> mixed=set("Void ranger helm",true,true);
  mixed.put(EquipmentSlot.LEGS,piece("Void knight robe",EquipmentSlot.LEGS));
  check(RangedLoadoutEffects.voidRanged(mixed)&&!RangedLoadoutEffects.eliteVoidRanged(mixed),"mixed normal/elite gets only normal ranged bonus");
  Map<EquipmentSlot,EquipmentCandidate> shadow=set("Void mage helm",true,true);
  shadow.put(EquipmentSlot.WEAPON,piece("Tumeken's shadow",EquipmentSlot.WEAPON));
  near(WikiEquipmentEngine.transform(shadow,null,null,0,0,100,.20,0).magicDamage,.75,"elite five points before Shadow scaling");
  check(!VoidEquipment.matches("Void mage helm (broken)","Void mage helm"),"no invented suffix support");
  System.out.println("PASS normal/elite/ornamented Void, missing/wrong pieces, mixed robes, rounding and additive Magic/Shadow stages");
 }
}
