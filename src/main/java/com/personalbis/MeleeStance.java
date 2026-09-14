package com.personalbis;

public enum MeleeStance
{
    ACCURATE("Accurate", 3, 0),
    AGGRESSIVE("Aggressive", 0, 3),
    CONTROLLED("Controlled", 1, 1);

    private final String displayName;
    private final int attackBoost;
    private final int strengthBoost;

    MeleeStance(String displayName, int attackBoost, int strengthBoost)
    {
        this.displayName = displayName;
        this.attackBoost = attackBoost;
        this.strengthBoost = strengthBoost;
    }

    public int getAttackBoost() { return attackBoost; }
    public int getStrengthBoost() { return strengthBoost; }
    @Override public String toString() { return displayName; }
}
