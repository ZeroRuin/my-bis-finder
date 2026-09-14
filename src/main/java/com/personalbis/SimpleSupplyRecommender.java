package com.personalbis;
import java.util.*;

/**
 * Deliberately simple inventory preparation: only recommends supplies that are
 * actually owned. Quantities are capped to the bank snapshot so the bank UI
 * never asks the player to withdraw more than they have.
 */
public final class SimpleSupplyRecommender {
 private static final int FOOD_QTY=12, POTION_QTY=1, PRAYER_QTY=2;
 private SimpleSupplyRecommender(){}

 public static List<RecommendedSupply> recommend(AttackStyle style,OptimizedMagicLoadout magic,List<BankItem> bank){
  return recommend(style,magic,null,bank);
 }
 public static List<RecommendedSupply> recommend(AttackStyle style,OptimizedMagicLoadout magic,OptimizedRangedLoadout ranged,List<BankItem> bank,MonsterDefinition target){
  return recommend(style,magic,ranged,bank,target,99);
 }
 public static List<RecommendedSupply> recommend(AttackStyle style,OptimizedMagicLoadout magic,OptimizedRangedLoadout ranged,List<BankItem> bank,MonsterDefinition target,int prayerLevel){
  List<RecommendedSupply> out=recommend(style,magic,ranged,bank);
  out.removeIf(s -> s.reason.contains("boost") || s.reason.contains("Prayer restore"));
  addBestCombatBoost(out,bank,style);
  addBestPrayerRestore(out,bank,prayerLevel);
  if(target!=null){
   String n=target.getName().toLowerCase(Locale.ROOT);
   boolean venom=n.contains("vorkath")||n.contains("zulrah")||n.contains("snakeling")
    ||n.contains("araxxor")||n.contains("araxyte")||n.contains("acid blood")
    ||n.contains("cursed baboon")||n.contains("manticore")||n.contains("ungael")||n.contains("venom");
   boolean poison=venom||n.contains("poison")||n.contains("snake")||n.contains("spider")||n.contains("scorpion")||n.contains("tribesman")||n.contains("jungle horror");
   boolean disease=n.contains("disease")||n.contains("broodoo")||n.contains("zogre")||n.contains("skogre");
   if(venom&&!addBestFamily(out,bank,1,"Strongest owned venom protection","Anti-venom+","Anti-venom"))
    addBestFamily(out,bank,1,"Owned fallback venom/poison cure","Sanfew serum","Antidote++","Antidote+","Superantipoison","Antipoison");
   else if(poison)addBestFamily(out,bank,1,"Strongest owned poison cure","Sanfew serum","Antidote++","Antidote+","Superantipoison","Antipoison");
   if(disease)addBestFamily(out,bank,1,"Strongest owned disease cure","Sanfew serum","Relicym's balm");
   if(target.hasAttribute("dragon")||n.contains("dragon")||n.contains("vorkath")||n.contains("wyvern"))
    addBestFamily(out,bank,1,"Strongest owned dragonfire protection","Extended super antifire","Super antifire","Extended antifire","Antifire potion");
   String[] finishing=SlayerTargetRequirements.finishingItems(target);
   if(finishing.length>0)addBestFamily(out,bank,1,"Owned Slayer finishing/luring item",finishing);
   if(SlayerTargetRequirements.slayerRingTeleportUseful(target))addBestSlayerRing(out,bank);
  }
  return out;
 }

