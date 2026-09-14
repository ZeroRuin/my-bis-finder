package com.personalbis;
public final class MagicCombatResult {
 private final double dps,accuracy,magicDamageBonus; private final int maxHit,attackRoll,defenceRoll,magicAttack,effectiveMagic,attackSpeed,preTransformMax;
 private final String prayer,spell,mechanicNote;
 public MagicCombatResult(double d,double a,int m,int ar,int dr,int ma,double mb,String p,String s){this(d,a,m,ar,dr,ma,mb,p,s,"",0,0,m);}
 public MagicCombatResult(double d,double a,int m,int ar,int dr,int ma,double mb,String p,String s,String note){this(d,a,m,ar,dr,ma,mb,p,s,note,0,0,m);}
 public MagicCombatResult(double d,double a,int m,int ar,int dr,int ma,double mb,String p,String s,String note,int eff,int ticks,int preMax){dps=d;accuracy=a;magicDamageBonus=mb;maxHit=m;attackRoll=ar;defenceRoll=dr;magicAttack=ma;prayer=p;spell=s;mechanicNote=note==null?"":note;effectiveMagic=eff;attackSpeed=ticks;preTransformMax=preMax;}
 public double getDps(){return dps;} public double getMagicDamageBonus(){return magicDamageBonus;} public double getAccuracy(){return accuracy;} public int getMaxHit(){return maxHit;} public int getAttackRoll(){return attackRoll;} public int getDefenceRoll(){return defenceRoll;} public int getMagicAttack(){return magicAttack;} public String getPrayer(){return prayer;} public String getSpell(){return spell;} public String getMechanicNote(){return mechanicNote;}
 public int getEffectiveMagic(){return effectiveMagic;} public int getAttackSpeed(){return attackSpeed;} public int getPreTransformMax(){return preTransformMax;}
}
