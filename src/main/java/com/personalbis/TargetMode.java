package com.personalbis;

public enum TargetMode
{
    MANUAL("Manual target"),
    SLAYER_TASK("Current Slayer task");

    private final String displayName;

    TargetMode(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
