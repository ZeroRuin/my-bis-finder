package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import java.util.*;
import java.lang.reflect.*;
import javax.swing.SwingUtilities;
import net.runelite.api.*;

/** Offline regression fixtures from supplied Wiki tables and pinned calculator source. */
public final class FracturedArchiveSelfTest {
    private static int checks;
    private static void check(boolean yes,String label){if(!yes)throw new AssertionError(label);checks++;}
    private static void near(double a,double b,String label){check(Math.abs(a-b)<1e-9,label+": "+a+" != "+b);}
    private static MonsterDefinition find(List<MonsterDefinition> all,int id,String v){
        return all.stream().filter(m->m.getId()==id&&(v==null||m.getVersion().equals(v))).findFirst().orElseThrow(()->new AssertionError("Missing "+id+" "+v));
    }
    private static EquipmentCandidate item(int id,String name,EquipmentSlot slot,String category){
        return new EquipmentCandidate(new BankItem(id,1,name),slot,0,120,40,34,53,45,.15f,0,5,true,category,RequirementResult.usable("fixture"));
    }
    public static void main(String[] args)throws Exception {
        List<MonsterDefinition> all=MonsterDatabase.catalogue(new WikiDataPack(new Gson()).getMonsters());
        int[][] tables={
            {550,550,550,800,1030,1260,1490,1720,1950},
            {1100,1100,1100,1600,2060,2520,2980,3440,3910},
            {3000,3000,3000,4350,5610,6870,8130,9390,10650},
            {1500,1500,1500,2180,2810,3440,4070,4700,5330},
            {600,600,600,870,1120,1370,1630,1880,2130},
            {2100,2100,2100,3050,3930,4810,5690,6570,7460},
            {2600,2600,2600,3770,4860,5950,7050,8140,9230},
            {2500,2500,2500,3630,4680,5730,6780,7830,8880},
            {5250,5250,5250,7610,9820,12020,14230,16430,18640}};
        for(int[] t:tables)for(int n=1;n<=8;n++)check(FracturedArchiveRules.hp(t[0],n)==t[n],"HP table "+t[0]+" n="+n);
        MonsterDefinition flying=find(all,16659,"Flying"),ground=find(all,16659,"Grounded"),blade=find(all,16675,null);
        RaidSettings eight=new RaidSettings(99,126,1,false,0,0,1,false,99,8);
        check(RaidScaling.apply(ground,eight).getHitpoints()==3910,"grounded base override");
        check(RaidScaling.apply(flying,eight).getDefenceLevel()==260,"HP scaling does not invent Defence scaling");
        check(RaidScaling.apply(blade,eight).getHitpoints()==1000,"Blade does not inherit HP formula");
        check(RaidSettings.decode(eight.encode()).tfaParty==8,"saved party roundtrip");
        check(RaidSettings.decode("1|99|126|1|false|0|0|1|false|99").tfaParty==2,"old settings migration");
        check(RaidSettings.decode("bad").tfaParty==2,"invalid settings default");
        for(String q:new String[]{"tfa","TFA","the fractured archive","fractured archive","archive"}) {
            List<MonsterDefinition> results=TargetSearch.search(all,q,0);
            check(!results.isEmpty()&&results.stream().allMatch(FracturedArchiveRules::isTfa),"filter "+q);
        }
        check(TargetSearch.search(all,"tfa xarth",0).stream().allMatch(m->m.getName().toLowerCase(Locale.ROOT).contains("xarth")),"qualified filter");
        check(all.stream().noneMatch(m->m.getId()>=16678&&m.getId()<=16681),"unverified conditional adds excluded");
        int[][] floors={{16659,156},{16652,78},{16640,180},{16641,0},{16667,117},{16665,0},{16643,0},{16645,0},{16684,102},{16673,129},{16675,0},{16677,150}};
        for(int[] f:floors)check(ManualDefenceReduction.floor(find(all,f[0],null))==f[1],"floor "+f[0]);
        near(DemonbaneEffects.multiplier(find(all,16665,null),70),1.17,"Dreadnoth truncation");
        near(DemonbaneEffects.multiplier(find(all,16684,null),70),1.56,"Xarth demonbane");
        EquipmentCandidate crossbow=item(34681,"Ascension crossbows",EquipmentSlot.WEAPON,"Crossbow");
        EquipmentCandidate normal=item(9185,"Rune crossbow",EquipmentSlot.WEAPON,"Crossbow");
        EquipmentCandidate diamond=item(34696,"Diamond ascension bolts (e)",EquipmentSlot.AMMO,"");
        EquipmentCandidate ordinary=item(9243,"Diamond bolts (e)",EquipmentSlot.AMMO,"");
        check(RangedAmmoRules.compatible(crossbow,diamond)&&!RangedAmmoRules.compatible(crossbow,ordinary),"ascension ammo family");
        check(!RangedAmmoRules.compatible(normal,diamond),"no ascension bolts in ordinary crossbow");
        check(RangedAmmoRules.defenceClass(crossbow)==RangedAmmoRules.DefenceClass.HEAVY,"heavy class");
        check(WikiEquipmentEngine.ammoApplicability(34681,34696)==WikiEquipmentEngine.AmmoApplicability.INCLUDED,"engine counts ammo strength");
        double standard=HitDistribution.linear(.5,0,40).expectedHit();
        double proc=HitDistribution.linear(1,0,46).expectedHit();
        near(EnchantedBoltEffects.preAccurateZeroDistribution(diamond,ground,99,40,.5,true).expectedHit(),standard*(1-.055)+proc*.055,"half diamond diary proc");
        EquipmentCandidate ony=item(34708,"Onyx ascension bolts (e)",EquipmentSlot.AMMO,"");
        near(EnchantedBoltEffects.preAccurateZeroDistribution(ony,ground,99,40,.5,false).expectedHit(),.5*((1-.055)*20+.055*24),"half onyx proc");
        EquipmentCandidate obl=item(34715,"Obligator",EquipmentSlot.WEAPON,"Extending Chain Mace");
        check(MeleeAttackStyleResolver.legalStances(obl,AttackStyle.MELEE_CRUSH).size()==3,"three Obligator modes");
        check(!MeleeAttackStyleResolver.supports(obl,AttackStyle.MELEE_STAB),"no invented stab mode");
        check(ObligatorRules.max(40,ground)==56,"large-target bonus");
        HitDistribution.AttackDistribution dist=ObligatorRules.distribution(40,.7,ground);
        near(dist.dists().get(0).probability(),1,"distribution normalization");
        double expected=0;
        for(int x=0;x<=40;x++)expected+=.7*(Math.pow((x+1.0)/41,4)-Math.pow(x/41.0,4))*Math.max(1,x);
        near(dist.expectedDamage(),expected,"best of four damage rolls, one accuracy roll");
        check(PoweredStaff.forWeapon("Zorya's tome").baseMaxHit(85)==18&&PoweredStaff.forWeapon("Zorya's tome").baseMaxHit(99)==23,"tome level formula");
        check(!WikiTargetEligibility.meleeCanDamage(find(all,16667,null),obl),"Moia unreachable");
        check(!WikiTargetEligibility.meleeCanDamage(flying,obl),"flying Guardian not ordinary melee");
        near(MeleePrayer.ZEAL.getAttack(),1.25,"Zeal attack");near(MeleePrayer.ZEAL.getStrength(),1.28,"Zeal strength");
        check(FinalLoadoutSelection.betterTtk(Double.POSITIVE_INFINITY,5,Double.POSITIVE_INFINITY,4),"DPS-only ranking");
        AccountSnapshot account=new AccountSnapshot(null);
        Field levels=AccountSnapshot.class.getDeclaredField("real");levels.setAccessible(true);
        @SuppressWarnings("unchecked") Map<Skill,Integer> real=(Map<Skill,Integer>)levels.get(account);
        real.put(Skill.PRAYER,84);real.put(Skill.ATTACK,75);real.put(Skill.STRENGTH,75);
        check(!account.isZealUnlocked(),"levels alone do not unlock Zeal");
        Field zeal=AccountSnapshot.class.getDeclaredField("zealUnlocked");zeal.setAccessible(true);zeal.setBoolean(account,true);
        check(account.isZealUnlocked(),"Zeal permanent unlock at requirements");
        real.put(Skill.PRAYER,83);check(!account.isZealUnlocked(),"Zeal Prayer requirement");real.put(Skill.PRAYER,84);
        real.put(Skill.ATTACK,74);check(!account.isZealUnlocked(),"Zeal Attack requirement");real.put(Skill.ATTACK,75);
        real.put(Skill.STRENGTH,74);check(!account.isZealUnlocked(),"Zeal Strength requirement");
        MagicSpell fire=new MagicSpell("Fire Surge",95,24),water=new MagicSpell("Water Surge",85,22);
        check(account.elementalFragmentBonus(fire)==0,"locked fragment no bonus");
        Field fragment=AccountSnapshot.class.getDeclaredField("fragmentFire");fragment.setAccessible(true);fragment.setBoolean(account,true);
        check(account.elementalFragmentBonus(fire)==2&&account.elementalFragmentBonus(water)==0,"fragment applies only to unlocked element");
        WikiMagicDamage.Result frag=WikiMagicDamage.standard(99,.5,0,Collections.emptyList(),null,fire,false,account.elementalFragmentBonus(fire));
        check(frag.baseMax==26&&frag.maxHit==39,"fragment precedes equipment damage multiplier");
        for(AttackStyle style:new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH}){
            check(TargetStyleImmunity.immune(blade,style),"Blade excludes "+style);
            check(TargetStyleImmunity.calculate(blade,style,()->{throw new AssertionError("Blade melee search ran");},()->true),"Blade skips melee search "+style);
        }
        check(!WikiTargetEligibility.meleeCanDamage(blade,obl),"Blade rejects melee equipment");
        check(!TargetStyleImmunity.immune(blade,AttackStyle.RANGED)&&!TargetStyleImmunity.immune(blade,AttackStyle.MAGIC),"Blade keeps Ranged and Magic");
        check(TargetStyleImmunity.message(blade,AttackStyle.MELEE_CRUSH).contains("phase mechanics"),"Blade explains planning exclusion");
        check(find(all,16641,null).getName().equals("The Smorks"),"Smorks remains a separate target");
        for(int i=1;i<=4;i++){
            MonsterDefinition bork=find(all,16640,"Armour stage "+i);
            check(bork.getName().equals("Bork")&&!bork.getVersion().contains("Smorks"),"Bork label stage "+i);
            check(FracturedArchiveRules.borkStage(bork)==i-1,"Bork mechanics retained stage "+i);
            near(WikiNpcTransforms.apply(HitDistribution.linear(1,30,30),bork,AttackStyle.RANGED,null,"",false).expectedHit(),30-(8+2*(i-1)),"Bork armour stage "+i);
        }
        SwingUtilities.invokeAndWait(()->{
            RaidOptionsPanel p=new RaidOptionsPanel(()->{},new java.awt.Font("Dialog",0,12),new java.awt.Font("Dialog",1,12));
            p.restore(eight);p.showFor(ground);check(p.tfa.isVisible()&&!p.cox.isVisible()&&!p.toa.isVisible(),"TFA control isolation");
            p.tfaParty.setValue(3);check(p.settings().tfaParty==3,"party control snapshots");
        });
        System.out.println("PASS "+checks+" Fractured Archive checks");
    }
}
