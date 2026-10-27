package com.personalbis;

import java.util.*;
import javax.swing.*;

/** Manual HP, fixed interpolation denominator, variant/profile restore and Swing state. */
public final class VardorvisHpSelfTest
{
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    private static MonsterDefinition monster(String version){
        return new MonsterDefinition(version.equals("Quest")?12224:12223,"Vardorvis",version,784,0,215,0,
            version.equals("Quest")?500:version.equals("Awakened")?1400:700,
            215,65,85,580,580,580,580,Collections.emptyList(),2,"",0,0);
    }
    public static void main(String[] args)throws Exception{
        VardorvisHpSettings defaults=VardorvisHpSettings.decode(null);
        for(String v:new String[]{"Quest","Post-quest","Awakened"}){
            MonsterDefinition m=monster(v);
            check(defaults.apply(m).getHitpoints()==m.getHitpoints(),"full-HP default "+v);
            MonsterDefinition chosen=defaults.with(m,m.getHitpoints()/2).apply(m);
            check(chosen.getHitpoints()==m.getHitpoints()/2,"selected starting HP "+v);
            check(VardorvisRules.maximumHp(chosen)==m.getHitpoints(),"original maximum retained "+v);
            check(VardorvisRules.defence(chosen,chosen.getHitpoints())==VardorvisRules.defence(m,m.getHitpoints()/2),"original scaling reference "+v);
        }
        MonsterDefinition partial=defaults.with(monster("Post-quest"),200).apply(monster("Post-quest"));
        int[] highestHp={0};
        double ttk=ExpectedKillTime.seconds(partial.getHitpoints(),4,hp->{
            highestHp[0]=Math.max(highestHp[0],hp);
            return new HitDistribution.AttackDistribution(Collections.singletonList(HitDistribution.single(1,true)));
        });
        check(highestHp[0]==200&&Math.abs(ttk-480.0)<1e-8,"remaining fight starts at selected HP, not maximum");
        VardorvisHpSettings saved=defaults.with(monster("Post-quest"),350).with(monster("Quest"),100);
        check(saved.hp(monster("Awakened"))==1400,"independent variants");
        check(VardorvisRules.startingDefence(saved.apply(monster("Post-quest")))==180,"350 of 700 HP starts at Defence180");
        check(VardorvisHpSettings.decode(saved.encode()).encode().equals(saved.encode()),"saved round trip");
        check(VardorvisHpSettings.decode("bad").encode().equals(defaults.encode()),"corrupt setting default");
        check(new VardorvisHpSettings(0,800,2000).encode().equals("1|1|700|1400"),"bounds");
        MonsterDefinition other=new MonsterDefinition(1,"Other",1,1,1,1,10);
        check(saved.apply(other)==other,"non-Vardorvis unchanged");
        check(!VardorvisRules.supported(monster("Deadman")),"unsupported variant excluded");
        SwingUtilities.invokeAndWait(()->{
            int[] changes={0};VardorvisHpOptions ui=new VardorvisHpOptions(()->changes[0]++);
            check(!ui.isVisible(),"hidden by default");
            ui.showFor(monster("Post-quest"));check(ui.isVisible()&&ui.startingHp.getValue().equals(700),"post-quest defaults");
            ui.startingHp.setValue(350);check(changes[0]==1&&ui.settings().postQuest==350,"explicit change callback");
            ui.showFor(monster("Quest"));check(ui.startingHp.getValue().equals(500),"Quest default and maximum");
            ui.showFor(monster("Awakened"));check(ui.startingHp.getValue().equals(1400),"Awakened default");
            ui.showFor(monster("Post-quest"));check(ui.startingHp.getValue().equals(350),"variant value preserved");
            ui.restore(defaults);check(ui.startingHp.getValue().equals(700)&&changes[0]==1,"profile default restore without writing");
            ui.restore(saved);check(ui.startingHp.getValue().equals(350)&&changes[0]==1,"profile selection restore");
            ui.showFor(other);check(!ui.isVisible(),"other target hides control");
        });
        System.out.println("PASS Vardorvis manual HP variants, scaling reference, saved settings and Swing/profile restore");
    }
}
