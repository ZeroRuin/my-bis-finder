package com.personalbis;

import java.util.*;


/** Explicit player assumptions about internal darts, never detected item contents. */
public final class BlowpipeAmmoPlan
{
    public static final String CONFIG_KEY="blowpipeAmmo";
    private BlowpipeAmmoPlan() {}

    public enum Pipe
    {
        TOXIC("Toxic blowpipe",9,12924,12926),
        BLAZING("Blazing blowpipe",9,28687,28688),
        CAMPHOR("Camphor blowpipe",5,31575,31577),
        IRONWOOD("Ironwood blowpipe",6,31579,31581),
        ROSEWOOD("Rosewood blowpipe",7,31583,31585),
        DRYGORE("Drygore blowpipe",9,30373,30374);
        public final String label;
        private final int maxTier;
        private final int[] ids;
        Pipe(String label,int maxTier,int... ids){this.label=label;this.maxTier=maxTier;this.ids=ids;}
        public boolean accepts(Dart dart){return dart!=null&&dart!=Dart.UNKNOWN&&dart.tier<=maxTier;}
    }

    public enum Dart
    {
        UNKNOWN("Not selected",0,0), BRONZE("Bronze dart",1,806), IRON("Iron dart",2,807),
        STEEL("Steel dart",3,808), BLACK("Black dart",4,3093), MITHRIL("Mithril dart",5,809),
        ADAMANT("Adamant dart",6,810), RUNE("Rune dart",7,811),
        AMETHYST("Amethyst dart",8,25849), DRAGON("Dragon dart",9,11230);
        public final String label;
        public final int itemId;
        private final int tier;
        Dart(String label,int tier,int itemId){this.label=label;this.tier=tier;this.itemId=itemId;}
        @Override public String toString(){return label;}
    }

    public static Pipe pipe(int itemId)
    {
        for(Pipe pipe:Pipe.values())for(int id:pipe.ids)if(itemId==id)return pipe;
        return null;
    }

    public static Set<Pipe> scannedPipes(List<BankItem> bank)
    {
        Set<Pipe> found=EnumSet.noneOf(Pipe.class);
        if(bank!=null)for(BankItem item:bank)if(item!=null&&item.getQuantity()>0){
            Pipe p=pipe(item.getItemId());if(p!=null)found.add(p);
        }
        return found;
    }

    public static List<Dart> options(Pipe pipe)
    {
        List<Dart> options=new ArrayList<>();options.add(Dart.UNKNOWN);
        for(Dart dart:Dart.values())if(pipe.accepts(dart))options.add(dart);
        return Collections.unmodifiableList(options);
    }

    public static Map<Pipe,Dart> decode(String raw)
    {
        Map<Pipe,Dart> result=new EnumMap<>(Pipe.class);
        if(raw!=null)for(String entry:raw.split(",")){
            String[] parts=entry.split(":");if(parts.length!=2)continue;
            try{Pipe pipe=Pipe.valueOf(parts[0]);Dart dart=Dart.valueOf(parts[1]);
                if(pipe.accepts(dart))result.put(pipe,dart);
            }catch(IllegalArgumentException ignored){}
        }
        return result;
    }

    public static String encode(Map<Pipe,Dart> selections)
    {
        StringJoiner result=new StringJoiner(",");
        for(Pipe pipe:Pipe.values()){Dart dart=selections.get(pipe);
            if(pipe.accepts(dart))result.add(pipe.name()+":"+dart.name());}
        return result.toString();
    }

}
