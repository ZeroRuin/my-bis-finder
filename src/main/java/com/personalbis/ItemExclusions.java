package com.personalbis;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ItemComposition;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

@Singleton
public final class ItemExclusions
{
    public static final String KEY = "excludedItems";
    public static final String MENU_OPTION = "Exclude from My BiS Finder";
    private final ConfigManager config;
    private final ItemManager items;

    @Inject
    public ItemExclusions(ConfigManager config, ItemManager items)
    {
        this.config = config;
        this.items = items;
    }

    public ItemExclusionList snapshot()
    {
        return new ItemExclusionList(config.getConfiguration("personalbis", KEY));
    }

    // Item definition and canonicalisation calls belong on the client thread.
    public String nameFor(int itemId)
    {
        ItemComposition definition = items.getItemComposition(items.canonicalize(itemId));
        return definition.getName();
    }

    public void exclude(int itemId)
    {
        if (itemId <= 0) return;
        String name = nameFor(itemId);
        if (name == null || name.isEmpty() || snapshot().excludes(name, name)) return;
        String current = config.getConfiguration("personalbis", KEY);
        config.setConfiguration("personalbis", KEY,
            current == null || current.trim().isEmpty() ? name : current.trim() + ", " + name);
    }

    public List<BankItem> filter(List<BankItem> owned, ItemExclusionList policy)
    {
        return policy.filter(owned, this::nameFor);
    }
}
