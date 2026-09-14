package com.personalbis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import com.personalbis.wiki.WikiDataPack;
import com.personalbis.wiki.WikiEquipment;

public class EquipmentIntelligence
{
    private final ItemManager itemManager;
    private final ItemRequirementRegistry requirementRegistry;
    private final WikiDataPack wikiDataPack;

    @Inject
    public EquipmentIntelligence(ItemManager itemManager, ItemRequirementRegistry requirementRegistry, WikiDataPack wikiDataPack)
    {
        this.itemManager = itemManager;
        this.requirementRegistry = requirementRegistry;
        this.wikiDataPack = wikiDataPack;
    }

    public Map<EquipmentSlot, List<EquipmentCandidate>> rank(List<BankItem> bank, AttackStyle style)
    {
        return rank(bank, style, false);
    }

    public Map<EquipmentSlot, List<EquipmentCandidate>> rank(List<BankItem> bank, AttackStyle style, boolean onSlayerTask)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ranked.put(slot, new ArrayList<EquipmentCandidate>());
        }

        for (BankItem bankItem : bank)
        {
            int canonicalId = itemManager.canonicalize(bankItem.getItemId());
            ItemStats itemStats = itemManager.getItemStats(canonicalId);
            if (itemStats == null || !itemStats.isEquipable() || itemStats.getEquipment() == null)
            {
                continue;
            }

            ItemEquipmentStats equipment = itemStats.getEquipment();
            EquipmentSlot slot = EquipmentSlot.fromRuneLiteSlot(equipment.getSlot());
            if (slot == null)
            {
                continue;
            }

            RequirementResult requirements = requirementRegistry.check(bankItem, slot);
            WikiEquipment wiki = wikiDataPack.getEquipment(WikiEquipmentEngine.canonicalId(canonicalId));
            int attackBonus = wiki != null ? attackForStyle(wiki, style) : attackForStyle(equipment, style);
            int score = (wiki != null ? score(wiki, style, requirements) : score(equipment, style, requirements))
                + slayerTaskBonus(bankItem.getName(), style, onSlayerTask);

            ranked.get(slot).add(new EquipmentCandidate(
                bankItem,
                slot,
                score,
                attackBonus,
                wiki != null && wiki.bonuses != null ? wiki.bonuses.str : equipment.getStr(),
                wiki != null && wiki.offensive != null ? wiki.offensive.ranged : equipment.getArange(),
                wiki != null && wiki.bonuses != null ? wiki.bonuses.ranged_str : equipment.getRstr(),
                wiki != null && wiki.offensive != null ? wiki.offensive.magic : equipment.getAmagic(),
                wiki != null && wiki.bonuses != null ? (float)(wiki.bonuses.magic_str / 1000.0) : equipment.getMdmg(),
                wiki != null && wiki.bonuses != null ? wiki.bonuses.prayer : equipment.getPrayer(),
                wiki != null ? wiki.speed : equipment.getAspeed(),
                wiki != null ? wiki.isTwoHanded : equipment.isTwoHanded(),
                wiki != null ? wiki.category : "",
                requirements));
        }

        Comparator<EquipmentCandidate> comparator = new Comparator<EquipmentCandidate>()
        {
            @Override
            public int compare(EquipmentCandidate a, EquipmentCandidate b)
            {
                if (a.getRequirementResult().isBlocked() != b.getRequirementResult().isBlocked())
                {
                    return a.getRequirementResult().isBlocked() ? 1 : -1;
                }
                int scoreCompare = Integer.compare(b.getScore(), a.getScore());
                if (scoreCompare != 0)
                {
                    return scoreCompare;
                }
                return a.getItem().getName().compareToIgnoreCase(b.getItem().getName());
            }
        };

        for (List<EquipmentCandidate> candidates : ranked.values())
        {
            Collections.sort(candidates, comparator);
        }

        return ranked;
    }

    public List<EquipmentCandidate> allForSlot(List<BankItem> bank, AttackStyle style, EquipmentSlot slot)
    {
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked = rank(bank, style);
        List<EquipmentCandidate> candidates = ranked.get(slot);
        return candidates == null ? Collections.<EquipmentCandidate>emptyList() : candidates;
    }

    private int attackForStyle(WikiEquipment stats, AttackStyle style)
    {
        if (stats.offensive == null) return 0;
        switch (style)
        {
            case MELEE_STAB: return stats.offensive.stab;
            case MELEE_SLASH: return stats.offensive.slash;
            case MELEE_CRUSH: return stats.offensive.crush;
            case RANGED: return stats.offensive.ranged;
            case MAGIC: return stats.offensive.magic;
            default: return 0;
        }
    }

    private int score(WikiEquipment stats, AttackStyle style, RequirementResult requirements)
    {
        int attack = attackForStyle(stats, style);
        int prayer = stats.bonuses == null ? 0 : stats.bonuses.prayer;
        int base;
        switch (style)
        {
            case MELEE_STAB:
            case MELEE_SLASH:
            case MELEE_CRUSH:
                base = attack * 2 + (stats.bonuses == null ? 0 : stats.bonuses.str) * 5 + prayer;
                break;
            case RANGED:
                base = attack * 2 + (stats.bonuses == null ? 0 : stats.bonuses.ranged_str) * 5 + prayer;
                break;
            case MAGIC:
                base = attack * 2 + (stats.bonuses == null ? 0 : (int)Math.round(stats.bonuses.magic_str * 10.0)) + prayer;
                break;
            default:
                base = 0;
        }
        return requirements.isBlocked() ? base - 1000000 : base;
    }

    private int attackForStyle(ItemEquipmentStats stats, AttackStyle style)
    {
        switch (style)
        {
            case MELEE_STAB: return stats.getAstab();
            case MELEE_SLASH: return stats.getAslash();
            case MELEE_CRUSH: return stats.getAcrush();
            case RANGED: return stats.getArange();
            case MAGIC: return stats.getAmagic();
            default: return 0;
        }
    }

    private int slayerTaskBonus(String itemName, AttackStyle style, boolean onSlayerTask)
    {
        if (!onSlayerTask || itemName == null)
        {
            return 0;
        }

        String name = itemName.toLowerCase();

        // Slayer helmets and black masks are deliberately given a large selection
        // bonus while on-task so the owned-gear planner accounts for their task
        // damage/accuracy effect. Imbued variants also apply to ranged and magic.
        boolean slayerHelm = name.contains("slayer helmet") || name.contains("slayer helm");
        boolean blackMask = name.contains("black mask");
        if (!slayerHelm && !blackMask)
        {
            return 0;
        }

        if (style == AttackStyle.RANGED || style == AttackStyle.MAGIC)
        {
            return name.contains("(i)") || name.contains("imbued") ? 100000 : 0;
        }

        return 100000;
    }

    private int score(ItemEquipmentStats stats, AttackStyle style, RequirementResult requirements)
    {
        int base;
        switch (style)
        {
            case MELEE_STAB:
                base = stats.getAstab() * 2 + stats.getStr() * 5 + stats.getPrayer();
                break;
            case MELEE_SLASH:
                base = stats.getAslash() * 2 + stats.getStr() * 5 + stats.getPrayer();
                break;
            case MELEE_CRUSH:
                base = stats.getAcrush() * 2 + stats.getStr() * 5 + stats.getPrayer();
                break;
            case RANGED:
                base = stats.getArange() * 2 + stats.getRstr() * 5 + stats.getPrayer();
                break;
            case MAGIC:
                base = stats.getAmagic() * 2 + Math.round(stats.getMdmg() * 1000f) + stats.getPrayer();
                break;
            default:
                base = 0;
        }

        return requirements.isBlocked() ? base - 1000000 : base;
    }
}
