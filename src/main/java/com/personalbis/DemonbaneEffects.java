package com.personalbis;

/** Wiki-source demonbane vulnerability scaling. */
public final class DemonbaneEffects {
 private DemonbaneEffects(){}
 public static int vulnerability(MonsterDefinition m){
  if(m==null)return 100;
  if(m.getId()==14179)return 200; // Yama void flare
  if(m.getId()==14176)return 120; // Yama
  if(m.getId()==7584||m.getId()==7585)return 115; // Ice Demon
  if(m.getName()!=null&&m.getName().equalsIgnoreCase("Duke Sucellus"))return 70;
  return 100;
 }
 public static double multiplier(MonsterDefinition m,int weaponPercent){
  int scaled=(int)Math.floor(weaponPercent*vulnerability(m)/100.0);
  return 1.0+scaled/100.0;
 }
}
