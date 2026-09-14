package com.personalbis;
import net.runelite.api.Skill;
public final class MagicPrayer {
 public final String name; public final double accuracy; public final double damage;
 private MagicPrayer(String n,double a,double d){name=n;accuracy=a;damage=d;}
 static MagicPrayer forState(int p,boolean augury,boolean vigour){
  if(p>=77&&augury)return new MagicPrayer("Augury",1.25,0.04);
  if(p>=63&&vigour)return new MagicPrayer("Mystic Vigour",1.18,0.03);
  if(p>=45)return new MagicPrayer("Mystic Might",1.15,0.02);
  if(p>=27)return new MagicPrayer("Mystic Lore",1.10,0.01);
  if(p>=9)return new MagicPrayer("Mystic Will",1.05,0.00);
  return new MagicPrayer("None",1.00,0.00);
 }
 public static MagicPrayer best(AccountSnapshot a){return forState(a.real(Skill.PRAYER),a.isAuguryUnlocked(),a.isMysticVigourUnlocked());}
}