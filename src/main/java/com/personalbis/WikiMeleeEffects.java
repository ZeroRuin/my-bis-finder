package com.personalbis;

import java.util.Collection;

/** Source-ordered sustained melee Wilderness and exclusive-necklace modifiers. */
public final class WikiMeleeEffects
{
    private WikiMeleeEffects() {}


    public static boolean avariceRevenant(Collection<EquipmentCandidate> gear, MonsterDefinition monster)
    {
        return monster != null && monster.getName().toLowerCase().startsWith("revenant")
            && has(gear, "Amulet of avarice");
    }

    public static int accuracyAfterAvarice(int roll, Collection<EquipmentCandidate> gear,
        MonsterDefinition monster)
    {
        return avariceRevenant(gear, monster) ? WikiIntegerStages.factor(roll, 24, 20) : roll;
    }

    public static int maxAfterAvarice(int maxHit, Collection<EquipmentCandidate> gear,
        MonsterDefinition monster)
    {
        return avariceRevenant(gear, monster) ? WikiIntegerStages.factor(maxHit, 24, 20) : maxHit;
    }

    /** Crystal blessing applies only to melee max hit and counts helm/body/legs as 1/3/2 pieces. */
    public static int maxAfterCrystalBlessing(int maxHit, Collection<EquipmentCandidate> gear)
    {
        if (!has(gear, "Crystal blessing")) return maxHit;
        int pieces = has(gear, "Crystal helm") ? 1 : 0;
        pieces += has(gear, "Crystal body") ? 3 : 0;
        pieces += has(gear, "Crystal legs") ? 2 : 0;
        return WikiIntegerStages.factor(maxHit, 40 + pieces, 40);
    }

    public static boolean isChargedRevWeapon(EquipmentCandidate weapon, MonsterDefinition monster)
    {
        if (weapon == null || weapon.getItem() == null || !WikiRangedEffects.isWildernessTarget(monster))
        {
            return false;
        }
        int id = weapon.getItem().getItemId();
        return id == 22545 || id == 27660;
    }

    public static int postAccuracy(int roll, EquipmentCandidate weapon, MonsterDefinition monster)
    {
        return isChargedRevWeapon(weapon, monster) ? WikiIntegerStages.factor(roll, 3, 2) : roll;
    }

    public static int postDamage(int maxHit, EquipmentCandidate weapon, MonsterDefinition monster)
    {
        return isChargedRevWeapon(weapon, monster) ? WikiIntegerStages.factor(maxHit, 3, 2) : maxHit;
    }

    private static boolean has(Collection<EquipmentCandidate> gear, String name)
    {
        if (gear != null)
        {
            for (EquipmentCandidate candidate : gear)
            {
                if (candidate != null && candidate.getItem() != null
                    && candidate.getItem().getName().equalsIgnoreCase(name))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
