package com.personalbis;

import net.runelite.api.EquipmentInventorySlot;

public enum EquipmentSlot
{
    HEAD("Head", EquipmentInventorySlot.HEAD),
    CAPE("Cape", EquipmentInventorySlot.CAPE),
    NECK("Neck", EquipmentInventorySlot.AMULET),
    WEAPON("Weapon", EquipmentInventorySlot.WEAPON),
    BODY("Body", EquipmentInventorySlot.BODY),
    SHIELD("Shield", EquipmentInventorySlot.SHIELD),
    LEGS("Legs", EquipmentInventorySlot.LEGS),
    HANDS("Hands", EquipmentInventorySlot.GLOVES),
    FEET("Feet", EquipmentInventorySlot.BOOTS),
    RING("Ring", EquipmentInventorySlot.RING),
    AMMO("Ammo", EquipmentInventorySlot.AMMO);

    private final String displayName;
    private final EquipmentInventorySlot runeLiteSlot;

    EquipmentSlot(String displayName, EquipmentInventorySlot runeLiteSlot)
    {
        this.displayName = displayName;
        this.runeLiteSlot = runeLiteSlot;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getSlotIndex()
    {
        return runeLiteSlot.getSlotIdx();
    }

    public static EquipmentSlot fromRuneLiteSlot(int slot)
    {
        for (EquipmentSlot value : values())
        {
            if (value.getSlotIndex() == slot)
            {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
