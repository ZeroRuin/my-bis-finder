package com.personalbis;

import java.util.Locale;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Small authoritative guard for Slayer-only targets and their mandatory tools. */
public final class SlayerTargetRequirements {
    private SlayerTargetRequirements() {}

    public static int requiredLevel(MonsterDefinition monster) {
        if (monster == null) return 0;
        String n = monster.getName().toLowerCase(Locale.ROOT);
        if (n.contains("smoke devil")) return 93;
        if (n.contains("basilisk knight")) return 60;
        if (n.contains("basilisk")) return 40;
        if (n.contains("rockslug")) return 20;
        if (n.contains("zygomite")) return 57;
        if (n.contains("dust devil")) return 65;
        if (n.contains("nechryael")) return 80;
        if (n.contains("kraken")) return 87;
        if (n.contains("abyssal demon")) return 85;
        return 0;
    }

    public static String requiredItem(MonsterDefinition monster) {
        if (monster == null) return "";
        String n = monster.getName().toLowerCase(Locale.ROOT);
        if (n.contains("gargoyle")) return "Rock hammer or equivalent finishing hammer";
        if (isDesertLizard(n)) return "Ice cooler";
        if (n.contains("zygomite")) return "Fungicide spray";
        if (n.contains("rockslug")) return "Bag of salt or Brine sabre";
        if (n.contains("mogre")) return "Fishing explosive (to lure)";
        if (n.contains("molanisk")) return "Slayer bell (to lure)";
        if (n.contains("basilisk knight")) return "Mirror shield or V's shield";
        if (n.contains("basilisk")) return "Mirror shield or V's shield";
        if (n.contains("cockatrice")) return "Mirror shield or V's shield";
        if (n.contains("banshee")) return "Earmuffs, masked earmuffs or Slayer helmet";
        if (n.contains("dust devil") || n.contains("smoke devil")) return "Face mask or Slayer helmet";
        if (n.contains("aberrant spectre")) return "Nose peg or Slayer helmet";
        if (n.contains("wall beast")) return "Spiny helmet or Slayer helmet";
        if (n.contains("cave horror")) return "Witchwood icon";
        if (n.contains("killerwatt")) return "Insulated boots";
        if (n.contains("fever spider")) return "Slayer gloves";
        if (n.contains("harpie bug swarm")) return "Lit bug lantern";
        if (n.contains("turoth") || n.contains("kurask"))
            return "Leaf-bladed weapon, broad ammunition or Magic Dart";
        return "";
    }

    /** Equipment that must remain equipped for the target's attacks to be viable. */
    public static EquipmentSlot requiredEquipmentSlot(MonsterDefinition monster) {
        String n = monster == null ? "" : monster.getName().toLowerCase(Locale.ROOT);
        if (n.contains("basilisk") || n.contains("cockatrice") || n.contains("harpie bug swarm")) return EquipmentSlot.SHIELD;
        if (n.contains("banshee") || n.contains("dust devil") || n.contains("smoke devil") || n.contains("aberrant spectre") || n.contains("wall beast")) return EquipmentSlot.HEAD;
        if (n.contains("cave horror")) return EquipmentSlot.NECK;
        if (n.contains("killerwatt")) return EquipmentSlot.FEET;
        if (n.contains("fever spider")) return EquipmentSlot.HANDS;
        return null;
    }

    public static boolean validRequiredEquipment(MonsterDefinition monster, EquipmentCandidate candidate) {
        EquipmentSlot slot = requiredEquipmentSlot(monster);
        if (slot == null) return true;
        if (candidate == null || candidate.getSlot() != slot || candidate.getItem() == null) return false;
        String n = candidate.getItem().getName().toLowerCase(Locale.ROOT);
        String target = monster.getName().toLowerCase(Locale.ROOT);
        if (target.contains("basilisk") || target.contains("cockatrice"))
            return n.contains("mirror shield") || n.equals("v's shield");
        if (target.contains("harpie bug swarm")) return n.contains("lit bug lantern");
        if (target.contains("banshee")) return n.contains("earmuffs") || n.contains("slayer helmet");
        if (target.contains("dust devil") || target.contains("smoke devil")) return n.contains("face mask") || n.contains("facemask") || n.contains("slayer helmet");
        if (target.contains("aberrant spectre")) return n.contains("nose peg") || n.contains("slayer helmet");
        if (target.contains("wall beast")) return n.contains("spiny helmet") || n.contains("slayer helmet");
        if (target.contains("cave horror")) return n.contains("witchwood icon");
        if (target.contains("killerwatt")) return n.contains("insulated boots");
        if (target.contains("fever spider")) return n.contains("slayer gloves");
        return false;
    }

