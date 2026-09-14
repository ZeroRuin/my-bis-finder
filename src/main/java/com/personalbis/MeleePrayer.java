package com.personalbis;

public enum MeleePrayer
{
    NONE("None",1.00,1.00), LOW("5% pair",1.05,1.05), MID("10% pair",1.10,1.10),
    HIGH("15% pair",1.15,1.15), CHIVALRY("Chivalry",1.15,1.18), PIETY("Piety",1.20,1.23);
    private final String name; private final double attack,strength;
    MeleePrayer(String n,double a,double s){name=n;attack=a;strength=s;}
    public double getAttack(){return attack;} public double getStrength(){return strength;}
    @Override public String toString(){return name;}
}
