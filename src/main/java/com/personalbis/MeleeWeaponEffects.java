package com.personalbis;

public final class MeleeWeaponEffects {
 private MeleeWeaponEffects(){}

 public static double accuracyMultiplier(EquipmentCandidate w, MonsterDefinition m, AttackStyle s){
  if(w==null||m==null)return 1.0; String n=name(w);
  if(m.hasAttribute("demon")){
   if(n.contains("arclight")||n.contains("emberlight"))return DemonbaneEffects.multiplier(m,70);
   if(n.contains("silverlight")||n.contains("darklight"))return DemonbaneEffects.multiplier(m,60);
   if(n.contains("bone claws")||n.contains("burning claws"))return DemonbaneEffects.multiplier(m,5);
  }
  if(isDragonHunterLance(w)&&isDraconic(m))return 1.20;
  if(isKerisBreaching(w)&&isScarabTarget(m))return 1.33;
  if(isBarroniteMace(w)&&m.hasAttribute("golem"))return 1.15;
  if(isGraniteHammer(w)&&m.hasAttribute("golem"))return 1.30;
  return 1.0;
 }

 public static double damageMultiplier(EquipmentCandidate w, MonsterDefinition m, AttackStyle s){
  if(w==null||m==null)return 1.0; String n=name(w);
  if(m.hasAttribute("demon")&&(n.contains("arclight")||n.contains("emberlight")))return DemonbaneEffects.multiplier(m,70);
  if(m.hasAttribute("demon")&&(n.contains("silverlight")||n.contains("darklight")))return DemonbaneEffects.multiplier(m,60);
  if(m.hasAttribute("demon")&&(n.contains("bone claws")||n.contains("burning claws")))return DemonbaneEffects.multiplier(m,5);
  if(isDragonHunterLance(w)&&isDraconic(m))return 1.20;
  if(isKeris(w)&&isScarabTarget(m))return n.contains("amascut")?1.15:1.33;
  // Leaf-bladed battleaxe has a target-specific +17.5% damage passive on leafy monsters.
  // Leaf-bladed sword/spear are eligible to damage these monsters but do not receive this bonus.
  if(isLeafBladedBattleaxe(w)&&m.hasAttribute("leafy"))return 1.175;
  // Current golembane affinities: Barronite mace +15%, Granite hammer +30%.
  if(isBarroniteMace(w)&&m.hasAttribute("golem"))return 1.15;
  if(isGraniteHammer(w)&&m.hasAttribute("golem"))return 1.30;
  return 1.0;
 }

 public static double averageDamageMultiplier(EquipmentCandidate w, MonsterDefinition m, AttackStyle s){
  double base=damageMultiplier(w,m,s);
  if(isKeris(w)&&isScarabTarget(m)){
   // 1/51 triple-damage proc: E[multiplier] = 50/51*1 + 1/51*3 = 53/51.
   return base*(53.0/51.0);
  }
  return base;
 }


 public static boolean hasKerisProc(EquipmentCandidate w, MonsterDefinition m){
  return isKeris(w)&&isScarabTarget(m);
 }
 public static int kerisProcMax(int modifiedMax){return modifiedMax*3;}

 /**
  * Expected damage of a successful OSRS hit after the modern zero-to-one
  * conversion. A successful roll is uniform from 0..max inclusive; the rolled
  * zero becomes 1, adding 1/(max+1) to max/2.
  */
 public static double successfulHitAverage(int maxHit){
  if(maxHit<=0)return 0.0;
  return maxHit/2.0 + 1.0/(maxHit+1.0);
 }

 /**
  * Keris: 50/51 ordinary modified hits and 1/51 triple hits. Apply the
  * successful-hit zero conversion to each actual hit distribution separately.
  */
 public static HitDistribution kerisSuccessfulDistribution(int modifiedMax){
  if(modifiedMax<=0)return HitDistribution.single(0,false);
  HitDistribution raw=HitDistribution.linear(1.0,0,modifiedMax);
  HitDistribution ordinary=raw.accurateZeroToOne();
  HitDistribution critical=raw.transform(HitDistribution.multiply(3,1,0)).accurateZeroToOne();
  java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
  for(HitDistribution.WeightedHit wh:ordinary.hits())
   out.add(new HitDistribution.WeightedHit(wh.probability*(50.0/51.0),wh.hitsplats));
  for(HitDistribution.WeightedHit wh:critical.hits())
   out.add(new HitDistribution.WeightedHit(wh.probability*(1.0/51.0),wh.hitsplats));
  return new HitDistribution(out).flatten();
 }
 public static double kerisAverageHit(int modifiedMax){
  return kerisSuccessfulDistribution(modifiedMax).expectedHit();
 }

 /**
  * Current Fang accuracy outside Tombs of Amascut: roll the player's accuracy
  * twice against the same defence roll. This is not equivalent to squaring the
  * ordinary miss chance, which would also effectively re-roll defence.
  */
 public static double fangAccuracy(int attackRoll,int defenceRoll){
  return WikiAccuracyRoll.fang(attackRoll,defenceRoll);
 }

