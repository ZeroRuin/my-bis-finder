package com.personalbis;
import java.util.*;
public final class OptimizedLoadout {
 private final AttackStyle style; private final Map<EquipmentSlot,EquipmentCandidate> items; private final CombatResult result;
 public OptimizedLoadout(AttackStyle style,Map<EquipmentSlot,EquipmentCandidate> items,CombatResult result){this.style=style;this.items=Collections.unmodifiableMap(new EnumMap<>(items));this.result=result;}
 public AttackStyle getStyle(){return style;} public Map<EquipmentSlot,EquipmentCandidate> getItems(){return items;} public CombatResult getResult(){return result;}
 public List<Integer> itemIds(){List<Integer>x=new ArrayList<>();for(EquipmentSlot s:EquipmentSlot.values()){EquipmentCandidate c=items.get(s);if(c!=null)x.add(c.getItem().getItemId());}return x;}
}
