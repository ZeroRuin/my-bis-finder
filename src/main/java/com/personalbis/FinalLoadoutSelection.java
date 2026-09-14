package com.personalbis;

public final class FinalLoadoutSelection
{
    private FinalLoadoutSelection(){}

    public static boolean better(double candidateDps, double currentDps)
    {
        return Double.isFinite(candidateDps) &&
            (!Double.isFinite(currentDps) || candidateDps > currentDps);
    }

    public static boolean compatibleShield(EquipmentCandidate weapon, EquipmentCandidate shield)
    {
        return shield == null || weapon == null || !weapon.isTwoHanded();
    }

    public static boolean compatibleAmmo(EquipmentCandidate weapon, EquipmentCandidate ammo)
    {
        if (weapon == null) return ammo == null;
        if (!RangedAmmoRules.usesExternalAmmo(weapon)) return ammo == null;
        return ammo != null && RangedAmmoRules.compatible(weapon, ammo);
    }
}
