package com.personalbis;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("personalbis")
public interface PersonalBisConfig extends Config
{
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
