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

    public static boolean specialMagicWeapon(String name, MonsterDefinition m)
    {
        if(name==null)return false; String n=name.toLowerCase();
        if(PoweredStaff.forWeapon(n)!=null||PoweredStaff.requiresContext(n))return true;
        if(n.contains("iban")||n.contains("slayer's staff")||n.contains("slayer staff")
            ||n.contains("ancient staff")||n.contains("ancient sceptre")||n.contains("kodai")
            ||n.contains("nightmare staff")||n.contains("master wand")
            ||n.contains("saradomin staff")||n.contains("guthix staff")||n.contains("zamorak staff")
            ||n.contains("staff of light")||n.contains("staff of balance")||n.contains("staff of the dead")
            ||n.contains("dragon hunter wand")||n.contains("twinflame")||n.contains("smoke battlestaff")
            ||n.contains("mystic smoke staff"))return true;
        String e=m==null?"":m.getWeaknessElement();
        return !e.isEmpty()&&elementalRuneProvider(n,e);
    }
    private static boolean elementalRuneProvider(String n,String e)
    {
        if(!(n.contains("staff")||n.contains("battlestaff")))return false;
        if(e.equals("air"))return n.contains("air")||n.contains("smoke")||n.contains("dust")||n.contains("mist");
        if(e.equals("water"))return n.contains("water")||n.contains("steam")||n.contains("mud")||n.contains("mist");
        if(e.equals("earth"))return n.contains("earth")||n.contains("lava")||n.contains("mud")||n.contains("dust");
        if(e.equals("fire"))return n.contains("fire")||n.contains("lava")||n.contains("smoke")||n.contains("steam");
        return false;
    }
    public static boolean specialRangedWeapon(String name)
    {
        if(name==null)return false; String n=name.toLowerCase();
        return n.contains("twisted bow")||n.contains("bow of faerdhinen")||n.contains("crystal bow")
            ||n.contains("blowpipe")||n.contains("dragon hunter crossbow")||n.contains("webweaver")
            ||n.contains("craw's bow")||n.contains("venator bow")||n.contains("dark bow")
            ||n.contains("tonalztics of ralos")||n.contains("karil's crossbow");
    }
}
