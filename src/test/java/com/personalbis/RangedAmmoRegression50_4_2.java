package com.personalbis;
final class RangedAmmoRegression50_4_2 {
 private static EquipmentCandidate item(String n,EquipmentSlot slot){
  return new EquipmentCandidate(new BankItem(1,1,n),slot,0,0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));
 }
 private static EquipmentCandidate weapon(String n){return item(n,EquipmentSlot.WEAPON);}
 private static EquipmentCandidate ammo(String n){return item(n,EquipmentSlot.AMMO);}
 static boolean huntersKebbitOnly(){
  EquipmentCandidate w=weapon("Hunters' crossbow");
  return RangedAmmoRules.compatible(w,ammo("Kebbit bolts"))
    &&RangedAmmoRules.compatible(w,ammo("Long kebbit bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Rune bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Moonlight antler bolts"));
 }
 static boolean sunlightAntlerOnly(){
  EquipmentCandidate w=weapon("Hunters' sunlight crossbow");
  return RangedAmmoRules.compatible(w,ammo("Sunlight antler bolts"))
    &&RangedAmmoRules.compatible(w,ammo("Moonlight antler bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Kebbit bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Rune bolts"));
 }
 static boolean runeRejectsSpecial(){
  EquipmentCandidate w=weapon("Rune crossbow");
  return RangedAmmoRules.compatible(w,ammo("Rune bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Kebbit bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Moonlight antler bolts"))
    &&!RangedAmmoRules.compatible(w,ammo("Bolt rack"));
 }
 private RangedAmmoRegression50_4_2(){}
}