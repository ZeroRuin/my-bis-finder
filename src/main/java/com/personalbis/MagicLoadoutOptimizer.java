package com.personalbis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Skill;

public class MagicLoadoutOptimizer
{
    private static final int SLOT_CHOICES = 4, BEAM_WIDTH = 32, EFFECT_RESERVE = 8, ATTACK_CANDIDATES = 15;
    private final MagicCombatCalculator calc;
    private final AccountSnapshot account;

    @Inject
    public MagicLoadoutOptimizer(MagicCombatCalculator calc, AccountSnapshot account)
    {
        this.calc = calc;
        this.account = account;
    }

    /** The fastest magic loadout and spell, or null when nothing the player owns can cast at the target. */
    public OptimizedMagicLoadout optimize(List<BankItem> bank, Map<EquipmentSlot, List<EquipmentCandidate>> ranked, MonsterDefinition m, boolean task)
    {
        ranked = SlayerTargetRequirements.constrainRanked(m, ranked);
        EquipmentSlot requiredSlot = SlayerTargetRequirements.requiredEquipmentSlot(m);
        EquipmentCandidate requiredEquipment = requiredSlot == null ? null : SlayerTargetRequirements.firstRequiredEquipment(m, ranked.get(requiredSlot));
        // Attack first: rank the viable attacks for THIS target, then include every owned,
        // usable weapon that can autocast one of them. Powered staves are their own attack.
        // This stops raw weapon score from hiding a strong caster such as Blue moon spear.
        List<MagicSpell> attackCandidates = topAttackCandidates(bank, m, ATTACK_CANDIDATES);
        OptimizedMagicLoadout best = null;
        for (EquipmentCandidate weapon : weaponsForAttacks(ranked.get(EquipmentSlot.WEAPON), attackCandidates))
        {
            CalculationCancellation.check();
            if (requiredSlot == EquipmentSlot.SHIELD && weapon.isTwoHanded()) continue;
            // Complete loadout families are searched independently. A Void or Ahrim's family never
            // competes with ordinary raw-stat lines until both have finished and have real damage.
            List<Map<EquipmentSlot, EquipmentCandidate>> families = new ArrayList<>();
            families.add(new EnumMap<>(EquipmentSlot.class));
            EquipmentCandidate helm = LoadoutSearch.findNamed(ranked.get(EquipmentSlot.HEAD), "Void mage helm", ItemNames::is);
            EquipmentCandidate gloves = LoadoutSearch.findNamed(ranked.get(EquipmentSlot.HANDS), "Void knight gloves", ItemNames::is);
            if (helm != null && gloves != null)
                for (String[] robes : new String[][]{{"Void knight top", "Void knight robe"}, {"Elite void top", "Elite void robe"}})
                {
                    Map<EquipmentSlot, EquipmentCandidate> set = LoadoutSearch.ownedSet(ranked, ItemNames::is,
                        EquipmentSlot.BODY, robes[0], EquipmentSlot.LEGS, robes[1]);
                    if (set == null) continue;
                    set.put(EquipmentSlot.HEAD, helm);
                    set.put(EquipmentSlot.HANDS, gloves);
                    families.add(set);
                }
            if (ItemNames.is(weapon, "Ahrim's staff"))
            {
                Map<EquipmentSlot, EquipmentCandidate> ahrims = LoadoutSearch.ownedSet(ranked, ItemNames::is, EquipmentSlot.HEAD, "Ahrim's hood",
                    EquipmentSlot.BODY, "Ahrim's robetop", EquipmentSlot.LEGS, "Ahrim's robeskirt", EquipmentSlot.NECK, "Amulet of the damned");
                if (ahrims != null) families.add(ahrims);
            }

            for (Map<EquipmentSlot, EquipmentCandidate> family : families)
            {
                family.put(EquipmentSlot.WEAPON, weapon);
                if (requiredEquipment != null) family.put(requiredEquipment.getSlot(), requiredEquipment);
                OptimizedMagicLoadout familyBest = searchFamily(family, bank, ranked, m, task, weapon, attackCandidates);
                if (familyBest != null && (best == null || FinalLoadoutSelection.faster(familyBest.getResult(), best.getResult()))) best = familyBest;
            }
        }
        return best;
    }

    private OptimizedMagicLoadout searchFamily(Map<EquipmentSlot, EquipmentCandidate> family, List<BankItem> bank,
        Map<EquipmentSlot, List<EquipmentCandidate>> ranked, MonsterDefinition m, boolean task, EquipmentCandidate weapon, List<MagicSpell> attackCandidates)
    {
        List<Map<EquipmentSlot, EquipmentCandidate>> beam = new ArrayList<>();
        beam.add(family);
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (slot == EquipmentSlot.WEAPON || slot == EquipmentSlot.AMMO) continue;
            if (slot == EquipmentSlot.SHIELD && weapon.isTwoHanded()) continue;
            List<EquipmentCandidate> choices = LoadoutSearch.shortlist(ranked.get(slot), SLOT_CHOICES, MagicLoadoutEffects::synergy);
            List<Map<EquipmentSlot, EquipmentCandidate>> next = new ArrayList<>();
            for (Map<EquipmentSlot, EquipmentCandidate> g : beam)
            {
                next.add(new EnumMap<>(g));
                // Family pieces are locked. Remaining slots are searched normally.
                if (g.containsKey(slot)) continue;
                for (EquipmentCandidate c : choices)
                {
                    Map<EquipmentSlot, EquipmentCandidate> x = new EnumMap<>(g);
                    x.put(slot, c);
                    next.add(x);
                }
            }
            next.sort((a, b) -> Double.compare(gearPotential(b), gearPotential(a)));
            beam = new ArrayList<>(next.subList(0, Math.min(BEAM_WIDTH, next.size())));
            reserveEffectLines(beam, next, m, task);
        }

