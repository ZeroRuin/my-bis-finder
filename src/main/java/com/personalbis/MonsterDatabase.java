package com.personalbis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPCComposition;
import com.personalbis.wiki.WikiDataPack;
import com.personalbis.wiki.WikiMonster;

/**
 * Builds the monster catalogue from RuneLite's current NPC cache.
 * Must be called on the RuneLite client thread.
 */
@Singleton
public class MonsterDatabase
{
    private final Client client;
    private final WikiDataPack wikiDataPack;
    private List<MonsterDefinition> monsters = Collections.emptyList();

    @Inject
    public MonsterDatabase(Client client, WikiDataPack wikiDataPack)
    {
        this.client = client;
        this.wikiDataPack = wikiDataPack;
    }

    public void rebuild()
    {
        // alpha50.4.50.11.1: the bundled OSRS Wiki DPS calculator monster pack is
        // authoritative. This preserves exact NPC ids/versions/attributes used by
        // mechanics rules instead of reconstructing targets from RuneLite cache stats.
        monsters = Collections.unmodifiableList(catalogue(wikiDataPack.getMonsters()));
    }

    static List<MonsterDefinition> catalogue(List<WikiMonster> source)
    {
        Map<String, MonsterDefinition> unique = new LinkedHashMap<>();
        if (source == null) return Collections.emptyList();

        for (WikiMonster wiki : source)
        {
            if (wiki == null || wiki.name == null || wiki.skills == null || isUnused(wiki)) continue;
            MonsterDefinition candidate = fromWiki(wiki);
            String key = combatKey(candidate);
            MonsterDefinition existing = unique.get(key);
            if (existing == null)
            {
                unique.put(key, candidate);
            }
            else if (!existing.getVersion().equalsIgnoreCase(candidate.getVersion()))
            {
                // These records differ only by model/location/version text. They
                // produce the same combat result, so expose one neutral entry.
                unique.put(key, withoutVersion(existing));
            }
        }

        List<MonsterDefinition> built = new ArrayList<>(unique.values());
        built.sort(Comparator.comparing(MonsterDefinition::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(MonsterDefinition::getVersion, String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(MonsterDefinition::getCombatLevel)
            .thenComparingInt(MonsterDefinition::getId));
        return built;
    }

    private static boolean isUnused(WikiMonster wiki)
    {
        String name = wiki.name == null ? "" : wiki.name.toLowerCase(Locale.ROOT);
        String version = wiki.version == null ? "" : wiki.version.toLowerCase(Locale.ROOT);
        return name.contains("unused") || version.contains("unused");
    }

    static String combatKey(MonsterDefinition m)
    {
        return m.getName().trim().toLowerCase(Locale.ROOT) + '|' + m.getCombatLevel()
            + '|' + m.getDefenceLevel() + '|' + m.getMagicLevel() + '|' + m.getRangedLevel()
            + '|' + m.getHitpoints() + '|' + m.getMagicAttack()
            + '|' + m.getStabDefence() + '|' + m.getSlashDefence() + '|' + m.getCrushDefence()
            + '|' + m.getMagicDefence() + '|' + m.getLightDefence()
            + '|' + m.getStandardDefence() + '|' + m.getHeavyDefence()
            + '|' + m.getSize() + '|' + m.getWeaknessElement() + '|' + m.getWeaknessSeverity()
            + '|' + m.getAttributes().toString().toLowerCase(Locale.ROOT);
    }

    static boolean sameCatalogueEntry(MonsterDefinition left, MonsterDefinition right)
    {
        return left != null && right != null
            && left.getName().equalsIgnoreCase(right.getName())
            && left.getVersion().equalsIgnoreCase(right.getVersion())
            && left.getCombatLevel() == right.getCombatLevel();
    }

    private static MonsterDefinition withoutVersion(MonsterDefinition m)
    {
        return new MonsterDefinition(m.getId(), m.getName(), "", m.getCombatLevel(),
            m.getDefenceLevel(), m.getMagicLevel(), m.getRangedLevel(), m.getHitpoints(),
            m.getStabDefence(), m.getSlashDefence(), m.getCrushDefence(), m.getMagicDefence(),
            m.getLightDefence(), m.getStandardDefence(), m.getHeavyDefence(), m.getAttributes(),
            m.getSize(), m.getWeaknessElement(), m.getWeaknessSeverity(), m.getMagicAttack());
    }

    static String wikiKey(WikiMonster wiki)
    {
        return "wiki|" + wiki.id + "|" + (wiki.version == null ? "" : wiki.version.trim().toLowerCase(Locale.ROOT));
    }

    static MonsterDefinition fromWiki(WikiMonster wiki)
    {
        return new MonsterDefinition(
            wiki.id, wiki.name.trim(), wiki.version, wiki.level, wiki.skills.def,
            wiki.skills.magic, wiki.skills.ranged, wiki.skills.hp,
            wiki.defensive == null ? 0 : wiki.defensive.stab,
            wiki.defensive == null ? 0 : wiki.defensive.slash,
            wiki.defensive == null ? 0 : wiki.defensive.crush,
            wiki.defensive == null ? 0 : wiki.defensive.magic,
            wiki.defensive == null ? 0 : wiki.defensive.light,
            wiki.defensive == null ? 0 : wiki.defensive.standard,
            wiki.defensive == null ? 0 : wiki.defensive.heavy,
            withFlatArmour(wiki.attributes, wiki.defensive == null ? 0 : wiki.defensive.flat_armour),
            wiki.size, wiki.weakness == null ? null : wiki.weakness.element,
            wiki.weakness == null ? 0 : wiki.weakness.severity,
            wiki.offensive == null ? 0 : wiki.offensive.magic);
    }

    private static List<String> withFlatArmour(List<String> attrs, int flatArmour)
    {
        List<String> out = new ArrayList<>();
        if (attrs != null) out.addAll(attrs);
        if (flatArmour != 0) out.add("flat_armour:" + flatArmour);
        return out;
    }

    public List<MonsterDefinition> getAll()
    {
        return monsters;
    }
    public MonsterDefinition canonical(MonsterDefinition selected)
    {
        if (selected == null) return null;
        MonsterDefinition best = selected;
        for (MonsterDefinition m : monsters)
        {
            if (m.getId() == selected.getId()
                && m.getCombatLevel() == selected.getCombatLevel()
                && m.getName().equalsIgnoreCase(selected.getName()))
            {
                // Prefer the richer bundled-Wiki definition over an early/stale
                // RuneLite-cache object with missing defensive stats.
                if (m.getMagicLevel() > best.getMagicLevel()
                    || m.getMagicDefence() > best.getMagicDefence()
                    || m.getWeaknessSeverity() > best.getWeaknessSeverity())
                {
                    best = m;
                }
            }
        }
        return best;
    }


    public List<MonsterDefinition> search(String query, int limit)
    {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int safeLimit = limit <= 0 ? Integer.MAX_VALUE : limit;
        List<MonsterDefinition> out = new ArrayList<>();
        for (MonsterDefinition monster : monsters)
        {
            if (q.isEmpty() || monster.getName().toLowerCase(Locale.ROOT).contains(q))
            {
                out.add(monster);
                if (out.size() >= safeLimit)
                {
                    break;
                }
            }
        }
        return out;
    }


    /**
     * Return monster variants that can plausibly satisfy a Slayer task name.
     * Exact singular/plural matches are listed first, followed by named variants
     * such as Baby/Brutal/King Black dragon.
     */
    public List<MonsterDefinition> findTaskVariants(String task)
    {
        if (task == null || task.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        String normalized = normalizeTaskName(task);
        List<MonsterDefinition> exact = new ArrayList<>();
        List<MonsterDefinition> variants = new ArrayList<>();
        for (MonsterDefinition monster : monsters)
        {
            String name = normalizeTaskName(monster.getName());
            if (name.equals(normalized))
            {
                exact.add(monster);
            }
            else if (name.contains(normalized))
            {
                variants.add(monster);
            }
        }
        exact.addAll(variants);
        return exact;
    }

    public MonsterDefinition findBest(String text)
    {
        if (text == null)
        {
            return null;
        }

        String normalized = normalizeTaskName(text);
        MonsterDefinition contains = null;
        for (MonsterDefinition monster : monsters)
        {
            String name = normalizeTaskName(monster.getName());
            if (name.equals(normalized))
            {
                return monster;
            }
            if (contains == null && (name.contains(normalized) || normalized.contains(name)))
            {
                contains = monster;
            }
        }
        return contains;
    }

    private String normalizeTaskName(String value)
    {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        // Slayer task names are often plural while NPC cache names are singular.
        if (normalized.endsWith("ies") && normalized.length() > 3)
        {
            normalized = normalized.substring(0, normalized.length() - 3) + "y";
        }
        else if (normalized.endsWith("es") && normalized.length() > 3)
        {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        else if (normalized.endsWith("s") && normalized.length() > 2)
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
