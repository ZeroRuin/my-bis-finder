package com.personalbis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.ItemComposition;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.ScriptID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.ItemQuantityMode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.bank.BankSearch;

/**
 * Personal BiS native bank view.
 *
 * The RuneLite bank search first restricts the bank to owned recommended items.
 * After the bank finishes building, the existing bank item widgets are reused
 * and laid out under OSRS-style section headers for each combat style.
 */
@Singleton
public class PersonalBisBankFilter
{
    private static final String TOKEN = "personalbis";
    private static final int ITEMS_PER_ROW = 8;
    private static final int ITEM_X_START = 51;
    private static final int ITEM_X_SPACING = 48;
    private static final int ITEM_Y_SPACING = 36;
    private static final int HEADER_HEIGHT = 20;
    private static final int DIVIDER_HEIGHT = 5;
    private static final int SECTION_GAP = 5;
    private static final int BANK_ITEM_WIDTH = 36;
    private static final int BANK_ITEM_HEIGHT = 32;
    private static final int HEADER_COLOR = new Color(228, 216, 162).getRGB();
    private static final int BEST_COLOR = new Color(80, 255, 80).getRGB();
    private static final int DIVIDER_COLOR = new Color(63, 54, 42).getRGB();

    private final Client client;
    private final ClientThread clientThread;
    private final BankSearch bankSearch;
    private final ItemManager itemManager;
    private final Set<Integer> recommendedItemIds = new HashSet<>();
    private final Map<AttackStyle, List<Integer>> recommendations = new EnumMap<>(AttackStyle.class);
    private final List<AttackStyle> styleOrder = new ArrayList<>();
    private final List<Widget> addedWidgets = new ArrayList<>();
    private final List<Widget> hiddenNativeDecorations = new ArrayList<>();
    private final List<RecommendedSupply> supplies = new ArrayList<>();

    private boolean active;
    private AttackStyle bestStyle;
    private String targetHeader = "My BiS Finder";
    private int originalContainerChildren = -1;

    @Inject
    public PersonalBisBankFilter(Client client, ClientThread clientThread, BankSearch bankSearch, ItemManager itemManager)
    {
        this.client = client;
        this.clientThread = clientThread;
        this.bankSearch = bankSearch;
        this.itemManager = itemManager;
    }

    public void setRecommendedItemIds(Set<Integer> ids)
    {
        setRecommendations(Collections.emptyMap(), Collections.emptyList(), ids);
    }

    public void setRecommendations(Map<AttackStyle, List<Integer>> byStyle, List<AttackStyle> order, Set<Integer> allIds)
    {
        recommendations.clear();
        for (Map.Entry<AttackStyle, List<Integer>> entry : byStyle.entrySet())
        {
            recommendations.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        styleOrder.clear();
        styleOrder.addAll(order);
        recommendedItemIds.clear();
        recommendedItemIds.addAll(allIds);

        if (active)
        {
            clientThread.invokeLater(bankSearch::layoutBank);
        }
    }


    public void setBestStyle(AttackStyle style)
    {
        bestStyle = style;
        if (active) clientThread.invokeLater(bankSearch::layoutBank);
    }

    public void setTarget(MonsterDefinition monster)
    {
        targetHeader = SlayerTargetRequirements.bankHeader(monster);
        if (active) clientThread.invokeLater(bankSearch::layoutBank);
    }

    public void setSupplies(List<RecommendedSupply> items)
    {
        supplies.clear();
        if (items != null) supplies.addAll(items);
        for (RecommendedSupply item : supplies) recommendedItemIds.add(itemManager.canonicalize(item.itemId));
        if (active) clientThread.invokeLater(bankSearch::layoutBank);
    }

    public boolean isActive()
    {
        return active;
    }

    public void refreshLayout()
    {
        if (active)
        {
            clientThread.invokeLater(bankSearch::layoutBank);
        }
    }

    public void toggle()
    {
        clientThread.invokeLater(() ->
        {
            if (active)
            {
                deactivateOnClientThread();
                return;
            }

            active = true;
            // Dedicated Personal BiS view: keep the complete native bank item set.
            // We only rearrange widgets after RuneLite finishes building the bank.
            bankSearch.layoutBank();
        });
    }

    public void deactivate()
    {
        clientThread.invokeLater(this::deactivateOnClientThread);
    }

    private void deactivateOnClientThread()
    {
        if (!active)
        {
            return;
        }
        active = false;
        clearAddedWidgets();
        restoreNativeDecorations();
        bankSearch.reset(true);
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event)
    {
        if (!active || !"bankSearchFilter".equals(event.getEventName()))
        {
            return;
        }
        int[] intStack = client.getIntStack();
        int size = client.getIntStackSize();
        if (size < 2)
        {
            return;
        }
        // Personal BiS is a full-bank view, not a search filter. Keep every item.
        intStack[size - 2] = 1;
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        if (!active || event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING)
        {
            return;
        }

        Widget itemContainer = client.getWidget(InterfaceID.Bankmain.ITEMS);
        if (itemContainer == null)
        {
            return;
        }

        clientThread.invokeAtTickEnd(() -> layoutSections(itemContainer));
    }

    @Subscribe(priority = -1)
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!active || event.getParam1() != InterfaceID.Bankmain.ITEMS)
        {
            return;
        }

