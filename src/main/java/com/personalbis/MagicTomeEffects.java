package com.personalbis;
import java.util.*;
public final class MagicTomeEffects {
 private MagicTomeEffects(){}
 public static final class Effect {
  public final double accuracy, damage; public final String rune, note;
  Effect(double a,double d,String r,String n){accuracy=a;damage=d;rune=r;note=n;}
 }
 public static Effect evaluate(Collection<EquipmentCandidate> gear,MagicSpell spell){
  String tome=""; if(gear!=null)for(EquipmentCandidate e:gear)if(e!=null&&e.getItem()!=null){
   String n=e.getItem().getName().toLowerCase();
   if(n.equals("tome of fire")||n.equals("tome of water")||n.equals("tome of earth"))tome=n;
  }
  return evaluateName(tome,spell);
 }
 static Effect evaluateName(String tome,MagicSpell spell){
  String t=tome==null?"":tome.toLowerCase(); String el=spell==null?"":spell.element();
  if(t.equals("tome of fire")) return new Effect(1.0, "fire".equals(el)&&isStandardElemental(spell)?1.10:1.0,"fire rune",
    "Tome of fire: unlimited fire runes"+("fire".equals(el)&&isStandardElemental(spell)?", +10% fire damage":""));
  if(t.equals("tome of water")) return new Effect("water".equals(el)&&isStandardElemental(spell)?1.20:1.0,
    "water".equals(el)&&isStandardElemental(spell)?1.10:1.0,"water rune",
    "Tome of water: unlimited water runes"+("water".equals(el)&&isStandardElemental(spell)?", +10% water accuracy/damage":""));
  if(t.equals("tome of earth")) return new Effect(1.0, "earth".equals(el)&&isStandardElemental(spell)?1.10:1.0,"earth rune",
    "Tome of earth: unlimited earth runes"+("earth".equals(el)&&isStandardElemental(spell)?", +10% earth damage":""));
  return new Effect(1.0,1.0,"","");
 }
 private static boolean isStandardElemental(MagicSpell s){return s!=null&&"standard".equals(s.spellbook)&&!s.element().isEmpty();}
 public static boolean providesRune(Collection<EquipmentCandidate> gear,String rune){
  if(gear==null)return false;for(EquipmentCandidate e:gear)if(e!=null&&e.getItem()!=null){
   String n=e.getItem().getName().toLowerCase();
   if(n.equals("tome of fire")&&rune.equals("fire rune"))return true;
   if(n.equals("tome of water")&&rune.equals("water rune"))return true;
   if(n.equals("tome of earth")&&rune.equals("earth rune"))return true;
  }return false;
 }
}
