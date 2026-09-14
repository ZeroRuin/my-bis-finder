package com.personalbis;

import java.util.Collections;
import java.util.List;

public final class MonsterDefinition
{
    private final int id; private final String name; private final int combatLevel;
    private final int defenceLevel; private final int magicLevel; private final int rangedLevel; private final int hitpoints; private final int magicAttack;
    private final int stabDefence, slashDefence, crushDefence, magicDefence, lightDefence, standardDefence, heavyDefence;
    private final List<String> attributes;
    private final int size;
    private final String weaknessElement; private final int weaknessSeverity;
    private final String version;

    public MonsterDefinition(int id, String name, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints)
    {
        this(id,name,combatLevel,defenceLevel,magicLevel,rangedLevel,hitpoints,
            0,0,0,0,0,0,0,Collections.emptyList(),1,null,0);
    }

    public MonsterDefinition(int id, String name, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints,
        int stabDefence, int slashDefence, int crushDefence, int magicDefence, int lightDefence, int standardDefence, int heavyDefence,
        List<String> attributes)
    {
        this(id,name,combatLevel,defenceLevel,magicLevel,rangedLevel,hitpoints,
            stabDefence,slashDefence,crushDefence,magicDefence,lightDefence,standardDefence,heavyDefence,
            attributes,1,null,0);
    }

    public MonsterDefinition(int id, String name, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints,
        int stabDefence, int slashDefence, int crushDefence, int magicDefence, int lightDefence, int standardDefence, int heavyDefence,
        List<String> attributes, int size)
    {
        this(id,name,combatLevel,defenceLevel,magicLevel,rangedLevel,hitpoints,
            stabDefence,slashDefence,crushDefence,magicDefence,lightDefence,standardDefence,heavyDefence,
            attributes,size,null,0,0);
    }

    public MonsterDefinition(int id, String name, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints,
        int stabDefence, int slashDefence, int crushDefence, int magicDefence, int lightDefence, int standardDefence, int heavyDefence,
        List<String> attributes, int size, String weaknessElement, int weaknessSeverity)
    {
        this(id,name,combatLevel,defenceLevel,magicLevel,rangedLevel,hitpoints,stabDefence,slashDefence,crushDefence,magicDefence,lightDefence,standardDefence,heavyDefence,attributes,size,weaknessElement,weaknessSeverity,0);
    }

    public MonsterDefinition(int id, String name, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints,
        int stabDefence, int slashDefence, int crushDefence, int magicDefence, int lightDefence, int standardDefence, int heavyDefence,
        List<String> attributes, int size, String weaknessElement, int weaknessSeverity, int magicAttack)
    {
        this.id=id; this.name=name; this.version=""; this.combatLevel=combatLevel; this.defenceLevel=defenceLevel; this.magicLevel=magicLevel;
        this.rangedLevel=rangedLevel; this.hitpoints=hitpoints; this.magicAttack=magicAttack; this.stabDefence=stabDefence; this.slashDefence=slashDefence;
        this.crushDefence=crushDefence; this.magicDefence=magicDefence; this.lightDefence=lightDefence;
        this.standardDefence=standardDefence; this.heavyDefence=heavyDefence;
        this.attributes=attributes == null ? Collections.emptyList() : Collections.unmodifiableList(attributes);
        this.size=Math.max(1,size);
        this.weaknessElement=weaknessElement==null?"":weaknessElement.toLowerCase();
        this.weaknessSeverity=Math.max(0,weaknessSeverity);
    }

    // alpha50.4.29: preserve Wiki variants that share name/level/stats (for example
    // Kalphite Queen Crawling/Airborne) instead of collapsing them into one target.
    public MonsterDefinition(int id, String name, String version, int combatLevel, int defenceLevel, int magicLevel, int rangedLevel, int hitpoints,
        int stabDefence, int slashDefence, int crushDefence, int magicDefence, int lightDefence, int standardDefence, int heavyDefence,
        List<String> attributes, int size, String weaknessElement, int weaknessSeverity, int magicAttack)
    {
        this.id=id; this.name=name; this.version=version==null?"":version.trim(); this.combatLevel=combatLevel; this.defenceLevel=defenceLevel; this.magicLevel=magicLevel;
        this.rangedLevel=rangedLevel; this.hitpoints=hitpoints; this.magicAttack=magicAttack; this.stabDefence=stabDefence; this.slashDefence=slashDefence;
        this.crushDefence=crushDefence; this.magicDefence=magicDefence; this.lightDefence=lightDefence; this.standardDefence=standardDefence; this.heavyDefence=heavyDefence;
        this.attributes=attributes == null ? Collections.emptyList() : Collections.unmodifiableList(attributes); this.size=Math.max(1,size);
        this.weaknessElement=weaknessElement==null?"":weaknessElement.toLowerCase(); this.weaknessSeverity=Math.max(0,weaknessSeverity);
    }

    public int getId(){return id;} public String getName(){return name;} public String getVersion(){return version;} public int getCombatLevel(){return combatLevel;}
    public int getDefenceLevel(){return defenceLevel;} public int getMagicLevel(){return magicLevel;} public int getRangedLevel(){return rangedLevel;}
    public int getHitpoints(){return hitpoints;} public int getMagicAttack(){return magicAttack;} public int getStabDefence(){return stabDefence;} public int getSlashDefence(){return slashDefence;}
    public int getCrushDefence(){return crushDefence;} public int getMagicDefence(){return magicDefence;}
    public int getLightDefence(){return lightDefence;} public int getStandardDefence(){return standardDefence;} public int getHeavyDefence(){return heavyDefence;}
    public List<String> getAttributes(){return attributes;} public int getSize(){return size;}
    public String getWeaknessElement(){return weaknessElement;} public int getWeaknessSeverity(){return weaknessSeverity;}
    public boolean weakTo(String element){return element!=null && weaknessSeverity>0 && weaknessElement.equalsIgnoreCase(element);}
    public int defenceFor(AttackStyle style){ switch(style){case MELEE_STAB:return stabDefence;case MELEE_SLASH:return slashDefence;case MELEE_CRUSH:return crushDefence;case MAGIC:return magicDefence;default:return standardDefence;} }
    public boolean hasAttribute(String a){ for(String x:attributes) if(x!=null && x.equalsIgnoreCase(a)) return true; return false; }
    /** Wiki defensive.flat_armour, encoded into the bundled attribute list during import. */
    public int getFlatArmour(){ for(String x:attributes){ if(x!=null && x.toLowerCase().startsWith("flat_armour:")){ try{return Integer.parseInt(x.substring(x.indexOf(':')+1));}catch(Exception ignored){} } } return 0; }
    @Override public String toString(){String v=version.isEmpty()?"":" — "+version; return combatLevel>0?name+v+" (level "+combatLevel+")":name+v;}
}
