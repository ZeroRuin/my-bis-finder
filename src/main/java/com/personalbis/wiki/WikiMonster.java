package com.personalbis.wiki;

import java.util.Collections;
import java.util.List;

public final class WikiMonster
{
    public int id;
    public String name;
    public String version;
    public int level;
    public int speed;
    public int size;
    public Skills skills;
    public Defensive defensive;
    public Offensive offensive;
    public List<String> attributes = Collections.emptyList();
    public Weakness weakness;
    public boolean is_slayer_monster;

    public static final class Skills
    {
        public int atk;
        public int def;
        public int hp;
        public int magic;
        public int ranged;
        public int str;
    }

    public static final class Offensive
    {
        public int atk;
        public int magic;
        public int magic_str;
        public int ranged;
        public int ranged_str;
        public int str;
    }

    public static final class Defensive
    {
        public int flat_armour;
        public int stab;
        public int slash;
        public int crush;
        public int magic;
        public int light;
        public int standard;
        public int heavy;
    }

    public static final class Weakness
    {
        public String element;
        public int severity;
    }
}
