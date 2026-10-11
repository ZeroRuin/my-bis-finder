package com.personalbis;

import com.google.gson.Gson;

/** Loads the offline Slayer catalogues with RuneLite's injected JSON parser. */
public final class SlayerData
{
    private SlayerData() { }

    public static void initialize(Gson gson)
    {
        SlayerTaskCatalogue.initialize(gson);
        SlayerAssignmentLocation.initialize(gson);
        KonarTargetCatalogue.initialize(gson);
        ReviewedSlayerLocations.initialize(gson);
    }
}
