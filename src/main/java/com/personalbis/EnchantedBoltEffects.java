package com.personalbis;
/**
 * PvM expected-value enchanted bolt effects.
 * Accuracy-bypassing procs are modelled separately from procs which require
 * the ordinary hit to pass. Non-damage utility effects are deliberately not
 * converted into invented DPS.
 */
public final class EnchantedBoltEffects {
 private EnchantedBoltEffects(){}
 public static boolean enchanted(EquipmentCandidate a){return a!=null&&a.getItem().getName().toLowerCase().contains("(e)");}
 private static double proc(double base,boolean kandarinHard){return base*(kandarinHard?1.10:1.0);}

 /** Backwards-compatible entry point for tests/callers without weapon context. */
 public static double expectedDamagePerAttack(EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax, double accuracy, boolean kandarinHard){
  return expectedDamagePerAttack(null,ammo,m,rangedLevel,normalMax,accuracy,kandarinHard,false);
 }

 /** Expected PvM damage for one attack, including the ordinary projectile. */
 public static double expectedDamagePerAttack(EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax, double accuracy, boolean kandarinHard){
  return expectedDamagePerAttack(weapon,ammo,m,rangedLevel,normalMax,accuracy,kandarinHard,false);
 }
 public static double expectedDamagePerAttack(EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax, double accuracy, boolean kandarinHard, boolean corpReduced){
  return damageDistribution(weapon,ammo,m,rangedLevel,normalMax,accuracy,kandarinHard,corpReduced).expectedHit();
 }

 /**
  * Wiki-style bolt pipeline. Non-ruby bolt transforms happen before accurate-zero
  * conversion; Corp then divides each hitsplat; Ruby transforms last so its proc
  * retains full damage against Corp.
  */
 public static HitDistribution damageDistribution(EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax, double accuracy, boolean kandarinHard, boolean corpReduced){
  HitDistribution dist=preAccurateZeroDistribution(ammo,m,rangedLevel,normalMax,accuracy,kandarinHard).accurateZeroToOne();
  if(corpReduced) dist=dist.transform(HitDistribution.divide(2,0)).flatten();
  return applyRubyAfterCorp(dist,ammo,m,kandarinHard).flatten();
 }

 /** Wiki bolt stage before modern accurate-zero conversion. Ruby is deliberately excluded. */
 public static HitDistribution preAccurateZeroDistribution(EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax, double accuracy, boolean kandarinHard){
  HitDistribution dist=HitDistribution.linear(Math.max(0.0,Math.min(1.0,accuracy)),0,Math.max(0,normalMax));
  String n=enchanted(ammo)&&m!=null?ammo.getItem().getName().toLowerCase():"";
  if(!n.isEmpty()&&!n.contains("ruby")){
   if(n.contains("opal")) dist=bonusDamage(dist,proc(.05,kandarinHard),(int)Math.floor(rangedLevel/10.0),false);
   else if(n.contains("pearl")) dist=bonusDamage(dist,proc(.06,kandarinHard),(int)Math.floor(rangedLevel/(m.hasAttribute("fiery")?15.0:20.0)),false);
   else if(n.contains("diamond")) dist=independentReplacement(dist,proc(.10,kandarinHard),(int)Math.floor(normalMax*1.15));
   else if(n.contains("dragonstone")&&!dragonstoneBlocked(m)) dist=bonusDamage(dist,proc(.06,kandarinHard),(int)Math.floor(rangedLevel*.20),true);
   else if(n.contains("onyx")&&!m.hasAttribute("undead")) dist=accurateReplacement(dist,proc(.11,kandarinHard),(int)Math.floor(normalMax*1.20));
  }
  return dist.flatten();
 }

