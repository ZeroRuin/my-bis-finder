package com.personalbis;
/** Phase-1 weapon/ammunition compatibility and ranged defence classification. */
public final class RangedAmmoRules {
 private RangedAmmoRules(){}

 public static RangedAmmoType weaponAmmo(EquipmentCandidate w){
  if(w==null)return RangedAmmoType.NONE;
  String n=name(w), c=w.getWeaponCategory().toLowerCase();
  if(n.contains("bow of faerdhinen")||n.contains("crystal bow")||n.contains("twisted bow")&&false)return RangedAmmoType.INTERNAL;
  if(n.contains("blowpipe"))return RangedAmmoType.DART;
  if(c.contains("crossbow")||n.contains("crossbow"))return RangedAmmoType.BOLT;
  if(c.equals("bow")||n.endsWith(" bow")||n.contains("shortbow")||n.contains("longbow")||n.contains("twisted bow"))return RangedAmmoType.ARROW;
  if(c.contains("thrown")||n.contains("knife")||n.contains("javelin")||n.contains("thrownaxe"))return RangedAmmoType.INTERNAL;
  return RangedAmmoType.NONE;
 }

 public static RangedAmmoType ammoType(EquipmentCandidate a){
  if(a==null)return RangedAmmoType.NONE; String n=name(a);
  if(n.contains("arrow"))return RangedAmmoType.ARROW;
  if(n.contains("bolt")&&!n.contains("bolt pouch"))return RangedAmmoType.BOLT;
  if(n.contains("dart"))return RangedAmmoType.DART;
  return RangedAmmoType.NONE;
 }

 public static boolean compatible(EquipmentCandidate w,EquipmentCandidate a){
  // Blowpipe darts are stored inside the weapon rather than equipped in the
  // ammo slot. The Wiki equipment table therefore reports no equipped-ammo
  // applicability; production optimisation must still pair the weapon with
  // an owned dart so its Ranged Strength is included.
  if(usesLoadedDarts(w))return a!=null&&ammoType(a)==RangedAmmoType.DART;
  if(w!=null){ WikiEquipmentEngine.AmmoApplicability wa=WikiEquipmentEngine.ammoApplicability(w.getItem().getItemId(),a==null?null:a.getItem().getItemId()); if(wa==WikiEquipmentEngine.AmmoApplicability.INVALID)return false; if(wa==WikiEquipmentEngine.AmmoApplicability.INCLUDED)return true; }
  RangedAmmoType need=weaponAmmo(w);
  if(need==RangedAmmoType.NONE||need==RangedAmmoType.INTERNAL)return a==null;
  if(a==null||ammoType(a)!=need)return false;
  return tierCompatible(w,a);
 }
 public static String ammoDisplay(EquipmentCandidate w){
  RangedAmmoType t=weaponAmmo(w);
  if(t==RangedAmmoType.INTERNAL)return "Internal";
  if(t==RangedAmmoType.DART)return "Loaded darts";
  if(t==RangedAmmoType.NONE)return "None";
  return t.toString();
 }
 private static boolean tierCompatible(EquipmentCandidate w,EquipmentCandidate a){
  String wn=name(w), an=name(a);

  // Dedicated ammunition families must be resolved before the generic metal-bolt
  // progression. Their names contain "bolt", but they are not interchangeable.
  if(isHuntersSunlightCrossbow(wn))
   return an.contains("sunlight antler bolt")||an.contains("moonlight antler bolt");
  if(isHuntersCrossbow(wn))
   return an.contains("kebbit bolt")||an.contains("long kebbit bolt");
  if(wn.contains("karil")&&wn.contains("crossbow"))return an.contains("bolt rack");
  if(wn.contains("dorgeshuun crossbow"))return an.contains("bone bolt");
  if(wn.contains("ballista"))return an.contains("javelin");

  // Special-family ammunition must never leak into ordinary crossbows.
  if(an.contains("kebbit bolt")||an.contains("antler bolt")||an.contains("bolt rack")||an.contains("bone bolt"))
   return false;

  if(weaponAmmo(w)==RangedAmmoType.ARROW){
   int cap=arrowCap(wn), tier=arrowTier(an); return tier==0||cap==0||tier<=cap;
  }
  if(weaponAmmo(w)==RangedAmmoType.BOLT){
   // Crossbow families have many special exceptions; enforce the common metal progression.
   int cap=boltCap(wn), tier=boltTier(an); return tier==0||cap==0||tier<=cap;
  }
  return true;
 }
 private static boolean isHuntersSunlightCrossbow(String n){
  return n.contains("hunter")&&n.contains("sunlight")&&n.contains("crossbow");
 }
 private static boolean isHuntersCrossbow(String n){
  return n.contains("hunter")&&n.contains("crossbow")&&!n.contains("sunlight");
 }
 private static int arrowTier(String n){
  if(n.contains("dragon arrow"))return 7;if(n.contains("amethyst arrow"))return 6;if(n.contains("rune arrow"))return 5;
  if(n.contains("adamant arrow"))return 4;if(n.contains("mithril arrow"))return 3;if(n.contains("steel arrow"))return 2;if(n.contains("iron arrow"))return 1;return 0;
 }
 private static int arrowCap(String n){
  if(n.contains("twisted bow")||n.contains("dark bow"))return 7;if(n.contains("magic")||n.contains("seercull"))return 5;
  if(n.contains("yew"))return 5;if(n.contains("maple"))return 4;if(n.contains("willow"))return 3;if(n.contains("oak"))return 2;if(n.contains("shortbow")||n.contains("longbow"))return 1;return 0;
 }
 private static int boltTier(String n){
  if(n.contains("dragon bolt")||n.contains("dragon bolts"))return 7;
  if(n.contains("runite bolt")||n.contains("rune bolt")||n.contains("dragonstone bolt")||n.contains("onyx bolt"))return 6;
  if(n.contains("adamant bolt")||n.contains("ruby bolt")||n.contains("diamond bolt"))return 5;
  if(n.contains("mithril bolt")||n.contains("sapphire bolt")||n.contains("emerald bolt"))return 4;
  if(n.contains("steel bolt")||n.contains("red topaz bolt"))return 3;
  if(n.contains("iron bolt")||n.contains("pearl bolt"))return 2;
  if(n.contains("bronze bolt")||n.contains("opal bolt"))return 1;
  return 0;
 }
 private static int boltCap(String n){
  if(n.contains("dragon hunter crossbow")||n.contains("dragon crossbow")||n.contains("zaryte crossbow"))return 7;
  if(n.contains("rune crossbow"))return 6;if(n.contains("adamant crossbow"))return 5;if(n.contains("mithril crossbow"))return 4;
  if(n.contains("steel crossbow"))return 3;if(n.contains("iron crossbow"))return 2;if(n.contains("bronze crossbow"))return 1;return 0;
 }

