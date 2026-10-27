package com.personalbis;

import java.util.Collections;

public final class DefenceFillSelfTest
{
    private static void check(boolean value,String note){if(!value)throw new AssertionError(note);}
    private static DefenceFillPolicy.Offence offence(double accuracy,int max,int speed,double dps,double ttk,int roll,int rawMax)
    {return new DefenceFillPolicy.Offence(accuracy,max,speed,dps,ttk,roll,rawMax);}
    public static void main(String[] args)
    {
        check(FillPreference.decode(null)==FillPreference.PRAYER,"default Prayer");
        check(FillPreference.decode("DEFENCE")==FillPreference.DEFENCE,"saved Defence");
        check(FillPreference.decode("broken")==FillPreference.PRAYER,"invalid saved preference");
        DefenceFillPolicy.Offence base=offence(.75,42,4,6.5,12,15000,43);
        check(base.sameAs(offence(.75,42,4,6.5,12,15000,43)),"exact equality");
        check(!base.sameAs(offence(Math.nextDown(.75),42,4,6.5,12,15000,43)),"unrounded accuracy loss despite equal DPS");
        check(!base.sameAs(offence(.75,41,4,6.5,12,15000,43)),"max hit loss");
        check(!base.sameAs(offence(.75,42,5,6.5,12,15000,43)),"speed loss");
        check(!base.sameAs(offence(.75,42,4,6.4,12,15000,43)),"DPS loss");
        check(!base.sameAs(offence(.75,42,4,6.5,13,15000,43)),"TTK loss");
        check(!base.sameAs(offence(.75,42,4,6.5,12,14999,43)),"raw attack roll loss under forced accuracy");
        check(!base.sameAs(offence(.75,42,4,6.5,12,15000,42)),"pre-cap damage loss");
        check(!offence(.75,42,4,0,Double.POSITIVE_INFINITY,15000,43).sameAs(base),"unusable result");
        int[] slashGear={10,70,10,-5,20},magicGear={10,20,10,70,20};
        check(DefenceFillPolicy.compare(slashGear,0,magicGear,10,DefenceFillPolicy.Profile.SLASH)>0,"target Slash outweighs Prayer");
        check(DefenceFillPolicy.compare(slashGear,0,magicGear,0,DefenceFillPolicy.Profile.VORKATH)<0,"multiple styles use equal listed weights");
        check(DefenceFillPolicy.compare(slashGear,0,magicGear,0,DefenceFillPolicy.Profile.UNKNOWN)<0,"unknown uses all five bonuses");
        MonsterDefinition unknown=new MonsterDefinition(1,"Unknown",1,1,1,1,1);
        check(DefenceFillPolicy.profile(unknown)==DefenceFillPolicy.Profile.UNKNOWN,"do not infer offence from offensive bonuses");
        MonsterDefinition vard=new MonsterDefinition(12223,"Vardorvis","Post-quest",784,0,215,0,700,0,0,0,0,0,0,0,Collections.emptyList(),2,"",0,0);
        check(DefenceFillPolicy.profile(vard)==DefenceFillPolicy.Profile.SLASH,"verified Vardorvis variant");
        System.out.println("PASS fill preference persistence, exact offence guards and defensive weighting policy");
    }
}
