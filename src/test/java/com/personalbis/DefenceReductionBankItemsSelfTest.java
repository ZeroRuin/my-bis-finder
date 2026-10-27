package com.personalbis;
import java.util.*;
import com.personalbis.DefenceReductionPlan.Type;
public final class DefenceReductionBankItemsSelfTest {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static BankItem item(int id,int quantity){return new BankItem(id,quantity,"Item "+id);}
 private static int amount(List<RecommendedSupply> list,int id){return list.stream().filter(s->s.itemId==id).mapToInt(s->s.quantity).sum();}
 public static void main(String[]args){
  List<BankItem> bank=Arrays.asList(item(13576,1),item(26710,1),item(11804,1),item(555,100),item(557,100),item(566,100),item(21003,0));
  Set<Integer> usable=new HashSet<>(Arrays.asList(13576,26710,11804,21003));
  check(DefenceReductionBankItems.forPlan(DefenceReductionPlan.OFF,bank,usable).isEmpty(),"off means no section items");
  DefenceReductionPlan plan=DefenceReductionPlan.OFF.with(Type.DWH,2).with(Type.BGS,100).with(Type.VULNERABILITY,1);
  List<RecommendedSupply> all=DefenceReductionBankItems.forPlan(plan,bank,usable);
  check(all.size()==5&&amount(all,13576)==1&&amount(all,26710)==0&&amount(all,11804)==1,"one owned variant per selected weapon, not one per hit/damage");
  check(amount(all,555)==5&&amount(all,557)==5&&amount(all,566)==1,"one standard Vulnerability cast");
  check(DefenceReductionBankItems.forPlan(plan,Collections.emptyList(),usable).isEmpty(),"no invented withdrawable inventory/equipped or unknown bank items");
  check(DefenceReductionBankItems.forPlan(DefenceReductionPlan.OFF.with(Type.MAUL,1),bank,usable).isEmpty(),"zero-quantity placeholder omitted");
  check(DefenceReductionBankItems.forPlan(DefenceReductionPlan.OFF.with(Type.DWH,1),bank,Collections.emptySet()).isEmpty(),"unusable/excluded weapon omitted");
  List<RecommendedSupply> few=DefenceReductionBankItems.forPlan(DefenceReductionPlan.OFF.with(Type.VULNERABILITY,1),Arrays.asList(item(555,2),item(557,0)),usable);
  check(few.size()==1&&amount(few,555)==2,"quantities capped to bank; runes already carried need no invented stack");
  MonsterDefinition base=new MonsterDefinition(1,"Test",100,200,100,100,500);
  check(DefenceReductionPlan.appliedTo(base).encode().equals("2"),"no selection inferred from target");
  MonsterDefinition changed=plan.apply(base,EnumSet.of(Type.DWH));
  DefenceReductionPlan applied=DefenceReductionPlan.appliedTo(changed);
  check(applied.amount(Type.DWH)==2&&applied.amount(Type.BGS)==0&&applied.amount(Type.VULNERABILITY)==0,"only eligible explicitly applied effects reach bank section");
  check(DefenceReductionBankItems.forPlan(applied,bank,usable).size()==1,"ineligible selected items absent");
  try{all.clear();throw new AssertionError("mutable result");}catch(UnsupportedOperationException expected){}
  System.out.println("PASS defence reduction bank suggestions: applied selection, usable ownership, exact variants, placeholders, reset/off and one-cast rune quantities");
 }
}
