package com.personalbis;
import java.util.*;
import java.util.stream.IntStream;
import javax.inject.Inject;
import net.runelite.api.Skill;

public class RangedLoadoutOptimizer {
 // alpha50.4.50.80: accuracy-first rollback. The narrower 6/48/8 search was
 // fast, but real banks showed it could discard complete Crystal/Bowfa and
 // crossbow branches. Retain the safe caches/parallelism while restoring broad
 // supporting-gear coverage and exact final comparison.
 private static final int SLOT=12, BEAM=128, EXACT_FINALISTS=48;
 private final EquipmentIntelligence intel; private final RangedCombatCalculator calc; private final AccountSnapshot account;
 private volatile String lastProfileSummary="";
 private final ThreadLocal<Profile> activeProfile=new ThreadLocal<>();
 private static final class Profile {
  long started=System.nanoTime(), trimNanos, finalNanos, completionNanos;
  int weapons, ammoBranches, trimCalls, dpsCalls, fastScoreCalls, statesScored, uniqueStatesScored, dedupedStates, scoreCacheHits, finalStates, completionTrials;
  int slotCandidatesBefore, slotCandidatesAfter, slotCandidatesPruned;
  final Map<String,Double> scoreCache=new HashMap<>();
  final Map<String,RangedCombatResult> exactCache=new HashMap<>();
  final EnumMap<EquipmentSlot,List<EquipmentCandidate>> choiceCache=new EnumMap<>(EquipmentSlot.class);
 }
 private static final class WeaponResult {
  final EquipmentCandidate weapon; final OptimizedRangedLoadout loadout;
  WeaponResult(EquipmentCandidate w,OptimizedRangedLoadout l){weapon=w;loadout=l;}
 }
 public String getLastProfileSummary(){return lastProfileSummary;}
 @Inject public RangedLoadoutOptimizer(EquipmentIntelligence i,RangedCombatCalculator c,AccountSnapshot a){intel=i;calc=c;account=a;}

 public OptimizedRangedLoadout optimize(List<BankItem>b,MonsterDefinition m,boolean task){return optimizeWithReport(b,m,task).getBest();}

 public RangedOptimizationReport optimizeWithReport(List<BankItem>b,MonsterDefinition m,boolean task){
  Map<EquipmentSlot,List<EquipmentCandidate>> ranked=intel.rank(b,AttackStyle.RANGED,task);
  return optimizeWithReportRanked(ranked,m,task);
 }
 public RangedOptimizationReport optimizeWithReportRanked(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,MonsterDefinition m,boolean task){
  Profile profile=new Profile(); activeProfile.set(profile);
  try {
  ranked=SlayerTargetRequirements.constrainRanked(m,ranked);
  EquipmentSlot requiredSlot=SlayerTargetRequirements.requiredEquipmentSlot(m);
  EquipmentCandidate requiredEquipment=requiredSlot==null?null:SlayerTargetRequirements.firstRequiredEquipment(m,ranked.get(requiredSlot));
  // Alpha 50.4 completeness pass: evaluate every owned/equippable ranged
  // weapon. Exact target DPS, ammo compatibility and complete gear decide BiS;
  // raw Equipment Intelligence rank no longer excludes a weapon up front.
  List<EquipmentCandidate> weapons=allUsable(ranked.get(EquipmentSlot.WEAPON));
  profile.weapons=weapons.size();
  List<RangedWeaponComparison> comps=new ArrayList<>(); OptimizedRangedLoadout best=null;
  final Map<EquipmentSlot,List<EquipmentCandidate>> searchRanked=ranked;
  final Profile masterProfile=profile;
  List<WeaponResult> weaponResults=IntStream.range(0,weapons.size()).parallel().mapToObj(i->{
   EquipmentCandidate w=weapons.get(i);
   if(requiredSlot==EquipmentSlot.SHIELD&&RangedAmmoRules.occupiesBothHands(w))return new WeaponResult(w,null);
   Profile branch=new Profile();activeProfile.set(branch);
   try{return new WeaponResult(w,optimizeWeapon(searchRanked,w,m,task,requiredEquipment));}
   finally{activeProfile.remove();mergeProfile(masterProfile,branch);}
  }).collect(java.util.stream.Collectors.toList());
  activeProfile.set(profile);
  for(WeaponResult wr:weaponResults){
   EquipmentCandidate w=wr.weapon;OptimizedRangedLoadout one=wr.loadout;
   if(one==null)continue;
   EquipmentCandidate ammo=one.getLoadedAmmo()!=null?one.getLoadedAmmo():one.getItems().get(EquipmentSlot.AMMO);
   comps.add(new RangedWeaponComparison(w.getItem().getName(),ammo==null?RangedAmmoRules.ammoDisplay(w):ammo.getItem().getName(),RangedAmmoRules.weaponAmmo(w),RangedAmmoRules.defenceClass(w),
    ammo==null?"":EnchantedBoltEffects.effectLabel(ammo),one.getResult(),gearSummary(one.getItems()),one.getItems()));
   if(best==null||FinalLoadoutSelection.better(one.getResult().getDps(),best.getResult().getDps()))best=one;
  }
  comps.sort((a,z)->Double.compare(z.getResult().getDps(),a.getResult().getDps()));
  return new RangedOptimizationReport(best,comps);
  } finally {
   long total=System.nanoTime()-profile.started;
   long other=Math.max(0,total-profile.trimNanos-profile.finalNanos-profile.completionNanos);
   lastProfileSummary="Ranged detail:"+System.lineSeparator()+
    "  Weapons: "+profile.weapons+" | ammo branches: "+profile.ammoBranches+System.lineSeparator()+
    "  Beam trims: "+profile.trimCalls+" | states scored: "+profile.statesScored+System.lineSeparator()+
    "  Unique states: "+profile.uniqueStatesScored+" | deduped: "+profile.dedupedStates+System.lineSeparator()+
    "  DPS calculations: "+profile.dpsCalls+" | fast scores: "+profile.fastScoreCalls+System.lineSeparator()+
    "  Score-cache hits: "+profile.scoreCacheHits+System.lineSeparator()+
    "  Slot candidates: "+profile.slotCandidatesBefore+" -> "+profile.slotCandidatesAfter+" (pruned "+profile.slotCandidatesPruned+")"+System.lineSeparator()+
    "  Beam score/trim: "+(profile.trimNanos/1_000_000L)+" ms"+System.lineSeparator()+
    "  Final beam eval: "+(profile.finalNanos/1_000_000L)+" ms ("+profile.finalStates+" states)"+System.lineSeparator()+
    "  Neutral completion: "+(profile.completionNanos/1_000_000L)+" ms ("+profile.completionTrials+" trials)"+System.lineSeparator()+
    "  Branch/other: "+(other/1_000_000L)+" ms";
   activeProfile.remove();
  }
 }

