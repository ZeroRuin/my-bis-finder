package com.personalbis;
import java.util.Arrays;
final class EnchantedBoltRegression50_4_21 {
 private static EquipmentCandidate ammo(String n){return new EquipmentCandidate(new BankItem(1,1,n),EquipmentSlot.AMMO,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));}
 private static MonsterDefinition monster(String n,String...attrs){return new MonsterDefinition(1,n,100,100,100,100,500,0,0,0,0,0,0,0,Arrays.asList(attrs));}
 private static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 static boolean opalDiary(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); return near(EnchantedBoltEffects.expectedDamagePerAttack(ammo("Opal bolts (e)"),monster("x"),105,40,.5,true),normal+.055*10-.5*.055/41.0); }
 static boolean pearlNormalAndFiery(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); double a=EnchantedBoltEffects.expectedDamagePerAttack(ammo("Pearl bolts (e)"),monster("x"),105,40,.5,false); double b=EnchantedBoltEffects.expectedDamagePerAttack(ammo("Pearl bolts (e)"),monster("fire","fiery"),105,40,.5,false); return near(a,normal+.06*5-.5*.06/41.0)&&near(b,normal+.06*7-.5*.06/41.0); }
 static boolean onyxUndeadBlocked(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); return near(EnchantedBoltEffects.expectedDamagePerAttack(ammo("Onyx bolts (e)"),monster("undead","undead"),105,40,.5,true),normal); }
 static boolean onyxLivingDiary(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); double ev=EnchantedBoltEffects.expectedDamagePerAttack(ammo("Onyx bolts (e)"),monster("living"),105,40,.5,true); return near(ev,0.5*((1-.121)*MeleeWeaponEffects.successfulHitAverage(40)+.121*MeleeWeaponEffects.successfulHitAverage(48))); }
 static boolean dragonstoneImmune(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); return near(EnchantedBoltEffects.expectedDamagePerAttack(ammo("Dragonstone bolts (e)"),monster("dragon","dragon","fiery"),105,40,.5,true),normal); }
 static boolean utilityNotInvented(){ double normal=.5*MeleeWeaponEffects.successfulHitAverage(40); String[] names={"Jade bolts (e)","Topaz bolts (e)","Sapphire bolts (e)","Emerald bolts (e)"}; for(String n:names)if(!near(EnchantedBoltEffects.expectedDamagePerAttack(ammo(n),monster("x"),105,40,.5,true),normal))return false; return true; }
 private EnchantedBoltRegression50_4_21(){}
}
