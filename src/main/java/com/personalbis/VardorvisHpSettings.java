package com.personalbis;

/** Manual planning HP, independent of the variant's fixed maximum/scaling range. */
final class VardorvisHpSettings
{
    static final String CONFIG_KEY="vardorvisHp";
    static final VardorvisHpSettings DEFAULT=new VardorvisHpSettings(500,700,1400);
    final int quest,postQuest,awakened;
    VardorvisHpSettings(int quest,int postQuest,int awakened){
        this.quest=clamp(quest,500);this.postQuest=clamp(postQuest,700);this.awakened=clamp(awakened,1400);
    }
    private static int clamp(int hp,int maximum){return Math.max(1,Math.min(maximum,hp));}
    int hp(MonsterDefinition target){
        return "Quest".equals(target.getVersion())?quest:"Awakened".equals(target.getVersion())?awakened:postQuest;
    }
    VardorvisHpSettings with(MonsterDefinition target,int hp){
        return "Quest".equals(target.getVersion())?new VardorvisHpSettings(hp,postQuest,awakened)
            :"Awakened".equals(target.getVersion())?new VardorvisHpSettings(quest,postQuest,hp)
            :new VardorvisHpSettings(quest,hp,awakened);
    }
    String encode(){return "1|"+quest+"|"+postQuest+"|"+awakened;}
    static VardorvisHpSettings decode(String text){
        if(text==null)return DEFAULT;
        String[] parts=text.split("\\|",-1);
        if(parts.length!=4||!parts[0].equals("1"))return DEFAULT;
        try{return new VardorvisHpSettings(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));}
        catch(NumberFormatException ex){return DEFAULT;}
    }
    MonsterDefinition apply(MonsterDefinition m){
        if(!VardorvisRules.supported(m))return m;
        return atHp(m,hp(m));
    }
    static MonsterDefinition atHp(MonsterDefinition m,int hp){
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),m.getDefenceLevel(),m.getMagicLevel(),m.getRangedLevel(),hp,
            m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),m.getMagicDefence(),m.getLightDefence(),m.getStandardDefence(),m.getHeavyDefence(),
            m.getAttributes(),m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }
}
