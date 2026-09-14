package com.personalbis;

public final class CombatResult
{
    private final double dps;
    private final double accuracy;
    private final int maxHit;
    private final int attackRoll;
    private final int defenceRoll;
    private final int attackBonus;
    private final int strengthBonus;
    private final int attackSpeed;
    private final int effectiveAttack;
    private final int effectiveStrength;
    private final int preWeaponAttackRoll;
    private final int preWeaponMaxHit;
    private final MeleeStance stance;
    private final MeleePrayer prayer;

    public CombatResult(double dps, double accuracy, int maxHit, int attackRoll, int defenceRoll,
        int attackBonus, int strengthBonus, int attackSpeed, int effectiveAttack, int effectiveStrength,
        MeleeStance stance, MeleePrayer prayer, int preWeaponAttackRoll, int preWeaponMaxHit)
    {
        this.dps=dps; this.accuracy=accuracy; this.maxHit=maxHit; this.attackRoll=attackRoll;
        this.defenceRoll=defenceRoll; this.attackBonus=attackBonus; this.strengthBonus=strengthBonus;
        this.attackSpeed=attackSpeed; this.effectiveAttack=effectiveAttack; this.effectiveStrength=effectiveStrength;
        this.stance=stance; this.prayer=prayer;
        this.preWeaponAttackRoll=preWeaponAttackRoll; this.preWeaponMaxHit=preWeaponMaxHit;
    }

    public double getDps(){return dps;}
    public double getAccuracy(){return accuracy;}
    public int getMaxHit(){return maxHit;}
    public int getAttackRoll(){return attackRoll;}
    public int getDefenceRoll(){return defenceRoll;}
    public int getAttackBonus(){return attackBonus;}
    public int getStrengthBonus(){return strengthBonus;}
    public int getAttackSpeed(){return attackSpeed;}
    public int getEffectiveAttack(){return effectiveAttack;}
    public int getEffectiveStrength(){return effectiveStrength;}
    public MeleeStance getStance(){return stance;}
    public MeleePrayer getPrayer(){return prayer;}
    public int getPreWeaponAttackRoll(){return preWeaponAttackRoll;}
    public int getPreWeaponMaxHit(){return preWeaponMaxHit;}
}
