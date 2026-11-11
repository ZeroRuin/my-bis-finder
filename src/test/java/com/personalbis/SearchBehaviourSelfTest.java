package com.personalbis;

import com.personalbis.wiki.WikiEquipment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Checks what the searches recommend for hand-built banks. These replace older tests that
 * looked for exact lines of source code, so the search can be restructured freely as long
 * as its recommendations stay right.
 */
public final class SearchBehaviourSelfTest
{
    private static final SearchFixture FIXTURE = new SearchFixture();
    private static final List<String> RUNES = Arrays.asList("Air rune", "Water rune", "Earth rune", "Fire rune", "Death rune", "Blood rune", "Chaos rune", "Wrath rune", "Soul rune", "Mind rune");

    public static void main(String[] args)
    {
        emptySlotsStayEmptyWhenEveryItemHurts();
        emptySlotsAreFilledWithHarmlessItems();
        everyOwnedWeaponIsTried();
        everyOwnedAmmoIsTried();
        internalAmmoWeaponsNeverWearProjectiles();
        rangedStrengthRaisesMaxHit();
        ownedSetsAreNeverBeaten();
        barrowsChargeStates();
        magicWeaponsAreNotGatedByRank();
        suppliesUseFullOwnedStacks();
        System.out.println("ALL SEARCH BEHAVIOUR SELF-TESTS PASSED");
    }