        PoweredStaff powered = weapon.getItem().getItemId() == ItemIds.ZORYAS_TOME_UNCHARGED ? null : PoweredStaff.forWeapon(weapon.getItem().getName());
        Map<EquipmentSlot, EquipmentCandidate> bestGear = null;
        MagicCombatResult bestResult = null;
        MagicSpell bestSpell = null;
        for (Map<EquipmentSlot, EquipmentCandidate> gear : beam)
        {
            if (powered != null && account.real(Skill.MAGIC) >= powered.minLevel)
            {
                MagicCombatResult r = calc.calculatePowered(gear, m, powered, task);
                if (FinalLoadoutSelection.faster(r, bestResult))
                {
                    bestGear = gear;
                    bestResult = r;
                    bestSpell = new MagicSpell(powered.label, powered.minLevel, powered.baseMaxHit(RaidScaling.boosted(account, Skill.MAGIC, m)));
                }
                continue;
            }
            for (MagicSpell spell : attackCandidates)
            {
                if (!MagicSpellCompatibility.canAutocast(weapon.getItem().getName(), spell) || !runesAvailable(spell, bank, weapon, gear.values())) continue;
                MagicCombatResult r = calc.calculate(gear, m, spell, task);
                if (FinalLoadoutSelection.faster(r, bestResult))
                {
                    bestGear = gear;
                    bestResult = r;
                    bestSpell = spell;
                }
            }
        }
        if (bestGear == null) return null;

