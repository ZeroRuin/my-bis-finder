package com.personalbis;
public final class PoweredStaff {
 public final String label; public final int minLevel,speed;
 private final int offset,minHit;
 private PoweredStaff(String l,int ml,int s,int o,int mh){label=l;minLevel=ml;speed=s;offset=o;minHit=mh;}
 public int baseMaxHit(int visibleMagic){
  if("Tumeken's shadow".equals(label)) return Math.max(1,(visibleMagic/3)+1);
  if("Eye of ayak".equals(label)) return Math.max(1,(visibleMagic/3)-6);
  if("Sanguinesti staff".equals(label)) return Math.max(1,visibleMagic/3);
  if("Warped sceptre".equals(label)) return Math.max(1,(8*visibleMagic+96)/37);
  return Math.max(minHit,(visibleMagic/3)+offset);
 }
 public static boolean requiresContext(String raw){String n=raw==null?"":raw.toLowerCase();return n.contains("accursed sceptre")||n.contains("thammaron");}
 public static PoweredStaff forWeapon(String raw){
  if(raw==null)return null;String n=raw.toLowerCase();
  if(n.contains("trident of the swamp"))return new PoweredStaff("Trident of the swamp",78,4,-2,23);
  if(n.contains("trident of the seas"))return new PoweredStaff("Trident of the seas",75,4,-5,20);
  if(n.contains("sanguinesti staff")||n.contains("holy sanguinesti staff"))return new PoweredStaff("Sanguinesti staff",82,4,-1,6);
  if(n.contains("warped sceptre"))return new PoweredStaff("Warped sceptre",62,4,-8,1);
  if(n.equals("bone staff"))return new PoweredStaff("Bone staff",50,4,5,21);
  if(n.equals("tumeken's shadow"))return new PoweredStaff("Tumeken's shadow",85,5,-20,1);
  if(n.equals("eye of ayak"))return new PoweredStaff("Eye of ayak",83,3,-6,21);
  return null;
 }
}
