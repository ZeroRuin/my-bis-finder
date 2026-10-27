package com.personalbis;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Exact distribution parity and bounded-heap regression for the TTK kernel. */
public final class TtkPerformanceSelfTest
{
    private static void close(double a,double b) {
        if(!Double.isFinite(a)||!Double.isFinite(b)||Math.abs(a-b)>1e-9*Math.max(1.0,Math.abs(b)))
            throw new AssertionError(a+" != "+b);
    }
    private static double[] legacy(HitDistribution.AttackDistribution attack) {
        HitDistribution total=attack.zipped().cumulative();
        double[] values=new double[Math.max(0,total.max())+1];
        for(HitDistribution.WeightedHit hit:total.hits())values[Math.max(0,hit.sum())]+=hit.probability;
        return values;
    }
    private static double reference(int hp,int ticks,java.util.function.IntFunction<double[]> atHp) {
        double[] expected=new double[hp+1];
        for(int remaining=1;remaining<=hp;remaining++){
            double[] damage=atHp.apply(remaining);double continuation=0;
            for(int d=1;d<damage.length&&d<remaining;d++)continuation+=damage[d]*expected[remaining-d];
            expected[remaining]=(1+continuation)/(1-damage[0]);
        }
        return expected[hp]*ticks*.6;
    }
    private static HitDistribution.AttackDistribution attack(int maximum,double accuracy,int components) {
        List<HitDistribution> parts=new ArrayList<>();
        for(int hit=0;hit<components;hit++)parts.add(HitDistribution.linear(accuracy,0,maximum>>hit).accurateZeroToOne());
        return new HitDistribution.AttackDistribution(parts);
    }
    public static void main(String[] args) {
        Random random=new Random(9917);
        for(int i=0;i<100;i++){
            HitDistribution.AttackDistribution attack=attack(1+random.nextInt(12),.1+.9*random.nextDouble(),1+random.nextInt(3));
            double[] old=legacy(attack),compact=attack.damageProbabilities();
            if(old.length!=compact.length)throw new AssertionError("damage support");
            for(int damage=0;damage<old.length;damage++)close(compact[damage],old[damage]);
            close(ExpectedKillTime.seconds(attack,70,4),reference(70,4,hp->old));
        }
        // Correlated hits within one component must not become independent.
        HitDistribution correlated=new HitDistribution(Arrays.asList(
            new HitDistribution.WeightedHit(.3,Arrays.asList(new HitDistribution.Hitsplat(4,true),new HitDistribution.Hitsplat(2,true))),
            new HitDistribution.WeightedHit(.7,Collections.singletonList(new HitDistribution.Hitsplat(0,false)))));
        HitDistribution.AttackDistribution joint=new HitDistribution.AttackDistribution(Arrays.asList(correlated,HitDistribution.linear(.4,0,3)));
        double[] old=legacy(joint),compact=joint.damageProbabilities();
        for(int damage=0;damage<old.length;damage++)close(compact[damage],old[damage]);
        Map<Integer,HitDistribution.AttackDistribution> varying=new HashMap<>();
        java.util.function.IntFunction<HitDistribution.AttackDistribution> atHp=hp->varying.computeIfAbsent(hp/5,key->attack(12,.9-hp*.005,2));
        close(ExpectedKillTime.seconds(70,4,atHp),reference(70,4,hp->legacy(atHp.apply(hp))));
        // Cancellation must stop a running HP loop, and not leak into the next job.
        AtomicInteger calls=new AtomicInteger();
        CalculationCancellation.runWhile(()->calls.get()<4,()->{
            ExpectedKillTime.seconds(1400,4,hp->{calls.incrementAndGet();return joint;});
            throw new AssertionError("obsolete job continued");
        });
        if(calls.get()!=4)throw new AssertionError("cancellation responsiveness");
        CalculationCancellation.check();
        close(ExpectedKillTime.seconds(attack(1,1,1),10,4),24.0);
        long start=System.nanoTime();
        // Repeated Vardorvis-shaped HP/Defence contexts. Intended to run with -Xmx64m.
        for(int candidate=0;candidate<100;candidate++){
            final int maximum=60+candidate%40;
            Map<Integer,HitDistribution.AttackDistribution> contexts=new HashMap<>();
            double ttk=ExpectedKillTime.seconds(1400,5,hp->{
                int defence=268-(int)Math.ceil((1400-hp)*87.0/1400);
                return contexts.computeIfAbsent(defence,key->attack(maximum,.85-key/1000.0,2));
            });
            if(!Double.isFinite(ttk)||ttk<=0)throw new AssertionError("stress TTK");
        }
        // Large three-hit support is a few hundred doubles, not millions of tuples.
        double[] large=attack(200,.7,3).damageProbabilities();
        if(large.length!=351)throw new AssertionError("compact support");
        System.out.printf(Locale.ROOT,"PASS exact multi-hit/HP parity, cancellation and 100 bounded-heap TTK cases (%.3f s kernel stress)%n",(System.nanoTime()-start)/1e9);
    }
}