 /** Wiki Ruby stage after Corp division: Blood Forfeit remains full damage against Corp. */
 public static HitDistribution applyRubyAfterCorp(HitDistribution dist, EquipmentCandidate ammo, MonsterDefinition m, boolean kandarinHard){
  if(dist==null||m==null||!enchanted(ammo)) return dist;
  String n=ammo.getItem().getName().toLowerCase();
  if(!n.contains("ruby")) return dist;
  double p=proc(.06,kandarinHard);
  int effect=(int)Math.min(100.0,Math.floor(m.getHitpoints()*.20));
  return replaceIndependent(dist,p,effect).flatten();
 }

 private static HitDistribution bonusDamage(HitDistribution base,double chance,int bonus,boolean accurateOnly){
  return base.transform(h->{
   if(accurateOnly&&!h.accurate)return HitDistribution.single(h.damage,false);
   java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
   out.add(new HitDistribution.WeightedHit(chance,java.util.Collections.singletonList(new HitDistribution.Hitsplat(h.damage+bonus,h.accurate))));
   out.add(new HitDistribution.WeightedHit(1.0-chance,java.util.Collections.singletonList(new HitDistribution.Hitsplat(h.damage,h.accurate))));
   return new HitDistribution(out);
  });
 }
 private static HitDistribution independentReplacement(HitDistribution base,double chance,int effectMax){
  HitDistribution effect=HitDistribution.linear(1.0,0,effectMax);
  return branchReplacement(base,chance,effect,false);
 }
 private static HitDistribution accurateReplacement(HitDistribution base,double chance,int effectMax){
  HitDistribution effect=HitDistribution.linear(1.0,0,effectMax);
  return branchReplacement(base,chance,effect,true);
 }
 private static HitDistribution branchReplacement(HitDistribution base,double chance,HitDistribution effect,boolean accurateOnly){
  java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
  for(HitDistribution.WeightedHit wh:base.hits()){
   HitDistribution.Hitsplat h=wh.hitsplats.get(0);
   if(accurateOnly&&!h.accurate){out.add(wh);continue;}
   for(HitDistribution.WeightedHit ew:effect.hits())
    out.add(new HitDistribution.WeightedHit(wh.probability*chance*ew.probability,ew.hitsplats));
   out.add(new HitDistribution.WeightedHit(wh.probability*(1.0-chance),wh.hitsplats));
  }
  return new HitDistribution(out).flatten();
 }
 private static HitDistribution replaceIndependent(HitDistribution base,double chance,int damage){
  java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
  for(HitDistribution.WeightedHit wh:base.hits()){
   out.add(new HitDistribution.WeightedHit(wh.probability*chance,java.util.Collections.singletonList(new HitDistribution.Hitsplat(damage,true))));
   out.add(new HitDistribution.WeightedHit(wh.probability*(1.0-chance),wh.hitsplats));
  }
  return new HitDistribution(out).flatten();
 }
 /** Highest projectile hit used by the currently validated proc models.
  * Returns the ordinary max when the effect does not replace/increase the hit
  * in a way we have explicitly validated.  This is diagnostic only; EV ranking
  * continues to use expectedDamagePerAttack().
  */
 public static int validatedProcMaxHit(EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, int normalMax){
  if(!enchanted(ammo)||m==null)return normalMax;
  String n=ammo.getItem().getName().toLowerCase();
  if(n.contains("diamond"))return (int)Math.floor(normalMax*1.15);
  if(n.contains("ruby"))return (int)Math.min(100.0,Math.floor(m.getHitpoints()*.20));
  if(n.contains("opal"))return normalMax+(int)Math.floor(rangedLevel/10.0);
  if(n.contains("pearl"))return normalMax+(int)Math.floor(rangedLevel/(m.hasAttribute("fiery")?15.0:20.0));
  if(n.contains("dragonstone")&&!dragonstoneBlocked(m))return normalMax+(int)Math.floor(rangedLevel*.20);
  if(n.contains("onyx")&&!m.hasAttribute("undead"))return (int)Math.floor(normalMax*1.20);
  return normalMax;
 }

