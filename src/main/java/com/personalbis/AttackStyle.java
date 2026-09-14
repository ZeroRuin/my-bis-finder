package com.personalbis;

public enum AttackStyle
{
    MELEE_STAB("Melee - Stab"),
    MELEE_SLASH("Melee - Slash"),
    MELEE_CRUSH("Melee - Crush"),
    RANGED("Ranged"),
    MAGIC("Magic");

    private final String displayName;

    AttackStyle(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
