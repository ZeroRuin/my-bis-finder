package com.personalbis;

import com.google.gson.*;
import com.personalbis.wiki.WikiDataPack;
import java.nio.file.*;

public final class ReviewedSlayerLocationsSelfTest
{
    public static void main(String[] args) throws Exception
    {
        MonsterDatabase db = new MonsterDatabase(null, new WikiDataPack(new Gson()));
        db.rebuild();
        JsonArray rows = new Gson().fromJson(Files.readString(Paths.get(
            "src/main/resources/slayer-data/reviewed-locations.json")), JsonObject.class).getAsJsonArray("rows");
        int checked = 0;
        for (JsonElement element : rows)
        {
            JsonObject row = element.getAsJsonObject();
            String task = row.get("task").getAsString(), area = row.get("area").getAsString();
            int id = row.get("id").getAsInt();
            MonsterDefinition monster = db.findTaskVariants(task).stream().filter(m -> m.getId() == id)
                .findFirst().orElseThrow(() -> new AssertionError("Missing reviewed profile " + id));
            boolean yes = row.get("eligible").getAsBoolean();
            SlayerAssignmentLocation.Eligibility expected = yes ? SlayerAssignmentLocation.Eligibility.ELIGIBLE
                : SlayerAssignmentLocation.Eligibility.INELIGIBLE;
            if (db.taskLocationEligibility(task, monster, area) != expected)
                throw new AssertionError("Review decision mismatch " + task + "/" + area + "/" + id);
            if (db.findTaskVariants(task, area).stream().anyMatch(m -> m.getId() == id) != yes)
                throw new AssertionError("Picker mismatch " + id);
            if (ReviewedSlayerLocations.decision(task, "", monster) != null
                || ReviewedSlayerLocations.decision("Unrelated task", area, monster) != null)
                throw new AssertionError("Review leaked beyond scope " + id);
            checked++;
        }
        if (checked != 470) throw new AssertionError("Incomplete imported review");
        System.out.println("PASS all " + checked + " reviewed decisions, picker results and scope isolation");
    }
}
