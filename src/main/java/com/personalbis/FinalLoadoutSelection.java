package com.personalbis;

public final class FinalLoadoutSelection
{
    private FinalLoadoutSelection(){}

    public static boolean better(double candidateDps, double currentDps)
    {
        return Double.isFinite(candidateDps) &&
            (!Double.isFinite(currentDps) || candidateDps > currentDps);
    }

    /** Finite-fight ordering: lower expected TTK wins, sustained DPS breaks ties. */
    public static boolean betterTtk(double candidateTtk, double candidateDps,
        double currentTtk, double currentDps)
    {
        if (!Double.isFinite(candidateTtk)) return false;
        if (!Double.isFinite(currentTtk)) return true;
        int ttk = Double.compare(candidateTtk,currentTtk);
        return ttk < 0 || (ttk == 0 && better(candidateDps,currentDps));
    }

    public static int compareTtk(double leftTtk, double leftDps, double rightTtk, double rightDps)
    {
        double l=Double.isFinite(leftTtk)?leftTtk:Double.POSITIVE_INFINITY;
        double r=Double.isFinite(rightTtk)?rightTtk:Double.POSITIVE_INFINITY;
        int ttk=Double.compare(l,r);
        return ttk!=0?ttk:Double.compare(rightDps,leftDps);
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
