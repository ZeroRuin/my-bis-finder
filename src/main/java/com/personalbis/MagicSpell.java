package com.personalbis;
import java.util.*;
public final class MagicSpell {
 public final String spellbook;
 public final String name; public final int level,maxHit; public final Map<String,Integer> runes;
 public final boolean markOfDarkness;
 public MagicSpell(String n,int l,int h,Object... rs){this(n,l,h,"standard",false,rs);}
 private MagicSpell(String n,int l,int h,String book,boolean mark,Object[] rs){name=n;level=l;maxHit=h;spellbook=book;markOfDarkness=mark;runes=new LinkedHashMap<>();for(int i=0;i<rs.length;i+=2)runes.put((String)rs[i],(Integer)rs[i+1]);}
 private static MagicSpell ancientSpell(String n,int l,int h,Object... rs){return new MagicSpell(n,l,h,"ancient",false,rs);}
 private static MagicSpell arceuusSpell(String n,int l,int h,Object... rs){return new MagicSpell(n,l,h,"arceuus",false,rs);}
 private static MagicSpell markedDemonbane(String n,int l,int h,Object... rs){
  MagicSpell x=new MagicSpell(n+" + Mark of Darkness",l,h,"arceuus",true,rs);
  // Mark itself costs 1 Soul + 1 Cosmic rune. Treat one cast as required
  // availability; Recommended Inventory can surface the combined rune types.
  x.runes.put("Soul rune",x.runes.getOrDefault("Soul rune",0)+1);
  x.runes.put("Cosmic rune",1);
  return x;
 }
 public boolean isDemonbane(){return name.contains("Demonbane");}
 public String element(){String x=name.toLowerCase();if(x.startsWith("wind"))return "air";if(x.startsWith("water"))return "water";if(x.startsWith("earth"))return "earth";if(x.startsWith("fire"))return "fire";return "";}
 public String tier(){
  if(element().isEmpty())return "";
  String x=name.toLowerCase();
  if(x.endsWith(" strike"))return "strike";
  if(x.endsWith(" bolt"))return "bolt";
  if(x.endsWith(" blast"))return "blast";
  if(x.endsWith(" wave"))return "wave";
  if(x.endsWith(" surge"))return "surge";
  return "";
 }
 public int scaledBaseMax(int magicLevel){
  if("Magic Dart".equals(name)) return (magicLevel/10)+10;
  String t=tier();
  if(t.isEmpty()) return maxHit;
  int best=maxHit;
  // Since Project Rebalance, all elemental spells within the same tier scale
  // to the strongest spell of that tier unlocked by the player's Magic level.
  // Example: at 85-89 Magic, Wind Surge scales to Water Surge's base max 22.
  for(MagicSpell s:standard())
   if(s.level<=magicLevel && s.tier().equals(t) && !s.element().isEmpty())
    best=Math.max(best,s.maxHit);
  return best;
 }

