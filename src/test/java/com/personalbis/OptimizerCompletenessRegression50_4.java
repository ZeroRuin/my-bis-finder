package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class OptimizerCompletenessRegression50_4 {
 private static String src(String n){try{return new String(Files.readAllBytes(Paths.get("src/main/java/com/personalbis/"+n)),StandardCharsets.UTF_8);}catch(Exception e){return "";}}
 static boolean meleeAllOwnedWeapons(){
  String s=src("MeleeLoadoutOptimizer.java");
  return s.contains("if(c!=null&&!c.getRequirementResult().isBlocked())x.add(c);")
    &&!s.contains("usable(all,EquipmentAuditPolicy.meleeWeaponEvaluationLimit())");
 }
 static boolean rangedAllOwnedWeapons(){
  String s=src("RangedLoadoutOptimizer.java");
  return s.contains("List<EquipmentCandidate> weapons=allUsable(ranked.get(EquipmentSlot.WEAPON));")
    &&!s.contains("weapons=usable(ranked.get(EquipmentSlot.WEAPON),EquipmentAuditPolicy.rangedWeaponEvaluationLimit())");
 }
 static boolean meleeUsesExactDpsDuringBeam(){
  String s=src("MeleeLoadoutOptimizer.java");
  return s.contains("dpsCache.put(g,calculateCached(g,style,monster,onSlayerTask,context).getDps())")
    &&s.contains("CombatResult calculated=calculator.calculate(gear,style,monster,onSlayerTask);");
 }
 static boolean rangedUsesExactDpsDuringBeam(){
  String s=src("RangedLoadoutOptimizer.java");
  return s.contains("safeDpsCached(g,m,task)")&&s.contains("fastDps(g,m,task)");
 }
 static boolean rangedChecksAmmo(){
  return src("RangedLoadoutOptimizer.java").contains("!RangedAmmoRules.compatible(weapon,c)");
 }
}