 public static boolean damageEffectModelled(EquipmentCandidate ammo){
  if(!enchanted(ammo))return false;
  String n=ammo.getItem().getName().toLowerCase();
  return n.contains("opal")||n.contains("pearl")||n.contains("ruby")||n.contains("diamond")||n.contains("dragonstone")||n.contains("onyx");
 }
 /** Target-aware diagnostic label used by the DEV bolt harness. */
 public static String validationLabel(EquipmentCandidate ammo, MonsterDefinition m, int rangedLevel, boolean kandarinHard){
  if(!enchanted(ammo))return "";
  String n=ammo.getItem().getName().toLowerCase();
  if(n.contains("opal"))return String.format("Lucky Lightning | proc %.2f%% independent accuracy | +%d damage",proc(.05,kandarinHard)*100.0,(int)Math.floor(rangedLevel/10.0));
  if(n.contains("jade"))return String.format("Earth's Fury | proc %.2f%% | utility, +0 direct DPS",proc(.05,kandarinHard)*100.0);
  if(n.contains("pearl"))return String.format("Sea Curse%s | proc %.2f%% independent accuracy | +%d damage",m!=null&&m.hasAttribute("fiery")?" (fiery)":"",proc(.06,kandarinHard)*100.0,(int)Math.floor(rangedLevel/(m!=null&&m.hasAttribute("fiery")?15.0:20.0)));
  if(n.contains("topaz"))return "Down to Earth | PvP only | +0 PvM DPS";
  if(n.contains("sapphire"))return String.format("Clear Mind | proc %.2f%% | utility, +0 direct DPS",proc(.05,kandarinHard)*100.0);
  if(n.contains("emerald"))return String.format("Magical Poison | proc %.2f%% after hit | poison DoT not ranked",proc(.55,kandarinHard)*100.0);
  if(n.contains("ruby"))return String.format("Blood Forfeit | proc %.2f%% independent accuracy | replaces hit",proc(.06,kandarinHard)*100.0);
  if(n.contains("diamond"))return String.format("Armour Piercing | proc %.2f%% independent accuracy | +15%% max",proc(.10,kandarinHard)*100.0);
  if(n.contains("onyx")){
   if(m!=null&&m.hasAttribute("undead"))return "Life Leech BLOCKED (undead) | proc 0.00%";
   return String.format("Life Leech | proc %.2f%% after hit | +20%% hit damage",proc(.11,kandarinHard)*100.0);
  }
  if(n.contains("dragonstone")){
   if(dragonstoneBlocked(m))return "Dragon's Breath BLOCKED (dragonfire immune) | proc 0.00%";
   return String.format("Dragon's Breath | proc %.2f%% after hit | +%d damage",proc(.06,kandarinHard)*100.0,(int)Math.floor(0.20*rangedLevel));
  }
  return effectLabel(ammo);
 }

 public static String validationLabel(EquipmentCandidate ammo, MonsterDefinition m, boolean kandarinHard){ return validationLabel(ammo,m,99,kandarinHard); }

 private static boolean dragonstoneBlocked(MonsterDefinition m){
  return m!=null&&(m.hasAttribute("dragonfire immune")||m.hasAttribute("dragon")||m.hasAttribute("fiery"));
 }

 public static String effectLabel(EquipmentCandidate ammo){
  if(!enchanted(ammo))return "";
  String n=ammo.getItem().getName().toLowerCase();
  if(n.contains("opal"))return "Lucky Lightning EV";
  if(n.contains("pearl"))return "Sea Curse EV";
  if(n.contains("ruby"))return "Blood Forfeit EV";
  if(n.contains("diamond"))return "Armour Piercing EV";
  if(n.contains("onyx"))return "Life Leech EV";
  if(n.contains("dragonstone"))return "Dragon's Breath EV";
  if(n.contains("jade"))return "Earth's Fury (utility; no DPS EV)";
  if(n.contains("topaz"))return "Down to Earth (PvP only)";
  if(n.contains("sapphire"))return "Clear Mind (utility; no DPS EV)";
  if(n.contains("emerald"))return "Magical Poison (DoT not ranked yet)";
  return "";
 }
}
