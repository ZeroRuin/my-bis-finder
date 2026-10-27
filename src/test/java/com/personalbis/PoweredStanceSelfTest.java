package com.personalbis;
import java.util.*;
public final class PoweredStanceSelfTest {
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    private static EquipmentCandidate item(String name,EquipmentSlot slot){return new EquipmentCandidate(new BankItem(1,1,name),slot,0,0,0,0,0,0,0,0,0,false,"",RequirementResult.usable("fixture"));}
    public static void main(String[] args){
        List<EquipmentCandidate> gear=Collections.emptyList();
        WikiMagicAccuracy.Result standard=WikiMagicAccuracy.calculate(99,1.25,100,gear,null,null,false);
        WikiMagicAccuracy.Result powered=WikiMagicAccuracy.calculatePoweredAccurate(99,1.25,100,gear,null,false);
        check(standard.effectiveLevel==132&&powered.effectiveLevel==134,"stance bonus after truncated prayer");
        check(powered.attackRoll==134*164,"powered accurate roll");
        List<EquipmentCandidate> voidGear=Arrays.asList(item("Void mage helm",EquipmentSlot.HEAD),item("Void knight top",EquipmentSlot.BODY),item("Void knight robe",EquipmentSlot.LEGS),item("Void knight gloves",EquipmentSlot.HANDS));
        check(WikiMagicAccuracy.calculatePoweredAccurate(99,1.25,100,voidGear,null,false).effectiveLevel==194,"stance before Void truncation");
        check(WikiMagicAccuracy.calculate(99,1.25,100,voidGear,null,null,false).effectiveLevel==191,"autocast unaffected");
        for(String staff:new String[]{"Trident of the seas","Trident of the swamp","Sanguinesti staff","Warped sceptre","Tumeken's shadow","Eye of ayak","Bone staff"})
            check(CombatStyleLabels.magic(staff).equals("Accurate"),"powered label: "+staff);
        check(CombatStyleLabels.magic("Fire Surge").equals("Spell"),"spell label");
        System.out.println("PASS powered Accurate label/bonus and unchanged ordinary autocast context");
    }
}
