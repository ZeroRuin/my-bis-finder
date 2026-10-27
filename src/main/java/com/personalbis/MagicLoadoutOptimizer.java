package com.personalbis;
import java.util.*; import javax.inject.Inject; import net.runelite.api.Skill;
public class MagicLoadoutOptimizer {
 private static final int BEAM_WIDTH=32;
 private final EquipmentIntelligence intel; private final MagicCombatCalculator calc; private final AccountSnapshot account;
 @Inject public MagicLoadoutOptimizer(EquipmentIntelligence i,MagicCombatCalculator c,AccountSnapshot a){intel=i;calc=c;account=a;}

 public OptimizedMagicLoadout optimize(List<BankItem> bank,MonsterDefinition m,boolean task){return optimizeWithReport(bank,m,task).getBest();}

 public MagicOptimizationReport optimizeWithReport(List<BankItem> bank,MonsterDefinition m,boolean task){
  Map<EquipmentSlot,List<EquipmentCandidate>> ranked=intel.rank(bank,AttackStyle.MAGIC,task);
  return optimizeWithReportRanked(bank,ranked,m,task);
 }
 public MagicOptimizationReport optimizeWithReportRanked(List<BankItem> bank,Map<EquipmentSlot,List<EquipmentCandidate>> ranked,MonsterDefinition m,boolean task){
  ranked=SlayerTargetRequirements.constrainRanked(m,ranked);
  EquipmentSlot requiredSlot=SlayerTargetRequirements.requiredEquipmentSlot(m);
  EquipmentCandidate requiredEquipment=requiredSlot==null?null:SlayerTargetRequirements.firstRequiredEquipment(m,ranked.get(requiredSlot));
  // Alpha 50.3 attack-first intelligence:
  // rank viable attacks for THIS target first, then include every owned/equippable
  // weapon capable of autocasting one of those attacks. Powered staves remain
  // inherent attack/weapon pairs. This prevents raw weapon score from hiding a
  // strong caster such as Blue moon spear.
  List<MagicSpell> attackCandidates=topAttackCandidates(bank,m,15);
  List<EquipmentCandidate> weapons=weaponsForAttacks(ranked.get(EquipmentSlot.WEAPON),attackCandidates,m);
  OptimizedMagicLoadout global=null;
  for(EquipmentCandidate weapon:weapons){
   CalculationCancellation.check();
   if(requiredSlot==EquipmentSlot.SHIELD&&weapon!=null&&weapon.isTwoHanded())continue;
   // Alpha 50.2: optimise complete loadout families independently.
   // A Void family is never allowed to compete with ordinary raw-stat branches
   // until BOTH families have finished slot expansion and have real combat DPS.
   List<Map<EquipmentSlot,EquipmentCandidate>> familySeeds=new ArrayList<>();
   Map<EquipmentSlot,EquipmentCandidate> ordinary=new EnumMap<>(EquipmentSlot.class);
   if(weapon!=null)ordinary.put(EquipmentSlot.WEAPON,weapon);
   if(requiredEquipment!=null)ordinary.put(requiredEquipment.getSlot(),requiredEquipment);
   familySeeds.add(ordinary);

   List<Map<EquipmentSlot,EquipmentCandidate>> voidSeeds=new ArrayList<>();
   addVoidMageBundles(voidSeeds,ranked,weapon);
   if(requiredEquipment!=null)for(Map<EquipmentSlot,EquipmentCandidate> seed:voidSeeds)seed.put(requiredEquipment.getSlot(),requiredEquipment);
   familySeeds.addAll(voidSeeds);
   Map<EquipmentSlot,EquipmentCandidate> ahrimSeed=ahrimBundle(ranked,weapon);
   if(ahrimSeed!=null&&requiredEquipment!=null)ahrimSeed.put(requiredEquipment.getSlot(),requiredEquipment);
   if(ahrimSeed!=null)familySeeds.add(ahrimSeed);
   OptimizedMagicLoadout weaponBest=null;
   for(Map<EquipmentSlot,EquipmentCandidate> familySeed:familySeeds){
    List<Map<EquipmentSlot,EquipmentCandidate>> beam=new ArrayList<>();
    beam.add(new EnumMap<>(familySeed));

    for(EquipmentSlot slot:EquipmentSlot.values()){
     if(slot==EquipmentSlot.WEAPON||slot==EquipmentSlot.AMMO)continue;
     if(slot==EquipmentSlot.SHIELD&&weapon!=null&&weapon.isTwoHanded())continue;
     List<EquipmentCandidate> choices=usable(ranked.get(slot),4);
     List<Map<EquipmentSlot,EquipmentCandidate>> next=new ArrayList<>();
     for(Map<EquipmentSlot,EquipmentCandidate> g:beam){
      // Required family pieces are locked. Remaining slots are optimised normally.
      if(g.containsKey(slot)){next.add(new EnumMap<>(g));continue;}
      next.add(new EnumMap<>(g));
      for(EquipmentCandidate c:choices){
       Map<EquipmentSlot,EquipmentCandidate> x=new EnumMap<>(g);x.put(slot,c);next.add(x);
      }
     }
     next.sort((a,b)->Double.compare(gearPotential(b),gearPotential(a)));
     beam=new ArrayList<>(next.subList(0,Math.min(BEAM_WIDTH,next.size())));
    }

    OptimizedMagicLoadout familyBest=evaluateFamily(beam,bank,m,task,weapon,attackCandidates);
    if(familyBest!=null){
     familyBest=completeNeutralSlots(familyBest,ranked,m,task,weapon);
     if(weaponBest==null||FinalLoadoutSelection.betterTtk(
       familyBest.getResult().getExpectedTtkSeconds(),familyBest.getResult().getDps(),weaponBest.getResult().getExpectedTtkSeconds(),weaponBest.getResult().getDps()))weaponBest=familyBest;
    }
   }
   if(weaponBest!=null
      &&(global==null||FinalLoadoutSelection.betterTtk(
        weaponBest.getResult().getExpectedTtkSeconds(),weaponBest.getResult().getDps(),global.getResult().getExpectedTtkSeconds(),global.getResult().getDps())))global=weaponBest;
  }
  return new MagicOptimizationReport(global);
 }

