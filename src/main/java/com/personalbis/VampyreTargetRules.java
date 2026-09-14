package com.personalbis;

import java.util.Map;

/**
 * Wiki DPS parity: vampyre weapon eligibility and auto-attack modifiers.
 * Ported from upstream BaseCalc.wearingVampyrebane/isWearingSilverWeapon and
 * PlayerVsNPCCalc vampyre accuracy/damage/immunity stages.
 */
public final class VampyreTargetRules {
 private VampyreTargetRules(){}
 public static boolean isVampyre(MonsterDefinition m){return m!=null&&(m.hasAttribute("vampyre1")||m.hasAttribute("vampyre2")||m.hasAttribute("vampyre3"));}
 public static int tier(MonsterDefinition m){if(m==null)return 0;if(m.hasAttribute("vampyre3"))return 3;if(m.hasAttribute("vampyre2"))return 2;if(m.hasAttribute("vampyre1"))return 1;return 0;}
 private static String n(EquipmentCandidate c){return c==null||c.getItem()==null?"":c.getItem().getName().toLowerCase();}
 public static boolean has(Map<EquipmentSlot,EquipmentCandidate> gear,String needle){if(gear==null)return false;for(EquipmentCandidate c:gear.values())if(n(c).contains(needle.toLowerCase()))return true;return false;}
 public static boolean isSilverWeapon(EquipmentCandidate w){String x=n(w);return x.contains("blessed axe")||x.contains("ivandis flail")||x.contains("blisterwood flail")||x.contains("hallowed flail")||x.contains("silver sickle")||x.contains("emerald sickle")||x.contains("ruby sickle")||x.contains("blisterwood sickle")||x.contains("silverlight")||x.contains("darklight")||x.contains("arclight")||x.contains("rod of ivandis")||x.contains("wolfbane");}
 public static boolean isRangedSilver(EquipmentCandidate w,EquipmentCandidate ammo){String wn=n(w),an=n(ammo);return wn.contains("blisterwood stake")||an.contains("silver bolts");}
 public static boolean fullVampyrebane(EquipmentCandidate w,MonsterDefinition m){String x=n(w);int t=tier(m);if(t==0)return false;if(x.contains("blisterwood stake"))return true;if(t==2&&x.contains("rod of ivandis"))return true;return x.contains("ivandis flail")||x.contains("blisterwood sickle")||x.contains("blisterwood flail")||x.contains("hallowed flail")||x.contains("sunspear");}
 public static boolean canDamageMelee(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,MonsterDefinition m){int t=tier(m);if(t<=1)return true;if(t==3)return fullVampyrebane(w,m);return fullVampyrebane(w,m)||has(gear,"efaritay's aid")||isSilverWeapon(w);}
 public static boolean canDamageRanged(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,EquipmentCandidate ammo,MonsterDefinition m){int t=tier(m);if(t<=1)return true;if(t==3)return n(w).contains("blisterwood stake");return n(w).contains("blisterwood stake")||has(gear,"efaritay's aid")||isRangedSilver(w,ammo);}
 public static double meleeAccuracyMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,MonsterDefinition m){if(!isVampyre(m))return 1.0;String x=n(w);double z=1.0;if(x.contains("blisterwood flail")||x.contains("blisterwood sickle"))z*=1.05;else if(x.contains("hallowed flail")||x.contains("sunspear"))z*=1.25;if(has(gear,"efaritay's aid")&&isSilverWeapon(w))z*=1.15;return z;}
 public static double meleeDamageMultiplier(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,MonsterDefinition m){if(!isVampyre(m))return 1.0;String x=n(w);double z=1.0;boolean ef=has(gear,"efaritay's aid");if(x.contains("blisterwood flail")||x.contains("hallowed flail")||x.contains("blisterwood stake"))z=1.25;else if(x.contains("sunspear"))z=1.50;else if(x.contains("blisterwood sickle"))z=1.15;else if(x.contains("ivandis flail"))z=1.20;else if(x.contains("rod of ivandis")&&tier(m)!=3)z=1.10;else if(isSilverWeapon(w)&&tier(m)==1)z=1.10;if(ef&&z>1.0)z*=1.10;return z;}
 /** Tier-2 fallback: Efaritay + non-vampyrebane deals half damage; silver without Efaritay is capped at 10. */
 public static boolean tier2HalfDamage(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,MonsterDefinition m){return tier(m)==2&&!fullVampyrebane(w,m)&&has(gear,"efaritay's aid");}
 public static boolean tier2SilverCap10(Map<EquipmentSlot,EquipmentCandidate> gear,EquipmentCandidate w,MonsterDefinition m){return tier(m)==2&&!fullVampyrebane(w,m)&&!has(gear,"efaritay's aid")&&isSilverWeapon(w);}
}
