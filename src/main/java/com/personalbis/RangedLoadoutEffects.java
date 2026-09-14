package com.personalbis;
import java.util.Map;
public final class RangedLoadoutEffects {
 private RangedLoadoutEffects(){}
 public static double crystalAccuracy(Map<EquipmentSlot,EquipmentCandidate> g,EquipmentCandidate w){
  if(!RangedWeaponEffects.usesCrystalArmourBonus(w))return 1.0; double b=0;
  if(named(g.get(EquipmentSlot.HEAD),"crystal helm"))b+=0.05;
  if(named(g.get(EquipmentSlot.BODY),"crystal body"))b+=0.15;
  if(named(g.get(EquipmentSlot.LEGS),"crystal legs"))b+=0.10;
  return 1+b;
 }
 public static double crystalDamage(Map<EquipmentSlot,EquipmentCandidate> g,EquipmentCandidate w){
  if(!RangedWeaponEffects.usesCrystalArmourBonus(w))return 1.0; double b=0;
  if(named(g.get(EquipmentSlot.HEAD),"crystal helm"))b+=0.025;
  if(named(g.get(EquipmentSlot.BODY),"crystal body"))b+=0.075;
  if(named(g.get(EquipmentSlot.LEGS),"crystal legs"))b+=0.05;
  return 1+b;
 }
 public static boolean voidRanged(Map<EquipmentSlot,EquipmentCandidate> g){
  return named(g.get(EquipmentSlot.HEAD),"void ranger helm") &&
   (named(g.get(EquipmentSlot.BODY),"void knight top")||named(g.get(EquipmentSlot.BODY),"elite void top")) &&
   (named(g.get(EquipmentSlot.LEGS),"void knight robe")||named(g.get(EquipmentSlot.LEGS),"elite void robe")) &&
   named(g.get(EquipmentSlot.HANDS),"void knight gloves");
 }
 public static boolean eliteVoidRanged(Map<EquipmentSlot,EquipmentCandidate> g){
  return named(g.get(EquipmentSlot.HEAD),"void ranger helm") &&
   named(g.get(EquipmentSlot.BODY),"elite void top") &&
   named(g.get(EquipmentSlot.LEGS),"elite void robe") &&
   named(g.get(EquipmentSlot.HANDS),"void knight gloves");
 }
 public static double voidAccuracy(Map<EquipmentSlot,EquipmentCandidate> g){return voidRanged(g)?1.10:1.0;}
 public static double voidDamage(Map<EquipmentSlot,EquipmentCandidate> g){return voidRanged(g)?1.10:1.0;}
 public static boolean synergy(EquipmentCandidate c){if(c==null)return false;String n=c.getItem().getName().toLowerCase();
  return n.contains("crystal helm")||n.contains("crystal body")||n.contains("crystal legs")||n.contains("void ranger helm")||
   n.contains("void knight top")||n.contains("elite void top")||n.contains("void knight robe")||n.contains("elite void robe")||
   n.contains("void knight gloves")||n.contains("salve amulet(i)")||n.contains("salve amulet (i)")||
   n.contains("salve amulet(ei)")||n.contains("salve amulet (ei)")||n.contains("karil's coif")||
   n.contains("karil's leathertop")||n.contains("karil's leatherskirt")||n.contains("amulet of the damned");}
 private static boolean named(EquipmentCandidate c,String n){return c!=null&&c.getItem().getName().toLowerCase().contains(n);}

 public static double salveAccuracy(Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition m){
  if(!isUndead(m)) return 1.0;
  String n=neckName(gear);
  if(n.contains("salve amulet(ei)")||n.contains("salve amulet (ei)")) return 1.20;
  if(n.contains("salve amulet(i)")||n.contains("salve amulet (i)")) return 7.0/6.0;
  return 1.0;
 }
 public static double salveDamage(Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition m){
  return salveAccuracy(gear,m);
 }
 public static boolean rangedSalveActive(Map<EquipmentSlot,EquipmentCandidate> gear, MonsterDefinition m){
  return salveAccuracy(gear,m)>1.0;
 }
 private static boolean isUndead(MonsterDefinition m){
  return m!=null && (m.hasAttribute("undead") || m.hasAttribute("spectral"));
 }
 private static String neckName(Map<EquipmentSlot,EquipmentCandidate> gear){
  EquipmentCandidate c=gear.get(EquipmentSlot.NECK);
  return c==null?"":c.getItem().getName().toLowerCase();
 }
}
