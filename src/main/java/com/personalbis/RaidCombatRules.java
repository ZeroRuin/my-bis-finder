package com.personalbis;

/** OSRS Wiki PlayerVsNPCCalc: Warden bounds, core max hits and Guardian pickaxe scaling. */
final class RaidCombatRules
{
    private RaidCombatRules() {}
    static boolean wardenP2(MonsterDefinition m) {
        return m!=null&&m.getName().endsWith("' Warden")&&m.getVersion().startsWith("Phase 2 (")
            &&m.getVersion().contains("vulnerable");
    }
    static boolean core(MonsterDefinition m) {
        return m!=null&&m.getName().endsWith("' Warden")&&m.getVersion().contains("Core-ejected");
    }
    static int[] wardenBounds(int max,int attackRoll,int defenceRoll) {
        long delta=Math.max(0L,(long)attackRoll-defenceRoll/3);
        int percent=(int)Math.min(40,15+25*delta/42000);
        return new int[]{max*percent/100,max*(percent+20)/100};
    }
    static int pickaxeBonus(String name) {
        String n=name.toLowerCase(java.util.Locale.ROOT);
        if(n.startsWith("bronze")||n.startsWith("iron"))return 1;
        if(n.startsWith("steel"))return 6;
        if(n.startsWith("black"))return 11;
        if(n.startsWith("mithril"))return 21;
        if(n.startsWith("adamant"))return 31;
        if(n.startsWith("rune")||n.startsWith("gilded"))return 41;
        return 61; // dragon, infernal, crystal and their cosmetic variants
    }
    static HitDistribution.AttackDistribution guardian(HitDistribution.AttackDistribution d,
        MonsterDefinition m,EquipmentCandidate weapon,int mining) {
        if(!WikiTargetEligibility.isGuardian(m)||weapon==null)return d;
        return d.transform(HitDistribution.multiply(50+Math.max(1,Math.min(99,mining))
            +pickaxeBonus(weapon.getItem().getName()),150,0),false).flatten();
    }
}
