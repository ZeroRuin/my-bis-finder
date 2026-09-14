package com.personalbis;

import java.util.*;
import java.util.function.Function;

/**
 * Integer hitsplat distribution model ported from the OSRS Wiki DPS calculator HitDist layer.
 * Keeps accuracy state separate from damage so target transforms can operate per hitsplat.
 */
public final class HitDistribution {
    public static final class Hitsplat {
        public final int damage;
        public final boolean accurate;
        public Hitsplat(int damage, boolean accurate){ this.damage=damage; this.accurate=accurate; }
    }
    public static final class WeightedHit {
        public final double probability;
        public final List<Hitsplat> hitsplats;
        public WeightedHit(double probability,List<Hitsplat> hitsplats){this.probability=probability;this.hitsplats=Collections.unmodifiableList(new ArrayList<>(hitsplats));}
        int sum(){int s=0;for(Hitsplat h:hitsplats)s+=h.damage;return s;}
        boolean anyAccurate(){for(Hitsplat h:hitsplats)if(h.accurate)return true;return false;}
    }
    public interface Transformer extends Function<Hitsplat,HitDistribution>{}
    private final List<WeightedHit> hits;
    public HitDistribution(List<WeightedHit> hits){this.hits=Collections.unmodifiableList(new ArrayList<>(hits));}
    public List<WeightedHit> hits(){return hits;}

    public static HitDistribution linear(double accuracy,int minimum,int maximum){
        List<WeightedHit> out=new ArrayList<>();
        double p=accuracy/(maximum-minimum+1.0);
        for(int i=minimum;i<=maximum;i++)out.add(new WeightedHit(p,Collections.singletonList(new Hitsplat(i,true))));
        if(accuracy!=1.0)out.add(new WeightedHit(1.0-accuracy,Collections.singletonList(new Hitsplat(0,false))));
        return new HitDistribution(out);
    }
    /** Wiki modern successful-zero conversion: an accurate zero becomes one. */
    public HitDistribution accurateZeroToOne(){return transform(h->single(Math.max(1,h.damage),h.accurate),false);}
    public HitDistribution transform(Transformer t){return transform(t,true);}
    public HitDistribution transform(Transformer t,boolean transformInaccurate){
        List<WeightedHit> out=new ArrayList<>();
        for(WeightedHit wh:hits){
            HitDistribution acc=new HitDistribution(Collections.singletonList(new WeightedHit(wh.probability,Collections.emptyList())));
            for(Hitsplat h:wh.hitsplats){
                HitDistribution part=(!h.accurate&&!transformInaccurate)?single(h.damage,false):t.apply(h);
                acc=acc.zip(part);
            }
            out.addAll(acc.hits);
        }
        return new HitDistribution(out).flatten();
    }
    public HitDistribution zip(HitDistribution other){
        List<WeightedHit> out=new ArrayList<>();
        for(WeightedHit a:hits)for(WeightedHit b:other.hits){List<Hitsplat> hs=new ArrayList<>(a.hitsplats);hs.addAll(b.hitsplats);out.add(new WeightedHit(a.probability*b.probability,hs));}
        return new HitDistribution(out);
    }
    public HitDistribution flatten(){
        Map<String,Double> probs=new LinkedHashMap<>(); Map<String,List<Hitsplat>> lists=new LinkedHashMap<>();
        for(WeightedHit wh:hits){StringBuilder k=new StringBuilder();for(Hitsplat h:wh.hitsplats)k.append(h.damage).append(':').append(h.accurate?'1':'0').append(';');String key=k.toString();probs.put(key,probs.getOrDefault(key,0.0)+wh.probability);lists.putIfAbsent(key,wh.hitsplats);}
        List<WeightedHit> out=new ArrayList<>();for(Map.Entry<String,Double> e:probs.entrySet())if(e.getValue()>0)out.add(new WeightedHit(e.getValue(),lists.get(e.getKey())));return new HitDistribution(out);
    }
    public HitDistribution cumulative(){
        Map<String,Double> probs=new LinkedHashMap<>();
        for(WeightedHit wh:hits){String key=wh.sum()+":"+(wh.anyAccurate()?1:0);probs.put(key,probs.getOrDefault(key,0.0)+wh.probability);}
        List<WeightedHit> out=new ArrayList<>();for(Map.Entry<String,Double> e:probs.entrySet()){String[] p=e.getKey().split(":");out.add(new WeightedHit(e.getValue(),Collections.singletonList(new Hitsplat(Integer.parseInt(p[0]),"1".equals(p[1])))));}return new HitDistribution(out);
    }
    public double expectedHit(){double v=0;for(WeightedHit wh:hits)v+=wh.probability*wh.sum();return v;}
    public int size(){return hits.size();}
    /** Wiki HitDist.scaleProbability. */
    public HitDistribution scaleProbability(double factor){List<WeightedHit> out=new ArrayList<>();for(WeightedHit wh:hits)out.add(new WeightedHit(wh.probability*factor,wh.hitsplats));return new HitDistribution(out);}
    /** Wiki HitDist.scaleDamage with per-hitsplat truncation. */
    public HitDistribution scaleDamage(int numerator,int divisor){if(divisor==0)throw new IllegalArgumentException("divisor");List<WeightedHit> out=new ArrayList<>();for(WeightedHit wh:hits){List<Hitsplat> hs=new ArrayList<>();for(Hitsplat h:wh.hitsplats)hs.add(new Hitsplat((int)(((long)h.damage*numerator)/divisor),h.accurate));out.add(new WeightedHit(wh.probability,hs));}return new HitDistribution(out);}
    public HitDistribution scaleDamage(int factor){return scaleDamage(factor,1);}
    public int min(){int v=Integer.MAX_VALUE;for(WeightedHit wh:hits)v=Math.min(v,wh.sum());return v==Integer.MAX_VALUE?0:v;}
    public int max(){int v=Integer.MIN_VALUE;for(WeightedHit wh:hits)v=Math.max(v,wh.sum());return v==Integer.MIN_VALUE?0:v;}
    public double probability(){double p=0;for(WeightedHit wh:hits)p+=wh.probability;return p;}

