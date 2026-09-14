package com.personalbis;
import java.util.*;
public final class MagicSpellCompatibility {
 private MagicSpellCompatibility(){}
 public static boolean canAutocast(String rawWeapon,MagicSpell spell){
  if(rawWeapon==null||spell==null)return false;String w=rawWeapon.toLowerCase();String n=spell.name;
  if(PoweredStaff.forWeapon(w)!=null||PoweredStaff.requiresContext(w))return false;
  if("ancient".equals(spell.spellbook))return canAutocastAncient(w);
  if("arceuus".equals(spell.spellbook))return canAutocastArceuus(w);
  if("Iban Blast".equals(n))return one(w,"iban's staff","iban's staff (u)");
  if("Magic Dart".equals(n))return one(w,"slayer's staff","slayer's staff (e)","staff of the dead","toxic staff of the dead","staff of light","staff of balance");
  if("Saradomin Strike".equals(n))return one(w,"saradomin staff","staff of light");
  if("Claws of Guthix".equals(n))return one(w,"guthix staff","void knight mace","staff of balance");
  if("Flames of Zamorak".equals(n))return one(w,"zamorak staff","staff of the dead","toxic staff of the dead");
  if(!spell.element().isEmpty())return w.contains("staff")||w.contains("battlestaff")||w.contains("wand")
    ||w.contains("ancient sceptre")||w.contains("skull sceptre")||w.equals("blue moon spear");
  return false;
 }
 public static int attackTicks(String rawWeapon,MagicSpell spell){
  String w=rawWeapon==null?"":rawWeapon.toLowerCase();
  // Wiki Equipment.calculateAttackSpeed(): cast stances are normally 5t.
  // Harmonised is 4t only for STANDARD spellbook autocasts; Twinflame is 6t.
  if(w.equals("harmonised nightmare staff") && spell!=null && "standard".equals(spell.spellbook)) return 4;
  if(w.equals("twinflame staff")) return 6;
  return 5;
 }
 public static String note(String rawWeapon,MagicSpell spell){
  if(rawWeapon!=null&&rawWeapon.equalsIgnoreCase("Harmonised nightmare staff")&&"standard".equals(spell.spellbook))return "Harmonised: standard spell autocast 4t";
  if(rawWeapon!=null&&rawWeapon.equalsIgnoreCase("Twinflame staff"))return "Twinflame: spellcasting attack speed 6t";
  if("ancient".equals(spell.spellbook))return AncientSpellEffects.note(spell,rawWeapon);
  if("arceuus".equals(spell.spellbook))return spell.markOfDarkness?"Arceuus Demonbane + Mark of Darkness":"Arceuus Demonbane";
  if("Iban Blast".equals(spell.name))return "Requires Iban's staff";
  if("Magic Dart".equals(spell.name))return "Magic Dart: level-scaled max hit";
  if(spell.name.equals("Saradomin Strike")||spell.name.equals("Claws of Guthix")||spell.name.equals("Flames of Zamorak"))return "God spell: weapon-restricted; Charge not assumed";
  return "";
 }
 public static boolean canAutocastArceuus(String w){
  return w.equals("skull sceptre (i)")||w.equals("slayer's staff")||w.equals("slayer's staff (e)")
    ||w.equals("ahrim's staff")||w.equals("blue moon spear")||w.equals("staff of the dead")
    ||w.equals("toxic staff of the dead")||w.equals("purging staff")||w.equals("master wand")
    ||w.equals("kodai wand");
 }
 public static boolean canAutocastAncient(String w){
  return w.equals("ancient staff")||w.contains("ancient sceptre")||w.equals("master wand")||w.equals("kodai wand")
    ||w.equals("nightmare staff")||w.equals("eldritch nightmare staff")||w.equals("volatile nightmare staff")
    ||w.equals("harmonised nightmare staff")||w.equals("thammaron's sceptre (a)")||w.equals("accursed sceptre (a)")
    ||w.equals("blue moon spear");
 }
 private static boolean one(String w,String... names){for(String n:names)if(w.equals(n))return true;return false;}
}