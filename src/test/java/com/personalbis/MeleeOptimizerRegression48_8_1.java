package com.personalbis;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

final class MeleeOptimizerRegression48_8_1
{
    private static String source()
    {
        try
        {
            return new String(Files.readAllBytes(Paths.get(
                "src/main/java/com/personalbis/MeleeLoadoutOptimizer.java")),StandardCharsets.UTF_8);
        }
        catch(Exception ex)
        {
            return "";
        }
    }

    static boolean neutralCompletionPresent()
    {
        String s=source();
        return s.contains("completeNeutralSlots(best,ranked,style,monster,onSlayerTask,weapon,context)");
    }

    static boolean neverSacrificesDps()
    {
        String s=source();
        return s.contains("if(Double.compare(r.getDps(),current.getDps())<0)continue;");
    }
}
