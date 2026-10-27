package com.personalbis;
import java.util.*;
import com.personalbis.DefenceReductionPlan.Type;
public final class DefenceReductionPlanSelfTest {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static MonsterDefinition target(int id,String name,boolean demon){return new MonsterDefinition(id,name,"Standard",100,215,200,80,500,30,40,50,60,70,80,90,Arrays.asList("npc_attack_level:100","npc_strength_level:120",demon?"demon":"undead"),3,"fire",50,10);}
 private static MonsterDefinition apply(MonsterDefinition m,DefenceReductionPlan p){return p.apply(m,EnumSet.allOf(Type.class));}
 public static void main(String[] args){
  check(DefenceReductionPlan.decode(null).encode().equals("2"),"off default");
  check(DefenceReductionPlan.decode("DWH").amount(Type.DWH)==1,"legacy save migration");
  check(DefenceReductionPlan.decode("2|DWH=bad").encode().equals("2"),"corrupt save");
  DefenceReductionPlan p=DefenceReductionPlan.OFF.with(Type.MAUL,1).with(Type.DWH,1).with(Type.ARCLIGHT,1).with(Type.EMBERLIGHT,1).with(Type.TONALZTICS,1).with(Type.BGS,10).with(Type.SEERCULL,25).with(Type.AYAK,15).with(Type.ACCURSED,1);
  check(DefenceReductionPlan.decode(p.encode()).encode().equals(p.encode()),"all amounts persist");
  MonsterDefinition changed=apply(target(1,"Test",true),p);
  check(changed.getDefenceLevel()==0&&changed.getMagicLevel()==145&&changed.getMagicDefence()==45,"full stacking order, rounded reductions and bonus drain");
  check(DefenceReductionPlan.metadata(changed,"npc_strength_level:")==86&&DefenceReductionPlan.metadata(changed,"npc_attack_level:")==73,"base-stat sword drains and BGS overflow");
  MonsterDefinition overflow=apply(target(1,"Test",false),DefenceReductionPlan.OFF.with(Type.BGS,500));
  check(overflow.getDefenceLevel()==0&&overflow.getMagicLevel()==135,"BGS Defence/Strength/Attack/Magic order");
  MonsterDefinition floor=apply(target(13668,"Araxxor",false),DefenceReductionPlan.OFF.with(Type.BGS,500));
  check(floor.getDefenceLevel()==90&&floor.getMagicLevel()==200,"positive floor stops BGS overflow");
  check(apply(target(1,"Test",false),DefenceReductionPlan.OFF.with(Type.ARCLIGHT,2)).getDefenceLevel()==193,"non-demon base-stat drain");
  check(apply(target(1,"Test",true),DefenceReductionPlan.OFF.with(Type.ARCLIGHT,2)).getDefenceLevel()==171,"demon base-stat drain");
  DefenceReductionPlan alternatives=p.with(Type.VULNERABILITY,1);check(alternatives.amount(Type.ACCURSED)==0&&alternatives.amount(Type.VULNERABILITY)==1,"mutually exclusive curses");
  check(apply(target(1,"Test",false),DefenceReductionPlan.OFF.with(Type.VULNERABILITY,1)).getDefenceLevel()==193,"Vulnerability remaining fraction floor");
  MonsterDefinition vard=apply(new MonsterDefinition(12223,"Vardorvis","Post-quest",784,0,215,0,500,215,65,85,580,580,580,580,Collections.emptyList(),2,"",0,0),DefenceReductionPlan.OFF.with(Type.DWH,10));
  check(vard.getDefenceLevel()==195&&VardorvisRules.startingDefence(vard)==195,"Vardorvis Defence floor and HP scaling preserved");
  Set<Type> owned=DefenceReductionPlan.available(Arrays.asList(new BankItem(13576,1,"DWH"),new BankItem(27662,1,"Uncharged"),new BankItem(555,5,"Water"),new BankItem(557,5,"Earth"),new BankItem(566,1,"Soul")),66);
  check(owned.equals(EnumSet.of(Type.DWH,Type.VULNERABILITY)),"charged ownership and spell supplies");
  check(!DefenceReductionPlan.available(Arrays.asList(new BankItem(13576,0,"DWH")),99).contains(Type.DWH),"zero quantity absent");
  check(p.revalidate(EnumSet.of(Type.MAUL)).amount(Type.DWH)==0&&p.revalidate(EnumSet.of(Type.MAUL)).amount(Type.MAUL)==1,"clear only missing item");
  check(p.apply(target(1,"Test",false),Collections.emptySet()).getDefenceLevel()==215,"excluded/unusable inputs not applied");
  check(DefenceReductionPlan.OFF.with(Type.DWH,999).amount(Type.DWH)==20,"bounded input");
  check(changed.getHitpoints()==500&&changed.getWeaknessSeverity()==50&&changed.getSlashDefence()==40,"context preserved");
  System.out.println("PASS expanded manual drains: stacking, floors, zero stats, BGS overflow, saved amounts, ownership and exclusion guards");
 }
}
