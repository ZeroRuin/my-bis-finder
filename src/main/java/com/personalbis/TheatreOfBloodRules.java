package com.personalbis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Runtime labels and combat states; the upstream monster snapshot stays intact. */
final class TheatreOfBloodRules
{
    private static final List<String> BOSSES = Arrays.asList("The Maiden of Sugadinti",
        "Pestilent Bloat", "Nylocas Vasilias", "Sotetseg", "Xarpus", "Verzik Vitur", "Nylocas Prinkipas");

    private TheatreOfBloodRules() {}

    static boolean isBoss(String name)
    {
        return BOSSES.contains(name);
    }

    static String label(String name, String version)
    {
        if (!isBoss(name)) return version;
        String raw = version == null ? "" : version.trim();
        if (name.equals("Nylocas Prinkipas")) return "Hard Mode, " + raw + " form";
        String lower = raw.toLowerCase(Locale.ROOT);
        String mode = lower.contains("entry") ? "Entry Mode"
            : lower.contains("hard") ? "Hard Mode" : "Normal Mode";
        int phase = lower.indexOf("phase ");
        if (phase >= 0) return mode + ", Phase " + raw.substring(phase + 6);
        if (lower.contains("health")) return mode + ", " + raw;
        return mode;
    }

    static List<String> versions(String name, String version)
    {
        String labelled = label(name, version);
        if ("Nylocas Vasilias".equals(name))
            return Arrays.asList(labelled + ", Melee form", labelled + ", Ranged form", labelled + ", Magic form");
        return Collections.singletonList(labelled);
    }

    /** Keep equal-stat difficulties and style-locked forms distinct in the picker. */
    static String catalogueKey(MonsterDefinition monster)
    {
        return isBoss(monster.getName()) ? "|tob|" + monster.getVersion().toLowerCase(Locale.ROOT) : "";
    }

    static boolean phaseOne(MonsterDefinition monster)
    {
        if (monster == null || !"Verzik Vitur".equals(monster.getName())) return false;
        switch (monster.getId())
        {
            case 10830: case 10831: case 10832:
            case 8369: case 8370: case 8371:
            case 10847: case 10848: case 10849: return true;
            default: return false;
        }
    }

    static boolean canDamage(MonsterDefinition monster, AttackStyle style)
    {
        if (monster == null || !("Nylocas Vasilias".equals(monster.getName())
            || "Nylocas Prinkipas".equals(monster.getName()))) return true;
        String version = monster.getVersion().toLowerCase(Locale.ROOT);
        if (version.endsWith("magic form")) return style == AttackStyle.MAGIC;
        if (version.endsWith("ranged form")) return style == AttackStyle.RANGED;
        if (version.endsWith("melee form")) return style != AttackStyle.MAGIC && style != AttackStyle.RANGED;
        return true;
    }
}