    /** Inventory-use finishing/luring tools, in preferred fallback order. */
    public static String[] finishingItems(MonsterDefinition monster) {
        String n = monster == null ? "" : monster.getName().toLowerCase(Locale.ROOT);
        if (n.contains("gargoyle")) return new String[]{"Rock hammer", "Rock thrownhammer", "Granite hammer"};
        if (isDesertLizard(n)) return new String[]{"Ice cooler"};
        if (n.contains("zygomite")) return new String[]{"Fungicide spray"};
        if (n.contains("rockslug")) return new String[]{"Bag of salt", "Brine sabre"};
        if (n.contains("mogre")) return new String[]{"Fishing explosive"};
        if (n.contains("molanisk")) return new String[]{"Slayer bell"};
        return new String[0];
    }

    private static boolean isDesertLizard(String name) {
        return name.equals("lizard") || name.contains("small lizard") || name.contains("desert lizard");
    }

    public static EquipmentCandidate firstRequiredEquipment(MonsterDefinition monster,
        java.util.List<EquipmentCandidate> candidates) {
        if (requiredEquipmentSlot(monster) == null) return null;
        String target=monster.getName().toLowerCase(Locale.ROOT);
        if ((target.contains("basilisk") || target.contains("cockatrice")) && candidates != null)
            for (EquipmentCandidate candidate : candidates)
                if (candidate != null && !candidate.getRequirementResult().isBlocked()
                    && candidate.getItem()!=null
                    && candidate.getItem().getName().equalsIgnoreCase("V's shield")) return candidate;
        if (candidates != null) for (EquipmentCandidate candidate : candidates)
            if (candidate != null && !candidate.getRequirementResult().isBlocked()
                && validRequiredEquipment(monster, candidate)) return candidate;
        return null;
    }

    /**
     * Reserve a mandatory slot. When the player owns a valid item it is the only
     * candidate for that slot; otherwise the empty list deliberately leaves the
     * slot blank while allowing the rest of the loadout to be calculated.
     */
    public static Map<EquipmentSlot,List<EquipmentCandidate>> constrainRanked(
        MonsterDefinition monster, Map<EquipmentSlot,List<EquipmentCandidate>> ranked) {
        Map<EquipmentSlot,List<EquipmentCandidate>> constrained=new EnumMap<>(EquipmentSlot.class);
        if(ranked!=null)for(Map.Entry<EquipmentSlot,List<EquipmentCandidate>> entry:ranked.entrySet())
            constrained.put(entry.getKey(),entry.getValue()==null?new ArrayList<>():new ArrayList<>(entry.getValue()));
        EquipmentSlot slot=requiredEquipmentSlot(monster);
        if(slot!=null){
            EquipmentCandidate required=firstRequiredEquipment(monster,constrained.get(slot));
            constrained.put(slot,required==null?new ArrayList<>():new ArrayList<>(java.util.Collections.singletonList(required)));
        }
        return constrained;
    }

    public static boolean allowed(MonsterDefinition monster, int slayerLevel) {
        return requiredLevel(monster) <= 0 || slayerLevel >= requiredLevel(monster);
    }

    public static String bankHeader(MonsterDefinition monster) {
        if (monster == null) return "My BiS Finder";
        String header = monster.getName() + (monster.getVersion().isEmpty() ? "" : " [" + monster.getVersion() + "]");
        String requirement = requiredItem(monster);
        return requirement.isEmpty() ? header : header + " — Requires " + requirement;
    }

    /** Target families found at one or more Slayer-ring teleport destinations. */
    public static boolean slayerRingTeleportUseful(MonsterDefinition monster) {
        if (monster == null) return false;
        String n = monster.getName().toLowerCase(Locale.ROOT);
        String[] families = {
            // Slayer Tower
            "crawling hand", "banshee", "infernal mage", "bloodveld", "aberrant spectre",
            "gargoyle", "nechryael", "abyssal demon",
            // Fremennik Slayer Dungeon
            "cave crawler", "rockslug", "cockatrice", "pyrefiend", "basilisk", "jelly",
            "turoth", "kurask",
            // Stronghold Slayer Cave
            "ankou", "blue dragon", "brine rat", "fire giant", "greater demon", "hellhound",
            // Tarn's Lair and Mourner tunnels
            "terror dog", "dark beast"
        };
        for (String family : families) if (n.contains(family)) return true;
        return false;
    }
}
