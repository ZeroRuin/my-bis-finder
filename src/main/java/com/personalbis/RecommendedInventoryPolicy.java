package com.personalbis;
import java.util.List;

/** Alpha 49 invariants for the deliberately-simple Recommended bank section. */
public final class RecommendedInventoryPolicy
{
    private RecommendedInventoryPolicy(){}

    public static boolean ownedAndCapped(RecommendedSupply supply, List<BankItem> bank)
    {
        if (supply == null || supply.quantity <= 0 || bank == null) return false;
        for (BankItem item : bank)
        {
            if (item != null && item.getItemId() == supply.itemId && item.getQuantity() > 0)
                return supply.quantity <= item.getQuantity();
        }
        return false;
    }

    public static boolean allOwnedAndCapped(List<RecommendedSupply> supplies, List<BankItem> bank)
    {
        if (supplies == null) return false;
        for (RecommendedSupply supply : supplies)
            if (!ownedAndCapped(supply, bank)) return false;
        return true;
    }
}
