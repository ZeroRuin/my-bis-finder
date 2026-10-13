package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.WikiDataPack;
import com.personalbis.wiki.WikiEquipment;
import java.util.*;
import net.runelite.api.Skill;

public final class HueycoatlSelfTest
{
    private static int checks;
    private static void check(boolean result, String label)
    {
        if (!result) throw new AssertionError(label);
        checks++;
    }
    private static void near(double actual, double expected, String label)
    {
        check(Math.abs(actual - expected) < 1e-9, label + ": " + actual);
    }
    private static EquipmentCandidate candidate(WikiEquipment w)
    {
        EquipmentSlot slot = EquipmentSlot.valueOf(w.slot.toUpperCase(Locale.ROOT));
        return new EquipmentCandidate(new BankItem(w.id, 1, w.name), slot, 0,
            w.offensive == null ? 0 : w.offensive.crush, w.bonuses == null ? 0 : w.bonuses.str,
            0, 0, 0, 0, w.bonuses == null ? 0 : w.bonuses.prayer,
            w.speed, w.isTwoHanded, w.category, RequirementResult.usable("test"));
    }
    public static void main(String[] args)
    {
        WikiDataPack pack = new WikiDataPack(new Gson());
        List<MonsterDefinition> parts = new ArrayList<>();
        for (MonsterDefinition m : MonsterDatabase.catalogue(pack.getMonsters()))
            if (m.getName().equals("The Hueycoatl")) parts.add(m);
        check(parts.size() == 3, "Exactly three attackable Hueycoatl targets");
        MonsterDefinition tail = null, body = null, head = null;
        for (MonsterDefinition m : parts)
        {
            if (m.getId() == 14014) tail = m;
            if (m.getId() == 14017) body = m;
            if (m.getId() == 14009) head = m;
        }
        check(tail != null && tail.getVersion().equals("Tail"), "Tail label retained");
        check(body != null && body.getVersion().equals("Body (phase 1)"), "Body phase label");
        check(head != null && head.getVersion().equals("Head"), "Head label retained");
        HitDistribution high = HitDistribution.single(40, true);
        HitDistribution miss = HitDistribution.single(0, false);
        HitDistribution ordinary = HueycoatlRules.tail(high, tail, AttackStyle.MELEE_STAB, null, false);
        check(ordinary.max() == 4, "Ordinary cap 4");
        near(ordinary.expectedHit(), 2, "Uniform limiter 0..4 rather than hard clipping");
        near(HueycoatlRules.tail(HitDistribution.single(2, true), tail, AttackStyle.RANGED, null, false).expectedHit(), 1.4,
            "Limiter applies to low rolled hits too");
        HitDistribution crush = HueycoatlRules.tail(high, tail, AttackStyle.MELEE_CRUSH, null, true);
        check(crush.max() == 9 && crush.min() == 1, "Qualifying crush range 1..9");
        near(crush.expectedHit(), 4.6, "Crush limiter expected damage");
        near(HueycoatlRules.tail(miss, tail, AttackStyle.MELEE_CRUSH, null, true).expectedHit(), 1, "Crush misses become one");
        near(HueycoatlRules.tail(miss, tail, AttackStyle.MELEE_CRUSH, null, false).expectedHit(), 0, "Nonqualifying crush misses remain zero");
        HitDistribution earth = HueycoatlRules.tail(high, tail, AttackStyle.MAGIC, "earth", false);
        check(earth.max() == 4 && earth.min() == 0, "Earth uses standard cap 4");
        near(HueycoatlRules.tail(miss, tail, AttackStyle.MAGIC, "earth", false).expectedHit(), 0, "Earth misses remain zero");
        check(HueycoatlRules.tail(high, head, AttackStyle.MELEE_CRUSH, null, true).max() == 40, "No head cap");
        check(HueycoatlRules.tail(high, body, AttackStyle.MELEE_CRUSH, null, true).max() == 40, "No body cap");
        check(WikiPlayerVsNpcCalc.magicFinalDistribution(40, 1, false, tail,
            "Earth staff", "earth", false).max() == 4, "Earth spell pipeline cap");
        check(WikiPlayerVsNpcCalc.magicFinalDistribution(40, 1, false, tail,
            "Fire staff", "fire", false).max() == 4, "Other spell pipeline cap");
        check(WikiPlayerVsNpcCalc.finalDistribution(
            WikiPlayerVsNpcCalc.standardAttacker(40, 1), tail, AttackStyle.RANGED,
            null, null, false).max() == 4, "Ranged final pipeline cap");
        EquipmentCandidate hammer = null, lance = null, dual = null, axe = null;
        for (WikiEquipment w : pack.getEquipment())
        {
            if (w.name.equals("Dragon warhammer")) hammer = candidate(w);
            if (w.name.equals("Dragon hunter lance")) lance = candidate(w);
            if (w.name.equals("Dual macuahuitl")) dual = candidate(w);
            if (w.name.equals("Leaf-bladed battleaxe")) axe = candidate(w);
        }
        check(hammer != null && lance != null && dual != null, "Real weapon fixtures exist");
        check(HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH, Collections.singletonList(hammer)), "Crush-leading weapon qualifies");
        check(!HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_STAB, Collections.singletonList(hammer)), "Crush-leading stats require crush attack style");
        check(!HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH, Collections.singletonList(lance)), "Lance's higher stab bonus prevents crush benefit");
        check(axe != null && !HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH,
            Collections.singletonList(axe)), "Equal slash/crush bonuses do not qualify");
        EquipmentCandidate staff = candidate(WikiEquipmentEngine.canonicalItem(12000));
        EquipmentCandidate ancestral = candidate(WikiEquipmentEngine.canonicalItem(21021));
        EquipmentCandidate rangedBody = candidate(WikiEquipmentEngine.canonicalItem(11828));
        EquipmentCandidate rangedLegs = candidate(WikiEquipmentEngine.canonicalItem(11830));
        check(HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH,
            Collections.singletonList(staff)), "Staff alone has highest crush bonus");
        check(!HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH,
            Arrays.asList(staff, ancestral)), "Equipped magic bonus beats crush");
        check(!HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH,
            Arrays.asList(staff, rangedBody)), "Tied ranged and crush bonuses do not qualify");
        check(!HueycoatlRules.qualifyingCrush(AttackStyle.MELEE_CRUSH,
            Arrays.asList(staff, rangedBody, rangedLegs)), "Equipped ranged bonus beats crush");
        AccountSnapshot account = new AccountSnapshot(null)
        {
            @Override public int real(Skill s) { return 99; }
            @Override public int boosted(Skill s) { return 99; }
        };
        MeleeCombatCalculator calc = new MeleeCombatCalculator(account);
        Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.WEAPON, hammer);
        CombatResult one = calc.calculate(gear, AttackStyle.MELEE_CRUSH, tail, false);
        check(one.getMaxHit() == 9 && Double.isFinite(one.getExpectedTtkSeconds()), "Full calculator capped max and TTK");
        gear.put(EquipmentSlot.WEAPON, lance);
        check(calc.calculate(gear, AttackStyle.MELEE_CRUSH, tail, false).getMaxHit() == 4, "Full calculator checks other attack bonuses");
        gear.put(EquipmentSlot.WEAPON, dual);
        check(calc.calculate(gear, AttackStyle.MELEE_CRUSH, tail, false).getMaxHit() == 18, "Two hits capped individually, total max 18");
        HitDistribution.AttackDistribution two = WikiPlayerVsNpcCalc.finalDistribution(
            WikiPlayerVsNpcCalc.independentTwoHitAttacker(80, 0), tail,
            AttackStyle.MELEE_CRUSH, hammer, null, false, Collections.singletonList(hammer));
        near(two.expectedDamage(), 2, "Both independent misses become one");
        System.out.println("PASS " + checks + " Hueycoatl catalogue and damage checks");
    }
}
