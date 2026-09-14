package com.personalbis;
public final class CombatEffectiveLevels {
 private CombatEffectiveLevels(){}
 public static int melee(int boosted,double prayer,int styleBonus,boolean voidMelee){
  int e=(int)Math.floor(Math.max(1,boosted)*prayer)+styleBonus+8;
  return voidMelee?(int)Math.floor(e*1.10):e;
 }
 public static int ranged(int boosted,double prayer,int styleBonus,boolean voidRanged){
  int e=(int)Math.floor(Math.max(1,boosted)*prayer)+styleBonus+8;
  return voidRanged?(int)Math.floor(e*11.0/10.0):e;
 }
 public static int rangedDamage(int boosted,double prayer,int styleBonus,boolean voidRanged,boolean eliteVoidRanged){
  int e=(int)Math.floor(Math.max(1,boosted)*prayer)+styleBonus+8;
  if(eliteVoidRanged)return (int)Math.floor(e*9.0/8.0);
  return voidRanged?(int)Math.floor(e*11.0/10.0):e;
 }
 public static int magic(int boosted,double prayer,boolean voidMagic,int poweredStyleBonus){
  int e=(int)Math.floor(Math.max(1,boosted)*prayer);
  if(voidMagic)e=(int)Math.floor(e*1.45);
  return e+poweredStyleBonus+9;
 }
}
