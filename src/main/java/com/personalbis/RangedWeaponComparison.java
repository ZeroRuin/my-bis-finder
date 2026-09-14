package com.personalbis;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
public final class RangedWeaponComparison {
 private final String weaponName, ammoName, effectLabel, gearSummary; private final RangedAmmoType ammoType; private final RangedAmmoRules.DefenceClass defenceClass;
 private final RangedCombatResult result; private final Map<EquipmentSlot,EquipmentCandidate> items;
 public RangedWeaponComparison(String w,String a,RangedAmmoType t,RangedAmmoRules.DefenceClass dc,String e,RangedCombatResult r,String g,Map<EquipmentSlot,EquipmentCandidate> items){weaponName=w;ammoName=a;ammoType=t;defenceClass=dc;effectLabel=e;result=r;gearSummary=g;this.items=items==null?Collections.emptyMap():Collections.unmodifiableMap(new EnumMap<>(items));}
 public String getWeaponName(){return weaponName;} public String getAmmoName(){return ammoName;} public RangedAmmoType getAmmoType(){return ammoType;} public RangedAmmoRules.DefenceClass getDefenceClass(){return defenceClass;} public String getEffectLabel(){return effectLabel;} public RangedCombatResult getResult(){return result;} public String getGearSummary(){return gearSummary;} public Map<EquipmentSlot,EquipmentCandidate> getItems(){return items;}
}
