package com.personalbis;

import java.util.Collections;

public final class HpStateTtkSelfTest
{
    private static HitDistribution.AttackDistribution hit(double chance,int damage)
    {
        return new HitDistribution.AttackDistribution(Collections.singletonList(HitDistribution.linear(chance,damage,damage)));
    }
    private static void close(double a,double b){if(Math.abs(a-b)>1e-9)throw new AssertionError(a+" != "+b);}
    public static void main(String[] args)
    {
        close(ExpectedKillTime.seconds(10,4,hp->hit(1,2)),12.0);
        close(ExpectedKillTime.secondsWithStates(10,4,1,(state,hp)->hit(1,2)),12.0);
        close(ExpectedKillTime.secondsWithStates(4,4,2,(state,hp)->hit(state==0?0:1,2)),7.2);
        close(ExpectedKillTime.secondsWithStates(4,4,2,(state,hp)->hit(.5,2)),9.6);
        close(ExpectedKillTime.seconds(4,4,hp->hit(hp>2?.5:1,2)),7.2);
        // HP scaling is resolved before an attack; all hitsplats in that attack share its context.
        HitDistribution.AttackDistribution multi=new HitDistribution.AttackDistribution(java.util.Arrays.asList(HitDistribution.single(1,true),HitDistribution.single(1,true)));
        close(ExpectedKillTime.seconds(3,4,hp->multi),4.8);
        System.out.println("PASS HP-varying and stateful finite TTK, misses and multi-hit overkill");
    }
}
