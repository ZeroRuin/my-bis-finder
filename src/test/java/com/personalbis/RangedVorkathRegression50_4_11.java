package com.personalbis;
import java.util.EnumMap;
import java.util.Map;
/** Exact regressions captured against the OSRS Wiki DPS calculator for Vorkath. */
final class RangedVorkathRegression50_4_11 {
 private static EquipmentCandidate item(String n,EquipmentSlot slot){
  return new EquipmentCandidate(new BankItem(1,1,n),slot,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));
 }
 private static EquipmentCandidate weapon(String n){return item(n,EquipmentSlot.WEAPON);}
 private static EquipmentCandidate ammo(String n){return item(n,EquipmentSlot.AMMO);}
 private static boolean near(double a,double b){return Math.abs(a-b)<0.00001;}

 static boolean rubyVorkathNoDiary(){
  MonsterDefinition vorkath=new MonsterDefinition(0,"Vorkath",732,214,150,308,750);
  EquipmentCandidate ruby=ammo("Ruby bolts (e)");
  double accuracy=1.0-(20070.0+2.0)/(2.0*(35824.0+1.0));
  double ev=EnchantedBoltEffects.expectedDamagePerAttack(ruby,vorkath,105,38,accuracy,false);
  return near(ev,18.87405780950847)&&near(ev/3.0,6.29135260316949);
 }
 static boolean rubyVorkathKandarinHard(){
  MonsterDefinition vorkath=new MonsterDefinition(0,"Vorkath",732,214,150,308,750);
  EquipmentCandidate ruby=ammo("Ruby bolts (e)");
  double accuracy=1.0-(20070.0+2.0)/(2.0*(35824.0+1.0));
  double ev=EnchantedBoltEffects.expectedDamagePerAttack(ruby,vorkath,105,38,accuracy,true);
  return near(ev,19.391882972426504)&&near(ev/3.0,6.463960990808835);
 }
 static boolean runeCrossbowRejectsDragonRuby(){
  return RangedAmmoRules.compatible(weapon("Rune crossbow"),ammo("Ruby bolts (e)"))
    &&!RangedAmmoRules.compatible(weapon("Rune crossbow"),ammo("Ruby dragon bolts (e)"))
    &&RangedAmmoRules.compatible(weapon("Dragon crossbow"),ammo("Ruby dragon bolts (e)"));
 }
 static boolean crystalBonusWeaponScope(){
  Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);
  g.put(EquipmentSlot.HEAD,weapon("Crystal helm"));
  g.put(EquipmentSlot.BODY,weapon("Crystal body"));
  g.put(EquipmentSlot.LEGS,weapon("Crystal legs"));
  EquipmentCandidate bowfa=weapon("Bow of faerdhinen (c)"), crystal=weapon("Crystal bow"), rcb=weapon("Rune crossbow");
  return near(RangedLoadoutEffects.crystalAccuracy(g,bowfa),1.30)
    &&near(RangedLoadoutEffects.crystalDamage(g,bowfa),1.15)
    &&near(RangedLoadoutEffects.crystalAccuracy(g,crystal),1.30)
    &&near(RangedLoadoutEffects.crystalDamage(g,crystal),1.15)
    &&near(RangedLoadoutEffects.crystalAccuracy(g,rcb),1.0)
    &&near(RangedLoadoutEffects.crystalDamage(g,rcb),1.0);
 }
 private RangedVorkathRegression50_4_11(){}
}
