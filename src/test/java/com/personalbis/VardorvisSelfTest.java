package com.personalbis;

import java.util.Collections;

/** Standalone checks; runs without RuneLite or a signed-in account. */
public final class VardorvisSelfTest
{
    private static MonsterDefinition target(String version)
    {
        int hp="Quest".equals(version)?500:"Awakened".equals(version)?1400:700;
        return new MonsterDefinition("Quest".equals(version)?12224:12223,"Vardorvis",version,
            784,0,215,0,hp,215,65,85,580,580,580,580,
            Collections.emptyList(),2,"",0,0);
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)
    {
        String[] variants={"Quest","Post-quest","Awakened"};
        int[] starts={180,215,268},ends={130,145,181};
        for(int i=0;i<variants.length;i++)
        {
            MonsterDefinition m=target(variants[i]);
            check(VardorvisRules.defence(m,m.getHitpoints())==starts[i],"full HP "+variants[i]);
            check(VardorvisRules.defence(m,0)==ends[i],"zero HP "+variants[i]);
            check(VardorvisRules.defence(m,-1)==ends[i],"lower clamp");
            check(VardorvisRules.defence(m,m.getHitpoints()+1)==starts[i],"upper clamp");
            for(int hp=0;hp<=m.getHitpoints();hp++)
            {
                int expected=(int)(((double)(hp-m.getHitpoints())*(ends[i]-starts[i])/-m.getHitpoints())+starts[i]);
                check(VardorvisRules.defence(m,hp)==expected,"Wiki lerp parity at "+hp);
            }
        }
        check(!VardorvisRules.supported(target("Deadman")),"unsupported variant remains unsupported");
        check(VardorvisRules.defence(target("Awakened"),700)==224,"truncates completed interpolation");
        System.out.println("PASS Vardorvis endpoints, every HP/variant, clamping and variant guard");
    }
}
