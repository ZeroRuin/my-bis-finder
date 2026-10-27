package com.personalbis;

/** HP-dependent Defence from the Wiki calculator's scaling/Vardorvis.ts.
 * Only the three verified variants are recognised; Deadman is not inferred.
 */
public final class VardorvisRules
{
    private VardorvisRules() {}

    public static boolean supported(MonsterDefinition m)
    {
        if (m == null || !"Vardorvis".equals(m.getName())) return false;
        String v = m.getVersion();
        return (m.getId() == 12224 && "Quest".equals(v))
            || (m.getId() == 12223 && ("Post-quest".equals(v) || "Awakened".equals(v)));
    }

    public static int maximumHp(MonsterDefinition m)
    {
        if(!supported(m))return m==null?1:m.getHitpoints();
        return "Quest".equals(m.getVersion())?500:"Awakened".equals(m.getVersion())?1400:700;
    }

    public static int defence(MonsterDefinition m, int remainingHp)
    {
        if (!supported(m)) return m == null ? 1 : m.getDefenceLevel();
        int hp, start, end;
        switch (m.getVersion())
        {
            case "Quest": hp=500; start=180; end=130; break;
            case "Awakened": hp=1400; start=268; end=181; break;
            default: hp=700; start=215; end=145;
        }
        int current=Math.max(0,Math.min(hp,remainingHp));
        // Truncate the completed positive interpolation, not the loss separately.
        return (int)((long)end + (long)(start-end)*current/hp);
    }

    public static int startingDefence(MonsterDefinition m)
    {
        return defence(m,m == null ? 1 : m.getHitpoints());
    }
}
