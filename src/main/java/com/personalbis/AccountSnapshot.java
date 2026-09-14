package com.personalbis;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

@Singleton
public class AccountSnapshot
{
    private final Client client;
    private final Map<Skill, Integer> real = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> boosted = new EnumMap<>(Skill.class);
    // Permanent prayer unlock varbits (server-backed account state).
    private static final int RIGOUR_UNLOCKED_VARBIT = 5451;
    private static final int AUGURY_UNLOCKED_VARBIT = 5452;
    private static final int DEADEYE_UNLOCKED_VARBIT = 16097;
    private static final int MYSTIC_VIGOUR_UNLOCKED_VARBIT = 16098;
    // RuneLite Varbits.CAMELOT_TRAINING_ROOM_STATUS: 8 = Knight Waves complete.
    private static final int CAMELOT_TRAINING_ROOM_STATUS_VARBIT = 3909;
    // RuneLite Varbits.DIARY_KANDARIN_HARD. Completion grants +10% enchanted-bolt proc chance.
    private static final int KANDARIN_HARD_DIARY_VARBIT = 4477;
    private boolean rigourUnlocked, auguryUnlocked, deadeyeUnlocked, mysticVigourUnlocked, chivalryUnlocked, pietyUnlocked;
    private boolean ancientMagicksUnlocked, kingdomDividedUnlocked, kandarinHardCompleted;

    @Inject
    public AccountSnapshot(Client client)
    {
        this.client = client;
    }

    public void refresh()
    {
        if (client.getGameState() != GameState.LOGGED_IN) return;
        for (Skill skill : Skill.values())
        {
            real.put(skill, client.getRealSkillLevel(skill));
            boosted.put(skill, client.getBoostedSkillLevel(skill));
        }
        rigourUnlocked = client.getVarbitValue(RIGOUR_UNLOCKED_VARBIT) > 0;
        auguryUnlocked = client.getVarbitValue(AUGURY_UNLOCKED_VARBIT) > 0;
        deadeyeUnlocked = client.getVarbitValue(DEADEYE_UNLOCKED_VARBIT) > 0;
        mysticVigourUnlocked = client.getVarbitValue(MYSTIC_VIGOUR_UNLOCKED_VARBIT) > 0;
        int knightWaves = client.getVarbitValue(CAMELOT_TRAINING_ROOM_STATUS_VARBIT);
        chivalryUnlocked = knightWaves >= 8;
        pietyUnlocked = knightWaves >= 8;
        ancientMagicksUnlocked = questFinished("DESERT_TREASURE_I");
        kingdomDividedUnlocked = questFinished("A_KINGDOM_DIVIDED");
        kandarinHardCompleted = client.getVarbitValue(KANDARIN_HARD_DIARY_VARBIT) > 0;
    }

    public int real(Skill skill) { return real.getOrDefault(skill, 0); }
    public int boosted(Skill skill) { return boosted.getOrDefault(skill, real(skill)); }
    public boolean isRigourUnlocked() { return rigourUnlocked; }
    public boolean isAuguryUnlocked() { return auguryUnlocked; }
    public boolean isDeadeyeUnlocked() { return deadeyeUnlocked; }
    public boolean isMysticVigourUnlocked() { return mysticVigourUnlocked; }
    public boolean isChivalryUnlocked() { return chivalryUnlocked; }
    public boolean isPietyUnlocked() { return pietyUnlocked; }
    public boolean isAncientMagicksUnlocked() { return ancientMagicksUnlocked; }
    public boolean isKingdomDividedUnlocked() { return kingdomDividedUnlocked; }
    public boolean isKandarinHardCompleted() { return kandarinHardCompleted; }

    private boolean questFinished(String enumName)
    {
        try { return Quest.valueOf(enumName).getState(client) == QuestState.FINISHED; }
        catch (IllegalArgumentException ex) { return false; }
    }

    public String recommendedPrayer(AttackStyle style)
    {
        int p = real(Skill.PRAYER);
        switch (style)
        {
            case MELEE_STAB:
            case MELEE_SLASH:
            case MELEE_CRUSH:
                if (p >= 70 && pietyUnlocked) return "Piety";
                if (p >= 60 && chivalryUnlocked) return "Chivalry";
                if (p >= 34) return "Incredible Reflexes + Ultimate Strength";
                if (p >= 31) return "Ultimate Strength";
                return "Best available melee prayer";
            case RANGED:
                if (p >= 74 && rigourUnlocked) return "Rigour";
                if (p >= 62 && deadeyeUnlocked) return "Deadeye";
                if (p >= 44) return "Eagle Eye";
                if (p >= 26) return "Hawk Eye";
                return "Sharp Eye";
            case MAGIC:
                if (p >= 77 && auguryUnlocked) return "Augury";
                if (p >= 63 && mysticVigourUnlocked) return "Mystic Vigour";
                if (p >= 45) return "Mystic Might";
                if (p >= 27) return "Mystic Lore";
                return "Mystic Will";
            default:
                return "None";
        }
    }
}