 private void mergeProfile(Profile into,Profile from){synchronized(into){
  into.trimNanos+=from.trimNanos;into.finalNanos+=from.finalNanos;into.completionNanos+=from.completionNanos;
  into.ammoBranches+=from.ammoBranches;into.trimCalls+=from.trimCalls;into.dpsCalls+=from.dpsCalls;
  into.fastScoreCalls+=from.fastScoreCalls;into.statesScored+=from.statesScored;into.uniqueStatesScored+=from.uniqueStatesScored;
  into.dedupedStates+=from.dedupedStates;into.scoreCacheHits+=from.scoreCacheHits;into.finalStates+=from.finalStates;
  into.completionTrials+=from.completionTrials;into.slotCandidatesBefore+=from.slotCandidatesBefore;
  into.slotCandidatesAfter+=from.slotCandidatesAfter;into.slotCandidatesPruned+=from.slotCandidatesPruned;
 }}

 private OptimizedRangedLoadout optimizeWeapon(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate weapon,MonsterDefinition m,boolean task,EquipmentCandidate requiredEquipment){
  // Alpha 50.4.8: weapon/ammo anchored search.  Ammo is selected before any
  // armour slot so a strong partial Bowfa/crystal branch cannot prune a
  // crossbow's best supporting gear before its bolt effect exists.
  if(RangedAmmoRules.requiresEquippedAmmo(weapon)||RangedAmmoRules.usesLoadedDarts(weapon)){
   OptimizedRangedLoadout best=null;
   List<EquipmentCandidate> ammunition=allUsable(ranked.get(
     RangedAmmoRules.usesLoadedDarts(weapon)?EquipmentSlot.WEAPON:EquipmentSlot.AMMO));
   for(EquipmentCandidate rawAmmo:ammunition){
    EquipmentCandidate ammo=RangedAmmoRules.usesLoadedDarts(weapon)?asLoadedDart(rawAmmo):rawAmmo;
    if(!RangedAmmoRules.compatible(weapon,ammo))continue;
    Profile p=activeProfile.get(); if(p!=null)p.ammoBranches++;
    OptimizedRangedLoadout one=optimizeWeaponAmmo(ranked,weapon,ammo,m,task,requiredEquipment);
    if(one!=null&&(best==null||FinalLoadoutSelection.better(one.getResult().getDps(),best.getResult().getDps())
      ||(Double.compare(one.getResult().getDps(),best.getResult().getDps())==0&&prayerBonus(one.getItems())>prayerBonus(best.getItems()))))best=one;
   }
   return best;
  }
  Profile p=activeProfile.get(); if(p!=null)p.ammoBranches++;
  return optimizeWeaponAmmo(ranked,weapon,null,m,task,requiredEquipment);
 }

