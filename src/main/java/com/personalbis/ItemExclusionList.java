package com.personalbis;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.function.IntFunction;

/** Immutable name policy captured once per manual loadout generation. */
public final class ItemExclusionList
{
    private final Set<String> names;

    public ItemExclusionList(String text)
    {
        Set<String> parsed = new LinkedHashSet<>();
        if (text != null)
            for (String entry : text.split("[,\\r\\n]+"))
            {
                String name = normalise(entry);
                if (!name.isEmpty()) parsed.add(name);
            }
        names = Collections.unmodifiableSet(parsed);
    }

    private static String normalise(String name)
    {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ").replaceFirst("\\([1-4]\\)$", "").trim();
    }

    public boolean excludes(String name, String canonicalName)
    {
        return names.contains(normalise(name)) || names.contains(normalise(canonicalName));
    }

    public boolean sameAs(ItemExclusionList other)
    {
        return other != null && names.equals(other.names);
    }

    public List<BankItem> filter(List<BankItem> owned, IntFunction<String> canonicalNames)
    {
        List<BankItem> allowed = new ArrayList<>();
        for (BankItem item : owned)
            if (!excludes(item.getName(), canonicalNames.apply(item.getItemId()))) allowed.add(item);
        return allowed;
    }
}
