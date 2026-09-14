package com.personalbis;

public final class WeaponComparison
{
    private final String weaponName;
    private final OptimizedLoadout loadout;

    public WeaponComparison(String weaponName, OptimizedLoadout loadout)
    {
        this.weaponName = weaponName;
        this.loadout = loadout;
    }

    public String getWeaponName(){return weaponName;}
    public OptimizedLoadout getLoadout(){return loadout;}
    public CombatResult getResult(){return loadout.getResult();}
}
