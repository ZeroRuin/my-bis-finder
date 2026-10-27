package com.personalbis;
import java.util.*;
public final class VoidLoadoutFamiliesSelfTest {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static EquipmentCandidate item(int id,String name,EquipmentSlot slot,boolean blocked){return new EquipmentCandidate(new BankItem(id,1,name),slot,0,0,0,0,0,0,0,0,0,false,"",blocked?RequirementResult.blocked("fixture"):RequirementResult.usable("fixture"));}
 static void add(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate c){ranked.computeIfAbsent(c.getSlot(),s->new ArrayList<>()).add(c);}
 public static void main(String[] args){
  for(String helm:new String[]{"Void ranger helm","Void melee helm"}){
   Map<EquipmentSlot,List<EquipmentCandidate>> r=new EnumMap<>(EquipmentSlot.class);
   add(r,item(1,helm+" (or)",EquipmentSlot.HEAD,false));add(r,item(2,"Void knight gloves",EquipmentSlot.HANDS,false));
   add(r,item(3,"Void knight top",EquipmentSlot.BODY,false));add(r,item(4,"Void knight robe",EquipmentSlot.LEGS,false));
   add(r,item(5,"Elite void top (or)",EquipmentSlot.BODY,false));add(r,item(6,"Elite void robe (or)",EquipmentSlot.LEGS,false));
   List<Map<EquipmentSlot,EquipmentCandidate>> families=VoidLoadoutFamilies.families(r,null,helm);
   check(families.size()==4&&families.get(0).size()==4&&families.get(1).size()==4,"complete normal, mixed and elite families "+helm);
   check(VoidLoadoutFamilies.families(r,item(99,"Required helm",EquipmentSlot.HEAD,false),helm).isEmpty(),"target requirements override Void");
   check(VoidLoadoutFamilies.families(r,item(100,"Required shield",EquipmentSlot.SHIELD,false),helm).size()==4,"unrelated required slot retained");
   r.remove(EquipmentSlot.HANDS);check(VoidLoadoutFamilies.families(r,null,helm).isEmpty(),"missing gloves");
   add(r,item(2,"Void knight gloves",EquipmentSlot.HANDS,true));check(VoidLoadoutFamilies.families(r,null,helm).isEmpty(),"blocked gloves");
   try{families.get(0).clear();throw new AssertionError("mutable family");}catch(UnsupportedOperationException expected){}
  }
  check(!RangedAmmoRules.compatible(item(21012,"Dragon hunter crossbow",EquipmentSlot.WEAPON,false),item(10158,"Kebbit bolts",EquipmentSlot.AMMO,false)),"DHCB is not a hunters crossbow");
  check(RangedAmmoRules.compatible(item(21012,"Dragon hunter crossbow",EquipmentSlot.WEAPON,false),item(21905,"Dragon bolts",EquipmentSlot.AMMO,false)),"DHCB dragon-bolt fallback");
  System.out.println("PASS complete melee/ranged normal/elite Void families, ornaments, target/ownership restrictions and DHCB ammo fallback");
 }
}
