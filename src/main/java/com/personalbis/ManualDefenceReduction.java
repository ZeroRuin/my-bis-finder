package com.personalbis;

import java.util.*;

/** A single explicitly selected pre-applied drain; no stacking or rotation model.
 * Formula/floors: Wiki DPS calculator scaling/DefenceReduction.ts, 89c3e25b.
 */
public final class ManualDefenceReduction
{
    public static final String CONFIG_KEY="manualDefenceReduction";
    private static final String NOTE="manual_defence_note:";
    private ManualDefenceReduction() {}

    public enum Choice
    {
        OFF("Off",0,0), DWH("Dragon warhammer",30,13576),
        MAUL("Elder maul",35,21003);
        public final String label;
        private final int percent;
        public final int weaponId;
        Choice(String label,int percent,int weaponId){this.label=label;this.percent=percent;this.weaponId=weaponId;}
        @Override public String toString(){return label;}
    }
    public static Choice decode(String raw)
    {try{return Choice.valueOf(raw);}catch(IllegalArgumentException|NullPointerException ignored){return Choice.OFF;}}

    public static Set<Choice> owned(List<BankItem> items)
    {
        Set<Choice> result=EnumSet.noneOf(Choice.class);
        if(items!=null)for(BankItem item:items)if(item!=null&&item.getQuantity()>0)
        {
            // Canonical base and ornament variants in the bundled equipment aliases.
            if(item.getItemId()==13576||item.getItemId()==26710||item.getItemId()==28035)result.add(Choice.DWH);
            if(item.getItemId()==21003||item.getItemId()==27100)result.add(Choice.MAUL);
        }
        return result;
    }

    public static Choice revalidate(Choice selected,Set<Choice> owned)
    {return selected!=null&&(selected==Choice.OFF||owned.contains(selected))?selected:Choice.OFF;}

    public static boolean immune(MonsterDefinition m)
    {
        if(m==null)return true;
        int id=m.getId();
        return "Vardorvis".equals(m.getName())||"Verzik Vitur".equals(m.getName())
            ||contains(id,12223,12224,12228,12425,12426,13656,10830,10831,10832,8369,8370,8371,
                10847,10848,10849,10833,10834,10835,8372,8373,8374,10850,10851,10852);
    }

    public static int floor(MonsterDefinition m)
    {
        if(m==null)return 0;
        int id=m.getId();
        int tfaFloor=FracturedArchiveRules.floor(m);if(tfaFloor>=0)return tfaFloor;
        if(immune(m))return Math.max(0,VardorvisRules.startingDefence(m));
        if(contains(id,8387,8388,10867,10868))return 100;
        if(contains(id,378,9425,9426,9427,9428,9429,9430,9431,9432,9433,9460,
            377,9423,9416,9417,9418,9419,9420,9421,9422,9424,11153,11154,11155))return 120;
        if(contains(id,11789,11790,11791,11792,11793,11794,11795,11796))return 70;
        if(contains(id,11778,11779,11780,11719,11721,11751,11750,11752))return 60;
        if(contains(id,11730,11732,11733))return 50;
        if(contains(id,11761,11763,11762,11764))return 120;
        if(contains(id,11278,11279,11280,11281,11282))return 250;
        if(id==13668)return 90;
        if(contains(id,14009,14010,14013,14017,14014))return 120;
        if(id==14176)return 145;
        return 0;
    }

    private static boolean contains(int id,int... values)
    {for(int value:values)if(id==value)return true;return false;}

    public static int reducedLevel(int current,int floor,Choice selected)
    {
        int level=Math.max(0,current);
        if(selected==null||selected==Choice.OFF)return level;
        // Subtract the floored loss, not floor(level * remaining fraction).
        return Math.max(Math.min(level,Math.max(0,floor)),level-(int)((long)level*selected.percent/100));
    }

    public static MonsterDefinition apply(MonsterDefinition m,Choice selected,boolean canLand)
    {
        if(m==null||selected==null||selected==Choice.OFF)return m;
        List<String> attributes=new ArrayList<>(m.getAttributes());attributes.removeIf(a->a.startsWith(NOTE));
        int before=m.getDefenceLevel(),after=before;
        String note;
        if(immune(m))note=selected.label+" not applied: target is immune to Defence drains";
        else if(!canLand)note=selected.label+" not applied: weapon unavailable, excluded, unusable or unable to damage this target";
        else
        {
            after=reducedLevel(before,floor(m),selected);
            note=selected.label+", one damaging special: Defence "+before+" → "+after
                +". Pre-applied full-HP comparison, fixed for this estimate; no recovery, repeats, special damage, preparation time or energy cost.";
        }
        attributes.add(NOTE+note);
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),after,m.getMagicLevel(),m.getRangedLevel(),m.getHitpoints(),
            m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),m.getMagicDefence(),m.getLightDefence(),m.getStandardDefence(),m.getHeavyDefence(),
            attributes,m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }

    public static String note(MonsterDefinition m)
    {
        if(m!=null)for(String attribute:m.getAttributes())if(attribute.startsWith(NOTE))return attribute.substring(NOTE.length());
        return "";
    }
}
