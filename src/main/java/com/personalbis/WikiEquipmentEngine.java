package com.personalbis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.personalbis.wiki.WikiEquipment;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Java parity port of the OSRS Wiki DPS calculator's src/lib/Equipment.ts.
 *
 * PBIS owns bank/unlock/optimizer policy; this class owns Wiki equipment semantics:
 * canonical aliases, exact ranged-ammo applicability, equipment-specific aggregate
 * transforms, and the equipment-derived attack-speed rules used by combat calculators.
 */
public final class WikiEquipmentEngine {
 public enum AmmoApplicability { INCLUDED, ALLOWED, INVALID }
 public static final class Totals {
  public int str, rangedStr, magicAttack, rangedAttack;
  public double magicDamage;
  Totals(int s,int rs,int ma,double md,int ra){str=s;rangedStr=rs;magicAttack=ma;magicDamage=md;rangedAttack=ra;}
 }
 /** Full Wiki Equipment.ts aggregate, retaining all offensive/defensive/bonus stages. */
 public static final class EquipmentBonuses {
  public int str,rangedStr,prayer,stabAttack,slashAttack,crushAttack,magicAttack,rangedAttack;
  public int stabDefence,slashDefence,crushDefence,magicDefence,rangedDefence,attackSpeed;
  /** PBIS fractional form: Wiki magic_str 50 == 0.05. */ public double magicDamage;
 }
 public static final Set<String> NO_STAT_EXCEPTIONS=Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
  "Castle wars bracelet","Lightbearer","Ring of recoil","Phoenix necklace","Reinforced goggles","Expeditious bracelet",
  "Bracelet of slaughter","Facemask","Earmuffs","Bug lantern","Nose peg","Efaritay's aid","Inoculation bracelet",
  "Bracelet of ethereum","Atlatl dart")));
 public static final Map<String,Integer> WEAPON_SPEC_COSTS;
 public static final Set<Integer> GAUNTLET_EQUIPMENT_IDS;
 public static final Set<Integer> CORRUPTED_GAUNTLET_EQUIPMENT_IDS;
 private static final Map<Integer,Integer> CANONICAL=new HashMap<>();
 private static final Map<Integer,List<Integer>> AMMO=new HashMap<>();
 private static final Map<Integer,WikiEquipment> EQUIPMENT=new HashMap<>();
 private static final Set<Integer> TOA=new HashSet<>(Arrays.asList(
  11789,11790,11791,11792,11793,11794,11795,11796,11797,11798,11799,
  11778,11779,11780,11719,11721,11724,11725,11726,11730,11732,11733,
  11751,11750,11752,11753,11754,11756,11757,11755,11758,11761,11763,11762,11764));
 static {
  Map<String,Integer> specs=new LinkedHashMap<>();
  String[] specData={"Abyssal dagger:25","Dragon dagger:25","Dragon longsword:25","Dragon mace:25","Osmumten's fang:25","Osmumten's fang (or):25","Dual macuahuitl:25","Scorching bow:25","Dragon knife:25","Purging staff:25","Rosewood blowpipe:25","Dawnbringer:30","Dragon halberd:30","Crystal halberd:30","Burning claws:35","Arkan blade:30","Magic longbow:35","Magic comp bow:35","Dragon sword:40","Elder maul:50","Dragon warhammer:50","Bandos godsword:50","Saradomin godsword:50","Accursed sceptre:50","Accursed sceptre (a):50","Arclight:50","Emberlight:50","Tonalztics of Ralos:50","Dragon claws:50","Voidwaker:50","Toxic blowpipe:50","Blazing blowpipe:50","Webweaver bow:50","Magic shortbow (i):50","Ancient godsword:50","Armadyl godsword:50","Zamorak godsword:50","Abyssal bludgeon:50","Abyssal whip:50","Barrelchest anchor:50","Eye of Ayak:50","Crimson kisten:50","Sunspear:50","Magic shortbow:55","Dark bow:55","Eldritch Nightmare staff:55","Volatile Nightmare staff:55","Dragon scimitar:55","Granite hammer:60","Heavy ballista:65","Light ballista:65","Saradomin's blessed sword:65","Brine sabre:75","Zaryte crossbow:75","Saradomin sword:100","Seercull:100"};
  for(String x:specData){int k=x.lastIndexOf(':');specs.put(x.substring(0,k),Integer.parseInt(x.substring(k+1)));} WEAPON_SPEC_COSTS=Collections.unmodifiableMap(specs);
  GAUNTLET_EQUIPMENT_IDS=Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(23861,23862,23863,23864,23886,23887,23888,23889,23890,23891,23892,23893,23894,23895,23896,23897,23898,23899,23900,23901,23902,23903)));
  CORRUPTED_GAUNTLET_EQUIPMENT_IDS=Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(23820,23821,23822,23823,23840,23841,23842,23843,23844,23845,23846,23847,23848,23849,23850,23851,23852,23853,23854,23855,23856,23857)));
  Gson g=new Gson();
  try {
   Type at=new TypeToken<Map<String,List<Integer>>>(){}.getType();
   Map<String,List<Integer>> aliases=g.fromJson(new InputStreamReader(WikiEquipmentEngine.class.getResourceAsStream("/wiki-data/equipment-aliases.json"),StandardCharsets.UTF_8),at);
   for(Map.Entry<String,List<Integer>> e:aliases.entrySet()){int base=Integer.parseInt(e.getKey()); for(int v:e.getValue())CANONICAL.put(v,base);}
   Type mt=new TypeToken<Map<String,List<Integer>>>(){}.getType();
   Map<String,List<Integer>> ammo=g.fromJson(new InputStreamReader(WikiEquipmentEngine.class.getResourceAsStream("/wiki-data/ranged-ammo.json"),StandardCharsets.UTF_8),mt);
   for(Map.Entry<String,List<Integer>> e:ammo.entrySet())AMMO.put(Integer.parseInt(e.getKey()),e.getValue());
   Type et=new TypeToken<List<WikiEquipment>>(){}.getType();
   List<WikiEquipment> eq=g.fromJson(new InputStreamReader(WikiEquipmentEngine.class.getResourceAsStream("/wiki-data/equipment.json"),StandardCharsets.UTF_8),et);
   for(WikiEquipment w:eq) if(!EQUIPMENT.containsKey(w.id)||empty(EQUIPMENT.get(w.id).version)) EQUIPMENT.put(w.id,w);
  } catch(Exception ex){ throw new ExceptionInInitializerError(ex); }
 }
 private WikiEquipmentEngine(){}
 private static boolean empty(String s){return s==null||s.isEmpty();}
 public static int canonicalId(int id){return CANONICAL.getOrDefault(id,id);}
 public static WikiEquipment canonicalItem(int id){return EQUIPMENT.get(canonicalId(id));}
 public static AmmoApplicability ammoApplicability(int weaponId,Integer ammoId){
  int w=canonicalId(weaponId); List<Integer> valid=AMMO.get(w);
  if(valid==null||valid.isEmpty()) return AmmoApplicability.ALLOWED;
  if(ammoId!=null && valid.contains(canonicalId(ammoId))) return AmmoApplicability.INCLUDED;
  // Upstream lists ammo IDs directly; aliases are accepted as their canonical equivalent too.
  if(ammoId!=null && valid.contains(ammoId)) return AmmoApplicability.INCLUDED;
  return AmmoApplicability.INVALID;
 }
 public static boolean exactAmmoCompatible(EquipmentCandidate weapon,EquipmentCandidate ammo){
  if(weapon==null)return ammo==null;
  AmmoApplicability a=ammoApplicability(weapon.getItem().getItemId(),ammo==null?null:ammo.getItem().getItemId());
  return a!=AmmoApplicability.INVALID;
 }
 public static boolean isBlowpipeId(int id){return id==12926||id==28688||id==31575||id==31579||id==31583;}
 public static int transformMeleeAttack(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition monster,int attack){ return "keris partisan of amascut".equals(name(gear.get(EquipmentSlot.WEAPON)))&&!isToa(monster)?attack-50:attack; }
 public static Totals transform(Map<EquipmentSlot,EquipmentCandidate> gear,MonsterDefinition monster,String spellbook,
                                int str,int rangedStr,int magicAttack,double magicDamage,int rangedAttack){
  EquipmentCandidate weapon=gear.get(EquipmentSlot.WEAPON); String wn=name(weapon);
  if("keris partisan of amascut".equals(wn) && !isToa(monster)){str-=22;}
  if(("dinh's bulwark".equals(wn)||"dinh's blazing bulwark".equals(wn))){
   int defenceSum=0;
   for(EquipmentCandidate c:gear.values()){WikiEquipment w=item(c); if(w!=null&&w.defensive!=null)defenceSum+=w.defensive.stab+w.defensive.slash+w.defensive.crush+w.defensive.ranged;}
   str+=Math.max(0,((defenceSum-800)/12)-38);
  }
  if("ancient".equals(spellbook)){
   int pieces=0; for(EquipmentSlot s:Arrays.asList(EquipmentSlot.HEAD,EquipmentSlot.BODY,EquipmentSlot.LEGS)){String n=name(gear.get(s));if(n.contains("virtus"))pieces++;}
   magicDamage+=0.03*pieces;
  }
  if(isEliteVoidMage(gear))magicDamage+=0.05;
  if("tumeken's shadow".equals(wn)){
   int factor=isToa(monster)?4:3; magicAttack*=factor; magicDamage=Math.min(1.0,magicDamage*factor);
  }
  EquipmentCandidate cape=gear.get(EquipmentSlot.CAPE), ammo=gear.get(EquipmentSlot.AMMO);
  WikiEquipment cw=item(cape);
  boolean dizana=cw!=null && ("Dizana's max cape".equals(cw.name)||"Blessed Dizana's quiver".equals(cw.name)||("Dizana's quiver".equals(cw.name)&&"Charged".equals(cw.version)));
  if(dizana && weapon!=null && ammo!=null && ammoApplicability(weapon.getItem().getItemId(),ammo.getItem().getItemId())==AmmoApplicability.INCLUDED){rangedAttack+=10;rangedStr+=1;}
  return new Totals(str,rangedStr,magicAttack,magicDamage,rangedAttack);
 }
 /** Exact Equipment.ts attack-speed ordering. */
 public static int calculateAttackSpeed(int weaponSpeed,String weaponName,MonsterDefinition monster,boolean rangedRapid,boolean castStance,boolean standardSpellbook,boolean manualCast){
  int speed=weaponSpeed>0?weaponSpeed:4;
  if(rangedRapid)speed-=1;
  else if(castStance){if("Harmonised Nightmare staff".equals(weaponName)&&standardSpellbook&&!manualCast)speed=4;else if("Twinflame staff".equals(weaponName))speed=6;else speed=5;}
  if(monster!=null&&monster.getId()==7223&&!manualCast&&("Bone mace".equals(weaponName)||"Bone shortbow".equals(weaponName)||"Bone staff".equals(weaponName)))speed=1;
  return Math.max(speed,1);
 }
 /** Returns the Wiki offensive style name with the greatest value; ties keep stab->slash->crush->magic->ranged order. */
 public static String highestOffensiveStyle(int stab,int slash,int crush,int magic,int ranged){
  String best="stab";int v=stab;if(slash>v){best="slash";v=slash;}if(crush>v){best="crush";v=crush;}if(magic>v){best="magic";v=magic;}if(ranged>v)best="ranged";return best;
 }
 /**
  * Full data-driven Equipment.ts aggregation. item IDs are canonicalised before stats/ammo checks.
  * blowpipeDartId models the Wiki itemVars.blowpipeDartId field; null means no loaded dart.
  */
 public static EquipmentBonuses calculateEquipmentBonuses(Map<EquipmentSlot,Integer> ids,MonsterDefinition monster,String spellbook,
    boolean castStance,boolean manualCast,boolean rangedRapid,Integer blowpipeDartId){
  EquipmentBonuses t=new EquipmentBonuses();
  Integer weaponId=ids.get(EquipmentSlot.WEAPON),ammoId=ids.get(EquipmentSlot.AMMO); WikiEquipment weapon=weaponId==null?null:canonicalItem(weaponId);
  for(Map.Entry<EquipmentSlot,Integer> e:ids.entrySet()){WikiEquipment w=canonicalItem(e.getValue());if(w==null)continue;
   boolean apply=e.getKey()!=EquipmentSlot.AMMO||ammoApplicability(weaponId==null?-1:weaponId,e.getValue())==AmmoApplicability.INCLUDED;
   if(w.bonuses!=null){t.str+=w.bonuses.str;if(apply)t.rangedStr+=w.bonuses.ranged_str;t.magicDamage+=w.bonuses.magic_str/1000.0;t.prayer+=w.bonuses.prayer;}
   if(w.offensive!=null){t.stabAttack+=w.offensive.stab;t.slashAttack+=w.offensive.slash;t.crushAttack+=w.offensive.crush;t.magicAttack+=w.offensive.magic;if(apply)t.rangedAttack+=w.offensive.ranged;}
   if(w.defensive!=null){t.stabDefence+=w.defensive.stab;t.slashDefence+=w.defensive.slash;t.crushDefence+=w.defensive.crush;t.magicDefence+=w.defensive.magic;t.rangedDefence+=w.defensive.ranged;}
  }
  if(weapon!=null&&isBlowpipeId(weapon.id)&&blowpipeDartId!=null){WikiEquipment dart=canonicalItem(blowpipeDartId);if(dart!=null&&dart.bonuses!=null)t.rangedStr+=dart.bonuses.ranged_str;}
  String wn=weapon==null?"":weapon.name;
  if("Tumeken's shadow".equals(wn)&&!manualCast){int f=isToa(monster)?4:3;t.magicDamage=Math.min(1.0,t.magicDamage*f);t.magicAttack*=f;}
  if("Keris partisan of amascut".equals(wn)&&!isToa(monster)){t.str-=22;t.stabAttack-=50;}
  if("Dinh's bulwark".equals(wn)||"Dinh's blazing bulwark".equals(wn)){int ds=t.stabDefence+t.slashDefence+t.crushDefence+t.rangedDefence;t.str+=Math.max(0,((ds-800)/12)-38);}
  if("ancient".equals(spellbook)&&castStance){int pieces=0;for(EquipmentSlot s:Arrays.asList(EquipmentSlot.HEAD,EquipmentSlot.BODY,EquipmentSlot.LEGS)){Integer id=ids.get(s);WikiEquipment w=id==null?null:canonicalItem(id);if(w!=null&&w.name.contains("Virtus"))pieces++;}t.magicDamage+=.03*pieces;}
  if(named(ids,EquipmentSlot.HEAD,"Void mage helm")&&named(ids,EquipmentSlot.BODY,"Elite void top")&&named(ids,EquipmentSlot.LEGS,"Elite void robe")&&named(ids,EquipmentSlot.HANDS,"Void knight gloves"))t.magicDamage+=.05;
  Integer capeId=ids.get(EquipmentSlot.CAPE);WikiEquipment cape=capeId==null?null:canonicalItem(capeId);boolean dizana=cape!=null&&("Dizana's max cape".equals(cape.name)||"Blessed Dizana's quiver".equals(cape.name)||("Dizana's quiver".equals(cape.name)&&"Charged".equals(cape.version)));
  if(dizana&&weaponId!=null&&ammoId!=null&&ammoApplicability(weaponId,ammoId)==AmmoApplicability.INCLUDED){t.rangedAttack+=10;t.rangedStr+=1;}
  t.attackSpeed=calculateAttackSpeed(weapon==null?4:weapon.speed,wn,monster,rangedRapid,castStance,"standard".equals(spellbook),manualCast);return t;
 }
 private static boolean named(Map<EquipmentSlot,Integer> ids,EquipmentSlot slot,String n){Integer id=ids.get(slot);WikiEquipment w=id==null?null:canonicalItem(id);return w!=null&&n.equals(w.name);}
 private static boolean isEliteVoidMage(Map<EquipmentSlot,EquipmentCandidate> g){return "void mage helm".equals(name(g.get(EquipmentSlot.HEAD)))&&"elite void top".equals(name(g.get(EquipmentSlot.BODY)))&&"elite void robe".equals(name(g.get(EquipmentSlot.LEGS)))&&"void knight gloves".equals(name(g.get(EquipmentSlot.HANDS)));}
 private static boolean isToa(MonsterDefinition m){return m!=null&&TOA.contains(m.getId());}
 private static WikiEquipment item(EquipmentCandidate c){return c==null?null:canonicalItem(c.getItem().getItemId());}
 private static String name(EquipmentCandidate c){return c==null||c.getItem()==null||c.getItem().getName()==null?"":c.getItem().getName().toLowerCase();}
}
