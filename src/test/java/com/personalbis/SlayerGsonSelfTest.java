package com.personalbis;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.personalbis.wiki.WikiDataPack;
import java.util.HashSet;
import java.util.Set;

/** Verifies that all Slayer loaders use the supplied client parser. */
public final class SlayerGsonSelfTest
{
    public static void main(String[] args)
    {
        Set<String> parsed = new HashSet<>();
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new TypeAdapterFactory()
        {
            public <T> TypeAdapter<T> create(Gson parser, TypeToken<T> type)
            {
                parsed.add(type.getRawType().getName());
                return null;
            }
        }).create();
        new WikiDataPack(gson);
        for (String name : new String[]{"SlayerTaskCatalogue$Pack", "SlayerAssignmentLocation$Pack",
            "SlayerAssignmentLocation$PositivePack", "SlayerAssignmentLocation$ScopePack",
            "KonarTargetCatalogue$Pack", "ReviewedSlayerLocations$Pack"})
            if (!parsed.contains("com.personalbis." + name))
                throw new AssertionError("Loader bypassed supplied Gson: " + name);
        if (SlayerTaskCatalogue.taskCount() < 150) throw new AssertionError("Task data missing");
        SlayerData.initialize(gson);
        System.out.println("PASS all six Slayer packs use the supplied Gson; repeated initialization is safe");
    }
}
