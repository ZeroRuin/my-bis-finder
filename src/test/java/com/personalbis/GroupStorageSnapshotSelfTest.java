package com.personalbis;

import java.lang.reflect.Proxy;
import java.time.Instant;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;

/** Real event objects with mocked API interfaces; no login, credentials or game mutations. */
public final class GroupStorageSnapshotSelfTest
{
    public static void main(String[] args)
    {
        GroupStorageSnapshot snapshot = new GroupStorageSnapshot();
        Instant first = Instant.parse("2026-10-09T00:00:00Z");
        Item[] raw = {new Item(22325, 1), new Item(2440, 5), null, new Item(-1, 0), new Item(11832, 0), new Item(999, 1)};
        require(!snapshot.capture(event(InventoryID.INVENTORY.getId(), raw), GroupStorageSnapshotSelfTest::definition, () -> first), "inventory event ignored");
        require(!snapshot.hasSnapshot(), "ignored event creates no snapshot");
        require(snapshot.capture(event(InventoryID.GROUP_STORAGE.getId(), raw), GroupStorageSnapshotSelfTest::definition, () -> first), "shared event accepted");
        require(snapshot.getItems().size() == 2, "empty, zero and placeholder entries excluded");
        require(snapshot.getItems().get(0).getItemId() == 22325 && snapshot.getItems().get(1).getQuantity() == 5, "scythe and supplies retained");
        raw[0] = new Item(22325, 99);
        require(snapshot.getItems().get(0).getQuantity() == 1, "snapshot does not alias event array");
        Instant second = first.plusSeconds(60);
        snapshot.capture(event(InventoryID.GROUP_STORAGE.getId(), new Item[]{new Item(2440, 2)}), GroupStorageSnapshotSelfTest::definition, () -> second);
        require(snapshot.getItems().size() == 1 && snapshot.getItems().get(0).getQuantity() == 2, "updates replace rather than accumulate");
        require(snapshot.getCapturedAt().equals(second), "timestamp refreshed");
        snapshot.capture(event(InventoryID.GROUP_STORAGE.getId(), new Item[0]), GroupStorageSnapshotSelfTest::definition, () -> second);
        require(snapshot.hasSnapshot() && snapshot.getItems().isEmpty(), "empty storage differs from unscanned");
        snapshot.clear();
        require(!snapshot.hasSnapshot() && snapshot.getItems().isEmpty(), "logout reset clears data and timestamp");
        System.out.println("ALL GROUP STORAGE SNAPSHOT SELF-TESTS PASSED");
    }
    private static ItemContainerChanged event(int id, Item[] items)
    {
        ItemContainer container = (ItemContainer) Proxy.newProxyInstance(ItemContainer.class.getClassLoader(), new Class<?>[]{ItemContainer.class}, (o,m,a) -> {
            if (m.getName().equals("getItems")) return items;
            throw new AssertionError("Unexpected container call: " + m.getName());
        });
        return new ItemContainerChanged(id, container);
    }
    private static ItemComposition definition(int id)
    {
        return (ItemComposition) Proxy.newProxyInstance(ItemComposition.class.getClassLoader(), new Class<?>[]{ItemComposition.class}, (o,m,a) -> {
            if (m.getName().equals("getPlaceholderTemplateId")) return id == 999 ? 14401 : -1;
            if (m.getName().equals("getName")) return "Fixture item " + id;
            throw new AssertionError("Unexpected definition call: " + m.getName());
        });
    }
    private static void require(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
        System.out.println("PASS " + message);
    }
}