 private OptimizedMagicLoadout evaluateFamily(List<Map<EquipmentSlot,EquipmentCandidate>> beam,
   List<BankItem> bank,MonsterDefinition m,boolean task,EquipmentCandidate weapon,List<MagicSpell> attackCandidates){
  OptimizedMagicLoadout best=null;
  PoweredStaff powered=weapon==null||weapon.getItem().getItemId()==34727?null:PoweredStaff.forWeapon(weapon.getItem().getName());
  for(Map<EquipmentSlot,EquipmentCandidate> gear:beam){
   if(powered!=null&&account.real(Skill.MAGIC)>=powered.minLevel){
    MagicCombatResult r=calc.calculatePowered(gear,m,powered,task);
    MagicSpell pseudo=new MagicSpell(powered.label,powered.minLevel,powered.baseMaxHit(RaidScaling.boosted(account,Skill.MAGIC,m)));
    if(best==null||FinalLoadoutSelection.betterTtk(r.getExpectedTtkSeconds(),r.getDps(),best.getResult().getExpectedTtkSeconds(),best.getResult().getDps()))
     best=new OptimizedMagicLoadout(gear,r,pseudo);
    continue;
   }
   for(MagicSpell sp:attackCandidates){
    if(sp.level>account.real(Skill.MAGIC)){
     continue;
    }
    if("ancient".equals(sp.spellbook)&&!account.isAncientMagicksUnlocked())continue;
    if("arceuus".equals(sp.spellbook)){
     if(sp.isDemonbane()&&(m==null||!m.hasAttribute("demon")))continue;
     if((sp.name.startsWith("Superior Demonbane")||sp.name.startsWith("Dark Demonbane")||sp.markOfDarkness)
       &&!account.isKingdomDividedUnlocked())continue;
    }
    if(!MagicSpellCompatibility.canAutocast(weapon==null?null:weapon.getItem().getName(),sp)){
     continue;
    }
    if(!runesAvailable(sp,bank,weapon,gear.values())){
     continue;
    }
    MagicCombatResult r=calc.calculate(gear,m,sp,task);
    if(best==null||FinalLoadoutSelection.betterTtk(r.getExpectedTtkSeconds(),r.getDps(),best.getResult().getExpectedTtkSeconds(),best.getResult().getDps()))
     best=new OptimizedMagicLoadout(gear,r,sp);
   }
  }
  return best;
 }

