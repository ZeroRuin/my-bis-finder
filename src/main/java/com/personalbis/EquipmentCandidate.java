package com.personalbis;

public class EquipmentCandidate
{
    private final BankItem item;
    private final EquipmentSlot slot;
    private final int score;
    private final int attackBonus;
    private final int strengthBonus;
    private final int rangedAttack;
    private final int rangedStrength;
    private final int magicAttack;
    private final float magicDamage;
    private final int prayer;
    private final int attackSpeed;
    private final boolean twoHanded;
    private final String weaponCategory;
    private final RequirementResult requirementResult;

    public EquipmentCandidate(BankItem item, EquipmentSlot slot, int score,
        int attackBonus, int strengthBonus, int rangedAttack, int rangedStrength,
        int magicAttack, float magicDamage, int prayer, int attackSpeed,
        boolean twoHanded, String weaponCategory, RequirementResult requirementResult)
    {
        this.item = item;
        this.slot = slot;
        this.score = score;
        this.attackBonus = attackBonus;
        this.strengthBonus = strengthBonus;
        this.rangedAttack = rangedAttack;
        this.rangedStrength = rangedStrength;
        this.magicAttack = magicAttack;
        this.magicDamage = magicDamage;
        this.prayer = prayer;
        this.attackSpeed = attackSpeed;
        this.twoHanded = twoHanded;
        this.weaponCategory = weaponCategory == null ? "" : weaponCategory;
        this.requirementResult = requirementResult;
    }

    public BankItem getItem() { return item; }
    public EquipmentSlot getSlot() { return slot; }
    public int getScore() { return score; }
    public int getAttackBonus() { return attackBonus; }
    public int getStrengthBonus() { return strengthBonus; }
    public int getRangedAttack() { return rangedAttack; }
    public int getRangedStrength() { return rangedStrength; }
    public int getMagicAttack() { return magicAttack; }
    public float getMagicDamage() { return magicDamage; }
    public int getPrayer() { return prayer; }
    public int getAttackSpeed() { return attackSpeed; }
    public boolean isTwoHanded() { return twoHanded; }
    public String getWeaponCategory() { return weaponCategory; }
    public RequirementResult getRequirementResult() { return requirementResult; }
}
