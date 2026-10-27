package com.personalbis;

import java.util.EnumSet;

/** Only verified ordinary offensive options; unknown weapons gain no fictional styles. */
public final class MeleeAttackStyleResolver
{
    private MeleeAttackStyleResolver() {}
    public static EnumSet<MeleeStance> legalStances(EquipmentCandidate weapon,AttackStyle style)
    {
        if(style!=AttackStyle.MELEE_STAB&&style!=AttackStyle.MELEE_SLASH&&style!=AttackStyle.MELEE_CRUSH)
            return EnumSet.noneOf(MeleeStance.class);
        if(weapon!=null&&weapon.getItem().getItemId()==34717)return style==AttackStyle.MELEE_CRUSH?EnumSet.of(MeleeStance.SMASH):EnumSet.noneOf(MeleeStance.class);
        return CombatStyleLabels.meleeStances(weapon==null?"Unarmed":weapon.getWeaponCategory(),style);
    }
    public static boolean supports(EquipmentCandidate weapon,AttackStyle style)
    {
        return !legalStances(weapon,style).isEmpty();
    }
}
