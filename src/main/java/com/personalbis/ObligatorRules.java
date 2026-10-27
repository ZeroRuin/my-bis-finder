package com.personalbis;
import java.util.*;
final class ObligatorRules {
    private ObligatorRules(){}
    static boolean weapon(EquipmentCandidate w){return w!=null&&(w.getItem().getItemId()==34715||w.getItem().getItemId()==34717);}
    static boolean thrust(EquipmentCandidate w,MeleeStance stance){return w!=null&&w.getItem().getItemId()==34715&&stance!=MeleeStance.SMASH;}
    static int max(int max,MonsterDefinition m){return m==null||m.getSize()<2?max:max*(m.getSize()==2?120:140)/100;}
    static HitDistribution.AttackDistribution distribution(int max,double accuracy,MonsterDefinition m){
        int rolls=2+(m!=null&&m.getStabDefence()>m.getCrushDefence()?1:0)+(m!=null&&m.getSlashDefence()>m.getCrushDefence()?1:0);
        List<HitDistribution.WeightedHit> hits=new ArrayList<>();
        for(int damage=0;damage<=max;damage++){
            double probability=Math.pow((damage+1.0)/(max+1),rolls)-Math.pow(damage/(max+1.0),rolls);
            hits.add(new HitDistribution.WeightedHit(accuracy*probability,Collections.singletonList(new HitDistribution.Hitsplat(Math.max(1,damage),true))));
        }
        if(accuracy<1)hits.add(new HitDistribution.WeightedHit(1-accuracy,Collections.singletonList(new HitDistribution.Hitsplat(0,false))));
        return new HitDistribution.AttackDistribution(Collections.singletonList(new HitDistribution(hits).flatten()));
    }
}
