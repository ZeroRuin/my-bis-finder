package com.personalbis;

import com.google.gson.Gson;
import com.personalbis.wiki.*;
import java.util.*;
import net.runelite.api.Skill;

/** Independent boundary vectors plus production-path checks for the combat/search update. */
public final class CombatSearchSelfTest
{
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    private static void close(double actual,double expected,String message){check(Math.abs(actual-expected)<1e-9,message+": "+actual+" != "+expected);}
    private static MonsterDefinition find(List<MonsterDefinition> all,String name,String version){
        return all.stream().filter(m->m.getName().equals(name)&&m.getVersion().equals(version)).findFirst().orElseThrow(()->new AssertionError(name+version));
    }
    private static EquipmentCandidate item(int id,AttackStyle style){
        WikiEquipment w=WikiEquipmentEngine.canonicalItem(id);
        check(w!=null,"Fixture item "+id);
        EquipmentSlot slot=w.slot.equals("2h")?EquipmentSlot.WEAPON:EquipmentSlot.valueOf(w.slot.equals("ammo")?"AMMO":w.slot.toUpperCase(java.util.Locale.ROOT));
        return new EquipmentCandidate(new BankItem(id,1,w.name),slot,0,
            style==AttackStyle.MELEE_CRUSH?w.offensive.crush:style==AttackStyle.MELEE_SLASH?w.offensive.slash:w.offensive.stab,
            w.bonuses.str,w.offensive.ranged,w.bonuses.ranged_str,w.offensive.magic,(float)(w.bonuses.magic_str/1000.0),
            w.bonuses.prayer,w.speed,w.isTwoHanded,w.category,RequirementResult.usable("fixture"));
    }
    private static Map<EquipmentSlot,EquipmentCandidate> gear(AttackStyle style,int... ids){
        Map<EquipmentSlot,EquipmentCandidate> out=new EnumMap<>(EquipmentSlot.class);
        for(int id:ids){EquipmentCandidate c=item(id,style);out.put(c.getSlot(),c);}return out;
    }
    public static void main(String[] args){
        WikiDataPack pack=new WikiDataPack(new Gson());
        List<MonsterDefinition> all=MonsterDatabase.catalogue(pack.getMonsters());
        check(WikiEquipmentEngine.canonicalId(28949)==28947,"Locked uncharged quiver");
        check(WikiEquipmentEngine.canonicalId(28953)==28951,"Locked charged quiver");
        for(int id:new int[]{34633,34637,34625})check(WikiEquipmentEngine.canonicalItem(id).bonuses.magic_str==20,"Ancient cape stats");
        check(WikiEquipmentEngine.canonicalItem(34607)==null,"Deadman cape not enabled");
        for(String raid:new String[]{"toa","cox","tob"}){
            List<MonsterDefinition> matches=TargetSearch.search(all,raid,0);
            check(!matches.isEmpty(),raid+" has results");
            check(matches.stream().allMatch(m->TargetSearch.inRaid(m,raid)),raid+" isolated");
        }
        String[][] raidTerms={{"toa","tombs","amascut","tombs of amascut"},
            {"cox","chambers","xeric","chambers of xeric"},{"tob","theatre","blood","theatre of blood"}};
        for(String[] terms:raidTerms){
            List<MonsterDefinition> expected=TargetSearch.search(all,terms[0],0);
            for(String term:terms){
                check(TargetSearch.search(all,term,0).equals(expected),"Equivalent full raid filter: "+term);
                check(TargetSearch.search(all,term+" vorkath",0).isEmpty(),"Raid filter excludes other targets: "+term);
            }
        }
        check(!TargetSearch.search(all,"amascut zebak",0).isEmpty(),"Raid word plus boss");
        check(!TargetSearch.search(all,"xeric olm",0).isEmpty(),"Raid word plus shorthand");
        check(!TargetSearch.search(all,"blood verzik",0).isEmpty(),"Blood plus boss");
        RaidSettings saved=new RaidSettings(88,111,7,true,350,4,6,true,72);
        RaidSettings loaded=RaidSettings.decode(saved.encode());
        check(loaded.encode().equals(saved.encode()),"All nine raid options survive serialization");
        check(RaidSettings.decode(null).encode().equals(RaidSettings.DEFAULT.encode()),"First-run defaults");
        check(RaidSettings.decode("bad").encode().equals(RaidSettings.DEFAULT.encode()),"Malformed saved preferences");
        check(RaidSettings.decode("1|88|111|7|yes|350|4|6|true|72")==RaidSettings.DEFAULT,"Invalid boolean rejected");
        check(RaidSettings.decode("1|999|999|999|true|999|999|999|true|999").coxMining==99,"Saved bounds validated");
        try {
            javax.swing.SwingUtilities.invokeAndWait(()->{
                int[] callbacks={0};
                java.awt.Font font=new java.awt.Font("Dialog",java.awt.Font.PLAIN,14);
                RaidOptionsPanel restored=new RaidOptionsPanel(()->callbacks[0]++,font,font);
                restored.restore(loaded);
                check(restored.settings().encode().equals(saved.encode()),"Recreated panel restores saved options");
                check(callbacks[0]==0,"Restoring does not overwrite settings or invalidate results");
                check(restored.challenge.isSelected()&&restored.excludeSalts.isSelected(),"Checkboxes restored");
                check(((Number)restored.coxMining.getValue()).intValue()==72,"Mining control restored");
                restored.raidLevel.setValue(400);
                check(callbacks[0]==1&&restored.settings().raidLevel==400,"Edits still notify after restore");
                check(restored.settings().coxHp==88&&restored.settings().coxMining==72,"Editing ToA preserves CoX");
            });
        } catch(Exception ex){throw new AssertionError(ex);}
        check(TargetSearch.search(all,"ToA zebak",0).stream().allMatch(m->m.getName().equals("Zebak")),"Raid and target");
        check(TargetSearch.search(all,"tob vorkath",0).isEmpty(),"Raid never falls back to unrelated targets");
        check(TargetSearch.search(all,"kBd",1).get(0).getName().equals("King Black Dragon"),"KBD alias");
        check(TargetSearch.search(all,"vorktha",1).get(0).getName().equals("Vorkath"),"Transposition");
        check(TargetSearch.search(all,"olm great",1).get(0).getName().equals("Great Olm"),"Token order");
        check(TargetSearch.search(all,"xyzzyplugh",0).isEmpty(),"Unrelated query stays empty");
        check(TargetSearch.search(all,"",3).size()==3,"Search limit");
        String[] modes={"Normal Mode","Entry Mode","Hard Mode"};
        int[][] formIds={{8355,8356,8357},{10787,10788,10789},{10808,10809,10810}};
        String[] forms={"Melee","Magic","Ranged"};
        for(int mode=0;mode<3;mode++)for(int form=0;form<3;form++)
            check(find(all,"Nylocas Vasilias",modes[mode]+", "+forms[form]+" form").getId()==formIds[mode][form],"Verified Vasilias NPC ID");
        check(find(all,"Nylocas Vasilias","Normal Mode, Magic form").getId()==8356,"Normal Vasilias actual form ID");
        check(find(all,"Nylocas Vasilias","Hard Mode, Ranged form").getId()==10810,"Hard Vasilias actual form ID");
        check(Arrays.equals(RaidCombatRules.wardenBounds(50,0,9000),new int[]{7,17}),"Warden lower clamp");
        check(Arrays.equals(RaidCombatRules.wardenBounds(50,24000,9000),new int[]{13,23}),"Warden integer interpolation");
        check(Arrays.equals(RaidCombatRules.wardenBounds(50,45000,9000),new int[]{20,30}),"Warden upper clamp");
        check(Arrays.equals(RaidCombatRules.wardenBounds(50,Integer.MAX_VALUE,0),new int[]{20,30}),"Warden overflow-safe clamp");
        AccountSnapshot account=new AccountSnapshot(null){
            @Override public int real(Skill s){return 99;}
            @Override public int boosted(Skill s){return s==Skill.HITPOINTS?1:99;}
        };
        MonsterDefinition p2=find(all,"Elidinis' Warden","Phase 2 (Magic-vulnerable)");
        MagicCombatResult magic=new MagicCombatCalculator(account).calculatePowered(gear(AttackStyle.MAGIC,11905),p2,PoweredStaff.forWeapon("Trident of the seas"),false);
        close(magic.getAccuracy(),1,"Production P2 magic guaranteed hit");
        check(magic.getMaxHit()>0&&Double.isFinite(magic.getExpectedTtkSeconds()),"P2 magic finite");
        MonsterDefinition rangeP2=find(all,"Elidinis' Warden","Phase 2 (Ranged-vulnerable)");
        RangedCombatResult ranged=new RangedCombatCalculator(account).calculate(gear(AttackStyle.RANGED,9185,9144),rangeP2,false,RangedStance.RAPID,RangedPrayer.NONE);
        close(ranged.getAccuracy(),1,"Production P2 ranged guaranteed hit");
        int[] bounds=RaidCombatRules.wardenBounds(50,24000,9000);
        HitDistribution bolt=EnchantedBoltEffects.preAccurateZeroDistribution(null,p2,99,bounds[0],bounds[1],1,false).accurateZeroToOne();
        close(bolt.expectedHit(),18,"Warden ranged lower bound retained");
        MonsterDefinition core=find(all,"Elidinis' Warden","Phase 2 (Core-ejected)");
        CombatResult coreHit=new MeleeCombatCalculator(account).calculateForStance(gear(AttackStyle.MELEE_STAB,4151),AttackStyle.MELEE_SLASH,core,false,MeleeStance.ACCURATE,MeleePrayer.NONE);
        close(coreHit.getAccuracy(),1,"Core melee guaranteed");
        close(coreHit.getDps(),coreHit.getMaxHit()/(coreHit.getAttackSpeed()*.6),"Core always max, not average");
        MonsterDefinition guardian=all.stream().filter(WikiTargetEligibility::isGuardian).findFirst().get();
        EquipmentCandidate pick=item(11920,AttackStyle.MELEE_STAB);
        HitDistribution.AttackDistribution base=new HitDistribution.AttackDistribution(Collections.singletonList(HitDistribution.single(30,true)));
        close(RaidCombatRules.guardian(base,guardian,pick,99).max(),42,"Dragon pick Mining99 multiplier");
        close(RaidCombatRules.guardian(base,guardian,item(1265,AttackStyle.MELEE_STAB),1).max(),10,"Bronze pick Mining1 multiplier");
        check(RaidScaling.apply(guardian,new RaidSettings(99,126,1,false,0,0,1,false,1)).getHitpoints()==152,"Guardian average Mining HP");
        check(EquipmentSetRules.dharokFactor(99,1)==19702,"Dharok missing HP factor");
        check(EquipmentSetRules.dharokFactor(99,110)==10000,"Dharok overheal cannot reduce damage");
        Map<EquipmentSlot,EquipmentCandidate> dh=gear(AttackStyle.MELEE_SLASH,4718,4716,4720,4722);
        check(EquipmentSetRules.dharok(dh),"Full Dharok set recognized");
        Map<EquipmentSlot,EquipmentCandidate> brokenDh=gear(AttackStyle.MELEE_SLASH,4718,4884,4720,4722);
        check(!EquipmentSetRules.dharok(brokenDh),"Broken Dharok piece does not activate set");
        MonsterDefinition vorkath=find(all,"Vorkath","Post-quest");
        AccountSnapshot healthy=new AccountSnapshot(null){@Override public int real(Skill s){return 99;}@Override public int boosted(Skill s){return 99;}};
        CombatResult lowHp=new MeleeCombatCalculator(account).calculateForStance(dh,AttackStyle.MELEE_SLASH,vorkath,false,MeleeStance.AGGRESSIVE,MeleePrayer.NONE);
        CombatResult fullHp=new MeleeCombatCalculator(healthy).calculateForStance(dh,AttackStyle.MELEE_SLASH,vorkath,false,MeleeStance.AGGRESSIVE,MeleePrayer.NONE);
        check(lowHp.getMaxHit()==fullHp.getMaxHit()*19702/10000,"Production Dharok max scales per roll");
        check(lowHp.getDps()>fullHp.getDps(),"Production Dharok improves low-HP damage");
        Map<EquipmentSlot,EquipmentCandidate> blood=gear(AttackStyle.MELEE_CRUSH,28997,29028,29022,29025);
        CombatResult bloodHit=new MeleeCombatCalculator(healthy).calculateForStance(blood,AttackStyle.MELEE_CRUSH,vorkath,false,MeleeStance.AGGRESSIVE,MeleePrayer.NONE);
        HitDistribution.AttackDistribution bloodDist=WikiPlayerVsNpcCalc.dualMacuahuitlAttacker(bloodHit.getMaxHit(),bloodHit.getAccuracy());
        double ticks=4;
        close(bloodHit.getDps(),bloodDist.expectedDamage()/(ticks*.6),"Blood Moon pieces retain stats without full-set speed effect");
        RangedCombatResult atlatl=new RangedCombatCalculator(healthy).calculate(gear(AttackStyle.RANGED,29000,28991),vorkath,false,RangedStance.RAPID,RangedPrayer.NONE);
        AccountSnapshot weak=new AccountSnapshot(null){@Override public int real(Skill s){return 99;}@Override public int boosted(Skill s){return s==Skill.STRENGTH?50:99;}};
        RangedCombatResult weakAtlatl=new RangedCombatCalculator(weak).calculate(gear(AttackStyle.RANGED,29000,28991),vorkath,false,RangedStance.RAPID,RangedPrayer.NONE);
        check(atlatl.getMaxHit()>weakAtlatl.getMaxHit(),"Atlatl damage responds to Strength");
        close(atlatl.getAccuracy(),weakAtlatl.getAccuracy(),"Atlatl accuracy remains Ranged");
        Map<EquipmentSlot,Integer> quiver=new EnumMap<>(EquipmentSlot.class);
        quiver.put(EquipmentSlot.WEAPON,9185);quiver.put(EquipmentSlot.AMMO,9144);quiver.put(EquipmentSlot.CAPE,28949);
        WikiEquipmentEngine.EquipmentBonuses uncharged=WikiEquipmentEngine.calculateEquipmentBonuses(quiver,vorkath,null,false,false,false,null);
        quiver.put(EquipmentSlot.CAPE,28953);
        WikiEquipmentEngine.EquipmentBonuses charged=WikiEquipmentEngine.calculateEquipmentBonuses(quiver,vorkath,null,false,false,false,null);
        check(charged.rangedAttack==uncharged.rangedAttack+10&&charged.rangedStr==uncharged.rangedStr+1,"Locked charged quiver keeps Sunfire bonuses");
        dh.remove(EquipmentSlot.HEAD);check(!EquipmentSetRules.dharok(dh),"Incomplete Dharok no effect");
        System.out.println("ALL COMBAT AND SEARCH SELF-TESTS PASSED");
    }
}
