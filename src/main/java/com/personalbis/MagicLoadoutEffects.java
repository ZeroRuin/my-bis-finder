package com.personalbis;
import java.util.*;
public final class MagicLoadoutEffects {
 private MagicLoadoutEffects(){}
 public static final class Effect {
  public final double accuracy,damage; public final String note;
  Effect(double a,double d,String n){accuracy=a;damage=d;note=n;}
 }
 public static Effect evaluate(Collection<EquipmentCandidate> gear,MonsterDefinition m,boolean onTask){return evaluate(gear,m,onTask,null);}
 public static Effect evaluate(Collection<EquipmentCandidate> gear,MonsterDefinition m,boolean onTask,MagicSpell spell){
  List<String> names=new ArrayList<>();if(gear!=null)for(EquipmentCandidate e:gear)if(e!=null&&e.getItem()!=null)names.add(e.getItem().getName());
  Effect e=evaluateNames(names,m,onTask);
  if(spell!=null&&"ancient".equals(spell.spellbook)){int pieces=0;for(String n:names)if(n!=null&&n.toLowerCase().startsWith("virtus "))pieces++;if(pieces>0)return new Effect(e.accuracy,e.damage*(1.0+0.05*pieces),e.note+(e.note.isEmpty()?"":" | ")+"Virtus Ancient: +"+(pieces*5)+"% damage");}
  return e;
 }
 static Effect evaluateNames(Collection<String> names,MonsterDefinition m,boolean onTask){
  boolean undead=m!=null&&m.hasAttribute("undead");
  boolean salvei=has(names,"salve amulet(i)"), salveei=has(names,"salve amulet(ei)");
  boolean slayer=onTask&&(has(names,"slayer helmet (i)")||has(names,"black mask (i)"));
  boolean mageHelm=has(names,"void mage helm"), gloves=has(names,"void knight gloves");
  boolean top=has(names,"void knight top")||has(names,"elite void top");
  boolean robe=has(names,"void knight robe")||has(names,"elite void robe");
  boolean elite=has(names,"elite void top")&&has(names,"elite void robe");
  double a=1,d=1;StringBuilder n=new StringBuilder();
  if(undead&&(salvei||salveei)){
   double b=salveei?1.20:1.15;a*=b;d*=b;n.append(salveei?"Salve(ei): +20% accuracy/damage":"Salve(i): +15% accuracy/damage");
   slayer=false; // Salve takes precedence and does not stack.
  } else if(slayer){a*=1.15;d*=1.15;n.append("Slayer imbue: +15% accuracy/damage");}
  if(mageHelm&&gloves&&top&&robe){a*=1.45;if(elite)d*=1.05;if(n.length()>0)n.append(" | ");n.append(elite?"Elite Void Mage: +45% accuracy, +5% damage":"Void Mage: +45% accuracy");}
  return new Effect(a,d,n.toString());
 }
 public static boolean synergy(EquipmentCandidate c){
  if(c==null||c.getItem()==null)return false;
  String n=c.getItem().getName().toLowerCase();
  return n.contains("salve amulet")||n.contains("slayer helmet")||n.contains("slayer helm")||n.contains("black mask")||
   n.contains("void mage helm")||n.contains("void knight gloves")||n.contains("void knight top")||
   n.contains("elite void top")||n.contains("void knight robe")||n.contains("elite void robe")||
   n.startsWith("virtus ")||n.contains("tome of fire")||n.contains("tome of water")||n.contains("tome of earth");
 }
 private static boolean has(Collection<String> g,String x){if(g==null)return false;for(String n:g)if(n!=null&&n.equalsIgnoreCase(x))return true;return false;}
}