package com.personalbis;

/** Equipment-bonus preference only. No incoming hit, protection or survival model. */
public final class DefenceFillPolicy
{
    private DefenceFillPolicy() {}

    public enum Profile
    {
        SLASH("Slash defence",new int[]{1}),
        VORKATH("Slash / Ranged / Magic defence (equal weights; dragonfire excluded)",new int[]{1,3,4}),
        UNKNOWN("Target offence unknown: all five defence bonuses (equal weights)",new int[]{0,1,2,3,4});
        public final String description;
        private final int[] channels;
        Profile(String description,int[] channels){this.description=description;this.channels=channels;}
    }

    public static Profile profile(MonsterDefinition target)
    {
        if(VardorvisRules.supported(target))return Profile.SLASH;
        if(target!=null&&"Vorkath".equals(target.getName())
            &&(target.getId()==8058||target.getId()==8059||target.getId()==8060||target.getId()==8061))return Profile.VORKATH;
        return Profile.UNKNOWN;
    }

    /** Channels: stab, slash, crush, magic, ranged. Sum first, weakest bonus on ties. */
    public static int compare(int[] candidate,int candidatePrayer,int[] current,int currentPrayer,Profile profile)
    {
        int candidateSum=0,currentSum=0,candidateMin=Integer.MAX_VALUE,currentMin=Integer.MAX_VALUE;
        for(int channel:profile.channels){candidateSum+=candidate[channel];currentSum+=current[channel];
            candidateMin=Math.min(candidateMin,candidate[channel]);currentMin=Math.min(currentMin,current[channel]);}
        int order=Integer.compare(candidateSum,currentSum);
        if(order==0)order=Integer.compare(candidateMin,currentMin);
        return order==0?Integer.compare(candidatePrayer,currentPrayer):order;
    }

    /** Exact, unrounded results. An accuracy loss is never a rounding tie. */
    public static final class Offence
    {
        private final double accuracy,dps,ttk;
        private final int maxHit,speed,attackRoll,preTransformMax;
        public Offence(double accuracy,int maxHit,int speed,double dps,double ttk,int attackRoll,int preTransformMax)
        {this.accuracy=accuracy;this.maxHit=maxHit;this.speed=speed;this.dps=dps;this.ttk=ttk;this.attackRoll=attackRoll;this.preTransformMax=preTransformMax;}
        public boolean sameAs(Offence other)
        {
            return other!=null&&Double.isFinite(accuracy)&&Double.isFinite(ttk)&&Double.isFinite(dps)
                &&Double.compare(accuracy,other.accuracy)==0&&maxHit==other.maxHit&&speed==other.speed
                &&Double.compare(dps,other.dps)==0&&Double.compare(ttk,other.ttk)==0
                &&attackRoll==other.attackRoll&&preTransformMax==other.preTransformMax;
        }
    }
}
