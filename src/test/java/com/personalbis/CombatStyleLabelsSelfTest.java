package com.personalbis;
public final class CombatStyleLabelsSelfTest {
    private static void label(String category,AttackStyle type,MeleeStance stance,String expected){
        String actual=CombatStyleLabels.melee(category,type,stance);
        if(!expected.equals(actual))throw new AssertionError(category+": "+actual+" != "+expected);
    }
    public static void main(String[] args)throws Exception{
        java.util.Map<String,java.util.Set<String>> expected=new java.util.LinkedHashMap<>();
        try(java.io.BufferedReader reader=new java.io.BufferedReader(new java.io.InputStreamReader(
            CombatStyleLabelsSelfTest.class.getResourceAsStream("/com/personalbis/combat-style-options.tsv"),java.nio.charset.StandardCharsets.UTF_8))){
            String line;
            while((line=reader.readLine())!=null){
                if(line.startsWith("#")||line.isEmpty())continue;
                String[] row=line.split("\\t");String key=row[0]+"|"+row[1]+"|"+row[2];
                expected.computeIfAbsent(key,k->new java.util.HashSet<>()).add(row[3]);
            }
        }
        for(java.util.Map.Entry<String,java.util.Set<String>> entry:expected.entrySet()){
            String[] row=entry.getKey().split("\\|");String actual;
            if(row[1].equals("ranged")){
                RangedStance stance=RangedStance.valueOf(row[2].toUpperCase(java.util.Locale.ROOT));
                if(!CombatStyleLabels.rangedStances(row[0]).contains(stance))throw new AssertionError(entry.getKey());
                actual=CombatStyleLabels.ranged(row[0],stance);
            } else {
                AttackStyle type=row[1].equals("stab")?AttackStyle.MELEE_STAB:row[1].equals("slash")?AttackStyle.MELEE_SLASH:AttackStyle.MELEE_CRUSH;
                MeleeStance stance=MeleeStance.valueOf(row[2].toUpperCase(java.util.Locale.ROOT));
                if(!CombatStyleLabels.meleeStances(row[0],type).contains(stance))throw new AssertionError(entry.getKey());
                actual=CombatStyleLabels.melee(row[0],type,stance);
            }
            if(!entry.getValue().contains(actual))throw new AssertionError(entry.getKey()+": "+actual);
        }
        if(!CombatStyleLabels.meleeStances("Polearm",AttackStyle.MELEE_CRUSH).isEmpty())throw new AssertionError("fictional halberd Crush");
        if(!CombatStyleLabels.meleeStances("Flail",AttackStyle.MELEE_CRUSH).isEmpty())throw new AssertionError("fictional flail Crush");
        if(CombatStyleLabels.meleeStances("Bulwark",AttackStyle.MELEE_CRUSH).contains(MeleeStance.AGGRESSIVE))throw new AssertionError("fictional Bulwark Aggressive");
        if(CombatStyleLabels.meleeStances("Bludgeon",AttackStyle.MELEE_CRUSH).contains(MeleeStance.ACCURATE))throw new AssertionError("fictional Bludgeon Accurate");
        if(!CombatStyleLabels.rangedStances("Stab Sword").isEmpty())throw new AssertionError("fictional ranged Fang");
        if(CombatStyleLabels.rangedStances("Salamander").size()!=1)throw new AssertionError("salamander only Flare/Rapid");

        label("Stab Sword",AttackStyle.MELEE_STAB,MeleeStance.AGGRESSIVE,"Lunge");
        label("Stab Sword",AttackStyle.MELEE_STAB,MeleeStance.ACCURATE,"Stab");
        label("Stab Sword",AttackStyle.MELEE_SLASH,MeleeStance.AGGRESSIVE,"Slash");
        label("Partisan",AttackStyle.MELEE_CRUSH,MeleeStance.AGGRESSIVE,"Pound");
        label("Slash Sword",AttackStyle.MELEE_STAB,MeleeStance.CONTROLLED,"Lunge");
        label("Axe",AttackStyle.MELEE_SLASH,MeleeStance.AGGRESSIVE,"Hack");
        label("2h Sword",AttackStyle.MELEE_SLASH,MeleeStance.AGGRESSIVE,"Slash");
        label("Scythe",AttackStyle.MELEE_SLASH,MeleeStance.AGGRESSIVE,"Chop");
        label("Scythe",AttackStyle.MELEE_CRUSH,MeleeStance.AGGRESSIVE,"Jab");
        label("Blunt",AttackStyle.MELEE_CRUSH,MeleeStance.AGGRESSIVE,"Pummel");
        label("Spear",AttackStyle.MELEE_SLASH,MeleeStance.CONTROLLED,"Swipe");
        label("Whip",AttackStyle.MELEE_SLASH,MeleeStance.CONTROLLED,"Lash");
        label("Pickaxe",AttackStyle.MELEE_STAB,MeleeStance.ACCURATE,"Spike");
        label("Unknown",AttackStyle.MELEE_STAB,MeleeStance.AGGRESSIVE,"Stab / Aggressive");
        if(!"Medium fuse".equals(CombatStyleLabels.ranged("Chinchompas",RangedStance.RAPID)))throw new AssertionError("chinchompa label");
        if(!"Rapid".equals(CombatStyleLabels.ranged("Bow",RangedStance.RAPID)))throw new AssertionError("bow label");
        if(!"Flare".equals(CombatStyleLabels.ranged("Salamander",RangedStance.RAPID)))throw new AssertionError("salamander label");
        System.out.println("PASS full primary-source offensive category fixture, rejected fictional options, Fang Lunge and fallback");
    }
}
