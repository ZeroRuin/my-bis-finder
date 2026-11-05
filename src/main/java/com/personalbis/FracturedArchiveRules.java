package com.personalbis;

import java.util.*;

/** Offline TFA planning. HP formula is provisional, fitted to supplied Wiki tables.
 * No live mechanics, incoming damage, movement or encounter simulation. */
final class FracturedArchiveRules {
    private FracturedArchiveRules() {}
    private static final Set<Integer> IDS=new HashSet<>(Arrays.asList(16659,16660,16640,16641,16652,
        16665,16666,16667,16670,16643,16645,16647,16650,16684,16687,16688,
        16673,16674,16675,16677,16678,16679,16680,16681));
    static boolean isTfa(MonsterDefinition m){return m!=null&&IDS.contains(m.getId());}
    static boolean blade(MonsterDefinition m){return m!=null&&m.getId()==16675;}
    static boolean imp(MonsterDefinition m){return m!=null&&m.getId()==16650;}
    enum BorkPhase {
        FULL("Smork HP 100% - 75%"), HIGH("Smork HP 75% - 50%"),
        LOW("Smork HP 50% - 25%"), EMPTY("Smork HP 25% - 0%"), STUNNED("Stunned");
        private final String label;
        BorkPhase(String label){this.label=label;}
        @Override public String toString(){return label;}
    }
    static boolean isBork(MonsterDefinition m){return m!=null&&m.getId()==16640;}
    static MonsterDefinition withBorkPhase(MonsterDefinition m,BorkPhase phase){
        if(!isBork(m))return m;
        int delta=phase==BorkPhase.STUNNED?100:0;
        return new MonsterDefinition(m.getId(),m.getName(),phase.toString(),m.getCombatLevel(),
            m.getDefenceLevel(),m.getMagicLevel(),m.getRangedLevel(),m.getHitpoints(),
            m.getStabDefence()-delta,m.getSlashDefence()-delta,m.getCrushDefence()-delta,
            m.getMagicDefence(),m.getLightDefence(),m.getStandardDefence(),m.getHeavyDefence(),
            m.getAttributes(),m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }
    static boolean hpSupported(MonsterDefinition m){
        if(m==null)return false;
        switch(m.getId()){
            case 16659:case 16660:case 16640:case 16641:case 16652:case 16665:case 16684:
            case 16673:case 16674:case 16677:return true;
            default:return false;
        }
    }
    static int hp(int base,int party){
        int n=Math.max(2,Math.min(8,party));
        if(n==2)return base;
        // Percent arithmetic followed by nearest-ten rounding, ties upward.
        return (int)(((long)base*(145+42*(n-3))+500)/1000)*10;
    }
    static MonsterDefinition scale(MonsterDefinition m,int party){
        if(!isTfa(m))return m;
        List<String> a=new ArrayList<>(m.getAttributes());
        a.removeIf(x->x.startsWith("tfa_party:")||x.startsWith("flat_armour:")&&m.getId()==16640);
        a.add("tfa_party:"+Math.max(2,Math.min(8,party)));
        int base=m.getHitpoints();
        if(m.getId()==16659&&m.getVersion().equalsIgnoreCase("Grounded"))base=1100;
        if(hpSupported(m))a.add("tfa_scaled_hp");
        return new MonsterDefinition(m.getId(),m.getName(),m.getVersion(),m.getCombatLevel(),m.getDefenceLevel(),
            m.getMagicLevel(),m.getRangedLevel(),hpSupported(m)?hp(base,party):base,
            m.getStabDefence(),m.getSlashDefence(),m.getCrushDefence(),m.getMagicDefence(),m.getLightDefence(),
            m.getStandardDefence(),m.getHeavyDefence(),a,m.getSize(),m.getWeaknessElement(),m.getWeaknessSeverity(),m.getMagicAttack());
    }
    static int floor(MonsterDefinition m){
        if(m==null)return -1;
        switch(m.getId()){
            case 16659:case 16660:return 156;case 16652:return 120;case 16640:return 180;
            case 16667:return 117;case 16684:return 102;case 16673:case 16674:return 129;case 16677:return 150;
            case 16641:case 16665:case 16643:case 16645:case 16647:case 16675:return 0;
            default:return -1;
        }
    }
    static int borkStage(MonsterDefinition m){
        if(!isBork(m)||BorkPhase.STUNNED.toString().equals(m.getVersion()))return -1;
        for(BorkPhase phase:BorkPhase.values())
            if(phase.toString().equals(m.getVersion()))return phase.ordinal();
        return 0;
    }
    static HitDistribution transform(HitDistribution d,MonsterDefinition m,AttackStyle style){
        int stage=borkStage(m);
        if(stage>=0){
            // Wiki excerpt and upstream disagree on initial Magic resistance.
            // Use upstream's 0/2/4/6 pending confirmation; documented in tooltip.
            int reduction=style==AttackStyle.MAGIC?2*stage:8+2*stage;
            d=d.transform(HitDistribution.flatAdd(-reduction,0),false);
        }
        return d;
    }
    static String note(MonsterDefinition m){
        if(!isTfa(m))return "";
        String s="TFA: static target estimate; HP scaling is provisional. No movement, team DPS, healing or encounter timing is simulated.";
        if(!hpSupported(m)&&!blade(m))s+=" Team-size HP scaling is not verified for this target; bundled HP is used.";
        if(blade(m))s+=" The Blade: melee recommendations excluded because of phase mechanics. DPS comparison only; phase duration and 50% transfer to phase 3 are not modelled. TTK unavailable.";
        if(m.getId()==16677)s+=" Phase 3 baseline; phase 4 shares base stats but prayers, rear bonus and conditional reductions are not modelled. Phase-2 damage transfer is excluded.";
        if(m.getId()==16665)s+=" No add-clear time or healing is included.";
        if(m.getId()==16652)s+=" Ezekiel enrage damage reduction is not modelled.";
        if(m.getId()==16640)s+=" Bork armour stages 1–4 correspond to Smorks HP 100–75%, 75–50%, 50–25% and 25–0%. Magic resistance follows upstream (0/2/4/6); the supplied Wiki prose conflicts. Stunned subtracts 100 from each melee defence bonus; alleged rear bonus excluded.";
        if(m.getId()==16643||m.getId()==16645||m.getId()==16647)s+=" Shared shield pool: estimate is for one depletion, not the full five-cycle encounter.";
        return s;
    }
}
