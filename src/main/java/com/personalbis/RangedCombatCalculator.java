package com.personalbis;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Skill;
public class RangedCombatCalculator {
 private final AccountSnapshot account;
 @Inject public RangedCombatCalculator(AccountSnapshot a){account=a;}
 public RangedCombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task){
  RangedCombatResult best=null;
  for(RangedStance st:RangedStance.values())for(RangedPrayer p:prayers()){
   RangedCombatResult r=calculate(gear,m,task,st,p); if(best==null||r.getDps()>best.getDps())best=r;
  } return best;
 }
 public RangedCombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,boolean task,RangedStance st,RangedPrayer p){
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
  int level=Math.max(1,account.boosted(Skill.RANGED));
  boolean voidRanged=RangedLoadoutEffects.voidRanged(gear), eliteVoid=RangedLoadoutEffects.eliteVoidRanged(gear);
  int eff=CombatEffectiveLevels.ranged(level,p.getRanged(),st.getRangedBoost(),voidRanged);
  boolean salve=RangedLoadoutEffects.rangedSalveActive(gear,m);
  int roll=eff*(atk+64);
  roll=(int)Math.floor(roll*RangedLoadoutEffects.crystalAccuracy(gear,weapon));
  roll=WikiRangedEffects.accuracyAfterExclusive(roll,gear.values(),m,task,slayer);
  roll=WikiRangedEffects.postAccuracy(roll,weapon,m,st,4);
  int def=m==null?1:Math.max(1,m.getDefenceLevel()), bonus=RangedAmmoRules.targetDefence(m,weapon);
  int defRoll=(def+9)*(bonus+64);
  double acc=WikiAccuracyRoll.normal(roll,defRoll);
  int dmgEff=CombatEffectiveLevels.rangedDamage(level,p.getRangedStrength(),st.getRangedBoost(),voidRanged,eliteVoid);
  int max=WikiIntegerStages.maxHitFromEffective(dmgEff,str+64);
  max=(int)Math.floor(max*RangedLoadoutEffects.crystalDamage(gear,weapon));
  max=WikiRangedEffects.damageAfterExclusive(max,gear.values(),m,weapon,task,slayer);
  max=WikiRangedEffects.postDamage(max,weapon,m,task,slayer);
  // Wiki parity: Bone shortbow receives the ratbane flat +10 max hit on rat targets.
  max += MeleeWeaponEffects.ratbaneBonusMax(weapon,m);
  int actualSpeed=WikiAttackSpeedRules.attackTicks(Math.max(1,speed+st.getSpeedDelta()),weapon,m);
  EquipmentCandidate ammo=gear.get(EquipmentSlot.AMMO);
  HitDistribution.AttackDistribution rangedDist=WikiPlayerVsNpcCalc.rangedFinalDistribution(
      weapon,ammo,m,level,max,acc,account.isKandarinHardCompleted(),gear.values());
  double expectedDamage=WikiPlayerVsNpcCalc.expectedDamage(rangedDist);
  double dps=WikiPlayerVsNpcCalc.dps(rangedDist,actualSpeed);
  max=rangedDist.max();
  if(!WikiTargetEligibility.rangedCanDamage(m) || !LeafyTargetRules.rangedCanDamage(m,ammo) || !MeleeWeaponEffects.ratBoneCanDamage(weapon,m)){max=0;dps=0.0;}
  return new RangedCombatResult(dps,acc,max,roll,defRoll,atk,str,actualSpeed,eff,dmgEff,st,p);
 }
 private RangedPrayer[] prayers(){int x=account.real(Skill.PRAYER);
  if(x>=74 && account.isRigourUnlocked())return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.RIGOUR};
  if(x>=62 && account.isDeadeyeUnlocked())return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.DEADEYE};
  if(x>=44)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.EAGLE_EYE};
  if(x>=26)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.HAWK_EYE};
  if(x>=8)return new RangedPrayer[]{RangedPrayer.NONE,RangedPrayer.SHARP_EYE};
  return new RangedPrayer[]{RangedPrayer.NONE};}
}
