package com.personalbis;

import java.util.Collection;

/** Source-ordered sustained ranged roll/max modifiers from Wiki PlayerVsNPCCalc. */
public final class WikiRangedEffects {
 private WikiRangedEffects(){}

 public static int accuracyAfterExclusive(int roll,Collection<EquipmentCandidate> gear,MonsterDefinition m,
   boolean task,boolean slayer){
  if(avariceRevenant(gear,m))return WikiIntegerStages.factor(roll,24,20);
  if(RangedLoadoutEffects.rangedSalveActive(asMap(gear),m))return WikiIntegerStages.factor(roll,
    RangedLoadoutEffects.salveAccuracy(asMap(gear),m)>=1.2?6:7,
    RangedLoadoutEffects.salveAccuracy(asMap(gear),m)>=1.2?5:6);
  return task&&slayer?WikiIntegerStages.factor(roll,23,20):roll;
 }

 public static int damageAfterExclusive(int max,Collection<EquipmentCandidate> gear,MonsterDefinition m,
   EquipmentCandidate weapon,boolean task,boolean slayer){
  if(avariceRevenant(gear,m))return WikiIntegerStages.factor(max,24,20);
  java.util.Map<EquipmentSlot,EquipmentCandidate> map=asMap(gear);
  if(RangedLoadoutEffects.rangedSalveActive(map,m))return WikiIntegerStages.factor(max,
    RangedLoadoutEffects.salveDamage(map,m)>=1.2?6:7,RangedLoadoutEffects.salveDamage(map,m)>=1.2?5:6);
  if(task&&slayer){
   int numerator=23;
   if(isChargedRevWeapon(weapon,m))numerator+=10;
   if(RangedWeaponEffects.isDragonHunterCrossbow(weapon)&&RangedWeaponEffects.isDraconic(m))numerator+=5;
   return WikiIntegerStages.factor(max,numerator,20);
  }
  return max;
 }

 public static int postAccuracy(int roll,EquipmentCandidate weapon,MonsterDefinition m,RangedStance stance,int distance){
  roll=(int)Math.floor(roll*RangedWeaponEffects.accuracyMultiplier(weapon,m));
  if(isChargedRevWeapon(weapon,m))roll=WikiIntegerStages.factor(roll,3,2);
  if(isChinchompa(weapon))roll=WikiIntegerStages.factor(roll,chinchompaNumerator(stance,distance),4);
  return roll;
 }

 public static int postDamage(int max,EquipmentCandidate weapon,MonsterDefinition m,boolean task,boolean slayer){
  if(isChargedRevWeapon(weapon,m)&&!(task&&slayer))max=WikiIntegerStages.factor(max,3,2);
  // Rev and dragon-hunter damage were already folded into the additive Slayer
  // factor above; without Slayer they remain ordinary multiplicative stages.
  if(!(task&&slayer&&RangedWeaponEffects.isDragonHunterCrossbow(weapon)&&RangedWeaponEffects.isDraconic(m)))
   max=(int)Math.floor(max*RangedWeaponEffects.damageMultiplier(weapon,m));
  if(isTonalztics(weapon))max=WikiIntegerStages.factor(max,3,4);
  return max;
 }

 public static int chinchompaNumerator(RangedStance stance,int distance){
  int d=Math.max(1,Math.min(7,distance));
  if(stance==RangedStance.ACCURATE)return d>=7?2:d>=4?3:4;       // Short fuse
  if(stance==RangedStance.RAPID)return d<4||d>=7?3:4;           // Medium fuse
  return d<4?2:d<7?3:4;                                        // Long fuse
 }

 public static boolean isChargedRevWeapon(EquipmentCandidate w,MonsterDefinition m){
  if(w==null||w.getItem()==null||!isWildernessTarget(m))return false;
  int id=w.getItem().getItemId();return id==22550||id==27655;
 }
 public static boolean isTonalztics(EquipmentCandidate w){return w!=null&&w.getItem()!=null&&
   w.getItem().getName().equalsIgnoreCase("Tonalztics of Ralos");}
 public static boolean isChinchompa(EquipmentCandidate w){return w!=null&&w.getItem()!=null&&
   w.getItem().getName().toLowerCase().contains("chinchompa");}
 public static boolean isWildernessTarget(MonsterDefinition m){
  if(m==null)return false;String n=m.getName().toLowerCase(),v=m.getVersion().toLowerCase();
  if(n.startsWith("revenant")||n.contains("callisto")||n.contains("artio")||n.contains("venenatis")||
    n.contains("spindel")||n.contains("vet'ion")||n.contains("calvar'ion")||n.contains("scorpia")||
    n.contains("chaos elemental")||n.contains("chaos fanatic")||n.contains("crazy archaeologist"))return true;
  return n.contains("wilderness")||v.contains("wilderness");
 }
 private static boolean avariceRevenant(Collection<EquipmentCandidate> gear,MonsterDefinition m){
  return m!=null&&m.getName().toLowerCase().startsWith("revenant")&&has(gear,"Amulet of avarice");
 }
 private static boolean has(Collection<EquipmentCandidate> gear,String name){if(gear!=null)for(EquipmentCandidate c:gear)
  if(c!=null&&c.getItem()!=null&&c.getItem().getName().equalsIgnoreCase(name))return true;return false;}
 private static java.util.Map<EquipmentSlot,EquipmentCandidate> asMap(Collection<EquipmentCandidate> gear){
  java.util.Map<EquipmentSlot,EquipmentCandidate> out=new java.util.EnumMap<>(EquipmentSlot.class);
  if(gear!=null)for(EquipmentCandidate c:gear)if(c!=null)out.put(c.getSlot(),c);return out;
 }
}
