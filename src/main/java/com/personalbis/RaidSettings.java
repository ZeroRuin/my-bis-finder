package com.personalbis;

/** Immutable options captured before handing work to the optimizer thread. */
final class RaidSettings
{
    static final RaidSettings DEFAULT = new RaidSettings(99, 126, 1, false, 0, 0, 1, false);
    final int coxHp, coxCombat, coxParty, raidLevel, pathLevel, toaParty;
    final boolean challenge, excludeSalts;

    RaidSettings(int hp, int combat, int party, boolean cm, int raid, int path, int toaSize, boolean exclude)
    {
        coxHp = clamp(hp, 1, 99); coxCombat = clamp(combat, 3, 126); coxParty = clamp(party, 1, 100);
        challenge = cm; raidLevel = clamp(raid, 0, 700); pathLevel = clamp(path, 0, 6);
        toaParty = clamp(toaSize, 1, 8); excludeSalts = exclude;
    }

    private static int clamp(int value, int low, int high) { return Math.max(low, Math.min(high, value)); }

    String key(MonsterDefinition m)
    {
        if (RaidScaling.isCox(m)) return "cox:" + coxHp + ':' + coxCombat + ':' + coxParty + ':' + challenge;
        if (RaidScaling.isToa(m)) return "toa:" + raidLevel + ':' + pathLevel + ':' + toaParty + ':' + excludeSalts;
        return "";
    }
}
