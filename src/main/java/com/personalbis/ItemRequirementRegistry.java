package com.personalbis;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

/**
 * Eligibility checks used by the equipment intelligence layer.
 *
 * r0.3.0 includes a working requirement framework plus common high-value gear
 * rules. Unknown item-specific requirements are marked UNVERIFIED instead of
 * being silently treated as known-good. The complete generated OSRS requirement
 * table is the next data-layer milestone.
 */
public class ItemRequirementRegistry
{
    private final Client client;

    @Inject
    public ItemRequirementRegistry(Client client)
    {
        this.client = client;
    }

    public RequirementResult check(BankItem item, EquipmentSlot slot)
    {
        String name = item.getName().toLowerCase();
        Map<Skill, Integer> required = inferCommonLevelRequirements(name, slot);

        for (Map.Entry<Skill, Integer> requirement : required.entrySet())
        {
            int current = client.getRealSkillLevel(requirement.getKey());
            if (current < requirement.getValue())
            {
                return RequirementResult.blocked(
                    "Requires " + requirement.getValue() + " " + pretty(requirement.getKey()) +
                    " (you have " + current + ")");
            }
        }

        QuestCheck questCheck = inferQuestRequirement(name);
        if (questCheck != null)
        {
            try
            {
                Quest quest = Quest.valueOf(questCheck.questEnumName);
                if (quest.getState(client) != QuestState.FINISHED)
                {
                    return RequirementResult.blocked("Requires quest: " + quest.getName());
                }
            }
            catch (IllegalArgumentException ignored)
            {
                return RequirementResult.unverified("Quest requirement could not be verified");
            }
        }

        if (required.isEmpty() && questCheck == null)
        {
            return RequirementResult.unverified("Equipable; specific skill/quest requirements not yet verified");
        }

        return RequirementResult.usable("Meets known level/quest requirements");
    }

    private Map<Skill, Integer> inferCommonLevelRequirements(String name, EquipmentSlot slot)
    {
        Map<Skill, Integer> req = new HashMap<>();

        if (name.contains("rune "))
        {
            req.put(isArmourSlot(slot) ? Skill.DEFENCE : Skill.ATTACK, 40);
        }
        if (name.contains("dragon "))
        {
            req.put(isArmourSlot(slot) ? Skill.DEFENCE : Skill.ATTACK, 60);
        }

        if (containsAny(name, "dharok", "guthan", "karil", "ahrim", "torag", "verac")) req.put(Skill.DEFENCE, 70);
        if (name.contains("bandos chestplate") || name.contains("bandos tassets")) req.put(Skill.DEFENCE, 65);
        // Dragonhide / clue ranged armour. These checks are important because
        // offensive stats alone can otherwise make unavailable gear look best.
        if (containsAny(name, "black d'hide", "black dragonhide"))
        {
            req.put(Skill.RANGED, 70);
            if (slot == EquipmentSlot.BODY || slot == EquipmentSlot.SHIELD)
            {
                req.put(Skill.DEFENCE, 40);
            }
        }
        if (containsAny(name,
            "ancient d'hide", "armadyl d'hide", "bandos d'hide",
            "guthix d'hide", "saradomin d'hide", "zamorak d'hide",
            "ancient coif", "armadyl coif", "bandos coif",
            "guthix coif", "saradomin coif", "zamorak coif",
            "ancient bracers", "armadyl bracers", "bandos bracers",
            "guthix bracers", "saradomin bracers", "zamorak bracers",
            "ancient chaps", "armadyl chaps", "bandos chaps",
            "guthix chaps", "saradomin chaps", "zamorak chaps",
            "ancient boots", "armadyl boots", "bandos boots",
            "guthix boots", "saradomin boots", "zamorak boots"))
        {
            req.put(Skill.RANGED, 70);
            if (slot != EquipmentSlot.LEGS && slot != EquipmentSlot.HANDS)
            {
                req.put(Skill.DEFENCE, 40);
            }
        }
        if (name.contains("blue d'hide")) req.put(Skill.RANGED, 50);
        if (name.contains("red d'hide")) req.put(Skill.RANGED, 60);
        if (name.contains("green d'hide")) req.put(Skill.RANGED, 40);

        if (containsAny(name, "karil's", "karils")) { req.put(Skill.RANGED, 70); req.put(Skill.DEFENCE, 70); }
        if (containsAny(name, "armadyl helmet", "armadyl chestplate", "armadyl chainskirt")) { req.put(Skill.RANGED, 70); req.put(Skill.DEFENCE, 70); }
        if (name.contains("crystal helm") || name.contains("crystal body") || name.contains("crystal legs"))
        {
            req.put(Skill.RANGED, 70);
            req.put(Skill.DEFENCE, 70);
        }
        if (name.contains("slayer helmet") || name.contains("slayer helm") || name.contains("black mask"))
        {
            req.put(Skill.DEFENCE, 10);
        }
        if (name.contains("masori")) req.put(Skill.RANGED, 80);
        if (name.contains("ancestral")) { req.put(Skill.MAGIC, 75); req.put(Skill.DEFENCE, 65); }
        if (name.contains("torva")) { req.put(Skill.DEFENCE, 80); req.put(Skill.STRENGTH, 80); }
        if (name.contains("abyssal whip")) req.put(Skill.ATTACK, 70);
        if (name.contains("abyssal tentacle")) req.put(Skill.ATTACK, 75);
        if (name.contains("godsword")) req.put(Skill.ATTACK, 75);
        if (name.contains("scythe of vitur")) { req.put(Skill.ATTACK, 80); req.put(Skill.STRENGTH, 90); }
        if (name.contains("bow of faerdhinen")) req.put(Skill.RANGED, 80);
        if (name.contains("twisted bow")) req.put(Skill.RANGED, 85);
        if (name.contains("toxic blowpipe")) req.put(Skill.RANGED, 75);
        if (name.contains("dragon hunter crossbow")) req.put(Skill.RANGED, 70);
        if (name.contains("trident of the seas")) req.put(Skill.MAGIC, 75);
        if (name.contains("trident of the swamp")) req.put(Skill.MAGIC, 78);
        if (name.contains("sanguinesti staff")) req.put(Skill.MAGIC, 82);
        if (name.contains("tumeken's shadow")) req.put(Skill.MAGIC, 85);

        return req;
    }

    private QuestCheck inferQuestRequirement(String name)
    {
        if (name.contains("dragon scimitar")) return new QuestCheck("MONKEY_MADNESS_I");
        if (name.contains("barrows gloves")) return new QuestCheck("RECIPE_FOR_DISASTER");
        if (name.contains("ava's assembler")) return new QuestCheck("DRAGON_SLAYER_II");
        if (name.contains("mythical cape")) return new QuestCheck("DRAGON_SLAYER_II");
        if (name.contains("crystal helm") || name.contains("crystal body") || name.contains("crystal legs") || name.contains("bow of faerdhinen")) return new QuestCheck("SONG_OF_THE_ELVES");
        return null;
    }

    private boolean isArmourSlot(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.BODY || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.SHIELD || slot == EquipmentSlot.HANDS || slot == EquipmentSlot.FEET;
    }

    private boolean containsAny(String text, String... terms)
    {
        for (String term : terms)
        {
            if (text.contains(term)) return true;
        }
        return false;
    }

    private String pretty(Skill skill)
    {
        String lower = skill.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static final class QuestCheck
    {
        private final String questEnumName;
        private QuestCheck(String questEnumName) { this.questEnumName = questEnumName; }
    }
}