 public static List<RecommendedSupply> recommend(AttackStyle style,OptimizedMagicLoadout magic,OptimizedRangedLoadout ranged,List<BankItem> bank){
  List<RecommendedSupply> out=new ArrayList<>();
  if(style==AttackStyle.MAGIC&&magic!=null&&magic.getSpell()!=null){
   String weapon="";EquipmentCandidate w=magic.getItems().get(EquipmentSlot.WEAPON);if(w!=null)weapon=w.getItem().getName().toLowerCase();
   if(PoweredStaff.forWeapon(weapon)==null){
    for(Map.Entry<String,Integer>e:magic.getSpell().runes.entrySet()){
     String rune=e.getKey().toLowerCase();
     if(provides(weapon,rune)||MagicTomeEffects.providesRune(magic.getItems().values(),rune))continue;
     addFullStack(out,bank,e.getKey(),"Full owned stack required by "+magic.getSpell().name);
    }
   }
  }

  if(style==AttackStyle.RANGED&&ranged!=null){
   EquipmentCandidate weapon=ranged.getItems().get(EquipmentSlot.WEAPON);
   if(RangedAmmoRules.requiresEquippedAmmo(weapon)){
    EquipmentCandidate ammo=ranged.getItems().get(EquipmentSlot.AMMO);
    if(ammo!=null)addFullStackById(out,bank,ammo.getItem().getItemId(),
      "Full owned stack required by "+weapon.getItem().getName());
   }
   else if(RangedAmmoRules.usesLoadedDarts(weapon)&&ranged.getLoadedAmmo()!=null)
    addFullStackById(out,bank,ranged.getLoadedAmmo().getItem().getItemId(),
      "Full owned stack loaded into "+weapon.getItem().getName());
  }

  if(style==AttackStyle.RANGED)
   addBestPotion(out,bank,POTION_QTY,"Best owned Ranged boost",new String[][]{
    {"Ranging potion(4)","Ranging potion(3)","Ranging potion(2)","Ranging potion(1)"}
   });
  else if(style==AttackStyle.MAGIC)
   addBestPotion(out,bank,POTION_QTY,"Best owned Magic boost",new String[][]{
    {"Magic potion(4)","Magic potion(3)","Magic potion(2)","Magic potion(1)"}
   });
  else {
   boolean superCombat=addBestPotion(out,bank,POTION_QTY,"Best owned Melee boost",new String[][]{{"Super combat potion(4)","Super combat potion(3)","Super combat potion(2)","Super combat potion(1)"}});
   if(!superCombat){
    addBestPotion(out,bank,POTION_QTY,"Owned Attack boost",new String[][]{{"Super attack(4)","Super attack(3)","Super attack(2)","Super attack(1)","Attack potion(4)","Attack potion(3)","Attack potion(2)","Attack potion(1)"}});
    addBestPotion(out,bank,POTION_QTY,"Owned Strength boost",new String[][]{{"Super strength(4)","Super strength(3)","Super strength(2)","Super strength(1)","Strength potion(4)","Strength potion(3)","Strength potion(2)","Strength potion(1)"}});
    addBestPotion(out,bank,POTION_QTY,"Owned Defence boost",new String[][]{{"Super defence(4)","Super defence(3)","Super defence(2)","Super defence(1)","Defence potion(4)","Defence potion(3)","Defence potion(2)","Defence potion(1)"}});
   }
  }

  addBestPotion(out,bank,PRAYER_QTY,"Best owned Prayer restore",new String[][]{
   {"Super restore(4)","Super restore(3)","Super restore(2)","Super restore(1)","Sanfew serum(4)","Sanfew serum(3)","Sanfew serum(2)","Sanfew serum(1)","Prayer potion(4)","Prayer potion(3)","Prayer potion(2)","Prayer potion(1)"}
  });

  // Healing groups are independent so combat/prayer potions cannot displace them.
  // Family matching also keeps alternate dose names usable and ignores placeholders.
  addBestFamily(out,bank,FOOD_QTY,"Strongest owned normal food",
   "Anglerfish","Dark crab","Manta ray","Shark","Sea turtle","Pineapple pizza",
   "Monkfish","Swordfish","Lobster");
  addBestFamily(out,bank,POTION_QTY,"Owned Saradomin brew healing",
   "Saradomin brew");
  addBestFamily(out,bank,FOOD_QTY,"Strongest owned combo food",
   "Cooked karambwan");
  return out;
 }

 static boolean provides(String w,String r){
  if(w.equals("kodai wand")&&r.equals("water rune"))return true;
  if(w.contains("staff")||w.contains("battlestaff")){
   if(r.equals("air rune")&&(w.contains("air")||w.contains("smoke")||w.contains("dust")||w.contains("mist")))return true;
   if(r.equals("water rune")&&(w.contains("water")||w.contains("steam")||w.contains("mud")||w.contains("mist")))return true;
   if(r.equals("earth rune")&&(w.contains("earth")||w.contains("lava")||w.contains("mud")||w.contains("dust")))return true;
   if(r.equals("fire rune")&&(w.contains("fire")||w.contains("lava")||w.contains("smoke")||w.contains("steam")))return true;
  }return false;
 }

