package com.personalbis;

/**
 * Structural Java port boundary for the OSRS Wiki PlayerVsNPCCalc attack pipeline.
 * PBIS owns loadout discovery; this class owns Wiki ordering once rolls/distributions exist.
 */
public final class WikiPlayerVsNpcCalc {
    private WikiPlayerVsNpcCalc() {}

    /** Wiki getDistributionImpl ordering: attacker distribution, encounter phase overrides, then NPC transforms. */
    public static HitDistribution.AttackDistribution finalDistribution(
        HitDistribution.AttackDistribution attacker,
        MonsterDefinition monster,
        AttackStyle style,
        EquipmentCandidate weapon,
        String spellElement,
        boolean demonbane)
    {
        if (attacker == null || monster == null) return attacker;
        java.util.List<HitDistribution> out = new java.util.ArrayList<>();
        for (HitDistribution d : attacker.dists())
            out.add(WikiNpcTransforms.apply(d, monster, style, weapon, spellElement, demonbane));
        return new HitDistribution.AttackDistribution(out).flatten();
    }

    /** Name-based final transform boundary for Magic calculators, which retain the weapon label rather than candidate. */
    public static HitDistribution.AttackDistribution finalDistributionByWeaponName(
        HitDistribution.AttackDistribution attacker, MonsterDefinition monster, AttackStyle style,
        String weaponName, String spellElement, boolean demonbane)
    {
        if (attacker == null || monster == null) return attacker;
        java.util.List<HitDistribution> out = new java.util.ArrayList<>();
        for (HitDistribution d : attacker.dists())
            out.add(WikiNpcTransforms.applyByWeaponName(d, monster, style, weaponName, spellElement, demonbane));
        return new HitDistribution.AttackDistribution(out).flatten();
    }

