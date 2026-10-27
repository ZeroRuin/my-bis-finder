package com.personalbis;

import java.util.*;
import com.personalbis.ManualDefenceReduction.Choice;

public final class ManualDefenceReductionSelfTest
{
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static MonsterDefinition monster(int id,String name,int defence)
    {return new MonsterDefinition(id,name,"Standard",100,defence,99,80,500,30,40,50,60,70,80,90,Arrays.asList("undead","raid_level:150"),3,"fire",50,10);}
    public static void main(String[] args)
    {
        check(ManualDefenceReduction.decode(null)==Choice.OFF,"new profile Off");
        check(ManualDefenceReduction.decode("DWH")==Choice.DWH,"saved choice");
        Set<Choice> owned=ManualDefenceReduction.owned(Arrays.asList(new BankItem(13576,1,"Dragon warhammer"),new BankItem(27100,1,"Elder maul (or)")));
        check(owned.equals(EnumSet.of(Choice.DWH,Choice.MAUL)),"base and cosmetic ownership");
        check(ManualDefenceReduction.owned(Arrays.asList(new BankItem(13576,0,"Dragon warhammer"))).isEmpty(),"zero quantity never owned");
        check(ManualDefenceReduction.revalidate(Choice.DWH,Collections.emptySet())==Choice.OFF,"clear active choice when item disappears");
        check(ManualDefenceReduction.revalidate(Choice.MAUL,owned)==Choice.MAUL,"retain owned choice");
        check(ManualDefenceReduction.reducedLevel(215,0,Choice.DWH)==151,"DWH floors loss, not remainder");
        check(ManualDefenceReduction.reducedLevel(215,0,Choice.MAUL)==140,"maul floors loss");
        check(ManualDefenceReduction.reducedLevel(99,0,Choice.DWH)==70,"DWH integer boundary");
        check(ManualDefenceReduction.reducedLevel(100,90,Choice.DWH)==90,"floor clamp");
        check(ManualDefenceReduction.reducedLevel(50,90,Choice.MAUL)==50,"never increase Defence below floor");
        int[][] floors={{8387,100},{9425,120},{11789,70},{11778,60},{11719,60},{11730,50},{11761,120},{11751,60},{11278,250},{13668,90},{14014,120},{14176,145}};
        for(int[] row:floors)check(ManualDefenceReduction.floor(monster(row[0],"Test",300))==row[1],"verified floor "+row[0]);
        check(ManualDefenceReduction.apply(monster(12223,"Vardorvis",215),Choice.DWH,true).getDefenceLevel()==215,"Vardorvis immune");
        check(ManualDefenceReduction.apply(monster(8372,"Verzik Vitur",300),Choice.MAUL,true).getDefenceLevel()==300,"Verzik immune");
        MonsterDefinition target=monster(8061,"Vorkath",215);
        check(ManualDefenceReduction.apply(target,Choice.DWH,false).getDefenceLevel()==215,"cannot-land guard");
        check(ManualDefenceReduction.apply(target,Choice.OFF,true)==target,"Off does not transform target");
        MonsterDefinition reduced=ManualDefenceReduction.apply(target,Choice.DWH,true);
        check(reduced.getDefenceLevel()==151&&reduced.getHitpoints()==500&&reduced.getMagicLevel()==99,"only selected Defence level changes");
        check(reduced.getSlashDefence()==40&&reduced.getWeaknessSeverity()==50&&reduced.hasAttribute("raid_level:150"),"target context preserved");
        check(ManualDefenceReduction.note(reduced).contains("no recovery")&&ManualDefenceReduction.note(reduced).contains("full-HP"),"explicit lifetime and setup assumption");
        System.out.println("PASS manual drain ownership, clearing, rounding, floors, immunity and modelling notes");
    }
}