 public enum DefenceClass { LIGHT, STANDARD, HEAVY }

 public static DefenceClass defenceClass(EquipmentCandidate w){
  if(w==null)return DefenceClass.STANDARD;
  String n=name(w), c=w.getWeaponCategory().toLowerCase();
  // Light: arrows, darts, knives and blowpipe-like rapid projectiles.
  if(weaponAmmo(w)==RangedAmmoType.ARROW || weaponAmmo(w)==RangedAmmoType.DART ||
     n.contains("knife") || n.contains("dart") || n.contains("blowpipe"))
   return DefenceClass.LIGHT;
  // Heavy projectile families. Keep crossbows STANDARD: bolts use standard defence.
  if(n.contains("ballista") || n.contains("javelin") || n.contains("thrownaxe") ||
     c.contains("ballista"))
   return DefenceClass.HEAVY;
  return DefenceClass.STANDARD;
 }

 public static int targetDefence(MonsterDefinition m,EquipmentCandidate w){
  if(m==null)return 0;
  switch(defenceClass(w)){
   case LIGHT:return m.getLightDefence();
   case HEAVY:return m.getHeavyDefence();
   default:return m.getStandardDefence();
  }
 }

 /** True when the equipped ammo slot is reserved for ammunition. */
 public static boolean requiresEquippedAmmo(EquipmentCandidate w){
  RangedAmmoType t=weaponAmmo(w);
  return t==RangedAmmoType.ARROW||t==RangedAmmoType.BOLT;
 }

 /** True when the ammo equipment slot is free for a blessing/neutral item. */
 public static boolean ammoSlotFreeForNeutralGear(EquipmentCandidate w){
  return !requiresEquippedAmmo(w);
 }

 public static boolean usesLoadedDarts(EquipmentCandidate w){return w!=null&&name(w).contains("blowpipe");}

 /** Defensive guard for mandatory-shield targets when upstream item metadata is incomplete. */
 public static boolean occupiesBothHands(EquipmentCandidate w){
  if(w==null)return false;
  String n=name(w);
  // Rune crossbows are one-handed. Keep this authoritative override ahead of
  // imported metadata so mandatory shields remain eligible after data refreshes.
  if(n.equals("rune crossbow")||n.startsWith("rune crossbow ("))return false;
  return w.isTwoHanded()||n.contains("blowpipe")
    ||((n.endsWith(" bow")||n.contains("shortbow")||n.contains("longbow")||n.contains("twisted bow"))
       &&!n.contains("crossbow"));
 }

 public static boolean usesExternalAmmo(EquipmentCandidate w){
  return requiresEquippedAmmo(w);
 }
 private static String name(EquipmentCandidate c){return c.getItem().getName().toLowerCase();}
}
