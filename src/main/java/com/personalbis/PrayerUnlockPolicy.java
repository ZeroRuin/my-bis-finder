package com.personalbis;

public final class PrayerUnlockPolicy
{
    private PrayerUnlockPolicy(){}

    public static boolean chivalry(int prayerLevel,int knightWavesState)
    {
        return prayerLevel>=60 && knightWavesState>=8;
    }

    public static boolean piety(int prayerLevel,int knightWavesState)
    {
        return prayerLevel>=70 && knightWavesState>=8;
    }

    public static boolean rigour(int prayerLevel,boolean unlocked)
    {
        return prayerLevel>=74 && unlocked;
    }

    public static boolean augury(int prayerLevel,boolean unlocked)
    {
        return prayerLevel>=77 && unlocked;
    }
}
