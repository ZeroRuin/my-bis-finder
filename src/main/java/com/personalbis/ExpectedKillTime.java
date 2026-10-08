package com.personalbis;

import java.util.function.IntFunction;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** Expected attacks/time to reduce a finite HP pool to zero. */
public final class ExpectedKillTime
{
    private static final Map<String,Double> STATIC_CACHE=new ConcurrentHashMap<>();
    private static final int CACHE_LIMIT=32768;

    private ExpectedKillTime(){}

    public static double seconds(HitDistribution.AttackDistribution distribution, int hitpoints, int attackTicks)
    {
        if (hitpoints <= 0) return 0.0;
        if (attackTicks <= 0 || distribution == null) return Double.POSITIVE_INFINITY;
        return seconds(distribution.zipped().cumulative(),hitpoints,attackTicks);
    }

    private static double seconds(HitDistribution total, int hitpoints, int attackTicks)
    {
        Map<Integer,Double> probabilities=new TreeMap<>();
        for(HitDistribution.WeightedHit hit:total.hits())
        {
            int damage=Math.max(0,hit.sum());
            probabilities.put(damage,probabilities.getOrDefault(damage,0.0)+hit.probability);
        }
        double zero=probabilities.getOrDefault(0,0.0),progress=1.0-zero;
        if(progress<=1e-12)return Double.POSITIVE_INFINITY;
        String key=cacheKey(probabilities,hitpoints,attackTicks);
        Double cached=STATIC_CACHE.get(key);
        if(cached!=null)return cached;
        if(STATIC_CACHE.size()>CACHE_LIMIT)STATIC_CACHE.clear();

        double[] attacks=new double[hitpoints+1];
        int maximum=probabilities.isEmpty()?0:((TreeMap<Integer,Double>)probabilities).lastKey();
        double base=maximum>0?probabilities.getOrDefault(maximum,0.0):0.0;
        boolean dense=base>0.0;
        for(int damage=2;damage<=maximum&&dense;damage++)
            dense=close(probabilities.getOrDefault(damage,0.0),base);
        double first=probabilities.getOrDefault(1,0.0);
        boolean uniform=dense&&close(first,base),modern=dense&&close(first,2.0*base);
        if(uniform||modern)
        {
            double window=0.0;
            for(int hp=1;hp<=hitpoints;hp++)
            {
                window+=attacks[hp-1];
                int expired=hp-maximum-1;
                if(expired>=0)window-=attacks[expired];
                double continuation=base*window+(modern?base*attacks[hp-1]:0.0);
                attacks[hp]=(1.0+continuation)/progress;
            }
        }
        else for(int hp=1;hp<=hitpoints;hp++)
        {
            double continuation=0.0;
            for(Map.Entry<Integer,Double> outcome:probabilities.entrySet())
            {
                int damage=outcome.getKey();
                if(damage>0&&damage<hp)continuation+=outcome.getValue()*attacks[hp-damage];
            }
            attacks[hp]=(1.0+continuation)/progress;
        }
        double result=attacks[hitpoints]*attackTicks*0.6;
        STATIC_CACHE.put(key,result);
        return result;
    }

    private static boolean close(double a,double b)
    {
        return Math.abs(a-b)<=1e-12*Math.max(1.0,Math.max(Math.abs(a),Math.abs(b)));
    }

    private static String cacheKey(Map<Integer,Double> probabilities,int hitpoints,int attackTicks)
    {
        StringBuilder key=new StringBuilder().append(hitpoints).append('/').append(attackTicks);
        for(Map.Entry<Integer,Double> outcome:probabilities.entrySet())key.append(';').append(outcome.getKey())
            .append(':').append(Long.toHexString(Double.doubleToLongBits(outcome.getValue())));
        return key.toString();
    }

    /** Supports effects such as Ruby bolts whose distribution changes with remaining HP. */
    public static double seconds(int hitpoints, int attackTicks,
        IntFunction<HitDistribution.AttackDistribution> distributionAtHp)
    {
        if (hitpoints <= 0) return 0.0;
        if (attackTicks <= 0 || distributionAtHp == null) return Double.POSITIVE_INFINITY;
        double[] attacks = new double[hitpoints + 1];
        for (int hp = 1; hp <= hitpoints; hp++)
        {
            HitDistribution.AttackDistribution attack = distributionAtHp.apply(hp);
            if (attack == null) return Double.POSITIVE_INFINITY;
            HitDistribution total = attack.zipped().cumulative();
            double zero = 0.0, continuation = 0.0;
            for (HitDistribution.WeightedHit hit : total.hits())
            {
                int damage = Math.max(0, hit.sum());
                if (damage == 0) zero += hit.probability;
                else if (damage < hp) continuation += hit.probability * attacks[hp - damage];
            }
            double progress = 1.0 - zero;
            if (progress <= 1e-12) return Double.POSITIVE_INFINITY;
            attacks[hp] = (1.0 + continuation) / progress;
        }
        return attacks[hitpoints] * attackTicks * 0.6;
    }

    public static double approximateSeconds(int hitpoints, double dps)
    {
        return hitpoints > 0 && dps > 0.0 && Double.isFinite(dps)
            ? hitpoints / dps : Double.POSITIVE_INFINITY;
    }
}
