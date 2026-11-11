package com.personalbis;

import java.util.*;

/** State-dependent set effects; HP is the account snapshot captured for generation. */
final class EquipmentSetRules
{
    private EquipmentSetRules() {}
    static boolean has(Map<EquipmentSlot,EquipmentCandidate> gear,String name) {
        for(EquipmentCandidate c:gear.values())if(ItemNames.is(c,name))return true;
        return false;
    }
    static boolean dharok(Map<EquipmentSlot,EquipmentCandidate> gear) {
        return has(gear,"Dharok's greataxe")&&has(gear,"Dharok's helm")
            &&has(gear,"Dharok's platebody")&&has(gear,"Dharok's platelegs");
    }
    static int dharokFactor(int maximumHp,int currentHp) {
        int max=Math.max(1,maximumHp),current=Math.max(1,currentHp);
        return 10000+Math.max(0,max-current)*max;
    }
    static int strengthRangedMax(int max,Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task) {
        if(m!=null&&m.hasAttribute("undead")){
            EquipmentCandidate neck=gear.get(EquipmentSlot.NECK);
            String n=neck==null?"":ItemNames.lower(neck).replace(" ","");
            if(n.equals("salveamulet(e)")||n.equals("salveamulet(ei)"))return max*6/5;
            if(n.equals("salveamulet")||n.equals("salveamulet(i)"))return max*7/6;
        }
        EquipmentCandidate head=gear.get(EquipmentSlot.HEAD);
        String n=head==null?"":ItemNames.lower(head);
        return task&&(n.contains("black mask")||n.contains("slayer helm"))?max*7/6:max;
    }

    static boolean strengthRanged(EquipmentCandidate weapon) {
        if(weapon==null)return false;
        String n=weapon.getItem().getName();
        return n.equalsIgnoreCase("Eclipse atlatl")||n.equalsIgnoreCase("Hunter's spear");
    }
}
