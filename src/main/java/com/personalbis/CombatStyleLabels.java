package com.personalbis;

import java.util.*;

/** Offline ordinary attack options verified against Wiki utils.ts at
 * 89c3e25b344aea90d0189746e4b5f73dde0f0383. Labels and offensive stance
 * eligibility share this table. Defensive-only melee options are not modelled.
 */
final class CombatStyleLabels
{
    private static final Map<String,List<String[]>> OPTIONS=new HashMap<>();
    static {
        add("2h Sword","Chop|slash|Accurate","Slash|slash|Aggressive","Smash|crush|Aggressive");
        add("Banner","Lunge|stab|Accurate","Swipe|slash|Aggressive","Pound|crush|Controlled");
        add("Bladed Staff","Jab|stab|Accurate","Swipe|slash|Aggressive");
        add("Blaster");
        add("Bow","Accurate|ranged|Accurate","Rapid|ranged|Rapid","Longrange|ranged|Longrange");
        add("Crossbow","Accurate|ranged|Accurate","Rapid|ranged|Rapid","Longrange|ranged|Longrange");
        add("Thrown","Accurate|ranged|Accurate","Rapid|ranged|Rapid","Longrange|ranged|Longrange");
        add("Gun","Kick|crush|Aggressive");
        add("Bulwark","Pummel|crush|Accurate");
        add("Multi-Melee","Poke|stab|Accurate","Slash|slash|Aggressive","Pound|crush|Aggressive");
        add("Partisan","Stab|stab|Accurate","Lunge|stab|Aggressive","Pound|crush|Aggressive");
        add("Pickaxe","Spike|stab|Accurate","Impale|stab|Aggressive","Smash|crush|Aggressive");
        add("Polearm","Jab|stab|Controlled","Swipe|slash|Aggressive");
        add("Powered Staff");
        add("Powered Wand");
        add("Salamander","Scorch|slash|Aggressive","Flare|ranged|Rapid");
        add("Chinchompas","Short fuse|ranged|Accurate","Medium fuse|ranged|Rapid","Long fuse|ranged|Longrange");
        add("Claw","Chop|slash|Accurate","Slash|slash|Aggressive","Lunge|stab|Controlled");
        add("Extending Chain Mace","Thrust|crush|Accurate","Thrust|crush|Aggressive","Smash|crush|Smash");
        add("Bludgeon","Pound|crush|Aggressive","Pummel|crush|Aggressive","Smash|crush|Aggressive");
        add("Blunt","Pound|crush|Accurate","Pummel|crush|Aggressive");
        add("Polestaff","Bash|crush|Accurate","Pound|crush|Aggressive");
        add("Spiked","Pound|crush|Accurate","Pummel|crush|Aggressive","Spike|stab|Controlled");
        add("Staff","Bash|crush|Accurate","Pound|crush|Aggressive");
        add("Axe","Chop|slash|Accurate","Hack|slash|Aggressive","Smash|crush|Aggressive");
        add("Unarmed","Punch|crush|Accurate","Kick|crush|Aggressive");
        add("Scythe","Reap|slash|Accurate","Chop|slash|Aggressive","Jab|crush|Aggressive");
        add("Slash Sword","Chop|slash|Accurate","Slash|slash|Aggressive","Lunge|stab|Controlled");
        add("Spear","Lunge|stab|Controlled","Swipe|slash|Controlled","Pound|crush|Controlled");
        add("Stab Sword","Stab|stab|Accurate","Lunge|stab|Aggressive","Slash|slash|Aggressive");
        add("Whip","Flick|slash|Accurate","Lash|slash|Controlled");
        add("Flail","Chop|slash|Accurate","Slash|slash|Aggressive");
        add("Dagger");
    }
    private CombatStyleLabels() {}
    private static String key(String category){return category==null?"":category.trim().toLowerCase(Locale.ROOT);}
    private static void add(String category,String... options){
        List<String[]> rows=new ArrayList<>();
        for(String option:options)rows.add(option.split("\\|"));
        OPTIONS.put(key(category),Collections.unmodifiableList(rows));
    }
    private static List<String[]> options(String category){return OPTIONS.getOrDefault(key(category),Collections.emptyList());}

    static EnumSet<MeleeStance> meleeStances(String category,AttackStyle style){
        EnumSet<MeleeStance> result=EnumSet.noneOf(MeleeStance.class);
        String type=type(style).toLowerCase(Locale.ROOT);
        for(String[] row:options(category))if(row[1].equals(type)){
            if(row[2].equals("Smash"))result.add(MeleeStance.SMASH);
            else if(row[2].equals("Accurate"))result.add(MeleeStance.ACCURATE);
            else if(row[2].equals("Aggressive"))result.add(MeleeStance.AGGRESSIVE);
            else if(row[2].equals("Controlled"))result.add(MeleeStance.CONTROLLED);
        }
        return result;
    }
    static EnumSet<RangedStance> rangedStances(String category){
        EnumSet<RangedStance> result=EnumSet.noneOf(RangedStance.class);
        for(String[] row:options(category))if(row[1].equals("ranged")){
            if(row[2].equals("Accurate"))result.add(RangedStance.ACCURATE);
            else if(row[2].equals("Rapid"))result.add(RangedStance.RAPID);
            else if(row[2].equals("Longrange"))result.add(RangedStance.LONGRANGE);
        }
        return result;
    }
    static String melee(EquipmentCandidate weapon,AttackStyle style,MeleeStance stance){
        return melee(weapon==null?"Unarmed":weapon.getWeaponCategory(),style,stance);
    }
    static String melee(String category,AttackStyle style,MeleeStance stance){
        if(stance==null)return "—";
        for(String[] row:options(category))
            if(row[1].equals(type(style).toLowerCase(Locale.ROOT))&&row[2].equals(stance.toString()))return row[0];
        return type(style)+" / "+stance; // Explicit diagnostics, never a fabricated in-game name.
    }
    static String ranged(String category,RangedStance stance){
        if(stance==null)return "—";
        for(String[] row:options(category))
            if(row[1].equals("ranged")&&row[2].equals(stance.toString()))return row[0];
        return "Ranged / "+stance;
    }
    static String magic(String spellOrStaff){
        return PoweredStaff.forWeapon(spellOrStaff)!=null?"Accurate":"Spell";
    }
    static String magicStance(String spellOrStaff){
        return PoweredStaff.forWeapon(spellOrStaff)!=null?"Accurate":"Spell casting";
    }
    static String type(AttackStyle style){
        return style==AttackStyle.MELEE_STAB?"Stab":style==AttackStyle.MELEE_SLASH?"Slash":style==AttackStyle.MELEE_CRUSH?"Crush":style==AttackStyle.RANGED?"Ranged":"Magic";
    }
    static String description(EquipmentCandidate weapon,AttackStyle style,MeleeStance stance){
        return melee(weapon,style,stance)+" ("+type(style)+", "+(stance==MeleeStance.SMASH?"Aggressive":stance)+")";
    }
}
