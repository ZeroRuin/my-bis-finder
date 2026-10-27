package com.personalbis;

import java.util.*;

/** Explicit pre-applied drain inputs, in the Wiki calculator's fixed order. */
final class DefenceReductionPlan
{
    enum Type {
        MAUL("Elder maul hits",20,21003,27100), DWH("Dragon warhammer hits",20,13576,26710,28035),
        ARCLIGHT("Arclight hits",20,19675), EMBERLIGHT("Emberlight hits",20,29589),
        TONALZTICS("Tonalztics hits",40,28922,28919), BGS("Bandos godsword damage",10000,11804,20370),
        SEERCULL("Seercull damage",10000,6724), AYAK("Eye of Ayak damage",10000,31113),
        ACCURSED("Accursed sceptre",1,27665,27679), VULNERABILITY("Vulnerability (10%)",1);
        final String label;final int maximum;final int[] ids;
        Type(String label,int maximum,int... ids){this.label=label;this.maximum=maximum;this.ids=ids;}
        boolean matches(int id){for(int value:ids)if(value==id)return true;return false;}
    }
    static final DefenceReductionPlan OFF=new DefenceReductionPlan(Collections.emptyMap());
    private final Map<Type,Integer> amounts;
    DefenceReductionPlan(Map<Type,Integer> values){
        EnumMap<Type,Integer> copy=new EnumMap<>(Type.class);
        for(Type t:Type.values()){int value=Math.max(0,Math.min(t.maximum,values.getOrDefault(t,0)));if(value>0)copy.put(t,value);}
        if(copy.containsKey(Type.ACCURSED))copy.remove(Type.VULNERABILITY);
        amounts=Collections.unmodifiableMap(copy);
    }
    int amount(Type type){return amounts.getOrDefault(type,0);}
    DefenceReductionPlan with(Type type,int value){
        Map<Type,Integer> next=new EnumMap<>(Type.class);next.putAll(amounts);
        if(value>0&&type==Type.VULNERABILITY)next.remove(Type.ACCURSED);
        if(value>0&&type==Type.ACCURSED)next.remove(Type.VULNERABILITY);
        next.put(type,value);return new DefenceReductionPlan(next);
    }
    DefenceReductionPlan revalidate(Set<Type> available){Map<Type,Integer> next=new EnumMap<>(Type.class);for(Type t:available)next.put(t,amount(t));return new DefenceReductionPlan(next);}
    String encode(){StringBuilder out=new StringBuilder("2");for(Type t:Type.values())if(amount(t)>0)out.append('|').append(t.name()).append('=').append(amount(t));return out.toString();}
    static DefenceReductionPlan decode(String text){
        if("DWH".equals(text))return OFF.with(Type.DWH,1);
        if("MAUL".equals(text))return OFF.with(Type.MAUL,1);
        if(text==null||!text.startsWith("2"))return OFF;
        Map<Type,Integer> values=new EnumMap<>(Type.class);
        try{String[] parts=text.split("\\|");if(!parts[0].equals("2"))return OFF;for(int i=1;i<parts.length;i++){String[] pair=parts[i].split("=",-1);if(pair.length!=2)return OFF;values.put(Type.valueOf(pair[0]),Integer.parseInt(pair[1]));}return new DefenceReductionPlan(values);}
        catch(IllegalArgumentException ex){return OFF;}
    }
    static Set<Type> available(List<BankItem> items,int magic){
        Set<Type> result=EnumSet.noneOf(Type.class);long water=0,earth=0,soul=0;
        if(items!=null)for(BankItem item:items)if(item!=null&&item.getQuantity()>0){
            for(Type type:Type.values())if(type.matches(item.getItemId()))result.add(type);
            if(item.getItemId()==555)water+=item.getQuantity();if(item.getItemId()==557)earth+=item.getQuantity();if(item.getItemId()==566)soul+=item.getQuantity();
        }
        // Conservative supplies check: loose water/earth/soul only, no inferred pouch/staff contents.
        if(magic>=66&&water>=5&&earth>=5&&soul>=1)result.add(Type.VULNERABILITY);
        return result;
    }
    static int metadata(MonsterDefinition m,String prefix){for(String a:m.getAttributes())if(a.startsWith(prefix))try{return Integer.parseInt(a.substring(prefix.length()));}catch(NumberFormatException ex){return -1;}return -1;}
    static DefenceReductionPlan appliedTo(MonsterDefinition target){
        if(target!=null)for(String attribute:target.getAttributes())
            if(attribute.startsWith("manual_defence_applied:"))return decode(attribute.substring("manual_defence_applied:".length()));
        return OFF;
    }
    MonsterDefinition apply(MonsterDefinition m,Set<Type> eligible){
        if(m==null||amounts.isEmpty())return m;
        DefenceReductionPlan plan=revalidate(eligible);
        int originalDef=VardorvisRules.supported(m)?VardorvisRules.startingDefence(m):m.getDefenceLevel(),def=originalDef,magic=m.getMagicLevel(),ranged=m.getRangedLevel(),magicDef=m.getMagicDefence();
        int attack=metadata(m,"npc_attack_level:"),strength=metadata(m,"npc_strength_level:");
        int baseAttack=attack,baseStrength=strength;
        int floor=Math.min(def,ManualDefenceReduction.floor(m));
        if(plan.amount(Type.ACCURSED)>0){def=Math.max(floor,def*17/20);magic=magic*17/20;}
        else if(plan.amount(Type.VULNERABILITY)>0)def=Math.max(floor,def*9/10);
        for(int i=0;i<plan.amount(Type.MAUL);i++)def=Math.max(floor,def-def*35/100);
        for(int i=0;i<plan.amount(Type.DWH);i++)def=Math.max(floor,def-def*3/10);
        for(Type t:new Type[]{Type.ARCLIGHT,Type.EMBERLIGHT}){
            int numerator=m.hasAttribute("demon")?(t==Type.EMBERLIGHT?3:2):1;
            int count=plan.amount(t);
            def=Math.max(floor,def-count*(originalDef*numerator/20+1));
            if(attack>=0)attack=Math.max(0,attack-count*(baseAttack*numerator/20+1));
            if(strength>=0)strength=Math.max(0,strength-count*(baseStrength*numerator/20+1));
        }
        for(int i=0;i<plan.amount(Type.TONALZTICS);i++)def=Math.max(floor,def-magic/8);
        magic=Math.max(0,magic-plan.amount(Type.SEERCULL));
        int excess=plan.amount(Type.BGS);
        int after=Math.max(floor,def-excess);excess=after>0?0:Math.max(0,excess-def);def=after;
        boolean unknownOverflow=excess>0&&(attack<0||strength<0);
        if(!unknownOverflow&&excess>0){int loss=Math.min(strength,excess);strength-=loss;excess-=loss;loss=Math.min(attack,excess);attack-=loss;excess-=loss;loss=Math.min(magic,excess);magic-=loss;excess-=loss;ranged=Math.max(0,ranged-excess);}
        if(magicDef>0)magicDef=Math.max(0,magicDef-plan.amount(Type.AYAK));
        List<String> attrs=new ArrayList<>(m.getAttributes());attrs.removeIf(a->a.startsWith("manual_defence_note:")||a.startsWith("manual_defence_applied:")||a.startsWith("npc_attack_level:")||a.startsWith("npc_strength_level:"));
        if(attack>=0)attrs.add("npc_attack_level:"+attack);if(strength>=0)attrs.add("npc_strength_level:"+strength);
        StringBuilder note=new StringBuilder("Manual pre-applied drains: ");
        for(Type t:Type.values())if(amount(t)>0)note.append(t.label).append('=').append(amount(t)).append(plan.amount(t)==0?" (not applied: unavailable/excluded/unusable)":"").append("; ");
        note.append("Defence ").append(originalDef).append(" → ").append(def).append(", Magic ").append(m.getMagicLevel()).append(" → ").append(magic).append(", Magic defence bonus ").append(m.getMagicDefence()).append(" → ").append(magicDef).append(". Held fixed for estimate; no recovery, special damage, setup time or energy. Hits/amounts are assumed successful, not predicted. Vulnerability is standard 10%; enhanced Tome effect excluded.");
        if(unknownOverflow)note.append(" BGS excess beyond Defence not modelled: intermediate NPC stats unknown.");
        attrs.add("manual_defence_note:"+note);
        attrs.add("manual_defence_applied:"+plan.encode());
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),def,magic,ranged,m.getHitpoints(),m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),magicDef,m.getLightDefence(),m.getStandardDefence(),m.getHeavyDefence(),attrs,m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }
}
