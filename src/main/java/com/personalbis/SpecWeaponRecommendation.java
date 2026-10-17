package com.personalbis;

/** A separate, single-special burst score; never modifies the ordinary loadout's DPS. */
final class SpecWeaponRecommendation
{
    final EquipmentCandidate weapon,ammo;
    final double dps,damage;
    final int maximum,energy,ticks;
    final String style,note;
    SpecWeaponRecommendation(EquipmentCandidate weapon,EquipmentCandidate ammo,double damage,int maximum,int energy,int ticks,String style,String note){
        this.weapon=weapon;this.ammo=ammo;this.damage=damage;this.maximum=maximum;this.energy=energy;this.ticks=ticks;
        this.dps=damage/(ticks*.6);this.style=style;this.note=note;
    }
    SpecWeaponRecommendation withCoverage(String omitted){
        return new SpecWeaponRecommendation(weapon,ammo,damage,maximum,energy,ticks,style,
            note+(omitted.isEmpty()?"":" | Unmodelled owned specials: "+omitted));
    }
}
