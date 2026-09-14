package com.personalbis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Shared target damage-eligibility rules ported from the OSRS Wiki DPS calculator
 * PlayerVsNPCCalc.isImmune() path. Keep these checks before optimiser ranking.
 */
public final class WikiTargetEligibility {
 private WikiTargetEligibility() {}
 private static Set<Integer> ids(Integer... x){ return new HashSet<>(Arrays.asList(x)); }
 private static final Set<Integer> MELEE_IMMUNE=ids(494,7533,7706,7708,12214,12215,12219,2042,2043,2044);
 private static final Set<Integer> ZULRAH=ids(2042,2043,2044);
 private static final Set<Integer> VESPULA=ids(7530,7531,7532);
 private static final Set<Integer> NON_SALAMANDER_MELEE=ids(3169,3170,3171,3172,3173,3174,3175,3176,3177,3178,3179,3180,3181,3182,3183,7037);
 private static final Set<Integer> GUARDIANS=ids(7569,7571,7570,7572);
 private static final Set<Integer> RANGED_IMMUNE=ids(
  7540,7543,7544,7545, 7851,7854,7855,7882,7883,7886,7887,7888,7889, 7568,
  2463,2465,2467,2464,2466,2468,2137,2138,2139,2140,2141,2142);
 private static final Set<Integer> MAGIC_IMMUNE=ids(
  7851,7854,7855,7882,7883,7886,7887,7888,7889,
  2463,2465,2467,2464,2466,2468,2137,2138,2139,2140,2141,2142);

 private static String cat(EquipmentCandidate w){ return w==null?"":w.getWeaponCategory().trim().toLowerCase(); }
 public static boolean polearm(EquipmentCandidate w){ return cat(w).equals("polearm"); }
 public static boolean salamander(EquipmentCandidate w){ return cat(w).equals("salamander"); }
 public static boolean pickaxe(EquipmentCandidate w){ return cat(w).equals("pickaxe"); }

 public static boolean meleeCanDamage(MonsterDefinition m, EquipmentCandidate w){
  if(m==null)return true; int id=m.getId();
  if(MELEE_IMMUNE.contains(id)){
   // Wiki exception: Zulrah may be hit by polearms.
   if(ZULRAH.contains(id)&&polearm(w))return true;
   return false;
  }
  if(m.hasAttribute("flying")){
   // Vespula remains melee-immune despite carrying the flying attribute.
   if(VESPULA.contains(id))return false;
   if(!polearm(w)&&!salamander(w))return false;
  }
  if(NON_SALAMANDER_MELEE.contains(id)&&!salamander(w))return false;
  if(GUARDIANS.contains(id)&&!pickaxe(w))return false;
  return true;
 }
 public static boolean rangedCanDamage(MonsterDefinition m){
  if(m==null)return true;
  if(RANGED_IMMUNE.contains(m.getId()))return false;
  if(GUARDIANS.contains(m.getId()))return false;
  return true;
 }
 public static boolean magicCanDamage(MonsterDefinition m){
  if(m==null)return true;
  if(MAGIC_IMMUNE.contains(m.getId()))return false;
  if(GUARDIANS.contains(m.getId()))return false;
  return true;
 }
 public static boolean isGuardian(MonsterDefinition m){ return m!=null&&GUARDIANS.contains(m.getId()); }
}
