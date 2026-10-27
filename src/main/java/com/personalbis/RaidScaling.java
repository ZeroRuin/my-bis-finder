package com.personalbis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import net.runelite.api.Skill;

/** Selected-target scaling, with all truncation stages kept explicit. */
final class RaidScaling
{
    private RaidScaling() {}
    private static boolean id(MonsterDefinition m, Integer... ids)
    {
        return m != null && Arrays.asList(ids).contains(m.getId());
    }
    static boolean isCox(MonsterDefinition m) { return m != null && m.hasAttribute("xerician"); }
    private static final Set<Integer> TOA = new HashSet<>(Arrays.asList(
        11789,11790,11791,11792,11793,11794,11795,11796,11797,11798,11799,
        11778,11779,11780,11719,11721,11724,11725,11726,11730,11732,11733,
        11751,11750,11752,11753,11754,11756,11757,11755,11758,11761,11763,11762,11764));
    static boolean isToa(MonsterDefinition m) { return m != null && TOA.contains(m.getId()); }
    private static boolean path(MonsterDefinition m)
    {
        return id(m,11789,11790,11791,11792,11793,11794,11795,11796,11797,11798,11799,
            11778,11779,11780,11719,11721,11724,11725,11726,11730,11732,11733);
    }
    private static boolean overlord(MonsterDefinition m) { int id=m.getId(); return id==11724||id==11725||id==11726; }
    static int invocation(MonsterDefinition m) { return value(m,"raid_level:"); }
    private static int value(MonsterDefinition m, String prefix)
    {
        if (m != null) for (String a : m.getAttributes()) if (a.startsWith(prefix)) return Integer.parseInt(a.substring(prefix.length()));
        return 0;
    }
    static int defenceRoll(MonsterDefinition m, int roll)
    {
        return isToa(m) && !overlord(m) ? (int)((long)roll * (250 + invocation(m)) / 250) : roll;
    }
    static int boosted(AccountSnapshot account, Skill skill, MonsterDefinition m)
    {
        int current = account.boosted(skill);
        if (!isToa(m) || !(skill == Skill.ATTACK || skill == Skill.STRENGTH || skill == Skill.RANGED || skill == Skill.MAGIC)) return current;
        int base = account.real(skill), salted = base + base * 16 / 100 + 11;
        if (m.hasAttribute("raid_assume_salts")) return Math.max(current, salted);
        if (m.hasAttribute("raid_exclude_salts"))
        {
            String prefix="raid_boost_"+skill.name()+":";
            for(String a:m.getAttributes()) if(a.startsWith(prefix)) return Integer.parseInt(a.substring(prefix.length()));
            return base;
        }
        return current;
    }

