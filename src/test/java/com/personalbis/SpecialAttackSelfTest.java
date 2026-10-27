package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.*;
import java.util.*;
import net.runelite.api.Skill;

/** Formula boundaries and owned-gear selection, independent of optimizer ranking scores. */
public final class SpecialAttackSelfTest
{
    private static void check(boolean x,String m){if(!x)throw new AssertionError(m);}
    private static void close(double a,double b,String m){check(Math.abs(a-b)<1e-8,m+": "+a+" != "+b);}
    private static EquipmentCandidate item(int id,AttackStyle style){
        WikiEquipment w=WikiEquipmentEngine.canonicalItem(id);
        EquipmentSlot slot=w.slot.equals("2h")?EquipmentSlot.WEAPON:EquipmentSlot.valueOf(w.slot.toUpperCase(Locale.ROOT));
        return new EquipmentCandidate(new BankItem(id,1,w.name),slot,0,
            style==AttackStyle.MELEE_CRUSH?w.offensive.crush:style==AttackStyle.MELEE_SLASH?w.offensive.slash:w.offensive.stab,
            w.bonuses.str,w.offensive.ranged,w.bonuses.ranged_str,w.offensive.magic,(float)(w.bonuses.magic_str/1000.0),
            w.bonuses.prayer,w.speed,w.isTwoHanded,w.category,RequirementResult.usable("fixture"));
    }
    public static void main(String[] args)throws Exception{
        for(MagicSpell spell:MagicSpell.combat())check(SpellSpriteIds.forSpell(spell.name)>=0,"Combat spell icon: "+spell.name);
        check(SpellSpriteIds.forSpell("Fire Surge")==net.runelite.api.SpriteID.SPELL_FIRE_SURGE,"Explicit standard spell sprite");
        check(SpellSpriteIds.forSpell("Ice Barrage")==net.runelite.api.SpriteID.SPELL_ICE_BARRAGE,"Explicit ancient spell sprite");
        check(SpellSpriteIds.forSpell("Dark Demonbane + Mark of Darkness")==net.runelite.api.SpriteID.SPELL_DARK_DEMONBANE,"Marked spell keeps base icon");
        check(SpellSpriteIds.forSpell("Tumeken's shadow")==-1&&SpellSpriteIds.forSpell(null)==-1,"Powered and missing spell fallback");
        close(SpecialAttackRules.melee("Dragon dagger",40,1,1,1).expectedDamage(),46+2.0/47,"DDS two independent 46-max hits, including accurate zero");
        close(SpecialAttackRules.melee("Abyssal dagger",40,0,0,1).expectedDamage(),0,"Abyssal dagger cannot land second hit after miss");
        close(SpecialAttackRules.melee("Voidwaker",40,0,0,1).expectedDamage(),40,"Voidwaker ignores accuracy and rolls 20..60");
        check(SpecialAttackRules.melee("Osmumten's fang",40,1,1,1).max()==40,"Fang spec restores full maximum");
        close(SpecialAttackRules.melee("Granite hammer",40,0,0,1).expectedDamage(),5,"Granite hammer flat five even on miss");
        close(SpecialAttackRules.claws(0,40).expectedDamage(),4.0/3,"Claw all-fail chip damage");
        for(double accuracy:new double[]{0,.25,.9,1}){
            HitDistribution.AttackDistribution d=SpecialAttackRules.claws(accuracy,40);
            double mass=0;for(HitDistribution.WeightedHit h:d.dists().get(0).hits())mass+=h.probability;
            close(mass,1,"Claw distribution normalized");
        }
        WikiDataPack pack=new WikiDataPack(new Gson());
        MonsterDefinition target=MonsterDatabase.catalogue(pack.getMonsters()).stream().filter(m->m.getName().equals("Vorkath")&&m.getVersion().equals("Post-quest")).findFirst().get();
        EquipmentCandidate ruby=item(9242,AttackStyle.RANGED),diamond=item(9243,AttackStyle.RANGED);
        close(SpecialBoltRules.distribution(ruby,target,99,0,40,1,false,false).expectedHit(),110,"ZCB accurate ruby forces capped proc");
        close(SpecialBoltRules.distribution(ruby,target,99,0,40,0,false,false).expectedHit(),6.6,"ZCB ruby can ordinary-proc on missed accuracy roll");
        close(SpecialBoltRules.distribution(diamond,target,99,0,40,1,false,false).expectedHit(),25+1.0/51,"ZCB diamond forced effect uses 126 percent max");
        AccountSnapshot account=new AccountSnapshot(null){@Override public int real(Skill s){return 99;}@Override public int boosted(Skill s){return 99;}};
        Map<AttackStyle,Map<EquipmentSlot,List<EquipmentCandidate>>> owned=new EnumMap<>(AttackStyle.class);
        for(AttackStyle style:new AttackStyle[]{AttackStyle.MELEE_STAB,AttackStyle.MELEE_SLASH,AttackStyle.MELEE_CRUSH}){
            Map<EquipmentSlot,List<EquipmentCandidate>> slots=new EnumMap<>(EquipmentSlot.class);
            slots.put(EquipmentSlot.WEAPON,Arrays.asList(item(1215,style),item(4151,style)));owned.put(style,slots);
        }
        Map<EquipmentSlot,EquipmentCandidate> worn=new EnumMap<>(EquipmentSlot.class);worn.put(EquipmentSlot.WEAPON,item(4151,AttackStyle.MELEE_SLASH));
        Map<EquipmentSlot,EquipmentCandidate> original=new EnumMap<>(worn);
        CombatResult ordinaryBefore=new MeleeCombatCalculator(account).calculate(worn,AttackStyle.MELEE_SLASH,target,false);
        SpecWeaponRecommendation r=new SpecWeaponRecommender(account).best(Collections.unmodifiableMap(worn),owned,AttackStyle.MELEE_SLASH,target,false);
        check(worn.equals(original),"Special recommendation cannot replace ordinary equipment");
        CombatResult ordinaryAfter=new MeleeCombatCalculator(account).calculate(worn,AttackStyle.MELEE_SLASH,target,false);
        close(ordinaryAfter.getDps(),ordinaryBefore.getDps(),"Special recommendation leaves ordinary DPS unchanged");
        close(ordinaryAfter.getExpectedTtkSeconds(),ordinaryBefore.getExpectedTtkSeconds(),"Special recommendation leaves ordinary TTK unchanged");
        check(ordinaryAfter.getMaxHit()==ordinaryBefore.getMaxHit(),"Special recommendation leaves ordinary max hit unchanged");
        check(r!=null&&r.weapon.getItem().getItemId()==1215,"Owned dagger beats whip direct spec");
        check(new SpecWeaponRecommender(account).best(worn,owned,AttackStyle.MELEE_SLASH,null,false)!=null,"Generic target spec does not require monster fields");
        close(r.dps,r.damage/(r.ticks*.6),"Burst score uses special attack interval");
        check(new SpecWeaponRecommender(account).best(worn,Collections.emptyMap(),AttackStyle.MELEE_SLASH,target,false)==null,"No unowned weapon fabricated");
        javax.swing.SwingUtilities.invokeAndWait(()->{
            LoadoutActionRow row=new LoadoutActionRow();row.setSize(208,50);row.doLayout();
            check(row.spec.getWidth()==50&&row.spell.getWidth()==50&&row.style.getWidth()==100,"Two single tiles and double-width style");
        });
        System.out.println("SpecialAttackSelfTest passed");
    }
}
