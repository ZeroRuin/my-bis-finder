package com.personalbis;

public final class EquipmentAuditPolicy
{
    private EquipmentAuditPolicy(){}

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
        if(name==null)return false; String n=name.toLowerCase(java.util.Locale.ROOT);
        return n.contains("twisted bow")||n.contains("bow of faerdhinen")||n.contains("crystal bow")
            ||n.contains("blowpipe")||n.contains("dragon hunter crossbow")||n.contains("webweaver")
            ||n.contains("craw's bow")||n.contains("venator bow")||n.contains("dark bow")
            ||n.contains("tonalztics of ralos")||n.contains("karil's crossbow");
    }
}