 static void addFullStackById(List<RecommendedSupply>o,List<BankItem>b,int id,String reason){
  if(b!=null)for(BankItem x:b)if(x!=null&&x.getQuantity()>0&&x.getItemId()==id){
   o.add(new RecommendedSupply(x.getItemId(),x.getQuantity(),x.getName(),reason));return;
  }
 }
 static void addFullStack(List<RecommendedSupply>o,List<BankItem>b,String n,String reason){
  BankItem x=find(b,n);if(x!=null)o.add(new RecommendedSupply(x.getItemId(),x.getQuantity(),x.getName(),reason));
 }
 static void addNamed(List<RecommendedSupply>o,List<BankItem>b,String n,int q,String reason){
  BankItem x=find(b,n);if(x!=null)o.add(new RecommendedSupply(x.getItemId(),Math.min(q,x.getQuantity()),x.getName(),reason));
 }
 static void addFirst(List<RecommendedSupply>o,List<BankItem>b,int q,String reason,String...ns){
  for(String n:ns){BankItem x=find(b,n);if(x!=null){o.add(new RecommendedSupply(x.getItemId(),Math.min(q,x.getQuantity()),x.getName(),reason));return;}}
 }
 static boolean addBestPotion(List<RecommendedSupply>o,List<BankItem>b,int q,String reason,String[][]groups){
  for(String[] group:groups){
   for(String n:group){BankItem x=find(b,n);if(x!=null){o.add(new RecommendedSupply(x.getItemId(),Math.min(q,x.getQuantity()),x.getName(),reason));return true;}}
  }
  return false;
 }

 static void addBestCombatBoost(List<RecommendedSupply> out,List<BankItem> bank,AttackStyle style){
  if(style==AttackStyle.MAGIC){
   addBestFamily(out,bank,1,"Strongest owned Magic boost","Saturated heart","Forgotten brew","Imbued heart","Ancient brew","Divine magic potion","Magic potion");
   return;
  }
  if(style==AttackStyle.RANGED){
   addBestFamily(out,bank,1,"Strongest owned Ranged boost","Divine bastion potion","Divine ranging potion","Bastion potion","Ranging potion");
   return;
  }
  if(addBestFamily(out,bank,1,"Strongest owned Melee boost","Divine super combat potion","Super combat potion"))return;
  addBestFamily(out,bank,1,"Owned Attack boost","Super attack","Attack potion","Ruby harvest");
  addBestFamily(out,bank,1,"Owned Strength boost","Super strength","Strength potion","Black warlock");
  addBestFamily(out,bank,1,"Owned Defence boost","Super defence","Defence potion","Sapphire glacialis");
 }

 static void addBestPrayerRestore(List<RecommendedSupply> out,List<BankItem> bank,int prayerLevel){
  BankItem best=null;int bestScore=-1;
  if(bank!=null)for(BankItem item:bank){
   if(item==null||item.getQuantity()<=0)continue;
   String n=item.getName().toLowerCase(Locale.ROOT);int doses=doses(n);int perDose=0;
   if(n.startsWith("super restore(")||n.startsWith("sanfew serum(")||n.startsWith("blighted super restore("))perDose=(Math.max(1,prayerLevel)/4)+8;
   else if(n.startsWith("prayer potion("))perDose=(Math.max(1,prayerLevel)/4)+7;
   else if(n.startsWith("moonlight moth mix("))perDose=22;
   else if(n.equals("moonlight moth")||n.equals("moonlight moth jar")) {perDose=22;doses=1;}
   else if(n.startsWith("sunlight moth mix("))perDose=8;
   else if(n.equals("sunlight moth")||n.equals("sunlight moth jar")) {perDose=8;doses=1;}
   if(perDose<=0)continue;
   int score=perDose*Math.max(1,doses);
   if(score>bestScore){best=item;bestScore=score;}
  }
  if(best!=null)out.add(new RecommendedSupply(best.getItemId(),Math.min(PRAYER_QTY,best.getQuantity()),best.getName(),"Highest-total owned Prayer restore"));
 }

