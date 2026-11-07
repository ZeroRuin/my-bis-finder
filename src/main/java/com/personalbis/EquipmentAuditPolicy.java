package com.personalbis;

public final class EquipmentAuditPolicy
{
    private EquipmentAuditPolicy(){}

    public static boolean shieldAllowed(EquipmentCandidate weapon)
    {
        return weapon == null || !weapon.isTwoHanded();
    }

    public static boolean slayerAppliesToRangedOrMagic(String name)
    {
        if (name == null) return false;
        String n=name.toLowerCase();
        boolean slayer=n.contains("slayer helmet")||n.contains("slayer helm")||n.contains("black mask");
        return slayer && (n.contains("(i)")||n.contains("imbued"));
    }

    public static boolean monsterSpecificMeleeWeapon(String name, MonsterDefinition m)
    {
        if(name==null||m==null)return false;
        String n=name.toLowerCase();
        if(m.hasAttribute("demon")&&(n.contains("arclight")||n.contains("emberlight")||n.contains("silverlight")||n.contains("darklight")))return true;
        if((m.hasAttribute("dragon")||m.hasAttribute("draconic"))&&n.contains("dragon hunter lance"))return true;
        if((m.hasAttribute("kalphite")||m.hasAttribute("scabarite"))&&n.contains("keris"))return true;
        return n.contains("osmumten")&&n.contains("fang") || n.contains("scythe of vitur");
    }


    public static int magicWeaponEvaluationLimit(){ return 12; }

    /**
     * Full-set Magic synergies must enter the optimizer before ordinary slot pruning,
     * otherwise individually weak pieces can never be evaluated as a complete set.
     */
    public static boolean seedCompleteMagicSetsBeforePruning(){ return true; }
    public static int meleeWeaponEvaluationLimit(){ return 12; }
    public static int rangedWeaponEvaluationLimit(){ return 12; }

    // Alpha 50.4: retained legacy limits document the old heuristic, but weapon
    // optimizers deliberately evaluate all owned/equippable candidates.
    public static boolean evaluateAllOwnedMeleeWeapons(){ return true; }
    public static boolean evaluateAllOwnedRangedWeapons(){ return true; }

    public static boolean specialRangedWeapon(String name)
    {
        if(name==null)return false; String n=name.toLowerCase();
        return n.contains("twisted bow")||n.contains("bow of faerdhinen")||n.contains("crystal bow")
            ||n.contains("blowpipe")||n.contains("dragon hunter crossbow")||n.contains("webweaver")
            ||n.contains("craw's bow")||n.contains("venator bow")||n.contains("dark bow")
            ||n.contains("tonalztics of ralos")||n.contains("karil's crossbow");
    }
}