 /**
  * Fang normal damage uses the integer 15% floor as its lower endpoint, then
  * subtracts that same integer from the true max for the upper endpoint:
  *   min = floor(trueMax * 0.15)
  *   max = trueMax - min
  * This preserves the weapon's expected successful-hit damage at trueMax/2
  * and matches the current Wiki calculator (e.g. true max 46 -> 6..40).
  */
 public static int fangMinHit(int trueMax){return Math.max(0,(int)Math.floor(trueMax*0.15));}
 public static int fangDisplayedMaxHit(int trueMax){
  if(trueMax<=0)return 0;
  return trueMax-fangMinHit(trueMax);
 }
 public static double fangAverageHit(int trueMax){
  if(trueMax<=0)return 0.0;
  return (fangMinHit(trueMax)+fangDisplayedMaxHit(trueMax))/2.0;
 }



 public static boolean isRatBoneWeapon(EquipmentCandidate w){
  if(w==null)return false; String n=name(w);
  return n.equals("bone mace")||n.equals("bone shortbow")||n.equals("bone staff");
 }
 public static int ratbaneBonusMax(EquipmentCandidate w,MonsterDefinition m){
  return isRatBoneWeapon(w)&&m!=null&&m.hasAttribute("rat")?10:0;
 }
 public static boolean ratBoneCanDamage(EquipmentCandidate w,MonsterDefinition m){
  return !isRatBoneWeapon(w)||(m!=null&&m.hasAttribute("rat"));
 }
 public static boolean isColossalBlade(EquipmentCandidate w){return w!=null&&name(w).contains("colossal blade");}
 public static int colossalBladeBonusMax(EquipmentCandidate w,MonsterDefinition m){
  if(!isColossalBlade(w)||m==null)return 0;
  return 2*Math.min(5,Math.max(1,m.getSize()));
 }
 public static boolean isBarroniteMace(EquipmentCandidate w){return w!=null&&name(w).contains("barronite mace");}
 public static boolean isGraniteHammer(EquipmentCandidate w){return w!=null&&name(w).contains("granite hammer");}
 public static boolean isLeafBladedBattleaxe(EquipmentCandidate w){
  return w!=null&&name(w).contains("leaf-bladed battleaxe");
 }
 public static boolean isObsidianMeleeWeapon(EquipmentCandidate w){
  if(w==null)return false; String n=name(w);
  return n.contains("toktz-xil-ak") || n.contains("toktz-xil-ek")
      || n.contains("tzhaar-ket-em") || n.contains("tzhaar-ket-om");
 }
 public static boolean isFang(EquipmentCandidate w){return w!=null&&name(w).contains("osmumten")&&name(w).contains("fang");}
 public static boolean isDragonHunterLance(EquipmentCandidate w){return w!=null&&name(w).contains("dragon hunter lance");}
 public static boolean isKeris(EquipmentCandidate w){return w!=null&&name(w).contains("keris");}
 public static boolean isKerisBreaching(EquipmentCandidate w){return isKeris(w)&&name(w).contains("breaching");}
 public static boolean isScythe(EquipmentCandidate w){return w!=null&&name(w).contains("scythe of vitur");}

