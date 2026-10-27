package com.personalbis;

import com.personalbis.wiki.WikiMonster;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class RaidTargetVariants
{
    private RaidTargetVariants() {}
    static boolean hidden(WikiMonster w)
    {
        return (w.id>=16678&&w.id<=16681) || w.id==16688 || (w.id==11763||w.id==11764) && "Invulnerable".equalsIgnoreCase(w.version);
    }
    static List<String> versions(WikiMonster w)
    {
        String v=w.version==null?"":w.version;
        if(w.id==16640)return Arrays.asList("Armour stage 1","Armour stage 2","Armour stage 3","Armour stage 4","Stunned");
        if(w.attributes!=null&&w.attributes.contains("xerician"))
        {
            if(w.name.equals("Great Olm"))
            {
                if(w.id==7551||w.id==7554)return Collections.singletonList("Head");
                if(w.id==7552||w.id==7555)return Collections.singletonList("Left claw (Melee hand)");
                if(w.id==7550||w.id==7553)return Collections.singletonList("Right claw (Magic hand)");
            }
            return Collections.singletonList(v);
        }
        if(w.name.equals("Akkha"))return Arrays.asList("Magic-vulnerable phase","Ranged-vulnerable phase","Melee-vulnerable phase","Final phase (Melee only)");
        if(w.name.endsWith("' Warden"))
        {
            if(v.equals("Active"))return Arrays.asList("Phase 2 (Magic-vulnerable)","Phase 2 (Ranged-vulnerable)");
            if(v.equals("Core-ejected"))return Collections.singletonList("Phase 2 (Core-ejected)");
            if(v.equals("Damaged"))return Collections.singletonList("Phase 3");
            if(v.equals("Enraged"))return Collections.singletonList("Phase 3 (Enraged)");
        }
        if(w.name.equals("Obelisk (Tombs of Amascut)"))return Collections.singletonList("Phase 1");
        if(w.name.equals("Kephri")&&v.equals("Aggressive"))return Collections.singletonList("Unshielded");
        return TheatreOfBloodRules.versions(w.name,HueycoatlRules.part(w.id,v));
    }
    static int hp(WikiMonster w,String version)
    {
        return w.name.equals("Akkha")&&version.startsWith("Final phase")?Math.max(1,w.skills.hp/5):w.skills.hp;
    }
    static String key(MonsterDefinition m)
    {
        return RaidScaling.isCox(m)||RaidScaling.isToa(m)||FracturedArchiveRules.isTfa(m)?"|raid-state|"+m.getVersion().toLowerCase(Locale.ROOT):"";
    }
    static boolean canDamage(MonsterDefinition m,AttackStyle style)
    {
        if(m==null)return true;
        String v=m.getVersion();
        if(m.getName().equals("Akkha"))
        {
            if(v.startsWith("Magic-vulnerable"))return style==AttackStyle.MAGIC;
            if(v.startsWith("Ranged-vulnerable"))return style==AttackStyle.RANGED;
            if(v.startsWith("Melee-vulnerable")||v.startsWith("Final phase"))return style!=AttackStyle.MAGIC&&style!=AttackStyle.RANGED;
        }
        if(m.getName().endsWith("' Warden")&&v.startsWith("Phase 2 ("))
        {
            if(v.contains("Magic-vulnerable"))return style==AttackStyle.MAGIC;
            if(v.contains("Ranged-vulnerable"))return style==AttackStyle.RANGED;
        }
        return true;
    }
    static String note(MonsterDefinition m)
    {
        if(FracturedArchiveRules.isTfa(m))return FracturedArchiveRules.note(m);
        if(m!=null&&m.getName().endsWith("' Warden")&&m.getVersion().contains("Core-ejected"))
            return "Exposed core: melee attacks always deal their maximum damage. TTK assumes continuous exposure; ejection windows and body damage are not simulated.";
        if(m!=null&&m.getName().endsWith("' Warden")&&m.getVersion().contains("vulnerable"))
            return "Phase 2: attacks always hit; damage bounds depend on attack roll and scaled target defence.";
        return "";
    }
}