    private static void emptySlotsStayEmptyWhenEveryItemHurts()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        OptimizedRangedLoadout ranged = FIXTURE.ranged(bank("Magic shortbow", "Rune arrow", "Rune platebody"), target, false);
        require(ranged != null && !ranged.getItems().containsKey(EquipmentSlot.BODY), "ranged leaves the body slot empty rather than wear a ranged-attack penalty");
        OptimizedLoadout melee = FIXTURE.melee(bank("Abyssal whip", "Black d'hide shield", "Archer helm"), AttackStyle.MELEE_SLASH, target, false);
        require(melee != null && !melee.getItems().containsKey(EquipmentSlot.SHIELD) && !melee.getItems().containsKey(EquipmentSlot.HEAD),
            "melee leaves slots empty rather than wear a slash penalty");
    }

    private static void emptySlotsAreFilledWithHarmlessItems()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        List<BankItem> bank = bank("Abyssal whip", "Magic shortbow", "Rune arrow", "Staff of air", "Holy blessing", "Ring of the gods", "Climbing boots");
        OptimizedLoadout melee = FIXTURE.melee(bank, AttackStyle.MELEE_SLASH, target, false);
        require(wears(melee.getItems(), "Ring of the gods") && wears(melee.getItems(), "Holy blessing") && wears(melee.getItems(), "Climbing boots"), "melee fills free slots with items that do not slow the kill");
        OptimizedMagicLoadout magic = FIXTURE.magic(bank, target, false);
        require(magic != null && wears(magic.getItems(), "Ring of the gods") && wears(magic.getItems(), "Holy blessing"), "magic fills free slots with items that do not slow the kill");
        OptimizedRangedLoadout ranged = FIXTURE.ranged(bank, target, false);
        require(wears(ranged.getItems(), "Ring of the gods") && wears(ranged.getItems(), "Rune arrow"), "ranged fills free slots and keeps its arrows");
    }

    private static void everyOwnedWeaponIsTried()
    {
        // Only leaf-bladed weapons damage a Kurask, and the sword ranks below every other weapon here.
        MonsterDefinition kurask = FIXTURE.monster("Kurask");
        List<BankItem> bank = bank("Leaf-bladed sword", "Abyssal whip", "Dragon scimitar", "Abyssal tentacle", "Blade of saeldor", "Dragon longsword", "Rune scimitar",
            "Saradomin sword", "Dragon sword", "Abyssal dagger", "Dragon dagger", "Osmumten's fang", "Ghrazi rapier", "Zamorakian hasta", "Dragon hasta");
        for (AttackStyle style : new AttackStyle[]{AttackStyle.MELEE_STAB, AttackStyle.MELEE_SLASH})
        {
            OptimizedLoadout best = FIXTURE.melee(bank, style, kurask, true);
            require(best != null && wears(best.getItems(), "Leaf-bladed sword"), "the only weapon that can damage the target is found for " + style + " despite a low rank");
        }
    }

    private static void everyOwnedAmmoIsTried()
    {
        MonsterDefinition kurask = FIXTURE.monster("Kurask");
        List<BankItem> bank = bank("Rune crossbow", "Broad bolts", "Runite bolts", "Adamant bolts", "Mithril bolts", "Steel bolts", "Iron bolts", "Bronze bolts",
            "Diamond bolts (e)", "Ruby bolts (e)", "Emerald bolts (e)", "Sapphire bolts (e)", "Pearl bolts (e)", "Opal bolts (e)", "Topaz bolts (e)", "Jade bolts (e)", "Rune arrow", "Dragon arrow");
        OptimizedRangedLoadout best = FIXTURE.ranged(bank, kurask, true);
        require(best != null && wears(best.getItems(), "Broad bolts"), "the only ammunition that can damage the target is found among many stronger bolts");
        OptimizedRangedLoadout ordinary = FIXTURE.ranged(bank, FIXTURE.monster("Abyssal demon"), false);
        EquipmentCandidate ammo = ordinary.getItems().get(EquipmentSlot.AMMO);
        require(ammo != null && RangedAmmoRules.compatible(ordinary.getItems().get(EquipmentSlot.WEAPON), ammo) && !ammo.getItem().getName().contains("arrow"), "a crossbow is only given bolts it can fire");
    }

    private static void internalAmmoWeaponsNeverWearProjectiles()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        OptimizedRangedLoadout best = FIXTURE.ranged(bank("Crystal bow", "Rune arrow", "Dragon arrow", "Holy blessing"), target, false);
        require(best != null && wears(best.getItems(), "Crystal bow") && wears(best.getItems(), "Holy blessing"), "a bow with its own ammunition wears a blessing, not arrows");
        OptimizedRangedLoadout noBlessing = FIXTURE.ranged(bank("Crystal bow", "Rune arrow", "Dragon arrow"), target, false);
        require(noBlessing != null && !noBlessing.getItems().containsKey(EquipmentSlot.AMMO), "a bow with its own ammunition leaves the ammo slot empty rather than wear arrows");
    }

    private static void rangedStrengthRaisesMaxHit()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked = FIXTURE.intelligence.rank(bank("Magic shortbow", "Bronze arrow", "Amethyst arrow"), AttackStyle.RANGED, false);
        int weak = FIXTURE.rangedCalculator.calculate(gear(ranked, "Magic shortbow", "Bronze arrow"), target, false).getMaxHit();
        int strong = FIXTURE.rangedCalculator.calculate(gear(ranked, "Magic shortbow", "Amethyst arrow"), target, false).getMaxHit();
        require(strong > weak, "the exact ranged calculation counts ammunition Ranged Strength (max hit " + weak + " -> " + strong + ")");
    }

    /** A complete owned set must never lose to the search's own answer, even beside stronger ordinary pieces. */
    private static void ownedSetsAreNeverBeaten()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        String[] distractors = {"Torva full helm", "Torva platebody", "Torva platelegs", "Masori mask (f)", "Masori body (f)", "Masori chaps (f)",
            "Ancestral hat", "Ancestral robe top", "Ancestral robe bottom", "Amulet of fury", "Barrows gloves", "Dragon boots"};

        String[] crystal = {"Bow of faerdhinen", "Crystal helm", "Crystal body", "Crystal legs"};
        String[] karils = {"Karil's crossbow", "Bolt rack", "Karil's coif", "Karil's leathertop", "Karil's leatherskirt", "Amulet of the damned"};
        String[] voidRanged = {"Magic shortbow", "Rune arrow", "Void ranger helm", "Void knight top", "Void knight robe", "Void knight gloves"};
        for (String[] set : new String[][]{crystal, karils, voidRanged})
        {
            List<BankItem> bank = bank(join(set, distractors));
            Map<EquipmentSlot, List<EquipmentCandidate>> ranked = FIXTURE.intelligence.rank(bank, AttackStyle.RANGED, false);
            double setTtk = FIXTURE.rangedCalculator.calculate(gear(ranked, set), target, false).getExpectedTtkSeconds();
            double found = FIXTURE.ranged(bank, target, false).getResult().getExpectedTtkSeconds();
            require(found <= setTtk + 1e-9, "ranged search is at least as fast as the complete " + set[2] + " set (" + found + " vs " + setTtk + ")");
        }

        String[] veracs = {"Verac's flail", "Verac's helm", "Verac's brassard", "Verac's plateskirt"};
        String[] dharoks = {"Dharok's greataxe", "Dharok's helm", "Dharok's platebody", "Dharok's platelegs"};
        String[] voidMelee = {"Abyssal whip", "Void melee helm", "Elite void top", "Elite void robe", "Void knight gloves"};
        for (String[] set : new String[][]{veracs, dharoks, voidMelee})
            for (AttackStyle style : new AttackStyle[]{AttackStyle.MELEE_SLASH, AttackStyle.MELEE_CRUSH})
            {
                List<BankItem> bank = bank(join(set, distractors));
                Map<EquipmentSlot, List<EquipmentCandidate>> ranked = FIXTURE.intelligence.rank(bank, style, false);
                if (!MeleeAttackStyleResolver.supports(gear(ranked, set).get(EquipmentSlot.WEAPON), style)) continue;
                double setTtk = FIXTURE.meleeCalculator.calculate(gear(ranked, set), style, target, false).getExpectedTtkSeconds();
                double found = FIXTURE.melee(bank, style, target, false).getResult().getExpectedTtkSeconds();
                require(found <= setTtk + 1e-9, style + " search is at least as fast as the complete " + set[1] + " set (" + found + " vs " + setTtk + ")");
            }

        String[] ahrims = {"Ahrim's staff", "Ahrim's hood", "Ahrim's robetop", "Ahrim's robeskirt", "Amulet of the damned"};
        String[] voidMage = {"Staff of fire", "Void mage helm", "Elite void top", "Elite void robe", "Void knight gloves"};
        for (String[] set : new String[][]{ahrims, voidMage})
        {
            List<BankItem> bank = bank(join(set, distractors));
            Map<EquipmentSlot, List<EquipmentCandidate>> ranked = FIXTURE.intelligence.rank(bank, AttackStyle.MAGIC, false);
            OptimizedMagicLoadout found = FIXTURE.magic(bank, target, false);
            double setTtk = FIXTURE.magicCalculator.calculate(gear(ranked, set), target, found.getSpell(), false).getExpectedTtkSeconds();
            require(found.getResult().getExpectedTtkSeconds() <= setTtk + 1e-9, "magic search is at least as fast as the complete " + set[1] + " set");
        }
    }

    /** Barrows pieces work at 100, 75, 50 and 25 charge; at 0 they are broken and must never be recommended. */
    private static void barrowsChargeStates()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        List<BankItem> broken = bank("Dharok's greataxe 0", "Dharok's helm 0", "Dharok's platebody 0", "Dharok's platelegs 0", "Karil's crossbow 0", "Karil's coif 0",
            "Ahrim's staff 0", "Ahrim's hood 0", "Echo ahrim's staff 0", "Echo ahrim's robetop 0", "Guthan's warspear 0", "Torag's hammers 0", "Verac's flail 0", "Bolt rack", "Abyssal whip", "Magic shortbow", "Rune arrow", "Staff of air");
        for (AttackStyle style : AttackStyle.values())
            for (List<EquipmentCandidate> slot : FIXTURE.intelligence.rank(broken, style, false).values())
                for (EquipmentCandidate c : slot) require(!c.getItem().getName().endsWith(" 0"), "fully degraded " + c.getItem().getName() + " is never a candidate for " + style);

        for (String charge : new String[]{" 100", " 75", " 50", " 25"})
        {
            String[] karils = {"Karil's crossbow" + charge, "Bolt rack", "Karil's coif" + charge, "Karil's leathertop" + charge, "Karil's leatherskirt" + charge, "Amulet of the damned"};
            Map<EquipmentSlot, List<EquipmentCandidate>> ranked = FIXTURE.intelligence.rank(bank(karils), AttackStyle.RANGED, false);
            String[] fresh = {"Karil's crossbow", "Bolt rack", "Karil's coif", "Karil's leathertop", "Karil's leatherskirt", "Amulet of the damned"};
            double freshTtk = FIXTURE.rangedCalculator.calculate(gear(FIXTURE.intelligence.rank(bank(fresh), AttackStyle.RANGED, false), fresh), target, false).getExpectedTtkSeconds();
            double worn = FIXTURE.rangedCalculator.calculate(gear(ranked, karils), target, false).getExpectedTtkSeconds();
            require(Math.abs(worn - freshTtk) < 1e-9, "Karil's set at charge" + charge + " keeps its set effect");
            require(Math.abs(FIXTURE.ranged(bank(karils), target, false).getResult().getExpectedTtkSeconds() - freshTtk) < 1e-9, "ranged search uses the Karil's set at charge" + charge);

            String[] ahrims = {"Ahrim's staff" + charge, "Ahrim's hood" + charge, "Ahrim's robetop" + charge, "Ahrim's robeskirt" + charge, "Amulet of the damned"};
            String[] freshAhrims = {"Ahrim's staff", "Ahrim's hood", "Ahrim's robetop", "Ahrim's robeskirt", "Amulet of the damned"};
            OptimizedMagicLoadout freshMagic = FIXTURE.magic(bank(freshAhrims), target, false);
            OptimizedMagicLoadout wornMagic = FIXTURE.magic(bank(ahrims), target, false);
            require(wornMagic != null && Math.abs(wornMagic.getResult().getExpectedTtkSeconds() - freshMagic.getResult().getExpectedTtkSeconds()) < 1e-9, "magic search uses the Ahrim's set at charge" + charge);

            String[] dharoks = {"Dharok's greataxe" + charge, "Dharok's helm" + charge, "Dharok's platebody" + charge, "Dharok's platelegs" + charge};
            String[] freshDharoks = {"Dharok's greataxe", "Dharok's helm", "Dharok's platebody", "Dharok's platelegs"};
            double freshMelee = FIXTURE.melee(bank(freshDharoks), AttackStyle.MELEE_SLASH, target, false).getResult().getExpectedTtkSeconds();
            require(Math.abs(FIXTURE.melee(bank(dharoks), AttackStyle.MELEE_SLASH, target, false).getResult().getExpectedTtkSeconds() - freshMelee) < 1e-9, "melee search uses the Dharok's set at charge" + charge);
        }
    }

    private static void magicWeaponsAreNotGatedByRank()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        // Fifteen weapons with a higher magic rank that cannot cast anything, and one plain staff that can.
        List<String> names = new ArrayList<>(Arrays.asList("Staff of air"));
        List<WikiEquipment> blunt = new ArrayList<>();
        for (WikiEquipment item : FIXTURE.pack.getEquipment())
        {
            if (!"weapon".equals(item.slot) || FIXTURE.pack.getEquipment(item.id) != item || item.offensive == null || item.offensive.magic < 12) continue;
            String name = SearchFixture.bankName(item);
            boolean casts = PoweredStaff.forWeapon(name) != null || PoweredStaff.requiresContext(name) || name.endsWith(" 0") || "Broken".equals(item.version);
            for (MagicSpell spell : MagicSpell.combat()) casts |= MagicSpellCompatibility.canAutocast(name, spell);
            if (!casts) blunt.add(item);
        }
        for (WikiEquipment item : blunt.subList(0, Math.min(15, blunt.size()))) names.add(SearchFixture.bankName(item));
        require(names.size() > 5, "found weapons with magic attack that cannot autocast (" + (names.size() - 1) + ")");
        OptimizedMagicLoadout best = FIXTURE.magic(bank(names.toArray(new String[0])), target, false);
        require(best != null && wears(best.getItems(), "Staff of air"), "a low-ranked staff is still found when higher-ranked weapons cannot cast");
    }

    private static void suppliesUseFullOwnedStacks()
    {
        MonsterDefinition target = FIXTURE.monster("Abyssal demon");
        List<BankItem> bank = bank("Staff of air", "Magic shortbow", "Rune arrow");
        OptimizedMagicLoadout magic = FIXTURE.magic(bank, target, false);
        List<RecommendedSupply> supplies = SimpleSupplyRecommender.recommend(AttackStyle.MAGIC, magic, null, bank);
        require(!magic.getSpell().runes.isEmpty(), "the chosen spell needs runes");
        for (String rune : magic.getSpell().runes.keySet())
        {
            if (rune.equalsIgnoreCase("Air rune")) continue;   // supplied by the staff
            require(supplies.stream().anyMatch(s -> s.name.equalsIgnoreCase(rune) && s.quantity == 5000), "the full owned stack of " + rune + " is recommended");
        }
        require(supplies.stream().noneMatch(s -> s.name.equalsIgnoreCase("Air rune")), "runes the staff supplies are not recommended");
        OptimizedRangedLoadout ranged = FIXTURE.ranged(bank, target, false);
        List<RecommendedSupply> rangedSupplies = SimpleSupplyRecommender.recommend(AttackStyle.RANGED, null, ranged, bank);
        require(rangedSupplies.stream().anyMatch(s -> s.name.equals("Rune arrow") && s.quantity == 5000), "the full owned stack of the chosen ammunition is recommended");
    }

    private static List<BankItem> bank(String... names)
    {
        List<BankItem> bank = new ArrayList<>();
        int runeId = 900000;
        for (String rune : RUNES) bank.add(new BankItem(runeId++, 5000, rune));
        for (String name : names)
        {
            BankItem item = FIXTURE.bankItem(name);
            bank.add(new BankItem(item.getItemId(), 5000, item.getName()));
        }
        return bank;
    }

    private static String[] join(String[] a, String[] b)
    {
        List<String> all = new ArrayList<>(Arrays.asList(a));
        all.addAll(Arrays.asList(b));
        return all.toArray(new String[0]);
    }

    private static Map<EquipmentSlot, EquipmentCandidate> gear(Map<EquipmentSlot, List<EquipmentCandidate>> ranked, String... names)
    {
        Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
        for (String name : names)
        {
            boolean found = false;
            for (List<EquipmentCandidate> slot : ranked.values())
                for (EquipmentCandidate c : slot)
                    if (c.getItem().getName().equalsIgnoreCase(name)) { gear.put(c.getSlot(), c); found = true; }
            require(found, name + " is a ranked candidate");
        }
        return gear;
    }

    private static boolean wears(Map<EquipmentSlot, EquipmentCandidate> gear, String name)
    {
        for (EquipmentCandidate c : gear.values()) if (c.getItem().getName().equalsIgnoreCase(name)) return true;
        return false;
    }

    private static int passes;

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        passes++;
        if (!label.endsWith("is a ranked candidate") && !label.contains("never a candidate")) System.out.println("PASS " + label);
    }
}
