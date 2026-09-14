package com.personalbis;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PersonalBisPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(PersonalBisPlugin.class);
        RuneLite.main(args);
    }
}
