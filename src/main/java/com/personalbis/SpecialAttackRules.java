package com.personalbis;

import java.util.*;

/** Direct-damage special distributions. Source: Wiki PlayerVsNPCCalc and dists/claws.ts (GPL-3.0).
 * Utility, defence drains, burns, regeneration and rotations are intentionally not credited. */
final class SpecialAttackRules
{
    private SpecialAttackRules() {}
    static final Set<String> MELEE=new HashSet<>(Arrays.asList("Dragon dagger","Abyssal dagger","Dragon claws",
        "Armadyl godsword","Bandos godsword","Saradomin godsword","Zamorak godsword","Voidwaker",
        "Dragon mace","Dragon sword","Dragon longsword","Dragon scimitar","Abyssal whip","Dragon warhammer",
        "Elder maul","Barrelchest anchor","Dragon halberd","Crystal halberd","Granite hammer","Osmumten's fang"));
    static int attackPercent(String name){
        if(name.endsWith("godsword")||name.equals("Barrelchest anchor"))return 200;
        if(name.equals("Osmumten's fang")||name.equals("Granite hammer"))return 150;
        if(name.equals("Dragon dagger"))return 115;
        if(Arrays.asList("Elder maul","Dragon mace","Dragon sword","Dragon scimitar","Abyssal whip","Abyssal dagger").contains(name))return 125;
        return 100;
    }
    static int maxHit(String name,int max){
        if(name.endsWith("godsword"))max=max*11/10;
        if(name.equals("Bandos godsword"))return max*11/10;
        if(Arrays.asList("Armadyl godsword","Dragon sword","Dragon longsword").contains(name))return max*5/4;
        if(name.equals("Dragon mace")||name.equals("Dragon warhammer"))return max*3/2;
        if(name.equals("Dragon dagger"))return max*23/20;
        if(name.equals("Abyssal dagger"))return max*17/20;
        if(name.equals("Dragon halberd")||name.equals("Crystal halberd")||name.equals("Barrelchest anchor"))return max*11/10;
        return max;
    }
    static HitDistribution.AttackDistribution melee(String name,int baseMax,double accuracy,double secondAccuracy,int size){
        int max=maxHit(name,baseMax);
        if(name.equals("Dragon claws"))return claws(accuracy,max);
        if(name.equals("Voidwaker"))return single(HitDistribution.linear(1,baseMax/2,baseMax+baseMax/2));
        if(name.equals("Osmumten's fang"))return single(HitDistribution.linear(accuracy,baseMax*3/20,baseMax).accurateZeroToOne());
        HitDistribution hit=HitDistribution.linear(accuracy,0,max).accurateZeroToOne();
        if(name.equals("Granite hammer"))hit=hit.transform(HitDistribution.flatAdd(5,0),true);
        if(name.equals("Dragon dagger"))return new HitDistribution.AttackDistribution(Arrays.asList(hit,hit));
        if(name.equals("Abyssal dagger")){
            List<HitDistribution.WeightedHit> out=new ArrayList<>();
            HitDistribution raw=HitDistribution.linear(accuracy,0,max).accurateZeroToOne();
            for(HitDistribution.WeightedHit h:raw.hits()){
                if(h.hitsplats.get(0).accurate)for(HitDistribution.WeightedHit h2:HitDistribution.linear(1,0,max).accurateZeroToOne().hits()){
                    List<HitDistribution.Hitsplat> splats=new ArrayList<>(h.hitsplats);splats.addAll(h2.hitsplats);
                    out.add(new HitDistribution.WeightedHit(h.probability*h2.probability,splats));
                }else out.add(h);
            }
            return single(new HitDistribution(out));
        }
        if(size>1&&(name.equals("Dragon halberd")||name.equals("Crystal halberd")))return new HitDistribution.AttackDistribution(
            Arrays.asList(hit,HitDistribution.linear(secondAccuracy,0,max).accurateZeroToOne()));
        return single(hit);
    }
    static HitDistribution.AttackDistribution single(HitDistribution d){return new HitDistribution.AttackDistribution(Collections.singletonList(d));}
    static HitDistribution.AttackDistribution claws(double accuracy,int max){
        if(max<=0)return single(HitDistribution.single(0,false));
        List<HitDistribution.WeightedHit> out=new ArrayList<>();
        for(int failed=0;failed<4;failed++){
            int low=max*(4-failed)/4,high=max+low-1;
            double p=Math.pow(1-accuracy,failed)*accuracy/(high-low+1);
            for(int total=low;total<=high;total++){
                int[] hits=failed==0?new int[]{total/2,total/4,total/8,total/8+1}
                    :failed==1?new int[]{total/2,total/4,total/4+1}
                    :failed==2?new int[]{total/2,total/2+1}:new int[]{total+1};
                List<HitDistribution.Hitsplat> splats=new ArrayList<>();
                for(int h:hits)splats.add(new HitDistribution.Hitsplat(h,true));
                out.add(new HitDistribution.WeightedHit(p,splats));
            }
        }
        double fail=Math.pow(1-accuracy,4);
        out.add(new HitDistribution.WeightedHit(fail*2/3,Arrays.asList(new HitDistribution.Hitsplat(1,false),new HitDistribution.Hitsplat(1,false))));
        out.add(new HitDistribution.WeightedHit(fail/3,Collections.singletonList(new HitDistribution.Hitsplat(0,false))));
        return single(new HitDistribution(out).flatten());
    }
}
