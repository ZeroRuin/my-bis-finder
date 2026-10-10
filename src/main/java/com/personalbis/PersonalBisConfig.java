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
}
