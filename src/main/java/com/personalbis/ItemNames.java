package com.personalbis;

import java.util.Locale;
import java.util.regex.Pattern;

/** One place for matching items by name, so every search and effect treats names the same way. */
final class ItemNames
{
    private static final Pattern BARROWS_CHARGE = Pattern.compile(" (100|75|50|25)$");
    private static final Pattern BARROWS_BROKEN = Pattern.compile("^(Echo )?(Ahrim|Dharok|Guthan|Karil|Torag|Verac)'s .* 0$", Pattern.CASE_INSENSITIVE);

    private ItemNames() {}

    /** The item's name in lower case, or an empty string when there is no item. */
    static String lower(EquipmentCandidate c)
    {
        return c == null || c.getItem() == null || c.getItem().getName() == null ? "" : c.getItem().getName().toLowerCase(Locale.ROOT);
    }

    /** A Barrows piece at 0 charge: it has no stats or set effect until repaired, so it is never recommended. */
    static boolean fullyDegraded(String name)
    {
        return name != null && BARROWS_BROKEN.matcher(name).matches();
    }

    /** The name without a Barrows charge suffix: "Karil's coif 75" becomes "Karil's coif". */
    static String withoutCharge(String name)
    {
        return name == null ? null : BARROWS_CHARGE.matcher(name).replaceFirst("");
    }

    /** An item broken on death, such as "Fire cape (broken)": it cannot be equipped until repaired. */
    static boolean broken(String name)
    {
        return name != null && name.toLowerCase(Locale.ROOT).endsWith("(broken)");
    }

    /**
     * True when the candidate is the named item. A Barrows piece matches at any working
     * charge ("Karil's coif 75" is "Karil's coif"), Void pieces match with their ornament
     * kit, and broken items never match.
     */
    static boolean is(EquipmentCandidate c, String name)
    {
        if (c == null || c.getItem() == null || c.getItem().getName() == null) return false;
        String actual = c.getItem().getName();
        if (actual.toLowerCase(Locale.ROOT).contains("broken") || actual.endsWith(" 0") || ItemIds.isBrokenSetPiece(c.getItem().getItemId())) return false;
        actual = withoutCharge(actual);
        return actual.equalsIgnoreCase(name) || (name.toLowerCase(Locale.ROOT).contains("void ") && VoidEquipment.matches(actual, name));
    }

    static boolean containsAny(EquipmentCandidate c, String... parts)
    {
        String name = lower(c);
        for (String part : parts) if (name.contains(part)) return true;
        return false;
    }
}
