package com.personalbis;
public final class MagicWeaponEffects {
 private MagicWeaponEffects(){}
 public static boolean isTwinflame(String raw){return norm(raw).equals("twinflame staff");}
 public static boolean isSmokeStaff(String raw){
  String n=norm(raw); return n.equals("smoke battlestaff")||n.equals("mystic smoke staff");
 }
 public static boolean isDragonHunterWand(String raw){return norm(raw).equals("dragon hunter wand");}
 public static boolean isStandardElemental(MagicSpell spell){return spell!=null&&!spell.element().isEmpty();}
 public static boolean isDraconic(MonsterDefinition m){
  return m!=null&&(m.hasAttribute("dragon")||m.hasAttribute("draconic"));
 }
 public static double accuracyMultiplier(String weapon,MagicSpell spell,MonsterDefinition m){
  if(isDragonHunterWand(weapon)&&isDraconic(m))return 1.75;
  if((isSmokeStaff(weapon)||isTwinflame(weapon))&&isStandardElemental(spell))return 1.10;
  return 1.0;
 }
 public static double damageMultiplier(String weapon,MagicSpell spell,MonsterDefinition m){
  if(isDragonHunterWand(weapon)&&isDraconic(m))return 1.40;
  if((isSmokeStaff(weapon)||isTwinflame(weapon))&&isStandardElemental(spell))return 1.10;
  return 1.0;
 }
 public static int attackTicks(String weapon){return isTwinflame(weapon)?6:5;}
 public static boolean twinflameEcho(String weapon,MagicSpell spell){
  if(!isTwinflame(weapon)||spell==null)return false;
  String t=spell.tier();return "bolt".equals(t)||"blast".equals(t)||"wave".equals(t);
 }
 public static double expectedHitMultiplier(String weapon,MagicSpell spell){
  return twinflameEcho(weapon,spell)?1.40:1.0;
 }
 public static String note(String weapon,MagicSpell spell,MonsterDefinition m){
  if(isDragonHunterWand(weapon)&&isDraconic(m))return "Dragonbane: +75% accuracy, +40% damage";
  if(isTwinflame(weapon)&&isStandardElemental(spell)){
   return twinflameEcho(weapon,spell)?"Twinflame: +10% accuracy/damage, 40% echo, 6t":"Twinflame: +10% accuracy/damage, 6t";
  }
  if(isSmokeStaff(weapon)&&isStandardElemental(spell))return "Smoke staff: +10% accuracy/damage";
  return "";
 }
 private static String norm(String x){return x==null?"":x.toLowerCase().trim();}
}