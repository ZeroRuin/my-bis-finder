package com.personalbis;

import net.runelite.api.SpriteID;

/** Explicit combat spell sprites: Plugin Hub does not permit reflective field lookup. */
final class SpellSpriteIds
{
    private SpellSpriteIds() {}
    static int forSpell(String name)
    {
        if(name==null)return -1;
        String spell=name.replace(" + Mark of Darkness", "");
        switch(spell)
        {
            case "Smoke Rush": return SpriteID.SPELL_SMOKE_RUSH;
            case "Shadow Rush": return SpriteID.SPELL_SHADOW_RUSH;
            case "Blood Rush": return SpriteID.SPELL_BLOOD_RUSH;
            case "Ice Rush": return SpriteID.SPELL_ICE_RUSH;
            case "Smoke Burst": return SpriteID.SPELL_SMOKE_BURST;
            case "Shadow Burst": return SpriteID.SPELL_SHADOW_BURST;
            case "Blood Burst": return SpriteID.SPELL_BLOOD_BURST;
            case "Ice Burst": return SpriteID.SPELL_ICE_BURST;
            case "Smoke Blitz": return SpriteID.SPELL_SMOKE_BLITZ;
            case "Shadow Blitz": return SpriteID.SPELL_SHADOW_BLITZ;
            case "Blood Blitz": return SpriteID.SPELL_BLOOD_BLITZ;
            case "Ice Blitz": return SpriteID.SPELL_ICE_BLITZ;
            case "Smoke Barrage": return SpriteID.SPELL_SMOKE_BARRAGE;
            case "Shadow Barrage": return SpriteID.SPELL_SHADOW_BARRAGE;
            case "Blood Barrage": return SpriteID.SPELL_BLOOD_BARRAGE;
            case "Ice Barrage": return SpriteID.SPELL_ICE_BARRAGE;
            case "Inferior Demonbane": return SpriteID.SPELL_INFERIOR_DEMONBANE;
            case "Superior Demonbane": return SpriteID.SPELL_SUPERIOR_DEMONBANE;
            case "Dark Demonbane": return SpriteID.SPELL_DARK_DEMONBANE;
            case "Wind Strike": return SpriteID.SPELL_WIND_STRIKE;
            case "Water Strike": return SpriteID.SPELL_WATER_STRIKE;
            case "Earth Strike": return SpriteID.SPELL_EARTH_STRIKE;
            case "Fire Strike": return SpriteID.SPELL_FIRE_STRIKE;
            case "Wind Bolt": return SpriteID.SPELL_WIND_BOLT;
            case "Water Bolt": return SpriteID.SPELL_WATER_BOLT;
            case "Earth Bolt": return SpriteID.SPELL_EARTH_BOLT;
            case "Fire Bolt": return SpriteID.SPELL_FIRE_BOLT;
            case "Wind Blast": return SpriteID.SPELL_WIND_BLAST;
            case "Water Blast": return SpriteID.SPELL_WATER_BLAST;
            case "Earth Blast": return SpriteID.SPELL_EARTH_BLAST;
            case "Fire Blast": return SpriteID.SPELL_FIRE_BLAST;
            case "Wind Wave": return SpriteID.SPELL_WIND_WAVE;
            case "Water Wave": return SpriteID.SPELL_WATER_WAVE;
            case "Earth Wave": return SpriteID.SPELL_EARTH_WAVE;
            case "Fire Wave": return SpriteID.SPELL_FIRE_WAVE;
            case "Wind Surge": return SpriteID.SPELL_WIND_SURGE;
            case "Water Surge": return SpriteID.SPELL_WATER_SURGE;
            case "Earth Surge": return SpriteID.SPELL_EARTH_SURGE;
            case "Fire Surge": return SpriteID.SPELL_FIRE_SURGE;
            case "Iban Blast": return SpriteID.SPELL_IBAN_BLAST;
            case "Magic Dart": return SpriteID.SPELL_MAGIC_DART;
            case "Saradomin Strike": return SpriteID.SPELL_SARADOMIN_STRIKE;
            case "Claws of Guthix": return SpriteID.SPELL_CLAWS_OF_GUTHIX;
            case "Flames of Zamorak": return SpriteID.SPELL_FLAMES_OF_ZAMORAK;
            default: return -1; // Powered attacks and unknown spells use the weapon icon.
        }
    }
}
