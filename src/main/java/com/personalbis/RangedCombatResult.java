package com.personalbis;
public final class RangedCombatResult {
 private final double dps,accuracy; private final int maxHit,attackRoll,defenceRoll,rangedAttack,rangedStrength,speed,effectiveRanged,effectiveDamage;
 private final RangedStance stance; private final RangedPrayer prayer;
 public RangedCombatResult(double d,double a,int m,int ar,int dr,int ra,int rs,int sp,int er,int ed,RangedStance st,RangedPrayer p)
 {dps=d;accuracy=a;maxHit=m;attackRoll=ar;defenceRoll=dr;rangedAttack=ra;rangedStrength=rs;speed=sp;effectiveRanged=er;effectiveDamage=ed;stance=st;prayer=p;}
 public double getDps(){return dps;} public double getAccuracy(){return accuracy;} public int getMaxHit(){return maxHit;}
 public int getAttackRoll(){return attackRoll;} public int getDefenceRoll(){return defenceRoll;} public int getRangedAttack(){return rangedAttack;}
 public int getRangedStrength(){return rangedStrength;} public int getAttackSpeed(){return speed;} public int getEffectiveRanged(){return effectiveRanged;} public int getEffectiveDamage(){return effectiveDamage;}
 public RangedStance getStance(){return stance;} public RangedPrayer getPrayer(){return prayer;}
}
