package com.personalbis;
public enum RangedPrayer {
 NONE("None",1.00,1.00), SHARP_EYE("Sharp Eye",1.05,1.05), HAWK_EYE("Hawk Eye",1.10,1.10),
 EAGLE_EYE("Eagle Eye",1.15,1.15), DEADEYE("Deadeye",1.18,1.18), RIGOUR("Rigour",1.20,1.23);
 private final String name; private final double accuracy,rangedStrength;
 RangedPrayer(String n,double a,double d){name=n;accuracy=a;rangedStrength=d;}
 /** Accuracy/effective attack multiplier. */
 public double getRanged(){return accuracy;}
 public double getAccuracy(){return accuracy;}
 /** Effective ranged-strength multiplier used by max-hit calculation. */
 public double getRangedStrength(){return rangedStrength;}
 @Override public String toString(){return name;}
}
