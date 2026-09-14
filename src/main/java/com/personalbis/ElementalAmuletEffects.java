package com.personalbis;
import java.util.*;
public final class ElementalAmuletEffects {
 private ElementalAmuletEffects(){}
 public static final class Effect {
  public final int flatMaxHit; public final String note;
  Effect(int h,String n){flatMaxHit=h;note=n;}
 }
 public static Effect evaluate(Collection<EquipmentCandidate> gear,MagicSpell spell){
  String amulet="";if(gear!=null)for(EquipmentCandidate e:gear)if(e!=null&&e.getItem()!=null&&e.getSlot()==EquipmentSlot.NECK){
   String n=e.getItem().getName().toLowerCase();
   if(n.equals("amulet of air")||n.equals("amulet of water")||n.equals("amulet of earth")||n.equals("amulet of fire")||n.equals("elemental amulet"))amulet=n;
  }
  return evaluateName(amulet,spell);
 }
 static Effect evaluateName(String amulet,MagicSpell spell){
  if(spell==null||!"standard".equals(spell.spellbook)||spell.element().isEmpty())return new Effect(0,"");
  String a=amulet==null?"":amulet.toLowerCase(), el=spell.element().toLowerCase();
  boolean matching=a.equals("elemental amulet")||a.equals("amulet of "+el);
  if(!matching)return new Effect(0,"");
  String label=a.equals("elemental amulet")?"Elemental amulet":"Amulet of "+Character.toUpperCase(el.charAt(0))+el.substring(1);
  return new Effect(2,label+": +10 Magic accuracy, +2 "+el+" max hit");
 }
}
