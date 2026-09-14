package com.personalbis;
import java.util.*;
public final class OptimizedMagicLoadout {
 private final Map<EquipmentSlot,EquipmentCandidate> items; private final MagicCombatResult result; private final MagicSpell spell;
 public OptimizedMagicLoadout(Map<EquipmentSlot,EquipmentCandidate> i,MagicCombatResult r,MagicSpell s){items=new EnumMap<>(i);result=r;spell=s;}
 public Map<EquipmentSlot,EquipmentCandidate> getItems(){return Collections.unmodifiableMap(items);} public MagicCombatResult getResult(){return result;} public MagicSpell getSpell(){return spell;}
 public java.util.List<Integer> itemIds(){java.util.List<Integer>x=new ArrayList<>();for(EquipmentCandidate c:items.values())if(c!=null)x.add(c.getItem().getItemId());return x;}
}