 /** alpha50.4.46: normal two-hitsplat melee families. */
 public static boolean isDualMacuahuitl(EquipmentCandidate w){return w!=null&&name(w).contains("dual macuahuitl");}
 public static boolean isIndependentTwoHit(EquipmentCandidate w){
  if(w==null)return false; String n=name(w);
  return n.contains("torag\'s hammers")||n.contains("sulphur blades")||n.contains("glacial temotli")||n.contains("earthbound tecpatl");
 }
 public static boolean isSplitTwoHit(EquipmentCandidate w){return isDualMacuahuitl(w)||isIndependentTwoHit(w);}
 public static int splitFirstMax(int combinedMax){return Math.max(0,combinedMax/2);}
 public static int splitSecondMax(int combinedMax){return Math.max(0,combinedMax-splitFirstMax(combinedMax));}
 /**
  * Torag/Sulphur/Temotli roll both half-hits independently. Accuracy is applied
  * outside this helper once to the sum of the two successful-hit expectations.
  */
 public static double independentTwoHitSuccessfulAverage(int combinedMax){
  // Shared HitDistribution path: both splats are successful here (accuracy=1),
  // then modern accurate-zero conversion is applied independently per splat.
  HitDistribution a=HitDistribution.linear(1.0,0,splitFirstMax(combinedMax)).accurateZeroToOne();
  HitDistribution b=HitDistribution.linear(1.0,0,splitSecondMax(combinedMax)).accurateZeroToOne();
  return a.zip(b).expectedHit();
 }
 public static double independentTwoHitExpectedDamage(int combinedMax,double accuracy){
  return independentTwoHitDistribution(combinedMax,accuracy).expectedHit();
 }
 /**
  * Dual macuahuitl is sequential: the second distribution exists only on an
  * accurate first splat. This mirrors Wiki HitDist rather than reducing the
  * mechanic algebraically to P*A + P^2*B.
  */
 public static double dualMacuahuitlExpectedDamage(int combinedMax,double accuracy){
  return dualMacuahuitlDistribution(combinedMax,accuracy).expectedHit();
 }
 public static int scytheHitCount(EquipmentCandidate w,MonsterDefinition m){
  if(!isScythe(w)||m==null)return 1;
  return m.getSize()>=3?3:(m.getSize()==2?2:1);
 }
 public static int scytheSecondMax(int baseMax){return Math.max(0,baseMax/2);}
 public static int scytheThirdMax(int baseMax){return Math.max(0,baseMax/4);}
 public static int scytheCombinedMax(int baseMax,int hits){
  int total=Math.max(0,baseMax);
  if(hits>=2)total+=scytheSecondMax(baseMax);
  if(hits>=3)total+=scytheThirdMax(baseMax);
  return total;
 }
 public static double scytheAverageHit(int baseMax,int hits){
  return scytheExpectedDamage(baseMax,hits,1.0);
 }
 public static HitDistribution standardDistribution(int maxHit,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  return HitDistribution.linear(a,0,Math.max(0,maxHit)).accurateZeroToOne();
 }
 public static HitDistribution fangDistribution(int trueMax,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  return HitDistribution.linear(a,fangMinHit(trueMax),fangDisplayedMaxHit(trueMax));
 }
 public static HitDistribution kerisDistribution(int modifiedMax,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  HitDistribution raw=HitDistribution.linear(a,0,Math.max(0,modifiedMax));
  HitDistribution ordinary=raw.accurateZeroToOne();
  HitDistribution critical=raw.transform(HitDistribution.multiply(3,1,0),false).accurateZeroToOne();
  java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
  for(HitDistribution.WeightedHit wh:ordinary.hits()) out.add(new HitDistribution.WeightedHit(wh.probability*(50.0/51.0),wh.hitsplats));
  for(HitDistribution.WeightedHit wh:critical.hits()) out.add(new HitDistribution.WeightedHit(wh.probability*(1.0/51.0),wh.hitsplats));
  return new HitDistribution(out).flatten();
 }
 public static HitDistribution independentTwoHitDistribution(int combinedMax,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  return standardDistribution(splitFirstMax(combinedMax),a).zip(standardDistribution(splitSecondMax(combinedMax),a));
 }
 public static HitDistribution dualMacuahuitlDistribution(int combinedMax,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  HitDistribution first=standardDistribution(splitFirstMax(combinedMax),a);
  HitDistribution second=standardDistribution(splitSecondMax(combinedMax),a);
  java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
  for(HitDistribution.WeightedHit wh:first.hits()){
   HitDistribution.Hitsplat h=wh.hitsplats.get(0);
   if(h.accurate){
    for(HitDistribution.WeightedHit sw:second.hits()){
     java.util.List<HitDistribution.Hitsplat> hs=new java.util.ArrayList<>(); hs.add(h); hs.addAll(sw.hitsplats);
     out.add(new HitDistribution.WeightedHit(wh.probability*sw.probability,hs));
    }
   } else out.add(new HitDistribution.WeightedHit(wh.probability,java.util.Arrays.asList(h,new HitDistribution.Hitsplat(0,false))));
  }
  return new HitDistribution(out).flatten();
 }
 public static HitDistribution scytheDistribution(int baseMax,int hits,double accuracy){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  HitDistribution dist=standardDistribution(Math.max(0,baseMax),a);
  if(hits>=2)dist=dist.zip(standardDistribution(scytheSecondMax(baseMax),a));
  if(hits>=3)dist=dist.zip(standardDistribution(scytheThirdMax(baseMax),a));
  return dist;
 }
 public static HitDistribution obsidianDistribution(int baseMax,double accuracy,double multiplier){
  double a=Math.max(0.0,Math.min(1.0,accuracy));
  HitDistribution raw=HitDistribution.linear(a,0,Math.max(0,baseMax));
  return raw.transform(h->HitDistribution.single((int)Math.floor(h.damage*multiplier),h.accurate),false);
 }
 public static double scytheExpectedDamage(int baseMax,int hits,double accuracy){
  return scytheDistribution(baseMax,hits,accuracy).expectedHit();
 }
 private static boolean isDraconic(MonsterDefinition m){return m.hasAttribute("dragon")||m.hasAttribute("draconic");}
 private static boolean isScarabTarget(MonsterDefinition m){
  // Wiki monster data is authoritative; do not infer target families from names.
  return m!=null&&(m.hasAttribute("kalphite")||m.hasAttribute("scabarite")||m.hasAttribute("scarab"));
 }
 private static String name(EquipmentCandidate w){return w.getItem().getName().toLowerCase();}
}
