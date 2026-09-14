package com.personalbis;
import java.util.Arrays;
/** Exact Vorkath Diamond-bolt checkpoint validated against OSRS Wiki + in-game POH dummy max hits. */
final class EnchantedBoltRegression50_4_23 {
 private static EquipmentCandidate ammo(String n){return new EquipmentCandidate(new BankItem(1,1,n),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition vorkath(){return new MonsterDefinition(1,"Vorkath",214,308,308,150,750,26,108,108,240,26,26,26,Arrays.asList("boss","dragon","fiery","undead","dragonfire immune"));}
 private static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 static boolean vorkathDiamondWikiParity(){
  MonsterDefinition v=vorkath(); EquipmentCandidate diamond=ammo("Diamond bolts (e)");
  int attackRoll=35824, defenceRoll=20070, normalMax=38;
  double accuracy=1-(defenceRoll+2.0)/(2*(attackRoll+1.0));
  double ev=EnchantedBoltEffects.expectedDamagePerAttack(diamond,v,105,normalMax,accuracy,true);
  double dps=ev/3.0;
  return near(accuracy,0.7198604326587579)&&near(ev,14.556767500492064)&&near(dps,4.852255833497355)
      && EnchantedBoltEffects.validatedProcMaxHit(diamond,v,105,normalMax)==43;
 }
 static boolean rubyProcMaxStill100(){return EnchantedBoltEffects.validatedProcMaxHit(ammo("Ruby bolts (e)"),vorkath(),105,38)==100;}
 private EnchantedBoltRegression50_4_23(){}
}