 public static List<MagicSpell> ancient(){return Arrays.asList(
   ancientSpell("Smoke Rush",50,13,"Air rune",1,"Fire rune",1,"Chaos rune",2,"Death rune",2),
   ancientSpell("Shadow Rush",52,14,"Air rune",1,"Soul rune",1,"Chaos rune",2,"Death rune",2),
   ancientSpell("Blood Rush",56,15,"Blood rune",1,"Chaos rune",2,"Death rune",2),
   ancientSpell("Ice Rush",58,16,"Water rune",2,"Chaos rune",2,"Death rune",2),
   ancientSpell("Smoke Burst",62,17,"Air rune",2,"Fire rune",2,"Chaos rune",4,"Death rune",2),
   ancientSpell("Shadow Burst",64,18,"Air rune",1,"Soul rune",2,"Chaos rune",4,"Death rune",2),
   ancientSpell("Blood Burst",68,21,"Blood rune",2,"Chaos rune",4,"Death rune",2),
   ancientSpell("Ice Burst",70,22,"Water rune",4,"Chaos rune",4,"Death rune",2),
   ancientSpell("Smoke Blitz",74,23,"Air rune",2,"Fire rune",2,"Blood rune",2,"Death rune",2),
   ancientSpell("Shadow Blitz",76,24,"Air rune",2,"Soul rune",2,"Blood rune",2,"Death rune",2),
   ancientSpell("Blood Blitz",80,25,"Blood rune",4,"Death rune",2),
   ancientSpell("Ice Blitz",82,26,"Water rune",3,"Blood rune",2,"Death rune",2),
   ancientSpell("Smoke Barrage",86,27,"Air rune",4,"Fire rune",4,"Blood rune",2,"Death rune",4),
   ancientSpell("Shadow Barrage",88,28,"Air rune",4,"Soul rune",3,"Blood rune",2,"Death rune",4),
   ancientSpell("Blood Barrage",92,29,"Soul rune",1,"Blood rune",4,"Death rune",4),
   ancientSpell("Ice Barrage",94,30,"Water rune",6,"Blood rune",2,"Death rune",4));}
 public static List<MagicSpell> arceuus(){
  return Arrays.asList(
   arceuusSpell("Inferior Demonbane",44,16,"Fire rune",3,"Chaos rune",1),
   arceuusSpell("Superior Demonbane",62,23,"Fire rune",5,"Soul rune",1),
   arceuusSpell("Dark Demonbane",82,30,"Fire rune",7,"Soul rune",2),
   markedDemonbane("Superior Demonbane",62,23,"Fire rune",5,"Soul rune",1),
   markedDemonbane("Dark Demonbane",82,30,"Fire rune",7,"Soul rune",2));
 }
 public static List<MagicSpell> combat(){List<MagicSpell>x=new ArrayList<>(standard());x.addAll(ancient());x.addAll(arceuus());return x;}
 public static List<MagicSpell> standard(){
  return Arrays.asList(
   new MagicSpell("Wind Strike",1,2,"Air rune",1,"Mind rune",1),
   new MagicSpell("Water Strike",5,4,"Air rune",1,"Water rune",1,"Mind rune",1),
   new MagicSpell("Earth Strike",9,6,"Air rune",1,"Earth rune",2,"Mind rune",1),
   new MagicSpell("Fire Strike",13,8,"Air rune",2,"Fire rune",3,"Mind rune",1),
   new MagicSpell("Wind Bolt",17,9,"Air rune",2,"Chaos rune",1),
   new MagicSpell("Water Bolt",23,10,"Air rune",2,"Water rune",2,"Chaos rune",1),
   new MagicSpell("Earth Bolt",29,11,"Air rune",2,"Earth rune",3,"Chaos rune",1),
   new MagicSpell("Fire Bolt",35,12,"Air rune",3,"Fire rune",4,"Chaos rune",1),
   new MagicSpell("Wind Blast",41,13,"Air rune",3,"Death rune",1),
   new MagicSpell("Water Blast",47,14,"Air rune",3,"Water rune",3,"Death rune",1),
   new MagicSpell("Earth Blast",53,15,"Air rune",3,"Earth rune",4,"Death rune",1),
   new MagicSpell("Fire Blast",59,16,"Air rune",4,"Fire rune",5,"Death rune",1),
   new MagicSpell("Wind Wave",62,17,"Air rune",5,"Blood rune",1),
   new MagicSpell("Water Wave",65,18,"Air rune",5,"Water rune",7,"Blood rune",1),
   new MagicSpell("Earth Wave",70,19,"Air rune",5,"Earth rune",7,"Blood rune",1),
   new MagicSpell("Fire Wave",75,20,"Air rune",5,"Fire rune",7,"Blood rune",1),
   new MagicSpell("Wind Surge",81,21,"Air rune",7,"Wrath rune",1),
   new MagicSpell("Water Surge",85,22,"Air rune",7,"Water rune",10,"Wrath rune",1),
   new MagicSpell("Earth Surge",90,23,"Air rune",7,"Earth rune",10,"Wrath rune",1),
   new MagicSpell("Fire Surge",95,24,"Air rune",7,"Fire rune",10,"Wrath rune",1),
   new MagicSpell("Iban Blast",50,25,"Fire rune",5,"Death rune",1),
   new MagicSpell("Magic Dart",50,0,"Mind rune",4,"Death rune",1),
   new MagicSpell("Saradomin Strike",60,20,"Air rune",4,"Fire rune",2,"Blood rune",2),
   new MagicSpell("Claws of Guthix",60,20,"Air rune",4,"Fire rune",1,"Blood rune",2),
   new MagicSpell("Flames of Zamorak",60,20,"Air rune",4,"Fire rune",4,"Blood rune",2));
 }
}