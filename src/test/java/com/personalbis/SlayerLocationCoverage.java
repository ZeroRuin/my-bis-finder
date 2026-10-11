package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

/** Development-only audit of actual picker results; never runs in the client. */
public final class SlayerLocationCoverage
{
    private static final class Scope
    {
        Map<String, List<String>> masters;
        Map<String, List<String>> konarLocations;
    }

    public static void main(String[] args) throws Exception
    {
        Gson gson = new Gson();
        Scope scope;
        try (InputStreamReader in = new InputStreamReader(SlayerLocationCoverage.class
            .getResourceAsStream("/slayer-data/restricted-task-scope.json"), StandardCharsets.UTF_8))
        {
            scope = gson.fromJson(in, Scope.class);
        }
        MonsterDatabase database = new MonsterDatabase(null, new WikiDataPack(gson));
        database.rebuild();
        StringBuilder table = new StringBuilder("| Master | Task | Assigned area | Confirmed picker entries | Unverified entries hidden |\n"
            + "|---|---|---|---|---|\n");
        int pairs = 0, empty = 0;
        for (Map.Entry<String, List<String>> master : scope.masters.entrySet())
            for (String task : master.getValue())
                for (String area : master.getKey().equals("Krystilia") ? Collections.singletonList("Wilderness")
                    : scope.konarLocations.getOrDefault(task, Collections.emptyList()))
                {
                    List<MonsterDefinition> choices = database.findTaskVariants(task, area);
                    choices.removeIf(m -> !SlayerTargetRequirements.allowed(m, 99));
                    int unknown = (int) database.findTaskVariants(task).stream()
                        .filter(m -> SlayerTargetRequirements.allowed(m, 99))
                        .filter(m -> database.taskLocationEligibility(task, m, area) == SlayerAssignmentLocation.Eligibility.UNKNOWN).count();
                    pairs++;
                    if (choices.isEmpty()) empty++;
                    String labels = choices.stream().map(m -> m.getName() + " (" + m.getCombatLevel()
                        + (m.getVersion().isEmpty() ? "" : ", " + m.getVersion()) + ")")
                        .collect(Collectors.joining("; ")).replace("|", "/");
                    table.append("| ").append(master.getKey()).append(" | ").append(task).append(" | ")
                        .append(area).append(" | ").append(labels.isEmpty() ? "**COVERAGE GAP**" : labels)
                        .append(" | ").append(unknown).append(" |\n");
                }
        String report = "# Restricted Slayer location coverage\n\n"
            + "Generated from the real bundled combat catalogue and the confirmed-only selector at Slayer level 99.\n\n"
            + pairs + " researched task/area combinations; " + (pairs - empty) + " have at least one confirmed target; "
            + empty + " currently have no confirmed target. A populated row does not prove every possible variant is covered.\n\n"
            + "Only Konar and Krystilia are included. Optional unlocks and legacy individual metal-dragon families are included in the research scope. "
            + "Named boss assignments without an assigned area are handled separately. Unknowns stay hidden.\n\n" + table;
        Files.write(Paths.get(args.length == 0 ? "SLAYER-LOCATION-COVERAGE.md" : args[0]), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("Coverage: " + (pairs - empty) + "/" + pairs + " task/area combinations populated; " + empty + " gaps.");
    }
}
