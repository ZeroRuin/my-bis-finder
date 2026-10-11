package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import java.util.List;

/** Uses the real bundled monster pack, including entries merged by combat stats. */
public final class SlayerTaskSelfTest
{
    private static int checks;

    public static void main(String[] args) throws Exception
    {
        MonsterDatabase database = new MonsterDatabase(null, new WikiDataPack(new Gson()));
        database.rebuild();
        require(SlayerTaskCatalogue.taskCount() >= 150, "full RuneLite task snapshot loaded");
        List<MonsterDefinition> greater = database.findTaskVariants("Greater demons");
        for (String name : new String[]{"Greater demon", "K'ril Tsutsaroth", "Tstanon Karlak", "Skotizo", "Tormented Demon"})
            contains(greater, name, true, "greater demon alternative");
        contains(greater, "Black demon", false, "unrelated demon excluded");
        require(greater.stream().noneMatch(m -> m.getName().contains("Scar")), "Scar fixtures excluded");
        contains(database.findTaskVariants("Blue dragons"), "Vorkath", true, "blue dragon boss alternative");
        contains(database.findTaskVariants("Hellhounds"), "Cerberus", true, "hellhound boss alternative");
        contains(database.findTaskVariants("Black demons"), "Demonic gorilla", true, "black demon alternative");
        contains(database.findTaskVariants("Abyssal demons"), "Abyssal Sire", true, "abyssal demon alternative");
        contains(database.findTaskVariants("Gargoyles"), "Dawn", true, "gargoyle boss alternative");
        contains(database.findTaskVariants("Gargoyles"), "Dusk", true, "gargoyle boss alternative");
        contains(database.findTaskVariants("Hydras"), "Alchemical Hydra", true, "hydra boss alternative");
        require(!SlayerTargetRequirements.allowed(database.findBest("Cerberus"), 90)
            && SlayerTargetRequirements.allowed(database.findBest("Cerberus"), 91), "Cerberus Slayer level gate");
        require(!SlayerTargetRequirements.allowed(database.findBest("Alchemical Hydra"), 94), "Hydra Slayer level gate");
        require(!SlayerTargetRequirements.allowed(database.findBest("Abyssal Sire"), 84), "Sire Slayer level gate");
        require(!SlayerTargetRequirements.allowed(database.findBest("Dawn"), 74), "Guardians Slayer level gate");
        contains(database.findTaskVariants("Aviansies"), "Aviansie", true, "irregular plural aviansies");
        contains(database.findTaskVariants("Wolves"), "Wolf", true, "irregular plural wolves");
        contains(database.findTaskVariants("Banshees"), "Banshee", true, "banshee plural retains e");
        contains(database.findTaskVariants("Duke Sucellus"), "Duke Sucellus", true, "singular boss ending in s");
        require(database.findTaskVariants(null).isEmpty() && database.findTaskVariants(" ").isEmpty(), "no-task results empty");
        require(!SlayerTaskCatalogue.knownTask("Future unknown task"), "unknown task mapping reported");

        List<MonsterDefinition> catacombs = database.findTaskVariants("Greater demons", "Catacombs of Kourend");
        contains(catacombs, "Skotizo", true, "Catacombs Skotizo preserved");
        contains(catacombs, "Tormented Demon", false, "Catacombs excludes Tormented demons");
        contains(catacombs, "K'ril Tsutsaroth", false, "Catacombs excludes GWD boss");
        contains(catacombs, "Tstanon Karlak", false, "Catacombs excludes GWD minion");
        require(catacombs.stream().filter(m -> m.getName().equals("Greater demon"))
            .allMatch(m -> m.getCombatLevel() == 100 || m.getCombatLevel() == 101 || m.getCombatLevel() == 113),
            "Catacombs keeps correct greater demon combat variants");
        List<MonsterDefinition> chasm = database.findTaskVariants("Greater demons", "Chasm of Fire");
        require(chasm.size() == 1 && chasm.get(0).getCombatLevel() == 92, "Chasm keeps only level 92 greater demon");
        List<MonsterDefinition> gwd = database.findTaskVariants("Greater demons", "God Wars Dungeon");
        require(gwd.isEmpty(), "Konar Greater demon research excludes unassigned GWD location");
        contains(database.findTaskVariants("Greater demons"), "K'ril Tsutsaroth", true,
            "unrestricted Greater demon task retains GWD boss");
        contains(database.findTaskVariants("Greater demons"), "Tstanon Karlak", true,
            "unrestricted Greater demon task retains GWD minion");
        List<MonsterDefinition> wilderness = database.findTaskVariants("Greater demons", "Wilderness");
        contains(wilderness, "Skotizo", false, "Wilderness excludes Catacombs boss");
        contains(wilderness, "Tormented Demon", false, "Wilderness excludes Tormented demons");
        require(wilderness.stream().allMatch(m -> m.getCombatLevel() == 92 || m.getCombatLevel() == 104),
            "Wilderness greater demon combat variants");
        require(SlayerAssignmentLocation.assignment(null, true).equals("Wilderness"), "Krystilia restriction without location text");
        require(SlayerAssignmentLocation.assignment("Catacombs of Kourend", false).equals("Catacombs of Kourend"), "Konar location retained");
        require(SlayerAssignmentLocation.assignment(null, false).isEmpty(), "unrestricted assignment");
        require(SlayerAssignmentLocation.assignment(null, false, true).equals(SlayerAssignmentLocation.UNKNOWN_LOCATION),
            "missing Konar area text is not treated as unrestricted");
        require(database.taskLocationEligibility(database.findBest("Skotizo"), SlayerAssignmentLocation.UNKNOWN_LOCATION)
            == SlayerAssignmentLocation.Eligibility.UNKNOWN, "unresolved assignment area cannot confirm a target");
        contains(database.findTaskVariants("Blue dragons", "Taverley Dungeon"), "Vorkath", false, "Taverley excludes Vorkath");
        contains(database.findTaskVariants("Hellhounds", "Catacombs of Kourend"), "Cerberus", false, "Catacombs excludes Cerberus");
        contains(database.findTaskVariants("Hellhounds", "Taverley Dungeon"), "Cerberus", true, "Taverley permits Cerberus");
        contains(database.findTaskVariants("Abyssal demons", "Catacombs of Kourend"), "Abyssal Sire", false, "Catacombs excludes Sire");
        contains(database.findTaskVariants("Abyssal demons", "The Abyss"), "Abyssal Sire", true, "Abyss permits Sire");
        contains(database.findTaskVariants("Hydras", "Mount Karuulm"), "Alchemical Hydra", true, "Karuulm location alias");
        List<MonsterDefinition> brutal = database.findTaskVariants("Blue dragons", "Ruins of Tapoyauik");
        contains(brutal, "Brutal blue dragon", true, "merged combat record retains Tapoyauik eligibility");
        contains(database.findTaskVariants("Blue dragons", "Catacombs of Kourend"), "Brutal blue dragon", true,
            "merged combat record retains Catacombs eligibility");
        MonsterDefinition unknown = database.findBest("Bloodveld");
        require(database.taskLocationEligibility(unknown, "Unknown cave") == SlayerAssignmentLocation.Eligibility.UNKNOWN,
            "missing location data remains unknown");
        require(database.taskLocationEligibility(database.findBest("Skotizo"), "Future new area")
            == SlayerAssignmentLocation.Eligibility.UNKNOWN, "new location is unknown rather than silently rejected");
        require(SlayerAssignmentLocation.note("Bloodveld", "Unknown cave", 1, true).contains("unverified option hidden"),
            "hidden unknown options reported");
        require(SlayerAssignmentLocation.note(null, "", 0, false).equals("No active Slayer task detected."), "task-ended message");
        require(!SlayerAssignmentLocation.note("Greater demons", "", 0, true).contains("unverified"), "unrestricted task has no location warning");
        require(database.taskLocationEligibility(database.findBest("Abyssal demon"), "Slayer Tower")
            == SlayerAssignmentLocation.Eligibility.ELIGIBLE, "imported positive match normalizes tower floor");
        require(database.taskLocationEligibility(database.findBest("Kalphite Queen"), "Kalphite Lair")
            == SlayerAssignmentLocation.Eligibility.ELIGIBLE, "imported positive match confirms KQ lair");
        require(database.taskLocationEligibility(database.findBest("Kalphite Queen"), "Kalphite Cave")
            == SlayerAssignmentLocation.Eligibility.INELIGIBLE, "reviewed KQ lair rule excludes Kalphite Cave");
        List<MonsterDefinition> brimhavenBlack = database.findTaskVariants("Black demons", "Brimhaven Dungeon");
        require(brimhavenBlack.size() == 1 && brimhavenBlack.get(0).getCombatLevel() == 172,
            "Brimhaven Black demons shows only the confirmed level 172 variant");
        List<MonsterDefinition> catacombsBlack = database.findTaskVariants("Black demons", "Catacombs of Kourend");
        require(catacombsBlack.stream().filter(m -> m.getName().equals("Black demon"))
            .allMatch(m -> m.getCombatLevel() == 178 || m.getCombatLevel() == 184),
            "Catacombs excludes level 172 and Wilderness level 188 Black demons");
        contains(catacombsBlack, "Skotizo", true, "Catacombs Black demon boss alternative");
        require(database.findTaskVariants("Greater demons", "Future new area").isEmpty(),
            "unknown assigned area fails closed");
        require(database.findTaskVariants("Black demons", SlayerAssignmentLocation.UNKNOWN_LOCATION).isEmpty(),
            "unresolved location offers no unverified targets");
        for (String task : new String[]{"Black demons", "Fire giants", "Dust devils", "Bloodveld", "Ankou", "Jellies", "Zombies", "Magic axes"})
            for (String area : new String[]{"Brimhaven Dungeon", "Catacombs of Kourend", "Wilderness"})
                require(database.findTaskVariants(task, area).stream().allMatch(m ->
                    database.taskLocationEligibility(task, m, area) == SlayerAssignmentLocation.Eligibility.ELIGIBLE),
                    "restricted picker contains confirmed targets only: " + task + " / " + area);
        require(database.findTaskVariants("Fire giants", "Brimhaven Dungeon").stream()
            .allMatch(m -> m.getCombatLevel() == 86), "Brimhaven excludes mislabelled level 109 Fire giant");
        require(database.findTaskVariants("Dust devils", "Catacombs of Kourend").stream()
            .noneMatch(m -> m.getCombatLevel() == 93), "Catacombs excludes level 93 Smoke Dungeon Dust devil");
        contains(database.findTaskVariants("Magic axes", "Wilderness"), "Magic axe", true, "Wilderness Magic axe plural");
        contains(database.findTaskVariants("Zombies", "Wilderness"), "Zombie", true, "Wilderness Zombie plural");
        require(SlayerAssignmentLocation.assignment("Brimhaven Dungeon", 6, true).isEmpty(),
            "other masters ignore stale Konar location text and area varp");
        require(SlayerAssignmentLocation.assignment("Brimhaven Dungeon", 8, true).equals("Brimhaven Dungeon"),
            "Konar master ID retains assigned area");
        require(SlayerAssignmentLocation.assignment(null, 7, false).equals("Wilderness"),
            "Krystilia master ID forces Wilderness");
        contains(database.findTaskVariants("Lesser Nagua", "Crypt of Tonali"), "Earthen Nagua", true,
            "Earthen Nagua task alternative");
        require(SlayerAssignmentLocation.assignment(null, 8, false).equals(SlayerAssignmentLocation.UNKNOWN_LOCATION),
            "Konar location loading never becomes unrestricted");
        require(SlayerAssignmentLocation.assignment(null, 0, false).equals(SlayerAssignmentLocation.UNKNOWN_LOCATION),
            "unresolved master during login offers no unrestricted fallback");
        require(SlayerAssignmentLocation.assignment(null, 8, false, "The Alchemical Hydra").isEmpty(),
            "named Konar boss assignment has no area roll");
        require(SlayerAssignmentLocation.assignment(null, 8, true, "The Alchemical Hydra")
            .equals(SlayerAssignmentLocation.UNKNOWN_LOCATION), "boss name cannot bypass a recorded area restriction");
        List<MonsterDefinition> lighthouse = database.findTaskVariants("Dagannoth", "Lighthouse");
        require(lighthouse.stream().anyMatch(m -> m.getId() == 970), "Lighthouse level 74 Dagannoth available");
        require(lighthouse.stream().noneMatch(m -> m.getId() == 7259),
            "same-level Catacombs Dagannoth combat stats do not leak into Lighthouse");
        javax.swing.SwingUtilities.invokeAndWait(() ->
        {
            try
            {
                javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.GridBagLayout());
                card.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
                card.setSize(205, 600);
                AutoSizingTextArea note = new AutoSizingTextArea();
                // Use RuneLite's actual font without starting its application
                // singleton or loading custom user fonts during a headless test.
                try (java.io.InputStream font = SlayerTaskSelfTest.class.getResourceAsStream("/net/runelite/client/ui/runescape.ttf"))
                {
                    note.setFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, font).deriveFont(16f));
                }
                note.setLineWrap(true);
                note.setWrapStyleWord(true);
                note.setText(SlayerAssignmentLocation.note("Greater demons", "Catacombs of Kourend", 12, true)
                    + " Selected target's location is unverified.");
                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
                constraints.weightx = 1.0;
                constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                card.add(note, constraints);
                card.setSize(205, card.getPreferredSize().height);
                card.doLayout();
                java.awt.geom.Rectangle2D lastLine = note.modelToView2D(note.getText().length());
                require(lastLine != null && lastLine.getMaxY() <= note.getHeight(), "wrapped assignment warning fits sidebar card");
                int longHeight = note.getPreferredSize().height;
                note.setText(SlayerAssignmentLocation.note(null, "", 0, false));
                require(note.getPreferredSize().height < longHeight, "assignment note shrinks after task ends");
            }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });
        for (String king : new String[]{"Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme"})
        {
            contains(database.findTaskVariants("Dagannoth", "Waterbirth Island"), king, true,
                "reviewed Waterbirth boss alternative");
            contains(database.findTaskVariants("Dagannoth", "Lighthouse"), king, false,
                "Waterbirth boss never enters Lighthouse task");
        }
        contains(database.findTaskVariants("Dark beasts", "Iorwerth Dungeon"), "Night beast", true,
            "reviewed dark beast superior location");
        contains(database.findTaskVariants("Revenants", "Wilderness"), "Revenant maledictus", true,
            "reviewed dynamic Wilderness revenant");
        require(database.findTaskVariants("Black demons").stream()
            .filter(m -> m.getName().contains("Nightmare Zone"))
            .allMatch(m -> database.taskLocationEligibility(m, "Brimhaven Dungeon")
                == SlayerAssignmentLocation.Eligibility.INELIGIBLE),
            "NMZ variants resolved as outside Konar areas");
        require(database.findTaskVariants("Steel dragons").stream()
            .filter(m -> m.getName().contains("Construction"))
            .allMatch(m -> database.taskLocationEligibility(m, "Brimhaven Dungeon")
                == SlayerAssignmentLocation.Eligibility.INELIGIBLE),
            "POH guards resolved as outside Konar areas");
        contains(database.findTaskVariants("Lesser Nagua", "Ruins of Tapoyauik"), "Amoxliatl", true,
            "Wiki table adds Lesser Nagua boss alternative");
        contains(database.findTaskVariants("Vampyres", "Vampyrium"), "Venator", true,
            "Wiki table adds Vampyrium Venator alternative");
        contains(database.findTaskVariants("Wyrms", "Wyrmscraig Cavern"), "Wyrmling", true,
            "single-profile Wyrmling matches listed Wyrmscraig area");
        contains(database.findTaskVariants("Bloodveld", "Slayer Tower"), "Insatiable Bloodveld", true,
            "single-profile Bloodveld superior at Slayer Tower");
        contains(database.findTaskVariants("Bloodveld", "God Wars Dungeon"), "Insatiable Bloodveld", false,
            "GWD Bloodveld superior exception");
        contains(database.findTaskVariants("Bloodveld", "Catacombs of Kourend"), "Insatiable mutated Bloodveld", true,
            "mutated Bloodveld superior in Catacombs");
        contains(database.findTaskVariants("Trolls", "Death Plateau"), "Rock Crab", false,
            "explicit Konar table rejects broad Rock alias false positive");
        require(database.findTaskVariants("Steel dragons").stream()
            .noneMatch(m -> m.getName().contains("Construction")), "Construction never counts as a Slayer target");
        require(database.taskLocationEligibility("Ankou", database.findTaskVariants("Ankou").stream()
            .filter(m -> m.getCombatLevel() == 98).findFirst().get(), "Stronghold of Security")
            != SlayerAssignmentLocation.Eligibility.ELIGIBLE, "multi-level Ankou is never auto assigned everywhere");
        require(database.taskLocationEligibility("Black dragons", database.findTaskVariants("Black dragons").stream()
            .filter(m -> m.getName().equals("Baby black dragon") && m.getId() == 7955).findFirst().get(),
            "Taverley Dungeon") == SlayerAssignmentLocation.Eligibility.ELIGIBLE,
            "user review confirms level-83 baby dragon profile at Taverley");
        require(database.taskLocationEligibility("Wyrms", database.findBest("Wyrmling"), "Wilderness")
            != SlayerAssignmentLocation.Eligibility.ELIGIBLE, "Konar table does not propagate to Krystilia");
        System.out.println("ALL SLAYER TASK SELF-TESTS PASSED (" + checks + " checks)");
    }

    private static void contains(List<MonsterDefinition> list, String name, boolean expected, String label)
    {
        require(list.stream().anyMatch(m -> m.getName().equalsIgnoreCase(name)) == expected, label + ": " + name);
    }

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        checks++;
        System.out.println("PASS " + label);
    }
}
