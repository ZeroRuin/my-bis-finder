package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class OptimizerCompletionRegression48_8_7 {
 private static String src(String n){try{return new String(Files.readAllBytes(Paths.get("src/main/java/com/personalbis/"+n)),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 static boolean rangedCompletionPresent(){
  String s=src("RangedLoadoutOptimizer.java");
  // Legacy direct-return form OR alpha50.4.15 profiler form, which times the
  // same neutral-slot completion call before returning the completed loadout.
  return s.contains("return completeNeutralSlots(best,ranked,weapon,m,task);")
      ||(s.contains("completeNeutralSlots(best,ranked,weapon,m,task)")
         &&s.contains("return completed;"));
 }
 static boolean magicCompletionPresent(){
  String s=src("MagicLoadoutOptimizer.java");
  // Alpha 50.2 completes each independent loadout-family winner before it
  // competes at weapon/global level; the old assertion named weaponBest directly.
  return s.contains("familyBest=completeNeutralSlots(familyBest,ranked,m,task,weapon);")
      ||s.contains("weaponBest=completeNeutralSlots(weaponBest,ranked,m,task,weapon);");
 }
 static boolean rangedAmmoCompatibilityPresent(){
  String s=src("RangedLoadoutOptimizer.java");
  // Legacy completion path OR alpha50.4.9 anchored weapon/ammo path.  The
  // anchored path filters compatibility before a pair can enter the beam and
  // still guards the neutral-slot completion fallback.
  return s.contains("slot==EquipmentSlot.AMMO&&!RangedAmmoRules.compatible(weapon,c)")
      ||(s.contains("if(!RangedAmmoRules.compatible(weapon,ammo))continue;")
         &&s.contains("if(slot==EquipmentSlot.AMMO&&RangedAmmoRules.requiresEquippedAmmo(weapon)")
         &&s.contains("&&!RangedAmmoRules.compatible(weapon,c))continue;"));
 }
}
