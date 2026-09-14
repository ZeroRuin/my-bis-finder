package com.personalbis;
/** Synthetic DEV bolt-suite regressions. Ownership must not be required to exercise mechanics. */
final class EnchantedBoltRegression50_4_26 {
 private static EquipmentCandidate ammo(String name){return new EquipmentCandidate(new BankItem(1,1,name),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition monster(String... attrs){return new MonsterDefinition(1,"Synthetic",100,500,100,0,0,
  0,0,0,0,0,0,0,java.util.Arrays.asList(attrs));}
 static boolean procMaxDiagnostics(){
  MonsterDefinition living=monster(); MonsterDefinition fiery=monster("fiery"); MonsterDefinition undead=monster("undead");
  return EnchantedBoltEffects.validatedProcMaxHit(ammo("Onyx bolts (e)"),living,105,42)==50
   && EnchantedBoltEffects.validatedProcMaxHit(ammo("Onyx bolts (e)"),undead,105,42)==42
   && EnchantedBoltEffects.validatedProcMaxHit(ammo("Opal bolts (e)"),living,105,20)==30
   && EnchantedBoltEffects.validatedProcMaxHit(ammo("Pearl bolts (e)"),living,105,20)==25
   && EnchantedBoltEffects.validatedProcMaxHit(ammo("Pearl bolts (e)"),fiery,105,20)==27;
 }
 static boolean labelsCoverSuite(){
  MonsterDefinition living=monster();
  String[] names={"Opal bolts (e)","Jade bolts (e)","Pearl bolts (e)","Topaz bolts (e)","Sapphire bolts (e)","Emerald bolts (e)","Ruby bolts (e)","Diamond bolts (e)","Dragonstone bolts (e)","Onyx bolts (e)"};
  for(String n:names) if(EnchantedBoltEffects.validationLabel(ammo(n),living,105,true).trim().isEmpty()) return false;
  return true;
 }
 private EnchantedBoltRegression50_4_26(){}
}
