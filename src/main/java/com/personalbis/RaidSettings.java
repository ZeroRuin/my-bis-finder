package com.personalbis;

/** Immutable options captured before handing work to the optimizer thread. */
final class RaidSettings
{
    static final RaidSettings DEFAULT = new RaidSettings(99, 126, 1, false, 0, 0, 1, false);
    final int coxMining;
    final int tfaParty;
    final int coxHp, coxCombat, coxParty, raidLevel, pathLevel, toaParty;
    final boolean challenge, excludeSalts;

    RaidSettings(int hp, int combat, int party, boolean cm, int raid, int path, int toaSize, boolean exclude)
    {
        this(hp,combat,party,cm,raid,path,toaSize,exclude,99);
    }

    RaidSettings(int hp,int combat,int party,boolean cm,int raid,int path,int toaSize,boolean exclude,int mining)
    {
        this(hp,combat,party,cm,raid,path,toaSize,exclude,mining,2);
    }

    RaidSettings(int hp,int combat,int party,boolean cm,int raid,int path,int toaSize,boolean exclude,int mining,int tfaSize)
    {
        tfaParty=clamp(tfaSize,2,8);
        coxMining=clamp(mining,1,99);
        coxHp = clamp(hp, 1, 99); coxCombat = clamp(combat, 3, 126); coxParty = clamp(party, 1, 100);
        challenge = cm; raidLevel = clamp(raid, 0, 700); pathLevel = clamp(path, 0, 6);
        toaParty = clamp(toaSize, 1, 8); excludeSalts = exclude;
    }

    static final String CONFIG_KEY="raidSettings";

    String encode()
    {
        return "1|"+coxHp+'|'+coxCombat+'|'+coxParty+'|'+challenge+'|'+raidLevel+'|'+pathLevel+'|'+toaParty+'|'+excludeSalts+'|'+coxMining+'|'+tfaParty;
    }

    static RaidSettings decode(String value)
    {
        if(value==null)return DEFAULT;
        String[] fields=value.split("\\|",-1);
        if((fields.length!=10&&fields.length!=11)||!fields[0].equals("1"))return DEFAULT;
        if(!(fields[4].equals("true")||fields[4].equals("false"))
            ||!(fields[8].equals("true")||fields[8].equals("false")))return DEFAULT;
        try {
            return new RaidSettings(Integer.parseInt(fields[1]),Integer.parseInt(fields[2]),
                Integer.parseInt(fields[3]),Boolean.parseBoolean(fields[4]),Integer.parseInt(fields[5]),
                Integer.parseInt(fields[6]),Integer.parseInt(fields[7]),Boolean.parseBoolean(fields[8]),Integer.parseInt(fields[9]),fields.length==11?Integer.parseInt(fields[10]):2);
        } catch(NumberFormatException ex) { return DEFAULT; }
    }

    private static int clamp(int value, int low, int high) { return Math.max(low, Math.min(high, value)); }

    String key(MonsterDefinition m)
    {
        if(FracturedArchiveRules.isTfa(m))return "tfa:"+tfaParty;
        if (RaidScaling.isCox(m)) return "cox:" + coxHp + ':' + coxCombat + ':' + coxParty + ':' + challenge + ':' + coxMining;
        if (RaidScaling.isToa(m)) return "toa:" + raidLevel + ':' + pathLevel + ':' + toaParty + ':' + excludeSalts;
        return "";
    }
}
