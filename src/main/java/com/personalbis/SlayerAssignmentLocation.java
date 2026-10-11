package com.personalbis;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/** Conservative location checks: missing data is UNKNOWN, never assumed eligible. */
final class SlayerAssignmentLocation
{
    static final String UNKNOWN_LOCATION = "Location not yet detected";
    enum Eligibility { ELIGIBLE, INELIGIBLE, UNKNOWN }
    private static volatile Pack PACK;
    private static volatile List<PositiveMatch> MATCHES;
    private static volatile ScopePack SCOPE;

    static synchronized void initialize(Gson gson)
    {
        if (SCOPE != null) return;
        PACK = load(gson);
        MATCHES = loadMatches(gson);
        SCOPE = loadScope(gson);
    }

    private static final class ScopePack
    {
        List<String> locationFreeBossTasks;
        Map<String, List<String>> masters;
        Map<String, List<String>> konarLocations;
    }

    private static ScopePack loadScope(Gson gson)
    {
        try (InputStream in = SlayerAssignmentLocation.class.getResourceAsStream("/slayer-data/restricted-task-scope.json"))
        {
            if (in == null) throw new IllegalStateException("Missing restricted Slayer task scope");
            return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), ScopePack.class);
        }
        catch (java.io.IOException ex) { throw new IllegalStateException("Cannot load restricted Slayer task scope", ex); }
    }

    private static final class PositivePack { List<PositiveMatch> rows; }
    private static final class PositiveMatch
    {
        String name;
        int level;
        String location;
        List<Integer> catalogueIds;
    }

    private static List<PositiveMatch> loadMatches(Gson gson)
    {
        try (InputStream in = SlayerAssignmentLocation.class.getResourceAsStream("/slayer-data/restricted-location-matches.json"))
        {
            if (in == null) throw new IllegalStateException("Missing Slayer location matches");
            PositivePack pack = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PositivePack.class);
            return Collections.unmodifiableList(pack.rows);
        }
        catch (java.io.IOException ex)
        {
            throw new IllegalStateException("Cannot load Slayer location matches", ex);
        }
    }

    private static final class Pack
    {
        Map<String, List<String>> names;
        Map<String, List<String>> ids;
    }

    private static Pack load(Gson gson)
    {
        try (InputStream in = SlayerAssignmentLocation.class.getResourceAsStream("/slayer-data/locations.json"))
        {
            if (in == null) throw new IllegalStateException("Missing Slayer location definitions");
            return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Pack.class);
        }
        catch (java.io.IOException ex)
        {
            throw new IllegalStateException("Cannot load Slayer locations", ex);
        }
    }

    static String assignment(String reportedLocation, boolean wildernessTask)
    {
        return wildernessTask ? "Wilderness" : reportedLocation == null ? "" : reportedLocation.trim();
    }

    static String assignment(String reportedLocation, boolean wildernessTask, boolean assignedArea)
    {
        String location = assignment(reportedLocation, wildernessTask);
        return location.isEmpty() && assignedArea ? UNKNOWN_LOCATION : location;
    }

    static String assignment(String reportedLocation, int master, boolean assignedArea)
    {
        return assignment(reportedLocation, master, assignedArea, null);
    }

    static String assignment(String reportedLocation, int master, boolean assignedArea, String task)
    {
        if (master == 7) return "Wilderness";
        if (master == 0) return UNKNOWN_LOCATION;
        if (master != 8) return "";
        String location = assignment(reportedLocation, false);
        if (!location.isEmpty()) return location;
        // Named boss assignments have no Konar area roll. Ordinary, unknown or
        // newly added families stay closed until their area has been detected.
        if (!assignedArea && SCOPE.locationFreeBossTasks.stream().anyMatch(b ->
            SlayerTaskCatalogue.normalize(b).equals(SlayerTaskCatalogue.normalize(task)))) return "";
        return UNKNOWN_LOCATION;
    }

    static boolean supportsTaskArea(String task, String location)
    {
        String area = normalize(location);
        if (area.isEmpty()) return true;
        if (area.equals("wilderness"))
            return SCOPE.masters.get("Krystilia").stream().anyMatch(t -> sameFamily(t, task));
        for (Map.Entry<String, List<String>> entry : SCOPE.konarLocations.entrySet())
            if (sameFamily(entry.getKey(), task))
                return entry.getValue().stream().anyMatch(a -> normalize(a).equals(area));
        return false;
    }

    private static boolean sameFamily(String left, String right)
    {
        return SlayerTaskCatalogue.normalize(left).equals(SlayerTaskCatalogue.normalize(right))
            || SlayerTaskCatalogue.singular(left).equals(SlayerTaskCatalogue.singular(right));
    }

    static String normalize(String location)
    {
        String n = SlayerTaskCatalogue.normalize(location).replace('’', '\'');
        n = n.replace("wildnerness", "wilderness").replace("labratories", "laboratories");
        if (n.equals("ruins of tapouik")) return "ruins of tapoyauik";
        if (n.startsWith("slayer tower - floor")) return "slayer tower";
        if (n.startsWith("catacombs of kourend -")) return "catacombs of kourend";
        if (n.startsWith("karuulm slayer dungeon -")) return "karuulm slayer dungeon";
        if (n.equals("stronghold slayer dungeon")) return "stronghold slayer cave";
        if (n.equals("waterbirth island dungeon")) return "waterbirth island";
        if (n.equals("myths' guild dungeon")) return "myths' guild";
        if (n.equals("witchaven shrine dungeon")) return "witchaven dungeon";
        if (n.equals("isle of souls dungeon")) return "isle of souls";
        if (n.equals("mount karuulm") || n.equals("karuulm dungeon")) return "karuulm slayer dungeon";
        if (n.equals("the abyss")) return "abyss";
        if (n.equals("wilderness slayer cave") || n.equals("wilderness slayer caves")) return "wilderness";
        if (n.equals("lizardman settlement (lizardman caves included)") || n.equals("lizardman caves"))
            return "lizardman settlement";
        if (n.equals("molch (lizardman temple included)") || n.equals("lizardman temple")) return "molch";
        if (n.equals("kebos swamp (area surrounding xeric's shrine)")) return "kebos swamp";
        return n;
    }

    static Eligibility eligibility(MonsterDefinition monster, String location)
    {
        String area = normalize(location);
        if (area.isEmpty()) return Eligibility.ELIGIBLE;
        // A new/unrecognised area can require updated location metadata. Do not
        // claim a confirmed mismatch. The picker hides unknowns separately.
        boolean knownArea = PACK.names.values().stream().flatMap(List::stream)
            .anyMatch(place -> normalize(place).equals(area))
            || PACK.ids.values().stream().flatMap(List::stream)
                .anyMatch(place -> normalize(place).equals(area))
            || MATCHES.stream().anyMatch(match -> normalize(match.location).equals(area));
        if (!knownArea) return Eligibility.UNKNOWN;
        List<String> places = PACK.ids.get(Integer.toString(monster.getId()));
        if (places == null)
            for (Map.Entry<String, List<String>> entry : PACK.names.entrySet())
                if (entry.getKey().equalsIgnoreCase(monster.getName())) { places = entry.getValue(); break; }
        if (places == null)
        {
            for (PositiveMatch match : MATCHES)
                if (match.name.equalsIgnoreCase(monster.getName()) && match.level == monster.getCombatLevel()
                    && match.catalogueIds != null && match.catalogueIds.contains(monster.getId()))
                    if (normalize(match.location).equals(area)) return Eligibility.ELIGIBLE;
            // This source is incomplete: absence never proves ineligibility.
            return Eligibility.UNKNOWN;
        }
        for (String place : places)
            if (normalize(place).equals(area)) return Eligibility.ELIGIBLE;
        return Eligibility.INELIGIBLE;
    }

    static String note(String task, String location, int unverified, boolean knownTask)
    {
        if (task == null || task.trim().isEmpty()) return "No active Slayer task detected.";
        String text = location == null || location.isEmpty()
            ? "Slayer: " + task + ". No assigned location restriction."
            : "Slayer: " + task + ". " + (UNKNOWN_LOCATION.equals(location)
                ? "Assigned location not yet detected; check your Slayer task again."
                : "Assigned location: " + location + ".");
        if (!knownTask) text += " Task mapping unavailable; showing name matches only.";
        if (unverified > 0) text += " " + unverified
            + " unverified option" + (unverified == 1 ? "" : "s") + " hidden. Location coverage is incomplete.";
        return text;
    }
}
