package com.personalbis;
import java.util.Arrays;
/** Dragonstone Dragon's Breath target gating, diary chance, and diagnostic max. */
final class EnchantedBoltRegression50_4_25 {
 private static EquipmentCandidate ammo(){return new EquipmentCandidate(new BankItem(1,1,"Dragonstone bolts (e)"),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition monster(String name,String... attrs){return new MonsterDefinition(1,name,214,308,308,150,750,26,108,108,240,26,26,26,Arrays.asList(attrs));}
 private static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 static boolean vorkathBlocksDragonsBreath(){
  EquipmentCandidate a=ammo(); MonsterDefinition v=monster("Vorkath","boss","dragon","fiery","undead","dragonfire immune");
  int max=42, level=105; double acc=.7199, normal=acc*MeleeWeaponEffects.successfulHitAverage(max);
  String label=EnchantedBoltEffects.validationLabel(a,v,level,true);
  return near(EnchantedBoltEffects.expectedDamagePerAttack(a,v,level,max,acc,true),normal)
      && EnchantedBoltEffects.validatedProcMaxHit(a,v,level,max)==max
      && label.contains("BLOCKED") && label.contains("0.00%");
 }
 static boolean livingKandarinAndProcDamage(){
  EquipmentCandidate a=ammo(); MonsterDefinition living=monster("Living target","boss");
  int max=42, level=105; double acc=.7199;
  double normal=acc*MeleeWeaponEffects.successfulHitAverage(max);
  double expected=acc*((1-.066)*MeleeWeaponEffects.successfulHitAverage(max)+.066*(max/2.0+Math.floor(level*.20)));
  String label=EnchantedBoltEffects.validationLabel(a,living,level,true);
  return near(EnchantedBoltEffects.expectedDamagePerAttack(a,living,level,max,acc,true),expected)
      && EnchantedBoltEffects.validatedProcMaxHit(a,living,level,max)==63
      && label.contains("6.60%") && label.contains("+21 damage");
 }
 private EnchantedBoltRegression50_4_25(){}
}
