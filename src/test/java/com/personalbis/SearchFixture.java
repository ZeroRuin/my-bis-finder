package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import com.personalbis.wiki.WikiEquipment;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemStats;

/**
 * Runs the real melee, ranged and magic searches from the bundled Wiki data, with no
 * game client. The account is maxed with every prayer, spellbook and quest unlocked.
 */
final class SearchFixture
{
    /** Items whose value comes from an effect, so random banks should contain them often. */
    private static final String[] NOTABLE = {
        "salve amulet", "slayer helmet", "black mask", "void ", "crystal ", "karil's", "ahrim's",
        "dharok's", "verac's", "amulet of the damned", "obsidian", "berserker necklace", "inquisitor",
        "virtus", "tome of", "twisted bow", "bow of faerdhinen", "dragon hunter", "arclight", "emberlight",
        "scorching bow", "purging staff", "keris", "leaf-bladed", "osmumten", "tumeken", "(e)",
        "dizana", "torva", "masori", "ancestral", "amulet of torture", "necklace of anguish", "occult"};

    final WikiDataPack pack = new WikiDataPack(new Gson());
    final AccountSnapshot account;
    final EquipmentIntelligence intelligence;
    final MeleeCombatCalculator meleeCalculator;
    final RangedCombatCalculator rangedCalculator;
    final MagicCombatCalculator magicCalculator;
    final List<MonsterDefinition> monsters;
    final MeleeLoadoutOptimizer meleeOptimizer;
    final RangedLoadoutOptimizer rangedOptimizer;
    final MagicLoadoutOptimizer magicOptimizer;
    private final Map<EquipmentSlot, List<WikiEquipment>> ordinary = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, List<WikiEquipment>> notable = new EnumMap<>(EquipmentSlot.class);
    private final List<BankItem> runes = new ArrayList<>();

