package com.personalbis;

import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Skill;

public class MeleeCombatCalculator
{
    private final AccountSnapshot account;

    @Inject
    public MeleeCombatCalculator(AccountSnapshot account)
    {
        this.account=account;
    }

    public CombatResult calculate(Map<EquipmentSlot,EquipmentCandidate> gear, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask)
    {
        CombatResult best = null;
        EquipmentCandidate weapon = gear.get(EquipmentSlot.WEAPON);
        for (MeleeStance stance : MeleeAttackStyleResolver.legalStances(weapon, style))
        {
            for (MeleePrayer prayer : availablePrayers())
            {
            CombatResult result = calculateForStance(gear, style, monster, onSlayerTask, stance, prayer);
            if (best == null || result.getDps() > best.getDps())
            {
                best = result;
            }
            }
        }
        return best;
    }

    public CombatResult calculateForStance(Map<EquipmentSlot,EquipmentCandidate> gear, AttackStyle style,
        MonsterDefinition monster, boolean onSlayerTask, MeleeStance stance, MeleePrayer prayer)
    {
        int attackBonus=0, strBonus=0, speed=4;
        boolean slayer=false;
        EquipmentCandidate weapon = gear.get(EquipmentSlot.WEAPON);

        for (EquipmentCandidate c : gear.values())
        {
            if (c==null) continue;
            attackBonus += c.getAttackBonus();
            strBonus += c.getStrengthBonus();
            if (c.getSlot()==EquipmentSlot.WEAPON && c.getAttackSpeed()>0) speed=c.getAttackSpeed();
            String n=c.getItem().getName().toLowerCase();
            if (c.getSlot()==EquipmentSlot.HEAD &&
                (n.contains("slayer helmet") || n.contains("slayer helm") || n.contains("black mask")))
            {
                slayer=true;
            }
        }

        attackBonus=WikiEquipmentEngine.transformMeleeAttack(gear,monster,attackBonus);
        WikiEquipmentEngine.Totals wikiEq=WikiEquipmentEngine.transform(gear,monster,null,strBonus,0,0,0.0,0); strBonus=wikiEq.str;

        int atk=Math.max(1,account.boosted(Skill.ATTACK));
        int str=Math.max(1,account.boosted(Skill.STRENGTH));

        // No prayer multiplier yet. Alpha4 makes the stance contribution explicit so close
        // weapon comparisons can be audited instead of hiding the +3/+1 style bonuses.
        boolean voidMelee=MeleeLoadoutEffects.hasVoidMelee(gear);
        int effAtk=CombatEffectiveLevels.melee(atk,prayer.getAttack(),stance.getAttackBoost(),voidMelee);
        int effStr=CombatEffectiveLevels.melee(str,prayer.getStrength(),stance.getStrengthBoost(),voidMelee);

        double salve=MeleeLoadoutEffects.salveMultiplier(gear,monster);
        final boolean avariceRevenant=WikiMeleeEffects.avariceRevenant(gear.values(),monster);

        final boolean slayerMeleeBonus = onSlayerTask && slayer && salve <= 1.0 && !avariceRevenant;
        int attackRoll=effAtk*(attackBonus+64);
        if (avariceRevenant)
        {
            attackRoll=WikiMeleeEffects.accuracyAfterAvarice(attackRoll,gear.values(),monster);
        }
        else if (salve > 1.0)
        {
            // Salve is a completed-roll modifier. Apply it after the ordinary
            // effective-level × equipment-bonus roll, preserving OSRS integer rounding.
            // Salve and Slayer helmet/black mask do not stack; Salve takes precedence.
            attackRoll=MeleeLoadoutEffects.salveAttackRoll(attackRoll,gear,monster);
        }
        if (slayerMeleeBonus)
        {
            // Black mask / Slayer helmet is a target-specific gear bonus:
            // apply x7/6 to the completed attack roll, not to effective Attack.
            attackRoll=SlayerMeleeEffects.attackRoll(attackRoll,true,true,false);
        }
        int preWeaponAttackRoll=attackRoll;
        attackRoll=(int)Math.floor(attackRoll
            *MeleeWeaponEffects.accuracyMultiplier(weapon,monster,style)
            *VampyreTargetRules.meleeAccuracyMultiplier(gear,weapon,monster)
            *MeleeLoadoutEffects.inquisitorMultiplier(gear,style)
            *MeleeLoadoutEffects.obsidianAccuracyMultiplier(gear,weapon));
        attackRoll=WikiMeleeEffects.postAccuracy(attackRoll,weapon,monster);
        int defLevel=monster==null ? 1 : Math.max(1,monster.getDefenceLevel());
        int defBonus=monster==null ? 0 : monster.defenceFor(style);
        int defenceRoll=(defLevel+9)*(defBonus+64);

        double accuracy=WikiAccuracyRoll.normal(attackRoll,defenceRoll);
        if(MeleeWeaponEffects.isFang(weapon)&&style==AttackStyle.MELEE_STAB)
            accuracy=WikiAccuracyRoll.fang(attackRoll,defenceRoll);

        int maxHit=WikiIntegerStages.maxHitFromEffective(effStr,strBonus+64);
        maxHit=WikiMeleeEffects.maxAfterCrystalBlessing(maxHit,gear.values());
        if (avariceRevenant)
        {
            maxHit=WikiMeleeEffects.maxAfterAvarice(maxHit,gear.values(),monster);
        }
        else if (salve > 1.0)
        {
            // Damage modifiers are applied before the final /640 truncation.
            // Keep the base max-hit numerator intact through Salve so fractional
            // pre-Salve damage is not discarded too early.
            maxHit=MeleeLoadoutEffects.salveMaxHitFromNumerator(effStr,strBonus,gear,monster);
        }
        if (slayerMeleeBonus)
        {
            // Target-specific damage bonuses are applied after the ordinary max-hit
            // calculation and floored at this stage.
            maxHit=SlayerMeleeEffects.maxHit(maxHit,true,true,false);
        }
        int preWeaponMaxHit=maxHit;
        speed=WikiAttackSpeedRules.attackTicks(speed,weapon,monster);
        double seconds=Math.max(1,speed)*0.6;
        int scytheHits=MeleeWeaponEffects.scytheHitCount(weapon,monster);

        // Weapon-specific damage multipliers modify the integer max hit first.
        // This rounding stage matters for Keris: e.g. 26 * 1.33 floors to 34,
        // then the ordinary hit distribution averages from that 34 max.
        int displayedMaxHit=maxHit;
        double averageHit;
        if(MeleeWeaponEffects.isScythe(weapon))
        {
            // Scythe rolls each size-dependent splat independently. Production DPS
            // uses the same Wiki-validated EV path as the DEV harness, while the
            // displayed max is the combined theoretical maximum (1/2/3 splats).
            displayedMaxHit=MeleeWeaponEffects.scytheCombinedMax(maxHit,scytheHits);
            averageHit=MeleeWeaponEffects.scytheAverageHit(maxHit,scytheHits);
        }
        else
        {
            double weaponDamage=MeleeWeaponEffects.damageMultiplier(weapon,monster,style);
            double obsidianDamage=MeleeLoadoutEffects.obsidianDamageMultiplier(gear,weapon);
            displayedMaxHit=(int)Math.floor(maxHit*weaponDamage*obsidianDamage*VampyreTargetRules.meleeDamageMultiplier(gear,weapon,monster));
            displayedMaxHit=WikiMeleeEffects.postDamage(displayedMaxHit,weapon,monster);
            // Colossal blade adds a flat +2 max hit per target tile, capped at size 5.
            // This is a max-hit addition, not a percentage multiplier.
            displayedMaxHit += MeleeWeaponEffects.colossalBladeBonusMax(weapon,monster);
            // Wiki parity: rat-bone weapons add +10 max against rat-attributed targets before later armour transforms.
            displayedMaxHit += MeleeWeaponEffects.ratbaneBonusMax(weapon,monster);
            if(obsidianDamage>1.0 && weaponDamage==1.0)
                averageHit=MeleeLoadoutEffects.obsidianModifiedRollAverage(maxHit,obsidianDamage);
            else
                averageHit=MeleeWeaponEffects.successfulHitAverage(displayedMaxHit);
            if(MeleeWeaponEffects.isFang(weapon))
            {
                // Fang's displayed normal max is 85% of its true max and its
                // successful damage roll is uniformly constrained to 15%-85%.
                displayedMaxHit=MeleeWeaponEffects.fangDisplayedMaxHit(maxHit);
                averageHit=MeleeWeaponEffects.fangAverageHit(maxHit);
            }
            if(MeleeWeaponEffects.isKeris(weapon) && monster!=null &&
                (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite")))
            {
                // Model the ordinary and 1/51 triple-hit distributions separately;
                // each successful zero roll is converted to 1 by the game.
                averageHit=MeleeWeaponEffects.kerisAverageHit(displayedMaxHit);
            }
        }
        double inquisitor=MeleeLoadoutEffects.inquisitorMultiplier(gear,style);
        // Inquisitor's Crush damage bonus is applied to the integer ordinary/weapon
        // max hit, floored, and that modified max defines the successful-hit damage
        // distribution. Wiki checkpoint: 42 * 1.025 -> 43, whose successful-hit
        // average is 43/2 + 1/44 = 21.522727..., yielding 3.29106 DPS at the
        // Vorkath no-shield checkpoint. Do not multiply the old average by 1.025.
        if (inquisitor > 1.0 && !MeleeWeaponEffects.isScythe(weapon))
        {
            displayedMaxHit=(int)Math.floor(displayedMaxHit*inquisitor);
            if(MeleeWeaponEffects.isKeris(weapon) && monster!=null &&
                (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite")))
            {
                averageHit=MeleeWeaponEffects.kerisAverageHit(displayedMaxHit);
            }
            else
            {
                averageHit=MeleeWeaponEffects.successfulHitAverage(displayedMaxHit);
            }
        }
        // alpha50.4.50.10: preserve the complete melee attacker distribution until
        // after Corp and NPC transforms. This is required for per-hitsplat armour,
        // caps and reductions on Scythe/Keris/Fang/two-hit weapons.
        HitDistribution.AttackDistribution attackDist;
        if (MeleeWeaponEffects.isScythe(weapon))
            attackDist=WikiPlayerVsNpcCalc.scytheAttacker(maxHit,scytheHits,accuracy);
        else if (MeleeWeaponEffects.isDualMacuahuitl(weapon))
            attackDist=WikiPlayerVsNpcCalc.dualMacuahuitlAttacker(displayedMaxHit,accuracy);
        else if (MeleeWeaponEffects.isIndependentTwoHit(weapon))
            attackDist=WikiPlayerVsNpcCalc.independentTwoHitAttacker(displayedMaxHit,accuracy);
        else if (MeleeWeaponEffects.isFang(weapon))
            attackDist=WikiPlayerVsNpcCalc.fangAttacker(preWeaponMaxHit,accuracy);
        else if (MeleeWeaponEffects.isKeris(weapon) && monster!=null &&
            (monster.hasAttribute("kalphite")||monster.hasAttribute("scabarite")))
            attackDist=WikiPlayerVsNpcCalc.kerisAttacker(displayedMaxHit,accuracy);
        else if (MeleeLoadoutEffects.obsidianDamageMultiplier(gear,weapon)>1.0
            && MeleeWeaponEffects.damageMultiplier(weapon,monster,style)==1.0 && inquisitor==1.0)
            attackDist=WikiPlayerVsNpcCalc.obsidianAttacker(preWeaponMaxHit,accuracy,MeleeLoadoutEffects.obsidianDamageMultiplier(gear,weapon));
        else if ((weapon!=null&&weapon.getItem()!=null&&weapon.getItem().getName().equalsIgnoreCase("Gadderhammer")
            && monster!=null&&monster.hasAttribute("shade")) || fullVerac(gear))
            attackDist=WikiPlayerVsNpcCalc.meleeSustainedAttacker(displayedMaxHit,accuracy,gear.values(),monster);
        else
            attackDist=WikiPlayerVsNpcCalc.standardAttacker(displayedMaxHit,accuracy);

        attackDist=WikiPlayerVsNpcCalc.applyCorp(attackDist,monster,weapon,style);
        attackDist=WikiPlayerVsNpcCalc.finalDistribution(attackDist,monster,style,weapon,null,false);
        double dps=WikiPlayerVsNpcCalc.expectedDamage(attackDist)/seconds;
        displayedMaxHit=attackDist.max();

        // Soulreaper is stateful: production ranking starts at zero stacks, then
        // scores the actual 0/1/2/3/4 -> 5-stack ramp over this target's HP.
        // Accuracy does not change with stacks; only Strength/max hit does.
        if(SoulreaperAxeEffects.isSoulreaper(weapon))
        {
            double[] stackEv=new double[6];
            int potentialMax=displayedMaxHit;
            for(int stacks=0;stacks<=5;stacks++)
            {
                int soulEff=SoulreaperAxeEffects.effectiveStrength(str,prayer.getStrength(),stance.getStrengthBoost(),voidMelee,stacks);
                int soulMax=WikiIntegerStages.maxHitFromEffective(soulEff,strBonus+64);
                if(salve>1.0) soulMax=MeleeLoadoutEffects.salveMaxHitFromNumerator(soulEff,strBonus,gear,monster);
                if(slayerMeleeBonus) soulMax=SlayerMeleeEffects.maxHit(soulMax,true,true,false);
                soulMax=(int)Math.floor(soulMax*MeleeWeaponEffects.damageMultiplier(weapon,monster,style));
                soulMax=(int)Math.floor(soulMax*inquisitor);
                HitDistribution soulDist=MeleeWeaponEffects.standardDistribution(soulMax,accuracy);
                if(CorporealBeastRules.reduced(monster,weapon,style)) soulDist=soulDist.transform(HitDistribution.divide(2,0));
                soulDist=WikiNpcTransforms.apply(soulDist,monster,style,weapon,null,false);
                stackEv[stacks]=soulDist.expectedHit();
                if(stacks==5) potentialMax=soulDist.max();
            }
            displayedMaxHit=potentialMax;
            int targetHp=monster==null?1:Math.max(1,monster.getHitpoints());
            dps=SoulreaperAxeEffects.rampDps(stackEv,targetHp,seconds);
        }


        // Kurasks/Turoths calculate an ordinary accuracy roll for unsupported weapons,
        // but the hit itself is ineffective. Preserve accuracy/roll diagnostics while
        // forcing damage to zero so the optimizer cannot recommend an unusable weapon.
        if (VampyreTargetRules.tier2HalfDamage(gear,weapon,monster)) { displayedMaxHit/=2; dps/=2.0; }
        if (VampyreTargetRules.tier2SilverCap10(gear,weapon,monster)) { displayedMaxHit=Math.min(displayedMaxHit,10); dps=accuracy*MeleeWeaponEffects.successfulHitAverage(displayedMaxHit)/seconds; }
        if (!WikiTargetEligibility.meleeCanDamage(monster,weapon) || !LeafyTargetRules.meleeCanDamage(monster,weapon) || !MeleeWeaponEffects.ratBoneCanDamage(weapon,monster) || !VampyreTargetRules.canDamageMelee(gear,weapon,monster))
        {
            displayedMaxHit=0;
            dps=0.0;
        }

        return new CombatResult(dps,accuracy,displayedMaxHit,attackRoll,defenceRoll,
            attackBonus,strBonus,speed,effAtk,effStr,stance,prayer,preWeaponAttackRoll,preWeaponMaxHit);
    }


    private boolean fullVerac(Map<EquipmentSlot,EquipmentCandidate> gear)
    {
        return named(gear.get(EquipmentSlot.WEAPON),"Verac's flail")&&named(gear.get(EquipmentSlot.HEAD),"Verac's helm")
            &&named(gear.get(EquipmentSlot.BODY),"Verac's brassard")&&named(gear.get(EquipmentSlot.LEGS),"Verac's plateskirt");
    }

    private boolean named(EquipmentCandidate c,String name)
    { return c!=null&&c.getItem()!=null&&c.getItem().getName().equalsIgnoreCase(name); }

    private MeleePrayer[] availablePrayers()
    {
        int p = account.real(Skill.PRAYER);
        // The optimiser's objective is maximum DPS. Every supported offensive
        // Melee prayer increases both Attack and Strength, so NONE is strictly
        // dominated whenever a prayer is available. Evaluating it for every
        // temporary beam state doubled the hottest part of the search without
        // ever being able to produce the winning loadout.
        if (p >= 70 && account.isPietyUnlocked()) return new MeleePrayer[]{MeleePrayer.PIETY};
        if (p >= 60 && account.isChivalryUnlocked()) return new MeleePrayer[]{MeleePrayer.CHIVALRY};
        if (p >= 44) return new MeleePrayer[]{MeleePrayer.HIGH};
        if (p >= 31) return new MeleePrayer[]{MeleePrayer.MID};
        if (p >= 13) return new MeleePrayer[]{MeleePrayer.LOW};
        return new MeleePrayer[]{MeleePrayer.NONE};
    }
}
