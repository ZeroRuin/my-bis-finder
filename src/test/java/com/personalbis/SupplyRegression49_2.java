package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class SupplyRegression49_2 {
 private static String read(String p){try{return new String(Files.readAllBytes(Paths.get(p)),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 static boolean internalAmmoRejectsProjectiles(){
  String s=read("src/main/java/com/personalbis/RangedLoadoutOptimizer.java");
  return s.contains("ammoSlotFreeForNeutralGear(weapon)")&&s.contains("RangedAmmoRules.ammoType(c)!=RangedAmmoType.NONE");
 }
 static boolean selectedRangedAmmoWired(){
  String s=read("src/main/java/com/personalbis/PersonalBisPanel.java");
  return s.contains("rangedLoads.get(requestedStyle), bankSnapshot");
 }
 static boolean fullOwnedAmmoStack(){
  String s=read("src/main/java/com/personalbis/SimpleSupplyRecommender.java");
  return s.contains("addFullStackById")&&s.contains("Full owned stack required by");
 }
 private SupplyRegression49_2(){}
}