    /** Wiki Magic base distribution. Magic Dart preserves accurate zero; modern spells convert it to one. */
    public static HitDistribution.AttackDistribution magicAttacker(int maxHit, double accuracy, boolean magicDart) {
        HitDistribution d = HitDistribution.linear(accuracy, 0, Math.max(0, maxHit));
        if (!magicDart) d = d.accurateZeroToOne();
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(d));
    }

    /** Full sustained-Magic attacker effects in upstream getAttackerDist() order. */
    public static HitDistribution.AttackDistribution magicAttacker(
        int minHit, int maxHit, double accuracy, Double brimstoneAccuracy,
        java.util.Collection<EquipmentCandidate> gear, MonsterDefinition monster, MagicSpell spell)
    {
        HitDistribution d = HitDistribution.linear(accuracy, Math.max(0,minHit), Math.max(minHit,maxHit));
        if (brimstoneAccuracy != null)
        {
            java.util.List<HitDistribution.WeightedHit> mixed = new java.util.ArrayList<>();
            mixed.addAll(d.scaleProbability(.75).hits());
            mixed.addAll(HitDistribution.linear(brimstoneAccuracy,Math.max(0,minHit),Math.max(minHit,maxHit)).scaleProbability(.25).hits());
            d = new HitDistribution(mixed).flatten();
        }

        String weapon = equipmentName(gear,EquipmentSlot.WEAPON);
        if (equalsAny(weapon,"Sanguinesti staff","Holy sanguinesti staff"))
        {
            d = d.transform(h -> new HitDistribution(java.util.Arrays.asList(
                new HitDistribution.WeightedHit(.8,java.util.Collections.singletonList(h)),
                new HitDistribution.WeightedHit(.2,java.util.Collections.singletonList(new HitDistribution.Hitsplat(h.damage+8,h.accurate)))
            )),false);
        }

        boolean marked = spell != null && spell.markOfDarkness && spell.isDemonbane()
            && monster != null && monster.hasAttribute("demon");
        if (marked)
        {
            final int factor = equalsAny(weapon,"Purging staff") ? 50 : 25;
            final int vulnerability = DemonbaneEffects.vulnerability(monster);
            d = d.transform(h -> {
                int first = WikiIntegerStages.factor(h.damage,factor,100);
                int bonus = WikiIntegerStages.factor(first,vulnerability,100);
                return HitDistribution.single(h.damage+bonus,h.accurate);
            });
        }

        if (fullAhrim(gear))
        {
            d = d.transform(h -> new HitDistribution(java.util.Arrays.asList(
                new HitDistribution.WeightedHit(.75,java.util.Collections.singletonList(h)),
                new HitDistribution.WeightedHit(.25,java.util.Collections.singletonList(
                    new HitDistribution.Hitsplat(WikiIntegerStages.factor(h.damage,13,10),h.accurate)))
            )));
        }

        boolean preservesZero = spell != null && "Magic Dart".equalsIgnoreCase(spell.name);
        if (!preservesZero) d = d.accurateZeroToOne();

        if (MagicWeaponEffects.isTwinflame(weapon) && spell != null && "standard".equals(spell.spellbook)
            && ("bolt".equals(spell.tier()) || "blast".equals(spell.tier()) || "wave".equals(spell.tier())))
        {
            d = d.transform(h -> HitDistribution.single(1.0,java.util.Arrays.asList(
                new HitDistribution.Hitsplat(h.damage,h.accurate),
                new HitDistribution.Hitsplat(WikiIntegerStages.factor(h.damage,4,10),h.accurate))));
        }
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(d.flatten()));
    }

    /** Full Magic ordering: spell/powered attacker distribution first, NPC transforms last. */
    public static HitDistribution.AttackDistribution magicFinalDistribution(
        int maxHit, double accuracy, boolean magicDart, MonsterDefinition monster,
        String weaponName, String spellElement, boolean demonbane)
    {
        return finalDistributionByWeaponName(magicAttacker(maxHit, accuracy, magicDart), monster,
            AttackStyle.MAGIC, weaponName, spellElement, demonbane);
    }

    public static HitDistribution.AttackDistribution magicFinalDistribution(
        int minHit, int maxHit, double accuracy, Double brimstoneAccuracy,
        MonsterDefinition monster, java.util.Collection<EquipmentCandidate> gear, MagicSpell spell)
    {
        HitDistribution.AttackDistribution attacker=magicAttacker(minHit,maxHit,accuracy,brimstoneAccuracy,gear,monster,spell);
        return finalDistributionByWeaponName(attacker,monster,AttackStyle.MAGIC,
            equipmentName(gear,EquipmentSlot.WEAPON),spell==null?null:spell.element(),spell!=null&&spell.isDemonbane());
    }

    private static boolean fullAhrim(java.util.Collection<EquipmentCandidate> gear)
    {
        return hasEquipment(gear,"Ahrim's staff") && hasEquipment(gear,"Ahrim's hood")
            && hasEquipment(gear,"Ahrim's robetop") && hasEquipment(gear,"Ahrim's robeskirt")
            && hasEquipment(gear,"Amulet of the damned");
    }

    private static boolean hasEquipment(java.util.Collection<EquipmentCandidate> gear,String name)
    { return !equipmentName(gear,null,name).isEmpty(); }

    private static String equipmentName(java.util.Collection<EquipmentCandidate> gear,EquipmentSlot slot)
    { return equipmentName(gear,slot,null); }

    private static String equipmentName(java.util.Collection<EquipmentCandidate> gear,EquipmentSlot slot,String exact)
    {
        if(gear!=null)for(EquipmentCandidate c:gear)if(c!=null&&c.getItem()!=null
            && (slot==null||c.getSlot()==slot)&&(exact==null||c.getItem().getName().equalsIgnoreCase(exact)))return c.getItem().getName();
        return "";
    }

    private static boolean equalsAny(String value,String... names)
    { if(value!=null)for(String n:names)if(value.equalsIgnoreCase(n))return true;return false; }

    public static double expectedDamage(HitDistribution.AttackDistribution d) {
        return d == null ? 0.0 : d.expectedDamage();
    }

    public static double dps(HitDistribution.AttackDistribution d, int attackTicks) {
        return attackTicks <= 0 ? 0.0 : expectedDamage(d) / (attackTicks * 0.6);
    }


    /** Wiki getAttackerDist standard linear path with modern accurate-zero conversion. */
    public static HitDistribution.AttackDistribution standardAttacker(int maxHit, double accuracy) {
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(
            MeleeWeaponEffects.standardDistribution(maxHit, accuracy)));
    }

    /** Wiki sustained melee distribution branches that must precede accurate-zero and NPC transforms. */
    public static HitDistribution.AttackDistribution meleeSustainedAttacker(int maxHit,double accuracy,
        java.util.Collection<EquipmentCandidate> gear,MonsterDefinition monster) {
        HitDistribution raw=HitDistribution.linear(accuracy,0,Math.max(0,maxHit));
        String weapon=equipmentName(gear,EquipmentSlot.WEAPON);
        HitDistribution d=raw;
        if(equalsAny(weapon,"Gadderhammer")&&monster!=null&&monster.hasAttribute("shade")) {
            java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
            out.addAll(raw.scaleProbability(.95).scaleDamage(5,4).hits());
            out.addAll(raw.scaleProbability(.05).scaleDamage(2).hits());
            d=new HitDistribution(out).flatten();
        }
        if(fullVerac(gear)) {
            java.util.List<HitDistribution.WeightedHit> out=new java.util.ArrayList<>();
            out.addAll(raw.scaleProbability(.75).hits());
            out.addAll(HitDistribution.linear(1.0,1,Math.max(0,maxHit)+1).scaleProbability(.25).hits());
            d=new HitDistribution(out).flatten();
        }
        d=d.accurateZeroToOne();
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(d.flatten()));
    }

    private static boolean fullVerac(java.util.Collection<EquipmentCandidate> gear) {
        return hasEquipment(gear,"Verac's helm")&&hasEquipment(gear,"Verac's brassard")
            &&hasEquipment(gear,"Verac's plateskirt")&&hasEquipment(gear,"Verac's flail");
    }

    /** Wiki Scythe branch: one independent distribution per target-size hitsplat. */
    public static HitDistribution.AttackDistribution scytheAttacker(int baseMax, int hits, double accuracy) {
        java.util.List<HitDistribution> d = new java.util.ArrayList<>();
        d.add(MeleeWeaponEffects.standardDistribution(Math.max(0, baseMax), accuracy));
        if (hits >= 2) d.add(MeleeWeaponEffects.standardDistribution(MeleeWeaponEffects.scytheSecondMax(baseMax), accuracy));
        if (hits >= 3) d.add(MeleeWeaponEffects.standardDistribution(MeleeWeaponEffects.scytheThirdMax(baseMax), accuracy));
        return new HitDistribution.AttackDistribution(d);
    }

    /** Wiki normal two-hit family: both split hits roll independently. */
    public static HitDistribution.AttackDistribution independentTwoHitAttacker(int combinedMax, double accuracy) {
        return new HitDistribution.AttackDistribution(java.util.Arrays.asList(
            MeleeWeaponEffects.standardDistribution(MeleeWeaponEffects.splitFirstMax(combinedMax), accuracy),
            MeleeWeaponEffects.standardDistribution(MeleeWeaponEffects.splitSecondMax(combinedMax), accuracy)));
    }

    /** Dual macuahuitl remains sequential, so its conditional second hit stays in one distribution. */
    public static HitDistribution.AttackDistribution dualMacuahuitlAttacker(int combinedMax, double accuracy) {
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(
            MeleeWeaponEffects.dualMacuahuitlDistribution(combinedMax, accuracy)));
    }

    public static HitDistribution.AttackDistribution fangAttacker(int trueMax, double accuracy) {
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(
            MeleeWeaponEffects.fangDistribution(trueMax, accuracy)));
    }

    public static HitDistribution.AttackDistribution kerisAttacker(int modifiedMax, double accuracy) {
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(
            MeleeWeaponEffects.kerisDistribution(modifiedMax, accuracy)));
    }

    public static HitDistribution.AttackDistribution obsidianAttacker(int baseMax, double accuracy, double multiplier) {
        return new HitDistribution.AttackDistribution(java.util.Collections.singletonList(
            MeleeWeaponEffects.obsidianDistribution(baseMax, accuracy, multiplier)));
    }


    /** Wiki ranged ordering: non-Ruby bolt transforms -> accurate-zero -> Corp -> Ruby. */
    public static HitDistribution.AttackDistribution rangedAttacker(
        EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition monster, int rangedLevel,
        int normalMax, double accuracy, boolean kandarinHard) {
        return rangedAttacker(weapon,ammo,monster,rangedLevel,normalMax,accuracy,kandarinHard,null);
    }

    /** Sustained ranged weapon/set/ammunition effects in upstream getAttackerDist() order. */
    public static HitDistribution.AttackDistribution rangedAttacker(
        EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition monster, int rangedLevel,
        int normalMax, double accuracy, boolean kandarinHard,
        java.util.Collection<EquipmentCandidate> gear) {
        HitDistribution d = EnchantedBoltEffects.preAccurateZeroDistribution(
            ammo, monster, rangedLevel, normalMax, accuracy, kandarinHard);

        String weaponName=weapon==null||weapon.getItem()==null?"":weapon.getItem().getName();
        boolean twoIndependent=equalsAny(weaponName,"Dark bow") || isChargedTonalztics(weapon);
        if (fullKarils(gear))
            d=d.transform(h -> new HitDistribution(java.util.Arrays.asList(
                new HitDistribution.WeightedHit(.75,java.util.Collections.singletonList(h)),
                new HitDistribution.WeightedHit(.25,java.util.Arrays.asList(h,
                    new HitDistribution.Hitsplat(h.damage/2,true)))
            )),false);

        if (ammo != null && ammo.getItem() != null && ammo.getItem().getName().contains("Seeking"))
            d=d.transform(h -> HitDistribution.single(Math.max(h.damage,3),h.accurate),false);
        d = d.accurateZeroToOne();
        if (CorporealBeastRules.isCorp(monster)) d = d.transform(HitDistribution.divide(2, 0)).flatten();
        d = EnchantedBoltEffects.applyRubyAfterCorp(d, ammo, monster, kandarinHard);
        java.util.List<HitDistribution> hits=twoIndependent
            ? java.util.Arrays.asList(d.flatten(),d.flatten())
            : java.util.Collections.singletonList(d.flatten());
        return new HitDistribution.AttackDistribution(hits);
    }

    /** Full ranged final pipeline: attacker transforms first, NPC transforms last. */
    public static HitDistribution.AttackDistribution rangedFinalDistribution(
        EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition monster, int rangedLevel,
        int normalMax, double accuracy, boolean kandarinHard) {
        return rangedFinalDistribution(weapon,ammo,monster,rangedLevel,normalMax,accuracy,kandarinHard,null);
    }

    public static HitDistribution.AttackDistribution rangedFinalDistribution(
        EquipmentCandidate weapon, EquipmentCandidate ammo, MonsterDefinition monster, int rangedLevel,
        int normalMax, double accuracy, boolean kandarinHard,
        java.util.Collection<EquipmentCandidate> gear) {
        return finalDistribution(rangedAttacker(weapon, ammo, monster, rangedLevel, normalMax, accuracy, kandarinHard,gear),
            monster, AttackStyle.RANGED, weapon, null, false);
    }

    private static boolean isChargedTonalztics(EquipmentCandidate weapon) {
        return weapon!=null&&weapon.getItem()!=null&&weapon.getItem().getItemId()==28922;
    }

    private static boolean fullKarils(java.util.Collection<EquipmentCandidate> gear) {
        return hasEquipment(gear,"Karil's coif")&&hasEquipment(gear,"Karil's leathertop")
            &&hasEquipment(gear,"Karil's leatherskirt")&&hasEquipment(gear,"Karil's crossbow")
            &&hasEquipment(gear,"Amulet of the damned");
    }

    /** Corp is an encounter transform before generic NPC transforms, matching Wiki ordering. */
    public static HitDistribution.AttackDistribution applyCorp(
        HitDistribution.AttackDistribution attacker, MonsterDefinition monster, EquipmentCandidate weapon, AttackStyle style) {
        return CorporealBeastRules.reduced(monster, weapon, style)
            ? attacker.transform(HitDistribution.divide(2, 0), true).flatten() : attacker;
    }

    /** Wiki Tormented Demon accuracy override. Phase is explicit until PBIS exposes encounter inputs in the UI. */
    public static double tormentedDemonAccuracy(String monsterName, String phase, double ordinaryAccuracy) {
        if (monsterName != null && monsterName.equalsIgnoreCase("Tormented Demon") && !"Shielded".equalsIgnoreCase(phase)) return 1.0;
        return ordinaryAccuracy;
    }

    /** Wiki Tormented Demon shield transform: 20% reduction unless unshielded, demonbane, or abyssal. */
    public static HitDistribution tormentedDemonDamage(HitDistribution d, String monsterName, String phase, boolean demonbane, boolean abyssal) {
        if (d == null || monsterName == null || !monsterName.equalsIgnoreCase("Tormented Demon")) return d;
        if (!"Unshielded".equalsIgnoreCase(phase) && !demonbane && !abyssal)
            return d.transform(HitDistribution.multiply(4,5,1)).flatten();
        return d;
    }

    /** Giant rat (Scurrius) is guaranteed accuracy except Manual Cast in the Wiki pipeline. */
    public static double giantRatAccuracy(MonsterDefinition monster, boolean manualCast, double ordinaryAccuracy) {
        return monster != null && monster.getId() == 7223 && !manualCast ? 1.0 : ordinaryAccuracy;
    }
}
