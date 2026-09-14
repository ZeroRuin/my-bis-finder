package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class MagicOptimizerRegression50_3 {
 private static String src(String n){try{return new String(Files.readAllBytes(Paths.get("src/main/java/com/personalbis/"+n)),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 static boolean attackFirstPresent(){
  String s=src("MagicLoadoutOptimizer.java");
  return s.contains("topAttackCandidates(bank,m,15)")&&s.contains("weaponsForAttacks(");
 }
 static boolean noMagicTop12Gate(){
  String s=src("MagicLoadoutOptimizer.java");
  return !s.contains("magicWeapons(ranked.get(EquipmentSlot.WEAPON),m)");
 }
 static boolean blueMoonStandardAutocast(){
  return MagicSpellCompatibility.canAutocast("Blue moon spear",MagicSpell.standard().get(16)); // Wind Surge
 }
 static boolean blueMoonAncientAutocast(){
  return MagicSpellCompatibility.canAutocastAncient("blue moon spear");
 }
 static boolean markDemonbanePresent(){
  for(MagicSpell s:MagicSpell.arceuus())if(s.name.equals("Dark Demonbane + Mark of Darkness")&&s.markOfDarkness)return true;
  return false;
 }
 static boolean markRunePackagePresent(){
  for(MagicSpell s:MagicSpell.arceuus())if(s.name.equals("Dark Demonbane + Mark of Darkness"))
   return s.runes.getOrDefault("Cosmic rune",0)==1&&s.runes.getOrDefault("Soul rune",0)==3;
  return false;
 }
}