    SearchFixture()
    {
        Client client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class}, (proxy, method, args) -> {
            switch (method.getName())
            {
                case "getGameState": return GameState.LOGGED_IN;
                case "getRealSkillLevel":
                case "getBoostedSkillLevel": return 99;
                case "getVarbitValue": return 8;              // every unlock varbit set
                case "getIntStack": return new int[]{2};      // every quest finished
                default:
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) return false;
                    if (type == int.class) return 0;
                    if (type == long.class) return 0L;
                    if (type == double.class) return 0d;
                    if (type == float.class) return 0f;
                    return null;
            }
        });
        account = new AccountSnapshot(client);
        account.refresh();
        intelligence = new EquipmentIntelligence(null, new ItemRequirementRegistry(client), pack)
        {
            @Override
            int canonicalize(int itemId)
            {
                return itemId;
            }

            @Override
            ItemStats itemStats(int id)
            {
                WikiEquipment wiki = pack.getEquipment(id);
                if (wiki == null || wiki.slot == null) return null;
                EquipmentSlot slot = slotOf(wiki);
                if (slot == null) return null;
                return new ItemStats(true, 0, 0, ItemEquipmentStats.builder().slot(slot.getSlotIndex()).build());
            }
        };
        meleeCalculator = new MeleeCombatCalculator(account);
        rangedCalculator = new RangedCombatCalculator(account);
        magicCalculator = new MagicCombatCalculator(account);
        meleeOptimizer = new MeleeLoadoutOptimizer(meleeCalculator);
        rangedOptimizer = new RangedLoadoutOptimizer(rangedCalculator, account);
        magicOptimizer = new MagicLoadoutOptimizer(magicCalculator, account);
        monsters = MonsterDatabase.catalogue(pack.getMonsters());

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ordinary.put(slot, new ArrayList<>());
            notable.put(slot, new ArrayList<>());
        }
        for (WikiEquipment item : pack.getEquipment())
        {
            EquipmentSlot slot = slotOf(item);
            if (slot == null || pack.getEquipment(item.id) != item || item.id == ItemIds.DEADMAN_CAPE) continue;
            String version = item.version == null ? "" : item.version;
            if (version.equalsIgnoreCase("Broken") || item.name.startsWith("Broken")) continue;
            String lower = item.name.toLowerCase();
            boolean special = false;
            for (String word : NOTABLE) special |= lower.contains(word);
            if (special) notable.get(slot).add(item);
            else if (useful(item)) ordinary.get(slot).add(item);
        }
        Set<String> runeNames = new LinkedHashSet<>();
        for (MagicSpell spell : MagicSpell.combat()) runeNames.addAll(spell.runes.keySet());
        int id = 900000;
        for (String rune : runeNames) runes.add(new BankItem(id++, 100000, rune));
    }

    OptimizedLoadout melee(List<BankItem> bank, AttackStyle style, MonsterDefinition monster, boolean task)
    {
        return meleeOptimizer.optimize(intelligence.rank(bank, style, task), style, monster, task);
    }

    OptimizedRangedLoadout ranged(List<BankItem> bank, MonsterDefinition monster, boolean task)
    {
        return rangedOptimizer.optimize(intelligence.rank(bank, AttackStyle.RANGED, task), monster, task);
    }

    OptimizedMagicLoadout magic(List<BankItem> bank, MonsterDefinition monster, boolean task)
    {
        return magicOptimizer.optimize(bank, intelligence.rank(bank, AttackStyle.MAGIC, task), monster, task);
    }

    private static EquipmentSlot slotOf(WikiEquipment item)
    {
        if (item == null || item.slot == null) return null;
        try
        {
            return EquipmentSlot.valueOf(item.slot.toUpperCase());
        }
        catch (IllegalArgumentException unknown)
        {
            return null;
        }
    }

    private static boolean useful(WikiEquipment item)
    {
        if (slotOf(item) == EquipmentSlot.WEAPON || slotOf(item) == EquipmentSlot.AMMO) return true;
        WikiEquipment.CombatStats o = item.offensive;
        WikiEquipment.Bonuses b = item.bonuses;
        return (o != null && (o.stab > 0 || o.slash > 0 || o.crush > 0 || o.magic > 0 || o.ranged > 0))
            || (b != null && (b.str > 0 || b.ranged_str > 0 || b.magic_str > 0 || b.prayer > 0));
    }

    /** The name the game shows: Barrows pieces carry their charge state ("Karil's coif 75"). */
    static String bankName(WikiEquipment item)
    {
        String version = item.version == null ? "" : item.version;
        return Arrays.asList("0", "25", "50", "75", "100").contains(version) ? item.name + " " + version : item.name;
    }

    BankItem bankItem(int id)
    {
        WikiEquipment item = pack.getEquipment(id);
        if (item == null) throw new IllegalArgumentException("No equipment with id " + id);
        return new BankItem(id, 1, bankName(item));
    }

    BankItem bankItem(String name)
    {
        for (WikiEquipment item : pack.getEquipment())
            if (pack.getEquipment(item.id) == item && bankName(item).equalsIgnoreCase(name)) return new BankItem(item.id, 1, bankName(item));
        throw new IllegalArgumentException("No equipment named " + name);
    }

    MonsterDefinition monster(String name)
    {
        for (MonsterDefinition m : monsters) if (m.getName().equalsIgnoreCase(name)) return m;
        throw new IllegalArgumentException("No monster named " + name);
    }

    /**
     * A random bank holding {@code perSlot} ordinary items in each slot ({@code weapons} in the
     * weapon slot), plus effect items with the given chance per slot, plus every rune.
     */
    List<BankItem> randomBank(Random random, int perSlot, int weapons, double notableChance)
    {
        List<BankItem> bank = new ArrayList<>(runes);
        Set<Integer> used = new LinkedHashSet<>();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            int count = slot == EquipmentSlot.WEAPON ? weapons : perSlot;
            for (int i = 0; i < count; i++) add(bank, used, pick(random, ordinary.get(slot)));
            if (random.nextDouble() < notableChance) add(bank, used, pick(random, notable.get(slot)));
            if (random.nextDouble() < notableChance / 2) add(bank, used, pick(random, notable.get(slot)));
        }
        return bank;
    }

    private static WikiEquipment pick(Random random, List<WikiEquipment> pool)
    {
        return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
    }

    private static void add(List<BankItem> bank, Set<Integer> used, WikiEquipment item)
    {
        if (item != null && used.add(item.id)) bank.add(new BankItem(item.id, 500, bankName(item)));
    }

    /** Half the time a target with an attribute (undead, demon, dragon...), so item effects are exercised. */
    MonsterDefinition randomMonster(Random random)
    {
        while (true)
        {
            MonsterDefinition monster = monsters.get(random.nextInt(monsters.size()));
            if (monster.getHitpoints() <= 0) continue;
            if (random.nextBoolean() || !monster.getAttributes().isEmpty()) return monster;
        }
    }
}
