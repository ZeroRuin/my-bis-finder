package com.personalbis;

import java.util.*;

/** Ranked name search. Raid prefixes constrain results before fuzzy matching. */
final class TargetSearch
{
    private TargetSearch() {}
    private static final Map<String,String> ALIASES=new LinkedHashMap<>();
    static {
        ALIASES.put("kbd","king black dragon"); ALIASES.put("kq","kalphite queen");
        ALIASES.put("corp","corporeal beast"); ALIASES.put("cerb","cerberus");
        ALIASES.put("vork","vorkath"); ALIASES.put("huey","the hueycoatl");
        ALIASES.put("jad","tztok jad"); ALIASES.put("zuk","tzkal zuk");
        ALIASES.put("maiden","the maiden of sugadinti");
        ALIASES.put("bloat","pestilent bloat"); ALIASES.put("verzik","verzik vitur");
        ALIASES.put("olm","great olm"); ALIASES.put("baba","ba ba");
    }
    static String normalize(String s) {
        return s==null?"":s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();
    }
    static List<MonsterDefinition> search(List<MonsterDefinition> source,String query,int limit) {
        String q=normalize(query),raid="";
        for(String prefix:new String[]{"the fractured archive","fractured archive","tfa","archive","tombs of amascut","chambers of xeric","theatre of blood","toa","cox","tob","tombs","amascut","chambers","xeric","theatre","blood"}) {
            if(q.equals(prefix)||q.startsWith(prefix+" ")) {
                raid=prefix.equals("tfa")||prefix.contains("archive")?"tfa":prefix.equals("toa")||prefix.startsWith("tombs")||prefix.equals("amascut")?"toa":prefix.equals("cox")||prefix.startsWith("chambers")||prefix.equals("xeric")?"cox":"tob";
                q=q.substring(prefix.length()).trim(); break;
            }
        }
        q=ALIASES.getOrDefault(q,q);
        final String term=q;
        Map<MonsterDefinition,Integer> scores=new IdentityHashMap<>();
        for(MonsterDefinition m:source) {
            if(!raid.isEmpty()&&!inRaid(m,raid))continue;
            int score=score(m,term);
            if(score>=0)scores.put(m,score);
        }
        List<MonsterDefinition> out=new ArrayList<>(scores.keySet());
        out.sort(Comparator.comparingInt((MonsterDefinition m)->scores.get(m))
            .thenComparing(MonsterDefinition::getName,String.CASE_INSENSITIVE_ORDER)
            .thenComparing(MonsterDefinition::getVersion,String.CASE_INSENSITIVE_ORDER)
            .thenComparingInt(MonsterDefinition::getCombatLevel).thenComparingInt(MonsterDefinition::getId));
        return new ArrayList<>(out.subList(0,Math.min(out.size(),limit<=0?out.size():limit)));
    }
    static boolean inRaid(MonsterDefinition m,String raid) {
        if(raid.equals("tfa"))return FracturedArchiveRules.isTfa(m);
        if(raid.equals("cox"))return RaidScaling.isCox(m);
        if(raid.equals("toa"))return RaidScaling.isToa(m)||m.getId()>=11709&&m.getId()<=11718
            ||m.getId()==11781||m.getId()==11727||m.getId()==11697||m.getId()==11723;
        return TheatreOfBloodRules.isBoss(m.getName())||m.getName().startsWith("Nylocas ")
            ||m.getName().equals("Blood spawn");
    }
    private static int score(MonsterDefinition m,String query) {
        if(query.isEmpty())return 0;
        String name=normalize(m.getName()),full=name+" "+normalize(m.getVersion());
        if(name.equals(query))return 0;
        if(name.startsWith(query))return 1;
        if(full.contains(query))return 2;
        String[] tokens=query.split(" ");
        boolean all=true;
        for(String token:tokens)if(!full.contains(token)){all=false;break;}
        if(all)return 3;
        // Short queries stay literal to avoid flooding the picker with unrelated targets.
        if(query.length()<4||query.length()>80)return -1;
        int allowance=query.length()>=6?2:1;
        int distance=distance(query,name);
        for(String word:name.split(" "))distance=Math.min(distance,distance(query,word));
        return distance<=allowance?10+distance:-1;
    }
    /** Optimal-string-alignment distance includes adjacent transpositions. */
    static int distance(String a,String b) {
        int[][] d=new int[a.length()+1][b.length()+1];
        for(int i=0;i<=a.length();i++)d[i][0]=i;
        for(int j=0;j<=b.length();j++)d[0][j]=j;
        for(int i=1;i<=a.length();i++)for(int j=1;j<=b.length();j++) {
            d[i][j]=Math.min(d[i-1][j]+1,Math.min(d[i][j-1]+1,d[i-1][j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1)));
            if(i>1&&j>1&&a.charAt(i-1)==b.charAt(j-2)&&a.charAt(i-2)==b.charAt(j-1))d[i][j]=Math.min(d[i][j],d[i-2][j-2]+1);
        }
        return d[a.length()][b.length()];
    }
}
