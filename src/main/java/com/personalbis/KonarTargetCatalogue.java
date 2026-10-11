package com.personalbis;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Explicit task/species/area membership; separate from broad highlight aliases. */
final class KonarTargetCatalogue
{
    private static final Pack PACK = load();
    private static final class Pack
    {
        String combatSourceSha256;
        Map<String, Map<String, List<Rule>>> tasks;
    }
    private static final class Rule { String name; List<Integer> automaticIds; }

    private static Pack load()
    {
        try (InputStream in = KonarTargetCatalogue.class.getResourceAsStream("/slayer-data/konar-targets.json"))
        {
            if (in == null) throw new IllegalStateException("Missing Konar target definitions");
            Pack pack = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Pack.class);
            // Weekly combat updates must not reuse a uniqueness conclusion
            // established against different levels/stats. Membership remains
            // useful, but automatic resolutions wait for recompilation.
            try (InputStream combat = KonarTargetCatalogue.class.getResourceAsStream("/wiki-data/monsters.json"))
            {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] buffer = new byte[8192];
                int length;
                while ((length = combat.read(buffer)) != -1) digest.update(buffer, 0, length);
                StringBuilder hash = new StringBuilder();
                for (byte value : digest.digest()) hash.append(String.format("%02x", value & 0xff));
                if (!hash.toString().equals(pack.combatSourceSha256))
                    pack.tasks.values().stream().flatMap(a -> a.values().stream()).flatMap(List::stream)
                        .forEach(r -> r.automaticIds = java.util.Collections.emptyList());
            }
            catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
            return pack;
        }
        catch (java.io.IOException ex) { throw new IllegalStateException("Cannot load Konar target definitions", ex); }
    }

    private static Map<String, List<Rule>> task(String task)
    {
        for (Map.Entry<String, Map<String, List<Rule>>> entry : PACK.tasks.entrySet())
            if (SlayerTaskCatalogue.singular(entry.getKey()).equals(SlayerTaskCatalogue.singular(task)))
                return entry.getValue();
        return null;
    }

    static boolean listed(String task, MonsterDefinition monster)
    {
        Map<String, List<Rule>> areas = task(task);
        return areas != null && areas.values().stream().flatMap(List::stream)
            .anyMatch(r -> r.name.equalsIgnoreCase(monster.getName()));
    }

    /** null means keep using exact spawn/manual evidence for this permitted species. */
    static SlayerAssignmentLocation.Eligibility decision(String task, String location, MonsterDefinition monster)
    {
        String area = SlayerAssignmentLocation.normalize(location);
        if (area.isEmpty() || area.equals("wilderness")) return null;
        Map<String, List<Rule>> areas = task(task);
        if (areas == null) return null;
        List<Rule> rules = null;
        for (Map.Entry<String, List<Rule>> entry : areas.entrySet())
            if (SlayerAssignmentLocation.normalize(entry.getKey()).equals(area)) rules = entry.getValue();
        if (rules == null) return null;
        for (Rule rule : rules)
            if (rule.name.equalsIgnoreCase(monster.getName()))
                return rule.automaticIds.contains(monster.getId())
                    ? SlayerAssignmentLocation.Eligibility.ELIGIBLE : null;
        return SlayerAssignmentLocation.Eligibility.INELIGIBLE;
    }
}