 private EquipmentCandidate asLoadedDart(EquipmentCandidate dart){
  return new EquipmentCandidate(dart.getItem(),EquipmentSlot.AMMO,dart.getRangedStrength(),
    0,0,0,dart.getRangedStrength(),0,0f,0,0,false,"Loaded dart",dart.getRequirementResult());
 }

 private OptimizedRangedLoadout optimizeWeaponAmmo(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate weapon,
   EquipmentCandidate anchoredAmmo,MonsterDefinition m,boolean task,EquipmentCandidate requiredEquipment){
  List<Map<EquipmentSlot,EquipmentCandidate>> beam=new ArrayList<>();
  Map<EquipmentSlot,EquipmentCandidate> seed=new EnumMap<>(EquipmentSlot.class);
  seed.put(EquipmentSlot.WEAPON,weapon);
  if(anchoredAmmo!=null)seed.put(EquipmentSlot.AMMO,anchoredAmmo);
  if(requiredEquipment!=null)seed.put(requiredEquipment.getSlot(),requiredEquipment);
  beam.add(seed);
  Map<EquipmentSlot,EquipmentCandidate> karil=karilsBundle(ranked,weapon,anchoredAmmo);
  if(karil!=null){if(requiredEquipment!=null)karil.put(requiredEquipment.getSlot(),requiredEquipment);beam.add(karil);}
  // Crystal armour gains value as a set with Bowfa/Crystal bow. Seed the
  // complete owned set before per-slot beam trimming so individually weaker
  // pieces cannot remove the finished Bowfa branch before exact comparison.
  Map<EquipmentSlot,EquipmentCandidate> crystal=crystalBundle(ranked,weapon,anchoredAmmo);
  if(crystal!=null){if(requiredEquipment!=null)crystal.put(requiredEquipment.getSlot(),requiredEquipment);beam.add(crystal);}
  for(EquipmentSlot slot:EquipmentSlot.values()){
   if(slot==EquipmentSlot.WEAPON||slot==EquipmentSlot.AMMO)continue;
   if(SlayerTargetRequirements.requiredEquipmentSlot(m)==slot)continue;
   List<EquipmentCandidate> choices=choicesFor(ranked,slot);
   List<Map<EquipmentSlot,EquipmentCandidate>> next=new ArrayList<>();
   for(Map<EquipmentSlot,EquipmentCandidate> g:beam){
    next.add(new EnumMap<>(g));
    if(slot==EquipmentSlot.SHIELD&&RangedAmmoRules.occupiesBothHands(weapon))continue;
    for(EquipmentCandidate c:choices){
     Map<EquipmentSlot,EquipmentCandidate> n=new EnumMap<>(g);n.put(slot,c);next.add(n);
    }
   }
   beam=trim(next,m,task); if(beam.isEmpty())return null;
  }
  // Self/internal-ammo weapons may use the ammo slot for a blessing-like item.
  if(anchoredAmmo==null&&RangedAmmoRules.ammoSlotFreeForNeutralGear(weapon)){
   List<Map<EquipmentSlot,EquipmentCandidate>> next=new ArrayList<>();
   for(Map<EquipmentSlot,EquipmentCandidate> g:beam){
    next.add(new EnumMap<>(g));
    List<EquipmentCandidate> ammo=ranked.get(EquipmentSlot.AMMO);
    if(ammo!=null)for(EquipmentCandidate c:ammo){
     if(c.getRequirementResult().isBlocked()||RangedAmmoRules.ammoType(c)!=RangedAmmoType.NONE)continue;
     Map<EquipmentSlot,EquipmentCandidate> n=new EnumMap<>(g);n.put(EquipmentSlot.AMMO,c);next.add(n);
    }
   }
   beam=trim(next,m,task);
  }
  OptimizedRangedLoadout best=null;
  int finalists=Math.min(EXACT_FINALISTS,beam.size());
  Profile prof=activeProfile.get(); long finalStart=System.nanoTime(); if(prof!=null)prof.finalStates+=finalists;
  for(int i=0;i<finalists;i++){Map<EquipmentSlot,EquipmentCandidate>g=beam.get(i);RangedCombatResult r=exactCached(g,m,task);
   if(r!=null&&Double.isFinite(r.getDps())&&(best==null||FinalLoadoutSelection.better(r.getDps(),best.getResult().getDps())
    ||(Double.compare(r.getDps(),best.getResult().getDps())==0&&prayerBonus(g)>prayerBonus(best.getItems()))))best=new OptimizedRangedLoadout(g,r);}
  if(prof!=null)prof.finalNanos+=System.nanoTime()-finalStart;
  long completionStart=System.nanoTime();
  OptimizedRangedLoadout completed=completeNeutralSlots(best,ranked,weapon,m,task);
  if(prof!=null)prof.completionNanos+=System.nanoTime()-completionStart;
  if(completed!=null&&anchoredAmmo!=null&&RangedAmmoRules.usesLoadedDarts(weapon)){
   Map<EquipmentSlot,EquipmentCandidate> displayGear=new EnumMap<>(completed.getItems());
   displayGear.remove(EquipmentSlot.AMMO);
   EquipmentCandidate neutral=null;List<EquipmentCandidate> ammoItems=ranked.get(EquipmentSlot.AMMO);
   if(ammoItems!=null)for(EquipmentCandidate c:ammoItems)
    if(c!=null&&!c.getRequirementResult().isBlocked()&&RangedAmmoRules.ammoType(c)==RangedAmmoType.NONE
      &&(neutral==null||c.getPrayer()>neutral.getPrayer()))neutral=c;
   if(neutral!=null)displayGear.put(EquipmentSlot.AMMO,neutral);
   return new OptimizedRangedLoadout(displayGear,completed.getResult(),anchoredAmmo);
  }
  return completed;
 }

