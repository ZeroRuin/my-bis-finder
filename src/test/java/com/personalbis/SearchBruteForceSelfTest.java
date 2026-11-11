package com.personalbis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.runelite.api.Skill;

/**
 * Checks the melee, ranged and magic searches against an exhaustive search. Each case is
 * a small random bank (fixed seed) for which every legal loadout can be calculated; the
 * search must find one that is as fast as the true fastest.
 *
 * <p>Arguments: number of cases per style (default 25), first seed (default 0).
 */
public final class SearchBruteForceSelfTest
{
    private static final double TOLERANCE = 1e-9;
    private static final AttackStyle[] MELEE = {AttackStyle.MELEE_STAB, AttackStyle.MELEE_SLASH, AttackStyle.MELEE_CRUSH};

    private final SearchFixture fixture = new SearchFixture();
    private int checked;
    private final List<String> misses = new ArrayList<>();

    public static void main(String[] args)
    {
        int cases = args.length > 0 ? Integer.parseInt(args[0]) : 25;
        int firstSeed = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        SearchBruteForceSelfTest test = new SearchBruteForceSelfTest();
        for (int seed = firstSeed; seed < firstSeed + cases; seed++) test.runCase(seed);
        for (String miss : test.misses) System.out.println("MISS " + miss);
        if (!test.misses.isEmpty()) throw new AssertionError(test.misses.size() + " of " + test.checked + " searches missed the fastest loadout");
        System.out.println("PASS search matched the exhaustive fastest loadout in " + test.checked + " of " + test.checked + " searches");
    }

    private void runCase(int seed)
    {
        Random random = new Random(seed);
        MonsterDefinition monster;
        do monster = fixture.randomMonster(random);
        while (SlayerTargetRequirements.requiredEquipmentSlot(monster) != null || VardorvisRules.supported(monster));
        boolean task = random.nextBoolean();
        List<BankItem> bank = smallBank(random);
        String label = "seed " + seed + ", " + monster + (task ? ", on task" : "");

        for (AttackStyle style : MELEE)
        {
            OptimizedLoadout found = fixture.melee(bank, style, monster, task);
            Best best = bruteMelee(bank, style, monster, task);
            compare(label + ", " + style, best, found == null ? null : found.getResult().getExpectedTtkSeconds(), found == null ? null : found.getResult().getDps());
        }
        OptimizedRangedLoadout ranged = fixture.ranged(bank, monster, task);
        compare(label + ", Ranged", bruteRanged(bank, monster, task), ranged == null ? null : ranged.getResult().getExpectedTtkSeconds(), ranged == null ? null : ranged.getResult().getDps());
        OptimizedMagicLoadout magic = fixture.magic(bank, monster, task);
        compare(label + ", Magic", bruteMagic(bank, monster, task), magic == null ? null : magic.getResult().getExpectedTtkSeconds(), magic == null ? null : magic.getResult().getDps());
    }

