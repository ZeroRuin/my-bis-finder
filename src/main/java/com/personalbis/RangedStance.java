package com.personalbis;
public enum RangedStance {
 ACCURATE("Accurate",3,0), RAPID("Rapid",0,-1), LONGRANGE("Longrange",0,0);
 private final String name; private final int rangedBoost,speedDelta;
 RangedStance(String n,int r,int s){name=n;rangedBoost=r;speedDelta=s;}
 public int getRangedBoost(){return rangedBoost;} public int getSpeedDelta(){return speedDelta;}
 @Override public String toString(){return name;}
}