package com.personalbis;
import java.util.*; import javax.inject.Inject; import net.runelite.api.Skill;
public class MagicCombatCalculator {
 private final AccountSnapshot account;
 @Inject public MagicCombatCalculator(AccountSnapshot a){account=a;}
 public MagicCombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,MagicSpell spell,boolean task){
  int bonus=0; double md=0; boolean slayer=false; String weaponName="";
  for(EquipmentCandidate c:gear.values()){if(c==null)continue;bonus+=c.getMagicAttack();md+=c.getMagicDamage();String n=c.getItem().getName().toLowerCase();if(c.getSlot()==EquipmentSlot.WEAPON)weaponName=c.getItem().getName();if(c.getSlot()==EquipmentSlot.HEAD&&(n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask"))&&(n.contains("(i)")||n.contains("imbued")))slayer=true;}
  WikiEquipmentEngine.Totals wikiEq=WikiEquipmentEngine.transform(gear,m,spell.spellbook,0,0,bonus,md,0); bonus=wikiEq.magicAttack; md=wikiEq.magicDamage;
  int lvl=Math.max(1,account.boosted(Skill.MAGIC)); MagicPrayer mp=MagicPrayer.best(account); String pn=mp.name;
  int eff;
  MagicLoadoutEffects.Effect loadout=MagicLoadoutEffects.evaluate(gear.values(),m,task,spell);
  WikiMagicAccuracy.Result magicAccuracy=WikiMagicAccuracy.calculate(lvl,mp.accuracy,bonus,gear.values(),m,spell,task);
  eff=magicAccuracy.effectiveLevel;
  int roll=magicAccuracy.attackRoll;
  int ml=m==null?1:Math.max(1,m.getMagicLevel());
  int deff=ml+9, dr=deff*(m==null?64:m.getMagicDefence()+64);
  double weakness=ElementalWeaknessEffects.multiplier(m,spell);
  boolean demonbane=spell.isDemonbane()&&m!=null&&m.hasAttribute("demon");
  MagicTomeEffects.Effect tome=MagicTomeEffects.evaluate(gear.values(),spell);
  double acc=WikiAccuracyRoll.normal(roll,dr);
  ElementalAmuletEffects.Effect amulet=ElementalAmuletEffects.evaluate(gear.values(),spell);
  WikiMagicDamage.Result magicDamage=WikiMagicDamage.standard(lvl,md,mp.damage,gear.values(),m,spell,task);
  int max=magicDamage.maxHit;
  int ticks=MagicSpellCompatibility.attackTicks(weaponName,spell);
  int preTransformMax=max;
  Double brimstoneAccuracy=hasItem(gear,"Brimstone ring")?WikiAccuracyRoll.normal(roll,WikiIntegerStages.factor(dr,9,10)):null;
  HitDistribution.AttackDistribution magicAttack=WikiPlayerVsNpcCalc.magicFinalDistribution(magicDamage.minHit,max,acc,brimstoneAccuracy,m,gear.values(),spell);
  double expectedHit=WikiPlayerVsNpcCalc.expectedDamage(magicAttack);
  double dps=expectedHit/(ticks*.6);
  max=magicAttack.max();
  // All Magic is corpbane in the Wiki calculator, so Corp applies no reduction here.
  if(!WikiTargetEligibility.magicCanDamage(m) || !LeafyTargetRules.magicCanDamage(m,spell)){max=0;dps=0.0;}
  StringBuilder note=new StringBuilder();
  if(weakness>0)note.append("Weakness: ").append(spell.element()).append(" +").append(m.getWeaknessSeverity()).append("% accuracy/damage");
  String wn=MagicWeaponEffects.note(weaponName,spell,m);if(!wn.isEmpty()){if(note.length()>0)note.append(" | ");note.append(wn);}
  String cn=MagicSpellCompatibility.note(weaponName,spell);if(!cn.isEmpty()){if(note.length()>0)note.append(" | ");note.append(cn);} if(!tome.note.isEmpty()){if(note.length()>0)note.append(" | ");note.append(tome.note);} if(!amulet.note.isEmpty()){if(note.length()>0)note.append(" | ");note.append(amulet.note);} if(!loadout.note.isEmpty()){if(note.length()>0)note.append(" | ");note.append(loadout.note);}
  return new MagicCombatResult(dps,acc,max,roll,dr,bonus,md,pn,spell.name,note.toString(),eff,ticks,preTransformMax);
 }
 public MagicCombatResult calculatePowered(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition m,PoweredStaff staff,boolean task){
  int bonus=0; double md=0; boolean slayer=false;
  for(EquipmentCandidate c:gear.values()){if(c==null)continue;bonus+=c.getMagicAttack();md+=c.getMagicDamage();String n=c.getItem().getName().toLowerCase();if(c.getSlot()==EquipmentSlot.HEAD&&(n.contains("slayer helm")||n.contains("slayer helmet")||n.contains("black mask"))&&(n.contains("(i)")||n.contains("imbued")))slayer=true;}
  boolean shadow="Tumeken's shadow".equals(staff.label);
  WikiEquipmentEngine.Totals wikiEq=WikiEquipmentEngine.transform(gear,m,null,0,0,bonus,md,0); bonus=wikiEq.magicAttack; md=wikiEq.magicDamage;
  int lvl=Math.max(1,account.boosted(Skill.MAGIC)); MagicPrayer mp=MagicPrayer.best(account); String pn=mp.name;
  int eff;
  MagicLoadoutEffects.Effect loadout=MagicLoadoutEffects.evaluate(gear.values(),m,task);
  WikiMagicAccuracy.Result magicAccuracy=WikiMagicAccuracy.calculate(lvl,mp.accuracy,bonus,gear.values(),m,null,task);
  eff=magicAccuracy.effectiveLevel;
  int roll=magicAccuracy.attackRoll;
  int ml=m==null?1:Math.max(1,m.getMagicLevel());
  int deff=ml+9, dr=deff*(m==null?64:m.getMagicDefence()+64);
  double acc=WikiAccuracyRoll.normal(roll,dr);
  int base=staff.baseMaxHit(lvl);
  WikiMagicDamage.Result magicDamage=WikiMagicDamage.powered(base,md,mp.damage,gear.values(),m,task);
  int max=magicDamage.maxHit;
  int preTransformMax=max;
  Double brimstoneAccuracy=hasItem(gear,"Brimstone ring")?WikiAccuracyRoll.normal(roll,WikiIntegerStages.factor(dr,9,10)):null;
  HitDistribution.AttackDistribution poweredAttack=WikiPlayerVsNpcCalc.magicFinalDistribution(magicDamage.minHit,max,acc,brimstoneAccuracy,m,gear.values(),null);
  int poweredTicks=WikiAttackSpeedRules.attackTicks(staff.speed,staff.label,m);
  double dps=WikiPlayerVsNpcCalc.dps(poweredAttack,poweredTicks); max=poweredAttack.max();
  boolean ratBoneAllowed=!"Bone staff".equals(staff.label)||(m!=null&&m.hasAttribute("rat"));
  if(!WikiTargetEligibility.magicCanDamage(m) || !LeafyTargetRules.poweredMagicCanDamage(m) || !ratBoneAllowed){max=0;dps=0.0;}
  String poweredNote;
  if(shadow)poweredNote="Shadow: 3x equipment Magic attack/damage (outside ToA), damage cap 100%, 5t";
  else if("Eye of ayak".equals(staff.label))poweredNote="Eye of ayak: built-in spell, 3t; Soul Rend special not included in sustained DPS";
  else if("Bone staff".equals(staff.label)) poweredNote="Bone staff: powered ratbane spell, +10 max vs rats; "+poweredTicks+"t";
  else poweredNote="Powered staff: built-in spell, "+poweredTicks+"t";
  if(!loadout.note.isEmpty())poweredNote+=" | "+loadout.note;
 return new MagicCombatResult(dps,acc,max,roll,dr,bonus,md,pn,staff.label,poweredNote,eff,poweredTicks,preTransformMax);
 }

 private static boolean hasItem(Map<EquipmentSlot,EquipmentCandidate> gear,String name){
  if(gear!=null)for(EquipmentCandidate c:gear.values())if(c!=null&&c.getItem()!=null&&c.getItem().getName().equalsIgnoreCase(name))return true;
  return false;
 }

}
