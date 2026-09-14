package com.personalbis;
import java.util.*;
/** Post-attacker per-hitsplat target transforms ported from PlayerVsNPCCalc.applyNpcTransforms(). */
public final class WikiNpcTransforms {
 private WikiNpcTransforms(){}
 private static final Set<Integer> TEKTON=new HashSet<>(Arrays.asList(7540,7543,7544,7545));
 private static final Set<Integer> GLOWING=new HashSet<>(Arrays.asList(7568));
 private static final Set<Integer> OLM_HEAD=new HashSet<>(Arrays.asList(7551,7554));
 private static final Set<Integer> OLM_MELEE=new HashSet<>(Arrays.asList(7552,7555));
 private static final Set<Integer> OLM_MAGE=new HashSet<>(Arrays.asList(7550,7553));
 private static final Set<Integer> ICE_DEMON=new HashSet<>(Arrays.asList(7584,7585));
 private static final Set<Integer> NIGHTMARE_TOTEM=new HashSet<>(Arrays.asList(9434,9437,9440,9443,9435,9438,9441,9444));
 public static HitDistribution apply(HitDistribution d, MonsterDefinition m, AttackStyle style, EquipmentCandidate weapon, String spellElement, boolean demonbane){
  return applyInternal(d,m,style,weapon==null?null:weapon.getItem().getName(),weapon==null?null:weapon.getWeaponCategory(),spellElement,demonbane);
 }
 /** Explicit name-based entry point for calculators that do not retain the weapon EquipmentCandidate. */
 public static HitDistribution applyByWeaponName(HitDistribution d, MonsterDefinition m, AttackStyle style, String weaponName, String spellElement, boolean demonbane){
  return applyInternal(d,m,style,weaponName,null,spellElement,demonbane);
 }
 private static HitDistribution applyInternal(HitDistribution d, MonsterDefinition m, AttackStyle style, String weaponName, String weaponCategory, String spellElement, boolean demonbane){
  if(d==null||m==null)return d; int id=m.getId(); String n=m.getName();
  if("Zulrah".equalsIgnoreCase(n)) d=d.transform(HitDistribution.cappedReroll(50,5,45));
  if("Fragment of Seren".equalsIgnoreCase(n)) d=d.transform(HitDistribution.linearMin(2,22));
  if(("Kraken".equalsIgnoreCase(n)||"Cave kraken".equalsIgnoreCase(n))&&style==AttackStyle.RANGED) d=d.transform(HitDistribution.divide(7,1));
  if(TEKTON.contains(id)&&style==AttackStyle.MAGIC) d=d.transform(HitDistribution.divide(5,1));
  if(GLOWING.contains(id)&&style==AttackStyle.MAGIC) d=d.transform(HitDistribution.divide(3,0));
  if((OLM_MELEE.contains(id)||OLM_HEAD.contains(id))&&style==AttackStyle.MAGIC) d=d.transform(HitDistribution.divide(3,0));
  if((OLM_MAGE.contains(id)||OLM_MELEE.contains(id))&&style==AttackStyle.RANGED) d=d.transform(HitDistribution.divide(3,0));
  boolean usingDemonbane=demonbane || isDemonbaneWeapon(weaponName);
  if(ICE_DEMON.contains(id)&&!"fire".equalsIgnoreCase(spellElement)&&!usingDemonbane) d=d.transform(HitDistribution.divide(3,0));
  if("Slagilith".equalsIgnoreCase(n) && !isPickaxe(weaponName,weaponCategory)) d=d.transform(HitDistribution.divide(3,0));
  if(NIGHTMARE_TOTEM.contains(id)&&style==AttackStyle.MAGIC) d=d.transform(HitDistribution.multiply(2,1,0));
  if(m.getFlatArmour()!=0 && style!=AttackStyle.MAGIC) d=d.transform(HitDistribution.flatAdd(-m.getFlatArmour(),0),false);
  return d.flatten();
 }
 private static boolean isDemonbaneWeapon(String weaponName){
  if(weaponName==null)return false;
  return weaponName.equalsIgnoreCase("Scorching bow") || weaponName.equalsIgnoreCase("Arclight") || weaponName.equalsIgnoreCase("Emberlight") || weaponName.equalsIgnoreCase("Silverlight") || weaponName.equalsIgnoreCase("Darklight");
 }
 private static boolean isPickaxe(String weaponName,String weaponCategory){
  if(weaponCategory!=null && weaponCategory.equalsIgnoreCase("pickaxe"))return true;
  return weaponName!=null && weaponName.toLowerCase(Locale.ROOT).contains("pickaxe");
 }

}
