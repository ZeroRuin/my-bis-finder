package com.personalbis;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/** Search steps shared by the melee, ranged and magic loadout optimizers. */
final class LoadoutSearch
{
    private LoadoutSearch() {}

    /** Every candidate the player can equip. */
    static List<EquipmentCandidate> allUsable(List<EquipmentCandidate> all)
    {
        List<EquipmentCandidate> usable = new ArrayList<>();
        if (all != null) for (EquipmentCandidate c : all) if (c != null && !c.getRequirementResult().isBlocked()) usable.add(c);
        return usable;
    }

    /**
     * The candidates worth searching in one slot: the best {@code limit} by rank, the best
     * Prayer item, and every effect or set piece (these can look weak on their own).
     */
    static List<EquipmentCandidate> shortlist(List<EquipmentCandidate> all, int limit, Predicate<EquipmentCandidate> effectPiece)
    {
        List<EquipmentCandidate> usable = allUsable(all);
        List<EquipmentCandidate> chosen = new ArrayList<>(usable.subList(0, Math.min(limit, usable.size())));
        EquipmentCandidate prayerBest = null;
        for (EquipmentCandidate c : usable) if (prayerBest == null || c.getPrayer() > prayerBest.getPrayer()) prayerBest = c;
        if (prayerBest != null && prayerBest.getPrayer() > 0 && !chosen.contains(prayerBest)) chosen.add(prayerBest);
        for (EquipmentCandidate c : usable) if (effectPiece.test(c) && !chosen.contains(c)) chosen.add(c);
        return chosen;
    }

    /**
     * Removes candidates that another candidate in the same group beats or equals on attack,
     * strength and Prayer. A null group means the item is never removed and never removes others.
     */
    static List<EquipmentCandidate> dominancePrune(List<EquipmentCandidate> raw, Function<EquipmentCandidate, String> group,
        ToIntFunction<EquipmentCandidate> attack, ToIntFunction<EquipmentCandidate> strength)
    {
        if (raw.size() < 2) return raw;
        List<EquipmentCandidate> kept = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++)
        {
            EquipmentCandidate c = raw.get(i);
            String cGroup = group.apply(c);
            boolean dominated = false;
            for (int j = 0; j < raw.size() && !dominated && cGroup != null; j++)
            {
                EquipmentCandidate d = raw.get(j);
                if (i == j || !cGroup.equals(group.apply(d))) continue;
                boolean noWorse = attack.applyAsInt(d) >= attack.applyAsInt(c) && strength.applyAsInt(d) >= strength.applyAsInt(c)
                    && d.getPrayer() >= c.getPrayer();
                boolean strict = attack.applyAsInt(d) > attack.applyAsInt(c) || strength.applyAsInt(d) > strength.applyAsInt(c)
                    || d.getPrayer() > c.getPrayer();
                // Exact ties keep the earlier (higher ranked) candidate.
                dominated = noWorse && (strict || j < i);
            }
            if (!dominated) kept.add(c);
        }
        return kept;
    }

    static int prayerBonus(Map<EquipmentSlot, EquipmentCandidate> gear)
    {
        int prayer = 0;
        for (EquipmentCandidate c : gear.values()) if (c != null) prayer += c.getPrayer();
        return prayer;
    }

    static EquipmentCandidate findNamed(List<EquipmentCandidate> items, String name, BiPredicate<EquipmentCandidate, String> matches)
    {
        for (EquipmentCandidate c : allUsable(items)) if (c.getItem() != null && matches.test(c, name)) return c;
        return null;
    }

    /**
     * A complete owned set, given as slot, name, slot, name... Returns null unless every piece
     * is owned and usable. Sets are seeded into the search whole, because their pieces can
     * lose to ordinary gear one slot at a time.
     */
    static Map<EquipmentSlot, EquipmentCandidate> ownedSet(Map<EquipmentSlot, List<EquipmentCandidate>> ranked,
        BiPredicate<EquipmentCandidate, String> matches, Object... slotsAndNames)
    {
        Map<EquipmentSlot, EquipmentCandidate> set = new EnumMap<>(EquipmentSlot.class);
        for (int i = 0; i < slotsAndNames.length; i += 2)
        {
            EquipmentSlot slot = (EquipmentSlot) slotsAndNames[i];
            EquipmentCandidate piece = findNamed(ranked.get(slot), (String) slotsAndNames[i + 1], matches);
            if (piece == null) return null;
            set.put(slot, piece);
        }
        return set;
    }

    /**
     * The search may leave a slot empty while comparing loadouts. Once the winner is known,
     * fills each empty slot with the owned item that helps most (ties go to Prayer), and
     * never with one that makes the kill slower. Changes {@code gear} and returns its result.
     */
    static <R extends CombatOutcome> R fillEmptySlots(Map<EquipmentSlot, EquipmentCandidate> gear, R current,
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked,
        BiPredicate<EquipmentSlot, EquipmentCandidate> allowed, Function<Map<EquipmentSlot, EquipmentCandidate>, R> calculate)
    {
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            List<EquipmentCandidate> all = ranked.get(slot);
            if (slot == EquipmentSlot.WEAPON || gear.containsKey(slot) || all == null) continue;
            EquipmentCandidate chosen = null;
            R chosenResult = current;
            for (EquipmentCandidate c : all)
            {
                if (c.getRequirementResult().isBlocked() || !allowed.test(slot, c)) continue;
                Map<EquipmentSlot, EquipmentCandidate> trial = new EnumMap<>(gear);
                trial.put(slot, c);
                R result = calculate.apply(trial);
                if (result == null || FinalLoadoutSelection.faster(current, result)) continue;
                if (chosen == null || FinalLoadoutSelection.preferred(result, c.getPrayer(), chosenResult, chosen.getPrayer()))
                {
                    chosen = c;
                    chosenResult = result;
                }
            }
            if (chosen != null)
            {
                gear.put(slot, chosen);
                current = chosenResult;
            }
        }
        return current;
    }
}
