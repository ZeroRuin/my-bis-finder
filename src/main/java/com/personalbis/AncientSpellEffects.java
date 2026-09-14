package com.personalbis;
public final class AncientSpellEffects {
 private AncientSpellEffects(){}
 public static boolean isAncient(MagicSpell s){return s!=null&&"ancient".equals(s.spellbook);}
 public static boolean isMultiTarget(MagicSpell s){return isAncient(s)&&(s.name.contains("Burst")||s.name.contains("Barrage"));}
 public static int maxTargets(MagicSpell s){return isMultiTarget(s)?9:1;}
 public static double bloodHealFraction(MagicSpell s){return isAncient(s)&&s.name.startsWith("Blood ")?0.25:0.0;}
 public static int freezeTicks(MagicSpell s){
  if(!isAncient(s)||!s.name.startsWith("Ice "))return 0;
  if(s.name.endsWith("Rush"))return 8;if(s.name.endsWith("Burst"))return 16;
  if(s.name.endsWith("Blitz"))return 24;if(s.name.endsWith("Barrage"))return 32;return 0;
 }
 public static int attackReductionPercent(MagicSpell s){
  if(!isAncient(s)||!s.name.startsWith("Shadow "))return 0;
  return (s.name.endsWith("Rush")||s.name.endsWith("Burst"))?10:15;
 }
 public static int startingPoison(MagicSpell s){return isAncient(s)&&s.name.startsWith("Smoke ")?4:0;}
 public static boolean isAncientSceptre(String weapon){
  String w=weapon==null?"":weapon.toLowerCase();return w.equals("ancient sceptre")||w.contains("ancient sceptre (l)")
   ||w.startsWith("blood ancient sceptre")||w.startsWith("ice ancient sceptre")
   ||w.startsWith("shadow ancient sceptre")||w.startsWith("smoke ancient sceptre");
 }
 public static double secondaryEffectMultiplier(String weapon){return isAncientSceptre(weapon)?1.10:1.0;}
 public static double bloodHealFraction(MagicSpell s,String weapon){return bloodHealFraction(s)*secondaryEffectMultiplier(weapon);}
 public static double freezeSeconds(MagicSpell s,String weapon){return freezeTicks(s)*0.6*secondaryEffectMultiplier(weapon);}
 public static String note(MagicSpell s){return note(s,null);}
 public static String note(MagicSpell s,String weapon){
  if(!isAncient(s))return "";
  String w=weapon==null?"":weapon.toLowerCase();
  StringBuilder n=new StringBuilder("Ancient");
  if(isMultiTarget(s))n.append(": up to 9 targets");
  if(bloodHealFraction(s)>0)n.append(isMultiTarget(s)?", heals ":"; heals ").append(trim(bloodHealFraction(s,weapon)*100)).append("% damage");
  int f=freezeTicks(s);if(f>0)n.append(isMultiTarget(s)?", freeze ":"; freeze ").append(trim(freezeSeconds(s,weapon))).append("s");
  int a=attackReductionPercent(s);if(a>0)n.append(isMultiTarget(s)?", Attack -":"; Attack -").append(a).append("%");
  int poison=startingPoison(s);if(poison>0)n.append(isMultiTarget(s)?", poison starts ":"; poison starts ").append(poison);
  if(isAncientSceptre(weapon))n.append("; sceptre +10% secondary effects");
  if(w.startsWith("blood ancient sceptre")&&s.name.startsWith("Blood "))n.append("; can overheal +10% HP");
  if(w.startsWith("ice ancient sceptre")&&s.name.startsWith("Ice "))n.append("; +10% accuracy if target is freezable and unfrozen (conditional; not ranked)");
  if(w.startsWith("shadow ancient sceptre")&&s.name.startsWith("Shadow "))n.append("; also drains Strength/Defence");
  if(w.startsWith("smoke ancient sceptre")&&s.name.startsWith("Smoke "))n.append("; poisoned target healing -20%");
  n.append("; ranked by single-target DPS");return n.toString();
 }
 private static String trim(double x){String s=String.format(java.util.Locale.ROOT,"%.2f",x);while(s.endsWith("0"))s=s.substring(0,s.length()-1);if(s.endsWith("."))s=s.substring(0,s.length()-1);return s;}
}
