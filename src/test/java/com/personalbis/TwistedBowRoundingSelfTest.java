package com.personalbis;
import java.util.*;
public final class TwistedBowRoundingSelfTest {
 private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 private static void near(double actual,double expected,String why){check(Math.abs(actual-expected)<1e-9,why+": "+actual);}
 private static MonsterDefinition target(int magic,boolean cox){return new MonsterDefinition(3129,"K'ril Tsutsaroth",650,187,magic,1,255,70,80,80,80,80,80,80,cox?Arrays.asList("demon","xerician"):Arrays.asList("demon"),3,"water",30,0);}
 public static void main(String[] args){
  MonsterDefinition kril=target(200,false);
  check(RangedWeaponEffects.twistedPercent(kril,true)==129,"Kril accuracy integer percentage");
  check(RangedWeaponEffects.twistedPercent(kril,false)==191,"Kril damage integer percentage");
  int roll=RangedWeaponEffects.scaleTwisted(118*(254+64),kril,true),defence=(187+9)*(80+64);
  check(roll==48405&&defence==28224,"exact Wiki share rolls");
  double accuracy=WikiAccuracyRoll.normal(roll,defence);
  near(accuracy,0.7084452340618932,"Wiki share accuracy");
  check(RangedWeaponEffects.scaleTwisted(roll,kril,true)==62442,"second Warden P2 scaling truncates independently");
  int max=RangedWeaponEffects.scaleTwisted(43,kril,false);check(max==82,"Wiki share max");
  HitDistribution.AttackDistribution distribution=new HitDistribution.AttackDistribution(Collections.singletonList(HitDistribution.linear(accuracy,0,max).accurateZeroToOne()));
  double dps=distribution.expectedDamage()/3.0,ttk=ExpectedKillTime.seconds(distribution,255,5);
  check(Math.round(dps*1000)==9685&&Math.round(ttk*10)==291,"Wiki shared rounded DPS and TTK");
  // A separate fixture generated from the primary TypeScript tbowScaling function.
  try(java.io.BufferedReader input=new java.io.BufferedReader(new java.io.InputStreamReader(TwistedBowRoundingSelfTest.class.getResourceAsStream("/com/personalbis/tbow-scaling.tsv"),java.nio.charset.StandardCharsets.UTF_8))){
   String line;while((line=input.readLine())!=null){String[] p=line.split("\\t");int magic=Integer.parseInt(p[0]);MonsterDefinition m=target(magic,true);
    check(RangedWeaponEffects.scaleTwisted(37524,m,true)==Integer.parseInt(p[1]),"source accuracy fixture "+magic);
    check(RangedWeaponEffects.scaleTwisted(43,m,false)==Integer.parseInt(p[2]),"source damage fixture "+magic);
   }
  }catch(java.io.IOException ex){throw new RuntimeException(ex);}
  check(RangedWeaponEffects.scaleTwisted(37524,target(500,false),true)==RangedWeaponEffects.scaleTwisted(37524,target(250,false),true),"outside CoX cap");
  check(RangedWeaponEffects.scaleTwisted(37524,target(500,true),true)==RangedWeaponEffects.scaleTwisted(37524,target(350,true),true),"CoX cap");
  System.out.printf(java.util.Locale.ROOT,"PASS integer Tbow stages and primary fixture; Kril max=%d accuracy=%.8f%% DPS=%.8f TTK=%.8f%n",max,accuracy*100,dps,ttk);
 }
}
