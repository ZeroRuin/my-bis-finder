package com.personalbis;

import net.runelite.api.gameval.SpriteID;

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
            case "Smoke Rush": return SpriteID.Magicon2.SMOKE_RUSH;
            case "Shadow Rush": return SpriteID.Magicon2.SHADOW_RUSH;
            case "Blood Rush": return SpriteID.Magicon2.BLOOD_RUSH;
            case "Ice Rush": return SpriteID.Magicon2.ICE_RUSH;
            case "Smoke Burst": return SpriteID.Magicon2.SMOKE_BURST;
            case "Shadow Burst": return SpriteID.Magicon2.SHADOW_BURST;
            case "Blood Burst": return SpriteID.Magicon2.BLOOD_BURST;
            case "Ice Burst": return SpriteID.Magicon2.ICE_BURST;
            case "Smoke Blitz": return SpriteID.Magicon2.SMOKE_BLITZ;
            case "Shadow Blitz": return SpriteID.Magicon2.SHADOW_BLITZ;
            case "Blood Blitz": return SpriteID.Magicon2.BLOOD_BLITZ;
            case "Ice Blitz": return SpriteID.Magicon2.ICE_BLITZ;
            case "Smoke Barrage": return SpriteID.Magicon2.SMOKE_BARRAGE;
            case "Shadow Barrage": return SpriteID.Magicon2.SHADOW_BARRAGE;
            case "Blood Barrage": return SpriteID.Magicon2.BLOOD_BARRAGE;
            case "Ice Barrage": return SpriteID.Magicon2.ICE_BARRAGE;
            case "Inferior Demonbane": return SpriteID.MagicNecroOn.INFERIOR_DEMONBANE;
            case "Superior Demonbane": return SpriteID.MagicNecroOn.SUPERIOR_DEMONBANE;
            case "Dark Demonbane": return SpriteID.MagicNecroOn.DARK_DEMONBANE;
            case "Wind Strike": return SpriteID.Magicon.WIND_STRIKE;
            case "Water Strike": return SpriteID.Magicon.WATER_STRIKE;
            case "Earth Strike": return SpriteID.Magicon.EARTH_STRIKE;
            case "Fire Strike": return SpriteID.Magicon.FIRE_STRIKE;
            case "Wind Bolt": return SpriteID.Magicon.WIND_BOLT;
            case "Water Bolt": return SpriteID.Magicon.WATER_BOLT;
            case "Earth Bolt": return SpriteID.Magicon.EARTH_BOLT;
            case "Fire Bolt": return SpriteID.Magicon.FIRE_BOLT;
            case "Wind Blast": return SpriteID.Magicon.WIND_BLAST;
            case "Water Blast": return SpriteID.Magicon.WATER_BLAST;
            case "Earth Blast": return SpriteID.Magicon.EARTH_BLAST;
            case "Fire Blast": return SpriteID.Magicon.FIRE_BLAST;
            case "Wind Wave": return SpriteID.Magicon.WIND_WAVE;
            case "Water Wave": return SpriteID.Magicon.WATER_WAVE;
            case "Earth Wave": return SpriteID.Magicon.EARTH_WAVE;
            case "Fire Wave": return SpriteID.Magicon.FIRE_WAVE;
            case "Wind Surge": return SpriteID.Magicon2.WIND_SURGE;
            case "Water Surge": return SpriteID.Magicon2.WATER_SURGE;
            case "Earth Surge": return SpriteID.Magicon2.EARTH_SURGE;
            case "Fire Surge": return SpriteID.Magicon2.FIRE_SURGE;
            case "Iban Blast": return SpriteID.Magicon.IBAN_BLAST;
            case "Magic Dart": return SpriteID.Magicon2.MAGIC_DART;
            case "Saradomin Strike": return SpriteID.Magicon.SARADOMIN_STRIKE;
            case "Claws of Guthix": return SpriteID.Magicon.CLAWS_OF_GUTHIX;
            case "Flames of Zamorak": return SpriteID.Magicon.FLAMES_OF_ZAMORAK;
            default: return -1; // Powered attacks and unknown spells use the weapon icon.
        }
    }
}