 private OptimizedRangedLoadout completeNeutralSlots(OptimizedRangedLoadout best,
   Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate weapon,MonsterDefinition m,boolean task){
  if(best==null)return null;
  Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(best.getItems());
  RangedCombatResult current=best.getResult();
  for(EquipmentSlot slot:EquipmentSlot.values()){
   if(slot==EquipmentSlot.WEAPON||gear.containsKey(slot))continue;
   if(slot==EquipmentSlot.SHIELD&&RangedAmmoRules.occupiesBothHands(weapon))continue;
   if(slot==EquipmentSlot.AMMO&&RangedAmmoRules.requiresEquippedAmmo(weapon))continue;
   List<EquipmentCandidate> all=ranked.get(slot);if(all==null)continue;
   EquipmentCandidate chosen=null;RangedCombatResult chosenResult=current;
   for(EquipmentCandidate c:all){
    if(c.getRequirementResult().isBlocked())continue;
    // For self/internal-ammo weapons the Ammo slot is free, but actual arrows,
    // bolts and darts are not neutral gear. Only blessing-like/non-ammo items
    // may fill the free slot.
    if(slot==EquipmentSlot.AMMO&&RangedAmmoRules.ammoSlotFreeForNeutralGear(weapon)
      &&RangedAmmoRules.ammoType(c)!=RangedAmmoType.NONE)continue;
    if(slot==EquipmentSlot.AMMO&&RangedAmmoRules.requiresEquippedAmmo(weapon)
      &&!RangedAmmoRules.compatible(weapon,c))continue;
    Map<EquipmentSlot,EquipmentCandidate> trial=new EnumMap<>(gear);trial.put(slot,c);
    Profile prof=activeProfile.get(); if(prof!=null)prof.completionTrials++;
    RangedCombatResult r=exactCached(trial,m,task);
    if(r==null||Double.compare(r.getDps(),current.getDps())<0)continue;
    if(chosen==null||FinalLoadoutSelection.better(r.getDps(),chosenResult.getDps())
      ||(Double.compare(r.getDps(),chosenResult.getDps())==0&&c.getPrayer()>chosen.getPrayer())){
     chosen=c;chosenResult=r;
    }
   }
   if(chosen!=null){gear.put(slot,chosen);current=chosenResult;}
  }
  return new OptimizedRangedLoadout(gear,current);
 }

 private RangedCombatResult exactCached(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
  Profile p=activeProfile.get();StringBuilder key=new StringBuilder();
  for(EquipmentSlot slot:EquipmentSlot.values()){EquipmentCandidate c=gear.get(slot);key.append(c==null?0:c.getItem().getItemId()).append(',');}
  String k=key.toString();if(p!=null){RangedCombatResult hit=p.exactCache.get(k);if(hit!=null)return hit;p.dpsCalls++;}
  RangedCombatResult r=calc.calculate(gear,m,task);if(p!=null)p.exactCache.put(k,r);return r;
 }

