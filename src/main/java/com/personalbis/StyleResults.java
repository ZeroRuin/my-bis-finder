package com.personalbis;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.runelite.api.Skill;

/**
 * The search result for each attack style, kept while the target, account and owned items
 * are unchanged, so switching the visible style does not repeat an expensive search.
 */
final class StyleResults
{
    private volatile String key;
    private final Map<AttackStyle, Optional<StyleLoadout>> results = new ConcurrentHashMap<>();

    void invalidate()
    {
        key = null;
        results.clear();
    }

    /** The inputs the kept results were calculated for, or null when there are none. */
    String key()
    {
        return key;
    }

    boolean isFor(String inputs)
    {
        return inputs.equals(key);
    }

    /** Drops results calculated for different inputs. Call {@link #accept} once the new ones are complete. */
    void prepareFor(String inputs)
    {
        if (!isFor(inputs)) results.clear();
    }

    void accept(String inputs)
    {
        key = inputs;
    }

    /** The style's result, searched for on first use. Null when the target is immune or nothing usable is owned. */
    @SuppressWarnings("unchecked")
    <T extends StyleLoadout> T get(AttackStyle style, MonsterDefinition monster, Supplier<T> search)
    {
        Optional<StyleLoadout> result = results.get(style);
        if (result == null)
        {
            result = Optional.ofNullable(TargetStyleImmunity.calculate(monster, style, search, () -> null));
            results.put(style, result);
        }
        return (T) result.orElse(null);
    }

    /** The fastest style, or null until every style has a result (a lone result would always "win"). */
    AttackStyle bestIfComplete(MonsterDefinition target)
    {
        Map<AttackStyle, Double> scores = new EnumMap<>(AttackStyle.class);
        for (AttackStyle style : AttackStyle.values())
        {
            Optional<StyleLoadout> result = results.get(style);
            if (result == null) return null;
            CombatOutcome outcome = result.map(StyleLoadout::getResult).orElse(null);
            double ttk = outcome == null ? Double.POSITIVE_INFINITY : outcome.getExpectedTtkSeconds();
            double dps = outcome == null ? 0 : outcome.getDps();
            scores.put(style, FracturedArchiveRules.blade(target) ? dps : Double.isFinite(ttk) ? -ttk : Double.NEGATIVE_INFINITY);
        }
        return BestStyleSelection.best(scores);
    }

    /** Everything a search depends on, as one string: equal strings mean equal results. */
    static String inputs(MonsterDefinition monster, List<BankItem> owned, boolean slayer, String options, AccountSnapshot account)
    {
        StringBuilder k = new StringBuilder(256);
        k.append(monster == null ? -1 : monster.getId())
            .append('|').append(monster == null ? 1 : monster.getSize())
            .append('|').append(monster == null ? "" : monster.getVersion())
            .append('|').append(slayer).append('|').append(options);
        k.append('|').append(MonsterDatabase.combatKey(monster));
        Skill[] combatSkills = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC, Skill.PRAYER, Skill.HITPOINTS, Skill.MINING};
        for (Skill skill : combatSkills)
            k.append('|').append(account.real(skill)).append('/').append(account.boosted(skill));
        k.append('|').append(account.isZealUnlocked()).append(account.fragmentKey());
        k.append('|').append(account.isPietyUnlocked()).append(account.isChivalryUnlocked())
            .append(account.isRigourUnlocked()).append(account.isAuguryUnlocked())
            .append(account.isDeadeyeUnlocked()).append(account.isMysticVigourUnlocked())
            .append(account.isAncientMagicksUnlocked()).append(account.isKingdomDividedUnlocked())
            .append(account.isKandarinHardCompleted());
        // Scan order is stable for a snapshot; quantities are included because rune and
        // ammunition availability can change an otherwise identical loadout.
        for (BankItem item : owned)
            k.append('|').append(item.getItemId()).append(':').append(item.getQuantity());
        return k.toString();
    }
}