    public static HitDistribution single(int damage,boolean accurate){return new HitDistribution(Collections.singletonList(new WeightedHit(1.0,Collections.singletonList(new Hitsplat(damage,accurate)))));}
    /** Wiki HitDistribution.single(accuracy, hitsplats). */
    public static HitDistribution single(double accuracy,List<Hitsplat> hitsplats){List<WeightedHit> out=new ArrayList<>();out.add(new WeightedHit(accuracy,hitsplats));if(accuracy!=1.0)out.add(new WeightedHit(1.0-accuracy,Collections.singletonList(new Hitsplat(0,false))));return new HitDistribution(out);}
    public static Transformer flatLimit(int maximum,int minimum){return h->single(Math.max(minimum,Math.min(h.damage,maximum)),h.accurate);}
    public static Transformer multiply(int numerator,int divisor,int minimum){return h->{int d=(numerator*h.damage)/divisor;if(minimum!=0)d=h.damage>=minimum?Math.max(minimum,d):Math.max(h.damage,d);return single(d,h.accurate);};}
    public static Transformer divide(int divisor,int minimum){return multiply(1,divisor,minimum);}
    public static Transformer flatAdd(int addend,int minimum){return h->single(Math.max(minimum,h.damage+addend),h.accurate);}
    public static Transformer linearMin(int maximum,int offset){return h->{List<WeightedHit> out=new ArrayList<>();double p=1.0/(maximum+1.0);for(int i=0;i<=maximum;i++)out.add(new WeightedHit(p,Collections.singletonList(new Hitsplat(Math.min(h.damage,i+offset),h.accurate))));return new HitDistribution(out).flatten();};}
    public static Transformer cappedReroll(int limit,int rollMax,int offset){return h->{if(h.damage<=limit)return single(h.damage,h.accurate);List<WeightedHit> out=new ArrayList<>();double p=1.0/(rollMax+1.0);for(int i=0;i<=rollMax;i++)out.add(new WeightedHit(p,Collections.singletonList(new Hitsplat(i+offset,h.accurate))));return new HitDistribution(out).flatten();};}

