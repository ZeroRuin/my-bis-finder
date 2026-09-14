package com.personalbis;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
final class SupplyRegression49_1 {
 private static String source(){
  try{return new String(Files.readAllBytes(Paths.get("src/main/java/com/personalbis/SimpleSupplyRecommender.java")),StandardCharsets.UTF_8);}
  catch(Exception e){return "";}
 }
 static boolean fullRuneStack(){
  String s=source();
  return s.contains("x.getQuantity(),x.getName(),reason")
      && s.contains("addFullStack(out,bank,e.getKey()");
 }
 static boolean noCastTarget(){
  return !source().contains("RUNE_CASTS");
 }
 private SupplyRegression49_1(){}
}