 private boolean named(EquipmentCandidate c,String n){
  return c!=null&&c.getItem()!=null&&c.getItem().getName().equalsIgnoreCase(n);
 }

 private OptimizedMagicLoadout completeNeutralSlots(OptimizedMagicLoadout best,
   Map<EquipmentSlot,List<EquipmentCandidate>> ranked,MonsterDefinition m,boolean task,EquipmentCandidate weapon){
  if(best==null)return null;
  Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(best.getItems());
  MagicCombatResult current=best.getResult();
  PoweredStaff powered=weapon==null||weapon.getItem().getItemId()==34727?null:PoweredStaff.forWeapon(weapon.getItem().getName());
  for(EquipmentSlot slot:EquipmentSlot.values()){
   if(slot==EquipmentSlot.WEAPON||gear.containsKey(slot))continue;
   if(slot==EquipmentSlot.SHIELD&&weapon!=null&&weapon.isTwoHanded())continue;
   List<EquipmentCandidate> all=ranked.get(slot);if(all==null)continue;
   EquipmentCandidate chosen=null;MagicCombatResult chosenResult=current;
   for(EquipmentCandidate c:all){
    if(c.getRequirementResult().isBlocked())continue;
    Map<EquipmentSlot,EquipmentCandidate> trial=new EnumMap<>(gear);trial.put(slot,c);
    MagicCombatResult r=powered!=null
      ?calc.calculatePowered(trial,m,powered,task)
      :calc.calculate(trial,m,best.getSpell(),task);
    if(r==null||FinalLoadoutSelection.betterTtk(current.getExpectedTtkSeconds(),current.getDps(),r.getExpectedTtkSeconds(),r.getDps()))continue;
    if(chosen==null||FinalLoadoutSelection.betterTtk(r.getExpectedTtkSeconds(),r.getDps(),chosenResult.getExpectedTtkSeconds(),chosenResult.getDps())
      ||(Double.compare(r.getExpectedTtkSeconds(),chosenResult.getExpectedTtkSeconds())==0&&c.getPrayer()>chosen.getPrayer())){
     chosen=c;chosenResult=r;
    }
   }
   if(chosen!=null){gear.put(slot,chosen);current=chosenResult;}
  }
  return new OptimizedMagicLoadout(gear,current,best.getSpell());
 }

