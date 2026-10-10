package com.personalbis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;

public final class ItemExclusionSelfTest
{
    public static void main(String[] args) throws Exception
    {
        ItemExclusionList policy = new ItemExclusionList(" BLOOD RUNE, Shark\nSuper restore(4), Ruby bolts (e), Abyssal whip");
        require(policy.excludes("Blood rune", "Blood rune"), "case and whitespace ignored");
        require(!policy.excludes("Blood essence", "Blood essence"), "exact names do not block related items");
        require(policy.excludes("Super restore(1)", "Super restore(4)"), "all potion doses excluded");
        require(!policy.excludes("Diamond bolts (e)", "Diamond bolts (e)"), "other enchanted ammunition remains eligible");
        require(policy.excludes("Abyssal whip (or)", "Abyssal whip"), "canonical cosmetic variants excluded");
        require(policy.sameAs(new ItemExclusionList("Shark, blood rune, Abyssal whip, Ruby bolts (e), Super restore(2), Shark")), "ordering and duplicates do not mark results stale");
        ItemExclusionList changed = new ItemExclusionList("Air rune");
        require(!policy.sameAs(changed) && policy.excludes("Shark", "Shark"), "existing generation retains its immutable policy while edits are batched");
        require(!new ItemExclusionList("").excludes("Shark", "Shark"), "clearing settings restores eligibility");

        List<BankItem> stores = Arrays.asList(new BankItem(565, 100, "Blood rune"),
            new BankItem(385, 20, "Shark"), new BankItem(373, 20, "Swordfish"),
            new BankItem(3026, 3, "Super restore(2)"), new BankItem(2434, 2, "Prayer potion(4)"));
        List<BankItem> allowed = policy.filter(stores, id -> "");
        List<RecommendedSupply> supplies = SimpleSupplyRecommender.recommend(AttackStyle.MELEE_STAB, null, null, allowed, null, 99);
        require(supplies.stream().noneMatch(s -> s.itemId == 385 || s.itemId == 3026 || s.itemId == 565), "excluded supplies removed before recommendation");
        require(supplies.stream().anyMatch(s -> s.itemId == 373) && supplies.stream().anyMatch(s -> s.itemId == 2434), "lower food and prayer-potion alternatives selected");
        require(stores.size() == 5 && stores.get(0).getQuantity() == 100, "ownership snapshots are not mutated");

        MagicSpell spell = new MagicSpell("Fixture spell", 1, 1);
        spell.runes.put("Blood rune", 1);
        MagicLoadoutOptimizer optimizer = new MagicLoadoutOptimizer(null, null, null);
        Method available = MagicLoadoutOptimizer.class.getDeclaredMethod("runesAvailable", MagicSpell.class, List.class, EquipmentCandidate.class, java.util.Collection.class);
        available.setAccessible(true);
        require((Boolean) available.invoke(optimizer, spell, stores, null, Collections.emptyList()), "owned rune permits spell before exclusion");
        require(!(Boolean) available.invoke(optimizer, spell, allowed, null, Collections.emptyList()), "excluded rune prevents dependent spell");
        MagicSpell airSpell = new MagicSpell("Fixture air spell", 1, 1);
        airSpell.runes.put("Air rune", 1);
        EquipmentCandidate staff = new EquipmentCandidate(new BankItem(1381, 1, "Staff of air"), EquipmentSlot.WEAPON,
            0, 0, 0, 0, 0, 0, 0f, 0, 5, false, "", RequirementResult.usable("test"));
        require((Boolean) available.invoke(optimizer, airSpell, Collections.emptyList(), staff, Collections.singletonList(staff)), "allowed rune-providing staff still permits spell");
        bankMenuScope();
        System.out.println("ALL ITEM EXCLUSION SELF-TESTS PASSED");
    }

    @SuppressWarnings("unchecked")
    private static void bankMenuScope() throws Exception
    {
        AtomicInteger added = new AtomicInteger();
        MenuEntry created = (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(), new Class<?>[]{MenuEntry.class},
            (o, m, a) -> m.getReturnType() == MenuEntry.class ? o : null);
        Client client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class}, (o, m, a) -> {
            if (m.getName().equals("createMenuEntry")) { added.incrementAndGet(); return created; }
            throw new AssertionError(m.getName());
        });
        Widget widget = (Widget) Proxy.newProxyInstance(Widget.class.getClassLoader(), new Class<?>[]{Widget.class}, (o, m, a) -> {
            if (m.getName().equals("getItemId")) return 385;
            throw new AssertionError(m.getName());
        });
        MenuEntry examine = (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(), new Class<?>[]{MenuEntry.class}, (o, m, a) -> {
            if (m.getName().equals("getOption")) return "Examine";
            if (m.getName().equals("getWidget")) return widget;
            if (m.getName().equals("getTarget")) return "Shark";
            throw new AssertionError(m.getName());
        });
        PersonalBisBankFilter filter = new PersonalBisBankFilter(client, null, null, null, null);
        Field active = PersonalBisBankFilter.class.getDeclaredField("active"); active.setAccessible(true);
        Field generated = PersonalBisBankFilter.class.getDeclaredField("generatedWidgets"); generated.setAccessible(true);
        Set<Widget> widgets = (Set<Widget>) generated.get(filter);
        widgets.add(widget);
        filter.onMenuEntryAdded(new MenuEntryAdded(examine));
        require(added.get() == 0, "ordinary bank has no exclusion menu");
        active.setBoolean(filter, true); widgets.clear();
        filter.onMenuEntryAdded(new MenuEntryAdded(examine));
        require(added.get() == 0, "Other items section has no exclusion menu");
        widgets.add(widget); filter.onMenuEntryAdded(new MenuEntryAdded(examine));
        require(added.get() == 1, "generated recommendation gets exclusion menu");
    }

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS " + label);
    }
}
