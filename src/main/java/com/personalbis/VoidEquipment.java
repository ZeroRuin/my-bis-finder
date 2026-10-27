package com.personalbis;

/** Exact Void name recognition, including the supported ornament-kit suffix. */
final class VoidEquipment
{
    private VoidEquipment() {}
    static boolean matches(String actual,String expected) {
        return actual!=null&&(actual.equalsIgnoreCase(expected)
            ||actual.equalsIgnoreCase(expected+" (or)"));
    }
}
