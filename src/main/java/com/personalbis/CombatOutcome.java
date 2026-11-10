package com.personalbis;

/** What a loadout does to the target, whichever combat style produced it. */
public interface CombatOutcome
{
    double getDps();

    double getExpectedTtkSeconds();

    double getAccuracy();

    int getMaxHit();
}
