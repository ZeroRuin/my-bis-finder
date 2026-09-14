package com.personalbis;
public final class MagicSpecialAttackEffects {
 private MagicSpecialAttackEffects(){}
 public static boolean hasSpecial(String weapon){return weapon!=null&&weapon.equalsIgnoreCase("Eye of ayak");}
 public static int energyCost(String weapon){return hasSpecial(weapon)?50:0;}
 public static double accuracyMultiplier(String weapon){return hasSpecial(weapon)?2.0:1.0;}
 public static double maxHitMultiplier(String weapon){return hasSpecial(weapon)?1.30:1.0;}
 public static int attackTicks(String weapon){return hasSpecial(weapon)?5:0;}
 public static String note(String weapon){return hasSpecial(weapon)?"Soul Rend: 50% spec, 2x accuracy, +30% max hit, 5t; successful damage drains equal Magic Defence":"";}
}
