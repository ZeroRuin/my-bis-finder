package com.personalbis;
import java.util.*;

/** Complete Void families are protected until the remaining slots are searched. */
final class VoidLoadoutFamilies {
 private VoidLoadoutFamilies(){}
 static List<Map<EquipmentSlot,EquipmentCandidate>> families(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate required,String helmet){
  List<Map<EquipmentSlot,EquipmentCandidate>> result=new ArrayList<>();
  for(boolean eliteBody:new boolean[]{false,true})for(boolean eliteLegs:new boolean[]{false,true}){
   Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);
   EquipmentSlot[] slots={EquipmentSlot.HEAD,EquipmentSlot.BODY,EquipmentSlot.LEGS,EquipmentSlot.HANDS};
   String[] names={helmet,eliteBody?"Elite void top":"Void knight top",eliteLegs?"Elite void robe":"Void knight robe","Void knight gloves"};
   boolean complete=true;
   for(int i=0;i<slots.length;i++){
    EquipmentCandidate chosen=null;
    for(EquipmentCandidate candidate:ranked.getOrDefault(slots[i],Collections.emptyList()))
     if(candidate!=null&&candidate.getItem()!=null&&!candidate.getRequirementResult().isBlocked()
       &&candidate.getItem().getQuantity()>0&&VoidEquipment.matches(candidate.getItem().getName(),names[i])){
      if(required!=null&&required.getSlot()==slots[i]&&required.getItem().getItemId()!=candidate.getItem().getItemId())continue;
      chosen=candidate;break;
     }
    if(chosen==null){complete=false;break;}gear.put(slots[i],chosen);
   }
   if(complete)result.add(Collections.unmodifiableMap(gear));
  }
  return result;
 }
}
