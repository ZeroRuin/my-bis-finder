package com.personalbis;
import java.util.Arrays;
/** Onyx Life Leech target-gating diagnostics: Vorkath is undead, living targets are eligible. */
final class EnchantedBoltRegression50_4_24 {
 private static EquipmentCandidate ammo(){return new EquipmentCandidate(new BankItem(1,1,"Onyx bolts (e)"),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition monster(String name,String... attrs){return new MonsterDefinition(1,name,214,308,308,150,750,26,108,108,240,26,26,26,Arrays.asList(attrs));}
 private static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 static boolean vorkathBlocksLifeLeech(){
  EquipmentCandidate a=ammo(); MonsterDefinition v=monster("Vorkath","boss","dragon","fiery","undead","dragonfire immune");
  int max=40; double acc=.72, normal=acc*MeleeWeaponEffects.successfulHitAverage(max);
  return near(EnchantedBoltEffects.expectedDamagePerAttack(a,v,105,max,acc,true),normal)
      && EnchantedBoltEffects.validationLabel(a,v,true).contains("BLOCKED (undead)")
      && EnchantedBoltEffects.validationLabel(a,v,true).contains("0.00%");
 }
 static boolean livingShowsKandarinRate(){
  EquipmentCandidate a=ammo(); MonsterDefinition living=monster("Living target","boss");
  String label=EnchantedBoltEffects.validationLabel(a,living,true);
  return label.contains("12.10%") && label.contains("after hit");
 }
 private EnchantedBoltRegression50_4_24(){}
}