 static int doses(String name){
  int close=name.lastIndexOf(')'),open=name.lastIndexOf('(');
  if(open>=0&&close==name.length()-1)try{return Integer.parseInt(name.substring(open+1,close));}catch(NumberFormatException ignored){}
  return 1;
 }
 static boolean addFirstFound(List<RecommendedSupply>o,List<BankItem>b,int q,String reason,String...ns){
  for(String n:ns){BankItem x=find(b,n);if(x!=null){o.add(new RecommendedSupply(x.getItemId(),Math.min(q,x.getQuantity()),x.getName(),reason));return true;}}return false;
 }
 static boolean addBestFamily(List<RecommendedSupply> out,List<BankItem> bank,int quantity,String reason,String...families){
  if(bank==null)return false;
  for(String family:families){
   String wanted=normaliseFamily(family);BankItem best=null;int bestDose=-1;
   for(BankItem item:bank){
    if(item==null||item.getQuantity()<=0)continue;
    String actual=normaliseFamily(item.getName());
    String idFamily=familyForId(item.getItemId());
    boolean chargedFungicide=wanted.equals("fungicidespray")&&actual.startsWith(wanted);
    if(!actual.equals(wanted)&&!idFamily.equals(wanted)&&!chargedFungicide)continue;
    int dose=doses(item.getName().toLowerCase(Locale.ROOT));
    if(best==null||dose>bestDose){best=item;bestDose=dose;}
   }
   if(best!=null){
    for(RecommendedSupply existing:out)if(existing.itemId==best.getItemId())return true;
    out.add(new RecommendedSupply(best.getItemId(),Math.min(quantity,best.getQuantity()),best.getName(),reason));return true;
   }
  }
  return false;
 }
 static boolean addBestSlayerRing(List<RecommendedSupply> out,List<BankItem> bank){
  if(bank==null)return false;
  int[] preferred={21268,11866,11867,11868,11869,11870,11871,11872,11873};
  for(int id:preferred)for(BankItem item:bank)if(item!=null&&item.getQuantity()>0&&item.getItemId()==id){
   out.add(new RecommendedSupply(item.getItemId(),1,item.getName(),"Owned Slayer-ring teleport"));
   return true;
  }
  // Name fallback protects against future cache aliases while retaining eternal-first ordering.
  if(addBestFamily(out,bank,1,"Owned Slayer-ring teleport","Slayer ring (eternal)"))return true;
  BankItem best=null;int charges=-1;
  for(BankItem item:bank)if(item!=null&&item.getQuantity()>0&&item.getName()!=null
      &&item.getName().toLowerCase(Locale.ROOT).startsWith("slayer ring")){
   int c=doses(item.getName().toLowerCase(Locale.ROOT));if(best==null||c>charges){best=item;charges=c;}
  }
  if(best!=null){out.add(new RecommendedSupply(best.getItemId(),1,best.getName(),"Owned Slayer-ring teleport"));return true;}
  return false;
 }
 static String normaliseFamily(String name){
  if(name==null)return "";
  return name.toLowerCase(Locale.ROOT).replaceFirst("\\([1-4]\\)$","").replaceAll("[^a-z0-9+]","");
 }
 static String familyForId(int id){
  switch(id){
   case 12695:case 12697:case 12699:case 12701:return normaliseFamily("Super combat potion");
   case 2436:case 145:case 147:case 149:return normaliseFamily("Super attack");
   case 2440:case 157:case 159:case 161:return normaliseFamily("Super strength");
   case 2442:case 163:case 165:case 167:return normaliseFamily("Super defence");
   case 2428:case 121:case 123:case 125:return normaliseFamily("Attack potion");
   case 113:case 115:case 117:case 119:return normaliseFamily("Strength potion");
   case 2432:case 133:case 135:case 137:return normaliseFamily("Defence potion");
   default:return "";
  }
 }
 static BankItem find(List<BankItem>b,String n){if(b!=null)for(BankItem x:b)if(x!=null&&x.getQuantity()>0&&x.getName().equalsIgnoreCase(n))return x;return null;}
}
