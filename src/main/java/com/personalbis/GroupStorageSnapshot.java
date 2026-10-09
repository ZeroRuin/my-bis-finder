package com.personalbis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;

/** Client-thread snapshot logic, shared by future real and synthetic storage listeners. */
public final class GroupStorageSnapshot
{
    private volatile List<BankItem> items = Collections.emptyList();
    private volatile Instant capturedAt;

    public boolean capture(ItemContainerChanged event, IntFunction<ItemComposition> definitions, Supplier<Instant> clock)
    {
        if (event.getContainerId() != InventoryID.GROUP_STORAGE.getId() || event.getItemContainer() == null) return false;
        List<BankItem> next = new ArrayList<>();
        for (Item item : event.getItemContainer().getItems())
        {
            if (item == null || item.getId() <= 0 || item.getQuantity() <= 0) continue;
            ItemComposition definition = definitions.apply(item.getId());
            if (definition == null) throw new IllegalStateException("Missing item definition " + item.getId());
            if (definition.getPlaceholderTemplateId() >= 0) continue;
            next.add(new BankItem(item.getId(), item.getQuantity(), definition.getName() == null ? "" : definition.getName()));
        }
        // Publish only after processing the whole event; never retain its mutable array.
        Instant timestamp = clock.get();
        items = Collections.unmodifiableList(next);
        capturedAt = timestamp;
        return true;
    }

    public boolean hasSnapshot() { return capturedAt != null; }
    public Instant getCapturedAt() { return capturedAt; }
    public List<BankItem> getItems() { return items; }
    public void clear() { items = Collections.emptyList(); capturedAt = null; }
}