        Map<EquipmentSlot, EquipmentCandidate> gear = new EnumMap<>(bestGear);
        MagicSpell spell = bestSpell;
        MagicCombatResult result = LoadoutSearch.fillEmptySlots(gear, bestResult, ranked,
            (slot, c) -> !(slot == EquipmentSlot.SHIELD && weapon.isTwoHanded()),
            trial -> powered != null ? calc.calculatePowered(trial, m, powered, task) : calc.calculate(trial, m, spell, task));
        return new OptimizedMagicLoadout(gear, result, spell);
    }

    /** The attacks worth trying against this target: the strongest few, plus every weapon-specific spell. */
    private List<MagicSpell> topAttackCandidates(List<BankItem> bank, MonsterDefinition m, int limit)
    {
        List<MagicSpell> all = new ArrayList<>();
        for (MagicSpell sp : MagicSpell.combat())
        {
            // Target eligibility must take part in the shortlist. On leafy targets an otherwise
            // strong spell (for example Smoke Barrage) must not use up the shortlist and hide
            // Magic Dart, the legal attack.
            if (!LeafyTargetRules.magicCanDamage(m, sp)) continue;
            if (sp.level > account.real(Skill.MAGIC)) continue;
            if ("ancient".equals(sp.spellbook) && !account.isAncientMagicksUnlocked()) continue;
            if ("arceuus".equals(sp.spellbook))
            {
                if (sp.isDemonbane() && (m == null || !m.hasAttribute("demon"))) continue;
                if ((sp.name.startsWith("Superior Demonbane") || sp.name.startsWith("Dark Demonbane") || sp.markOfDarkness)
                    && !account.isKingdomDividedUnlocked()) continue;
            }
            // Weapon-independent rune check. Rune-providing weapons are handled later, so
            // elemental spells are not rejected merely because the bank lacks that element.
            if (!runesPotentiallyAvailable(sp, bank)) continue;
            all.add(sp);
        }
        all.sort((a, b) -> Double.compare(attackPotential(b, m), attackPotential(a, m)));
        List<MagicSpell> top = new ArrayList<>(all.subList(0, Math.min(limit, all.size())));
        // Spells tied to one weapon (Magic Dart, Iban Blast, the god spells) are always kept. Their
        // strength depends on that weapon - a Slayer's staff (e) on task, for example - which this
        // ranking cannot see, and a weapon that casts nothing else would otherwise get no result.
        for (MagicSpell sp : all.subList(top.size(), all.size()))
            if ("standard".equals(sp.spellbook) && sp.element().isEmpty()) top.add(sp);
        return top;
    }

    private double attackPotential(MagicSpell sp, MonsterDefinition m)
    {
        double base = sp.scaledBaseMax(RaidScaling.boosted(account, Skill.MAGIC, m));
        double speed = 5.0;
        double mult = 1.0 + ElementalWeaknessEffects.multiplier(m, sp);
        if (sp.isDemonbane() && m != null && m.hasAttribute("demon"))
        {
            mult *= sp.markOfDarkness ? 1.25 : 1.0;
            // Accuracy is deliberately represented lightly here; exact accuracy is only
            // used after a compatible weapon and complete loadout have been assembled.
            mult *= sp.markOfDarkness ? 1.40 : 1.20;
        }
        return base * mult / speed;
    }

    private static boolean runesPotentiallyAvailable(MagicSpell sp, List<BankItem> bank)
    {
        Map<String, Integer> have = runeCounts(bank);
        for (Map.Entry<String, Integer> e : sp.runes.entrySet())
        {
            String r = e.getKey().toLowerCase(java.util.Locale.ROOT);
            // An elemental rune may be supplied by the eventual weapon or tome.
            if (r.equals("air rune") || r.equals("water rune") || r.equals("earth rune") || r.equals("fire rune")) continue;
            if (have.getOrDefault(r, 0) < e.getValue()) return false;
        }
        return true;
    }

    private static Map<String, Integer> runeCounts(List<BankItem> bank)
    {
        Map<String, Integer> have = new HashMap<>();
        for (BankItem b : bank) have.put(b.getName().toLowerCase(java.util.Locale.ROOT), b.getQuantity());
        return have;
    }

    private static List<EquipmentCandidate> weaponsForAttacks(List<EquipmentCandidate> weapons, List<MagicSpell> attacks)
    {
        List<EquipmentCandidate> capable = new ArrayList<>();
        for (EquipmentCandidate c : LoadoutSearch.allUsable(weapons))
        {
            if (c.getItem() == null) continue;
            String n = c.getItem().getName();
            boolean canAttack = PoweredStaff.forWeapon(n) != null || PoweredStaff.requiresContext(n);
            for (MagicSpell sp : attacks) canAttack |= MagicSpellCompatibility.canAutocast(n, sp);
            if (canAttack) capable.add(c);
        }
        return capable;
    }

    /**
     * The beam is cut on raw magic stats, which can't see target effects: a Salve
     * amulet (ei) against undead, or an imbued Slayer helm on task, has low raw stats
     * but a large multiplier. Keep the best few lines for each active effect past the
     * cut, so their real damage is still calculated.
     */
    private static void reserveEffectLines(List<Map<EquipmentSlot, EquipmentCandidate>> beam,
        List<Map<EquipmentSlot, EquipmentCandidate>> sorted, MonsterDefinition m, boolean task)
    {
        if (sorted.size() <= beam.size()) return;
        Map<String, Integer> kept = new HashMap<>();
        for (Map<EquipmentSlot, EquipmentCandidate> g : beam)
        {
            String note = MagicLoadoutEffects.evaluate(g.values(), m, task).note;
            if (!note.isEmpty()) kept.merge(note, 1, Integer::sum);
        }
        for (Map<EquipmentSlot, EquipmentCandidate> g : sorted.subList(beam.size(), sorted.size()))
        {
            String note = MagicLoadoutEffects.evaluate(g.values(), m, task).note;
            if (note.isEmpty() || kept.getOrDefault(note, 0) >= EFFECT_RESERVE) continue;
            kept.merge(note, 1, Integer::sum);
            beam.add(g);
        }
    }

    private static double gearPotential(Map<EquipmentSlot, EquipmentCandidate> g)
    {
        int atk = 0, prayer = 0;
        double dmg = 0;
        for (EquipmentCandidate c : g.values())
        {
            if (c == null) continue;
            atk += c.getMagicAttack();
            dmg += c.getMagicDamage();
            prayer += c.getPrayer();
        }
        return atk + Math.max(0, dmg) * 1000.0 + prayer * 0.001;
    }

    static boolean runesAvailable(MagicSpell sp, List<BankItem> bank, EquipmentCandidate weapon, Collection<EquipmentCandidate> gear)
    {
        Map<String, Integer> have = runeCounts(bank);
        String wn = weapon == null ? "" : ItemNames.lower(weapon);
        for (Map.Entry<String, Integer> e : sp.runes.entrySet())
        {
            String rune = e.getKey().toLowerCase(java.util.Locale.ROOT);
            if (provides(wn, rune) || MagicTomeEffects.providesRune(gear, rune)) continue;
            if (have.getOrDefault(rune, 0) < e.getValue()) return false;
        }
        return true;
    }

    private static boolean provides(String w, String r)
    {
        if (w.equals("kodai wand") && r.equals("water rune")) return true;
        if (!w.contains("staff")) return false;
        if (r.equals("air rune")) return w.contains("air") || w.contains("smoke") || w.contains("dust") || w.contains("mist");
        if (r.equals("water rune")) return w.contains("water") || w.contains("steam") || w.contains("mud") || w.contains("mist");
        if (r.equals("earth rune")) return w.contains("earth") || w.contains("lava") || w.contains("mud") || w.contains("dust");
        if (r.equals("fire rune")) return w.contains("fire") || w.contains("lava") || w.contains("smoke") || w.contains("steam");
        return false;
    }
}
