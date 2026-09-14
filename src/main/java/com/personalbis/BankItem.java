package com.personalbis;

public final class BankItem
{
    private final int itemId;
    private final int quantity;
    private final String name;

    public BankItem(int itemId, int quantity, String name)
    {
        this.itemId = itemId;
        this.quantity = quantity;
        this.name = name;
    }

    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public String getName() { return name; }
}
