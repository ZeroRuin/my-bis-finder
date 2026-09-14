package com.personalbis;

public final class ElementalWeaknessEffects
{
    private ElementalWeaknessEffects(){}

    public static double multiplier(MonsterDefinition monster, MagicSpell spell)
    {
        if (monster == null || spell == null || !"standard".equals(spell.spellbook)
            || spell.element().isEmpty() || spell.tier().isEmpty() || !monster.weakTo(spell.element()))
            return 0.0;
        return monster.getWeaknessSeverity() / 100.0;
    }

    public static boolean applies(MonsterDefinition monster, MagicSpell spell)
    {
        return multiplier(monster, spell) > 0.0;
    }
}
