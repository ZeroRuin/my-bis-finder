package com.personalbis;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Offline snapshot of RuneLite's task families; no dependency on its private Task enum. */
final class SlayerTaskCatalogue
{
    private static volatile Map<String, List<String>> TASKS;

    static synchronized void initialize(Gson gson)
    {
        if (TASKS == null) TASKS = load(gson);
    }

    private static final class Pack
    {
        Map<String, List<String>> tasks;
    }

    private static Map<String, List<String>> load(Gson gson)
    {
        try (InputStream in = SlayerTaskCatalogue.class.getResourceAsStream("/slayer-data/tasks.json"))
        {
            if (in == null) throw new IllegalStateException("Missing Slayer task definitions");
            Pack pack = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Pack.class);
            Map<String, List<String>> tasks = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> entry : pack.tasks.entrySet())
            {
                List<String> names = new ArrayList<>();
                names.add(normalize(entry.getKey()));
                names.add(singular(entry.getKey()));
                for (String name : entry.getValue())
                {
                    names.add(normalize(name));
                    names.add(singular(name));
                }
                if (normalize(entry.getKey()).equals("lesser nagua")) names.add("earthen nagua");
                tasks.put(normalize(entry.getKey()), Collections.unmodifiableList(names));
            }
            return Collections.unmodifiableMap(tasks);
        }
        catch (java.io.IOException ex)
        {
            throw new IllegalStateException("Cannot load Slayer task definitions", ex);
        }
    }

    static String normalize(String value)
    {
        String n = value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return n.startsWith("the ") ? n.substring(4) : n;
    }

    static String singular(String value)
    {
        String n = normalize(value);
        if (n.equals("wolves")) return "wolf";
        if (n.equals("werewolves")) return "werewolf";
        if (n.equals("dwarves")) return "dwarf";
        if (n.equals("elves")) return "elf";
        if (n.equals("aviansies")) return "aviansie";
        if (n.equals("zombies")) return "zombie";
        if (n.endsWith(" axes")) return n.substring(0, n.length() - 1);
        if (n.endsWith("ies")) return n.substring(0, n.length() - 3) + "y";
        if (n.endsWith("sses") || n.endsWith("xes")) return n.substring(0, n.length() - 2);
        if (n.endsWith("s") && !n.endsWith("ss")) return n.substring(0, n.length() - 1);
        return n;
    }

    static boolean knownTask(String task)
    {
        return TASKS.containsKey(normalize(task));
    }

    static int taskCount() { return TASKS.size(); }

    static boolean matches(String task, MonsterDefinition monster)
    {
        String name = normalize(monster.getName());
        String version = normalize(monster.getVersion());
        // These combat fixtures are not repeatable assignment alternatives.
        if (name.contains("(construction)") || name.startsWith("reanimated ") || name.startsWith("scarred ")
            || name.contains("(the scar)") || version.equals("whirlpool") || version.startsWith("idle")
            || version.equals("quest")
            || version.startsWith("quest,") || version.contains("(quest)")) return false;
        if (KonarTargetCatalogue.listed(task, monster)) return true;
        List<String> names = TASKS.get(normalize(task));
        if (names == null) names = Collections.singletonList(singular(task));
        for (String root : names)
        {
            if (!root.isEmpty() && (name.equals(root) || name.startsWith(root + " ")
                || name.endsWith(" " + root) || name.contains(" " + root + " ")
                || name.startsWith(root + "-"))) return true;
        }
        return false;
    }
}
