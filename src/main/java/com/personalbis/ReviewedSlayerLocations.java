package com.personalbis;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** User-reviewed exact task/area/picker-profile decisions. */
final class ReviewedSlayerLocations
{
    private static final Pack PACK = load();
    private static final class Pack { String combatSourceSha256; List<Row> rows; }
    private static final class Row { String task, area, name; int id, level; boolean eligible; }

    private static Pack load()
    {
        try (InputStream in = ReviewedSlayerLocations.class.getResourceAsStream("/slayer-data/reviewed-locations.json");
             InputStream combat = ReviewedSlayerLocations.class.getResourceAsStream("/wiki-data/monsters.json"))
        {
            if (in == null || combat == null) throw new IllegalStateException("Missing reviewed Slayer data");
            Pack pack = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Pack.class);
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int count;
            while ((count = combat.read(buffer)) != -1) digest.update(buffer, 0, count);
            StringBuilder hash = new StringBuilder();
            for (byte value : digest.digest()) hash.append(String.format("%02x", value & 0xff));
            // A changed combat pack can change merged picker profiles and IDs.
            if (!hash.toString().equals(pack.combatSourceSha256)) pack.rows = java.util.Collections.emptyList();
            return pack;
        }
        catch (java.io.IOException | java.security.NoSuchAlgorithmException ex)
        { throw new IllegalStateException("Cannot load reviewed Slayer locations", ex); }
    }

    static SlayerAssignmentLocation.Eligibility decision(String task, String location, MonsterDefinition monster)
    {
        for (Row row : PACK.rows)
            if (row.id == monster.getId() && row.level == monster.getCombatLevel()
                && row.name.equalsIgnoreCase(monster.getName())
                && SlayerTaskCatalogue.singular(row.task).equals(SlayerTaskCatalogue.singular(task))
                && SlayerAssignmentLocation.normalize(row.area).equals(SlayerAssignmentLocation.normalize(location)))
                return row.eligible ? SlayerAssignmentLocation.Eligibility.ELIGIBLE
                    : SlayerAssignmentLocation.Eligibility.INELIGIBLE;
        return null;
    }
}
