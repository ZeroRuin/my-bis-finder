package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.runelite.api.Skill;

public final class RaidOptionsSelfTest
{
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static void near(double actual,double expected,String message){check(Math.abs(actual-expected)<1e-9,message+": "+actual);}
    private static MonsterDefinition find(java.util.List<MonsterDefinition> all,String name,String version)
    {
        for(MonsterDefinition m:all)if(m.getName().equals(name)&&m.getVersion().equals(version))return m;
        throw new AssertionError("Missing "+name+" / "+version);
    }
    private static RaidSettings cox(int hp,int combat,int size,boolean cm){return new RaidSettings(hp,combat,size,cm,0,0,1,false);}
    private static RaidSettings toa(int level,int path,int size,boolean exclude){return new RaidSettings(99,126,1,false,level,path,size,exclude);}
    public static void main(String[] args)throws Exception
    {
        WikiDataPack pack=new WikiDataPack(new Gson());
        java.util.List<MonsterDefinition> all=MonsterDatabase.catalogue(pack.getMonsters());
        RaidSettings d=RaidSettings.DEFAULT;
        check(d.coxHp==99&&d.coxCombat==126&&d.coxParty==1&&!d.challenge,"CoX defaults");
        check(d.raidLevel==0&&d.pathLevel==0&&d.toaParty==1&&!d.excludeSalts,"ToA defaults");
        RaidSettings max=new RaidSettings(999,999,999,true,999,999,999,true);
        check(max.coxHp==99&&max.coxCombat==126&&max.coxParty==100,"CoX maximums");
        check(max.raidLevel==700&&max.pathLevel==6&&max.toaParty==8,"ToA maximums");
        for(MonsterDefinition m:all)if(RaidScaling.isCox(m))
        {
            MonsterDefinition scaled=RaidScaling.apply(m,d);
            check(scaled.getHitpoints()==m.getHitpoints()&&scaled.getDefenceLevel()==m.getDefenceLevel()
                &&scaled.getMagicLevel()==m.getMagicLevel()&&scaled.getRangedLevel()==m.getRangedLevel(),"CoX defaults preserve "+m);
        }
        MonsterDefinition tekton=find(all,"Tekton","Normal");
        int[] sizes={1,2,3,4,5,6,7,8,9,10,25,50,75,100};
        int[] hp={450,900,900,1350,1350,1800,1800,2250,2250,2700,5850,11700,17100,22950};
        int[] def={246,248,250,284,287,290,292,292,295,301,332,390,438,491};
        for(int i=0;i<sizes.length;i++)
        {
            MonsterDefinition m=RaidScaling.apply(tekton,cox(99,126,sizes[i],true));
            check(m.getHitpoints()==hp[i]&&m.getDefenceLevel()==def[i]&&m.getMagicLevel()==def[i],"Wiki CM Tekton fixture party "+sizes[i]);
        }
        MonsterDefinition low=RaidScaling.apply(tekton,cox(89,104,2,true));
        check(low.getHitpoints()==741&&low.getDefenceLevel()==234,"Low-party-level CM Tekton fixture");
        MonsterDefinition crystal=find(all,"Glowing crystal","Normal");
        MonsterDefinition crystalCm=RaidScaling.apply(crystal,cox(99,126,1,true));
        check(crystalCm.getHitpoints()==crystal.getHitpoints()&&crystalCm.getDefenceLevel()==crystal.getDefenceLevel(),"CM crystal HP/Defence exception");
        MonsterDefinition head=find(all,"Great Olm","Head"),hand=find(all,"Great Olm","Right claw (Magic hand)");
        check(RaidScaling.apply(head,cox(99,126,1,true)).getHitpoints()==800,"Olm CM no extra HP");
        check(RaidScaling.apply(head,cox(99,126,3,false)).getHitpoints()==1600,"Olm party HP");
        check(RaidScaling.apply(hand,d).getMagicLevel()==87,"Olm magic-hand halving happens once");
        MonsterDefinition baba=find(all,"Ba-Ba","");
        check(RaidScaling.apply(baba,toa(150,0,1,false)).getHitpoints()==610,"Ba-Ba 150 HP fixture");
        check(RaidScaling.apply(baba,toa(300,2,1,false)).getHitpoints()==940,"Ba-Ba path 2 / 300 HP fixture");
        MonsterDefinition akkha=find(all,"Akkha","Magic-vulnerable phase");
        check(RaidScaling.apply(akkha,toa(300,1,1,false)).getHitpoints()==950,"Akkha 300 path 1 fixture");
        check(RaidScaling.apply(akkha,toa(0,0,2,false)).getHitpoints()==760,"ToA party 2 scaling");
        check(RaidScaling.apply(akkha,toa(0,0,3,false)).getHitpoints()==1120,"ToA party 3 scaling");
        check(RaidScaling.apply(akkha,toa(0,0,8,false)).getHitpoints()==2320,"ToA party 8 scaling");
        MonsterDefinition warden=find(all,"Elidinis' Warden","Phase 2 (Magic-vulnerable)");
        check(RaidScaling.apply(warden,toa(300,6,1,false)).getHitpoints()==310,"Wardens ignore path level");
        check(RaidScaling.apply(find(all,"Elidinis' Warden","Phase 2 (Core-ejected)"),toa(150,0,1,false)).getHitpoints()==5180,"Core HP invocation exception");
        MonsterDefinition scaled=RaidScaling.apply(akkha,toa(300,1,1,false));
        check(scaled.getDefenceLevel()==akkha.getDefenceLevel(),"ToA scales rolls, not skill level");
        check(RaidScaling.defenceRoll(scaled,10000)==22000,"ToA defence roll scaling");
        MonsterDefinition overlord=find(all,"Soldier Scarab","");
        check(RaidScaling.defenceRoll(RaidScaling.apply(overlord,toa(300,1,1,false)),10000)==10000,"Overlord defence exception");
        check(!RaidSettings.DEFAULT.key(akkha).equals(toa(300,0,1,false).key(akkha)),"Raid level invalidates key");
        check(!RaidSettings.DEFAULT.key(akkha).equals(toa(0,1,1,false).key(akkha)),"Path level invalidates key");
        check(!RaidSettings.DEFAULT.key(akkha).equals(toa(0,0,2,false).key(akkha)),"Party invalidates key");
        check(!RaidSettings.DEFAULT.key(akkha).equals(toa(0,0,1,true).key(akkha)),"Salts invalidate key");
        check(!RaidSettings.DEFAULT.key(tekton).equals(cox(99,126,1,true).key(tekton)),"CM invalidates key");
        check(RaidSettings.DEFAULT.key(akkha).equals(cox(50,80,10,true).key(akkha)),"Other raid settings isolated");
        check(akkha.getHitpoints()==400&&tekton.getHitpoints()==300,"Catalogue stays unscaled");
        check(RaidScaling.apply(tekton,cox(99,126,2,false)).getHitpoints()==600,"Regeneration starts at base, not previous scaled target");
        AccountSnapshot account=new AccountSnapshot(null){@Override public int real(Skill s){return 99;}@Override public int boosted(Skill s){return 125;}};
        check(RaidScaling.boosted(account,Skill.MAGIC,RaidScaling.apply(akkha,d))==125,"Full salts assumed");
        MonsterDefinition excluded=RaidScaling.apply(akkha,toa(0,0,1,true));
        check(RaidScaling.boosted(account,Skill.MAGIC,excluded)==99,"Observed salt boost cannot leak into exclusion");
        excluded=RaidScaling.withOwnedBoosts(excluded,account,Arrays.asList(new BankItem(1,1,"Super combat potion(4)"),new BankItem(2,1,"Ranging potion(4)"),new BankItem(3,1,"Saturated heart")));
        check(RaidScaling.boosted(account,Skill.ATTACK,excluded)==118,"Owned super combat fallback");
        check(RaidScaling.boosted(account,Skill.RANGED,excluded)==112,"Owned ranged fallback");
        check(RaidScaling.boosted(account,Skill.MAGIC,excluded)==115,"Owned heart fallback");
        for(String v:Arrays.asList("Magic-vulnerable phase","Ranged-vulnerable phase","Melee-vulnerable phase","Final phase (Melee only)"))
        {
            MonsterDefinition m=find(all,"Akkha",v);
            check(WikiTargetEligibility.magicCanDamage(m)==v.startsWith("Magic"),"Akkha Magic restriction "+v);
            check(WikiTargetEligibility.rangedCanDamage(m)==v.startsWith("Ranged"),"Akkha Ranged restriction "+v);
            check(WikiTargetEligibility.meleeCanDamage(m,null)==(v.startsWith("Melee")||v.startsWith("Final")),"Akkha Melee restriction "+v);
        }
        check(find(all,"Akkha","Final phase (Melee only)").getHitpoints()==80,"Akkha final HP snapshot");
        check(RaidScaling.apply(find(all,"Akkha","Final phase (Melee only)"),toa(150,0,1,false)).getHitpoints()==128,"Akkha final HP derives after full-HP rounding");
        check(!WikiTargetEligibility.meleeCanDamage(warden,null)&&WikiTargetEligibility.magicCanDamage(warden)&&!WikiTargetEligibility.rangedCanDamage(warden),"Warden phase 2 style restriction");
        check(find(all,"Zebak","Normal")!=find(all,"Zebak","Enraged"),"Zebak labels not merged");
        check(all.stream().noneMatch(m->m.getName().endsWith("' Warden")&&m.getVersion().equals("Invulnerable")),"Non-attackable Warden state hidden");
        Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);
        for(com.personalbis.wiki.WikiEquipment w:pack.getEquipment())if(w.name.equalsIgnoreCase("Osmumten's fang")){
            gear.put(EquipmentSlot.WEAPON,new EquipmentCandidate(new BankItem(w.id,1,w.name),EquipmentSlot.WEAPON,0,w.offensive.stab,w.bonuses.str,
                w.offensive.ranged,w.bonuses.ranged_str,w.offensive.magic,(float)w.bonuses.magic_str,w.bonuses.prayer,w.speed,w.isTwoHanded,w.category,RequirementResult.usable("fixture")));break;
        }
        check(!gear.isEmpty(),"Fang fixture exists");
        MeleeCombatCalculator meleeCalc=new MeleeCombatCalculator(account);
        CombatResult baseMelee=meleeCalc.calculate(gear,AttackStyle.MELEE_STAB,baba,false);
        MonsterDefinition baba300=RaidScaling.apply(baba,toa(300,0,1,false));
        CombatResult scaledMelee=meleeCalc.calculate(gear,AttackStyle.MELEE_STAB,baba300,false);
        check(scaledMelee.getDefenceRoll()==RaidScaling.defenceRoll(baba300,baseMelee.getDefenceRoll()),"Full melee calculator uses scaled defence");
        near(scaledMelee.getAccuracy(),1-Math.pow(1-WikiAccuracyRoll.normal(scaledMelee.getAttackRoll(),scaledMelee.getDefenceRoll()),2),"Full ToA Fang accuracy branch");
        RangedCombatCalculator rangedCalc=new RangedCombatCalculator(account);
        RangedCombatResult baseRanged=rangedCalc.calculate(Collections.emptyMap(),baba,false);
        RangedCombatResult scaledRanged=rangedCalc.calculate(Collections.emptyMap(),baba300,false);
        check(scaledRanged.getDefenceRoll()==RaidScaling.defenceRoll(baba300,baseRanged.getDefenceRoll()),"Full ranged calculator uses scaled defence");
        MagicCombatCalculator magicCalc=new MagicCombatCalculator(account);
        PoweredStaff staff=PoweredStaff.forWeapon("Trident of the seas");
        MagicCombatResult baseMagic=magicCalc.calculatePowered(Collections.emptyMap(),baba,staff,false);
        MagicCombatResult scaledMagic=magicCalc.calculatePowered(Collections.emptyMap(),baba300,staff,false);
        check(scaledMagic.getDefenceRoll()==RaidScaling.defenceRoll(baba300,baseMagic.getDefenceRoll()),"Full powered Magic uses scaled defence");
        MagicCombatResult excludedMagic=magicCalc.calculatePowered(Collections.emptyMap(),RaidScaling.apply(baba,toa(300,0,1,true)),staff,false);
        check(scaledMagic.getMaxHit()>excludedMagic.getMaxHit(),"Full powered Magic salts change max hit");
        SwingUtilities.invokeAndWait(()->
        {
            final int[] changes={0};RaidOptionsPanel panel=new RaidOptionsPanel(()->changes[0]++,font("runescape.ttf"),font("runescape_bold.ttf"));
            panel.showFor(tekton);check(panel.isVisible()&&panel.cox.isVisible()&&!panel.toa.isVisible(),"CoX controls only");
            panel.coxParty.setValue(100);check(panel.settings().coxParty==100&&changes[0]==1,"UI value captured immutably");
            RaidSettings captured=panel.settings();panel.coxParty.setValue(2);check(captured.coxParty==100,"Captured values do not mutate");
            panel.showFor(akkha);check(panel.toa.isVisible()&&!panel.cox.isVisible(),"ToA controls only");
            check(!panel.excludeSalts.isSelected(),"Salts excluded checkbox starts unchecked");
            panel.excludeSalts.doClick();check(panel.settings().excludeSalts,"Checkbox updates policy");
            panel.showFor(new MonsterDefinition(1,"Other",1,1,1,1,10));check(!panel.isVisible(),"Options hidden outside raids");
            if(args.length>0)try{panel.showFor(tekton);render(panel,args[0]+"-cox.png");panel.showFor(akkha);render(panel,args[0]+"-toa.png");}catch(Exception ex){throw new RuntimeException(ex);}
        });
        System.out.println("PASS "+checks+" raid scaling, variant and controls checks");
    }
    private static Font font(String name)
    {
        try(java.io.InputStream in=RaidOptionsSelfTest.class.getResourceAsStream("/net/runelite/client/ui/"+name)){return Font.createFont(Font.TRUETYPE_FONT,in).deriveFont(16f);}
        catch(Exception ex){throw new RuntimeException(ex);}
    }
    private static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container)layout((Container)x);}
    private static void render(RaidOptionsPanel panel,String path)throws Exception
    {
        JPanel bg=new JPanel(new BorderLayout());bg.setBackground(new Color(30,30,30));bg.add(panel);
        bg.setSize(220,panel.getPreferredSize().height);layout(bg);
        BufferedImage image=new BufferedImage(bg.getWidth(),bg.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();bg.printAll(g);g.dispose();ImageIO.write(image,"png",new File(path));
    }
}
