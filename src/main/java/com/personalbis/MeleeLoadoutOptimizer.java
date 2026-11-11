package com.personalbis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class MeleeLoadoutOptimizer
{
    private static final int SLOT_CHOICES = 4, BEAM_WIDTH = 32;
    private final MeleeCombatCalculator calculator;

    /** One weapon's search: the slot shortlists and exact results are reused across its loadout families. */
    private static final class SearchContext
    {
        final EnumMap<EquipmentSlot, List<EquipmentCandidate>> choices = new EnumMap<>(EquipmentSlot.class);
        final Map<String, CombatResult> exact = new HashMap<>();
    }

    @Inject
    public MeleeLoadoutOptimizer(MeleeCombatCalculator calculator)
    {
        this.calculator = calculator;
    }

    /** The fastest loadout for one melee style, or null when the player owns no weapon for it. */
    public OptimizedLoadout optimize(Map<EquipmentSlot, List<EquipmentCandidate>> ranked,
        AttackStyle style, MonsterDefinition monster, boolean onSlayerTask)
    {
        if (style != AttackStyle.MELEE_STAB && style != AttackStyle.MELEE_SLASH && style != AttackStyle.MELEE_CRUSH) return null;
        ranked = SlayerTargetRequirements.constrainRanked(monster, ranked);
        EquipmentSlot requiredSlot = SlayerTargetRequirements.requiredEquipmentSlot(monster);
        EquipmentCandidate requiredEquipment = requiredSlot == null ? null
            : SlayerTargetRequirements.firstRequiredEquipment(monster, ranked.get(requiredSlot));
        // Every owned, usable weapon gets a full search: raw rank never decides which weapons are tried.
        List<EquipmentCandidate> weapons = new ArrayList<>();
        for (EquipmentCandidate weapon : LoadoutSearch.allUsable(ranked.get(EquipmentSlot.WEAPON)))
        {
            if (requiredSlot == EquipmentSlot.SHIELD && weapon.isTwoHanded()) continue;
            if (MeleeAttackStyleResolver.supports(weapon, style)) weapons.add(weapon);
        }
        OptimizedLoadout best = searchWeapons(weapons, ranked, style, monster, onSlayerTask, requiredEquipment, null);

        // On an undead Slayer task the Salve amulet's bonus beats the Slayer helm's,
        // and the two don't stack, yet the helm's on-task ranking boost can steer
        // the beam away from the better Salve setup. Being on task can only add
        // options, so also search without the helm or mask and keep the faster
        // answer. Skipped when the head slot is a target requirement (Banshees).
        if (onSlayerTask && monster != null && monster.hasAttribute("undead") && requiredSlot != EquipmentSlot.HEAD)
        {
            List<EquipmentCandidate> allHeads = ranked.getOrDefault(EquipmentSlot.HEAD, Collections.emptyList());
            List<EquipmentCandidate> heads = new ArrayList<>(allHeads);
            heads.removeIf(MeleeLoadoutOptimizer::isTaskHead);
            if (heads.size() < allHeads.size())
            {
                Map<EquipmentSlot, List<EquipmentCandidate>> noTaskHead = new EnumMap<>(ranked);
                noTaskHead.put(EquipmentSlot.HEAD, heads);
                best = searchWeapons(weapons, noTaskHead, style, monster, onSlayerTask, requiredEquipment, best);
            }
        }
        return best;
    }

    private static boolean isTaskHead(EquipmentCandidate c)
    {
        return ItemNames.containsAny(c, "slayer helm", "black mask");
    }

    private OptimizedLoadout searchWeapons(List<EquipmentCandidate> weapons,
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked, AttackStyle style, MonsterDefinition monster,
        boolean onSlayerTask, EquipmentCandidate requiredEquipment, OptimizedLoadout best)
    {
        for (EquipmentCandidate weapon : weapons)
        {
            CalculationCancellation.check();
            OptimizedLoadout weaponBest = optimizeForWeapon(weapon, ranked, style, monster, onSlayerTask, requiredEquipment, new SearchContext());
            if (weaponBest != null && (best == null || FinalLoadoutSelection.faster(weaponBest.getResult(), best.getResult()))) best = weaponBest;
        }
        return best;
    }

    private OptimizedLoadout optimizeForWeapon(EquipmentCandidate weapon,
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask, EquipmentCandidate requiredEquipment, SearchContext context)
    {
        OptimizedLoadout best = optimizeForWeaponFamily(weapon, ranked, style, monster, onSlayerTask, requiredEquipment, context, Collections.emptyMap());
        for (Map<EquipmentSlot, EquipmentCandidate> family : VoidLoadoutFamilies.families(ranked, requiredEquipment, "Void melee helm"))
        {
            CalculationCancellation.check();
            OptimizedLoadout candidate = optimizeForWeaponFamily(weapon, ranked, style, monster, onSlayerTask, requiredEquipment, context, family);
            if (candidate != null && (best == null || FinalLoadoutSelection.preferred(candidate.getResult(),
                LoadoutSearch.prayerBonus(candidate.getItems()), best.getResult(), LoadoutSearch.prayerBonus(best.getItems())))) best = candidate;
        }
        return best;
    }

    private OptimizedLoadout optimizeForWeaponFamily(EquipmentCandidate weapon,
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask, EquipmentCandidate requiredEquipment, SearchContext context,
        Map<EquipmentSlot, EquipmentCandidate> locked)
    {
        Map<EquipmentSlot, EquipmentCandidate> seed = new EnumMap<>(EquipmentSlot.class);
        seed.put(EquipmentSlot.WEAPON, weapon);
        if (requiredEquipment != null) seed.put(requiredEquipment.getSlot(), requiredEquipment);
        seed.putAll(locked);
        List<Map<EquipmentSlot, EquipmentCandidate>> beam = new ArrayList<>();
        beam.add(seed);
        if (locked.isEmpty())
        {
            addSet(beam, veracsSet(ranked, weapon), weapon, requiredEquipment);
            addSet(beam, LoadoutSearch.ownedSet(ranked, ItemNames::is, EquipmentSlot.NECK, "Crystal blessing",
                EquipmentSlot.HEAD, "Crystal helm", EquipmentSlot.BODY, "Crystal body", EquipmentSlot.LEGS, "Crystal legs"), weapon, requiredEquipment);
        }

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (slot == EquipmentSlot.WEAPON || locked.containsKey(slot)) continue;
            if (SlayerTargetRequirements.requiredEquipmentSlot(monster) == slot) continue;
            if (weapon.isTwoHanded() && slot == EquipmentSlot.SHIELD) continue;
            List<EquipmentCandidate> choices = context.choices.computeIfAbsent(slot, s -> LoadoutSearch.dominancePrune(
                LoadoutSearch.shortlist(ranked.get(s), SLOT_CHOICES, MeleeLoadoutEffects::isSynergyItem),
                c -> MeleeLoadoutEffects.isSynergyItem(c) ? null : "", EquipmentCandidate::getAttackBonus, EquipmentCandidate::getStrengthBonus));
            if (choices.isEmpty()) continue;

            List<Map<EquipmentSlot, EquipmentCandidate>> next = new ArrayList<>();
            for (Map<EquipmentSlot, EquipmentCandidate> b : beam)
            {
                // Empty optional slots are legal. This prevents a negative-bonus item
                // from being forced into an otherwise stronger complete loadout.
                next.add(new EnumMap<>(b));
                for (EquipmentCandidate c : choices)
                {
                    Map<EquipmentSlot, EquipmentCandidate> n = new EnumMap<>(b);
                    n.put(slot, c);
                    next.add(n);
                }
            }
            if (VardorvisRules.supported(monster))
            {
                beam = VardorvisShortlist.select(next, monster.getHitpoints(), BEAM_WIDTH / 4,
                    (g, hp) -> calculator.calculateSnapshot(g, style, VardorvisHpSettings.atHp(monster, hp), onSlayerTask).getDps(), LoadoutSearch::prayerBonus);
            }
            else
            {
                // Lines that differ only in items with no effect on damage would crowd better lines out
                // of the beam, so equal lines collapse to the one with the most Prayer. Slots left
                // empty by this are filled again once the winner is known.
                final Map<Map<EquipmentSlot, EquipmentCandidate>, Double> dps = new IdentityHashMap<>();
                Map<String, Map<EquipmentSlot, EquipmentCandidate>> distinct = new LinkedHashMap<>();
                for (Map<EquipmentSlot, EquipmentCandidate> g : next)
                {
                    dps.put(g, calculateCached(g, style, monster, onSlayerTask, context).getDps());
                    Map<EquipmentSlot, EquipmentCandidate> same = distinct.putIfAbsent(damageKey(g, dps.get(g)), g);
                    if (same != null && LoadoutSearch.prayerBonus(g) > LoadoutSearch.prayerBonus(same)) distinct.put(damageKey(g, dps.get(g)), g);
                }
                next = new ArrayList<>(distinct.values());
                next.sort((a, b) -> {
                    int d = Double.compare(dps.get(b), dps.get(a));
                    return d != 0 ? d : Integer.compare(LoadoutSearch.prayerBonus(b), LoadoutSearch.prayerBonus(a));
                });
                beam = new ArrayList<>(next.subList(0, Math.min(BEAM_WIDTH, next.size())));
            }
        }

        if (VardorvisRules.supported(monster))
            beam = VardorvisShortlist.select(beam, monster.getHitpoints(), 2,
                (g, hp) -> calculator.calculateSnapshot(g, style, VardorvisHpSettings.atHp(monster, hp), onSlayerTask).getDps(), LoadoutSearch::prayerBonus);
        Map<EquipmentSlot, EquipmentCandidate> bestGear = null;
        CombatResult bestResult = null;
        for (Map<EquipmentSlot, EquipmentCandidate> g : beam)
        {
            CombatResult r = calculateCached(g, style, monster, onSlayerTask, context);
            if (bestGear == null || FinalLoadoutSelection.preferred(r, LoadoutSearch.prayerBonus(g), bestResult, LoadoutSearch.prayerBonus(bestGear)))
            {
                bestGear = g;
                bestResult = r;
            }
        }
        if (bestGear == null) return null;

        // Complete sets must be evaluated together even when their individual pieces lose beam pruning.
        if (ItemNames.is(weapon, "Dharok's greataxe"))
        {
            Map<EquipmentSlot, EquipmentCandidate> dharok = LoadoutSearch.ownedSet(ranked, ItemNames::is,
                EquipmentSlot.HEAD, "Dharok's helm", EquipmentSlot.BODY, "Dharok's platebody", EquipmentSlot.LEGS, "Dharok's platelegs");
            EquipmentCandidate clash = dharok == null || requiredEquipment == null ? null : dharok.get(requiredEquipment.getSlot());
            if (dharok != null && (clash == null || clash.getItem().getItemId() == requiredEquipment.getItem().getItemId()))
            {
                Map<EquipmentSlot, EquipmentCandidate> trial = new EnumMap<>(bestGear);
                trial.putAll(dharok);
                CombatResult r = calculateCached(trial, style, monster, onSlayerTask, context);
                if (FinalLoadoutSelection.faster(r, bestResult))
                {
                    bestGear = trial;
                    bestResult = r;
                }
            }
        }

        Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(bestGear);
        CombatResult result = LoadoutSearch.fillEmptySlots(gear, bestResult, ranked,
            (slot, c) -> !(weapon.isTwoHanded() && slot == EquipmentSlot.SHIELD),
            trial -> calculateCached(trial, style, monster, onSlayerTask, context));
        return new OptimizedLoadout(style, gear, result);
    }

    /** Lines with the same key deal the same damage now and can gain the same set and effect bonuses later. */
    private static String damageKey(Map<EquipmentSlot, EquipmentCandidate> gear, double dps)
    {
        int attack = 0, strength = 0;
        StringBuilder key = new StringBuilder().append(dps);
        for (EquipmentCandidate c : gear.values())
        {
            attack += c.getAttackBonus();
            strength += c.getStrengthBonus();
            if (c.getSlot() == EquipmentSlot.WEAPON || MeleeLoadoutEffects.isSynergyItem(c)) key.append('|').append(c.getItem().getItemId());
        }
        return key.append('|').append(attack).append('|').append(strength).toString();
    }

    private static void addSet(List<Map<EquipmentSlot, EquipmentCandidate>> beam, Map<EquipmentSlot, EquipmentCandidate> set,
        EquipmentCandidate weapon, EquipmentCandidate requiredEquipment)
    {
        if (set == null) return;
        set.put(EquipmentSlot.WEAPON, weapon);
        if (requiredEquipment != null) set.put(requiredEquipment.getSlot(), requiredEquipment);
        beam.add(set);
    }

    private static Map<EquipmentSlot, EquipmentCandidate> veracsSet(Map<EquipmentSlot, List<EquipmentCandidate>> ranked, EquipmentCandidate weapon)
    {
        if (!ItemNames.is(weapon, "Verac's flail")) return null;
        return LoadoutSearch.ownedSet(ranked, ItemNames::is,
            EquipmentSlot.HEAD, "Verac's helm", EquipmentSlot.BODY, "Verac's brassard", EquipmentSlot.LEGS, "Verac's plateskirt");
    }

    private CombatResult calculateCached(Map<EquipmentSlot, EquipmentCandidate> gear, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask, SearchContext context)
    {
        CalculationCancellation.check();
        StringBuilder key = new StringBuilder();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            EquipmentCandidate c = gear.get(slot);
            key.append(c == null ? 0 : c.getItem().getItemId()).append(',');
        }
        String k = key.toString();
        CombatResult hit = context.exact.get(k);
        if (hit != null) return hit;
        CombatResult calculated = calculator.calculate(gear, style, monster, onSlayerTask);
        if (context.exact.size() >= 4096) context.exact.clear();
        context.exact.put(k, calculated);
        return calculated;
    }
}
