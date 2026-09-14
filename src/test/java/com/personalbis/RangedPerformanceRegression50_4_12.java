package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class RangedPerformanceRegression50_4_12 {
 private static String src(){try{return new String(Files.readAllBytes(Paths.get("src/main/java/com/personalbis/RangedLoadoutOptimizer.java")),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 static boolean accuracyFirstBeam(){String s=src();return s.contains("private static final int SLOT=12, BEAM=128, EXACT_FINALISTS=48;");}
 static boolean keepsAllWeaponsAndAnchoredAmmo(){String s=src();return
  s.contains("weapons=allUsable(ranked.get(EquipmentSlot.WEAPON))")&&
  s.contains("List<EquipmentCandidate> ammunition=allUsable(ranked.get(")&&
  s.contains("for(EquipmentCandidate rawAmmo:ammunition)")&&
  s.contains("if(!RangedAmmoRules.compatible(weapon,ammo))continue;")&&
  s.contains("optimizeWeaponAmmo(ranked,weapon,ammo,m,task,requiredEquipment)");}
}
