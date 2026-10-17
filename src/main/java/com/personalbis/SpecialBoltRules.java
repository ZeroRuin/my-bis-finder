package com.personalbis;

import java.util.*;

/** Zaryte special: a successful accuracy roll forces its enchanted-bolt effect.
 * Wiki dists/bolts.ts ordering: non-ruby, accurate-zero, Corp, ruby, NPC transforms. */
final class SpecialBoltRules
{
    private SpecialBoltRules(){}
    static HitDistribution distribution(EquipmentCandidate ammo,MonsterDefinition m,int level,int min,int max,
        double accuracy,boolean diary,boolean corp){
        String n=m!=null&&EnchantedBoltEffects.enchanted(ammo)?ammo.getItem().getName().toLowerCase(Locale.ROOT):"";
        HitDistribution d=HitDistribution.linear(accuracy,min,max);
        boolean dragonBlocked=m!=null&&(m.hasAttribute("dragon")||m.hasAttribute("fiery")||m.hasAttribute("dragonfire immune"));
        if(n.contains("diamond"))d=replacement(d,HitDistribution.linear(1,0,max*126/100),.10*(diary?1.1:1),false);
        else if(n.contains("onyx")&&!m.hasAttribute("undead"))d=replacement(d,HitDistribution.linear(1,0,max*132/100),.11*(diary?1.1:1),true);
        else if(n.contains("opal"))d=bonus(d,level/9,.05*(diary?1.1:1),false);
        else if(n.contains("pearl"))d=bonus(d,level/(m.hasAttribute("fiery")?13:18),.06*(diary?1.1:1),false);
        else if(n.contains("dragonstone")&&!dragonBlocked)d=bonus(d,level*2/9,.06*(diary?1.1:1),true);
        d=d.accurateZeroToOne();
        if(corp)d=d.transform(HitDistribution.divide(2,0));
        if(n.contains("ruby")){
            int cap=m.getId()==14779?66:110;
            d=replacement(d,HitDistribution.single(Math.min(cap,m.getHitpoints()*22/100),true),.06*(diary?1.1:1),false);
        }
        return d.flatten();
    }
    private static HitDistribution replacement(HitDistribution base,HitDistribution effect,double p,boolean accurateOnly){
        return base.transform(h->{
            if(h.accurate)return effect;
            if(accurateOnly)return HitDistribution.single(h.damage,false);
            List<HitDistribution.WeightedHit> out=new ArrayList<>();
            for(HitDistribution.WeightedHit e:effect.hits())out.add(new HitDistribution.WeightedHit(p*e.probability,e.hitsplats));
            out.add(new HitDistribution.WeightedHit(1-p,Collections.singletonList(h)));return new HitDistribution(out);
        });
    }
    private static HitDistribution bonus(HitDistribution base,int extra,double p,boolean accurateOnly){
        return base.transform(h->{
            if(h.accurate)return HitDistribution.single(h.damage+extra,true);
            if(accurateOnly)return HitDistribution.single(h.damage,false);
            return new HitDistribution(Arrays.asList(
                new HitDistribution.WeightedHit(p,Collections.singletonList(new HitDistribution.Hitsplat(h.damage+extra,false))),
                new HitDistribution.WeightedHit(1-p,Collections.singletonList(h))));
        });
    }
}
