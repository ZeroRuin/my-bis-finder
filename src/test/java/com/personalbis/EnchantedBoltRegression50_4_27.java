package com.personalbis;
import java.util.Arrays;
/** Wiki-locked enchanted-bolt checkpoints captured during the alpha50.4.26.2 validation pass. */
final class EnchantedBoltRegression50_4_27 {
 private static EquipmentCandidate ammo(String n){return new EquipmentCandidate(new BankItem(1,1,n),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition monster(String name,int hp,String... attrs){return new MonsterDefinition(1,name,214,308,308,150,hp,26,108,108,240,26,26,26,Arrays.asList(attrs));}
 private static MonsterDefinition vorkath(){return monster("Vorkath",750,"boss","dragon","fiery","undead","dragonfire immune");}
 private static boolean near(double a,double b,double eps){return Math.abs(a-b)<eps;}
 private static final double VORK_ACC=0.7198604326587579;
 private static boolean vorkath(String name,int normalMax,double wikiEv,int procMax){
  EquipmentCandidate a=ammo(name); MonsterDefinition v=vorkath();
  double ev=EnchantedBoltEffects.expectedDamagePerAttack(a,v,99,normalMax,VORK_ACC,true);
  return near(ev,wikiEv,0.00001) && EnchantedBoltEffects.validatedProcMaxHit(a,v,99,normalMax)==procMax;
 }
 static boolean wikiLockedVorkathSuite(){
  return vorkath("Opal bolts (e)",18,7.00955,27)
   && vorkath("Jade bolts (e)",21,7.59126,21)
   && vorkath("Pearl bolts (e)",26,9.77909,32)
   && vorkath("Topaz bolts (e)",30,10.82113,30)
   && vorkath("Sapphire bolts (e)",33,11.89887,33)
   && vorkath("Emerald bolts (e)",34,12.25819,34)
   && vorkath("Ruby bolts (e)",38,19.39188,100)
   && vorkath("Diamond bolts (e)",38,14.55677,43)
   && vorkath("Dragonstone bolts (e)",42,15.13381,42)
   && vorkath("Onyx bolts (e)",42,15.13381,42);
 }
 static boolean onyxActiveKrilWikiParity(){
  MonsterDefinition kril=monster("K'ril Tsutsaroth",255,"boss","demon"); EquipmentCandidate a=ammo("Onyx bolts (e)");
  double accuracy=31034.0/(2.0*(40176.0+1.0));
  double ev=EnchantedBoltEffects.expectedDamagePerAttack(a,kril,105,35,accuracy,true);
  return near(accuracy,0.3862159942255519,1e-12)
   && near(ev,6.932859273538236,1e-12) && near(ev/3.0,2.310953091179412,1e-12)
   && EnchantedBoltEffects.validatedProcMaxHit(a,kril,105,35)==42;
 }
 static boolean pearlNonFieryBranch(){
  MonsterDefinition living=monster("Living target",500,"boss"); EquipmentCandidate a=ammo("Pearl bolts (e)");
  int max=26, level=105; double acc=VORK_ACC;
  double normal=acc*MeleeWeaponEffects.successfulHitAverage(max);
  double expected=acc*((1-.066)*MeleeWeaponEffects.successfulHitAverage(max)+.066*(max/2.0+Math.floor(level/20.0)))+(1-acc)*.066*Math.floor(level/20.0);
  String label=EnchantedBoltEffects.validationLabel(a,living,level,true);
  return near(EnchantedBoltEffects.expectedDamagePerAttack(a,living,level,max,acc,true),expected,1e-12)
   && EnchantedBoltEffects.validatedProcMaxHit(a,living,level,max)==31
   && label.contains("6.60%") && label.contains("+5 damage") && !label.contains("fiery");
 }
 static boolean dragonstoneActiveBranch(){return EnchantedBoltRegression50_4_25.livingKandarinAndProcDamage();}
 private EnchantedBoltRegression50_4_27(){}
}
