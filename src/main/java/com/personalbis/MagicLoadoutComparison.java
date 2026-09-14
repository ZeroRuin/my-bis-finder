package com.personalbis;
public final class MagicLoadoutComparison {
 private final String weaponName,spellName; private final MagicCombatResult result;
 public MagicLoadoutComparison(String w,String s,MagicCombatResult r){weaponName=w;spellName=s;result=r;}
 public String getWeaponName(){return weaponName;} public String getSpellName(){return spellName;} public MagicCombatResult getResult(){return result;}
}