package com.personalbis;
import java.util.*;
public final class OptimizedRangedLoadout {
 private final Map<EquipmentSlot,EquipmentCandidate> items; private final RangedCombatResult result; private final EquipmentCandidate loadedAmmo;
 public OptimizedRangedLoadout(Map<EquipmentSlot,EquipmentCandidate>i,RangedCombatResult r){this(i,r,null);}
 public OptimizedRangedLoadout(Map<EquipmentSlot,EquipmentCandidate>i,RangedCombatResult r,EquipmentCandidate a){items=Collections.unmodifiableMap(new EnumMap<>(i));result=r;loadedAmmo=a;}
 public Map<EquipmentSlot,EquipmentCandidate> getItems(){return items;} public RangedCombatResult getResult(){return result;} public EquipmentCandidate getLoadedAmmo(){return loadedAmmo;}
 public List<Integer> itemIds(){List<Integer>x=new ArrayList<>();for(EquipmentCandidate c:items.values())if(c!=null)x.add(c.getItem().getItemId());if(loadedAmmo!=null&&!x.contains(loadedAmmo.getItem().getItemId()))x.add(loadedAmmo.getItem().getItemId());return x;}
}
