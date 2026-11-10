package com.personalbis;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Prints one line per search for a fixed set of random banks and targets. Run it before
 * and after a change to the search and compare the two outputs: any difference is a
 * change in what the plugin recommends.
 *
 * <p>Arguments: output file, number of cases (default 300).
 */
public final class SearchSnapshot
{
    public static void main(String[] args) throws Exception
    {
        int cases = args.length > 1 ? Integer.parseInt(args[1]) : 300;
        SearchFixture fixture = new SearchFixture();
        try (PrintStream out = args.length > 0 ? new PrintStream(args[0], "UTF-8") : System.out)
        {
            for (int seed = 0; seed < cases; seed++)
            {
                Random random = new Random(seed);
                List<BankItem> bank = fixture.randomBank(random, 2 + random.nextInt(5), 3 + random.nextInt(6), 0.6);
                MonsterDefinition monster = fixture.randomMonster(random);
                boolean task = random.nextBoolean();
                String head = seed + "|" + monster + "|" + task + "|";
                for (AttackStyle style : new AttackStyle[]{AttackStyle.MELEE_STAB, AttackStyle.MELEE_SLASH, AttackStyle.MELEE_CRUSH})
                {
                    OptimizedLoadout best = fixture.melee(bank, style, monster, task);
                    out.println(head + style + "|" + (best == null ? "none" : line(best.getResult().getExpectedTtkSeconds(), best.getResult().getDps(), best.itemIds(), "")));
                }
                OptimizedRangedLoadout ranged = fixture.ranged(bank, monster, task);
                out.println(head + "RANGED|" + (ranged == null ? "none" : line(ranged.getResult().getExpectedTtkSeconds(), ranged.getResult().getDps(), ranged.itemIds(), "")));
                OptimizedMagicLoadout magic = fixture.magic(bank, monster, task);
                out.println(head + "MAGIC|" + (magic == null ? "none" : line(magic.getResult().getExpectedTtkSeconds(), magic.getResult().getDps(), magic.itemIds(), magic.getSpell().name)));
            }
        }
    }

    private static String line(double ttk, double dps, List<Integer> ids, String spell)
    {
        List<Integer> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);
        return ttk + "|" + dps + "|" + sorted + "|" + spell;
    }
}
