package com.personalbis.wiki;

public final class WikiEquipment
{
    public String name;
    public int id;
    public String version;
    public String slot;
    public int speed;
    public String category;
    public Bonuses bonuses;
    public CombatStats offensive;
    public CombatStats defensive;
    public boolean isTwoHanded;

    public static final class Bonuses
    {
        public int str;
        public int ranged_str;
        public double magic_str;
        public int prayer;
    }

    public static final class CombatStats
    {
        public int stab;
        public int slash;
        public int crush;
        public int magic;
        public int ranged;
    }
}