        Widget widget = event.getWidget();
        if (widget == null || widget.getItemId() < 0)
        {
            return;
        }

        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null)
        {
            return;
        }

        int realIndex = findRealIndex(bank, widget.getItemId());
        if (realIndex < 0)
        {
            return;
        }

        MenuEntry menu = event.getMenuEntry();
        if (menu != null && menu.getParam0() != realIndex)
        {
            menu.setParam0(realIndex);
        }
    }

    private void layoutSections(Widget itemContainer)
    {
        clearAddedWidgets();
        restoreNativeDecorations();

        Widget[] children = itemContainer.getDynamicChildren();
        if (children == null || children.length == 0)
        {
            return;
        }

        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null)
        {
            return;
        }

        // Preserve the native item widgets. Reuse each widget exactly once so withdraw
        // actions remain RuneLite/OSRS-native and do not require rebuilding on each click.
        List<Widget> itemWidgets = new ArrayList<>();
        for (Widget child : children)
        {
            if (child == null) continue;
            if (child.getItemId() >= 0)
            {
                itemWidgets.add(child);
                child.setHidden(true);
            }
            else if (isNativeBankDivider(child))
            {
                // Default tab dividers retain their old absolute Y positions after we
                // rearrange native item widgets. Hide them in Personal BiS mode.
                child.setHidden(true);
                hiddenNativeDecorations.add(child);
            }
        }

        Map<Integer, Widget> ownedWidgets = new java.util.HashMap<>();
        Map<Integer, Widget> exactOwnedWidgets = new java.util.HashMap<>();
        for (Widget widget : itemWidgets)
        {
            Widget exactPrevious = exactOwnedWidgets.get(widget.getItemId());
            if (exactPrevious == null || (exactPrevious.getItemQuantity() <= 0 && widget.getItemQuantity() > 0))
                exactOwnedWidgets.put(widget.getItemId(), widget);
            int canonical = itemManager.canonicalize(widget.getItemId());
            Widget previous = ownedWidgets.get(canonical);
            if (previous == null || (previous.getItemQuantity() <= 0 && widget.getItemQuantity() > 0))
                ownedWidgets.put(canonical, widget);
        }

        Set<Widget> used = new HashSet<>();
        int y = 0;
        y = addHeader(itemContainer, targetHeader, y, true);

        List<AttackStyle> order = styleOrder.isEmpty() ? Arrays.asList(AttackStyle.values()) : styleOrder;
        for (AttackStyle style : order)
        {
            List<Integer> ids = recommendations.get(style);
            if (ids == null || ids.isEmpty()) continue;

            y = addDivider(itemContainer, y);
            String title = displayStyle(style);
            if (style == bestStyle) title += "  <col=50ff50>Best style</col>";
            y = addHeader(itemContainer, title, y, style == bestStyle);

            int itemInSection = 0;
            for (Integer itemId : ids)
            {
                if (itemId == null) continue;
                Widget widget = ownedWidgets.get(itemManager.canonicalize(itemId));
                if (widget == null || used.contains(widget)) continue;
                used.add(widget);
                widget.setHidden(false);
                placeItem(widget, itemInSection++, y);
            }
            int rows = Math.max(1, (itemInSection + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
            y += rows * ITEM_Y_SPACING + SECTION_GAP;
        }

        if (!supplies.isEmpty())
        {
            y = addDivider(itemContainer, y);
            y = addHeader(itemContainer, "Recommended", y, false);
            int itemInSection = 0;
            for (RecommendedSupply supply : supplies)
            {
                // Potion doses canonicalise to the 4-dose ID. Supplies must use
                // their exact owned ID or a stale 4-dose placeholder is shown.
                Widget widget = exactOwnedWidgets.get(supply.itemId);
                if (widget == null || used.contains(widget)) continue;
                if (widget.getItemQuantity() <= 0) continue;
                used.add(widget);
                widget.setHidden(false);
                placeItem(widget, itemInSection++, y);
            }
            int rows = Math.max(1, (itemInSection + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW);
            y += rows * ITEM_Y_SPACING + SECTION_GAP;
        }

        y = addDivider(itemContainer, y);
        y = addHeader(itemContainer, "Other items", y, false);
        int otherIndex = 0;
        for (Widget widget : itemWidgets)
        {
            if (used.contains(widget)) continue;
            widget.setHidden(false);
            placeItem(widget, otherIndex++, y);
        }
        int otherRows = (otherIndex + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW;
        y += otherRows * ITEM_Y_SPACING + SECTION_GAP;

        itemContainer.setScrollHeight(Math.max(itemContainer.getHeight(), y + 10));
        itemContainer.revalidate();
        int scrollY = itemContainer.getScrollY();
        clientThread.invokeLater(() -> client.runScript(
            ScriptID.UPDATE_SCROLLBAR,
            InterfaceID.Bankmain.SCROLLBAR,
            InterfaceID.Bankmain.ITEMS,
            scrollY));
    }

    private Widget nextUsableItemWidget(Widget[] children, int start)
    {
        for (int i = Math.max(0, start); i < children.length; i++)
        {
            Widget child = children[i];
            if (child != null && child.getItemId() >= 0)
            {
                return child;
            }
        }
        return null;
    }

    private int indexAfter(Widget[] children, Widget widget, int start)
    {
        for (int i = Math.max(0, start); i < children.length; i++)
        {
            if (children[i] == widget)
            {
                return i + 1;
            }
        }
        return start + 1;
    }

    private void configureBankItem(Widget widget, int itemId, int quantity)
    {
        int canonicalId = itemManager.canonicalize(itemId);
        ItemComposition normalDef = client.getItemDefinition(canonicalId);
        int displayId = canonicalId;
        boolean placeholder = quantity <= 0;

        if (placeholder)
        {
            int placeholderId = normalDef.getPlaceholderId();
            if (placeholderId > 0)
            {
                displayId = placeholderId;
            }
        }

        ItemComposition displayDef = client.getItemDefinition(displayId);
        widget.setOriginalWidth(BANK_ITEM_WIDTH);
        widget.setOriginalHeight(BANK_ITEM_HEIGHT);
        widget.setItemId(displayId);
        widget.setItemQuantity(Math.max(0, quantity));
        widget.setItemQuantityMode(placeholder ? ItemQuantityMode.NEVER : ItemQuantityMode.ALWAYS);
        widget.setOpacity(0);
        widget.setName("<col=ff9040>" + normalDef.getName() + "</col>");
        widget.setHidden(false);
        widget.setDragDeadTime(1000);
        widget.clearActions();
        if (placeholder)
        {
            widget.setAction(0, "Release");
        }
        else
        {
            widget.setAction(0, "Withdraw-1");
            widget.setAction(1, "Withdraw-5");
            widget.setAction(2, "Withdraw-10");
            widget.setAction(3, "Withdraw-X");
            widget.setAction(4, "Withdraw-All");
        }
        widget.setAction(10, "Examine");
        widget.revalidate();
    }

    private void configureRecommendedItem(Widget widget, int itemId, int owned, int recommended)
    {
        configureBankItem(widget, itemId, owned);
        ItemComposition def = client.getItemDefinition(itemManager.canonicalize(itemId));
        widget.setItemQuantity(Math.max(1, recommended));
        widget.setItemQuantityMode(ItemQuantityMode.ALWAYS);
        widget.setName("<col=ff9040>" + def.getName() + "</col><br><col=ffff00>Recommended: " + recommended + "</col><br>Bank: " + owned);
        widget.revalidate();
    }

    private int countOwned(ItemContainer bank, int canonicalId)
    {
        int count = 0;
        for (Item item : bank.getItems())
        {
            if (item != null && item.getId() > 0 && item.getQuantity() > 0
                && itemManager.canonicalize(item.getId()) == canonicalId)
            {
                count += item.getQuantity();
            }
        }
        return count;
    }

    private int findRealIndex(ItemContainer bank, int displayedItemId)
    {
        int canonicalId = itemManager.canonicalize(displayedItemId);
        Item[] items = bank.getItems();
        for (int i = 0; i < items.length; i++)
        {
            Item item = items[i];
            if (item != null && item.getId() > 0 && itemManager.canonicalize(item.getId()) == canonicalId)
            {
                return i;
            }
        }
        return -1;
    }

    private void placeItem(Widget widget, int itemIndex, int sectionY)
    {
        int x = ITEM_X_START + (itemIndex % ITEMS_PER_ROW) * ITEM_X_SPACING;
        int y = sectionY + (itemIndex / ITEMS_PER_ROW) * ITEM_Y_SPACING;
        widget.setOriginalX(x);
        widget.setOriginalY(y);
        widget.revalidate();
    }

    private int addHeader(Widget container, String text, int y, boolean best)
    {
        Widget label = container.createChild(-1, WidgetType.TEXT);
        label.setOriginalWidth(ITEMS_PER_ROW * ITEM_X_SPACING - ITEM_X_START);
        label.setOriginalHeight(16);
        // Align section headings with the first bank item rather than the bank container edge.
        label.setOriginalX(ITEM_X_START);
        label.setOriginalY(y + 2);
        label.setText(text);
        label.setFontId(FontID.PLAIN_11);
        label.setTextColor(best ? BEST_COLOR : HEADER_COLOR);
        label.setTextShadowed(true);
        label.revalidate();
        addedWidgets.add(label);
        return y + HEADER_HEIGHT;
    }

    private boolean isNativeBankDivider(Widget widget)
    {
        // Bank tab separators are thin, non-item graphical/rectangle children.
        // Restrict this deliberately so text, controls and native item widgets survive.
        return widget.getItemId() < 0
            && widget.getHeight() <= 3
            && widget.getWidth() >= 100;
    }

    private void restoreNativeDecorations()
    {
        for (Widget widget : hiddenNativeDecorations)
        {
            if (widget != null) widget.setHidden(false);
        }
        hiddenNativeDecorations.clear();
    }

    private int addDivider(Widget container, int y)
    {
        Widget line = container.createChild(-1, WidgetType.RECTANGLE);
        // Match the understated native bank-tab separator: one dark pixel,
        // spanning the same horizontal item area as the bank's own dividers.
        line.setOriginalX(ITEM_X_START);
        line.setOriginalY(y + 1);
        line.setOriginalWidth(ITEMS_PER_ROW * ITEM_X_SPACING - 4);
        line.setOriginalHeight(1);
        line.setTextColor(DIVIDER_COLOR);
        line.setFilled(true);
        line.setOpacity(0);
        line.revalidate();
        addedWidgets.add(line);
        return y + DIVIDER_HEIGHT;
    }

    private void clearAddedWidgets()
    {
        if (addedWidgets.isEmpty())
        {
            return;
        }
        for (Widget widget : addedWidgets)
        {
            if (widget != null)
            {
                widget.setHidden(true);
            }
        }
        addedWidgets.clear();
    }

    private String displayStyle(AttackStyle style)
    {
        switch (style)
        {
            case MELEE_STAB: return "Melee (Stab)";
            case MELEE_SLASH: return "Melee (Slash)";
            case MELEE_CRUSH: return "Melee (Crush)";
            case RANGED: return "Ranged";
            case MAGIC: return "Magic";
            default: return style.toString();
        }
    }
}