    static MonsterDefinition withOwnedBoosts(MonsterDefinition m, AccountSnapshot account, List<BankItem> owned)
    {
        if (!isToa(m) || !m.hasAttribute("raid_exclude_salts")) return m;
        List<String> attrs=new ArrayList<>(m.getAttributes());
        attrs.removeIf(a->a.startsWith("raid_boost_"));
        for(Skill skill:new Skill[]{Skill.ATTACK,Skill.STRENGTH,Skill.RANGED,Skill.MAGIC})
        {
            int level=account.real(skill),best=level;
            for(BankItem item:owned)
            {
                if(item.getQuantity()<=0)continue;
                String n=item.getName().toLowerCase(Locale.ROOT);
                int boost=0;
                if(skill==Skill.ATTACK){
                    if(n.startsWith("super attack(")||n.startsWith("super combat potion(")||n.startsWith("divine super attack potion(")||n.startsWith("divine super combat potion("))boost=5+level*15/100;
                    else if(n.startsWith("attack potion("))boost=3+level/10;
                }else if(skill==Skill.STRENGTH){
                    if(n.startsWith("super strength(")||n.startsWith("super combat potion(")||n.startsWith("divine super strength potion(")||n.startsWith("divine super combat potion("))boost=5+level*15/100;
                    else if(n.startsWith("strength potion("))boost=3+level/10;
                }else if(skill==Skill.RANGED){
                    if(n.startsWith("ranging potion(")||n.startsWith("divine ranging potion("))boost=4+level/10;
                }else{
                    if(n.equals("saturated heart"))boost=4+level*13/100;
                    else if(n.equals("imbued heart"))boost=1+level/10;
                    else if(n.startsWith("forgotten brew("))boost=3+level*8/100;
                    else if(n.startsWith("ancient brew("))boost=2+level*5/100;
                    else if(n.startsWith("magic potion(")||n.startsWith("divine magic potion("))boost=4;
                }
                best=Math.max(best,level+boost);
            }
            attrs.add("raid_boost_"+skill.name()+":"+best);
        }
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),m.getDefenceLevel(),m.getMagicLevel(),m.getRangedLevel(),m.getHitpoints(),
            m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),m.getMagicDefence(),m.getLightDefence(),m.getStandardDefence(),m.getHeavyDefence(),attrs,m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }

    private static int scaleDrainOffensive(int base,RaidSettings s,boolean single)
    {
        if(single){int factor=Math.max(s.coxHp,55);if(s.challenge)factor=factor*150/100;return Math.max(1,base*factor/99);}
        int n=s.coxParty-1,highestHp=Math.max(55,Math.min(99,55+44*s.coxHp/99));
        int value=base*highestHp/99;value=value*(100+(int)Math.sqrt(n)*7+n)/100;
        if(s.challenge)value=value*150/100;return Math.max(50,Math.min(5000,value));
    }

    static MonsterDefinition apply(MonsterDefinition m, RaidSettings s)
    {
        if(FracturedArchiveRules.isTfa(m))return FracturedArchiveRules.scale(m,s.tfaParty);
        if (m == null || (!isCox(m) && !isToa(m))) return m;
        int hp=m.getHitpoints(), def=m.getDefenceLevel(), magic=m.getMagicLevel(), ranged=m.getRangedLevel();
        List<String> attrs = new ArrayList<>(m.getAttributes());
        attrs.removeIf(a -> a.startsWith("raid_level:") || a.equals("raid_assume_salts") || a.equals("raid_exclude_salts"));
        attrs.removeIf(a->a.startsWith("raid_options:"));
        attrs.add("raid_options:"+s.key(m));
        if (isToa(m))
        {
            boolean finalAkkha=m.getName().equals("Akkha")&&m.getVersion().startsWith("Final phase");
            if(finalAkkha)hp*=5;
            boolean core = id(m,11755,11758);
            if (core) hp=4500;
            hp += hp * ((core ? 1 : 4) * s.raidLevel / 10) / 100;
            if (path(m) && s.pathLevel > 0) hp=hp*(103+5*s.pathLevel)/100;
            if (s.toaParty >= 2)
            {
                int partyFactor=9*(s.toaParty>=3?2:1)+(s.toaParty>=4?6*(s.toaParty-3):0);
                hp=hp*(10+partyFactor)/10;
            }
            if (hp>100) { int round=hp>300?10:5; hp=(hp+round/2)/round*round; }
            if(finalAkkha)hp=Math.max(1,hp/5);
            attrs.add("raid_level:"+s.raidLevel);
            attrs.add(s.excludeSalts?"raid_exclude_salts":"raid_assume_salts");
        }
        else
        {
            if(WikiTargetEligibility.isGuardian(m))hp=151+s.coxMining;
            boolean magicDef=id(m,7559,7540,7543,7544,7545,7533,7530,7531,7532,7538,7539,7552,7555,7550,7553);
            boolean single=id(m,7548,7549,7538,7539);
            int baseDef=Math.max(def,magicDef?magic:1);
            int baseOff=Math.max(ranged,magicDef?1:magic);
            int off, defensive;
            if (single)
            {
                int h=Math.max(s.coxCombat,60),stat=Math.max(s.coxHp,55);
                if(s.challenge){h=h*150/100;stat=stat*150/100;}
                hp=Math.max(5,hp*h/126); off=Math.max(1,baseOff*stat/99); defensive=Math.max(1,baseDef*stat/99);
            }
            else
            {
                int n=s.coxParty-1, sqrt=(int)Math.sqrt(n), highestHp=Math.max(55,Math.min(99,55+44*s.coxHp/99));
                off=baseOff*highestHp/99; defensive=baseDef*highestHp/99; hp=hp*Math.max(60,s.coxCombat)/126;
                off=off*(100+sqrt*7+n)/100; defensive=defensive*(100+sqrt+n*7/10)/100;
                hp+=hp*(s.coxParty*50/100);
                if(s.challenge)
                {
                    off=off*150/100;
                    if(!id(m,7568)) hp=hp*150/100;
                    if(id(m,7540,7543,7544,7545)) defensive=defensive*(s.coxParty<4?120:135)/100;
                    else if(!id(m,7568)) defensive=defensive*150/100;
                }
                hp=Math.max(50,Math.min(30000,hp));off=Math.max(50,Math.min(5000,off));defensive=Math.max(50,Math.min(20000,defensive));
            }
            // BGS overflow passes through Strength/Attack before Magic. Preserve
            // the source-scaled offensive levels without adding incoming damage.
            int sourceAttack=DefenceReductionPlan.metadata(m,"npc_attack_level:");
            int sourceStrength=DefenceReductionPlan.metadata(m,"npc_strength_level:");
            int drainOff=scaleDrainOffensive(Math.max(baseOff,Math.max(sourceAttack,sourceStrength)),s,single);
            attrs.removeIf(a->a.startsWith("npc_attack_level:")||a.startsWith("npc_strength_level:"));
            if(sourceAttack>=0)attrs.add("npc_attack_level:"+(sourceAttack==1?1:drainOff));
            if(sourceStrength>=0)attrs.add("npc_strength_level:"+(sourceStrength==1?1:drainOff));
            if(def!=1) def=defensive;
            if(ranged!=1) ranged=off;
            if(magic!=1) magic=magicDef?defensive:off;
            if(id(m,7550,7551,7552,7553,7554,7555))
            {
                int factor=Math.min(s.coxParty-1,50)-3*(Math.min(s.coxParty,50)/8);
                hp=id(m,7550,7552,7553,7555)?600+300*factor:800+400*factor;
                if(id(m,7550,7553)) magic/=2;
            }
        }
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),def,magic,ranged,hp,
            m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),m.getMagicDefence(),m.getLightDefence(),
            m.getStandardDefence(),m.getHeavyDefence(),attrs,m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }
}
