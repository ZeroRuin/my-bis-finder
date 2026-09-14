package com.personalbis;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemManager;

public class BankScanner
{
    private final Client client;
    private final ItemManager itemManager;

    @Inject
    public BankScanner(Client client, ItemManager itemManager)
    {
        this.client = client;
        this.itemManager = itemManager;
    }

    public List<BankItem> scan()
    {
        List<BankItem> result = new ArrayList<>();
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);

        if (bank == null)
        {
            return result;
        }

        for (Item item : bank.getItems())
        {
            if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
            {
                continue;
            }

            ItemComposition definition = client.getItemDefinition(item.getId());
            // Bank placeholders can be represented with a positive container
            // quantity even though the bank widget displays zero. Exclude the
            // placeholder item variant using its definition metadata.
            if (definition.getPlaceholderTemplateId() >= 0)
            {
                continue;
            }

            // Keep the snapshot cheap, but preserve the item name because Slayer
            // equipment detection and owned-supply selection are name based.
            // Client item definitions are already cached by RuneLite.
            // Preserve the real dose-specific item definition. Canonicalising here
            // turns every potion dose into the 4-dose name, so an owned (2) dose
            // can be mistaken for a zero-quantity (4) placeholder later.
            String name = definition.getName();
            result.add(new BankItem(item.getId(), item.getQuantity(), name == null ? "" : name));
        }

        return result;
    }
}