    /** Structural port of Wiki HitDist.AttackDistribution. */
    public static final class AttackDistribution {
        private final List<HitDistribution> dists;
        public AttackDistribution(List<HitDistribution> dists){this.dists=new ArrayList<>(dists);}
        public List<HitDistribution> dists(){return Collections.unmodifiableList(dists);}
        public void addDist(HitDistribution d){dists.add(d);}
        public HitDistribution zipped(){if(dists.isEmpty())return HitDistribution.single(0,false);HitDistribution r=dists.get(0);for(int i=1;i<dists.size();i++)r=r.zip(dists.get(i));return r;}
        public HitDistribution singleHitsplat(){if(dists.isEmpty())return HitDistribution.single(0,false);HitDistribution r=dists.get(0);for(int i=1;i<dists.size();i++)r=r.zip(dists.get(i)).cumulative();return r;}
        public AttackDistribution transform(Transformer t,boolean transformInaccurate){List<HitDistribution> o=new ArrayList<>();for(HitDistribution d:dists)o.add(d.transform(t,transformInaccurate));return new AttackDistribution(o);}
        public AttackDistribution flatten(){List<HitDistribution> o=new ArrayList<>();for(HitDistribution d:dists)o.add(d.flatten());return new AttackDistribution(o);}
        public AttackDistribution scaleProbability(double f){List<HitDistribution> o=new ArrayList<>();for(HitDistribution d:dists)o.add(d.scaleProbability(f));return new AttackDistribution(o);}
        public AttackDistribution scaleDamage(int n,int div){List<HitDistribution> o=new ArrayList<>();for(HitDistribution d:dists)o.add(d.scaleDamage(n,div));return new AttackDistribution(o);}
        public int min(){int v=0;for(HitDistribution d:dists)v+=d.min();return v;} public int max(){int v=0;for(HitDistribution d:dists)v+=d.max();return v;} public double expectedDamage(){double v=0;for(HitDistribution d:dists)v+=d.expectedHit();return v;}
        public AttackDistribution firstHitMax(){if(dists.isEmpty())return this;HitDistribution first=dists.get(0);int mx=0;for(WeightedHit wh:first.hits)if(!wh.hitsplats.isEmpty()&&wh.hitsplats.get(0).accurate)mx=Math.max(mx,wh.hitsplats.get(0).damage);List<WeightedHit> hs=new ArrayList<>();for(WeightedHit wh:first.hits){if(wh.hitsplats.isEmpty()||!wh.hitsplats.get(0).accurate){hs.add(wh);continue;}List<Hitsplat> spl=new ArrayList<>(wh.hitsplats);spl.set(0,new Hitsplat(mx,true));hs.add(new WeightedHit(wh.probability,spl));}List<HitDistribution> o=new ArrayList<>();o.add(new HitDistribution(hs).flatten());o.addAll(dists.subList(1,dists.size()));return new AttackDistribution(o);}
        public AttackDistribution firstHitAccurate(){if(dists.isEmpty())return this;List<WeightedHit> hs=new ArrayList<>();double p=0;for(WeightedHit wh:dists.get(0).hits)if(!wh.hitsplats.isEmpty()&&wh.hitsplats.get(0).accurate){hs.add(wh);p+=wh.probability;}if(p==0)return this;List<HitDistribution> o=new ArrayList<>();o.add(new HitDistribution(hs).scaleProbability(1.0/p));o.addAll(dists.subList(1,dists.size()));return new AttackDistribution(o);}
        public AttackDistribution firstHitMinimum(int minimum){if(dists.isEmpty())return this;List<HitDistribution> o=new ArrayList<>();o.add(dists.get(0).transform(h->HitDistribution.single(Math.max(h.damage,minimum),h.accurate),false));o.addAll(dists.subList(1,dists.size()));return new AttackDistribution(o).flatten();}
    }

}
