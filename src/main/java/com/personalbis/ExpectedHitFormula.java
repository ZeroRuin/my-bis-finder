package com.personalbis;

/** Shared expected-damage formula for a successful OSRS hit. */
public final class ExpectedHitFormula
{
    private ExpectedHitFormula(){}

    public static double successfulHitAverage(int maxHit)
    {
        if(maxHit<=0)return 0.0;
        return maxHit/2.0 + 1.0/(maxHit+1.0);
    }
}