 private List<Map<EquipmentSlot,EquipmentCandidate>> trim(List<Map<EquipmentSlot,EquipmentCandidate>> list,MonsterDefinition m,boolean task){
  Profile prof=activeProfile.get(); long started=System.nanoTime();
  if(prof!=null){prof.trimCalls++;prof.statesScored+=list.size();}
  // alpha50.4.16: collapse offensively equivalent partial states before the
  // expensive combat calculation.  The signature retains every identity that
  // can change a later ranged mechanic/set bonus; among equivalent states we
  // keep the higher-Prayer representative.
  LinkedHashMap<String,Map<EquipmentSlot,EquipmentCandidate>> unique=new LinkedHashMap<>();
  for(Map<EquipmentSlot,EquipmentCandidate> g:list){
   String k=combatStateKey(g); Map<EquipmentSlot,EquipmentCandidate> prior=unique.get(k);
   if(prior==null||prayerBonus(g)>prayerBonus(prior))unique.put(k,g);
  }
  if(prof!=null){prof.uniqueStatesScored+=unique.size();prof.dedupedStates+=list.size()-unique.size();}
  // alpha50.4.18: bounded top-K selection.  We only need the best BEAM
  // states, so sorting every unique partial state is wasted work.  Score each
  // state once, keep a min-heap of the best BEAM states, then sort only those for a
  // deterministic best-first beam.  Ranking semantics are unchanged: DPS
  // first, Prayer as the exact tie-break.
  final class ScoredState {
   final Map<EquipmentSlot,EquipmentCandidate> gear; final double score; final int prayer;
   ScoredState(Map<EquipmentSlot,EquipmentCandidate> g,double s){gear=g;score=s;prayer=prayerBonus(g);}
  }
  Comparator<ScoredState> ascending=(a,z)->{int d=Double.compare(a.score,z.score);return d!=0?d:Integer.compare(a.prayer,z.prayer);};
  PriorityQueue<ScoredState> top=new PriorityQueue<>(BEAM,ascending);
  for(Map<EquipmentSlot,EquipmentCandidate> g:unique.values()){
   ScoredState ss=new ScoredState(g,safeDpsCached(g,m,task));
   if(top.size()<BEAM)top.offer(ss);
   else if(ascending.compare(ss,top.peek())>0){top.poll();top.offer(ss);}
  }
  List<ScoredState> winners=new ArrayList<>(top);
  winners.sort(ascending.reversed());
  List<Map<EquipmentSlot,EquipmentCandidate>> result=new ArrayList<>(winners.size());
  for(ScoredState ss:winners)result.add(ss.gear);
  if(prof!=null)prof.trimNanos+=System.nanoTime()-started;
  return result;
 }
 private double safeDpsCached(Map<EquipmentSlot,EquipmentCandidate>g,MonsterDefinition m,boolean task){
  Profile p=activeProfile.get(); String k=combatStateKey(g);
  if(p!=null){Double hit=p.scoreCache.get(k);if(hit!=null){p.scoreCacheHits++;return hit;}}
  if(p!=null)p.fastScoreCalls++; double d=fastDps(g,m,task); if(p!=null)p.scoreCache.put(k,d); return d;
 }
 private double fastDps(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
  int atk=0,str=0,speed=4; boolean slayer=false; EquipmentCandidate weapon=gear.get(EquipmentSlot.WEAPON);
  for(EquipmentCandidate c:gear.values()){if(c==null)continue;
   if(c.getSlot()==EquipmentSlot.AMMO&&!RangedAmmoRules.compatible(weapon,c))continue;
   atk+=c.getRangedAttack();str+=c.getRangedStrength();
   if(c.getSlot()==EquipmentSlot.WEAPON&&c.getAttackSpeed()>0)speed=c.getAttackSpeed();
   String n=c.getItem().getName().toLowerCase();
   if(c.getSlot()==EquipmentSlot.HEAD&&(n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask"))&&(n.contains("(i)")||n.contains("imbued")))slayer=true;
  }
  int level=Math.max(1,account.boosted(Skill.RANGED)); boolean voidRanged=RangedLoadoutEffects.voidRanged(gear),eliteVoid=RangedLoadoutEffects.eliteVoidRanged(gear);
  boolean salve=RangedLoadoutEffects.rangedSalveActive(gear,m); int def=m==null?1:Math.max(1,m.getDefenceLevel()),bonus=RangedAmmoRules.targetDefence(m,weapon); int defRoll=(def+9)*(bonus+64);
  EquipmentCandidate ammo=gear.get(EquipmentSlot.AMMO); double best=-1;
  for(RangedStance st:RangedStance.values())for(RangedPrayer p:fastPrayers()){
   int eff=CombatEffectiveLevels.ranged(level,p.getRanged(),st.getRangedBoost(),voidRanged); int roll=eff*(atk+64);
   roll=(int)Math.floor(roll*RangedLoadoutEffects.crystalAccuracy(gear,weapon));
   roll=WikiRangedEffects.accuracyAfterExclusive(roll,gear.values(),m,task,slayer);
   roll=WikiRangedEffects.postAccuracy(roll,weapon,m,st,4);
   double acc=roll>defRoll?1-(defRoll+2.0)/(2*(roll+1.0)):roll/(2.0*(defRoll+1.0)); acc=Math.max(0,Math.min(1,acc));
   int dmgEff=CombatEffectiveLevels.rangedDamage(level,p.getRangedStrength(),st.getRangedBoost(),voidRanged,eliteVoid);
   int max=(int)Math.floor(0.5+dmgEff*(str+64)/640.0); max=(int)Math.floor(max*RangedLoadoutEffects.crystalDamage(gear,weapon));
   max=WikiRangedEffects.damageAfterExclusive(max,gear.values(),m,weapon,task,slayer);
   max=WikiRangedEffects.postDamage(max,weapon,m,task,slayer); int actualSpeed=Math.max(1,speed+st.getSpeedDelta());
   double expected;
   if(EnchantedBoltEffects.damageEffectModelled(ammo)&&!CorporealBeastRules.isCorp(m))
    expected=fastBoltExpected(ammo,m,level,max,acc,account.isKandarinHardCompleted());
   else if(requiresDistributionBeamScore(weapon,ammo,m))
    expected=WikiPlayerVsNpcCalc.rangedAttacker(weapon,ammo,m,level,max,acc,account.isKandarinHardCompleted(),gear.values()).expectedDamage();
   else
    // Exact expected value of Wiki's ordinary linear 0..max distribution after
    // accurate-zero becomes one. Avoid allocating/flattening a distribution for
    // the tens of thousands of ordinary partial beam states.
    expected=acc*((max*(max+1.0)/2.0+1.0)/(max+1.0));
   double d=expected/(actualSpeed*0.6); if(d>best)best=d;
  } return best;
 }
 private boolean requiresDistributionBeamScore(EquipmentCandidate weapon,EquipmentCandidate ammo,MonsterDefinition monster){
  if(CorporealBeastRules.isCorp(monster))return true;
  String a=ammo==null||ammo.getItem()==null?"":ammo.getItem().getName().toLowerCase();
  if(a.contains("seeking"))return true;
  String w=weapon==null||weapon.getItem()==null?"":weapon.getItem().getName().toLowerCase();
  return w.equals("dark bow")||w.contains("tonalztics")||w.equals("karil's crossbow");
 }
 private double fastBoltExpected(EquipmentCandidate ammo,MonsterDefinition m,int level,int max,double accuracy,boolean diary){
  String n=ammo.getItem().getName().toLowerCase();double pScale=diary?1.10:1.0;
  double ordinary=successfulAverage(max),base=accuracy*ordinary;
  if(n.contains("ruby")){double p=.06*pScale,effect=Math.min(100.0,Math.floor(m.getHitpoints()*.20));return p*effect+(1.0-p)*base;}
  if(n.contains("diamond")){double p=.10*pScale,effect=successfulAverage((int)Math.floor(max*1.15));return p*effect+(1.0-p)*base;}
  if(n.contains("opal")){double p=.05*pScale;return base+p*Math.floor(level/10.0);}
  if(n.contains("pearl")){double p=.06*pScale;return base+p*Math.floor(level/(m.hasAttribute("fiery")?15.0:20.0));}
  if(n.contains("dragonstone")&&!dragonstoneBlocked(m)){double p=.06*pScale;return base+accuracy*p*Math.floor(level*.20);}
  if(n.contains("onyx")&&!m.hasAttribute("undead")){double p=.11*pScale,effect=successfulAverage((int)Math.floor(max*1.20));return accuracy*((1.0-p)*ordinary+p*effect);}
  return base;
 }
 private double successfulAverage(int max){return (max*(max+1.0)/2.0+1.0)/(max+1.0);}
 private boolean dragonstoneBlocked(MonsterDefinition m){return m!=null&&(m.hasAttribute("dragonfire immune")||m.hasAttribute("dragon")||m.hasAttribute("fiery"));}
 private RangedPrayer[] fastPrayers(){int x=account.real(Skill.PRAYER);
  // DPS is the sole search objective.  A stronger unlocked offensive prayer is
  // monotonic in both accuracy and max hit, so evaluating NONE as well cannot
  // change the winning gear state and merely doubles every beam-score pass.
  if(x>=74&&account.isRigourUnlocked())return new RangedPrayer[]{RangedPrayer.RIGOUR};
  if(x>=62&&account.isDeadeyeUnlocked())return new RangedPrayer[]{RangedPrayer.DEADEYE};
  if(x>=44)return new RangedPrayer[]{RangedPrayer.EAGLE_EYE}; if(x>=26)return new RangedPrayer[]{RangedPrayer.HAWK_EYE};
  if(x>=8)return new RangedPrayer[]{RangedPrayer.SHARP_EYE}; return new RangedPrayer[]{RangedPrayer.NONE};
 }

 private String combatStateKey(Map<EquipmentSlot,EquipmentCandidate> g){
  int atk=0,str=0; StringBuilder flags=new StringBuilder();
  for(EquipmentCandidate c:g.values()){if(c==null)continue;atk+=c.getRangedAttack();str+=c.getRangedStrength();}
  flags.append(atk).append('/').append(str);
  // Weapon/ammo identity can change speed, projectile class and special effects.
  appendIdentity(flags,g.get(EquipmentSlot.WEAPON)); appendIdentity(flags,g.get(EquipmentSlot.AMMO));
  // Preserve slot-specific identities only where current or future set/mechanic
  // completion can alter DPS. Ordinary same-stat gear is safely equivalent.
  appendMechanic(flags,g.get(EquipmentSlot.HEAD)); appendMechanic(flags,g.get(EquipmentSlot.BODY));
  appendMechanic(flags,g.get(EquipmentSlot.LEGS)); appendMechanic(flags,g.get(EquipmentSlot.HANDS));
  appendMechanic(flags,g.get(EquipmentSlot.NECK));
  return flags.toString();
 }
 private void appendIdentity(StringBuilder s,EquipmentCandidate c){s.append('|').append(c==null?"-":c.getItem().getName().toLowerCase());}
 private void appendMechanic(StringBuilder s,EquipmentCandidate c){
  if(c==null){s.append("|-");return;} String n=c.getItem().getName().toLowerCase();
  if(n.contains("crystal helm")||n.contains("crystal body")||n.contains("crystal legs")||n.contains("void ranger helm")||
    n.contains("void knight top")||n.contains("elite void top")||n.contains("void knight robe")||n.contains("elite void robe")||
    n.contains("void knight gloves")||n.contains("salve amulet")||n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask")||
    n.contains("karil's")||n.contains("amulet of the damned"))s.append('|').append(n);
  else s.append("|_");
 }
 // alpha50.4.19: build each ordinary supporting-slot shortlist once per
 // optimisation and remove strictly dominated candidates before beam expansion.
 // Dominance is intentionally conservative: candidates are compared only inside
 // the same mechanic identity class, so Crystal/Void/Salve/Slayer pieces can
 // never be pruned by ordinary raw-stat gear.  The optimiser currently ranks
 // Ranged DPS first and Prayer on exact DPS ties, making RA/RS/Prayer dominance
 // safe for ordinary supporting items.
 private List<EquipmentCandidate> choicesFor(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentSlot slot){
  Profile p=activeProfile.get();
  if(p!=null){List<EquipmentCandidate> cached=p.choiceCache.get(slot);if(cached!=null)return cached;}
  List<EquipmentCandidate> raw=usable(ranked.get(slot),SLOT);
  List<EquipmentCandidate> pruned=dominancePrune(raw);
  if(p!=null){
   p.slotCandidatesBefore+=raw.size();p.slotCandidatesAfter+=pruned.size();p.slotCandidatesPruned+=raw.size()-pruned.size();
   p.choiceCache.put(slot,pruned);
  }
  return pruned;
 }
 private List<EquipmentCandidate> dominancePrune(List<EquipmentCandidate> raw){
  if(raw.size()<2)return raw;
  List<EquipmentCandidate> out=new ArrayList<>();
  for(int i=0;i<raw.size();i++){
   EquipmentCandidate c=raw.get(i); boolean dominated=false; String ck=dominanceMechanicKey(c);
   for(int j=0;j<raw.size();j++){
    if(i==j)continue; EquipmentCandidate d=raw.get(j);
    if(!ck.equals(dominanceMechanicKey(d)))continue;
    boolean noWorse=d.getRangedAttack()>=c.getRangedAttack()&&d.getRangedStrength()>=c.getRangedStrength()&&d.getPrayer()>=c.getPrayer();
    boolean strict=d.getRangedAttack()>c.getRangedAttack()||d.getRangedStrength()>c.getRangedStrength()||d.getPrayer()>c.getPrayer();
    // Exact ties keep the earlier intelligence-ranked candidate deterministically.
    boolean exact=d.getRangedAttack()==c.getRangedAttack()&&d.getRangedStrength()==c.getRangedStrength()&&d.getPrayer()==c.getPrayer();
    if((noWorse&&strict)||(exact&&j<i)){dominated=true;break;}
   }
   if(!dominated)out.add(c);
  }
  return out;
 }
 private String dominanceMechanicKey(EquipmentCandidate c){
  if(c==null)return "-"; String n=c.getItem().getName().toLowerCase();
  if(n.contains("crystal helm")||n.contains("crystal body")||n.contains("crystal legs")||n.contains("void ranger helm")||
    n.contains("void knight top")||n.contains("elite void top")||n.contains("void knight robe")||n.contains("elite void robe")||
    n.contains("void knight gloves")||n.contains("salve amulet")||n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask")||
    n.contains("karil's")||n.contains("amulet of the damned"))return n;
  return "_";
 }
 private Map<EquipmentSlot,EquipmentCandidate> karilsBundle(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
   EquipmentCandidate weapon,EquipmentCandidate ammo){
  if(weapon==null||weapon.getItem()==null||!weapon.getItem().getName().equalsIgnoreCase("Karil's crossbow"))return null;
  EquipmentCandidate head=findNamed(ranked.get(EquipmentSlot.HEAD),"Karil's coif");
  EquipmentCandidate body=findNamed(ranked.get(EquipmentSlot.BODY),"Karil's leathertop");
  EquipmentCandidate legs=findNamed(ranked.get(EquipmentSlot.LEGS),"Karil's leatherskirt");
  EquipmentCandidate neck=findNamed(ranked.get(EquipmentSlot.NECK),"Amulet of the damned");
  if(head==null||body==null||legs==null||neck==null)return null;
  Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);g.put(EquipmentSlot.WEAPON,weapon);
  if(ammo!=null)g.put(EquipmentSlot.AMMO,ammo);g.put(EquipmentSlot.HEAD,head);g.put(EquipmentSlot.BODY,body);
  g.put(EquipmentSlot.LEGS,legs);g.put(EquipmentSlot.NECK,neck);return g;
 }
 private Map<EquipmentSlot,EquipmentCandidate> crystalBundle(Map<EquipmentSlot,List<EquipmentCandidate>> ranked,
   EquipmentCandidate weapon,EquipmentCandidate ammo){
  if(!RangedWeaponEffects.usesCrystalArmourBonus(weapon))return null;
  EquipmentCandidate head=findNamed(ranked.get(EquipmentSlot.HEAD),"Crystal helm");
  EquipmentCandidate body=findNamed(ranked.get(EquipmentSlot.BODY),"Crystal body");
  EquipmentCandidate legs=findNamed(ranked.get(EquipmentSlot.LEGS),"Crystal legs");
  if(head==null||body==null||legs==null)return null;
  Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);
  g.put(EquipmentSlot.WEAPON,weapon);if(ammo!=null)g.put(EquipmentSlot.AMMO,ammo);
  g.put(EquipmentSlot.HEAD,head);g.put(EquipmentSlot.BODY,body);g.put(EquipmentSlot.LEGS,legs);
  return g;
 }
 private EquipmentCandidate findNamed(List<EquipmentCandidate> items,String name){
  if(items!=null)for(EquipmentCandidate c:items)if(c!=null&&!c.getRequirementResult().isBlocked()&&c.getItem()!=null
    &&c.getItem().getName().equalsIgnoreCase(name))return c;return null;
 }
 private List<EquipmentCandidate> allUsable(List<EquipmentCandidate>a){
  List<EquipmentCandidate>x=new ArrayList<>();
  if(a!=null)for(EquipmentCandidate c:a)if(!c.getRequirementResult().isBlocked())x.add(c);
  return x;
 }
 private List<EquipmentCandidate> usable(List<EquipmentCandidate>a,int limit){
  List<EquipmentCandidate>x=new ArrayList<>();
  if(a!=null){
   for(EquipmentCandidate c:a)if(!c.getRequirementResult().isBlocked()){
    x.add(c); if(x.size()>=limit)break;
   }
   EquipmentCandidate prayerBest=null;
   for(EquipmentCandidate c:a)if(!c.getRequirementResult().isBlocked()
      &&(prayerBest==null||c.getPrayer()>prayerBest.getPrayer()))prayerBest=c;
   if(prayerBest!=null&&prayerBest.getPrayer()>0&&!x.contains(prayerBest))x.add(prayerBest);
   for(EquipmentCandidate c:a)if(!c.getRequirementResult().isBlocked()
     &&(EquipmentAuditPolicy.specialRangedWeapon(c.getItem().getName())||RangedLoadoutEffects.synergy(c))
     &&!x.contains(c))x.add(c);
  }
  return x;
 }
 private String gearSummary(Map<EquipmentSlot,EquipmentCandidate> g){
  StringBuilder s=new StringBuilder();
  for(EquipmentSlot slot:EquipmentSlot.values()){
   EquipmentCandidate c=g.get(slot); if(c==null)continue;
   if(s.length()>0)s.append(" | ");
   s.append(slot.name()).append(": ").append(c.getItem().getName())
    .append(" [RA ").append(c.getRangedAttack()>=0?"+":"").append(c.getRangedAttack())
    .append(", RS ").append(c.getRangedStrength()>=0?"+":"").append(c.getRangedStrength()).append("]");
  }
  return s.toString();
 }

 private int prayerBonus(Map<EquipmentSlot,EquipmentCandidate>g){int p=0;for(EquipmentCandidate c:g.values())if(c!=null)p+=c.getPrayer();return p;}
 private boolean special(EquipmentCandidate c){String n=c.getItem().getName().toLowerCase();return n.contains("twisted bow")||n.contains("bow of faerdhinen")||n.contains("crystal bow")||n.contains("blowpipe")||n.contains("dragon hunter crossbow");}
}
