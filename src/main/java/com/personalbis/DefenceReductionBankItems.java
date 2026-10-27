package com.personalbis;

import java.util.*;

/** Withdrawal suggestions for explicitly applied reductions; never infers attempts or extra casts. */
final class DefenceReductionBankItems {
 private DefenceReductionBankItems(){}
 static List<RecommendedSupply> forPlan(DefenceReductionPlan plan,List<BankItem> bank,Set<Integer> usableWeapons){
  Map<Integer,RecommendedSupply> result=new LinkedHashMap<>();
  for(DefenceReductionPlan.Type type:DefenceReductionPlan.Type.values()){
   if(plan.amount(type)<=0)continue;
   if(type==DefenceReductionPlan.Type.VULNERABILITY){
    addRune(result,bank,555,5,"Water rune");addRune(result,bank,557,5,"Earth rune");addRune(result,bank,566,1,"Soul rune");
   }else{
    for(BankItem item:bank)if(item!=null&&item.getQuantity()>0&&type.matches(item.getItemId())&&usableWeapons.contains(item.getItemId())){
     result.put(item.getItemId(),new RecommendedSupply(item.getItemId(),1,item.getName(),"Selected defence reduction; successful effect assumed pre-applied"));break;
    }
   }
  }
  return Collections.unmodifiableList(new ArrayList<>(result.values()));
 }
 private static void addRune(Map<Integer,RecommendedSupply> result,List<BankItem> bank,int id,int quantity,String name){
  long owned=0;for(BankItem item:bank)if(item!=null&&item.getItemId()==id&&item.getQuantity()>0)owned+=item.getQuantity();
  if(owned>0)result.put(id,new RecommendedSupply(id,(int)Math.min(owned,quantity),name,"Vulnerability: runes for one standard cast; no repeated casts assumed"));
 }
}
