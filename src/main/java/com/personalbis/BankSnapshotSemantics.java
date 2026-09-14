package com.personalbis;
public final class BankSnapshotSemantics {
 private BankSnapshotSemantics(){}
 public static boolean usableName(String name){return name!=null&&!name.trim().isEmpty();}
 public static boolean slayerHeadgearName(String name){
  if(!usableName(name))return false;
  String n=name.toLowerCase();
  return n.contains("slayer helmet")||n.contains("slayer helm")||n.contains("black mask");
 }
}
