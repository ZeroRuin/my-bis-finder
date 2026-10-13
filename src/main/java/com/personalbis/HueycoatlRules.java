package com.personalbis;

import java.util.Collection;
import com.personalbis.wiki.WikiEquipment;

/** Encounter parts and per-hitsplat tail transforms from the Wiki DPS calculator. */
public final class HueycoatlRules
{
    private HueycoatlRules() {}

    static boolean nonAttackable(int id) { return id == 14011 || id == 14015; }
    static String part(int id, String version)
    {
        if (id == 14017) return "Body (phase 1)";
        if (id == 14009 || id == 14010 || id == 14013) return "Head";
        if (id == 14014) return "Tail";
        return version;
    }

    /** Crush must be strictly higher than all other offensive bonuses, across all gear. */
    static boolean qualifyingCrush(AttackStyle style, Collection<EquipmentCandidate> gear)
    {
        if (style != AttackStyle.MELEE_CRUSH || gear == null) return false;
        int stab = 0, slash = 0, crush = 0, ranged = 0, magic = 0;
        for (EquipmentCandidate c : gear)
        {
            if (c == null) continue;
            WikiEquipment item = WikiEquipmentEngine.canonicalItem(c.getItem().getItemId());
            if (item == null) return false; // Do not infer other attack bonuses from a style-specific candidate.
            if (item.offensive == null) continue;
            stab += item.offensive.stab;
            slash += item.offensive.slash;
            crush += item.offensive.crush;
            ranged += item.offensive.ranged;
            magic += item.offensive.magic;
        }
        return crush > stab && crush > slash && crush > ranged && crush > magic;
    }

    static HitDistribution tail(HitDistribution d, MonsterDefinition monster,
        AttackStyle style, String element, boolean qualifyingCrush)
    {
        if (monster == null || monster.getId() != 14014) return d;
        boolean crush = style == AttackStyle.MELEE_CRUSH && qualifyingCrush;
        d = d.transform(HitDistribution.linearMin(crush ? 9 : 4, 0));
        if (crush) d = d.transform(h -> h.damage > 0
            ? HitDistribution.single(h.damage, h.accurate)
            : HitDistribution.single(1, true));
        return d.flatten();
    }
}