    /** Up to three weapons, and at most two items in four slots, so every loadout can be tried. */
    private List<BankItem> smallBank(Random random)
    {
        List<BankItem> full = fixture.randomBank(random, 2, 3, 0.7);
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked = fixture.intelligence.rank(full, AttackStyle.MELEE_SLASH, false);
        List<EquipmentSlot> slots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) if (slot != EquipmentSlot.WEAPON) slots.add(slot);
        Collections.shuffle(slots, random);
        List<EquipmentSlot> wide = slots.subList(0, 4);
        List<Integer> dropped = new ArrayList<>();
        for (Map.Entry<EquipmentSlot, List<EquipmentCandidate>> entry : ranked.entrySet())
        {
            List<EquipmentCandidate> items = new ArrayList<>(entry.getValue());
            Collections.shuffle(items, random);
            int keep = entry.getKey() == EquipmentSlot.WEAPON ? 3 : entry.getKey() == EquipmentSlot.AMMO ? 2 : wide.contains(entry.getKey()) ? 2 : 1;
            for (int i = keep; i < items.size(); i++) dropped.add(items.get(i).getItem().getItemId());
        }
        List<BankItem> bank = new ArrayList<>();
        for (BankItem item : full)
            if (!dropped.contains(item.getItemId()) && !unmodelled(item.getName().toLowerCase())) bank.add(item);
        return bank;
    }

    /** Blowpipes take their dart from a separate plan that this exhaustive search does not model. */
    private static boolean unmodelled(String name)
    {
        return name.contains("blowpipe");
    }

    private static final class Best
    {
        double ttk = Double.POSITIVE_INFINITY;
        double dps = Double.NEGATIVE_INFINITY;
        String gear = "";
        boolean any;

        void offer(double candidateTtk, double candidateDps, Map<EquipmentSlot, EquipmentCandidate> candidate, String note)
        {
            if (any && !FinalLoadoutSelection.betterTtk(candidateTtk, candidateDps, ttk, dps)) return;
            any = true;
            ttk = candidateTtk;
            dps = candidateDps;
            StringBuilder names = new StringBuilder(note);
            for (EquipmentCandidate c : candidate.values()) names.append(names.length() == 0 ? "" : ", ").append(c.getItem().getName());
            gear = names.toString();
        }
    }

    private void compare(String label, Best best, Double foundTtk, Double foundDps)
    {
        checked++;
        if (!best.any || !Double.isFinite(best.ttk)) return;   // nothing can damage the target
        if (foundTtk == null)
        {
            misses.add(label + ": search found nothing, fastest is " + best.ttk + "s with " + best.gear);
            return;
        }
        if (best.ttk < foundTtk * (1 - TOLERANCE))
            misses.add(String.format("%s: search %.4fs, fastest %.4fs (%.2f%% slower) with %s", label, foundTtk, best.ttk, (foundTtk / best.ttk - 1) * 100, best.gear));
    }

    private interface Visitor
    {
        void visit(Map<EquipmentSlot, EquipmentCandidate> gear);
    }

    /** Calls the visitor for every way of filling (or leaving empty) the given slots. */
    private static void combinations(Map<EquipmentSlot, EquipmentCandidate> gear, List<EquipmentSlot> slots, int index,
        Map<EquipmentSlot, List<EquipmentCandidate>> choices, Visitor visitor)
    {
        if (index == slots.size())
        {
            visitor.visit(gear);
            return;
        }
        EquipmentSlot slot = slots.get(index);
        combinations(gear, slots, index + 1, choices, visitor);
        for (EquipmentCandidate candidate : choices.getOrDefault(slot, Collections.emptyList()))
        {
            gear.put(slot, candidate);
            combinations(gear, slots, index + 1, choices, visitor);
            gear.remove(slot);
        }
    }

    private static Map<EquipmentSlot, List<EquipmentCandidate>> usable(Map<EquipmentSlot, List<EquipmentCandidate>> ranked)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> usable = new EnumMap<>(EquipmentSlot.class);
        for (Map.Entry<EquipmentSlot, List<EquipmentCandidate>> entry : ranked.entrySet())
        {
            List<EquipmentCandidate> items = new ArrayList<>();
            for (EquipmentCandidate c : entry.getValue()) if (!c.getRequirementResult().isBlocked()) items.add(c);
            usable.put(entry.getKey(), items);
        }
        return usable;
    }

    private static List<EquipmentSlot> slotsExcept(EquipmentSlot... excluded)
    {
        List<EquipmentSlot> slots = new ArrayList<>();
        outer:
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            for (EquipmentSlot skip : excluded) if (slot == skip) continue outer;
            slots.add(slot);
        }
        return slots;
    }

    private Best bruteMelee(List<BankItem> bank, AttackStyle style, MonsterDefinition monster, boolean task)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> choices = usable(fixture.intelligence.rank(bank, style, task));
        Best best = new Best();
        for (EquipmentCandidate weapon : choices.get(EquipmentSlot.WEAPON))
        {
            if (!MeleeAttackStyleResolver.supports(weapon, style)) continue;
            Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
            gear.put(EquipmentSlot.WEAPON, weapon);
            List<EquipmentSlot> slots = weapon.isTwoHanded() ? slotsExcept(EquipmentSlot.WEAPON, EquipmentSlot.SHIELD) : slotsExcept(EquipmentSlot.WEAPON);
            combinations(gear, slots, 0, choices, g -> {
                CombatResult r = fixture.meleeCalculator.calculate(g, style, monster, task);
                best.offer(r.getExpectedTtkSeconds(), r.getDps(), g, "");
            });
        }
        return best;
    }

    private Best bruteRanged(List<BankItem> bank, MonsterDefinition monster, boolean task)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> choices = usable(fixture.intelligence.rank(bank, AttackStyle.RANGED, task));
        Best best = new Best();
        for (EquipmentCandidate weapon : choices.get(EquipmentSlot.WEAPON))
        {
            if (CombatStyleLabels.rangedStances(weapon.getWeaponCategory()).isEmpty()) continue;
            List<EquipmentCandidate> ammunition = new ArrayList<>();
            ammunition.add(null);
            for (EquipmentCandidate ammo : choices.get(EquipmentSlot.AMMO))
            {
                if (RangedAmmoRules.requiresEquippedAmmo(weapon) ? RangedAmmoRules.compatible(weapon, ammo)
                    : RangedAmmoRules.ammoSlotFreeForNeutralGear(weapon) && RangedAmmoRules.ammoType(ammo) == RangedAmmoType.NONE) ammunition.add(ammo);
            }
            if (RangedAmmoRules.requiresEquippedAmmo(weapon)) ammunition.remove(0);
            List<EquipmentSlot> slots = RangedAmmoRules.occupiesBothHands(weapon)
                ? slotsExcept(EquipmentSlot.WEAPON, EquipmentSlot.AMMO, EquipmentSlot.SHIELD) : slotsExcept(EquipmentSlot.WEAPON, EquipmentSlot.AMMO);
            for (EquipmentCandidate ammo : ammunition)
            {
                Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
                gear.put(EquipmentSlot.WEAPON, weapon);
                if (ammo != null) gear.put(EquipmentSlot.AMMO, ammo);
                combinations(gear, slots, 0, choices, g -> {
                    RangedCombatResult r = fixture.rangedCalculator.calculate(g, monster, task);
                    if (r != null && Double.isFinite(r.getDps())) best.offer(r.getExpectedTtkSeconds(), r.getDps(), g, "");
                });
            }
        }
        return best;
    }

    private Best bruteMagic(List<BankItem> bank, MonsterDefinition monster, boolean task)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> choices = usable(fixture.intelligence.rank(bank, AttackStyle.MAGIC, task));
        int magicLevel = fixture.account.real(Skill.MAGIC);
        List<MagicSpell> spells = new ArrayList<>();
        for (MagicSpell spell : MagicSpell.combat())
        {
            if (!LeafyTargetRules.magicCanDamage(monster, spell) || spell.level > magicLevel) continue;
            if (spell.isDemonbane() && !monster.hasAttribute("demon")) continue;
            spells.add(spell);
        }
        Best best = new Best();
        for (EquipmentCandidate weapon : choices.get(EquipmentSlot.WEAPON))
        {
            String name = weapon.getItem().getName();
            PoweredStaff powered = weapon.getItem().getItemId() == ItemIds.ZORYAS_TOME_UNCHARGED ? null : PoweredStaff.forWeapon(name);
            List<MagicSpell> castable = new ArrayList<>();
            for (MagicSpell spell : spells) if (MagicSpellCompatibility.canAutocast(name, spell)) castable.add(spell);
            if (powered == null && castable.isEmpty() && !PoweredStaff.requiresContext(name)) continue;
            Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
            gear.put(EquipmentSlot.WEAPON, weapon);
            List<EquipmentSlot> slots = weapon.isTwoHanded() ? slotsExcept(EquipmentSlot.WEAPON, EquipmentSlot.SHIELD) : slotsExcept(EquipmentSlot.WEAPON);
            combinations(gear, slots, 0, choices, g -> {
                if (powered != null && magicLevel >= powered.minLevel)
                {
                    MagicCombatResult r = fixture.magicCalculator.calculatePowered(g, monster, powered, task);
                    best.offer(r.getExpectedTtkSeconds(), r.getDps(), g, powered.label);
                    return;
                }
                for (MagicSpell spell : castable)
                {
                    if (!MagicLoadoutOptimizer.runesAvailable(spell, bank, weapon, g.values())) continue;
                    MagicCombatResult r = fixture.magicCalculator.calculate(g, monster, spell, task);
                    best.offer(r.getExpectedTtkSeconds(), r.getDps(), g, spell.name);
                }
            });
        }
        return best;
    }
}
