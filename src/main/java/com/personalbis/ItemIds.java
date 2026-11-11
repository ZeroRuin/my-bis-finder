package com.personalbis;

/** Item ids the plugin refers to directly. */
public final class ItemIds
{
    private ItemIds() {}

    /** Deadman-only cape; not supported. */
    public static final int DEADMAN_CAPE = 34607;
    /** Uncharged, so it has no built-in attack. */
    static final int ZORYAS_TOME_UNCHARGED = 34727;
    static final int OBLIGATOR_CHARGED = 34715;
    static final int OBLIGATOR_UNCHARGED = 34717;

    private static final int[] BROKEN_SET_PIECES = {4884, 4890, 4896, 4902, 29067, 29070, 29073};

    /** Dharok's pieces at 0 charge and broken Blood Moon pieces. */
    static boolean isBrokenSetPiece(int id)
    {
        for (int broken : BROKEN_SET_PIECES) if (broken == id) return true;
        return false;
    }
}