 private void addVoidMageBundles(List<Map<EquipmentSlot,EquipmentCandidate>> beam,
   Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate weapon){
  EquipmentCandidate helm=findNamed(ranked.get(EquipmentSlot.HEAD),"void mage helm");
  EquipmentCandidate gloves=findNamed(ranked.get(EquipmentSlot.HANDS),"void knight gloves");
  if(helm==null||gloves==null)return;
  addVoidBundle(beam,weapon,helm,gloves,
   findNamed(ranked.get(EquipmentSlot.BODY),"void knight top"),
   findNamed(ranked.get(EquipmentSlot.LEGS),"void knight robe"));
  addVoidBundle(beam,weapon,helm,gloves,
   findNamed(ranked.get(EquipmentSlot.BODY),"elite void top"),
   findNamed(ranked.get(EquipmentSlot.LEGS),"elite void robe"));
 }
 private void addVoidBundle(List<Map<EquipmentSlot,EquipmentCandidate>> beam,EquipmentCandidate weapon,
   EquipmentCandidate helm,EquipmentCandidate gloves,EquipmentCandidate top,EquipmentCandidate robe){
  if(top==null||robe==null)return;
  Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);
  if(weapon!=null)g.put(EquipmentSlot.WEAPON,weapon);
  g.put(EquipmentSlot.HEAD,helm);g.put(EquipmentSlot.HANDS,gloves);
  g.put(EquipmentSlot.BODY,top);g.put(EquipmentSlot.LEGS,robe);
  beam.add(g);
 }
 private EquipmentCandidate findNamed(List<EquipmentCandidate> items,String name){
  if(items!=null)for(EquipmentCandidate c:items)if(c!=null&&!c.getRequirementResult().isBlocked()
    &&c.getItem()!=null&&(c.getItem().getName().equalsIgnoreCase(name)||((name.startsWith("void ")||name.startsWith("elite void "))&&VoidEquipment.matches(c.getItem().getName(),name))))return c;
  return null;
 }
 private Map<EquipmentSlot,EquipmentCandidate> ahrimBundle(
   Map<EquipmentSlot,List<EquipmentCandidate>> ranked,EquipmentCandidate weapon){
  if(weapon==null||weapon.getItem()==null||!weapon.getItem().getName().equalsIgnoreCase("Ahrim's staff"))return null;
  EquipmentCandidate hood=findNamed(ranked.get(EquipmentSlot.HEAD),"Ahrim's hood");
  EquipmentCandidate top=findNamed(ranked.get(EquipmentSlot.BODY),"Ahrim's robetop");
  EquipmentCandidate skirt=findNamed(ranked.get(EquipmentSlot.LEGS),"Ahrim's robeskirt");
  EquipmentCandidate amulet=findNamed(ranked.get(EquipmentSlot.NECK),"Amulet of the damned");
  if(hood==null||top==null||skirt==null||amulet==null)return null;
  Map<EquipmentSlot,EquipmentCandidate> g=new EnumMap<>(EquipmentSlot.class);
  g.put(EquipmentSlot.WEAPON,weapon);g.put(EquipmentSlot.HEAD,hood);g.put(EquipmentSlot.BODY,top);
  g.put(EquipmentSlot.LEGS,skirt);g.put(EquipmentSlot.NECK,amulet);return g;
 }
 private List<MagicSpell> topAttackCandidates(List<BankItem> bank,MonsterDefinition m,int limit){
  List<MagicSpell> all=new ArrayList<>();
  for(MagicSpell sp:MagicSpell.combat()){
   // Target eligibility must participate in attack shortlisting. On leafy
   // targets an otherwise high-potential spell (for example Smoke Barrage)
   // must not consume the bounded shortlist and hide Magic Dart, the legal
   // magic attack. The calculator still keeps the zero-DPS guard as defence
   // in depth for direct/synthetic calls.
   if(!LeafyTargetRules.magicCanDamage(m,sp))continue;
   if(sp.level>account.real(Skill.MAGIC))continue;
   if("ancient".equals(sp.spellbook)&&!account.isAncientMagicksUnlocked())continue;
   if("arceuus".equals(sp.spellbook)){
    if(sp.isDemonbane()&&(m==null||!m.hasAttribute("demon")))continue;
    if((sp.name.startsWith("Superior Demonbane")||sp.name.startsWith("Dark Demonbane")||sp.markOfDarkness)
      &&!account.isKingdomDividedUnlocked())continue;
   }
   // Weapon-independent rune check. Rune-providing weapons are handled later, so
   // don't reject elemental spells merely because the bank lacks that element.
   if(!runesPotentiallyAvailable(sp,bank))continue;
   all.add(sp);
  }
  all.sort((a,b)->Double.compare(attackPotential(b,m),attackPotential(a,m)));
  if(all.size()>limit)all=new ArrayList<>(all.subList(0,limit));
  return all;
 }
 private double attackPotential(MagicSpell sp,MonsterDefinition m){
  double base=sp.scaledBaseMax(RaidScaling.boosted(account,Skill.MAGIC,m));
  double speed=5.0;
  double weakness=ElementalWeaknessEffects.multiplier(m,sp);
  double mult=1.0+weakness;
  if(sp.isDemonbane()&&m!=null&&m.hasAttribute("demon")){
   mult*=sp.markOfDarkness?1.25:1.0;
   // Accuracy is deliberately represented lightly here; exact accuracy is only
   // used after a compatible weapon and complete loadout have been assembled.
   mult*=sp.markOfDarkness?1.40:1.20;
  }
  return base*mult/speed;
 }
 private boolean runesPotentiallyAvailable(MagicSpell sp,List<BankItem> bank){
  Map<String,Integer> have=new HashMap<>();for(BankItem b:bank)have.put(b.getName().toLowerCase(),b.getQuantity());
  for(Map.Entry<String,Integer> e:sp.runes.entrySet()){
   String r=e.getKey().toLowerCase();
   // Elemental rune may be supplied by the eventual weapon/tome.
   if(r.equals("air rune")||r.equals("water rune")||r.equals("earth rune")||r.equals("fire rune"))continue;
   if(have.getOrDefault(r,0)<e.getValue())return false;
  }
  return true;
 }
 private List<EquipmentCandidate> weaponsForAttacks(List<EquipmentCandidate>x,List<MagicSpell> attacks,MonsterDefinition m){
  List<EquipmentCandidate> r=new ArrayList<>();
  if(x==null)return r;
  for(EquipmentCandidate c:x){
   if(c==null||c.getRequirementResult().isBlocked()||c.getItem()==null)continue;
   String n=c.getItem().getName();
   if(PoweredStaff.forWeapon(n)!=null||PoweredStaff.requiresContext(n)){r.add(c);continue;}
   for(MagicSpell sp:attacks)if(MagicSpellCompatibility.canAutocast(n,sp)){r.add(c);break;}
  }
  return r;
 }
 private double gearPotential(Map<EquipmentSlot,EquipmentCandidate> g){
  int atk=0,prayer=0;double dmg=0;for(EquipmentCandidate c:g.values()){if(c!=null){atk+=c.getMagicAttack();dmg+=c.getMagicDamage();prayer+=c.getPrayer();}}
  return atk+Math.max(0,dmg)*1000.0+prayer*0.001;
 }
 private List<EquipmentCandidate> usable(List<EquipmentCandidate>x,int max){
  List<EquipmentCandidate>r=new ArrayList<>();
  if(x!=null){
   for(EquipmentCandidate c:x)if(!c.getRequirementResult().isBlocked()){r.add(c);if(r.size()>=max)break;}
   EquipmentCandidate prayerBest=null;
   for(EquipmentCandidate c:x)if(!c.getRequirementResult().isBlocked()
     &&(prayerBest==null||c.getPrayer()>prayerBest.getPrayer()))prayerBest=c;
   if(prayerBest!=null&&prayerBest.getPrayer()>0&&!r.contains(prayerBest))r.add(prayerBest);
   // Preserve effect/set pieces even if their raw magic score is outside top-N.
   for(EquipmentCandidate c:x)if(!c.getRequirementResult().isBlocked()&&MagicLoadoutEffects.synergy(c)&&!r.contains(c))r.add(c);
  }
  return r;
 }

 static boolean runesAvailable(MagicSpell sp,List<BankItem> bank,EquipmentCandidate weapon,Collection<EquipmentCandidate> gear){
  Map<String,Integer> have=new HashMap<>();for(BankItem b:bank)have.put(b.getName().toLowerCase(),b.getQuantity());
  String wn=weapon==null?"":weapon.getItem().getName().toLowerCase();
  for(Map.Entry<String,Integer> e:sp.runes.entrySet()){String rune=e.getKey().toLowerCase();if(provides(wn,rune)||MagicTomeEffects.providesRune(gear,rune))continue;if(have.getOrDefault(rune,0)<e.getValue())return false;}return true;
 }
 private static boolean provides(String w,String r){
  if(w.equals("kodai wand")&&r.equals("water rune"))return true;
  if(w.contains("staff")||w.contains("battlestaff")){
   if(r.equals("air rune")&&(w.contains("air")||w.contains("smoke")||w.contains("dust")||w.contains("mist")))return true;
   if(r.equals("water rune")&&(w.contains("water")||w.contains("steam")||w.contains("mud")||w.contains("mist")))return true;
   if(r.equals("earth rune")&&(w.contains("earth")||w.contains("lava")||w.contains("mud")||w.contains("dust")))return true;
   if(r.equals("fire rune")&&(w.contains("fire")||w.contains("lava")||w.contains("smoke")||w.contains("steam")))return true;
  }return false;
 }
}
