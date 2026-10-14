package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import com.personalbis.wiki.WikiEquipment;
import com.personalbis.wiki.WikiMonster;
import java.util.*;
import net.runelite.api.Skill;

/** Catalogue coverage, cap boundaries and complete calculator regression checks. */
public final class TheatreOfBloodSelfTest
{
    private static int checks;
    private static void check(boolean value, String label)
    {
        if (!value) throw new AssertionError(label);
        checks++;
    }
    private static void near(double actual, double expected, String label)
    {
        check(Math.abs(actual - expected) < 1e-9, label + ": " + actual);
    }
    private static MonsterDefinition target(List<MonsterDefinition> targets, String name, String version)
    {
        for (MonsterDefinition m : targets)
            if (m.getName().equals(name) && m.getVersion().equals(version)) return m;
        throw new AssertionError("Missing " + name + " / " + version);
    }
    private static EquipmentCandidate equipment(WikiDataPack pack, String name)
    {
        for (WikiEquipment w : pack.getEquipment()) if (w.name.equalsIgnoreCase(name))
            return new EquipmentCandidate(new BankItem(w.id, 1, w.name), EquipmentSlot.valueOf(w.slot.toUpperCase(Locale.ROOT)),
                0, w.offensive.slash, w.bonuses.str, w.offensive.ranged, w.bonuses.ranged_str,
                w.offensive.magic, (float) w.bonuses.magic_str, w.bonuses.prayer,
                w.speed, w.isTwoHanded, w.category, RequirementResult.usable("fixture"));
        throw new AssertionError("Missing equipment " + name);
    }
    private static HitDistribution transformed(int damage, boolean accurate, MonsterDefinition m, AttackStyle style)
    {
        return WikiNpcTransforms.apply(HitDistribution.single(damage, accurate), m, style, null, null, false);
    }
    public static void main(String[] args)
    {
        WikiDataPack pack = new WikiDataPack(new Gson());
        List<MonsterDefinition> targets = MonsterDatabase.catalogue(pack.getMonsters());
        check(targets.stream().filter(m -> m.getName().equals("Verzik Vitur")).count() == 9, "Exactly nine Verzik states");
        check(targets.stream().filter(m -> m.getName().equals("Nylocas Vasilias")).count() == 9, "Exactly nine Vasilias states");
        String[] modes = {"Entry Mode", "Normal Mode", "Hard Mode"};
        for (String mode : modes)
        {
            for (String name : Arrays.asList("The Maiden of Sugadinti", "Pestilent Bloat", "Sotetseg", "Xarpus"))
                check(target(targets, name, mode) != null, name + " " + mode + " retained");
            for (String form : Arrays.asList("Melee", "Ranged", "Magic"))
            {
                MonsterDefinition m = target(targets, "Nylocas Vasilias", mode + ", " + form + " form");
                check(WikiTargetEligibility.meleeCanDamage(m, null) == form.equals("Melee"), "Nylocas melee restriction " + m);
                check(WikiTargetEligibility.rangedCanDamage(m) == form.equals("Ranged"), "Nylocas ranged restriction " + m);
                check(WikiTargetEligibility.magicCanDamage(m) == form.equals("Magic"), "Nylocas magic restriction " + m);
            }
            for (int phase = 1; phase <= 3; phase++)
            {
                MonsterDefinition m = target(targets, "Verzik Vitur", mode + ", Phase " + phase);
                check(TheatreOfBloodRules.phaseOne(m) == (phase == 1), "Verzik phase isolation " + m);
                for (AttackStyle style : AttackStyle.values())
                {
                    boolean magicOrRanged = style == AttackStyle.MAGIC || style == AttackStyle.RANGED;
                    int cap = magicOrRanged ? 3 : 10;
                    HitDistribution d = transformed(60, true, m, style);
                    check(d.max() == (phase == 1 ? cap : 60), "Correct cap " + m + " " + style);
                    near(d.expectedHit(), phase == 1 ? cap / 2.0 : 60, "Correct reroll expectation " + m + " " + style);
                    near(transformed(cap, true, m, style).expectedHit(), cap, "At-cap hits unchanged");
                    near(transformed(1, true, m, style).expectedHit(), 1, "Low hit unchanged");
                    near(transformed(0, false, m, style).expectedHit(), 0, "Miss unchanged");
                }
            }
        }
        for (String form : Arrays.asList("Melee", "Ranged", "Magic"))
        {
            MonsterDefinition m = target(targets, "Nylocas Prinkipas", "Hard Mode, " + form + " form");
            check(WikiTargetEligibility.meleeCanDamage(m, null) == form.equals("Melee"), "Prinkipas melee form");
            check(WikiTargetEligibility.rangedCanDamage(m) == form.equals("Ranged"), "Prinkipas ranged form");
            check(WikiTargetEligibility.magicCanDamage(m) == form.equals("Magic"), "Prinkipas magic form");
        }
        // Catalogue must retain source HP/defences, even where mode stats are identical.
        for (WikiMonster wiki : pack.getMonsters()) if (TheatreOfBloodRules.isBoss(wiki.name))
            for (String version : TheatreOfBloodRules.versions(wiki.name, wiki.version))
            {
                MonsterDefinition m = target(targets, wiki.name, version);
                check(m.getId() == wiki.id && m.getHitpoints() == wiki.skills.hp
                    && m.getDefenceLevel() == wiki.skills.def
                    && m.getMagicLevel() == wiki.skills.magic, "Source identity and stats retained " + m);
            }
        MonsterDefinition normal = target(targets, "Verzik Vitur", "Normal Mode, Phase 1");
        MonsterDefinition hard = target(targets, "Verzik Vitur", "Hard Mode, Phase 1");
        check(!MonsterDatabase.sameCatalogueEntry(normal, hard), "Equal-stat modes are distinct choices");
        check(!MonsterDatabase.combatKey(normal).equals(MonsterDatabase.combatKey(hard)), "Equal-stat mode keys remain distinct");

        MonsterDefinition seren = null;
        for (MonsterDefinition m : targets) if (m.getName().equals("Fragment of Seren")) seren = m;
        check(seren != null, "Seren exists");
        for (int hit : new int[] {0, 1, 21, 22, 23, 24})
            near(transformed(hit, hit != 0, seren, AttackStyle.MAGIC).expectedHit(), hit, "Seren at/below cap " + hit);
        for (int hit : new int[] {25, 30, 100})
        {
            HitDistribution d = transformed(hit, true, seren, AttackStyle.MAGIC);
            check(d.min() == 22 && d.max() == 24, "Seren reroll range");
            near(d.expectedHit(), 23, "Seren reroll expectation");
        }
        HitDistribution.AttackDistribution serenAtCap = WikiPlayerVsNpcCalc.finalDistributionByWeaponName(
            new HitDistribution.AttackDistribution(Collections.singletonList(HitDistribution.single(24, true))),
            seren, AttackStyle.MAGIC, "Trident of the seas", null, false);
        near(ExpectedKillTime.seconds(serenAtCap, 24, 4), 2.4, "Seren TTK uses unchanged 24 hit");
        HitDistribution.AttackDistribution multi = WikiPlayerVsNpcCalc.finalDistribution(
            new HitDistribution.AttackDistribution(Arrays.asList(HitDistribution.single(60, true), HitDistribution.single(60, true))),
            normal, AttackStyle.MELEE_SLASH, null, null, false);
        check(multi.max() == 20, "Verzik caps each hitsplat, not total attack");
        near(multi.expectedDamage(), 10, "Multi-hit expectation");
        near(WikiPlayerVsNpcCalc.dps(multi, 4), 10 / 2.4, "Capped DPS pipeline");
        check(Double.isFinite(ExpectedKillTime.seconds(multi, 30, 4)), "Capped multi-hit TTK finite");

        AccountSnapshot account = new AccountSnapshot(null)
        {
            @Override public int real(Skill s) { return 99; }
            @Override public int boosted(Skill s) { return 99; }
        };
        Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.WEAPON, equipment(pack, "Dragon scimitar"));
        CombatResult melee = new MeleeCombatCalculator(account).calculate(gear, AttackStyle.MELEE_SLASH, normal, false);
        check(melee.getMaxHit() == 10 && Double.isFinite(melee.getExpectedTtkSeconds()), "Melee calculator phase-one cap/TTK");
        gear.put(EquipmentSlot.WEAPON, equipment(pack, "Bow of faerdhinen"));
        RangedCombatResult ranged = new RangedCombatCalculator(account).calculate(gear, normal, false);
        check(ranged.getMaxHit() == 3 && Double.isFinite(ranged.getExpectedTtkSeconds()), "Ranged calculator phase-one cap/TTK");
        gear.put(EquipmentSlot.WEAPON, equipment(pack, "Trident of the seas"));
        MagicCombatCalculator magicCalc = new MagicCombatCalculator(account);
        MagicCombatResult magic = magicCalc.calculatePowered(gear, normal, PoweredStaff.forWeapon("Trident of the seas"), false);
        check(magic.getMaxHit() == 3 && Double.isFinite(magic.getExpectedTtkSeconds()), "Powered magic phase-one cap/TTK");
        MonsterDefinition meleeForm = target(targets, "Nylocas Vasilias", "Hard Mode, Melee form");
        MagicCombatResult immune = magicCalc.calculatePowered(gear, meleeForm, PoweredStaff.forWeapon("Trident of the seas"), false);
        check(immune.getDps() == 0 && Double.isInfinite(immune.getExpectedTtkSeconds()), "Wrong Nylo style cannot win optimization");
        check(PoweredStaff.forWeapon("Dawnbringer") == null, "No Dawnbringer implementation added");
        System.out.println("PASS " + checks + " Seren and Theatre of Blood checks");
    }
}
