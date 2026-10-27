package com.personalbis;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Skill;
public class RangedCombatCalculator {
 private final AccountSnapshot account;
 @Inject public RangedCombatCalculator(AccountSnapshot a){account=a;}
 public RangedCombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
  RangedCombatResult best=null;
  for(RangedStance st:RangedStance.values())for(RangedPrayer p:prayers()){
   EquipmentCandidate weapon=gear.get(EquipmentSlot.WEAPON);
   if(weapon!=null&&"Salamander".equalsIgnoreCase(weapon.getWeaponCategory())&&st!=RangedStance.RAPID)continue;
   RangedCombatResult r=calculate(gear,m,task,st,p); if(best==null||FinalLoadoutSelection.betterTtk(r.getExpectedTtkSeconds(),r.getDps(),best.getExpectedTtkSeconds(),best.getDps()))best=r;
  } return best;
 }
 public RangedCombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task,RangedStance st,RangedPrayer p){
  return calculateInternal(gear,m,task,st,p,true);
 }
 RangedCombatResult calculateSnapshot(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task,RangedStance st,RangedPrayer p){
  return calculateInternal(gear,m,task,st,p,false);
 }
 private RangedCombatResult calculateInternal(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task,RangedStance st,RangedPrayer p,boolean finiteFight){
  int atk=0,str=0,speed=4; boolean slayer=false; EquipmentCandidate weapon=gear.get(EquipmentSlot.WEAPON);
  for(EquipmentCandidate c:gear.values()){if(c==null)continue;
   if(c.getSlot()==EquipmentSlot.AMMO && !RangedAmmoRules.compatible(weapon,c)) continue;
   atk+=c.getRangedAttack();str+=c.getRangedStrength();
   if(c.getSlot()==EquipmentSlot.WEAPON&&c.getAttackSpeed()>0)speed=c.getAttackSpeed();
   String n=c.getItem().getName().toLowerCase(); if(c.getSlot()==EquipmentSlot.HEAD&&(n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask"))&&(n.contains("(i)")||n.contains("imbued")))slayer=true;}
  // transform(str, rangedStr, magicAttack, magicDamage, rangedAttack): ranged
  // strength must occupy the second numeric channel. Passing it as melee Str
  // zeroed every weapon/ammo Ranged Strength during the exact final evaluation.
  WikiEquipmentEngine.Totals wikiEq=WikiEquipmentEngine.transform(gear,m,null,0,str,0,0.0,atk); atk=wikiEq.rangedAttack; str=wikiEq.rangedStr;
  int level=Math.max(1,RaidScaling.boosted(account,Skill.RANGED,m));
  boolean voidRanged=RangedLoadoutEffects.voidRanged(gear), eliteVoid=RangedLoadoutEffects.eliteVoidRanged(gear);
  int eff=CombatEffectiveLevels.ranged(level,p.getRanged(),st.getRangedBoost(),voidRanged);
  boolean salve=RangedLoadoutEffects.rangedSalveActive(gear,m);
  int roll=eff*(atk+64);
  roll=(int)Math.floor(roll*RangedLoadoutEffects.crystalAccuracy(gear,weapon));
  roll=WikiRangedEffects.accuracyAfterExclusive(roll,gear.values(),m,task,slayer);
  roll=WikiRangedEffects.postAccuracy(roll,weapon,m,st,4);
  if(RaidCombatRules.wardenP2(m)&&RangedWeaponEffects.isTwistedBow(weapon))
   roll=RangedWeaponEffects.scaleTwisted(roll,m,true);
  int def=m==null?1:Math.max(0,VardorvisRules.startingDefence(m)), bonus=RangedAmmoRules.targetDefence(m,weapon);
  int defRoll=RaidScaling.defenceRoll(m,(def+9)*(bonus+64));
  double acc=FracturedArchiveRules.imp(m)?1.0:WikiAccuracyRoll.normal(roll,defRoll);
  boolean strengthRanged=EquipmentSetRules.strengthRanged(weapon);
  int damageLevel=strengthRanged?Math.max(1,RaidScaling.boosted(account,Skill.STRENGTH,m)):level;
  if(strengthRanged){str=0;for(EquipmentCandidate c:gear.values())if(c!=null){
   com.personalbis.wiki.WikiEquipment item=WikiEquipmentEngine.canonicalItem(c.getItem().getItemId());
   str+=item!=null&&item.bonuses!=null?item.bonuses.str:c.getStrengthBonus();}}
  int dmgEff=CombatEffectiveLevels.rangedDamage(damageLevel,p.getRangedStrength(),st.getRangedBoost(),voidRanged,eliteVoid);
  int max=WikiIntegerStages.maxHitFromEffective(dmgEff,str+64);
  max=(int)Math.floor(max*RangedLoadoutEffects.crystalDamage(gear,weapon));
  max=strengthRanged?EquipmentSetRules.strengthRangedMax(max,gear,m,task):WikiRangedEffects.damageAfterExclusive(max,gear.values(),m,weapon,task,slayer);
  max=WikiRangedEffects.postDamage(max,weapon,m,task,slayer);
  // Wiki parity: Bone shortbow receives the ratbane flat +10 max hit on rat targets.
  max += MeleeWeaponEffects.ratbaneBonusMax(weapon,m);
  int actualSpeed=WikiAttackSpeedRules.attackTicks(Math.max(1,speed+st.getSpeedDelta()),weapon,m);
  int preSpecMax=max;
  int normalMin=0;
  if(RaidCombatRules.wardenP2(m)){int[] bounds=RaidCombatRules.wardenBounds(max,roll,defRoll);normalMin=bounds[0];max=bounds[1];acc=1.0;}
  EquipmentCandidate ammo=gear.get(EquipmentSlot.AMMO);
  HitDistribution.AttackDistribution rangedDist=WikiPlayerVsNpcCalc.finalDistribution(
      WikiPlayerVsNpcCalc.rangedAttacker(weapon,ammo,m,level,normalMin,max,acc,account.isKandarinHardCompleted(),gear.values()),
      m,AttackStyle.RANGED,weapon,null,false,gear.values());
  final int normalMax=max;
  double expectedDamage=WikiPlayerVsNpcCalc.expectedDamage(rangedDist);
  double dps=WikiPlayerVsNpcCalc.dps(rangedDist,actualSpeed);
  int targetHp=m==null?1:Math.max(1,m.getHitpoints());
  boolean ruby=ammo!=null&&ammo.getItem()!=null&&ammo.getItem().getName().toLowerCase().contains("ruby");
  Map<Integer,HitDistribution.AttackDistribution> hpDists=ruby?new HashMap<>():null;
  HitDistribution rubyBase=ruby?WikiPlayerVsNpcCalc.rangedRubyBaseDistribution(
      m,level,normalMin,normalMax,acc,account.isKandarinHardCompleted()):null;
  double ttk=!finiteFight||VardorvisRules.supported(m)?Double.POSITIVE_INFINITY:ruby
      ?ExpectedKillTime.seconds(targetHp,actualSpeed,hp->hpDists.computeIfAbsent(EnchantedBoltEffects.rubyHpBucket(hp),key->
          WikiPlayerVsNpcCalc.rangedRubyFinalDistributionAtHp(rubyBase,weapon,ammo,m,
              account.isKandarinHardCompleted(),key)))
      :ExpectedKillTime.seconds(rangedDist,targetHp,actualSpeed);
  if(finiteFight&&VardorvisRules.supported(m)){
   final int fixedRoll=roll, fixedMin=normalMin, fixedMax=normalMax;
   Map<String,HitDistribution.AttackDistribution> evolving=new HashMap<>();
   ttk=ExpectedKillTime.seconds(targetHp,actualSpeed,hp->{
    int defence=VardorvisRules.defence(m,hp);
    int rubyHp=ruby?EnchantedBoltEffects.rubyHpBucket(hp):targetHp;
    String key=defence+":"+rubyHp;
    return evolving.computeIfAbsent(key,k->{
     double chance=WikiAccuracyRoll.normal(fixedRoll,(defence+9)*(bonus+64));
     if(ruby){
      HitDistribution base=WikiPlayerVsNpcCalc.rangedRubyBaseDistribution(m,level,fixedMin,fixedMax,chance,account.isKandarinHardCompleted());
      return WikiPlayerVsNpcCalc.rangedRubyFinalDistributionAtHp(base,weapon,ammo,m,account.isKandarinHardCompleted(),rubyHp);
     }
     return WikiPlayerVsNpcCalc.finalDistribution(
      WikiPlayerVsNpcCalc.rangedAttacker(weapon,ammo,m,level,fixedMin,fixedMax,chance,account.isKandarinHardCompleted(),gear.values()),
      m,AttackStyle.RANGED,weapon,null,false,gear.values());
    });
   });
  }
  max=rangedDist.max();
  if(!WikiTargetEligibility.rangedCanDamage(m) || !LeafyTargetRules.rangedCanDamage(m,ammo) || !MeleeWeaponEffects.ratBoneCanDamage(weapon,m)){max=0;dps=0.0;ttk=Double.POSITIVE_INFINITY;}
  if(FracturedArchiveRules.blade(m))ttk=Double.POSITIVE_INFINITY;
  return new RangedCombatResult(dps,ttk,acc,max,roll,defRoll,atk,str,actualSpeed,eff,dmgEff,st,p,preSpecMax);
 }
 private RangedPrayer[] prayers(){int x=account.real(Skill.PRAYER);
  if(x>=74 && account.isRigourUnlocked())return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.RIGOUR};
  if(x>=62 && account.isDeadeyeUnlocked())return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.DEADEYE};
  if(x>=44)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.EAGLE_EYE};
  if(x>=26)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.HAWK_EYE};
  if(x>=8)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.SHARP_EYE};
  return new RangedPrayer[]{RangedPrayer.NONE};}
}
