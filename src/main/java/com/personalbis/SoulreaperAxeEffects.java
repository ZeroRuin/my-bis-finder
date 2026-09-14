package com.personalbis;

/** Current (Summer Sweep-Up 2026) Soulreaper axe auto-attack stack mechanics. */
public final class SoulreaperAxeEffects {
    private SoulreaperAxeEffects(){}

    public static final int MAX_STACKS = 5;

    public static boolean isSoulreaper(EquipmentCandidate w){
        if(w==null||w.getItem()==null||w.getItem().getName()==null)return false;
        return w.getItem().getName().toLowerCase().contains("soulreaper axe");
    }

    public static int clampStacks(int stacks){ return Math.max(0,Math.min(MAX_STACKS,stacks)); }

    /**
     * Each Soul stack contributes a separate 6% of the boosted Strength level.
     * Prayer and Soul-stack contributions are floored independently, then stance
     * and +8 are added. This ordering is required by the Wiki calculator's
     * 0..5-stack boundary cases (notably 4 stacks). Void, if present, is last.
     */
    public static int effectiveStrength(int boostedStrength,double prayerMultiplier,int stanceBoost,boolean voidMelee,int stacks){
        int strength=Math.max(1,boostedStrength);
        int prayerLevel=(int)Math.floor(strength*prayerMultiplier);
        int soulBonus=(int)Math.floor(strength*0.06*clampStacks(stacks));
        int e=prayerLevel+soulBonus+stanceBoost+8;
        return voidMelee?(int)Math.floor(e*1.10):e;
    }

    /**
     * Fight-length-aware auto-attack DPS from a zero-stack start.  The first
     * five swings use stack states 0..4; all later swings use state 5.
     * Expected damage is used continuously for the final partial attack so
     * optimizer ranking does not jump at arbitrary whole-attack HP boundaries.
     */
    public static double rampDps(double[] expectedDamageByStack,int targetHp,double secondsPerAttack){
        if(expectedDamageByStack==null||expectedDamageByStack.length<6||secondsPerAttack<=0)return 0.0;
        double hp=Math.max(1,targetHp);
        double dealt=0.0, attacks=0.0;
        for(int st=0;st<5;st++){
            double ev=Math.max(0.0,expectedDamageByStack[st]);
            if(ev<=0.0)continue;
            if(dealt+ev>=hp){ attacks+=(hp-dealt)/ev; return hp/(attacks*secondsPerAttack); }
            dealt+=ev; attacks+=1.0;
        }
        double sustained=Math.max(0.0,expectedDamageByStack[5]);
        if(sustained<=0.0)return 0.0;
        attacks+=(hp-dealt)/sustained;
        return hp/(attacks*secondsPerAttack);
    }

    public static int maxHit(int boostedStrength,double prayerMultiplier,int stanceBoost,boolean voidMelee,int stacks,int strengthBonus){
        int eff=effectiveStrength(boostedStrength,prayerMultiplier,stanceBoost,voidMelee,stacks);
        return (int)Math.floor(0.5 + eff*(strengthBonus+64)/640.0);
    }
}
