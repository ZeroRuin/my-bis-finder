package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.LineBorder;

public final class TargetStyleImmunitySelfTest
{
    private static int checks;
    private static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    private static MonsterDefinition target(List<MonsterDefinition> all,String name,String version)
    {
        for(MonsterDefinition m:all)if(m.getName().equals(name)&&m.getVersion().equals(version))return m;
        throw new AssertionError("Missing "+name+" / "+version);
    }
    public static void main(String[] args)throws Exception
    {
        List<MonsterDefinition> all=MonsterDatabase.catalogue(new WikiDataPack(new Gson()).getMonsters());
        MonsterDefinition tekton=target(all,"Tekton","Normal");
        check(TargetStyleImmunity.immune(tekton,AttackStyle.RANGED),"Tekton Ranged immunity");
        check(!TargetStyleImmunity.immune(tekton,AttackStyle.MAGIC),"Damage reduction is not immunity");
        MonsterDefinition guardian=target(all,"Guardian (Chambers of Xeric)","Normal");
        check(TargetStyleImmunity.immune(guardian,AttackStyle.RANGED)&&TargetStyleImmunity.immune(guardian,AttackStyle.MAGIC),"Guardian ranged/magic immunity");
        check(!TargetStyleImmunity.immune(guardian,AttackStyle.MELEE_CRUSH),"Pickaxe exception remains searchable");
        for(MonsterDefinition m:all)if(m.getName().equals("Zulrah"))
            for(AttackStyle s:new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH})
                check(!TargetStyleImmunity.immune(m,s),"Zulrah polearm exception is not blanket immunity");
        MonsterDefinition vespula=target(all,"Vespula","Normal");
        check(TargetStyleImmunity.immune(vespula,AttackStyle.MELEE_SLASH),"Vespula remains melee immune");
        MonsterDefinition nylo=target(all,"Nylocas Vasilias","Normal Mode, Magic form");
        check(TargetStyleImmunity.immune(nylo,AttackStyle.MELEE_SLASH)&&TargetStyleImmunity.immune(nylo,AttackStyle.RANGED),"Nylocas phase immunity");
        check(!TargetStyleImmunity.immune(nylo,AttackStyle.MAGIC),"Matching Nylo style allowed");
        MonsterDefinition akkha=target(all,"Akkha","Final phase (Melee only)");
        check(TargetStyleImmunity.immune(akkha,AttackStyle.MAGIC)&&TargetStyleImmunity.immune(akkha,AttackStyle.RANGED),"Akkha final phase immunity");
        check(!TargetStyleImmunity.immune(null,AttackStyle.MAGIC),"No target is not immune");
        AtomicInteger searches=new AtomicInteger();int skipped=0;
        for(MonsterDefinition m:all)for(AttackStyle style:AttackStyle.values())if(TargetStyleImmunity.immune(m,style))
        {
            int result=TargetStyleImmunity.calculate(m,style,()->{searches.incrementAndGet();return 1;},()->0);
            check(result==0,"Empty result for immune "+m+" / "+style);skipped++;
        }
        check(skipped>20&&searches.get()==0,"No immune optimizer supplier executes");
        int result=TargetStyleImmunity.calculate(nylo,AttackStyle.MAGIC,()->{searches.incrementAndGet();return 1;},()->0);
        check(result==1&&searches.get()==1,"Allowed search executes once");
        MeleeOptimizationReport empty=TargetStyleImmunity.calculate(nylo,AttackStyle.MELEE_SLASH,()->{throw new AssertionError("Unexpected search");},()->new MeleeOptimizationReport(null));
        check(empty!=null&&empty.getBest()==null,"Immune report is cached as completed empty result");
        Map<AttackStyle,Double> scores=new EnumMap<>(AttackStyle.class);
        for(AttackStyle s:AttackStyle.values())scores.put(s,TargetStyleImmunity.immune(nylo,s)?Double.NEGATIVE_INFINITY:s==AttackStyle.MAGIC?-10.0:-100.0);
        check(BestStyleSelection.best(scores)==AttackStyle.MAGIC,"Immune styles cannot win BEST");
        SwingUtilities.invokeAndWait(()->
        {
            JRadioButton button=new JRadioButton("Ranged");
            BufferedImage image=new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g=image.createGraphics();g.setColor(Color.RED);g.fillRect(0,0,8,8);g.dispose();
            ImageIcon normal=new ImageIcon(image);button.setIcon(normal);
            TargetStyleImmunity.icon(button,true);check(button.getIcon()!=normal,"Immune icon muted");
            Object muted=button.getIcon();TargetStyleImmunity.icon(button,true);check(button.getIcon()==muted,"Muted icon cached");
            JTextArea notice=new JTextArea();AtomicInteger clickedSearches=new AtomicInteger();
            button.addActionListener(e->{
                notice.setText(TargetStyleImmunity.message(nylo,AttackStyle.RANGED));
                TargetStyleImmunity.calculate(nylo,AttackStyle.RANGED,()->{clickedSearches.incrementAndGet();return 1;},()->0);
            });
            button.doClick();check(button.isEnabled()&&button.isSelected(),"Muted button stays clickable");
            check(notice.getText().equals("Nylocas Vasilias (Normal Mode, Magic form) is immune to this combat style."),"Message names target and phase");
            check(clickedSearches.get()==0,"Clicking immune style does not calculate");
            TargetStyleImmunity.icon(button,false);check(button.getIcon()==normal,"Normal icon restored on allowed target");
            RaidOptionsPanel panel=new RaidOptionsPanel(()->{},new Font(Font.DIALOG,Font.PLAIN,14),new Font(Font.DIALOG,Font.BOLD,14));
            for(JSpinner spinner:new JSpinner[]{panel.coxHp,panel.coxCombat,panel.coxParty,panel.raidLevel,panel.pathLevel,panel.toaParty,panel.tfaParty})
                check(spinner.getBorder() instanceof LineBorder&&((LineBorder)spinner.getBorder()).getLineColor().equals(new Color(103,90,67)),"Shared raid input border");
        });
        System.out.println("PASS "+checks+" target immunity, search skipping and input styling checks");
    }
}
