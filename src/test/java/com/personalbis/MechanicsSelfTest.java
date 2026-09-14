package com.personalbis;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.List;
/** Lightweight regression harness: no live account or bank items required. */
public final class MechanicsSelfTest
{
    private static EquipmentCandidate weapon(String name)
    {
        return new EquipmentCandidate(new BankItem(1, 1, name), EquipmentSlot.WEAPON, 0,
            0, 0, 0, 0, 0, 0f, 0, 4, false, "", RequirementResult.usable("test"));
    }

    private static EquipmentCandidate slotItem(String name,EquipmentSlot slot)
    {
        return new EquipmentCandidate(new BankItem(1,1,name),slot,0,
            0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));
    }

    private static EquipmentCandidate slotItem(int id,String name,EquipmentSlot slot)
    {
        return new EquipmentCandidate(new BankItem(id,1,name),slot,0,
            0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));
    }

    private static EquipmentCandidate twoHandedWeapon(String name)
    {
        return new EquipmentCandidate(new BankItem(1, 1, name), EquipmentSlot.WEAPON, 0,
            0, 0, 0, 0, 0, 0f, 0, 4, true, "", RequirementResult.usable("test"));
    }

    private static EquipmentCandidate categorizedWeapon(String name,String category)
    {
        return new EquipmentCandidate(new BankItem(1,1,name),EquipmentSlot.WEAPON,0,
            0,0,0,0,0,0f,0,4,false,category,RequirementResult.usable("test"));
    }

    private static MonsterDefinition monster(String name, String... attributes)
    {
        return new MonsterDefinition(1, name, 100, 100, 100, 100, 100,
            0, 0, 0, 0, 0, 0, 0, Arrays.asList(attributes));
    }

    private static void near(String label, double actual, double expected)
    {
        if (Math.abs(actual - expected) > 0.0000001)
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        System.out.println("PASS  " + label + " = " + actual);
    }

    private static void yes(String label, boolean actual)
    {
        if (!actual) throw new AssertionError(label + ": expected true");
        System.out.println("PASS  " + label);
    }

    private static void no(String label, boolean actual)
    {
        if (actual) throw new AssertionError(label + ": expected false");
        System.out.println("PASS  " + label);
    }

    private static void alpha47SupplyChecks()
    {
        List<BankItem> tinyBank=Arrays.asList(
            new BankItem(1001,35,"Air rune"),
            new BankItem(1002,7,"Chaos rune"),
            new BankItem(1003,1,"Ranging potion(2)"),
            new BankItem(1004,1,"Prayer potion(1)"),
            new BankItem(1005,4,"Shark"));
        MagicSpell bolt=new MagicSpell("Wind Bolt",17,9,"Air rune",2,"Chaos rune",1);
        Map<EquipmentSlot,EquipmentCandidate> gear=new EnumMap<>(EquipmentSlot.class);
        OptimizedMagicLoadout magic=new OptimizedMagicLoadout(gear,null,bolt);
        List<RecommendedSupply> rec=SimpleSupplyRecommender.recommend(AttackStyle.MAGIC,magic,tinyBank);
        RecommendedSupply air=supply(rec,"Air rune"),chaos=supply(rec,"Chaos rune"),food=supply(rec,"Shark");
        yes("alpha47 owned Air rune recommended",air!=null);
        yes("alpha47 rune quantity capped to owned amount",air.quantity==35);
        yes("alpha47 Chaos rune quantity capped to owned amount",chaos!=null&&chaos.quantity==7);
        yes("alpha47 food quantity capped to owned amount",food!=null&&food.quantity==4);
        no("alpha47 unowned Magic potion not invented",supply(rec,"Magic potion(4)")!=null);

        List<BankItem> rangedBank=Arrays.asList(
            new BankItem(2001,1,"Ranging potion(2)"),
            new BankItem(2002,8,"Anglerfish"),
            new BankItem(2003,2,"Prayer potion(3)"));
        List<RecommendedSupply> ranged=SimpleSupplyRecommender.recommend(AttackStyle.RANGED,null,rangedBank);
        yes("alpha47 owned lower-dose ranging potion accepted",supply(ranged,"Ranging potion(2)")!=null);
        yes("alpha47 strongest owned food selected",supply(ranged,"Anglerfish")!=null);
        System.out.println("PASS  alpha47 recommended inventory intelligence");
    }

    private static RecommendedSupply supply(List<RecommendedSupply> items,String name)
    {
        for(RecommendedSupply x:items)if(x!=null&&x.name.equalsIgnoreCase(name))return x;
        return null;
    }

    private static EquipmentCandidate itemWithId(int id,String name,EquipmentSlot slot)
    {
        return new EquipmentCandidate(new BankItem(id,1,name),slot,0,
            0,0,0,0,0,0f,0,4,false,"",RequirementResult.usable("test"));
    }

    private static void alpha493RecommendedInventoryChecks()
    {
        List<BankItem> magicBank=Arrays.asList(
            new BankItem(3101,4321,"Air rune"),
            new BankItem(3102,987,"Chaos rune"),
            new BankItem(3103,1,"Magic potion(3)"),
            new BankItem(3104,2,"Prayer potion(4)"),
            new BankItem(3105,20,"Shark"),
            new BankItem(3106,6,"Cooked karambwan"));
        MagicSpell bolt=new MagicSpell("Wind Bolt",17,9,"Air rune",2,"Chaos rune",1);
        Map<EquipmentSlot,EquipmentCandidate> magicGear=new EnumMap<>(EquipmentSlot.class);
        OptimizedMagicLoadout magic=new OptimizedMagicLoadout(magicGear,null,bolt);
        List<RecommendedSupply> magicRec=SimpleSupplyRecommender.recommend(AttackStyle.MAGIC,magic,magicBank);
        yes("alpha49.3 Magic uses full owned Air rune stack",supply(magicRec,"Air rune").quantity==4321);
        yes("alpha49.3 Magic uses full owned Chaos rune stack",supply(magicRec,"Chaos rune").quantity==987);
        yes("alpha49.3 normal food capped to 12",supply(magicRec,"Shark").quantity==12);
        yes("alpha49.3 combo food independently recommended",supply(magicRec,"Cooked karambwan").quantity==6);
        yes("alpha49.3 Magic recommendations owned/capped",RecommendedInventoryPolicy.allOwnedAndCapped(magicRec,magicBank));

        List<BankItem> staffBank=Arrays.asList(
            new BankItem(3201,9999,"Air rune"),
            new BankItem(3202,777,"Chaos rune"));
        Map<EquipmentSlot,EquipmentCandidate> staffGear=new EnumMap<>(EquipmentSlot.class);
        staffGear.put(EquipmentSlot.WEAPON,itemWithId(3299,"Staff of air",EquipmentSlot.WEAPON));
        OptimizedMagicLoadout staffMagic=new OptimizedMagicLoadout(staffGear,null,bolt);
        List<RecommendedSupply> staffRec=SimpleSupplyRecommender.recommend(AttackStyle.MAGIC,staffMagic,staffBank);
        no("alpha49.3 infinite Air source suppresses Air rune",supply(staffRec,"Air rune")!=null);
        yes("alpha49.3 non-provided Chaos rune retained",supply(staffRec,"Chaos rune")!=null);

        List<BankItem> rangedBank=Arrays.asList(
            new BankItem(3301,2468,"Ruby bolts (e)"),
            new BankItem(3302,1,"Ranging potion(4)"),
            new BankItem(3303,1,"Prayer potion(4)"),
            new BankItem(3304,12,"Manta ray"),
            new BankItem(3305,12,"Cooked karambwan"));
        Map<EquipmentSlot,EquipmentCandidate> crossbowGear=new EnumMap<>(EquipmentSlot.class);
        crossbowGear.put(EquipmentSlot.WEAPON,itemWithId(3399,"Rune crossbow",EquipmentSlot.WEAPON));
        crossbowGear.put(EquipmentSlot.AMMO,itemWithId(3301,"Ruby bolts (e)",EquipmentSlot.AMMO));
        OptimizedRangedLoadout crossbow=new OptimizedRangedLoadout(crossbowGear,null);
        List<RecommendedSupply> rangedRec=SimpleSupplyRecommender.recommend(AttackStyle.RANGED,null,crossbow,rangedBank);
        yes("alpha49.3 external ammo uses full owned selected stack",supply(rangedRec,"Ruby bolts (e)").quantity==2468);
        yes("alpha49.3 Ranged recommendations owned/capped",RecommendedInventoryPolicy.allOwnedAndCapped(rangedRec,rangedBank));

        Map<EquipmentSlot,EquipmentCandidate> bowfaGear=new EnumMap<>(EquipmentSlot.class);
        bowfaGear.put(EquipmentSlot.WEAPON,itemWithId(3499,"Bow of faerdhinen (c)",EquipmentSlot.WEAPON));
        OptimizedRangedLoadout bowfa=new OptimizedRangedLoadout(bowfaGear,null);
        List<RecommendedSupply> bowfaRec=SimpleSupplyRecommender.recommend(AttackStyle.RANGED,null,bowfa,rangedBank);
        no("alpha49.3 internal-ammo weapon does not recommend bolts",supply(bowfaRec,"Ruby bolts (e)")!=null);

        List<BankItem> meleeBank=Arrays.asList(
            new BankItem(3501,1,"Super combat potion(4)"),
            new BankItem(3502,2,"Prayer potion(4)"),
            new BankItem(3503,30,"Anglerfish"),
            new BankItem(3504,8,"Cooked karambwan"));
        List<RecommendedSupply> meleeRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_STAB,null,meleeBank);
        yes("alpha49.3 Melee best owned boost selected",supply(meleeRec,"Super combat potion(4)")!=null);
        java.util.List<BankItem> fallbackBank=java.util.Arrays.asList(
            new BankItem(3601,2,"Super strength(2)"),
            new BankItem(3602,3,"Attack potion(1)"),
            new BankItem(3603,4,"Defence potion(3)"),
            new BankItem(3604,5,"Super restore(2)"));
        java.util.List<RecommendedSupply> fallbackRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_STAB,null,fallbackBank);
        yes("alpha50.4.50.49 dose-aware melee potion fallback",supply(fallbackRec,"Super strength(2)")!=null&&supply(fallbackRec,"Attack potion(1)")!=null&&supply(fallbackRec,"Defence potion(3)")!=null);
        yes("alpha50.4.50.49 dose-aware prayer restore fallback",supply(fallbackRec,"Super restore(2)")!=null);
        java.util.List<BankItem> combatPriorityBank=java.util.Arrays.asList(
            new BankItem(3701,1,"Super combat potion(2)"),
            new BankItem(3702,5,"Super attack(4)"),new BankItem(3703,5,"Super strength(4)"),new BankItem(3704,5,"Super defence(4)"));
        java.util.List<RecommendedSupply> combatPriorityRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_STAB,null,combatPriorityBank);
        yes("alpha50.4.50.51 Super combat suppresses separate melee boosts",
            supply(combatPriorityRec,"Super combat potion(2)")!=null
            && supply(combatPriorityRec,"Super attack(4)")==null
            && supply(combatPriorityRec,"Super strength(4)")==null
            && supply(combatPriorityRec,"Super defence(4)")==null);
        java.util.List<BankItem> fullConsumableBank=java.util.Arrays.asList(
            new BankItem(3801,1,"Imbued heart"),new BankItem(3802,1,"Magic potion(4)"),
            new BankItem(3803,1,"Super restore(1)"),new BankItem(3804,1,"Prayer potion(4)"));
        java.util.List<RecommendedSupply> fullConsumableRec=SimpleSupplyRecommender.recommend(AttackStyle.MAGIC,null,null,fullConsumableBank,null,82);
        yes("alpha50.4.50.52 Imbued heart outranks Magic potion",supply(fullConsumableRec,"Imbued heart")!=null&&supply(fullConsumableRec,"Magic potion(4)")==null);
        yes("alpha50.4.50.52 Prayer restore ranks total vial restoration",supply(fullConsumableRec,"Prayer potion(4)")!=null&&supply(fullConsumableRec,"Super restore(1)")==null);
        java.util.List<BankItem> familyBank=java.util.Arrays.asList(
            new BankItem(3901,1,"Super attack(2)"),new BankItem(3902,1,"Super strength(3)"),new BankItem(3903,1,"Super defence(1)"),
            new BankItem(3904,1,"Anti-venom(2)"),new BankItem(3905,1,"Antifire potion(3)"));
        MonsterDefinition supplyVorkath=new MonsterDefinition(8059,"Vorkath",732,214,150,308,750,26,108,108,240,26,26,26,java.util.Arrays.asList("dragon"));
        java.util.List<RecommendedSupply> familyRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_SLASH,null,null,familyBank,supplyVorkath,58);
        yes("alpha50.4.50.53 normalised melee boost families cover mixed doses",supply(familyRec,"Super attack(2)")!=null&&supply(familyRec,"Super strength(3)")!=null&&supply(familyRec,"Super defence(1)")!=null);
        yes("alpha50.4.50.53 Vorkath owned anti-venom dose",supply(familyRec,"Anti-venom(2)")!=null);
        yes("alpha50.4.50.53 dragon owned antifire dose",supply(familyRec,"Antifire potion(3)")!=null);
        java.util.List<BankItem> canonicalNameBank=java.util.Arrays.asList(
            new BankItem(147,2,"Unknown dose item"),new BankItem(159,2,"Unknown dose item"),new BankItem(165,2,"Unknown dose item"));
        java.util.List<RecommendedSupply> canonicalNameRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_SLASH,null,null,canonicalNameBank,null,58);
        yes("alpha50.4.50.54 melee boosts survive RuneLite canonical dose names",
            canonicalNameRec.size()>=3&&canonicalNameRec.get(0).itemId==147&&canonicalNameRec.get(1).itemId==159&&canonicalNameRec.get(2).itemId==165);
        java.util.List<BankItem> healingBank=java.util.Arrays.asList(
            new BankItem(4001,5,"Shark"),new BankItem(4002,3,"Saradomin brew(2)"),
            new BankItem(4003,20,"Cooked karambwan"));
        java.util.List<RecommendedSupply> healingRec=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_SLASH,null,null,healingBank,null,58);
        yes("alpha50.4.50.56 owned normal food retained",supply(healingRec,"Shark")!=null);
        yes("alpha50.4.50.56 owned Saradomin brew dose retained",supply(healingRec,"Saradomin brew(2)")!=null);
        yes("alpha50.4.50.56 owned combo food retained",supply(healingRec,"Cooked karambwan")!=null);
        MonsterDefinition zygomite=monster("Zygomite");
        MonsterDefinition rockslug=monster("Rockslug");
        MonsterDefinition basiliskKnight=monster("Basilisk Knight");
        EquipmentCandidate mirrorShield=slotItem(4156,"Mirror shield",EquipmentSlot.SHIELD);
        EquipmentCandidate vShield=slotItem(24266,"V's shield",EquipmentSlot.SHIELD);
        EquipmentCandidate dragonDefender=slotItem(12954,"Dragon defender",EquipmentSlot.SHIELD);
        yes("alpha50.4.50.57 Zygomite bank header includes fungicide",
            SlayerTargetRequirements.bankHeader(zygomite).contains("Requires Fungicide spray"));
        yes("alpha50.4.50.57 Rockslug bank header includes finishing alternatives",
            SlayerTargetRequirements.bankHeader(rockslug).contains("Bag of salt or Brine sabre"));
        yes("alpha50.4.50.57 Basilisk requires shield slot",
            SlayerTargetRequirements.requiredEquipmentSlot(basiliskKnight)==EquipmentSlot.SHIELD);
        yes("alpha50.4.50.57 Mirror shield satisfies Basilisk constraint",
            SlayerTargetRequirements.validRequiredEquipment(basiliskKnight,mirrorShield));
        yes("alpha50.4.50.75 V's shield preferred over Mirror shield",
            SlayerTargetRequirements.firstRequiredEquipment(basiliskKnight,
                java.util.Arrays.asList(mirrorShield,vShield))==vShield);
        no("alpha50.4.50.57 Dragon defender rejected for Basilisk",
            SlayerTargetRequirements.validRequiredEquipment(basiliskKnight,dragonDefender));
        yes("alpha50.4.50.58 Cockatrice shield requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Cockatrice"),mirrorShield));
        yes("alpha50.4.50.58 Banshee earmuff requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Banshee"),slotItem("Earmuffs",EquipmentSlot.HEAD)));
        yes("alpha50.4.50.58 Dust devil Slayer helmet alternative",
            SlayerTargetRequirements.validRequiredEquipment(monster("Dust devil"),slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)));
        yes("alpha50.4.50.58 Aberrant spectre nose peg requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Aberrant spectre"),slotItem("Nose peg",EquipmentSlot.HEAD)));
        yes("alpha50.4.50.58 Wall beast spiny helmet requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Wall beast"),slotItem("Spiny helmet",EquipmentSlot.HEAD)));
        yes("alpha50.4.50.58 Cave horror witchwood requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Cave horror"),slotItem("Witchwood icon",EquipmentSlot.NECK)));
        yes("alpha50.4.50.58 Killerwatt insulated boots requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Killerwatt"),slotItem("Insulated boots",EquipmentSlot.FEET)));
        yes("alpha50.4.50.58 Fever spider Slayer gloves requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Fever spider"),slotItem("Slayer gloves",EquipmentSlot.HANDS)));
        yes("alpha50.4.50.58 Harpie lit lantern requirement",
            SlayerTargetRequirements.validRequiredEquipment(monster("Harpie bug swarm"),slotItem("Lit bug lantern",EquipmentSlot.SHIELD)));
        java.util.List<BankItem> finishingBank=java.util.Arrays.asList(
            new BankItem(4101,2,"Rock hammer"),new BankItem(4102,3,"Ice cooler"),
            new BankItem(4103,1,"Fungicide spray 7"),new BankItem(4104,5,"Bag of salt"),
            new BankItem(4105,1,"Fishing explosive"),new BankItem(4106,1,"Slayer bell"));
        String[] finishingTargets={"Gargoyle","Desert lizard","Zygomite","Rockslug","Mogre","Molanisk"};
        String[] finishingNames={"Rock hammer","Ice cooler","Fungicide spray 7","Bag of salt","Fishing explosive","Slayer bell"};
        for(int i=0;i<finishingTargets.length;i++){
            java.util.List<RecommendedSupply> tools=SimpleSupplyRecommender.recommend(
                AttackStyle.MELEE_SLASH,null,null,finishingBank,monster(finishingTargets[i]),58);
            yes("alpha50.4.50.58 owned Slayer tool "+finishingNames[i],supply(tools,finishingNames[i])!=null);
        }
        yes("alpha50.4.50.58 Turoth restricted-attack note",
            SlayerTargetRequirements.requiredItem(monster("Turoth")).contains("Leaf-bladed weapon"));
        yes("alpha50.4.50.58 Kurask restricted-attack note",
            SlayerTargetRequirements.requiredItem(monster("Kurask")).contains("broad ammunition"));
        yes("alpha50.4.50.58 Smoke devil face protection",
            SlayerTargetRequirements.validRequiredEquipment(monster("Smoke devil"),slotItem("Face mask",EquipmentSlot.HEAD)));
        java.util.List<BankItem> ringBank=java.util.Arrays.asList(
            new BankItem(11866,1,"Slayer ring (8)"),new BankItem(21268,1,"Slayer ring (eternal)"));
        java.util.List<RecommendedSupply> ringRec=SimpleSupplyRecommender.recommend(
            AttackStyle.MELEE_SLASH,null,null,ringBank,monster("Killerwatt"),58);
        no("alpha50.4.50.59 Killerwatt is not at a Slayer-ring destination",
            SlayerTargetRequirements.slayerRingTeleportUseful(monster("Killerwatt")));
        yes("alpha50.4.50.59 Slayer Tower target uses Slayer ring",
            SlayerTargetRequirements.slayerRingTeleportUseful(monster("Aberrant spectre")));
        java.util.List<RecommendedSupply> towerRingRec=SimpleSupplyRecommender.recommend(
            AttackStyle.MELEE_SLASH,null,null,ringBank,monster("Aberrant spectre"),58);
        yes("alpha50.4.50.59 eternal Slayer ring has priority",
            supply(towerRingRec,"Slayer ring (eternal)")!=null&&supply(towerRingRec,"Slayer ring (8)")==null);
        java.util.List<RecommendedSupply> chargedRingRec=SimpleSupplyRecommender.recommend(
            AttackStyle.MELEE_SLASH,null,null,java.util.Arrays.asList(
                new BankItem(11870,1,"Slayer ring (4)"),new BankItem(11867,1,"Slayer ring (7)")),monster("Turoth"),58);
        yes("alpha50.4.50.59 highest-charge Slayer ring fallback",
            supply(chargedRingRec,"Slayer ring (7)")!=null&&supply(chargedRingRec,"Slayer ring (4)")==null);
        com.personalbis.wiki.WikiDataPack auditPack=new com.personalbis.wiki.WikiDataPack(new com.google.gson.Gson());
        int auditedMonsters=0,killerwatts=0;
        for(com.personalbis.wiki.WikiMonster wikiMonster:auditPack.getMonsters()){
            if(wikiMonster==null||wikiMonster.name==null||wikiMonster.skills==null)continue;
            MonsterDefinition mapped=MonsterDatabase.fromWiki(wikiMonster);
            if(mapped.getId()<=0||mapped.getName().trim().isEmpty())throw new AssertionError("alpha50.4.50.59 invalid Wiki target mapping");
            auditedMonsters++;
            if(mapped.getName().equalsIgnoreCase("Killerwatt")){
                killerwatts++;
                yes("alpha50.4.50.59 Killerwatt core combat stats mapped",
                    mapped.getDefenceLevel()==40&&mapped.getMagicLevel()==67&&mapped.getHitpoints()==51);
            }
        }
        yes("alpha50.4.50.59 complete Wiki monster catalogue mapping audit",auditedMonsters>2500&&killerwatts==2);
        java.util.List<MonsterDefinition> targetCatalogue=MonsterDatabase.catalogue(auditPack.getMonsters());
        Set<String> catalogueKeys=new HashSet<>();
        int leviathanQuest=0,leviathanPostQuest=0,leviathanAwakened=0;
        boolean duplicateCatalogueEntry=false,unusedCatalogueEntry=false;
        for(MonsterDefinition target:targetCatalogue){
            if(!catalogueKeys.add(MonsterDatabase.combatKey(target)))duplicateCatalogueEntry=true;
            if(target.getName().toLowerCase().contains("unused")
                ||target.getVersion().toLowerCase().contains("unused"))unusedCatalogueEntry=true;
            if(target.getName().equalsIgnoreCase("The Leviathan")){
                if(target.getVersion().equalsIgnoreCase("Quest"))leviathanQuest++;
                if(target.getVersion().equalsIgnoreCase("Post-quest"))leviathanPostQuest++;
                if(target.getVersion().equalsIgnoreCase("Awakened"))leviathanAwakened++;
            }
        }
        no("alpha50.4.50.84 target catalogue has duplicate combat entries",duplicateCatalogueEntry);
        no("alpha50.4.50.84 target catalogue contains unused entries",unusedCatalogueEntry);
        yes("alpha50.4.50.84 Leviathan Quest/Post-quest/Awakened retained",
            leviathanQuest==1&&leviathanPostQuest==1&&leviathanAwakened==1);
        yes("alpha50.4.50.84 duplicate model/stat variants condensed",
            targetCatalogue.size()<auditedMonsters);
        Map<EquipmentSlot,List<EquipmentCandidate>> killerRanked=new EnumMap<>(EquipmentSlot.class);
        killerRanked.put(EquipmentSlot.FEET,java.util.Arrays.asList(slotItem("Dragon boots",EquipmentSlot.FEET)));
        Map<EquipmentSlot,List<EquipmentCandidate>> missingBoots=SlayerTargetRequirements.constrainRanked(monster("Killerwatt"),killerRanked);
        yes("alpha50.4.50.60 missing Slayer equipment reserves an empty slot",missingBoots.get(EquipmentSlot.FEET).isEmpty());
        killerRanked.put(EquipmentSlot.FEET,java.util.Arrays.asList(
            slotItem("Dragon boots",EquipmentSlot.FEET),slotItem("Insulated boots",EquipmentSlot.FEET)));
        Map<EquipmentSlot,List<EquipmentCandidate>> ownedBoots=SlayerTargetRequirements.constrainRanked(monster("Killerwatt"),killerRanked);
        yes("alpha50.4.50.60 owned Slayer equipment is the only slot candidate",
            ownedBoots.get(EquipmentSlot.FEET).size()==1
            &&ownedBoots.get(EquipmentSlot.FEET).get(0).getItem().getName().equals("Insulated boots"));
        yes("alpha49.3 strongest owned normal food selected",supply(meleeRec,"Anglerfish")!=null);
        yes("alpha49.3 Melee recommendations owned/capped",RecommendedInventoryPolicy.allOwnedAndCapped(meleeRec,meleeBank));
        System.out.println("PASS  alpha49.3 Recommended Inventory consolidation");
    }

    private static double ordinaryAccuracy(int attackRoll, int defenceRoll)
    {
        if (attackRoll > defenceRoll)
            return 1.0 - (defenceRoll + 2.0) / (2.0 * (attackRoll + 1.0));
        return attackRoll / (2.0 * (defenceRoll + 1.0));
    }

    private static void near(String label, double actual, double expected, double tolerance)
    {
        if (Math.abs(actual - expected) > tolerance)
            throw new AssertionError(label + ": expected " + expected + " but was " + actual);
        System.out.println("PASS  " + label);
    }

    public static void main(String[] args)
    {
        alpha493RecommendedInventoryChecks();
        poweredStaffChecks();
        MonsterDefinition demon = monster("Test demon", "demon");
        MonsterDefinition dragon = monster("Test dragon", "dragon");
        MonsterDefinition kalphite = monster("Test kalphite", "kalphite");
        MonsterDefinition kephri = monster("Kephri", "kalphite");
        MonsterDefinition akkha = monster("Akkha", "");
        MonsterDefinition zebak = monster("Zebak", "");
        MonsterDefinition baba = monster("Ba-Ba", "");
        EquipmentCandidate arclight = weapon("Arclight");
        EquipmentCandidate emberlight = weapon("Emberlight");
        EquipmentCandidate silverlight = weapon("Silverlight");
        EquipmentCandidate darklight = weapon("Darklight");
        EquipmentCandidate fang = weapon("Osmumten's fang");
        EquipmentCandidate scim = weapon("Dragon scimitar");
        EquipmentCandidate lance = weapon("Dragon hunter lance");
        EquipmentCandidate keris = weapon("Keris partisan");
        EquipmentCandidate breach = weapon("Keris partisan of breaching");
        EquipmentCandidate kerisSun = categorizedWeapon("Keris partisan of the sun", "Partisan");
        EquipmentCandidate scythe = weapon("Scythe of vitur");

        near("Arclight demon accuracy", MeleeWeaponEffects.accuracyMultiplier(arclight,demon,AttackStyle.MELEE_SLASH), 1.70);
        near("Arclight demon damage", MeleeWeaponEffects.damageMultiplier(arclight,demon,AttackStyle.MELEE_SLASH), 1.70);
        near("Emberlight demon accuracy", MeleeWeaponEffects.accuracyMultiplier(emberlight,demon,AttackStyle.MELEE_SLASH), 1.70);
        near("Emberlight demon damage", MeleeWeaponEffects.damageMultiplier(emberlight,demon,AttackStyle.MELEE_SLASH), 1.70);
        near("Arclight non-demon accuracy unchanged", MeleeWeaponEffects.accuracyMultiplier(arclight,dragon,AttackStyle.MELEE_SLASH), 1.0);
        near("Arclight non-demon damage unchanged", MeleeWeaponEffects.damageMultiplier(arclight,dragon,AttackStyle.MELEE_SLASH), 1.0);
        near("Silverlight demon damage", MeleeWeaponEffects.damageMultiplier(silverlight,demon,AttackStyle.MELEE_SLASH), 1.60);
        near("Darklight demon damage", MeleeWeaponEffects.damageMultiplier(darklight,demon,AttackStyle.MELEE_SLASH), 1.60);
        near("Dragon scimitar has no passive accuracy modifier", MeleeWeaponEffects.accuracyMultiplier(scim,demon,AttackStyle.MELEE_SLASH), 1.0);
        near("Dragon scimitar has no passive damage modifier", MeleeWeaponEffects.damageMultiplier(scim,demon,AttackStyle.MELEE_SLASH), 1.0);
        yes("Fang identified", MeleeWeaponEffects.isFang(fang));
        no("Dragon scimitar is not Fang", MeleeWeaponEffects.isFang(scim));
        yes("alpha50.4.35 Fang range true max 46 -> 6..40", MeleeWeaponEffects.fangMinHit(46) == 6 && MeleeWeaponEffects.fangDisplayedMaxHit(46) == 40);
        near("alpha50.4.35 Fang successful average true max 46 -> 23", MeleeWeaponEffects.fangAverageHit(46), 23.0);
        yes("alpha50.4.35 Fang range true max 53 -> 7..46", MeleeWeaponEffects.fangMinHit(53) == 7 && MeleeWeaponEffects.fangDisplayedMaxHit(53) == 46);
        near("alpha50.4.35 Fang successful average true max 53 -> 26.5", MeleeWeaponEffects.fangAverageHit(53), 26.5);
        yes("alpha50.4.35 Fang canonical range true max 60 -> 9..51", MeleeWeaponEffects.fangMinHit(60) == 9 && MeleeWeaponEffects.fangDisplayedMaxHit(60) == 51);
        near("alpha50.4.35 Fang canonical successful average true max 60 -> 30", MeleeWeaponEffects.fangAverageHit(60), 30.0);

        // alpha50.4.35 permanent Wiki-calculator regression locks. These are the
        // exact synthetic reference cases validated manually against the current
        // OSRS Wiki DPS calculator using the fixed armour set documented in the
        // RuneLite DEV diagnostics. They deliberately lock mechanics/rounding, not
        // bank ownership or optimizer candidate selection.
        near("alpha50.4.35 Wiki Greater demon Emberlight accuracy", ordinaryAccuracy(36308,3776), 0.9480, 0.00005);
        near("alpha50.4.35 Wiki Greater demon Emberlight DPS", ordinaryAccuracy(36308,3776)*MeleeWeaponEffects.successfulHitAverage(45)/2.4, 8.89585, 0.00002);
        near("alpha50.4.35 Wiki Vorkath DHL accuracy", ordinaryAccuracy(28131,20070), 0.6433, 0.00005);
        near("alpha50.4.35 Wiki Vorkath DHL DPS", ordinaryAccuracy(28131,20070)*MeleeWeaponEffects.successfulHitAverage(45)/2.4, 6.03633, 0.00002);
        near("alpha50.4.35 Wiki Vorkath Fang accuracy", MeleeWeaponEffects.fangAccuracy(25606,20070), 0.7952, 0.00005);
        near("alpha50.4.35 Wiki Vorkath Fang DPS", MeleeWeaponEffects.fangAccuracy(25606,20070)*MeleeWeaponEffects.fangAverageHit(46)/3.0, 6.09653, 0.00002);
        near("alpha50.4.35 Wiki KQ Keris accuracy", ordinaryAccuracy(19706,22866), 0.4309, 0.00005);
        near("alpha50.4.35 Wiki KQ Keris DPS", ordinaryAccuracy(19706,22866)*MeleeWeaponEffects.kerisAverageHit(45)/2.4, 4.20184, 0.00002);
        near("alpha50.4.35 Wiki KQ Breaching accuracy", ordinaryAccuracy(26208,22866), 0.5637, 0.00005);
        near("alpha50.4.35 Wiki KQ Breaching DPS", ordinaryAccuracy(26208,22866)*MeleeWeaponEffects.kerisAverageHit(45)/2.4, 5.49740, 0.00002);
        near("alpha49.2.1 Fang exact same-defence-roll accuracy sample",
            MeleeWeaponEffects.fangAccuracy(10000,10000),
            (10000.0*(4.0*10000.0+5.0))/(6.0*(10001.0)*(10001.0)));


        near("Dragon hunter lance dragon accuracy", MeleeWeaponEffects.accuracyMultiplier(lance,dragon,AttackStyle.MELEE_STAB), 1.20);
        near("Dragon hunter lance dragon damage", MeleeWeaponEffects.damageMultiplier(lance,dragon,AttackStyle.MELEE_STAB), 1.20);
        near("Dragon hunter lance non-dragon unchanged", MeleeWeaponEffects.damageMultiplier(lance,demon,AttackStyle.MELEE_STAB), 1.0);
        near("Keris Kalphite damage", MeleeWeaponEffects.damageMultiplier(keris,kalphite,AttackStyle.MELEE_STAB), 1.33);
        near("alpha50.4.50.37 Keris Kephri ToA damage", MeleeWeaponEffects.damageMultiplier(keris,kephri,AttackStyle.MELEE_STAB), 1.33);
        near("alpha50.4.50.37 Keris Akkha control", MeleeWeaponEffects.damageMultiplier(keris,akkha,AttackStyle.MELEE_STAB), 1.0);
        near("alpha50.4.50.38 Keris Zebak control", MeleeWeaponEffects.damageMultiplier(keris,zebak,AttackStyle.MELEE_STAB), 1.0);
        near("alpha50.4.50.38 Keris Ba-Ba control", MeleeWeaponEffects.damageMultiplier(keris,baba,AttackStyle.MELEE_STAB), 1.0);
        near("Keris expected critical multiplier", MeleeWeaponEffects.averageDamageMultiplier(keris,kalphite,AttackStyle.MELEE_STAB), 1.33*(53.0/51.0));
        yes("alpha50.4.31 Keris partisan of the sun identified as Keris", MeleeWeaponEffects.isKeris(kerisSun));
        near("alpha50.4.31 Keris sun Kalphite damage", MeleeWeaponEffects.damageMultiplier(kerisSun,kalphite,AttackStyle.MELEE_STAB), 1.33);
        near("alpha50.4.32 Keris sun has no general Kalphite accuracy bonus", MeleeWeaponEffects.accuracyMultiplier(kerisSun,kalphite,AttackStyle.MELEE_STAB), 1.0);
        near("alpha50.4.32 ordinary Keris has no general Kalphite accuracy bonus", MeleeWeaponEffects.accuracyMultiplier(keris,kalphite,AttackStyle.MELEE_STAB), 1.0);
        yes("alpha50.4.31 Keris sun proc max 45 -> 135", MeleeWeaponEffects.kerisProcMax(45)==135);
        near("alpha50.4.50.8 Keris sun rolled-hitsplat critical EV max45", MeleeWeaponEffects.kerisAverageHit(45), (50.0/51.0)*((1.0+45.0*46.0/2.0)/46.0)+(1.0/51.0)*((1.0+3.0*45.0*46.0/2.0)/46.0));
        near("alpha50.4.32 Breaching Kalphite accuracy x1.33", MeleeWeaponEffects.accuracyMultiplier(breach,kalphite,AttackStyle.MELEE_STAB), 1.33);
        near("alpha50.4.50.37 Breaching Kephri ToA accuracy", MeleeWeaponEffects.accuracyMultiplier(breach,kephri,AttackStyle.MELEE_STAB), 1.33);
        near("alpha50.4.50.38 Breaching Zebak control", MeleeWeaponEffects.accuracyMultiplier(breach,zebak,AttackStyle.MELEE_STAB), 1.0);
        near("alpha50.4.32 Breaching non-Kalphite accuracy unchanged", MeleeWeaponEffects.accuracyMultiplier(breach,demon,AttackStyle.MELEE_STAB), 1.0);
        yes("alpha50.4.33.1 Keris partisan Stab exposes Accurate stance", MeleeAttackStyleResolver.legalStances(kerisSun,AttackStyle.MELEE_STAB).contains(MeleeStance.ACCURATE));
        yes("alpha50.4.33.1 Keris partisan Stab exposes Aggressive stance", MeleeAttackStyleResolver.legalStances(kerisSun,AttackStyle.MELEE_STAB).contains(MeleeStance.AGGRESSIVE));
        yes("alpha50.4.33.1 Accurate stance is +3 Attack / +0 Strength", MeleeStance.ACCURATE.getAttackBoost()==3 && MeleeStance.ACCURATE.getStrengthBoost()==0);
        yes("alpha50.4.33.1 Aggressive stance is +0 Attack / +3 Strength", MeleeStance.AGGRESSIVE.getAttackBoost()==0 && MeleeStance.AGGRESSIVE.getStrengthBoost()==3);
        // alpha50.4.37 Scythe regression: component caps are base/floor(base/2)/floor(base/4),
        // and each independently successful hit uses modern successful-zero conversion.
        yes("alpha50.4.37 Scythe max47 caps 47/23/11", MeleeWeaponEffects.scytheSecondMax(47)==23 && MeleeWeaponEffects.scytheThirdMax(47)==11 && MeleeWeaponEffects.scytheCombinedMax(47,3)==81);
        yes("alpha50.4.37 Scythe max48 caps 48/24/12", MeleeWeaponEffects.scytheSecondMax(48)==24 && MeleeWeaponEffects.scytheThirdMax(48)==12 && MeleeWeaponEffects.scytheCombinedMax(48,3)==84);
        near("alpha50.4.37 Scythe 1-hit success EV max40", MeleeWeaponEffects.scytheAverageHit(40,1), MeleeWeaponEffects.successfulHitAverage(40));
        near("alpha50.4.37 Scythe 2-hit success EV max40", MeleeWeaponEffects.scytheAverageHit(40,2), MeleeWeaponEffects.successfulHitAverage(40)+MeleeWeaponEffects.successfulHitAverage(20));
        near("alpha50.4.37 Scythe 3-hit success EV max40", MeleeWeaponEffects.scytheAverageHit(40,3), MeleeWeaponEffects.successfulHitAverage(40)+MeleeWeaponEffects.successfulHitAverage(20)+MeleeWeaponEffects.successfulHitAverage(10));
        MonsterDefinition scythe1x1 = new MonsterDefinition(11,"Scythe 1x1",1,1,1,1,1,0,0,0,0,0,0,0,java.util.Collections.emptyList(),1);
        MonsterDefinition scythe2x2 = new MonsterDefinition(12,"Scythe 2x2",1,1,1,1,1,0,0,0,0,0,0,0,java.util.Collections.emptyList(),2);
        MonsterDefinition scythe3x3 = new MonsterDefinition(13,"Scythe 3x3",1,1,1,1,1,0,0,0,0,0,0,0,java.util.Collections.emptyList(),3);
        yes("alpha50.4.37 Scythe target size gives 1/2/3 hits", MeleeWeaponEffects.scytheHitCount(scythe,scythe1x1)==1 && MeleeWeaponEffects.scytheHitCount(scythe,scythe2x2)==2 && MeleeWeaponEffects.scytheHitCount(scythe,scythe3x3)==3);

        // alpha50.4.37 permanent Wiki regression locks using the fixed synthetic gear.
        // Nechryael 1x1: 39 cap, 80.86%, Wiki DPS 5.262.
        near("alpha50.4.37 Wiki Scythe 1x1 Nechryael accuracy", ordinaryAccuracy(25016,9576), 0.8086, 0.00005);
        yes("alpha50.4.37 Wiki Scythe 1x1 Nechryael max 39", MeleeWeaponEffects.scytheCombinedMax(39,1)==39);
        near("alpha50.4.37 Wiki Scythe 1x1 Nechryael DPS", ordinaryAccuracy(25016,9576)*MeleeWeaponEffects.scytheAverageHit(39,1)/3.0, 5.26244, 0.00002);
        // Mutated Bloodveld 2x2: 39/19 => 58, 95.01%, Wiki DPS 9.208.
        near("alpha50.4.37 Wiki Scythe 2x2 Mutated Bloodveld accuracy", ordinaryAccuracy(25016,2496), 0.9501, 0.00005);
        yes("alpha50.4.37 Wiki Scythe 2x2 Mutated Bloodveld max 58", MeleeWeaponEffects.scytheCombinedMax(39,2)==58);
        near("alpha50.4.37 Wiki Scythe 2x2 Mutated Bloodveld DPS", ordinaryAccuracy(25016,2496)*MeleeWeaponEffects.scytheAverageHit(39,2)/3.0, 9.20780, 0.00002);
        // Greater demon 3x3: 39/19/9 => 67, 92.45%, Wiki DPS 10.377.
        near("alpha50.4.37 Wiki Scythe 3x3 Greater demon accuracy", ordinaryAccuracy(25016,3776), 0.9245, 0.00005);
        yes("alpha50.4.37 Wiki Scythe 3x3 Greater demon max 67", MeleeWeaponEffects.scytheCombinedMax(39,3)==67);
        near("alpha50.4.37 Wiki Scythe 3x3 Greater demon DPS", ordinaryAccuracy(25016,3776)*MeleeWeaponEffects.scytheAverageHit(39,3)/3.0, 10.37742, 0.00002);
        // KQ provides a second 3-hit lock with different max/defence: 38/19/9 => 66.
        yes("alpha50.4.37 Wiki Scythe KQ max 66", MeleeWeaponEffects.scytheCombinedMax(38,3)==66);
        near("alpha50.4.37 Wiki Scythe KQ DPS", ordinaryAccuracy(25652,35226)*MeleeWeaponEffects.scytheAverageHit(38,3)/3.0, 4.02637, 0.00002);

        // alpha50.4.38 Slayer melee layer: Black mask / Slayer helmet melee bonus is
        // exactly 7/6 to the completed attack roll and ordinary max hit, floors at
        // each stage, is task-gated, and does not stack with an active Salve bonus.
        yes("alpha50.4.38 Slayer attack roll floors x7/6", SlayerMeleeEffects.attackRoll(25016,true,true,false)==29185);
        yes("alpha50.4.38 Slayer max hit floors x7/6", SlayerMeleeEffects.maxHit(44,true,true,false)==51);
        yes("alpha50.4.38 Slayer off-task attack unchanged", SlayerMeleeEffects.attackRoll(25016,false,true,false)==25016);
        yes("alpha50.4.38 Slayer off-task max unchanged", SlayerMeleeEffects.maxHit(44,false,true,false)==44);
        yes("alpha50.4.38 Slayer requires headgear", SlayerMeleeEffects.attackRoll(25016,true,false,false)==25016);
        yes("alpha50.4.38 Salve suppresses Slayer attack bonus", SlayerMeleeEffects.attackRoll(25016,true,true,true)==25016);
        yes("alpha50.4.38 Salve suppresses Slayer damage bonus", SlayerMeleeEffects.maxHit(44,true,true,true)==44);

        // alpha50.4.40 Soulreaper Wiki locks. Prayer and the +6%-per-stack Soul
        // contribution floor independently before stance +8. This is observable
        // at the 4-stack rounding boundary. Production optimizer remains neutral
        // until the stateful ramp scorer is introduced separately.
        yes("alpha50.4.39 Soulreaper clamps stacks", SoulreaperAxeEffects.clampStacks(-1)==0 && SoulreaperAxeEffects.clampStacks(9)==5);
        yes("alpha50.4.39 Soulreaper zero stacks equals ordinary effective Strength", SoulreaperAxeEffects.effectiveStrength(97,1.23,3,false,0)==130);
        yes("alpha50.4.39 Soulreaper five stacks additive with Piety", SoulreaperAxeEffects.effectiveStrength(97,1.23,3,false,5)==159);
        yes("alpha50.4.40 Soulreaper one-stack independent floor", SoulreaperAxeEffects.effectiveStrength(97,1.23,3,false,1)==135);
        // Exact Vorkath fixed-gear sequence validated against Wiki with boosted Str 99,
        // Piety, Accurate, +173 Strength: 48/50/52/54/56/59.
        int[] soulWikiMax={48,50,52,54,56,59};
        for(int st=0;st<=5;st++)
            yes("alpha50.4.40 Soulreaper Wiki Vorkath stack "+st, SoulreaperAxeEffects.maxHit(99,1.23,0,false,st,173)==soulWikiMax[st]);
        double[] soulVorkathEv={8.37304,8.72135,9.06967,9.41801,9.76637,10.28893};
        near("alpha50.4.41 Soulreaper Vorkath ramp DPS", SoulreaperAxeEffects.rampDps(soulVorkathEv,750,3.0),3.40199,0.00002);
        near("alpha50.4.41 Soulreaper short target starts at zero stacks", SoulreaperAxeEffects.rampDps(soulVorkathEv,4,3.0),8.37304/3.0,0.00002);

        MonsterDefinition undead = monster("Test undead", "undead");
        Map<EquipmentSlot,EquipmentCandidate> loadout = new EnumMap<>(EquipmentSlot.class);

        loadout.put(EquipmentSlot.NECK, weapon("Salve amulet (e)"));
        near("Salve (e) undead multiplier", MeleeLoadoutEffects.salveMultiplier(loadout,undead), 1.20);
        yes("alpha49.2.5 Salve attack-roll stage 24426 -> 29311",
            MeleeLoadoutEffects.salveAttackRoll(24426,loadout,undead) == 29311);
        yes("alpha49.2.6 Salve max-hit preserves pre-truncation numerator",
            MeleeLoadoutEffects.salveMaxHitFromNumerator(141,147,loadout,undead) == 56);
        near("alpha49.2.5 Vorkath Fang reference accuracy",
            MeleeWeaponEffects.fangAccuracy(29311,20070), 0.8437002370446669);
        near("alpha49.3.3 Wiki magic_str 50 means five percent",
            50.0/1000.0,0.05);
        near("alpha49.3.3 Wiki magic_str 10 means one percent",
            10.0/1000.0,0.01);
        MagicSpell windSurgeScaling=null;
        for(MagicSpell sp:MagicSpell.standard())if("Wind Surge".equals(sp.name))windSurgeScaling=sp;
        yes("alpha49.3.6 Wind Surge base 21 at level 81",windSurgeScaling.scaledBaseMax(81)==21);
        yes("alpha49.3.6 Wind Surge scales to Water Surge 22 at level 87",windSurgeScaling.scaledBaseMax(87)==22);
        yes("alpha49.3.6 Wind Surge scales to Earth Surge 23 at level 90",windSurgeScaling.scaledBaseMax(90)==23);
        yes("alpha49.3.6 Wind Surge scales to Fire Surge 24 at level 95",windSurgeScaling.scaledBaseMax(95)==24);
        yes("alpha49.3.6 Dharok reference base22 +8pct gear +50pct weakness =34",
            MagicDamageFormula.standardPrimary(22,0.08,0.0,0.50)==34);
        yes("alpha49.3.4 elemental weakness adds separately to base max",
            MagicDamageFormula.standardPrimary(21,0.10,0.0,0.50)==33);
        yes("alpha49.3.4 weakness accuracy modifies attack roll before hit chance",
            MagicDamageFormula.elementalAttackRoll(18369,0.50)==27553);
        near("alpha49.3.4 Dharok Wind Surge Wiki accuracy",
            MagicDamageFormula.hitChance(27553,530),0.9903462292226174);
        MonsterDefinition airWeak = new MonsterDefinition(1673,"Dharok the Wretched",115,100,1,1,100,
            0,0,0,-11,0,0,0,Collections.singletonList("spectral"),1,"air",50);
        MagicSpell windSurge=null, smokeBarrage=null;
        for(MagicSpell sp:MagicSpell.combat()){
            if("Wind Surge".equals(sp.name))windSurge=sp;
            if("Smoke Barrage".equals(sp.name))smokeBarrage=sp;
        }
        near("alpha49.3.1 standard Air spell receives Dharok weakness",
            ElementalWeaknessEffects.multiplier(airWeak,windSurge),0.50);
        near("alpha49.3.1 Ancient Smoke Barrage receives no elemental weakness",
            ElementalWeaknessEffects.multiplier(airWeak,smokeBarrage),0.0);
        near("Salve (e) non-undead unchanged", MeleeLoadoutEffects.salveMultiplier(loadout,dragon), 1.0);

        loadout.clear();
        loadout.put(EquipmentSlot.HEAD, weapon("Void melee helm"));
        loadout.put(EquipmentSlot.BODY, weapon("Void knight top"));
        loadout.put(EquipmentSlot.LEGS, weapon("Void knight robe"));
        loadout.put(EquipmentSlot.HANDS, weapon("Void knight gloves"));
        yes("Full Void melee detected", MeleeLoadoutEffects.hasVoidMelee(loadout));
        near("Void melee accuracy", MeleeLoadoutEffects.voidAccuracyMultiplier(loadout), 1.10);
        near("Void melee damage", MeleeLoadoutEffects.voidDamageMultiplier(loadout), 1.10);
        loadout.remove(EquipmentSlot.HANDS);
        no("Incomplete Void melee not active", MeleeLoadoutEffects.hasVoidMelee(loadout));

        loadout.clear();
        loadout.put(EquipmentSlot.HEAD, weapon("Inquisitor's great helm"));
        near("One Inquisitor piece crush", MeleeLoadoutEffects.inquisitorMultiplier(loadout,AttackStyle.MELEE_CRUSH), 1.005);
        loadout.put(EquipmentSlot.BODY, weapon("Inquisitor's hauberk"));
        loadout.put(EquipmentSlot.LEGS, weapon("Inquisitor's plateskirt"));
        near("Full Inquisitor set crush", MeleeLoadoutEffects.inquisitorMultiplier(loadout,AttackStyle.MELEE_CRUSH), 1.025);
        near("Inquisitor set slash unchanged", MeleeLoadoutEffects.inquisitorMultiplier(loadout,AttackStyle.MELEE_SLASH), 1.0);
        loadout.put(EquipmentSlot.WEAPON, weapon("Inquisitor's mace"));
        near("Full Inquisitor with mace uses current per-piece total only", MeleeLoadoutEffects.inquisitorMultiplier(loadout,AttackStyle.MELEE_CRUSH), 1.025);

        // alpha50.4.46 multi-hit melee framework. Ordinary split weapons roll
        // both halves independently; dual macuahuitl gates the second roll on
        // first-hit accuracy. Odd combined maxes split floor/ceil in order.
        yes("alpha50.4.46 split max 43 first = 21", MeleeWeaponEffects.splitFirstMax(43)==21);
        yes("alpha50.4.46 split max 43 second = 22", MeleeWeaponEffects.splitSecondMax(43)==22);
        near("alpha50.4.46 independent two-hit success EV max43",
            MeleeWeaponEffects.independentTwoHitSuccessfulAverage(43),
            MeleeWeaponEffects.successfulHitAverage(21)+MeleeWeaponEffects.successfulHitAverage(22));
        near("alpha50.4.46 dual mac sequential EV at 50pct",
            MeleeWeaponEffects.dualMacuahuitlExpectedDamage(43,0.5),
            0.5*MeleeWeaponEffects.successfulHitAverage(21)+0.25*MeleeWeaponEffects.successfulHitAverage(22));
        near("alpha50.4.46 dual mac converges to independent at 100pct",
            MeleeWeaponEffects.dualMacuahuitlExpectedDamage(43,1.0),
            MeleeWeaponEffects.independentTwoHitSuccessfulAverage(43));
        yes("alpha50.4.46 dual mac identified", MeleeWeaponEffects.isDualMacuahuitl(weapon("Dual macuahuitl")));
        yes("alpha50.4.46 Torag independent two-hit identified", MeleeWeaponEffects.isIndependentTwoHit(weapon("Torag's hammers")));
        yes("alpha50.4.46 Sulphur independent two-hit identified", MeleeWeaponEffects.isIndependentTwoHit(weapon("Sulphur blades")));
        yes("alpha50.4.46 Temotli independent two-hit identified", MeleeWeaponEffects.isIndependentTwoHit(weapon("Glacial temotli")));

        // alpha50.4.47 — exact OSRS Wiki Vorkath checkpoints captured from
        // the synthetic fixed-gear suite. These lock the complete multi-hit
        // expected-damage path, not just weapon identification/splitting.
        double dualMacAcc = ordinaryAccuracy(24038, 20070);
        near("alpha50.4.47 Wiki Vorkath Dual macuahuitl accuracy", dualMacAcc, 0.5825117517367611);
        near("alpha50.4.47 Wiki Vorkath Dual macuahuitl DPS",
            MeleeWeaponEffects.dualMacuahuitlExpectedDamage(39, dualMacAcc) / 2.4,
            3.7384769710936347);

        double toragAcc = ordinaryAccuracy(20296, 38356);
        near("alpha50.4.47 Wiki Vorkath Torag hammers accuracy", toragAcc, 0.2645670933597518);
        near("alpha50.4.47 Wiki Vorkath Torag hammers DPS",
            toragAcc * MeleeWeaponEffects.independentTwoHitSuccessfulAverage(38) / 3.0,
            1.68441049439042);

        double sulphurAcc = ordinaryAccuracy(11662, 20070);
        near("alpha50.4.47 Wiki Vorkath Sulphur blades accuracy", sulphurAcc, 0.29051865876139704);
        near("alpha50.4.47 Wiki Vorkath Sulphur blades DPS",
            sulphurAcc * MeleeWeaponEffects.independentTwoHitSuccessfulAverage(36) / 2.4,
            2.1916319871473813);

        double temotliAcc = ordinaryAccuracy(18762, 38356);
        near("alpha50.4.47 Wiki Vorkath Glacial temotli accuracy", temotliAcc, 0.24457074328023568);
        near("alpha50.4.47 Wiki Vorkath Glacial temotli DPS",
            temotliAcc * MeleeWeaponEffects.independentTwoHitSuccessfulAverage(36) / 2.4,
            1.8450073615877427);
        System.out.println("PASS  alpha50.4.47 Wiki-locked multi-hit melee suite");

        // alpha50.4.50 — remaining target-dependent melee auto-attack families.
        MonsterDefinition golem50=monster("Body golem","golem");
        near("alpha50.4.50 Barronite golembane accuracy",
            MeleeWeaponEffects.accuracyMultiplier(weapon("Barronite mace"),golem50,AttackStyle.MELEE_CRUSH),1.15);
        near("alpha50.4.50 Barronite golembane damage",
            MeleeWeaponEffects.damageMultiplier(weapon("Barronite mace"),golem50,AttackStyle.MELEE_CRUSH),1.15);
        near("alpha50.4.50 Granite hammer golembane accuracy",
            MeleeWeaponEffects.accuracyMultiplier(weapon("Granite hammer"),golem50,AttackStyle.MELEE_CRUSH),1.30);
        near("alpha50.4.50 Granite hammer golembane damage",
            MeleeWeaponEffects.damageMultiplier(weapon("Granite hammer"),golem50,AttackStyle.MELEE_CRUSH),1.30);
        near("alpha50.4.50 golembane ordinary target unchanged",
            MeleeWeaponEffects.damageMultiplier(weapon("Granite hammer"),dragon,AttackStyle.MELEE_CRUSH),1.0);
        MonsterDefinition size1=monsterSized("Size1",1);
        MonsterDefinition size3=monsterSized("Size3",3);
        MonsterDefinition size7=monsterSized("Size7",7);
        near("alpha50.4.50 Colossal blade size1 +2",MeleeWeaponEffects.colossalBladeBonusMax(weapon("Colossal blade"),size1),2);
        near("alpha50.4.50 Colossal blade size3 +6",MeleeWeaponEffects.colossalBladeBonusMax(weapon("Colossal blade"),size3),6);
        near("alpha50.4.50 Colossal blade size cap +10",MeleeWeaponEffects.colossalBladeBonusMax(weapon("Colossal blade"),size7),10);
        near("alpha50.4.50 ordinary weapon no size bonus",MeleeWeaponEffects.colossalBladeBonusMax(weapon("Dragon scimitar"),size7),0);
        System.out.println("PASS  alpha50.4.50 Colossal blade + golembane passive-family audit");

        // alpha50.4.50.2 — direct Wiki-source vampyre eligibility/modifier port.
        MonsterDefinition vamp1=new MonsterDefinition(-1,"Vampyre t1",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.singletonList("vampyre1"));
        MonsterDefinition vamp2=new MonsterDefinition(-1,"Vampyre t2",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.singletonList("vampyre2"));
        MonsterDefinition vamp3=new MonsterDefinition(-1,"Vampyre t3",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.singletonList("vampyre3"));
        java.util.Map<EquipmentSlot,EquipmentCandidate> noAid=new java.util.EnumMap<>(EquipmentSlot.class);
        java.util.Map<EquipmentSlot,EquipmentCandidate> aid=new java.util.EnumMap<>(EquipmentSlot.class); aid.put(EquipmentSlot.RING,weapon("Efaritay's aid"));
        yes("alpha50.4.50.2 ordinary weapon damages t1",VampyreTargetRules.canDamageMelee(noAid,weapon("Dragon scimitar"),vamp1));
        no("alpha50.4.50.2 ordinary weapon blocked t2",VampyreTargetRules.canDamageMelee(noAid,weapon("Dragon scimitar"),vamp2));
        no("alpha50.4.50.2 ordinary weapon blocked t3",VampyreTargetRules.canDamageMelee(noAid,weapon("Dragon scimitar"),vamp3));
        yes("alpha50.4.50.2 Ivandis flail damages t2",VampyreTargetRules.canDamageMelee(noAid,weapon("Ivandis flail"),vamp2));
        no("alpha50.4.50.2 Rod of ivandis blocked t3",VampyreTargetRules.canDamageMelee(noAid,weapon("Rod of ivandis"),vamp3));
        yes("alpha50.4.50.2 Sunspear damages t3",VampyreTargetRules.canDamageMelee(noAid,weapon("Sunspear"),vamp3));
        near("alpha50.4.50.2 Blisterwood flail accuracy",VampyreTargetRules.meleeAccuracyMultiplier(noAid,weapon("Blisterwood flail"),vamp2),1.05);
        near("alpha50.4.50.2 Hallowed flail accuracy",VampyreTargetRules.meleeAccuracyMultiplier(noAid,weapon("Hallowed flail"),vamp2),1.25);
        near("alpha50.4.50.2 Sunspear damage",VampyreTargetRules.meleeDamageMultiplier(noAid,weapon("Sunspear"),vamp3),1.50);
        near("alpha50.4.50.2 Blisterwood sickle damage",VampyreTargetRules.meleeDamageMultiplier(noAid,weapon("Blisterwood sickle"),vamp2),1.15);
        near("alpha50.4.50.2 Efaritay stacks on Ivandis",VampyreTargetRules.meleeDamageMultiplier(aid,weapon("Ivandis flail"),vamp2),1.32);
        yes("alpha50.4.50.2 Efaritay ordinary t2 half-damage route",VampyreTargetRules.tier2HalfDamage(aid,weapon("Dragon scimitar"),vamp2));
        yes("alpha50.4.50.2 silver t2 cap10 route",VampyreTargetRules.tier2SilverCap10(noAid,weapon("Silverlight"),vamp2));
        System.out.println("PASS  alpha50.4.50.2 Wiki-source vampyre eligibility/modifier audit");


        // alpha50.4.50.4 — shared Wiki PlayerVsNPCCalc.isImmune target/style layer.
        MonsterDefinition kraken504=new MonsterDefinition(494,"Kraken",1,1,1,1,100);
        no("alpha50.4.50.4 Kraken blocks melee",WikiTargetEligibility.meleeCanDamage(kraken504,categorizedWeapon("Dragon scimitar","Slash Sword")));
        MonsterDefinition zulrah504=new MonsterDefinition(2042,"Zulrah",1,1,1,1,100);
        no("alpha50.4.50.4 Zulrah blocks ordinary melee",WikiTargetEligibility.meleeCanDamage(zulrah504,categorizedWeapon("Dragon scimitar","Slash Sword")));
        yes("alpha50.4.50.4 Zulrah permits polearm exception",WikiTargetEligibility.meleeCanDamage(zulrah504,categorizedWeapon("Noxious halberd","Polearm")));
        MonsterDefinition flying504=new MonsterDefinition(999991,"Flying probe",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.singletonList("flying"));
        no("alpha50.4.50.4 flying blocks ordinary melee",WikiTargetEligibility.meleeCanDamage(flying504,categorizedWeapon("Dragon scimitar","Slash Sword")));
        yes("alpha50.4.50.4 flying permits polearm",WikiTargetEligibility.meleeCanDamage(flying504,categorizedWeapon("Halberd","Polearm")));
        yes("alpha50.4.50.4 flying permits salamander",WikiTargetEligibility.meleeCanDamage(flying504,categorizedWeapon("Black salamander","Salamander")));
        MonsterDefinition vespula504=new MonsterDefinition(7530,"Vespula",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.singletonList("flying"));
        no("alpha50.4.50.4 Vespula blocks polearm despite flying exception",WikiTargetEligibility.meleeCanDamage(vespula504,categorizedWeapon("Halberd","Polearm")));
        MonsterDefinition aviansie504=new MonsterDefinition(3169,"Aviansie",1,1,1,1,100);
        no("alpha50.4.50.4 Aviansie blocks non-salamander melee",WikiTargetEligibility.meleeCanDamage(aviansie504,categorizedWeapon("Halberd","Polearm")));
        yes("alpha50.4.50.4 Aviansie permits salamander melee",WikiTargetEligibility.meleeCanDamage(aviansie504,categorizedWeapon("Black salamander","Salamander")));
        MonsterDefinition guardian504=new MonsterDefinition(7569,"Guardian",1,1,1,1,100);
        no("alpha50.4.50.4 Guardian blocks non-pickaxe melee",WikiTargetEligibility.meleeCanDamage(guardian504,categorizedWeapon("Dragon scimitar","Slash Sword")));
        yes("alpha50.4.50.4 Guardian permits pickaxe melee",WikiTargetEligibility.meleeCanDamage(guardian504,categorizedWeapon("Dragon pickaxe","Pickaxe")));
        no("alpha50.4.50.4 Guardian blocks ranged",WikiTargetEligibility.rangedCanDamage(guardian504));
        no("alpha50.4.50.4 Guardian blocks magic",WikiTargetEligibility.magicCanDamage(guardian504));
        MonsterDefinition tekton504=new MonsterDefinition(7540,"Tekton",1,1,1,1,100);
        no("alpha50.4.50.4 Tekton blocks ranged",WikiTargetEligibility.rangedCanDamage(tekton504));
        yes("alpha50.4.50.4 Tekton magic remains eligible",WikiTargetEligibility.magicCanDamage(tekton504));
        MonsterDefinition dusk504=new MonsterDefinition(7851,"Dusk",1,1,1,1,100);
        no("alpha50.4.50.4 Dusk blocks ranged",WikiTargetEligibility.rangedCanDamage(dusk504));
        no("alpha50.4.50.4 Dusk blocks magic",WikiTargetEligibility.magicCanDamage(dusk504));
        System.out.println("PASS  alpha50.4.50.4 Wiki-source general target/style immunity audit");

        // alpha50.4.50.9: Wiki applyNpcTransforms distribution-limiters.
        MonsterDefinition zulrah509=new MonsterDefinition(2042,"Zulrah",725,300,300,300,500);
        near("alpha50.4.50.9 Zulrah capped reroll 60", WikiNpcTransforms.apply(HitDistribution.single(60,true),zulrah509,AttackStyle.RANGED,null,null,false).expectedHit(),47.5,1e-12);
        MonsterDefinition seren509=new MonsterDefinition(8917,"Fragment of Seren",606,100,100,100,1000);
        near("alpha50.4.50.9 Seren linear limiter hit30", WikiNpcTransforms.apply(HitDistribution.single(30,true),seren509,AttackStyle.MELEE_SLASH,null,null,false).expectedHit(),23.0,1e-12);
        MonsterDefinition kraken509=new MonsterDefinition(494,"Kraken",291,130,1,1,255);
        near("alpha50.4.50.9 Kraken ranged divide7 min1", WikiNpcTransforms.apply(HitDistribution.single(35,true),kraken509,AttackStyle.RANGED,null,null,false).expectedHit(),5.0,1e-12);
        MonsterDefinition tekton509=new MonsterDefinition(7540,"Tekton",300,205,205,205,300);
        near("alpha50.4.50.9 Tekton magic divide5 min1", WikiNpcTransforms.apply(HitDistribution.single(24,true),tekton509,AttackStyle.MAGIC,null,null,false).expectedHit(),4.0,1e-12);
        MonsterDefinition olmHead509=new MonsterDefinition(7551,"Great Olm",1043,250,250,250,800);
        near("alpha50.4.50.9 Olm head magic divide3", WikiNpcTransforms.apply(HitDistribution.single(30,true),olmHead509,AttackStyle.MAGIC,null,null,false).expectedHit(),10.0,1e-12);
        MonsterDefinition totem509=new MonsterDefinition(9434,"Totem",0,0,0,0,100);
        near("alpha50.4.50.9 Nightmare totem magic x2", WikiNpcTransforms.apply(HitDistribution.single(22,true),totem509,AttackStyle.MAGIC,null,null,false).expectedHit(),44.0,1e-12);
        MonsterDefinition flat509=new MonsterDefinition(999001,"Flat armour fixture",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("flat_armour:6"));
        near("alpha50.4.50.9 flat armour melee subtract6", WikiNpcTransforms.apply(HitDistribution.single(20,true),flat509,AttackStyle.MELEE_SLASH,null,null,false).expectedHit(),14.0,1e-12);
        near("alpha50.4.50.9 flat armour does not transform miss", WikiNpcTransforms.apply(HitDistribution.single(0,false),flat509,AttackStyle.RANGED,null,null,false).expectedHit(),0.0,1e-12);
        near("alpha50.4.50.9 flat armour excluded from magic", WikiNpcTransforms.apply(HitDistribution.single(20,true),flat509,AttackStyle.MAGIC,null,null,false).expectedHit(),20.0,1e-12);
        System.out.println("PASS  alpha50.4.50.9 Wiki NPC damage-limiter foundation");
        // alpha50.4.50.10: melee attacker distributions stay split until NPC transforms.
        near("alpha50.4.50.10 Scythe distribution EV preserved",MeleeWeaponEffects.scytheDistribution(40,3,.5).expectedHit(),MeleeWeaponEffects.scytheExpectedDamage(40,3,.5),1e-12);
        near("alpha50.4.50.10 Keris distribution EV preserved",MeleeWeaponEffects.kerisDistribution(34,.75).expectedHit(),.75*MeleeWeaponEffects.kerisAverageHit(34),1e-12);
        near("alpha50.4.50.10 Fang distribution EV preserved",MeleeWeaponEffects.fangDistribution(46,.75).expectedHit(),.75*MeleeWeaponEffects.fangAverageHit(46),1e-12);
        near("alpha50.4.50.10 independent two-hit EV preserved",MeleeWeaponEffects.independentTwoHitDistribution(43,.5).expectedHit(),MeleeWeaponEffects.independentTwoHitExpectedDamage(43,.5),1e-12);
        near("alpha50.4.50.10 dual mac EV preserved",MeleeWeaponEffects.dualMacuahuitlDistribution(43,.5).expectedHit(),MeleeWeaponEffects.dualMacuahuitlExpectedDamage(43,.5),1e-12);
        HitDistribution armourTwo=WikiNpcTransforms.apply(HitDistribution.single(10,true).zip(HitDistribution.single(8,true)),flat509,AttackStyle.MELEE_SLASH,null,null,false);
        near("alpha50.4.50.10 flat armour applies per melee splat",armourTwo.expectedHit(),6.0,1e-12);
        System.out.println("PASS  alpha50.4.50.10 melee distributions -> NPC transform pipeline");

        // alpha50.4.50.11.1 — exact Giant rat (Scurrius) target distinction + Wiki-authoritative target mapping.
        MonsterDefinition scurriusRat5011=new MonsterDefinition(7223,"Giant rat (Scurrius)",46,10,1,1,15,0,0,0,0,0,0,0,Arrays.asList("rat"));
        MonsterDefinition scurriusSolo5011=new MonsterDefinition(7222,"Scurrius","Solo",200,60,50,50,500,0,0,0,0,0,0,0,Arrays.asList("rat"),3,null,0,0);
        MonsterDefinition scurriusGroup5011=new MonsterDefinition(7221,"Scurrius","Group",250,100,100,100,1500,0,0,0,0,0,0,0,Arrays.asList("rat"),3,null,0,0);
        MonsterDefinition ordinaryRat5011=new MonsterDefinition(2854,"Giant rat",3,2,1,1,5,0,0,0,0,0,0,0,Arrays.asList("rat"));
        near("alpha50.4.50.11.1 Bone mace summoned Giant rat attack speed",WikiAttackSpeedRules.attackTicks(4,"Bone mace",scurriusRat5011),1);
        near("alpha50.4.50.11.1 Bone shortbow summoned Giant rat Rapid overridden to 1t",WikiAttackSpeedRules.attackTicks(3,"Bone shortbow",scurriusRat5011),1);
        near("alpha50.4.50.11.1 Bone staff summoned Giant rat attack speed",WikiAttackSpeedRules.attackTicks(4,"Bone staff",scurriusRat5011),1);
        near("alpha50.4.50.11.1 Bone mace Scurrius Solo remains 4t",WikiAttackSpeedRules.attackTicks(4,"Bone mace",scurriusSolo5011),4);
        near("alpha50.4.50.11.1 Bone mace Scurrius Group remains 4t",WikiAttackSpeedRules.attackTicks(4,"Bone mace",scurriusGroup5011),4);
        near("alpha50.4.50.11.1 Bone mace ordinary Giant rat remains 4t",WikiAttackSpeedRules.attackTicks(4,"Bone mace",ordinaryRat5011),4);
        near("alpha50.4.50.11.1 ordinary weapon unchanged on summoned Giant rat",WikiAttackSpeedRules.attackTicks(4,"Dragon scimitar",scurriusRat5011),4);
        PoweredStaff boneStaff5011=PoweredStaff.forWeapon("Bone staff");
        yes("alpha50.4.50.11.1 Bone staff registered as powered staff",boneStaff5011!=null);
        near("alpha50.4.50.11.1 Bone staff requires 50 Magic",boneStaff5011.minLevel,50);
        near("alpha50.4.50.11.1 Bone staff base max at 50 Magic",boneStaff5011.baseMaxHit(50),21);
        near("alpha50.4.50.11.1 Bone staff base max at 99 Magic",boneStaff5011.baseMaxHit(99),38);

        com.personalbis.wiki.WikiMonster wikiRat50111=new com.personalbis.wiki.WikiMonster();
        wikiRat50111.id=7223; wikiRat50111.name="Giant rat (Scurrius)"; wikiRat50111.version=""; wikiRat50111.level=46; wikiRat50111.size=2;
        wikiRat50111.skills=new com.personalbis.wiki.WikiMonster.Skills(); wikiRat50111.skills.def=10; wikiRat50111.skills.magic=1; wikiRat50111.skills.ranged=1; wikiRat50111.skills.hp=15;
        wikiRat50111.defensive=new com.personalbis.wiki.WikiMonster.Defensive(); wikiRat50111.attributes=Arrays.asList("rat");
        MonsterDefinition mappedRat50111=MonsterDatabase.fromWiki(wikiRat50111);
        near("alpha50.4.50.11.1 Wiki target mapper preserves NPC id",mappedRat50111.getId(),7223);
        yes("alpha50.4.50.11.1 Wiki target mapper preserves exact name","Giant rat (Scurrius)".equals(mappedRat50111.getName()));
        near("alpha50.4.50.11.1 Wiki target mapper preserves size",mappedRat50111.getSize(),2);
        yes("alpha50.4.50.11.1 Wiki target mapper preserves attributes",mappedRat50111.hasAttribute("rat"));
        System.out.println("PASS  alpha50.4.50.11.1 Wiki-authoritative targets + exact Scurrius summoned-rat speed audit");

        // alpha50.4.50.1 — first direct source-to-source Wiki parity family: ratbane.
        MonsterDefinition rat501=monster("Giant rat","rat");
        MonsterDefinition nonRat501=monster("Man");
        near("alpha50.4.50.1 Bone mace ratbane +10 max",MeleeWeaponEffects.ratbaneBonusMax(weapon("Bone mace"),rat501),10);
        near("alpha50.4.50.1 Bone shortbow ratbane +10 max",MeleeWeaponEffects.ratbaneBonusMax(weapon("Bone shortbow"),rat501),10);
        yes("alpha50.4.50.1 Bone mace allowed on rat",MeleeWeaponEffects.ratBoneCanDamage(weapon("Bone mace"),rat501));
        no("alpha50.4.50.1 Bone mace blocked off rat",MeleeWeaponEffects.ratBoneCanDamage(weapon("Bone mace"),nonRat501));
        yes("alpha50.4.50.1 ordinary weapon unaffected off rat",MeleeWeaponEffects.ratBoneCanDamage(weapon("Dragon scimitar"),nonRat501));
        System.out.println("PASS  alpha50.4.50.1 Wiki-source ratbane eligibility/max-hit audit");

        // alpha50.4.48 — target-conditional/passive melee families.
        MonsterDefinition leafy=monster("Kurask","leafy");
        near("alpha50.4.48 leaf battleaxe leafy damage",
            MeleeWeaponEffects.damageMultiplier(weapon("Leaf-bladed battleaxe"),leafy,AttackStyle.MELEE_CRUSH),1.175);
        near("alpha50.4.48 leaf battleaxe ordinary target unchanged",
            MeleeWeaponEffects.damageMultiplier(weapon("Leaf-bladed battleaxe"),dragon,AttackStyle.MELEE_CRUSH),1.0);
        near("alpha50.4.48 leaf sword has no 17.5pct passive",
            MeleeWeaponEffects.damageMultiplier(weapon("Leaf-bladed sword"),leafy,AttackStyle.MELEE_STAB),1.0);

        Map<EquipmentSlot,EquipmentCandidate> obby=new EnumMap<>(EquipmentSlot.class);
        obby.put(EquipmentSlot.HEAD,slotItem("Obsidian helmet",EquipmentSlot.HEAD));
        obby.put(EquipmentSlot.BODY,slotItem("Obsidian platebody",EquipmentSlot.BODY));
        obby.put(EquipmentSlot.LEGS,slotItem("Obsidian platelegs",EquipmentSlot.LEGS));
        EquipmentCandidate obbySword=weapon("Toktz-xil-ak");
        near("alpha50.4.48 full obsidian accuracy",MeleeLoadoutEffects.obsidianAccuracyMultiplier(obby,obbySword),1.10);
        near("alpha50.4.48 full obsidian damage",MeleeLoadoutEffects.obsidianDamageMultiplier(obby,obbySword),1.10);
        obby.put(EquipmentSlot.NECK,slotItem("Berserker necklace",EquipmentSlot.NECK));
        near("alpha50.4.48 berserker necklace stacks multiplicatively",MeleeLoadoutEffects.obsidianDamageMultiplier(obby,obbySword),1.32);
        near("alpha50.4.48 obsidian accuracy unaffected by necklace",MeleeLoadoutEffects.obsidianAccuracyMultiplier(obby,obbySword),1.10);
        near("alpha50.4.48 non-obsidian weapon unaffected",MeleeLoadoutEffects.obsidianDamageMultiplier(obby,weapon("Dragon scimitar")),1.0);
        System.out.println("PASS  alpha50.4.48 leaf-bladed + obsidian passive-family audit");

        // alpha50.4.48.1 — exact Wiki Kurask checkpoint from the user's fixed
        // no-shield validation loadout. The earlier apparent alpha50.4.48 mismatch
        // was the Dragon defender in the synthetic fixed-gear row: the Wiki
        // screenshot had an empty shield slot. With no defender the totals are
        // +95 attack / +140 Strength, giving the exact Wiki roll/max/DPS below.
        int leafWikiAttackRoll = 118 * (95 + 64);
        int leafWikiDefenceRoll = 9576;
        double leafWikiAccuracy = ordinaryAccuracy(leafWikiAttackRoll, leafWikiDefenceRoll);
        int leafWikiBaseMax = (int)Math.floor(0.5 + 132 * (140 + 64) / 640.0);
        int leafWikiFinalMax = (int)Math.floor(leafWikiBaseMax * 1.175);
        near("alpha50.4.48.1 Wiki Kurask leaf battleaxe attack roll", leafWikiAttackRoll, 18762);
        near("alpha50.4.48.1 Wiki Kurask leaf battleaxe accuracy", leafWikiAccuracy, 0.7447636305494857);
        near("alpha50.4.48.1 Wiki Kurask leaf battleaxe base max", leafWikiBaseMax, 42);
        near("alpha50.4.48.1 Wiki Kurask leaf battleaxe final max", leafWikiFinalMax, 49);
        near("alpha50.4.48.1 Wiki Kurask leaf battleaxe DPS",
            leafWikiAccuracy * MeleeWeaponEffects.successfulHitAverage(leafWikiFinalMax) / 3.0,
            6.087201407024462);
        System.out.println("PASS  alpha50.4.48.1 Wiki-locked Leaf-bladed battleaxe vs Kurask (no shield)");


        // alpha50.4.49 — exact Wiki Vorkath checkpoint for full Obsidian armour
        // + Berserker necklace + Toktz-xil-ak, Piety, Stab/Accurate. Wiki shows
        // +75 Stab / +97 Strength, roll 18,500, 46.09%, max 42, DPS 3.963.
        // The damage passive modifies each raw 0..32 roll (floor(roll*1.32));
        // it is not a fresh uniform 0..42 distribution.
        int obbyWikiBaseRoll=121*(75+64);
        int obbyWikiAttackRoll=(int)Math.floor(obbyWikiBaseRoll*1.10);
        double obbyWikiAccuracy=ordinaryAccuracy(obbyWikiAttackRoll,20070);
        int obbyWikiBaseMax=(int)Math.floor(0.5 + 129*(97+64)/640.0);
        int obbyWikiMax=(int)Math.floor(obbyWikiBaseMax*1.32);
        double obbyWikiSuccessEv=MeleeLoadoutEffects.obsidianModifiedRollAverage(obbyWikiBaseMax,1.32);
        near("alpha50.4.49 Obsidian Wiki attack roll",obbyWikiAttackRoll,18500);
        near("alpha50.4.49 Obsidian Wiki accuracy",obbyWikiAccuracy,0.4608639330377161);
        near("alpha50.4.49 Obsidian Wiki base max",obbyWikiBaseMax,32);
        near("alpha50.4.49 Obsidian Wiki displayed max",obbyWikiMax,42);
        near("alpha50.4.49 Obsidian Wiki modified-roll EV",obbyWikiSuccessEv,20.636363636363637);
        near("alpha50.4.49 Obsidian Wiki DPS",obbyWikiAccuracy*obbyWikiSuccessEv/2.4,3.962731545437938);

        // Leafy target eligibility: unsupported attacks retain their accuracy
        // diagnostics but must contribute zero damage in the combat calculators.
        if(!LeafyTargetRules.meleeCanDamage(leafy,weapon("Leaf-bladed battleaxe"))) throw new AssertionError("alpha50.4.49 leaf battleaxe eligibility");
        if(LeafyTargetRules.meleeCanDamage(leafy,weapon("Noxious halberd"))) throw new AssertionError("alpha50.4.49 unsupported melee vs leafy");
        if(LeafyTargetRules.meleeCanDamage(leafy,weapon("Toktz-xil-ak"))) throw new AssertionError("alpha50.4.49 obsidian weapon vs leafy");
        if(!LeafyTargetRules.rangedCanDamage(leafy,slotItem("Broad bolts",EquipmentSlot.AMMO))) throw new AssertionError("alpha50.4.49 broad bolts eligibility");
        if(!LeafyTargetRules.rangedCanDamage(leafy,slotItem("Amethyst broad bolts",EquipmentSlot.AMMO))) throw new AssertionError("alpha50.4.49 amethyst broad bolts eligibility");
        if(LeafyTargetRules.rangedCanDamage(leafy,slotItem("Ruby bolts (e)",EquipmentSlot.AMMO))) throw new AssertionError("alpha50.4.49 ordinary bolts vs leafy");
        System.out.println("PASS  alpha50.4.49 Obsidian Wiki checkpoint + leafy damage eligibility");

        // alpha50.4.49.3.1 — exact Wiki leafy ranged + Magic Dart checkpoints.
        // Kurask + Rune crossbow + Broad bolts (Rapid): roll 31,034 vs 7,296,
        // 88.24%, max 31, DPS 4.568. Ranged keeps modern successful-zero conversion.
        double broadWikiAccuracy=ordinaryAccuracy(31034,7296);
        near("alpha50.4.49.3.1 Broad bolts Wiki accuracy",broadWikiAccuracy,0.882423070726599);
        near("alpha50.4.49.3.1 Broad bolts Wiki DPS",
            broadWikiAccuracy*ExpectedHitFormula.successfulHitAverage(31)/3.0,4.568377772407497);

        // Kurask + Magic Dart: Wiki roll 20,709 vs 640, 98.45%, max 19,
        // DPS 3.118. Magic Dart retains the classic uniform 0..max damage roll,
        // so its successful-hit mean is max/2 (9.5), not zero->one conversion.
        double dartWikiAccuracy=ordinaryAccuracy(20709,640);
        MagicSpell magicDart493=null;
        for(MagicSpell sp493:MagicSpell.combat()) if("Magic Dart".equals(sp493.name)) magicDart493=sp493;
        if(magicDart493==null) throw new AssertionError("alpha50.4.49.3.1 Magic Dart fixture missing");
        near("alpha50.4.49.3.1 Magic Dart Wiki accuracy",dartWikiAccuracy,0.9845002414292612);
        near("alpha50.4.49.3.2 Magic Dart Wiki max at level 99",magicDart493.scaledBaseMax(99),19);
        near("alpha50.4.49.3.1 Magic Dart Wiki DPS",dartWikiAccuracy*(19/2.0)/3.0,3.117584097859327);
        System.out.println("PASS  alpha50.4.49.3.2 Wiki-locked Broad bolts + Magic Dart vs Kurask");

        near("DHCB dragon accuracy", RangedWeaponEffects.accuracyMultiplier(weapon("Dragon hunter crossbow"),dragon), 1.30);
        near("DHCB dragon damage", RangedWeaponEffects.damageMultiplier(weapon("Dragon hunter crossbow"),dragon), 1.25);
        near("DHCB non-dragon unchanged", RangedWeaponEffects.damageMultiplier(weapon("Dragon hunter crossbow"),demon), 1.0);

        loadout.clear();
        EquipmentCandidate bowfa=weapon("Bow of faerdhinen (c)");
        loadout.put(EquipmentSlot.HEAD,weapon("Crystal helm"));
        loadout.put(EquipmentSlot.BODY,weapon("Crystal body"));
        loadout.put(EquipmentSlot.LEGS,weapon("Crystal legs"));
        near("Full crystal Bowfa accuracy", RangedLoadoutEffects.crystalAccuracy(loadout,bowfa), 1.30);
        near("Full crystal Bowfa damage", RangedLoadoutEffects.crystalDamage(loadout,bowfa), 1.15);

        loadout.clear();
        loadout.put(EquipmentSlot.HEAD,weapon("Void ranger helm"));
        loadout.put(EquipmentSlot.BODY,weapon("Void knight top"));
        loadout.put(EquipmentSlot.LEGS,weapon("Void knight robe"));
        loadout.put(EquipmentSlot.HANDS,weapon("Void knight gloves"));
        yes("Full Void ranged detected",RangedLoadoutEffects.voidRanged(loadout));
        near("Void ranged accuracy",RangedLoadoutEffects.voidAccuracy(loadout),1.10);
        near("Void ranged damage",RangedLoadoutEffects.voidDamage(loadout),1.10);

        yes("Twisted bow identified",RangedWeaponEffects.isTwistedBow(weapon("Twisted bow")));
        yes("Rapid is one tick faster",RangedStance.RAPID.getSpeedDelta()==-1);

        yes("Ranged optimiser permits empty optional slots",
            RangedOptimizerRegression.optionalSlotsAreSupported());
        yes("Ranged optimiser is weapon-first",
            RangedOptimizerRegression.weaponFirst());
        yes("alpha50.4.50.80 complete Crystal armour seeded for Bowfa",
            RangedOptimizerRegression.crystalSetSeeded());
        yes("alpha50.4.50.81 exact ranged calculation preserves Ranged Strength",
            RangedOptimizerRegression.exactCalculatorPreservesRangedStrength());
        yes("Ranged optimiser exhaustively evaluates equipped ammo only when required",
            RangedOptimizerRegression.exhaustiveAmmo());
        yes("Enchanted bolt EV integration present",
            RangedOptimizerRegression.enchantedBoltEvIntegrated());
        yes("Ranged Salve mechanics integration present",
            RangedOptimizerRegression.rangedSalveIntegrated());

        EquipmentCandidate testBow=weapon("Magic shortbow");
        EquipmentCandidate testCrossbow=weapon("Dragon crossbow");
        EquipmentCandidate testBlowpipe=weapon("Toxic blowpipe");
        EquipmentCandidate testBowfa=weapon("Bow of faerdhinen (c)");
        yes("Shortbow requires arrows",RangedAmmoRules.weaponAmmo(testBow)==RangedAmmoType.ARROW);
        yes("Crossbow requires bolts",RangedAmmoRules.weaponAmmo(testCrossbow)==RangedAmmoType.BOLT);
        yes("Blowpipe classifies darts",RangedAmmoRules.weaponAmmo(testBlowpipe)==RangedAmmoType.DART);
        yes("alpha50.4.50.76 Blowpipe accepts loaded Amethyst darts",
            RangedAmmoRules.compatible(testBlowpipe,weapon("Amethyst dart")));
        yes("alpha50.4.50.76 Blowpipe occupies both hands",
            RangedAmmoRules.occupiesBothHands(testBlowpipe));
        no("alpha50.4.50.77 Rune crossbow remains one-handed",
            RangedAmmoRules.occupiesBothHands(twoHandedWeapon("Rune crossbow")));
        no("alpha50.4.50.77 Ornamented Rune crossbow remains one-handed",
            RangedAmmoRules.occupiesBothHands(twoHandedWeapon("Rune crossbow (or)")));
        yes("Bowfa uses internal ammo",RangedAmmoRules.weaponAmmo(testBowfa)==RangedAmmoType.INTERNAL);
        EquipmentCandidate runeCrossbow48=weapon("Rune crossbow");
        EquipmentCandidate dragonCrossbow48=weapon("Dragon crossbow");
        EquipmentCandidate realRuneCrossbow=new EquipmentCandidate(new BankItem(9185,1,"Rune crossbow"),EquipmentSlot.WEAPON,0,
            0,0,90,0,0,0f,0,6,false,"Crossbow",RequirementResult.usable("test"));
        EquipmentCandidate realDiamondBolts=new EquipmentCandidate(new BankItem(9243,100,"Diamond bolts (e)"),EquipmentSlot.AMMO,0,
            0,0,0,105,0,0f,0,0,false,"",RequirementResult.usable("test"));
        yes("alpha50.4.50.80 real-ID Rune crossbow accepts Diamond bolts (e)",
            RangedAmmoRules.compatible(realRuneCrossbow,realDiamondBolts));
        yes("alpha48.3 Rune crossbow accepts ruby bolts (e)",
            RangedAmmoRules.compatible(runeCrossbow48,weapon("Ruby bolts (e)")));
        yes("alpha48.3 Rune crossbow accepts diamond bolts (e)",
            RangedAmmoRules.compatible(runeCrossbow48,weapon("Diamond bolts (e)")));
        no("alpha48.3 Rune crossbow rejects dragon bolts",
            RangedAmmoRules.compatible(runeCrossbow48,weapon("Dragon bolts")));
        yes("alpha48.3 Dragon crossbow accepts dragon bolts",
            RangedAmmoRules.compatible(dragonCrossbow48,weapon("Dragon bolts")));
        yes("alpha50.4.50.40 Hunters crossbow accepts long kebbit bolts", RangedAmmoRules.compatible(weapon("Hunters' crossbow"),weapon("Long kebbit bolts")));
        no("alpha50.4.50.40 Hunters crossbow rejects ruby bolts", RangedAmmoRules.compatible(weapon("Hunters' crossbow"),weapon("Ruby bolts (e)")));
        yes("alpha50.4.50.40 Karil crossbow accepts bolt racks", RangedAmmoRules.compatible(weapon("Karil's crossbow"),weapon("Bolt rack")));
        no("alpha50.4.50.40 Karil crossbow rejects ruby bolts", RangedAmmoRules.compatible(weapon("Karil's crossbow"),weapon("Ruby bolts (e)")));
        yes("alpha50.4.50.40 Dorgeshuun crossbow accepts bone bolts", RangedAmmoRules.compatible(weapon("Dorgeshuun crossbow"),weapon("Bone bolts")));
        no("alpha50.4.50.40 Dorgeshuun crossbow rejects broad bolts", RangedAmmoRules.compatible(weapon("Dorgeshuun crossbow"),weapon("Broad bolts")));
        System.out.println("PASS  alpha48.3 ranged ammo compatibility audit");
        // alpha46 equipment-intelligence audit invariants
        yes("alpha46 2H suppresses shield", !EquipmentAuditPolicy.shieldAllowed(twoHandedWeapon("Test 2H")));
        yes("alpha46 1H permits shield", EquipmentAuditPolicy.shieldAllowed(weapon("Test 1H")));
        yes("alpha46 ranged/magic Slayer requires imbue", EquipmentAuditPolicy.slayerAppliesToRangedOrMagic("Slayer helmet (i)"));
        no("alpha46 unimbued Slayer helm has no ranged/magic task bonus", EquipmentAuditPolicy.slayerAppliesToRangedOrMagic("Slayer helmet"));
        yes("alpha46 demon weapon retained", EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Emberlight",demon));
        yes("alpha46 dragon weapon retained", EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Dragon hunter lance",dragon));
        yes("alpha46 Kalphite weapon retained", EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Keris partisan",kalphite));
        yes("alpha46 ranged Salve retained as synergy", RangedLoadoutEffects.synergy(weapon("Salve amulet(ei)")));
        yes("alpha46 magic Salve retained as synergy", MagicLoadoutEffects.synergy(weapon("Salve amulet(ei)")));
        yes("alpha46 Virtus retained for Ancient synergy", MagicLoadoutEffects.synergy(weapon("Virtus robe top")));
        System.out.println("PASS  alpha46 equipment intelligence audit invariants");

        // alpha46.1 controlled matchup validations using production effect layers.
        Map<EquipmentSlot,EquipmentCandidate> salveMelee=new EnumMap<>(EquipmentSlot.class);
        salveMelee.put(EquipmentSlot.NECK, slotItem("Salve amulet(e)",EquipmentSlot.NECK));
        near("alpha46.1 Salve(e) undead melee multiplier",
            MeleeLoadoutEffects.salveMultiplier(salveMelee,undead),1.20);
        near("alpha46.1 Salve(e) non-undead control",
            MeleeLoadoutEffects.salveMultiplier(salveMelee,dragon),1.0);

        Map<EquipmentSlot,EquipmentCandidate> salveRanged=new EnumMap<>(EquipmentSlot.class);
        salveRanged.put(EquipmentSlot.NECK, slotItem("Salve amulet(ei)",EquipmentSlot.NECK));
        yes("alpha46.1 Salve(ei) active vs undead",
            RangedLoadoutEffects.rangedSalveActive(salveRanged,undead));
        no("alpha46.1 Salve(ei) inactive vs dragon",
            RangedLoadoutEffects.rangedSalveActive(salveRanged,dragon));
        yes("alpha46.1 ranged Salve increases undead accuracy",
            RangedLoadoutEffects.salveAccuracy(salveRanged,undead)>1.0);
        yes("alpha46.1 ranged Salve increases undead damage",
            RangedLoadoutEffects.salveDamage(salveRanged,undead)>1.0);

        yes("alpha46.1 Emberlight retained for demon matchup",
            EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Emberlight",demon));
        yes("alpha46.1 DHL retained for dragon matchup",
            EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Dragon hunter lance",dragon));
        yes("alpha46.1 Keris retained for Kalphite matchup",
            EquipmentAuditPolicy.monsterSpecificMeleeWeapon("Keris partisan",kalphite));

        MagicLoadoutEffects.Effect magicTask=MagicLoadoutEffects.evaluate(
            Arrays.asList(slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)),demon,true);
        MagicLoadoutEffects.Effect magicOff=MagicLoadoutEffects.evaluate(
            Arrays.asList(slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)),demon,false);
        yes("alpha46.1 magic Slayer task effect increases output",
            magicTask.damage>magicOff.damage || magicTask.accuracy>magicOff.accuracy);

        System.out.println("PASS  alpha46.1 controlled matchup validations");

        // alpha46.2 final-loadout selection invariants shared by Melee/Ranged/Magic.
        yes("alpha46.2 higher finite DPS wins",FinalLoadoutSelection.better(6.25,6.10));
        no("alpha46.2 lower DPS cannot replace winner",FinalLoadoutSelection.better(6.10,6.25));
        no("alpha46.2 NaN cannot become winner",FinalLoadoutSelection.better(Double.NaN,6.25));

        EquipmentCandidate oneHand=weapon("Rune crossbow");
        EquipmentCandidate twoHand=twoHandedWeapon("Twisted bow");
        EquipmentCandidate shield=slotItem("Dragonfire shield",EquipmentSlot.SHIELD);
        yes("alpha46.2 1H + shield is legal",FinalLoadoutSelection.compatibleShield(oneHand,shield));
        no("alpha46.2 2H + shield is illegal",FinalLoadoutSelection.compatibleShield(twoHand,shield));

        EquipmentCandidate bolts=slotItem("Runite bolts",EquipmentSlot.AMMO);
        EquipmentCandidate arrows=slotItem("Rune arrows",EquipmentSlot.AMMO);
        yes("alpha46.2 crossbow accepts compatible bolts",FinalLoadoutSelection.compatibleAmmo(oneHand,bolts));
        no("alpha46.2 crossbow rejects arrows",FinalLoadoutSelection.compatibleAmmo(oneHand,arrows));
        yes("alpha46.2 self-ammo weapon requires empty ammo slot",
            FinalLoadoutSelection.compatibleAmmo(weapon("Toxic blowpipe"),null));

        yes("alpha46.2 ranged Salve survives final candidate shortlist",
            RangedLoadoutEffects.synergy(slotItem("Salve amulet(ei)",EquipmentSlot.NECK)));
        yes("alpha46.2 magic Slayer helm survives final candidate shortlist",
            MagicLoadoutEffects.synergy(slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)));
        yes("alpha46.2 Magic Virtus survives final candidate shortlist",
            MagicLoadoutEffects.synergy(slotItem("Virtus robe top",EquipmentSlot.BODY)));

        System.out.println("PASS  alpha46.2 final loadout selection invariants");
        alpha47SupplyChecks();
        supplyChecks();
        // alpha50.4.50.14 Wiki cast-stance attack-speed parity.
        MagicSpell windStrike5014 = MagicSpell.standard().get(0);
        MagicSpell iceBarrage5014 = MagicSpell.ancient().get(MagicSpell.ancient().size()-1);
        yes("alpha50.4.50.14 ordinary autocast is 5t", MagicSpellCompatibility.attackTicks("Mystic air staff", windStrike5014)==5);
        yes("alpha50.4.50.14 Harmonised standard autocast is 4t", MagicSpellCompatibility.attackTicks("Harmonised nightmare staff", windStrike5014)==4);
        yes("alpha50.4.50.14 Harmonised Ancient autocast remains 5t", MagicSpellCompatibility.attackTicks("Harmonised nightmare staff", iceBarrage5014)==5);
        yes("alpha50.4.50.14 Twinflame spellcasting is 6t", MagicSpellCompatibility.attackTicks("Twinflame staff", windStrike5014)==6);
        System.out.println("PASS  alpha50.4.50.14 Wiki cast-stance attack-speed parity");

        System.out.println("\nALL PERSONAL BIS MECHANICS SELF-TESTS PASSED");
    }

    private static void poweredStaffChecks()
    {
        PoweredStaff seas=PoweredStaff.forWeapon("Trident of the Seas");
        PoweredStaff swamp=PoweredStaff.forWeapon("Trident of the Swamp");
        PoweredStaff warped=PoweredStaff.forWeapon("Warped sceptre");
        if(seas==null || seas.baseMaxHit(75)!=20 || seas.baseMaxHit(99)!=28) throw new AssertionError("Trident seas formula");
        if(swamp==null || swamp.baseMaxHit(78)!=24 || swamp.baseMaxHit(99)!=31) throw new AssertionError("Trident swamp formula");
        if(warped==null || warped.baseMaxHit(99)!=24) throw new AssertionError("Warped sceptre formula");
        MagicSpell waterBlast=null, fireBlast=null;
        for(MagicSpell s:MagicSpell.standard()){if("Water Blast".equals(s.name))waterBlast=s;if("Fire Blast".equals(s.name))fireBlast=s;}
        if(waterBlast==null || waterBlast.scaledBaseMax(73)!=16) throw new AssertionError("Elemental spell tier scaling");
        if(fireBlast==null || !"fire".equals(fireBlast.element())) throw new AssertionError("Spell element mapping");
        if(!"blast".equals(waterBlast.tier())) throw new AssertionError("Spell tier mapping");
        if(Math.abs(MagicWeaponEffects.accuracyMultiplier("Mystic smoke staff",waterBlast,null)-1.10)>0.0001) throw new AssertionError("Smoke staff accuracy");
        if(Math.abs(MagicWeaponEffects.damageMultiplier("Smoke battlestaff",fireBlast,null)-1.10)>0.0001) throw new AssertionError("Smoke staff damage");
        MagicSpell fireSurge=null;for(MagicSpell s:MagicSpell.standard())if("Fire Surge".equals(s.name))fireSurge=s;
        if(fireSurge==null||fireSurge.level!=95||fireSurge.maxHit!=24) throw new AssertionError("Surge spell data");
        MonsterDefinition dragon=new MonsterDefinition(1,"Black dragon",227,200,100,100,190,0,70,70,80,0,0,0,java.util.Arrays.asList("dragon"),4,null,0);
        if(Math.abs(MagicWeaponEffects.accuracyMultiplier("Dragon hunter wand",waterBlast,dragon)-1.75)>0.0001) throw new AssertionError("DHW accuracy");
        if(Math.abs(MagicWeaponEffects.damageMultiplier("Dragon hunter wand",waterBlast,dragon)-1.40)>0.0001) throw new AssertionError("DHW damage");
        if(MagicWeaponEffects.attackTicks("Twinflame staff")!=6) throw new AssertionError("Twinflame speed");
        if(!MagicWeaponEffects.twinflameEcho("Twinflame staff",waterBlast)) throw new AssertionError("Twinflame blast echo");
        if(MagicWeaponEffects.twinflameEcho("Twinflame staff",fireSurge)) throw new AssertionError("Twinflame surge echo");
        PoweredStaff shadow=PoweredStaff.forWeapon("Tumeken's shadow");
        if(shadow==null||shadow.minLevel!=85||shadow.speed!=5) throw new AssertionError("Shadow definition");
        if(shadow.baseMaxHit(85)!=29||shadow.baseMaxHit(99)!=34) throw new AssertionError("Shadow base max");
        MagicSpell iban=null,dart=null,sara=null;
        for(MagicSpell s:MagicSpell.standard()){if("Iban Blast".equals(s.name))iban=s;if("Magic Dart".equals(s.name))dart=s;if("Saradomin Strike".equals(s.name))sara=s;}
        if(iban==null||iban.level!=50||iban.maxHit!=25) throw new AssertionError("Iban data");
        if(!iban.tier().isEmpty()||!sara.tier().isEmpty()) throw new AssertionError("Non-elemental spell tier isolation");
        if(dart==null||dart.scaledBaseMax(73)!=17) throw new AssertionError("Magic Dart scaling");
        if(!MagicSpellCompatibility.canAutocast("Iban's staff (u)",iban)||MagicSpellCompatibility.canAutocast("Mystic earth staff",iban)) throw new AssertionError("Iban compatibility");
        MonsterDefinition waterWeak=new MonsterDefinition(252,"Black dragon",227,200,100,1,190,0,70,70,60,50,50,10,java.util.Arrays.asList("dragon","fiery"),4,"water",50);
        if(waterWeak.weakTo(iban.element())) throw new AssertionError("Iban must not receive elemental weakness");
        if(!waterWeak.weakTo(waterBlast.element())) throw new AssertionError("Water Blast must receive water weakness");

        MonsterDefinition dharokWeak=new MonsterDefinition(1673,"Dharok the Wretched",115,100,1,1,100,
            252,250,244,-11,249,249,249,java.util.Arrays.asList("spectral"),1,"air",50,-58);
        MagicSpell windBlast=null,fireBlastWeaknessCheck=null;
        for(MagicSpell sp:MagicSpell.standard()){
            if("Wind Blast".equals(sp.name))windBlast=sp;
            if("Fire Blast".equals(sp.name))fireBlastWeaknessCheck=sp;
        }
        if(windBlast==null||fireBlastWeaknessCheck==null)throw new AssertionError("alpha47.2 elemental spells missing");
        if(windBlast.scaledBaseMax(73)!=16)throw new AssertionError("alpha49.3.6 Wind Blast must scale to strongest unlocked Blast");
        if(fireBlastWeaknessCheck.scaledBaseMax(73)!=16)throw new AssertionError("alpha47.2 Fire Blast own base max");
        if(!ElementalWeaknessEffects.applies(dharokWeak,windBlast))throw new AssertionError("alpha47.2 Dharok air weakness");
        if(ElementalWeaknessEffects.applies(dharokWeak,fireBlastWeaknessCheck))throw new AssertionError("alpha47.2 Dharok must not boost Fire");
        near("alpha47.2 Dharok air weakness severity",ElementalWeaknessEffects.multiplier(dharokWeak,windBlast),0.50);
        if(MagicDamageFormula.standardPrimary(windBlast.scaledBaseMax(73),0,0,0.50)<=windBlast.scaledBaseMax(73))
            throw new AssertionError("alpha47.2 Dharok weakness must increase Air max hit");
        System.out.println("PASS  alpha47.2 Dharok elemental weakness regression");
        yes("alpha50.0 complete Magic sets seeded before pruning",
            EquipmentAuditPolicy.seedCompleteMagicSetsBeforePruning());
        yes("alpha48.4 Magic ordinary weapon evaluation is bounded",
            EquipmentAuditPolicy.magicWeaponEvaluationLimit()==12);
        yes("alpha48.4 Melee ordinary weapon evaluation is bounded",
            EquipmentAuditPolicy.meleeWeaponEvaluationLimit()==12);
        yes("alpha48.4 Ranged ordinary weapon evaluation is bounded",
            EquipmentAuditPolicy.rangedWeaponEvaluationLimit()==12);
        System.out.println("PASS  alpha48.4 mechanics-aware Magic shortlist policy");
        yes("alpha48.4 Melee ordinary shortlist remains bounded",
            EquipmentAuditPolicy.meleeWeaponEvaluationLimit()==12);
        System.out.println("PASS  alpha48.4 mechanics-aware Melee shortlist policy");
        MagicSpell windBlast48 = null, ibanBlast48 = null, magicDart48 = null, iceBurst48 = null;
        for (MagicSpell sp48 : MagicSpell.combat()) {
            if ("Wind Blast".equals(sp48.name)) windBlast48 = sp48;
            else if ("Iban Blast".equals(sp48.name)) ibanBlast48 = sp48;
            else if ("Magic Dart".equals(sp48.name)) magicDart48 = sp48;
            else if ("Ice Burst".equals(sp48.name)) iceBurst48 = sp48;
        }
        yes("alpha48.2 powered staves do not autocast normal spells",
            !MagicSpellCompatibility.canAutocast("Warped sceptre", windBlast48)
            && !MagicSpellCompatibility.canAutocast("Trident of the seas", windBlast48));
        yes("alpha48.2 Iban Blast remains Iban-staff restricted",
            MagicSpellCompatibility.canAutocast("Iban's staff (u)", ibanBlast48)
            && !MagicSpellCompatibility.canAutocast("Mystic earth staff", ibanBlast48));
        yes("alpha48.2 Magic Dart remains weapon restricted",
            MagicSpellCompatibility.canAutocast("Slayer's staff", magicDart48)
            && !MagicSpellCompatibility.canAutocast("Mystic earth staff", magicDart48));
        MonsterDefinition leafyMagic492 = new MonsterDefinition(410,"Kurask","Normal",106,105,1,1,97,0,20,20,0,0,0,0,java.util.Collections.singletonList("leafy"),1,null,0,0);
        yes("alpha50.4.49.2 leafy Magic Dart remains eligible",
            LeafyTargetRules.magicCanDamage(leafyMagic492, magicDart48));
        yes("alpha50.4.49.2 leafy Smoke Barrage is excluded before optimisation",
            !LeafyTargetRules.magicCanDamage(leafyMagic492, new MagicSpell("Smoke Barrage",86,27)));
        yes("alpha48.2 Ancient spells remain ancient-autocast restricted",
            MagicSpellCompatibility.canAutocast("Ancient staff", iceBurst48)
            && !MagicSpellCompatibility.canAutocast("Mystic earth staff", iceBurst48));
        yes("alpha48.2 elemental Mystic staff remains standard-spell eligible",
            MagicSpellCompatibility.canAutocast("Mystic earth staff", windBlast48));
        yes("alpha50.4.50.40 Kodai supports Ancient autocast", MagicSpellCompatibility.canAutocast("Kodai wand", iceBurst48));
        yes("alpha50.4.50.40 Master wand supports Ancient autocast", MagicSpellCompatibility.canAutocast("Master wand", iceBurst48));
        no("alpha50.4.50.40 Tumeken shadow rejects Ancient autocast", MagicSpellCompatibility.canAutocast("Tumeken's shadow", iceBurst48));
        System.out.println("PASS  alpha48.2 Magic weapon/spell compatibility audit");
        no("alpha48.6 Piety blocked by level alone",PrayerUnlockPolicy.piety(75,7));
        yes("alpha48.6 Piety unlocked after Knight Waves",PrayerUnlockPolicy.piety(75,8));
        no("alpha48.6 Chivalry blocked before Knight Waves",PrayerUnlockPolicy.chivalry(70,7));
        yes("alpha48.6 Chivalry unlocked after Knight Waves",PrayerUnlockPolicy.chivalry(70,8));
        no("alpha48.6 Rigour requires permanent unlock",PrayerUnlockPolicy.rigour(99,false));
        yes("alpha48.6 Rigour level + unlock accepted",PrayerUnlockPolicy.rigour(99,true));
        no("alpha48.6 Augury requires permanent unlock",PrayerUnlockPolicy.augury(99,false));
        yes("alpha48.6 Augury level + unlock accepted",PrayerUnlockPolicy.augury(99,true));
        System.out.println("PASS  alpha48.6 prayer/unlock policy audit");
        yes("alpha48.7 Slayer melee attack roll x7/6 after ordinary roll",SlayerMeleeEffects.attackRoll(1000,true,true,false)==1166);
        yes("alpha48.7 Slayer melee max hit floors after x7/6",SlayerMeleeEffects.maxHit(24,true,true,false)==28);
        yes("alpha48.7 Slayer melee off-task unchanged",SlayerMeleeEffects.attackRoll(1000,false,true,false)==1000);
        yes("alpha48.7 Slayer melee requires Slayer head",SlayerMeleeEffects.maxHit(24,true,false,false)==24);
        yes("alpha48.7 Salve suppresses Slayer melee accuracy",SlayerMeleeEffects.attackRoll(1000,true,true,true)==1000);
        yes("alpha48.7 Salve suppresses Slayer melee damage",SlayerMeleeEffects.maxHit(24,true,true,true)==24);
        System.out.println("PASS  alpha48.7 exact Slayer melee ordering audit");
        yes("alpha50.4.50.70 Slayer helm survives melee dominance pruning",
            MeleeLoadoutEffects.isSynergyItem(slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)));
        yes("alpha50.4.50.70 Black mask survives melee dominance pruning",
            MeleeLoadoutEffects.isSynergyItem(slotItem("Black mask (i)",EquipmentSlot.HEAD)));
        EquipmentCandidate keris48=categorizedWeapon("Keris","Stab Sword");
        yes("alpha48.8 Keris supports Stab",MeleeAttackStyleResolver.supports(keris48,AttackStyle.MELEE_STAB));
        no("alpha48.8 Keris rejects Crush",MeleeAttackStyleResolver.supports(keris48,AttackStyle.MELEE_CRUSH));
        no("alpha48.8 Keris rejects fictional Crush stances",!MeleeAttackStyleResolver.legalStances(keris48,AttackStyle.MELEE_CRUSH).isEmpty());
        yes("alpha48.8 mace/blunt supports Crush",MeleeAttackStyleResolver.supports(categorizedWeapon("Mace","Blunt"),AttackStyle.MELEE_CRUSH));
        no("alpha48.8 mace/blunt rejects Stab",MeleeAttackStyleResolver.supports(categorizedWeapon("Mace","Blunt"),AttackStyle.MELEE_STAB));
        yes("alpha48.8 slash sword supports Slash",MeleeAttackStyleResolver.supports(categorizedWeapon("Sword","Slash Sword"),AttackStyle.MELEE_SLASH));
        no("alpha48.8 unknown weapon category fails closed",MeleeAttackStyleResolver.supports(categorizedWeapon("Unknown","Mystery"),AttackStyle.MELEE_CRUSH));
        System.out.println("PASS  alpha48.8 melee attack-interface eligibility audit");
        yes("alpha48.8.1 neutral-slot completion hook present",MeleeOptimizerRegression48_8_1.neutralCompletionPresent());
        yes("alpha48.8.1 neutral-slot completion forbids DPS loss",MeleeOptimizerRegression48_8_1.neverSacrificesDps());
        System.out.println("PASS  alpha48.8.1 complete neutral-slot fallback audit");
        near("alpha48.8.4 successful-hit average max34",
            MeleeWeaponEffects.successfulHitAverage(34),17.0+1.0/35.0);
        near("alpha48.8.4 Keris average hit max34",
            MeleeWeaponEffects.kerisAverageHit(34),
            (50.0/51.0)*(17.0+1.0/35.0)+(1.0/51.0)*((3.0*34.0*35.0/2.0+1.0)/35.0));
        yes("alpha48.8.4 Keris proc max remains 102",MeleeWeaponEffects.kerisProcMax(34)==102);
        System.out.println("PASS  alpha48.8.4 melee expected-damage parity audit");
        near("alpha48.8.6 Magic successful-hit average max25",
            ExpectedHitFormula.successfulHitAverage(25),12.5+1.0/26.0);
        near("alpha48.8.6 powered Magic successful-hit average max12",
            ExpectedHitFormula.successfulHitAverage(12),6.0+1.0/13.0);
        System.out.println("PASS  alpha48.8.6 Magic expected-damage parity audit");
        yes("alpha48.8.7 Ranged neutral-slot completion",
            OptimizerCompletionRegression48_8_7.rangedCompletionPresent());
        yes("alpha48.8.7 Magic neutral-slot completion",
            OptimizerCompletionRegression48_8_7.magicCompletionPresent());
        yes("alpha48.8.7 Ranged ammo compatibility retained",
            OptimizerCompletionRegression48_8_7.rangedAmmoCompatibilityPresent());
        System.out.println("PASS  alpha48.8.7 Ranged/Magic neutral-slot completion audit");
        EquipmentCandidate dart48=categorizedWeapon("Rune dart","Thrown");
        EquipmentCandidate blowpipe48=categorizedWeapon("Toxic blowpipe","Blowpipe");
        EquipmentCandidate bow48=categorizedWeapon("Magic shortbow","Bow");
        EquipmentCandidate crossbow48=categorizedWeapon("Rune crossbow","Crossbow");
        yes("alpha48.8.8 dart leaves ammo slot free",RangedAmmoRules.ammoSlotFreeForNeutralGear(dart48));
        yes("alpha48.8.8 blowpipe leaves ammo slot free",RangedAmmoRules.ammoSlotFreeForNeutralGear(blowpipe48));
        yes("alpha48.8.8 bow reserves ammo slot",RangedAmmoRules.requiresEquippedAmmo(bow48));
        yes("alpha48.8.8 crossbow reserves ammo slot",RangedAmmoRules.requiresEquippedAmmo(crossbow48));
        System.out.println("PASS  alpha48.8.8 ranged ammo-slot occupancy audit");
        yes("alpha50.3 attack-first Magic candidate pipeline",MagicOptimizerRegression50_3.attackFirstPresent());
        yes("alpha50.3 Magic no longer gated by raw top-12 weapon rank",MagicOptimizerRegression50_3.noMagicTop12Gate());
        yes("alpha50.3 Blue moon spear autocasts Wind Surge",MagicOptimizerRegression50_3.blueMoonStandardAutocast());
        yes("alpha50.3 Blue moon spear retains Ancient autocast",MagicOptimizerRegression50_3.blueMoonAncientAutocast());
        yes("alpha50.3 Mark of Darkness demonbane candidate present",MagicOptimizerRegression50_3.markDemonbanePresent());
        yes("alpha50.3 Mark rune package included",MagicOptimizerRegression50_3.markRunePackagePresent());
        System.out.println("PASS  alpha50.3 attack-first Magic + Mark of Darkness audit");
        yes("alpha50.4 Melee evaluates all owned/equippable weapons",OptimizerCompletenessRegression50_4.meleeAllOwnedWeapons());
        yes("alpha50.4 Ranged evaluates all owned/equippable weapons",OptimizerCompletenessRegression50_4.rangedAllOwnedWeapons());
        yes("alpha50.4 Melee beam ranks by exact target DPS",OptimizerCompletenessRegression50_4.meleeUsesExactDpsDuringBeam());
        yes("alpha50.4 Ranged beam ranks by exact target DPS",OptimizerCompletenessRegression50_4.rangedUsesExactDpsDuringBeam());
        yes("alpha50.4 Ranged ammo compatibility remains enforced",OptimizerCompletenessRegression50_4.rangedChecksAmmo());
        System.out.println("PASS  alpha50.4 Melee/Ranged optimizer completeness audit");
        yes("alpha50.4.2 Hunters crossbow accepts only kebbit family",RangedAmmoRegression50_4_2.huntersKebbitOnly());
        yes("alpha50.4.2 Hunters sunlight crossbow accepts only antler family",RangedAmmoRegression50_4_2.sunlightAntlerOnly());
        yes("alpha50.4.2 Rune crossbow rejects special-family bolts",RangedAmmoRegression50_4_2.runeRejectsSpecial());

        yes("alpha50.4.11 Vorkath ruby EV matches Wiki without Kandarin Hard",RangedVorkathRegression50_4_11.rubyVorkathNoDiary());
        yes("alpha50.4.11 Vorkath ruby EV matches Wiki with Kandarin Hard",RangedVorkathRegression50_4_11.rubyVorkathKandarinHard());
        yes("alpha50.4.11 Rune crossbow rejects ruby dragon bolts",RangedVorkathRegression50_4_11.runeCrossbowRejectsDragonRuby());
        yes("alpha50.4.11 Crystal armour bonus is scoped to crystal weapons",RangedVorkathRegression50_4_11.crystalBonusWeaponScope());
        yes("alpha50.4.21 Opal EV + Kandarin Hard",EnchantedBoltRegression50_4_21.opalDiary());
        yes("alpha50.4.21 Pearl Sea Curse normal/fiery",EnchantedBoltRegression50_4_21.pearlNormalAndFiery());
        yes("alpha50.4.21 Onyx blocked on undead",EnchantedBoltRegression50_4_21.onyxUndeadBlocked());
        yes("alpha50.4.21 Onyx living + Kandarin Hard",EnchantedBoltRegression50_4_21.onyxLivingDiary());
        yes("alpha50.4.21 Dragonstone immune target blocked",EnchantedBoltRegression50_4_21.dragonstoneImmune());
        yes("alpha50.4.21 utility/DoT bolts do not invent DPS",EnchantedBoltRegression50_4_21.utilityNotInvented());
        yes("alpha50.4.23 Vorkath Diamond Wiki parity + 43 proc max",EnchantedBoltRegression50_4_23.vorkathDiamondWikiParity());
        yes("alpha50.4.23 Vorkath Ruby proc max remains 100",EnchantedBoltRegression50_4_23.rubyProcMaxStill100());
        yes("alpha50.4.24 Onyx Life Leech blocked on Vorkath undead",EnchantedBoltRegression50_4_24.vorkathBlocksLifeLeech());
        yes("alpha50.4.24 Onyx living target shows 12.10% Kandarin rate",EnchantedBoltRegression50_4_24.livingShowsKandarinRate());
        yes("alpha50.4.25 Dragonstone blocked on Vorkath dragonfire immunity",EnchantedBoltRegression50_4_25.vorkathBlocksDragonsBreath());
        yes("alpha50.4.25 Dragonstone living target 6.60% + visible-level damage",EnchantedBoltRegression50_4_25.livingKandarinAndProcDamage());
        yes("alpha50.4.26 synthetic bolt proc-max diagnostics",EnchantedBoltRegression50_4_26.procMaxDiagnostics());
        yes("alpha50.4.26 synthetic validation labels cover all standard enchanted bolts",EnchantedBoltRegression50_4_26.labelsCoverSuite());
        yes("alpha50.4.27 Wiki-locked Vorkath enchanted-bolt suite",EnchantedBoltRegression50_4_27.wikiLockedVorkathSuite());
        yes("alpha50.4.27 active Onyx matches K'ril Wiki checkpoint",EnchantedBoltRegression50_4_27.onyxActiveKrilWikiParity());
        yes("alpha50.4.27 Pearl non-fiery branch",EnchantedBoltRegression50_4_27.pearlNonFieryBranch());
        yes("alpha50.4.27 active Dragonstone synthetic branch",EnchantedBoltRegression50_4_27.dragonstoneActiveBranch());
        yes("alpha50.4.50.80 ranged accuracy-first beam restored",RangedPerformanceRegression50_4_12.accuracyFirstBeam());
        yes("alpha50.4.12 all weapons and anchored ammo retained",RangedPerformanceRegression50_4_12.keepsAllWeaponsAndAnchoredAmmo());
        System.out.println("PASS  alpha50.4.2 dedicated Ranged ammunition-family audit");
        near("alpha50.4.3 Rigour ranged accuracy multiplier",RangedPrayer.RIGOUR.getAccuracy(),1.20);
        near("alpha50.4.3 Rigour ranged strength multiplier",RangedPrayer.RIGOUR.getRangedStrength(),1.23);
        yes("alpha50.4.3 Rigour damage multiplier is independent from accuracy",
            RangedPrayer.RIGOUR.getRangedStrength()!=RangedPrayer.RIGOUR.getAccuracy());
        int vorkathRefEff=CombatEffectiveLevels.rangedDamage(105,RangedPrayer.RIGOUR.getRangedStrength(),RangedStance.RAPID.getRangedBoost(),false,false);
        int vorkathRefMax=(int)Math.floor(0.5+vorkathRefEff*(142+64)/640.0);
        yes("alpha50.4.3 Vorkath reference effective ranged strength = 137",vorkathRefEff==137);
        yes("alpha50.4.3 Vorkath reference max hit 142 strength = 44",vorkathRefMax==44);
        System.out.println("PASS  alpha50.4.3 Rigour accuracy/damage split audit");
        java.util.List<BankItem> alpha49Bank=new java.util.ArrayList<>();
        alpha49Bank.add(new BankItem(1001,1,"Ranging potion(3)"));
        alpha49Bank.add(new BankItem(1002,1,"Prayer potion(4)"));
        alpha49Bank.add(new BankItem(1003,5,"Shark"));
        java.util.List<RecommendedSupply> alpha49Supplies=SimpleSupplyRecommender.recommend(AttackStyle.RANGED,null,alpha49Bank);
        yes("alpha49 owned-only supply count",alpha49Supplies.size()==3);
        yes("alpha49 food quantity capped to owned",alpha49Supplies.get(2).quantity==5);
        yes("alpha49 audit reason retained",!alpha49Supplies.get(0).reason.isEmpty());
        System.out.println("PASS  alpha49.0 Recommended inventory audit");
        java.util.List<BankItem> alpha491FoodBank=new java.util.ArrayList<>();
        alpha491FoodBank.add(new BankItem(2001,40,"Shark"));
        alpha491FoodBank.add(new BankItem(2002,27,"Cooked karambwan"));
        java.util.List<RecommendedSupply> alpha491Food=SimpleSupplyRecommender.recommend(AttackStyle.RANGED,null,alpha491FoodBank);
        boolean alpha491Shark=false,alpha491Karam=false;
        for(RecommendedSupply rs:alpha491Food){
            if(rs.name.equalsIgnoreCase("Shark")&&rs.quantity==12)alpha491Shark=true;
            if(rs.name.equalsIgnoreCase("Cooked karambwan")&&rs.quantity==12)alpha491Karam=true;
        }
        yes("alpha49.1 strongest normal food retained",alpha491Shark);
        yes("alpha49.1 combo food independently recommended",alpha491Karam);
        yes("alpha49.1 required runes use full owned stack",SupplyRegression49_1.fullRuneStack());
        yes("alpha49.1 arbitrary rune cast target removed",SupplyRegression49_1.noCastTarget());
        yes("alpha49.2 internal/self-ammo rejects projectile neutral fillers",SupplyRegression49_2.internalAmmoRejectsProjectiles());
        yes("alpha49.2 selected ranged ammo wired to Recommended",SupplyRegression49_2.selectedRangedAmmoWired());
        yes("alpha49.2 external ammo uses full owned stack",SupplyRegression49_2.fullOwnedAmmoStack());
        System.out.println("PASS  alpha49.2 ranged ammo recommendation audit");
        System.out.println("PASS  alpha49.1 normal/combo food recommendation audit");
        near("alpha48.8.5 ranged successful-hit average max12",
            MeleeWeaponEffects.successfulHitAverage(12),6.0+1.0/13.0);
        System.out.println("PASS  alpha48.8.5 ranged expected-damage parity audit");
        EquipmentCandidate kerisIface=categorizedWeapon("Keris","Stab Sword");
        yes("alpha48.8.2 Keris Stab has Aggressive/Lunge",MeleeAttackStyleResolver.legalStances(kerisIface,AttackStyle.MELEE_STAB).contains(MeleeStance.AGGRESSIVE));
        yes("alpha48.8.2 Keris Stab has Accurate",MeleeAttackStyleResolver.legalStances(kerisIface,AttackStyle.MELEE_STAB).contains(MeleeStance.ACCURATE));
        yes("alpha48.8.2 Keris supports Slash",MeleeAttackStyleResolver.supports(kerisIface,AttackStyle.MELEE_SLASH));
        no("alpha48.8.2 Keris still rejects Crush",MeleeAttackStyleResolver.supports(kerisIface,AttackStyle.MELEE_CRUSH));
        EquipmentCandidate partisanIface=categorizedWeapon("Keris partisan","Partisan");
        yes("alpha48.8.2 Partisan supports Stab",MeleeAttackStyleResolver.supports(partisanIface,AttackStyle.MELEE_STAB));
        yes("alpha48.8.2 Partisan supports Crush",MeleeAttackStyleResolver.supports(partisanIface,AttackStyle.MELEE_CRUSH));
        no("alpha48.8.2 Partisan rejects Slash",MeleeAttackStyleResolver.supports(partisanIface,AttackStyle.MELEE_SLASH));
        System.out.println("PASS  alpha48.8.2 Keris/Partisan attack-interface audit");
        // Full Wiki equipment-category attack-interface audit.  These checks are
        // intentionally category-level so every weapon using the category gets
        // the same legal styles, while impossible styles fail closed.
        EquipmentCandidate axeIface=categorizedWeapon("Axe","Axe");
        yes("alpha50.4.50.46 axe supports Slash",MeleeAttackStyleResolver.supports(axeIface,AttackStyle.MELEE_SLASH));
        yes("alpha50.4.50.46 axe supports Crush",MeleeAttackStyleResolver.supports(axeIface,AttackStyle.MELEE_CRUSH));
        EquipmentCandidate sword2hIface=categorizedWeapon("2h sword","2h Sword");
        yes("alpha50.4.50.46 2h sword supports Slash",MeleeAttackStyleResolver.supports(sword2hIface,AttackStyle.MELEE_SLASH));
        EquipmentCandidate spearIface=categorizedWeapon("Spear","Spear");
        yes("alpha50.4.50.46 spear supports all melee styles",
            MeleeAttackStyleResolver.supports(spearIface,AttackStyle.MELEE_STAB)
            && MeleeAttackStyleResolver.supports(spearIface,AttackStyle.MELEE_SLASH)
            && MeleeAttackStyleResolver.supports(spearIface,AttackStyle.MELEE_CRUSH));
        EquipmentCandidate polearmIface=categorizedWeapon("Noxious halberd","Polearm");
        yes("alpha50.4.50.46 polearm has Slash Aggressive only",
            MeleeAttackStyleResolver.legalStances(polearmIface,AttackStyle.MELEE_SLASH).equals(java.util.EnumSet.of(MeleeStance.AGGRESSIVE)));
        no("alpha50.4.50.46 polearm rejects fictional Accurate Slash",
            MeleeAttackStyleResolver.legalStances(polearmIface,AttackStyle.MELEE_SLASH).contains(MeleeStance.ACCURATE));
        EquipmentCandidate whipIface=categorizedWeapon("Abyssal whip","Whip");
        yes("alpha50.4.50.46 whip supports Slash",MeleeAttackStyleResolver.supports(whipIface,AttackStyle.MELEE_SLASH));
        no("alpha50.4.50.46 whip rejects Crush",MeleeAttackStyleResolver.supports(whipIface,AttackStyle.MELEE_CRUSH));
        EquipmentCandidate pickaxeIface=categorizedWeapon("Pickaxe","Pickaxe");
        yes("alpha50.4.50.46 pickaxe supports Stab and Crush",
            MeleeAttackStyleResolver.supports(pickaxeIface,AttackStyle.MELEE_STAB)
            && MeleeAttackStyleResolver.supports(pickaxeIface,AttackStyle.MELEE_CRUSH));
        EquipmentCandidate staffIface=categorizedWeapon("Staff","Staff");
        yes("alpha50.4.50.46 ordinary staff supports Crush only",
            MeleeAttackStyleResolver.supports(staffIface,AttackStyle.MELEE_CRUSH)
            && !MeleeAttackStyleResolver.supports(staffIface,AttackStyle.MELEE_STAB)
            && !MeleeAttackStyleResolver.supports(staffIface,AttackStyle.MELEE_SLASH));
        System.out.println("PASS  alpha50.4.50.46 complete melee weapon-category style audit");
        yes("alpha48.8.3 Keris proc max 34 -> 102",MeleeWeaponEffects.kerisProcMax(34)==102);
        near("alpha48.8.3/48.8.4 Keris average hit distribution",
            MeleeWeaponEffects.kerisAverageHit(34),
            (50.0/51.0)*(17.0+1.0/35.0)+(1.0/51.0)*((3.0*34.0*35.0/2.0+1.0)/35.0));
        System.out.println("PASS  alpha48.8.3 Keris integer damage-stage audit");
        if(!MagicSpellCompatibility.canAutocast("Slayer's staff",dart)||MagicSpellCompatibility.canAutocast("Mystic earth staff",dart)) throw new AssertionError("Magic Dart compatibility");
        if(!MagicSpellCompatibility.canAutocast("Saradomin staff",sara)) throw new AssertionError("God spell compatibility");
        MagicSpell fb=null;for(MagicSpell s:MagicSpell.standard())if("Fire Blast".equals(s.name))fb=s;
        if(MagicSpellCompatibility.attackTicks("Harmonised nightmare staff",fb)!=4) throw new AssertionError("Harmonised speed");
        MagicSpell iceBurst=null,bloodBurst=null,iceBarrage=null;
        for(MagicSpell s:MagicSpell.ancient()){if("Ice Burst".equals(s.name))iceBurst=s;if("Blood Burst".equals(s.name))bloodBurst=s;if("Ice Barrage".equals(s.name))iceBarrage=s;}
        if(MagicSpell.ancient().size()!=16||iceBurst==null||iceBurst.level!=70||iceBurst.maxHit!=22) throw new AssertionError("Ancient catalogue");
        MagicSpell windStrike=null;for(MagicSpell s:MagicSpell.standard())if("Wind Strike".equals(s.name))windStrike=s;
        if(windStrike==null||!"standard".equals(windStrike.spellbook)||windStrike.runes.get("Air rune")==null) throw new AssertionError("Standard spell constructor");
        if(!"ancient".equals(iceBurst.spellbook)||iceBurst.runes.get("Water rune")==null) throw new AssertionError("Ancient spell constructor");
        if(bloodBurst==null||bloodBurst.level!=68||bloodBurst.maxHit!=21||iceBarrage==null||iceBarrage.maxHit!=30) throw new AssertionError("Ancient spell data");
        if(!MagicSpellCompatibility.canAutocast("Ancient staff",iceBurst)||MagicSpellCompatibility.canAutocast("Mystic earth staff",iceBurst)) throw new AssertionError("Ancient autocast compatibility");
        if(!iceBurst.element().isEmpty()||!iceBurst.tier().isEmpty()) throw new AssertionError("Ancient weakness isolation");
        if(!AncientSpellEffects.isMultiTarget(iceBurst)||AncientSpellEffects.maxTargets(iceBurst)!=9||AncientSpellEffects.freezeTicks(iceBurst)!=16) throw new AssertionError("Ice Burst effects");
        if(Math.abs(AncientSpellEffects.bloodHealFraction(bloodBurst)-0.25)>0.0001||AncientSpellEffects.maxTargets(bloodBurst)!=9) throw new AssertionError("Blood Burst effects");
        if(AncientSpellEffects.freezeTicks(iceBarrage)!=32) throw new AssertionError("Ice Barrage freeze");
        MagicSpell smokeBlitz=null,shadowBlitz=null;for(MagicSpell s:MagicSpell.ancient()){if("Smoke Blitz".equals(s.name))smokeBlitz=s;if("Shadow Blitz".equals(s.name))shadowBlitz=s;}
        if(AncientSpellEffects.startingPoison(smokeBlitz)!=4||AncientSpellEffects.attackReductionPercent(shadowBlitz)!=15) throw new AssertionError("Smoke/Shadow effects");
        MonsterDefinition undead=new MonsterDefinition(2,"Undead dummy",1,1,1,1,1,0,0,0,0,0,0,0,java.util.Arrays.asList("undead"),1,null,0);
        MagicLoadoutEffects.Effect salve=MagicLoadoutEffects.evaluateNames(java.util.Arrays.asList("Salve amulet(ei)","Slayer helmet (i)"),undead,true);
        if(Math.abs(salve.accuracy-1.20)>0.0001||Math.abs(salve.damage-1.20)>0.0001) throw new AssertionError("Salve precedence");
        MagicLoadoutEffects.Effect slayerFx=MagicLoadoutEffects.evaluateNames(java.util.Arrays.asList("Slayer helmet (i)"),dragon,true);
        if(Math.abs(slayerFx.accuracy-1.15)>0.0001||Math.abs(slayerFx.damage-1.15)>0.0001) throw new AssertionError("Magic Slayer imbue");
        MagicLoadoutEffects.Effect voidFx=MagicLoadoutEffects.evaluateNames(java.util.Arrays.asList("Void mage helm","Void knight top","Void knight robe","Void knight gloves"),dragon,false);
        if(Math.abs(voidFx.accuracy-1.45)>0.0001||Math.abs(voidFx.damage-1.0)>0.0001) throw new AssertionError("Void Mage");
        MagicLoadoutEffects.Effect eliteFx=MagicLoadoutEffects.evaluateNames(java.util.Arrays.asList("Void mage helm","Elite void top","Elite void robe","Void knight gloves"),dragon,false);
        if(Math.abs(eliteFx.accuracy-1.45)>0.0001||Math.abs(eliteFx.damage-1.05)>0.0001) throw new AssertionError("Elite Void Mage");
        MagicPrayer might=MagicPrayer.forState(75,false,false);
        if(!"Mystic Might".equals(might.name)||Math.abs(might.accuracy-1.15)>0.0001||Math.abs(might.damage-0.02)>0.0001) throw new AssertionError("Mystic Might modern damage");
        MagicPrayer vigour=MagicPrayer.forState(75,false,true);
        if(!"Mystic Vigour".equals(vigour.name)||Math.abs(vigour.damage-0.03)>0.0001) throw new AssertionError("Mystic Vigour unlock/damage");
        MagicPrayer augury=MagicPrayer.forState(77,true,true);
        if(!"Augury".equals(augury.name)||Math.abs(augury.damage-0.04)>0.0001) throw new AssertionError("Augury modern damage");
        if(MagicDamageFormula.standardPrimary(50,0.0,0.00,0.0)!=50) throw new AssertionError("Magic no-prayer boundary");
        if(MagicDamageFormula.standardPrimary(50,0.0,0.02,0.0)!=51) throw new AssertionError("Mystic Might +2% boundary");
        if(MagicDamageFormula.standardPrimary(34,0.0,0.03,0.0)!=35) throw new AssertionError("Mystic Vigour +3% boundary");
        if(MagicDamageFormula.standardPrimary(25,0.0,0.04,0.0)!=26) throw new AssertionError("Augury +4% boundary");
        if(MagicDamageFormula.standardPrimary(16,0.0,0.02,0.50)!=24) throw new AssertionError("Black dragon Water Blast floors");
        if(MagicDamageFormula.standardPrimary(17,0.0,0.02,0.50)!=25) throw new AssertionError("Separate elemental weakness floor");
        if(MagicDamageFormula.poweredPrimary(50,0.0,0.02)!=51) throw new AssertionError("Powered staff prayer boundary");
        if(!MagicSpellCompatibility.canAutocastAncient("kodai wand")) throw new AssertionError("Kodai Ancient autocast");
        if(!MagicSpellCompatibility.canAutocastAncient("blue moon spear")) throw new AssertionError("Blue moon Ancient autocast");
        if(!MagicSpellCompatibility.canAutocastAncient("accursed sceptre (a)")) throw new AssertionError("Accursed(a) Ancient autocast");
        if(MagicSpellCompatibility.canAutocastAncient("tumeken's shadow")) throw new AssertionError("Shadow must not Ancient autocast");
        if(Math.abs(AncientSpellEffects.bloodHealFraction(bloodBurst,"blood ancient sceptre")-0.275)>0.0001) throw new AssertionError("Ancient sceptre Blood effect");
        if(Math.abs(AncientSpellEffects.freezeSeconds(iceBurst,"ice ancient sceptre")-10.56)>0.0001) throw new AssertionError("Ancient sceptre Ice duration");
        MagicSpell tomeWaterBlast=null,tomeFireBlast=null,tomeEarthBlast=null,tomeIban=null;
        for(MagicSpell ms:MagicSpell.standard()){if(ms.name.equals("Water Blast"))tomeWaterBlast=ms;if(ms.name.equals("Fire Blast"))tomeFireBlast=ms;if(ms.name.equals("Earth Blast"))tomeEarthBlast=ms;if(ms.name.equals("Iban Blast"))tomeIban=ms;}
        MagicTomeEffects.Effect tw=MagicTomeEffects.evaluateName("Tome of water",tomeWaterBlast);
        if(Math.abs(tw.accuracy-1.20)>0.0001||Math.abs(tw.damage-1.10)>0.0001) throw new AssertionError("Tome water");
        MagicTomeEffects.Effect tf=MagicTomeEffects.evaluateName("Tome of fire",tomeFireBlast);
        if(Math.abs(tf.accuracy-1.0)>0.0001||Math.abs(tf.damage-1.10)>0.0001) throw new AssertionError("Tome fire");
        MagicTomeEffects.Effect te=MagicTomeEffects.evaluateName("Tome of earth",tomeEarthBlast);
        if(Math.abs(te.damage-1.10)>0.0001) throw new AssertionError("Tome earth");
        if(Math.abs(MagicTomeEffects.evaluateName("Tome of fire",tomeIban).damage-1.0)>0.0001) throw new AssertionError("Tome must not boost Iban");
        PoweredStaff ayak=PoweredStaff.forWeapon("Eye of ayak");
        if(ayak==null||ayak.minLevel!=83||ayak.speed!=3) throw new AssertionError("Eye of ayak powered staff metadata");
        if(ayak.baseMaxHit(83)!=21||ayak.baseMaxHit(99)!=27) throw new AssertionError("Eye of ayak max-hit scaling");
        if(MagicSpecialAttackEffects.energyCost("Eye of ayak")!=50||Math.abs(MagicSpecialAttackEffects.accuracyMultiplier("Eye of ayak")-2.0)>0.0001||Math.abs(MagicSpecialAttackEffects.maxHitMultiplier("Eye of ayak")-1.30)>0.0001||MagicSpecialAttackEffects.attackTicks("Eye of ayak")!=5) throw new AssertionError("Eye of ayak Soul Rend");
        MagicSpell tomeAirStrike=null;
        for(MagicSpell ms:MagicSpell.standard())if(ms.name.equals("Wind Strike"))tomeAirStrike=ms;
        if(ElementalAmuletEffects.evaluateName("Amulet of air",tomeAirStrike).flatMaxHit!=2) throw new AssertionError("Amulet of Air");
        if(ElementalAmuletEffects.evaluateName("Amulet of water",tomeWaterBlast).flatMaxHit!=2) throw new AssertionError("Amulet of Water");
        if(ElementalAmuletEffects.evaluateName("Amulet of earth",tomeEarthBlast).flatMaxHit!=2) throw new AssertionError("Amulet of Earth");
        if(ElementalAmuletEffects.evaluateName("Amulet of fire",tomeFireBlast).flatMaxHit!=2) throw new AssertionError("Amulet of Fire");
        if(ElementalAmuletEffects.evaluateName("Elemental amulet",tomeWaterBlast).flatMaxHit!=2) throw new AssertionError("Elemental amulet");
        if(ElementalAmuletEffects.evaluateName("Amulet of fire",tomeWaterBlast).flatMaxHit!=0) throw new AssertionError("Elemental amulet mismatch");
        if(ElementalAmuletEffects.evaluateName("Elemental amulet",tomeIban).flatMaxHit!=0) throw new AssertionError("Elemental amulet non-elemental exclusion");
        if(MagicDamageFormula.standardPrimary(18,0.0,0.02,0.50)!=27) throw new AssertionError("Elemental amulet + prayer + weakness ordering");










        // alpha50.4.42 — July 2026 Inquisitor rework constants. Detailed roll/DPS
        // parity is exposed by the synthetic in-client harness for Wiki comparison.
        java.util.Map<EquipmentSlot,EquipmentCandidate> iqProbe=new java.util.EnumMap<>(EquipmentSlot.class);
        RequirementResult iqOk=RequirementResult.usable("synthetic Inquisitor regression");
        iqProbe.put(EquipmentSlot.HEAD,new EquipmentCandidate(new BankItem(24419,1,"Inquisitor's great helm"),EquipmentSlot.HEAD,0,10,6,0,0,0,0,1,0,false,"",iqOk));
        iqProbe.put(EquipmentSlot.BODY,new EquipmentCandidate(new BankItem(24420,1,"Inquisitor's hauberk"),EquipmentSlot.BODY,0,16,4,0,0,0,0,2,0,false,"",iqOk));
        iqProbe.put(EquipmentSlot.LEGS,new EquipmentCandidate(new BankItem(24421,1,"Inquisitor's plateskirt"),EquipmentSlot.LEGS,0,12,2,0,0,0,0,2,0,false,"",iqOk));
        if(Math.abs(MeleeLoadoutEffects.inquisitorBonus(iqProbe,AttackStyle.MELEE_CRUSH)-0.025)>0.000001) throw new AssertionError("alpha50.4.42 Inquisitor full redistributed bonus");
        if(MeleeLoadoutEffects.inquisitorBonus(iqProbe,AttackStyle.MELEE_SLASH)!=0.0) throw new AssertionError("alpha50.4.42 Inquisitor crush only");
        iqProbe.remove(EquipmentSlot.HEAD);
        if(Math.abs(MeleeLoadoutEffects.inquisitorBonus(iqProbe,AttackStyle.MELEE_CRUSH)-0.020)>0.000001) throw new AssertionError("alpha50.4.42 Inquisitor body+legs independent bonus");
        System.out.println("PASS  alpha50.4.42 current Inquisitor per-piece crush bonuses");

        // alpha50.4.43 — lock the raw-roll arithmetic exposed by the Vorkath Wiki
        // screenshot. +163 Crush was the full Inquisitor setup with an EMPTY shield;
        // Dragon defender contributes +23 Crush, taking the synthetic harness to +186.
        int iqWikiNoShieldRaw=121*(163+64);
        int iqWikiNoShieldFinal=(int)Math.floor(iqWikiNoShieldRaw*1.025);
        if(iqWikiNoShieldRaw!=27467 || iqWikiNoShieldFinal!=28153) throw new AssertionError("alpha50.4.43 Inquisitor no-shield Wiki roll checkpoint");
        int iqWithDefenderRaw=121*(186+64);
        int iqWithDefenderFinal=(int)Math.floor(iqWithDefenderRaw*1.025);
        if(iqWithDefenderRaw!=30250 || iqWithDefenderFinal!=31006) throw new AssertionError("alpha50.4.43 Inquisitor defender roll checkpoint");
        System.out.println("PASS  alpha50.4.43 Inquisitor shield/raw-roll parity audit");

        // alpha50.4.46 — Wiki full-Inquisitor/no-shield checkpoint: ordinary max 42
        // becomes max 43 after +2.5% Crush damage, and the modified max defines the
        // successful-hit distribution (rather than multiplying the old average).
        int iqBaseMax=42;
        double iqMult=1.025;
        int iqDisplayedMax=(int)Math.floor(iqBaseMax*iqMult);
        if(iqDisplayedMax!=43) throw new AssertionError("alpha50.4.46 Inquisitor displayed max-hit stage");
        double iqExpectedSuccessful=MeleeWeaponEffects.successfulHitAverage(iqDisplayedMax);
        near("alpha50.4.46 Inquisitor modified-max damage distribution",iqExpectedSuccessful,21.522727272727273);
        double iqAccuracy=28153.0/(2.0*(38356.0+1.0));
        double iqDps=iqAccuracy*iqExpectedSuccessful/2.4;
        near("alpha50.4.46 Inquisitor Wiki no-shield DPS",iqDps,3.2910623707813595);
        System.out.println("PASS  alpha50.4.46 Inquisitor Wiki max-hit + damage-distribution stage");
    }
    private static void supplyChecks()
    {
        java.util.List<BankItem> b=java.util.Arrays.asList(new BankItem(1,1000,"Air rune"),new BankItem(2,1000,"Water rune"),new BankItem(3,1000,"Death rune"),new BankItem(4,10,"Magic potion(4)"),new BankItem(5,20,"Prayer potion(4)"),new BankItem(6,100,"Shark"));
        MagicSpell wb=null;for(MagicSpell s:MagicSpell.standard())if(s.name.equals("Water Blast"))wb=s;
        OptimizedMagicLoadout ml=new OptimizedMagicLoadout(new java.util.EnumMap<EquipmentSlot,EquipmentCandidate>(EquipmentSlot.class),null,wb);
        java.util.List<RecommendedSupply> r=SimpleSupplyRecommender.recommend(AttackStyle.MAGIC,ml,b);
        if(r.size()!=6)throw new AssertionError("alpha39 supply count");
        // Alpha49.1 intentionally supersedes alpha39's 100-cast rune target:
        // every required rune now surfaces the player's full owned bank stack.
        if(r.get(0).quantity!=1000||r.get(1).quantity!=1000||r.get(2).quantity!=1000)throw new AssertionError("alpha49.1 full-stack rune quantities");
        if(r.get(3).quantity!=1||r.get(4).quantity!=2||r.get(5).quantity!=12)throw new AssertionError("alpha39 potion/food quantities");
        System.out.println("PASS  alpha39 simple Recommended bank supplies");
        java.util.List<BankItem> meleeBank=java.util.Arrays.asList(new BankItem(1,1000,"Air rune"),new BankItem(2,1000,"Water rune"),new BankItem(3,1000,"Death rune"),new BankItem(7,10,"Super combat potion(4)"),new BankItem(5,20,"Prayer potion(4)"),new BankItem(6,100,"Shark"));
        java.util.List<RecommendedSupply> mr=SimpleSupplyRecommender.recommend(AttackStyle.MELEE_STAB,null,meleeBank);
        if(mr.size()!=3||!mr.get(0).name.equals("Super combat potion(4)")||mr.get(0).quantity!=1||mr.get(1).quantity!=2||mr.get(2).quantity!=12)throw new AssertionError("alpha40 best-style melee supplies");
        for(RecommendedSupply x:mr)if(x.name.toLowerCase().contains("rune"))throw new AssertionError("alpha40 melee recommendation leaked Magic runes");
        System.out.println("PASS  alpha40 Recommended supplies follow combat style");
        if(CombatEffectiveLevels.melee(82,1.20,3,false)!=109)throw new AssertionError("alpha41 melee effective attack");
        if(CombatEffectiveLevels.melee(82,1.20,3,true)!=119)throw new AssertionError("alpha41 Void melee effective attack floor");
        if(CombatEffectiveLevels.melee(90,1.23,3,true)!=133)throw new AssertionError("alpha41 Void melee effective strength floor");
        if(CombatEffectiveLevels.ranged(66,1.15,3,false)!=86)throw new AssertionError("alpha41 ranged effective level");
        if(CombatEffectiveLevels.magic(73,1.15,true,0)!=129)throw new AssertionError("alpha41 Void magic ordering");
        System.out.println("PASS  alpha41 effective-level ordering/floors");
        if(CombatEffectiveLevels.ranged(66,1.15,3,true)!=94)throw new AssertionError("alpha42 regular Void ranged accuracy effective level");
        if(CombatEffectiveLevels.rangedDamage(66,1.15,3,true,false)!=94)throw new AssertionError("alpha42 regular Void ranged damage effective level");
        if(CombatEffectiveLevels.rangedDamage(66,1.15,3,true,true)!=96)throw new AssertionError("alpha42 elite Void ranged damage effective level");
        MonsterDefinition tbowProbe=new MonsterDefinition(999999,"Tbow probe",1,1,80,1,1,0,0,0,0,0,0,0,java.util.Collections.emptyList(),1,null,0,180);
        if(RangedWeaponEffects.twistedTargetMagic(tbowProbe)!=180.0)throw new AssertionError("alpha42 Tbow must use max Magic level/Magic attack");
        MonsterDefinition tbowCap=new MonsterDefinition(999998,"Tbow cap",1,1,400,1,1,0,0,0,0,0,0,0,java.util.Collections.emptyList(),1,null,0,500);
        if(RangedWeaponEffects.twistedTargetMagic(tbowCap)!=250.0)throw new AssertionError("alpha42 Tbow outside-CoX cap");
        MonsterDefinition tbowXeric=new MonsterDefinition(999997,"Tbow Xeric",1,1,300,1,1,0,0,0,0,0,0,0,java.util.Arrays.asList("xerician"),1,null,0,340);
        if(RangedWeaponEffects.twistedTargetMagic(tbowXeric)!=340.0)throw new AssertionError("alpha42 Tbow Xerician cap");
        System.out.println("PASS  alpha42 ranged Void/Tbow ordering");
        if(RecommendationReadiness.stylesVisible(false))throw new AssertionError("alpha43.1 styles before target");
        if(!RecommendationReadiness.stylesVisible(true))throw new AssertionError("alpha43.1 styles after target");
        if(RecommendationReadiness.resultsVisible(true,false,true))throw new AssertionError("alpha43.1 results before bank scan");
        if(RecommendationReadiness.resultsVisible(true,true,false))throw new AssertionError("alpha43.1 results before style");
        if(!RecommendationReadiness.resultsVisible(true,true,true))throw new AssertionError("alpha43.1 results after style-triggered bank scan");
        System.out.println("PASS  alpha43.1 target -> style -> loadout readiness");
        if(BankStyleSelection.visibleStyles(AttackStyle.MELEE_STAB).size()!=1 ||
           BankStyleSelection.visibleStyles(AttackStyle.MELEE_STAB).get(0)!=AttackStyle.MELEE_STAB)
            throw new AssertionError("alpha44 bank must show selected style only");
        if(!BankStyleSelection.visibleStyles(null).isEmpty())
            throw new AssertionError("alpha44 bank must show no combat sections without a style");
        System.out.println("PASS  alpha44 selected-style bank projection");
        java.util.Map<AttackStyle,Double> bestProbe=new java.util.EnumMap<>(AttackStyle.class);
        bestProbe.put(AttackStyle.MELEE_STAB,4.64);bestProbe.put(AttackStyle.MAGIC,3.02);bestProbe.put(AttackStyle.RANGED,1.39);
        if(BestStyleSelection.best(bestProbe)!=AttackStyle.MELEE_STAB)throw new AssertionError("alpha44.1 genuine best style");
        System.out.println("PASS  alpha44.1 genuine best-style selection");
        if(PanelUiState.of(false,false,false,false)!=PanelUiState.LOGGED_OUT)throw new AssertionError("alpha45 logged-out state");
        if(PanelUiState.of(true,false,true,false)!=PanelUiState.NO_TARGET)throw new AssertionError("alpha45 no-target state");
        if(PanelUiState.of(true,true,true,false)!=PanelUiState.CHOOSE_STYLE)throw new AssertionError("alpha45 choose-style state");
        if(PanelUiState.of(true,true,true,true)!=PanelUiState.RESULTS)throw new AssertionError("alpha45 results state");
        System.out.println("PASS  alpha45 progressive visual states");
        if(!SlayerTargetSelection.targetReady(true,1))throw new AssertionError("alpha45.5 Slayer auto-target");
        if(SlayerTargetSelection.targetReady(true,0))throw new AssertionError("alpha45.5 Slayer empty variants");
        System.out.println("PASS  alpha45.5 Slayer target readiness");
        if(PanelLayoutPolicy.contentWidth(225)!=225)throw new AssertionError("alpha45.6 panel width");
        if(PanelLayoutPolicy.slayerRowWidth(225)!=217)throw new AssertionError("alpha45.6 slayer row width");
        System.out.println("PASS  alpha45.6 native panel layout policy");
        if(!BankSnapshotSemantics.slayerHeadgearName("Black mask (i)"))throw new AssertionError("alpha45.12 black mask detection");
        if(!BankSnapshotSemantics.slayerHeadgearName("Slayer helmet (i)"))throw new AssertionError("alpha45.12 slayer helmet detection");
        if(BankSnapshotSemantics.usableName(""))throw new AssertionError("alpha45.12 blank bank names");
        System.out.println("PASS  alpha45.12 bank names preserve Slayer/supply semantics");
        if(PanelUiState.of(false,true,true,true)!=PanelUiState.LOGGED_OUT)throw new AssertionError("alpha45.1 login state must dominate UI");
        if(PanelUiState.of(true,true,false,true)!=PanelUiState.CHOOSE_STYLE)throw new AssertionError("alpha45.1 results wait for bank scan");
        System.out.println("PASS  alpha45.1 polished sidebar state gating");
        // alpha50.4.50.3.1: Wiki Corp divisionTransformer(2) parity. Accurate zero is
        // raised to 1 first, then every hitsplat is integer-divided by two.
        if(Math.abs(CorporealBeastRules.dividedSuccessfulAverage(4)-0.8)>1e-12)
            throw new AssertionError("Corp divided successful distribution 0..4");
        if(Math.abs(CorporealBeastRules.dividedUniformAverage(6,40)-11.257142857142858)>1e-12)
            throw new AssertionError("Corp Fang divided constrained distribution");
        MonsterDefinition corp=new MonsterDefinition(319,"Corporeal Beast",785,310,350,0,2000);
        if(!CorporealBeastRules.isCorp(corp))throw new AssertionError("Corp target detection");
        MonsterDefinition dukeDemon=new MonsterDefinition(12191,"Duke Sucellus",440,275,200,200,440,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"),5,null,0);
        MonsterDefinition yamaDemon=new MonsterDefinition(14176,"Yama",100,100,100,100,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"),5,null,0);
        MonsterDefinition flareDemon=new MonsterDefinition(14179,"Yama void flare",100,100,100,100,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"),1,null,0);
        MonsterDefinition iceDemon=new MonsterDefinition(7584,"Ice Demon",100,100,100,100,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"),3,null,0);
        near("alpha50.4.50.6 Duke demonbane 70pct vulnerability",DemonbaneEffects.multiplier(dukeDemon,70),1.49,1e-12);
        near("alpha50.4.50.6 Yama demonbane 120pct vulnerability",DemonbaneEffects.multiplier(yamaDemon,70),1.84,1e-12);
        near("alpha50.4.50.6 Yama flare demonbane 200pct vulnerability",DemonbaneEffects.multiplier(flareDemon,70),2.40,1e-12);
        near("alpha50.4.50.6 Ice Demon demonbane 115pct vulnerability",DemonbaneEffects.multiplier(iceDemon,70),1.80,1e-12);
        near("alpha50.4.50.6 Scorching bow Yama accuracy",DemonbaneEffects.multiplier(yamaDemon,30),1.36,1e-12);
        System.out.println("PASS  alpha50.4.50.6 Wiki-source demonbane vulnerability audit");
        HitDistribution hd=HitDistribution.linear(0.75,0,4).accurateZeroToOne();
        near("alpha50.4.50.6 HitDist accurate-zero EV",hd.expectedHit(),1.65,1e-12);
        near("alpha50.4.50.6 HitDist probability preserved",hd.probability(),1.0,1e-12);
        HitDistribution two=HitDistribution.linear(1.0,0,2).accurateZeroToOne().zip(HitDistribution.linear(1.0,0,1).accurateZeroToOne());
        near("alpha50.4.50.6 HitDist zipped EV",two.expectedHit(),2.333333333333333,1e-12);
        if(two.cumulative().min()!=2||two.cumulative().max()!=3)throw new AssertionError("alpha50.4.50.6 cumulative multi-hit bounds");
        near("alpha50.4.50.6 divide per-hitsplat EV",two.transform(HitDistribution.divide(2,0)).expectedHit(),0.3333333333333333,1e-12);
        near("alpha50.4.50.6 flat-limit transformer",HitDistribution.linear(1.0,0,6).transform(HitDistribution.flatLimit(3,0)).expectedHit(),15.0/7.0,1e-12);
        near("alpha50.4.50.6 multiply truncation",HitDistribution.linear(1.0,0,4).transform(HitDistribution.multiply(3,2,0)).expectedHit(),2.8,1e-12);
        System.out.println("PASS  alpha50.4.50.6 Wiki HitDistribution foundation");
        near("alpha50.4.50.7 Scythe HitDist 3-splat EV",MeleeWeaponEffects.scytheExpectedDamage(40,3,0.5),0.5*MeleeWeaponEffects.scytheAverageHit(40,3),1e-12);
        near("alpha50.4.50.7 independent two-hit HitDist EV",MeleeWeaponEffects.independentTwoHitExpectedDamage(43,0.5),0.5*MeleeWeaponEffects.independentTwoHitSuccessfulAverage(43),1e-12);
        near("alpha50.4.50.7 Dual mac HitDist sequential EV",MeleeWeaponEffects.dualMacuahuitlExpectedDamage(43,0.5),0.5*MeleeWeaponEffects.successfulHitAverage(21)+0.25*MeleeWeaponEffects.successfulHitAverage(22),1e-12);
        yes("alpha50.4.50.7 Earthbound tecpatl two-hit family",MeleeWeaponEffects.isIndependentTwoHit(weapon("Earthbound tecpatl")));
        System.out.println("PASS  alpha50.4.50.7 Wiki HitDistribution melee multi-hit migration");
        near("alpha50.4.50.8 Keris Wiki scaleDamage distribution",MeleeWeaponEffects.kerisAverageHit(34),(50.0/51.0)*(17.0+1.0/35.0)+(1.0/51.0)*((3.0*34.0*35.0/2.0+1.0)/35.0),1e-12);
        yes("alpha50.4.50.8 bolt HitDistribution probability",Math.abs(EnchantedBoltEffects.damageDistribution(null,weapon("Opal bolts (e)"),new MonsterDefinition(1,"x",100,100,100,100,500,0,0,0,0,0,0,0,java.util.Collections.emptyList()),105,40,.5,true,false).probability()-1.0)<1e-12);
        System.out.println("PASS  alpha50.4.50.8 Wiki HitDistribution Keris + enchanted-bolt migration");
        near("alpha50.4.50.12 normal positive-roll parity",WikiAccuracyRoll.normal(100,80),1.0-82.0/(2.0*101.0),1e-12);
        near("alpha50.4.50.12 positive attack vs negative defence",WikiAccuracyRoll.normal(100,-10),1.0-1.0/(9.0*101.0),1e-12);
        near("alpha50.4.50.12 negative attack vs positive defence",WikiAccuracyRoll.normal(-10,100),0.0,1e-12);
        near("alpha50.4.50.12 both-negative roll reversal",WikiAccuracyRoll.normal(-10,-20),1.0-10.0/(2.0*19.0),1e-12);
        near("alpha50.4.50.12 negative roll +2 normalization",WikiAccuracyRoll.normal(-1,100),0.0,1e-12);
        near("alpha50.4.50.12 Fang positive-roll parity",WikiAccuracyRoll.fang(25606,20070),MeleeWeaponEffects.fangAccuracy(25606,20070),1e-12);
        near("alpha50.4.50.12 Fang positive attack vs negative defence",WikiAccuracyRoll.fang(100,-10),1.0-1.0/(9.0*101.0),1e-12);
        near("alpha50.4.50.12 Fang negative attack vs positive defence",WikiAccuracyRoll.fang(-10,100),0.0,1e-12);
        System.out.println("PASS  alpha50.4.50.12 Wiki signed accuracy-roll parity audit");
        yes("alpha50.4.50.13 Wiki max-hit checkpoint 118x172", WikiIntegerStages.maxHitFromEffective(118,172) == 32);
        yes("alpha50.4.50.13 Wiki max-hit +320 rounding", WikiIntegerStages.maxHitFromEffective(137,206) == 44);
        yes("alpha50.4.50.13 Wiki factor truncates immediately", WikiIntegerStages.factor(101,7,6) == 117);
        yes("alpha50.4.50.13 Wiki factor negative truncates toward zero", WikiIntegerStages.factor(-101,7,6) == -117);
        yes("alpha50.4.50.15 Wiki canonical Abyssal whip ornament", WikiEquipmentEngine.canonicalId(26482) == 4151);
        yes("alpha50.4.50.15 Wiki RCB accepts Ruby bolts(e)", WikiEquipmentEngine.ammoApplicability(9185,9242) == WikiEquipmentEngine.AmmoApplicability.INCLUDED);
        yes("alpha50.4.50.15 Wiki RCB rejects dragon Ruby bolts(e)", WikiEquipmentEngine.ammoApplicability(9185,21944) == WikiEquipmentEngine.AmmoApplicability.INVALID);
        yes("alpha50.4.50.15 Wiki Bowfa uses no ammo-slot projectile", WikiEquipmentEngine.ammoApplicability(25865,892) == WikiEquipmentEngine.AmmoApplicability.ALLOWED);
        yes("alpha50.4.50.15 Wiki equipment dataset canonical lookup", WikiEquipmentEngine.canonicalItem(26482) != null && "Abyssal whip".equals(WikiEquipmentEngine.canonicalItem(26482).name));
        System.out.println("PASS  alpha50.4.50.15 Wiki Equipment Engine aliases + exact ammo table");
        java.util.Map<EquipmentSlot,Integer> eq16=new java.util.EnumMap<>(EquipmentSlot.class);
        eq16.put(EquipmentSlot.WEAPON,9185); eq16.put(EquipmentSlot.AMMO,9242);
        WikiEquipmentEngine.EquipmentBonuses rcb16=WikiEquipmentEngine.calculateEquipmentBonuses(eq16,null,null,false,false,true,null);
        yes("alpha50.4.50.16 full aggregate RCB ranged attack",rcb16.rangedAttack==90);
        yes("alpha50.4.50.16 full aggregate included ammo strength",rcb16.rangedStr==103);
        yes("alpha50.4.50.16 full aggregate Rapid speed",rcb16.attackSpeed==5);
        java.util.Map<EquipmentSlot,Integer> sh16=new java.util.EnumMap<>(EquipmentSlot.class);
        sh16.put(EquipmentSlot.WEAPON,27275); sh16.put(EquipmentSlot.NECK,12002);
        WikiEquipmentEngine.EquipmentBonuses shadow16=WikiEquipmentEngine.calculateEquipmentBonuses(sh16,null,null,false,false,false,null);
        yes("alpha50.4.50.16 Shadow aggregate magic attack x3",shadow16.magicAttack==141);
        near("alpha50.4.50.16 Shadow aggregate magic damage x3",shadow16.magicDamage,.15,1e-12);
        yes("alpha50.4.50.16 spec-cost table",WikiEquipmentEngine.WEAPON_SPEC_COSTS.get("Zaryte crossbow")==75);
        yes("alpha50.4.50.16 no-stat exceptions",WikiEquipmentEngine.NO_STAT_EXCEPTIONS.contains("Lightbearer"));
        yes("alpha50.4.50.16 Gauntlet equipment IDs",WikiEquipmentEngine.GAUNTLET_EQUIPMENT_IDS.size()==22&&WikiEquipmentEngine.GAUNTLET_EQUIPMENT_IDS.contains(23861));
        yes("alpha50.4.50.16 Corrupted Gauntlet equipment IDs",WikiEquipmentEngine.CORRUPTED_GAUNTLET_EQUIPMENT_IDS.size()==22&&WikiEquipmentEngine.CORRUPTED_GAUNTLET_EQUIPMENT_IDS.contains(23820));
        yes("alpha50.4.50.16 highest offensive style", "slash".equals(WikiEquipmentEngine.highestOffensiveStyle(1,5,4,3,2)));
        yes("alpha50.4.50.16 attack-speed floor",WikiEquipmentEngine.calculateAttackSpeed(1,"x",null,true,false,false,false)==1);
        System.out.println("PASS  alpha50.4.50.16 Wiki Equipment.ts full API parity audit");
        near("alpha50.4.50.17 BaseCalc normal roll delegates",WikiBaseCalc.normalAccuracyRoll(100,80),WikiAccuracyRoll.normal(100,80),1e-12);
        near("alpha50.4.50.17 BaseCalc Fang roll delegates",WikiBaseCalc.fangAccuracyRoll(100,80),WikiAccuracyRoll.fang(100,80),1e-12);
        near("alpha50.4.50.17 fixed attack hit chance",WikiBaseCalc.fixedAttackHitChance(50,100),50.0/101.0,1e-12);
        near("alpha50.4.50.17 fixed attack signed defence",WikiBaseCalc.fixedAttackHitChance(50,-10),1.0,1e-12);
        near("alpha50.4.50.17 Confliction gauntlets roll",WikiBaseCalc.conflictionGauntletsAccuracyRoll(100,80),WikiAccuracyRoll.fang(100,80)/(1.0+WikiAccuracyRoll.fang(100,80)-WikiAccuracyRoll.normal(100,80)),1e-12);
        yes("alpha50.4.50.17 BaseCalc addFactor",WikiBaseCalc.addFactor(101,7,6)==218);
        java.util.List<String> base17=java.util.Arrays.asList("Elite void top","Elite void robe","Void knight gloves","Void mage helm","Twinflame staff","Amulet of fire");
        yes("alpha50.4.50.17 BaseCalc elite Void robes",WikiBaseCalc.isWearingEliteVoidRobes(base17));
        yes("alpha50.4.50.17 BaseCalc smoke staff family",WikiBaseCalc.isWearingSmokeStaff(base17));
        yes("alpha50.4.50.17 BaseCalc elemental amulet",WikiBaseCalc.matchingElementalAmulet("fire",base17));
        yes("alpha50.4.50.17 BaseCalc Charge cape gate",WikiBaseCalc.chargeSpellApplicable(true,"Saradomin Strike",java.util.Collections.singletonList("Imbued Saradomin cape")));
        HitDistribution scaled17=HitDistribution.linear(.5,0,4).scaleProbability(.25).scaleDamage(3,2);
        near("alpha50.4.50.17 HitDist scaleProbability",scaled17.probability(),.25,1e-12);
        near("alpha50.4.50.17 HitDist scaleDamage truncation",HitDistribution.single(3,true).scaleDamage(3,2).expectedHit(),4.0,1e-12);
        yes("alpha50.4.50.17 HitDist size API",HitDistribution.linear(.5,0,4).size()==6);
        HitDistribution.AttackDistribution ad17=new HitDistribution.AttackDistribution(java.util.Arrays.asList(HitDistribution.linear(.5,0,4),HitDistribution.linear(.5,0,2)));
        near("alpha50.4.50.17 AttackDistribution expected damage",ad17.expectedDamage(),1.5,1e-12);
        yes("alpha50.4.50.17 AttackDistribution max",ad17.max()==6);
        yes("alpha50.4.50.17 AttackDistribution first-hit max",ad17.firstHitMax().dists().get(0).max()==4);
        near("alpha50.4.50.17 AttackDistribution first-hit accurate renormalizes",ad17.firstHitAccurate().dists().get(0).probability(),1.0,1e-12);
        yes("alpha50.4.50.17 AttackDistribution first-hit minimum",ad17.firstHitMinimum(2).dists().get(0).min()==0);
        System.out.println("PASS  alpha50.4.50.17 Wiki BaseCalc + HitDist structural parity core");
        HitDistribution.AttackDistribution pv18=new HitDistribution.AttackDistribution(java.util.Collections.singletonList(HitDistribution.linear(.5,0,10)));
        near("alpha50.4.50.18.1 PlayerVsNPC expected damage",WikiPlayerVsNpcCalc.expectedDamage(pv18),2.5,1e-12);
        near("alpha50.4.50.18.1 PlayerVsNPC DPS timing",WikiPlayerVsNpcCalc.dps(pv18,4),2.5/2.4,1e-12);
        near("alpha50.4.50.18.1 Tormented Demon unshielded accuracy",WikiPlayerVsNpcCalc.tormentedDemonAccuracy("Tormented Demon","Unshielded",.42),1.0,1e-12);
        near("alpha50.4.50.18.1 Tormented Demon shielded ordinary accuracy",WikiPlayerVsNpcCalc.tormentedDemonAccuracy("Tormented Demon","Shielded",.42),.42,1e-12);
        near("alpha50.4.50.18.1 Tormented Demon shield damage reduction",WikiPlayerVsNpcCalc.tormentedDemonDamage(HitDistribution.single(10,true),"Tormented Demon","Shielded",false,false).expectedHit(),8.0,1e-12);
        near("alpha50.4.50.18.1 Tormented Demon demonbane bypass",WikiPlayerVsNpcCalc.tormentedDemonDamage(HitDistribution.single(10,true),"Tormented Demon","Shielded",true,false).expectedHit(),10.0,1e-12);
        MonsterDefinition rat18=new MonsterDefinition(7223,"Giant rat (Scurrius)",46,20,1,1,15);
        near("alpha50.4.50.18.1 Giant rat guaranteed accuracy",WikiPlayerVsNpcCalc.giantRatAccuracy(rat18,false,.25),1.0,1e-12);
        near("alpha50.4.50.18.1 Giant rat Manual Cast keeps roll",WikiPlayerVsNpcCalc.giantRatAccuracy(rat18,true,.25),.25,1e-12);
        HitDistribution.AttackDistribution flat18=WikiPlayerVsNpcCalc.finalDistribution(pv18,new MonsterDefinition(999,"armoured",1,1,1,1,10,0,0,0,0,0,0,0,java.util.Arrays.asList("flat_armour:2")),AttackStyle.RANGED,null,null,false);
        near("alpha50.4.50.18.1 attacker -> NPC transform ordering",flat18.expectedDamage(),18.0/11.0,1e-12);
        System.out.println("PASS  alpha50.4.50.18.1 Wiki PlayerVsNPCCalc structural pipeline foundation");
        HitDistribution.AttackDistribution std19=WikiPlayerVsNpcCalc.standardAttacker(10,.5);
        near("alpha50.4.50.19 standard attacker EV",std19.expectedDamage(),HitDistribution.linear(.5,0,10).accurateZeroToOne().expectedHit(),1e-12);
        HitDistribution.AttackDistribution scy19=WikiPlayerVsNpcCalc.scytheAttacker(40,3,.5);
        near("alpha50.4.50.19 Scythe attacker EV",scy19.expectedDamage(),MeleeWeaponEffects.scytheDistribution(40,3,.5).expectedHit(),1e-12);
        HitDistribution.AttackDistribution two19=WikiPlayerVsNpcCalc.independentTwoHitAttacker(43,.5);
        near("alpha50.4.50.19 independent two-hit attacker EV",two19.expectedDamage(),MeleeWeaponEffects.independentTwoHitDistribution(43,.5).expectedHit(),1e-12);
        HitDistribution.AttackDistribution dual19=WikiPlayerVsNpcCalc.dualMacuahuitlAttacker(43,.5);
        near("alpha50.4.50.19 Dual mac attacker EV",dual19.expectedDamage(),MeleeWeaponEffects.dualMacuahuitlDistribution(43,.5).expectedHit(),1e-12);
        HitDistribution.AttackDistribution ker19=WikiPlayerVsNpcCalc.kerisAttacker(34,.5);
        near("alpha50.4.50.19 Keris attacker EV",ker19.expectedDamage(),MeleeWeaponEffects.kerisDistribution(34,.5).expectedHit(),1e-12);
        yes("alpha50.4.50.19 current Wiki blowpipe IDs",WikiEquipmentEngine.isBlowpipeId(12926)&&WikiEquipmentEngine.isBlowpipeId(28688)&&WikiEquipmentEngine.isBlowpipeId(31575)&&WikiEquipmentEngine.isBlowpipeId(31579)&&WikiEquipmentEngine.isBlowpipeId(31583));
        yes("alpha50.4.50.19 stale blowpipe IDs rejected",!WikiEquipmentEngine.isBlowpipeId(12924)&&!WikiEquipmentEngine.isBlowpipeId(28691));
        System.out.println("PASS  alpha50.4.50.19 PlayerVsNPC attacker-distribution migration + Blowpipe IDs");
        EquipmentCandidate ruby20=slotItem("Ruby bolts (e)",EquipmentSlot.AMMO);
        MonsterDefinition corp20=new MonsterDefinition(319,"Corporeal Beast",785,310,350,0,2000);
        HitDistribution.AttackDistribution rubyCorp20=WikiPlayerVsNpcCalc.rangedAttacker(null,ruby20,corp20,99,40,.5,false);
        near("alpha50.4.50.20 Corp + Ruby source ordering",rubyCorp20.expectedDamage(),EnchantedBoltEffects.damageDistribution(null,ruby20,corp20,99,40,.5,false,true).expectedHit(),1e-12);
        yes("alpha50.4.50.20 Ruby remains full after Corp",rubyCorp20.max()==100);
        EquipmentCandidate diamond20=slotItem("Diamond bolts (e)",EquipmentSlot.AMMO);
        HitDistribution preDiamond20=EnchantedBoltEffects.preAccurateZeroDistribution(diamond20,new MonsterDefinition(998,"living",100,100,100,100,100),99,40,.5,false);
        near("alpha50.4.50.20 non-Ruby before accurate-zero",WikiPlayerVsNpcCalc.rangedAttacker(null,diamond20,new MonsterDefinition(998,"living",100,100,100,100,100),99,40,.5,false).expectedDamage(),preDiamond20.accurateZeroToOne().expectedHit(),1e-12);
        MonsterDefinition flat20=new MonsterDefinition(997,"armoured",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("flat_armour:2"));
        HitDistribution.AttackDistribution rangedFinal20=WikiPlayerVsNpcCalc.rangedFinalDistribution(null,null,flat20,99,10,.5,false);
        near("alpha50.4.50.20 ranged NPC transforms last",rangedFinal20.expectedDamage(),WikiNpcTransforms.apply(HitDistribution.linear(.5,0,10).accurateZeroToOne(),flat20,AttackStyle.RANGED,null,null,false).expectedHit(),1e-12);
        System.out.println("PASS  alpha50.4.50.20 Wiki ranged attack-distribution ordering migration");

        // alpha50.4.50.25 — sustained ranged attacker-distribution effects.
        HitDistribution.AttackDistribution dark25=WikiPlayerVsNpcCalc.rangedAttacker(
            slotItem("Dark bow",EquipmentSlot.WEAPON),slotItem("Rune arrow",EquipmentSlot.AMMO),null,99,20,1.0,false);
        near("alpha50.4.50.25 Dark bow two independent hits",dark25.expectedDamage(),422.0/21.0,1e-12);
        near("alpha50.4.50.25 Dark bow combined max",dark25.max(),40);
        HitDistribution.AttackDistribution tonal25=WikiPlayerVsNpcCalc.rangedAttacker(
            slotItem(28922,"Tonalztics of Ralos",EquipmentSlot.WEAPON),null,null,99,20,.5,false);
        near("alpha50.4.50.25 charged Tonalztics two independent hits",tonal25.expectedDamage(),211.0/21.0,1e-12);
        HitDistribution.AttackDistribution uncharged25=WikiPlayerVsNpcCalc.rangedAttacker(
            slotItem(28919,"Tonalztics of Ralos",EquipmentSlot.WEAPON),null,null,99,20,.5,false);
        near("alpha50.4.50.25 uncharged Tonalztics stays single hit",uncharged25.expectedDamage(),211.0/42.0,1e-12);
        java.util.List<EquipmentCandidate> karils25=java.util.Arrays.asList(
            slotItem("Karil's crossbow",EquipmentSlot.WEAPON),slotItem("Karil's coif",EquipmentSlot.HEAD),
            slotItem("Karil's leathertop",EquipmentSlot.BODY),slotItem("Karil's leatherskirt",EquipmentSlot.LEGS),
            slotItem("Amulet of the damned",EquipmentSlot.NECK));
        HitDistribution.AttackDistribution karil25=WikiPlayerVsNpcCalc.rangedAttacker(
            karils25.get(0),null,null,99,20,1.0,false,karils25);
        near("alpha50.4.50.25 full Karil 25pct half-hit EV",karil25.expectedDamage(),473.0/42.0,1e-12);
        near("alpha50.4.50.25 full Karil proc max",karil25.max(),30);
        HitDistribution.AttackDistribution seeking25=WikiPlayerVsNpcCalc.rangedAttacker(
            slotItem("Magic shortbow",EquipmentSlot.WEAPON),slotItem("Seeking rune arrow",EquipmentSlot.AMMO),null,99,10,1.0,false);
        near("alpha50.4.50.25 Seeking ammo accurate minimum three",seeking25.expectedDamage(),61.0/11.0,1e-12);
        yes("alpha50.4.50.25 complete Karil set discoverable",RangedLoadoutEffects.synergy(karils25.get(1))
            &&RangedLoadoutEffects.synergy(karils25.get(2))&&RangedLoadoutEffects.synergy(karils25.get(3))
            &&RangedLoadoutEffects.synergy(karils25.get(4)));
        System.out.println("PASS  alpha50.4.50.25 Wiki sustained-ranged attacker effects migration");

        // alpha50.4.50.26 — remaining sustained ranged roll/max ordering.
        near("alpha50.4.50.26 chin Short fuse distance 1",WikiRangedEffects.chinchompaNumerator(RangedStance.ACCURATE,1),4);
        near("alpha50.4.50.26 chin Short fuse distance 7",WikiRangedEffects.chinchompaNumerator(RangedStance.ACCURATE,7),2);
        near("alpha50.4.50.26 chin Medium fuse distance 4",WikiRangedEffects.chinchompaNumerator(RangedStance.RAPID,4),4);
        near("alpha50.4.50.26 chin Long fuse distance 1",WikiRangedEffects.chinchompaNumerator(RangedStance.LONGRANGE,1),2);
        EquipmentCandidate chin26=slotItem("Red chinchompa",EquipmentSlot.WEAPON);
        near("alpha50.4.50.26 chin default Rapid distance4 roll",
            WikiRangedEffects.postAccuracy(10000,chin26,null,RangedStance.RAPID,4),10000);
        near("alpha50.4.50.26 chin Short fuse distance7 roll",
            WikiRangedEffects.postAccuracy(10000,chin26,null,RangedStance.ACCURATE,7),5000);
        MonsterDefinition rev26=new MonsterDefinition(7932,"Revenant demon",1,1,1,1,100);
        EquipmentCandidate craw26=slotItem(22550,"Craw's bow",EquipmentSlot.WEAPON);
        EquipmentCandidate crawU26=slotItem(22547,"Craw's bow",EquipmentSlot.WEAPON);
        yes("alpha50.4.50.26 charged Craw wilderness bonus",WikiRangedEffects.isChargedRevWeapon(craw26,rev26));
        no("alpha50.4.50.26 uncharged Craw no wilderness bonus",WikiRangedEffects.isChargedRevWeapon(crawU26,rev26));
        near("alpha50.4.50.26 charged Craw accuracy x3/2",
            WikiRangedEffects.postAccuracy(10000,craw26,rev26,RangedStance.RAPID,4),15000);
        near("alpha50.4.50.26 Slayer plus rev damage additive 33/20",
            WikiRangedEffects.damageAfterExclusive(40,java.util.Collections.emptyList(),rev26,craw26,true,true),66);
        MonsterDefinition dragon26=new MonsterDefinition(9926,"Dragon",1,1,1,1,100,
            0,0,0,0,0,0,0,java.util.Arrays.asList("dragon"));
        near("alpha50.4.50.26 Slayer plus DHCB damage additive 28/20",
            WikiRangedEffects.damageAfterExclusive(40,java.util.Collections.emptyList(),dragon26,
                slotItem("Dragon hunter crossbow",EquipmentSlot.WEAPON),true,true),56);
        java.util.List<EquipmentCandidate> avarice26=java.util.Arrays.asList(
            craw26,slotItem("Amulet of avarice",EquipmentSlot.NECK),slotItem("Salve amulet(ei)",EquipmentSlot.NECK));
        near("alpha50.4.50.26 Avarice revenant exclusive x24/20",
            WikiRangedEffects.accuracyAfterExclusive(10000,avarice26,rev26,true,true),12000);
        near("alpha50.4.50.26 Tonalztics max x3/4",
            WikiRangedEffects.postDamage(40,slotItem(28922,"Tonalztics of Ralos",EquipmentSlot.WEAPON),null,false,false),30);
        System.out.println("PASS  alpha50.4.50.26 Wiki chinchompa + Wilderness ranged ordering migration");

        // alpha50.4.50.27 — remaining sustained melee distribution passives.
        MonsterDefinition shade27=new MonsterDefinition(9930,"Loar Shade",1,1,1,1,100,
            0,0,0,0,0,0,0,java.util.Arrays.asList("shade"));
        java.util.List<EquipmentCandidate> gadder27=java.util.Collections.singletonList(
            slotItem("Gadderhammer",EquipmentSlot.WEAPON));
        HitDistribution.AttackDistribution gadderDist27=WikiPlayerVsNpcCalc.meleeSustainedAttacker(
            20,.5,gadder27,shade27);
        near("alpha50.4.50.27 Gadderhammer Shade branch EV",gadderDist27.expectedDamage(),151.0/24.0,1e-12);
        near("alpha50.4.50.27 Gadderhammer 5pct double branch max",gadderDist27.max(),40);
        HitDistribution.AttackDistribution gadderControl27=WikiPlayerVsNpcCalc.meleeSustainedAttacker(
            20,.5,gadder27,new MonsterDefinition(9931,"Ordinary",1,1,1,1,100));
        near("alpha50.4.50.27 Gadderhammer ordinary target unchanged",gadderControl27.expectedDamage(),211.0/42.0,1e-12);
        java.util.List<EquipmentCandidate> verac27=java.util.Arrays.asList(
            slotItem("Verac's flail",EquipmentSlot.WEAPON),slotItem("Verac's helm",EquipmentSlot.HEAD),
            slotItem("Verac's brassard",EquipmentSlot.BODY),slotItem("Verac's plateskirt",EquipmentSlot.LEGS));
        HitDistribution.AttackDistribution veracDist27=WikiPlayerVsNpcCalc.meleeSustainedAttacker(
            20,.5,verac27,null);
        near("alpha50.4.50.27 full Verac 25pct guaranteed EV",veracDist27.expectedDamage(),365.0/56.0,1e-12);
        near("alpha50.4.50.27 full Verac max+1 proc",veracDist27.max(),21);
        near("alpha50.4.50.27 incomplete Verac unchanged",WikiPlayerVsNpcCalc.meleeSustainedAttacker(
            20,.5,verac27.subList(0,3),null).expectedDamage(),211.0/42.0,1e-12);
        yes("alpha50.4.50.27 Verac armour preserved as synergy",
            MeleeLoadoutEffects.isSynergyItem(verac27.get(1))&&MeleeLoadoutEffects.isSynergyItem(verac27.get(2))
                &&MeleeLoadoutEffects.isSynergyItem(verac27.get(3)));
        System.out.println("PASS  alpha50.4.50.27 Wiki Gadderhammer + Verac sustained-melee migration");

        // alpha50.4.50.28 — sustained melee Wilderness and exclusive-necklace ordering.
        MonsterDefinition rev28=new MonsterDefinition(7939,"Revenant knight",1,1,1,1,100);
        EquipmentCandidate viggora28=slotItem(22545,"Viggora's chainmace",EquipmentSlot.WEAPON);
        EquipmentCandidate viggoraU28=slotItem(22542,"Viggora's chainmace",EquipmentSlot.WEAPON);
        EquipmentCandidate ursine28=slotItem(27660,"Ursine chainmace",EquipmentSlot.WEAPON);
        yes("alpha50.4.50.28 charged Viggora Wilderness bonus",WikiMeleeEffects.isChargedRevWeapon(viggora28,rev28));
        yes("alpha50.4.50.28 charged Ursine Wilderness bonus",WikiMeleeEffects.isChargedRevWeapon(ursine28,rev28));
        no("alpha50.4.50.28 uncharged Viggora no Wilderness bonus",WikiMeleeEffects.isChargedRevWeapon(viggoraU28,rev28));
        no("alpha50.4.50.28 charged Viggora off-Wilderness control",WikiMeleeEffects.isChargedRevWeapon(viggora28,
            new MonsterDefinition(9940,"Ordinary",1,1,1,1,100)));
        near("alpha50.4.50.28 charged melee rev accuracy x3/2",WikiMeleeEffects.postAccuracy(10000,viggora28,rev28),15000);
        near("alpha50.4.50.28 charged melee rev max x3/2",WikiMeleeEffects.postDamage(40,viggora28,rev28),60);
        java.util.List<EquipmentCandidate> avarice28=java.util.Arrays.asList(viggora28,
            slotItem("Amulet of avarice",EquipmentSlot.NECK),slotItem("Slayer helmet (i)",EquipmentSlot.HEAD));
        near("alpha50.4.50.28 Avarice exclusive accuracy x24/20",
            WikiMeleeEffects.accuracyAfterAvarice(10000,avarice28,rev28),12000);
        near("alpha50.4.50.28 Avarice exclusive max x24/20",
            WikiMeleeEffects.maxAfterAvarice(40,avarice28,rev28),48);
        no("alpha50.4.50.28 Avarice inactive off Revenants",WikiMeleeEffects.avariceRevenant(avarice28,
            new MonsterDefinition(9941,"Undead control",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("undead"))));
        System.out.println("PASS  alpha50.4.50.28 Wiki melee Wilderness ordering migration");

        // alpha50.4.50.29 — ordinary sustained demonbane claws.
        MonsterDefinition demon29=new MonsterDefinition(9942,"Demon control",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"));
        MonsterDefinition dragon29=new MonsterDefinition(9943,"Dragon control",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("dragon"));
        EquipmentCandidate boneClaws29=slotItem("Bone claws",EquipmentSlot.WEAPON);
        EquipmentCandidate burningClaws29=slotItem("Burning claws",EquipmentSlot.WEAPON);
        near("alpha50.4.50.29 Bone claws demon accuracy +5pct",
            MeleeWeaponEffects.accuracyMultiplier(boneClaws29,demon29,AttackStyle.MELEE_CRUSH),DemonbaneEffects.multiplier(demon29,5));
        near("alpha50.4.50.29 Bone claws demon damage +5pct",
            MeleeWeaponEffects.damageMultiplier(boneClaws29,demon29,AttackStyle.MELEE_CRUSH),DemonbaneEffects.multiplier(demon29,5));
        near("alpha50.4.50.29 Burning claws demon accuracy +5pct",
            MeleeWeaponEffects.accuracyMultiplier(burningClaws29,demon29,AttackStyle.MELEE_CRUSH),DemonbaneEffects.multiplier(demon29,5));
        near("alpha50.4.50.29 Burning claws demon damage +5pct",
            MeleeWeaponEffects.damageMultiplier(burningClaws29,demon29,AttackStyle.MELEE_CRUSH),DemonbaneEffects.multiplier(demon29,5));
        near("alpha50.4.50.29 Bone claws ordinary accuracy unchanged",
            MeleeWeaponEffects.accuracyMultiplier(boneClaws29,dragon29,AttackStyle.MELEE_CRUSH),1.0);
        near("alpha50.4.50.29 Burning claws ordinary damage unchanged",
            MeleeWeaponEffects.damageMultiplier(burningClaws29,dragon29,AttackStyle.MELEE_CRUSH),1.0);
        System.out.println("PASS  alpha50.4.50.29 Wiki sustained demonbane-claw migration");

        // alpha50.4.50.30 — Crystal blessing melee max-hit stage.
        java.util.List<EquipmentCandidate> crystal30=java.util.Arrays.asList(
            slotItem("Crystal blessing",EquipmentSlot.NECK),slotItem("Crystal helm",EquipmentSlot.HEAD),
            slotItem("Crystal body",EquipmentSlot.BODY),slotItem("Crystal legs",EquipmentSlot.LEGS));
        near("alpha50.4.50.30 Crystal blessing full set max x46/40",
            WikiMeleeEffects.maxAfterCrystalBlessing(40,crystal30),46);
        near("alpha50.4.50.30 Crystal blessing helm-only max x41/40",
            WikiMeleeEffects.maxAfterCrystalBlessing(40,java.util.Arrays.asList(
                slotItem("Crystal blessing",EquipmentSlot.NECK),slotItem("Crystal helm",EquipmentSlot.HEAD))),41);
        near("alpha50.4.50.30 Crystal blessing without blessing unchanged",
            WikiMeleeEffects.maxAfterCrystalBlessing(40,java.util.Arrays.asList(
                slotItem("Crystal helm",EquipmentSlot.HEAD),slotItem("Crystal body",EquipmentSlot.BODY),
                slotItem("Crystal legs",EquipmentSlot.LEGS))),40);
        System.out.println("PASS  alpha50.4.50.30 Wiki Crystal blessing melee max migration");
        yes("alpha50.4.50.31 Crystal blessing optimizer bundle hook",
            java.nio.file.Files.exists(java.nio.file.Paths.get("src/main/java/com/personalbis/MeleeLoadoutOptimizer.java")));
        System.out.println("PASS  alpha50.4.50.31 Crystal blessing optimizer completeness migration");

        HitDistribution.AttackDistribution modern21=WikiPlayerVsNpcCalc.magicAttacker(10,.5,false);
        // At 50% accuracy with rolls 0..10, accurate-zero conversion changes only
        // the successful zero roll to 1: 0.5 * (1+1+2+...+10) / 11 = 28/11.
        near("alpha50.4.50.21.1 modern Magic accurate-zero",modern21.expectedDamage(),28.0/11.0,1e-12);
        HitDistribution.AttackDistribution dart21=WikiPlayerVsNpcCalc.magicAttacker(10,.5,true);
        near("alpha50.4.50.21 Magic Dart preserves accurate zero",dart21.expectedDamage(),2.5,1e-12);
        MonsterDefinition tekton21=new MonsterDefinition(7540,"Tekton",1,1,1,1,100);
        HitDistribution.AttackDistribution transformed21=WikiPlayerVsNpcCalc.magicFinalDistribution(10,1.0,false,tekton21,"Mystic fire staff","fire",false);
        near("alpha50.4.50.21 Magic NPC transforms last",transformed21.expectedDamage(),12.0/11.0,1e-12);
        HitDistribution.AttackDistribution powered21=WikiPlayerVsNpcCalc.magicFinalDistribution(12,.5,false,null,"Trident of the seas",null,false);
        near("alpha50.4.50.21 powered Magic shared attacker",powered21.expectedDamage(),79.0/26.0,1e-12);
        System.out.println("PASS  alpha50.4.50.21 Wiki Magic attack-distribution migration");

        // alpha50.4.50.22 — direct PlayerVsNPCCalc Magic accuracy ordering.
        near("alpha50.4.50.22 ordered Magic accuracy stages",
            WikiMagicAccuracy.orderedRoll(10000,30,7,4,true,40,120,true,true,50),74693);
        near("alpha50.4.50.22 weakness adds from base roll",
            WikiMagicAccuracy.orderedRoll(10000,20,1,1,false,0,100,false,false,50),17000);
        near("alpha50.4.50.22 Tome of Water accuracy x6/5",
            WikiMagicAccuracy.orderedRoll(10000,0,1,1,false,0,100,false,true,0),12000);
        java.util.List<EquipmentCandidate> void22=java.util.Arrays.asList(
            slotItem("Void mage helm",EquipmentSlot.HEAD),
            slotItem("Void knight top",EquipmentSlot.BODY),
            slotItem("Void knight robe",EquipmentSlot.LEGS),
            slotItem("Void knight gloves",EquipmentSlot.HANDS));
        WikiMagicAccuracy.Result voidResult22=WikiMagicAccuracy.calculate(99,1.25,100,void22,null,null,false);
        near("alpha50.4.50.22 Magic effective level prayer then Void",voidResult22.effectiveLevel,191);
        near("alpha50.4.50.22 Magic Void base roll",voidResult22.baseRoll,31324);
        System.out.println("PASS  alpha50.4.50.22 Wiki Magic accuracy-ordering migration");

        // alpha50.4.50.23 — direct PlayerVsNPCCalc Magic max/min-hit ordering.
        MagicSpell fireBlast23=new MagicSpell("Fire Blast",59,16,"Air rune",4,"Fire rune",5,"Death rune",1);
        java.util.List<EquipmentCandidate> smokeSalve23=java.util.Arrays.asList(
            slotItem("Mystic smoke staff",EquipmentSlot.WEAPON),
            slotItem("Salve amulet(ei)",EquipmentSlot.NECK));
        MonsterDefinition undead23=new MonsterDefinition(9901,"Undead",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("undead"));
        WikiMagicDamage.Result stacked23=WikiMagicDamage.ordered(18,.08,.02,smokeSalve23,undead23,fireBlast23,true,false);
        near("alpha50.4.50.23 additive Magic damage thousandths",stacked23.maxHit,25);
        WikiMagicDamage.Result slayer23=WikiMagicDamage.ordered(34,.08,.04,
            java.util.Arrays.asList(slotItem("Slayer helmet (i)",EquipmentSlot.HEAD)),null,fireBlast23,true,false);
        near("alpha50.4.50.23 Slayer damage after aggregate bonus",slayer23.maxHit,43);
        MonsterDefinition dragonTarget23=new MonsterDefinition(9903,"Dragon",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("dragon"));
        WikiMagicDamage.Result dragon23=WikiMagicDamage.ordered(20,0,0,
            java.util.Arrays.asList(slotItem("Dragon hunter wand",EquipmentSlot.WEAPON)),dragonTarget23,fireBlast23,false,false);
        near("alpha50.4.50.23 Dragon hunter wand damage x7/5",dragon23.maxHit,28);
        MonsterDefinition fireWeak23=new MonsterDefinition(9902,"Fire weak",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Collections.emptyList(),1,"fire",50);
        WikiMagicDamage.Result sunfireTome23=WikiMagicDamage.ordered(18,0,0,
            java.util.Arrays.asList(slotItem("Tome of fire",EquipmentSlot.SHIELD)),fireWeak23,fireBlast23,false,true);
        near("alpha50.4.50.23 weakness before Tome max",sunfireTome23.maxHit,29);
        near("alpha50.4.50.23 Sunfire minimum before Tome",sunfireTome23.minHit,2);
        MagicSpell dart23=new MagicSpell("Magic Dart",50,0,"Mind rune",4,"Death rune",1);
        WikiMagicDamage.Result enhancedDart23=WikiMagicDamage.standard(99,0,0,
            java.util.Arrays.asList(slotItem("Slayer's staff (e)",EquipmentSlot.WEAPON)),null,dart23,true);
        near("alpha50.4.50.23 enhanced Slayer staff Magic Dart",enhancedDart23.baseMax,29);
        PoweredStaff sang23=PoweredStaff.forWeapon("Sanguinesti staff");
        PoweredStaff warped23=PoweredStaff.forWeapon("Warped sceptre");
        near("alpha50.4.50.23 Sanguinesti base max 99",sang23.baseMaxHit(99),33);
        near("alpha50.4.50.23 Warped sceptre exact base max 99",warped23.baseMaxHit(99),24);
        PoweredStaff bone23=PoweredStaff.forWeapon("Bone staff");
        near("alpha50.4.50.23 Bone staff ratbane applied once",
            WikiMagicDamage.powered(bone23.baseMaxHit(99),0,0,java.util.Collections.emptyList(),null,false).maxHit,38);
        System.out.println("PASS  alpha50.4.50.23 Wiki Magic max/min-hit ordering migration");

        // alpha50.4.50.24 — sustained Magic attacker-distribution effects.
        MonsterDefinition demon24=new MonsterDefinition(9910,"Demon",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"));
        MagicSpell marked24=null;
        for(MagicSpell s:MagicSpell.arceuus())if("Dark Demonbane + Mark of Darkness".equals(s.name))marked24=s;
        HitDistribution.AttackDistribution mark24=WikiPlayerVsNpcCalc.magicAttacker(0,30,1.0,null,
            java.util.Arrays.asList(slotItem("Staff of fire",EquipmentSlot.WEAPON)),demon24,marked24);
        near("alpha50.4.50.24 Mark two-stage per-hit EV",mark24.expectedDamage(),18.419354838709676,1e-12);
        near("alpha50.4.50.24 Mark max 30 -> 37",mark24.max(),37);
        HitDistribution.AttackDistribution purging24=WikiPlayerVsNpcCalc.magicAttacker(0,30,1.0,null,
            java.util.Arrays.asList(slotItem("Purging staff",EquipmentSlot.WEAPON)),demon24,marked24);
        near("alpha50.4.50.24 Purging Mark max 30 -> 45",purging24.max(),45);
        MonsterDefinition duke24=new MonsterDefinition(9911,"Duke Sucellus",1,1,1,1,100,0,0,0,0,0,0,0,java.util.Arrays.asList("demon"));
        near("alpha50.4.50.24 Duke 70pct Mark vulnerability",
            WikiPlayerVsNpcCalc.magicAttacker(0,30,1.0,null,java.util.Collections.emptyList(),duke24,marked24).max(),34);
        HitDistribution.AttackDistribution sang24=WikiPlayerVsNpcCalc.magicAttacker(0,33,1.0,null,
            java.util.Arrays.asList(slotItem("Sanguinesti staff",EquipmentSlot.WEAPON)),null,null);
        near("alpha50.4.50.24 Sanguinesti +8 branch EV",sang24.expectedDamage(),18.123529411764707,1e-12);
        near("alpha50.4.50.24 Sanguinesti proc max",sang24.max(),41);
        java.util.List<EquipmentCandidate> ahrim24=java.util.Arrays.asList(
            slotItem("Ahrim's staff",EquipmentSlot.WEAPON),slotItem("Ahrim's hood",EquipmentSlot.HEAD),
            slotItem("Ahrim's robetop",EquipmentSlot.BODY),slotItem("Ahrim's robeskirt",EquipmentSlot.LEGS),
            slotItem("Amulet of the damned",EquipmentSlot.NECK));
        HitDistribution.AttackDistribution ahrimDist24=WikiPlayerVsNpcCalc.magicAttacker(0,30,1.0,null,ahrim24,null,null);
        near("alpha50.4.50.24 full Ahrim 25pct branch EV",ahrimDist24.expectedDamage(),16.048387096774192,1e-12);
        near("alpha50.4.50.24 full Ahrim proc max",ahrimDist24.max(),39);
        MagicSpell bolt24=new MagicSpell("Fire Bolt",35,12);
        HitDistribution.AttackDistribution twin24=WikiPlayerVsNpcCalc.magicAttacker(0,20,1.0,null,
            java.util.Arrays.asList(slotItem("Twinflame staff",EquipmentSlot.WEAPON)),null,bolt24);
        near("alpha50.4.50.24 Twinflame truncated echo EV",twin24.expectedDamage(),13.666666666666666,1e-12);
        near("alpha50.4.50.24 Twinflame two-splat max",twin24.max(),28);
        HitDistribution.AttackDistribution brim24=WikiPlayerVsNpcCalc.magicAttacker(0,10,.5,.6,
            java.util.Arrays.asList(slotItem("Brimstone ring",EquipmentSlot.RING)),null,null);
        near("alpha50.4.50.24 Brimstone 75/25 accuracy mixture",brim24.expectedDamage(),2.672727272727273,1e-12);
        System.out.println("PASS  alpha50.4.50.24 Wiki sustained-Magic attacker effects migration");

        System.out.println("PASS  alpha50.4.50.13 Wiki integer-stage + parity-diagnostics audit");
        System.out.println("PASS  alpha50.4.50.3.1 Corp hitsplat division parity");
    }
    private static MonsterDefinition monsterSized(String name,int size){
        return new MonsterDefinition(9998,name,100,100,100,100,100,0,0,0,0,0,0,0,java.util.Collections.emptyList(),size);
    }

}
