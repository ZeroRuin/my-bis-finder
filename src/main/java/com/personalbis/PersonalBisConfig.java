package com.personalbis;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("personalbis")
public interface PersonalBisConfig extends Config
{
    @ConfigItem(
        keyName = "excludedItems",
        name = "Excluded items",
        description = "Comma-separated item names to exclude from gear and supplies. "
            + "Delete entries to restore them; empty the box to clear all. "
            + "Potion doses and equivalent RuneLite variants are excluded together. Generate again to apply changes.",
        position = 1
    )
    default String excludedItems()
    {
        return "";
    }

    @ConfigItem(
        keyName = "showSidebar",
        name = "Show sidebar",
        description = "Show the My BiS Finder sidebar."
    )
    default boolean showSidebar()
    {
        return true;
    }
    @ConfigItem(keyName = "hideOtherBankItems", name = "Hide Other items",
        description = "Hide the Other items section in the My BiS Finder bank view. The normal bank is unaffected.", position = 2)
    default boolean hideOtherBankItems() { return true; }

    @ConfigItem(keyName = "hideReductionMAUL", name = "Hide Elder maul reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 10)
    default boolean hideReductionMAUL() { return false; }

    @ConfigItem(keyName = "hideReductionDWH", name = "Hide Dragon warhammer reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 11)
    default boolean hideReductionDWH() { return false; }

    @ConfigItem(keyName = "hideReductionARCLIGHT", name = "Hide Arclight reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 12)
    default boolean hideReductionARCLIGHT() { return false; }

    @ConfigItem(keyName = "hideReductionEMBERLIGHT", name = "Hide Emberlight reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 13)
    default boolean hideReductionEMBERLIGHT() { return false; }

    @ConfigItem(keyName = "hideReductionTONALZTICS", name = "Hide Tonalztics reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 14)
    default boolean hideReductionTONALZTICS() { return false; }

    @ConfigItem(keyName = "hideReductionBGS", name = "Hide Bandos godsword reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 15)
    default boolean hideReductionBGS() { return false; }

    @ConfigItem(keyName = "hideReductionSEERCULL", name = "Hide Seercull reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 16)
    default boolean hideReductionSEERCULL() { return false; }

    @ConfigItem(keyName = "hideReductionAYAK", name = "Hide Eye of Ayak reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 17)
    default boolean hideReductionAYAK() { return false; }

    @ConfigItem(keyName = "hideReductionACCURSED", name = "Hide Accursed sceptre reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 18)
    default boolean hideReductionACCURSED() { return false; }

    @ConfigItem(keyName = "hideReductionVULNERABILITY", name = "Hide Vulnerability reduction",
        description = "Hide this Advanced control and clear its selected reduction. Controls otherwise require owned items or supplies.", position = 19)
    default boolean hideReductionVULNERABILITY() { return false; }

}
