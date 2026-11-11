package com.personalbis;
final class MagicOptimizerRegression50_3 {
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