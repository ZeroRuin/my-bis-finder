package com.personalbis;

public final class SlayerMeleeEffects
{
    private SlayerMeleeEffects(){}

    public static int attackRoll(int ordinaryRoll, boolean onTask, boolean slayerHead, boolean salveActive)
    {
        return onTask && slayerHead && !salveActive
            ? (int)Math.floor(ordinaryRoll * 7.0 / 6.0)
            : ordinaryRoll;
    }

    public static int maxHit(int ordinaryMaxHit, boolean onTask, boolean slayerHead, boolean salveActive)
    {
        return onTask && slayerHead && !salveActive
            ? (int)Math.floor(ordinaryMaxHit * 7.0 / 6.0)
            : ordinaryMaxHit;
    }
}
