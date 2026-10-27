# Combat option audit

Primary source: Wiki calculator commit `89c3e25b344aea90d0189746e4b5f73dde0f0383`,
[getCombatStylesForCategory](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/utils.ts).
The live calculator footer identified this same commit when the share was inspected.

The table covers every nonempty weapon category present in the bundled data.
Names describe options the planner actually models; this is not a claim that
every game item, charge state, special attack or training/defensive mode is supported.
There are 1,568 weapon records; 24 have no category and remain unsupported.
Defensive-only melee options and Bulwark Block do not become attack recommendations.
Where multiple buttons have identical type/stance, use the first equivalent option.

| Category | Bundled weapons | Modelled offensive melee/ranged buttons |
|---|---:|---|
| 2h Sword | 30 | Chop, Slash, Smash |
| Banner | 42 | Lunge, Swipe, Pound |
| Bladed Staff | 8 | Jab, Swipe |
| Blaster | 3 | None |
| Bow | 85 | Accurate, Rapid, Longrange |
| Crossbow | 31 | Accurate, Rapid, Longrange |
| Thrown | 100 | Accurate, Rapid, Longrange |
| Gun | 1 | Kick |
| Bulwark | 2 | Pummel |
| Multi-Melee | 1 | Poke, Slash, Pound |
| Partisan | 5 | Stab, Lunge, Pound |
| Pickaxe | 28 | Spike, Impale, Smash |
| Polearm | 18 | Jab, Swipe |
| Powered Staff | 42 | Accurate (supported powered attacks) |
| Salamander | 5 | Scorch, Flare |
| Chinchompas | 3 | Short fuse, Medium fuse, Long fuse |
| Claw | 16 | Chop, Slash, Lunge |
| Bludgeon | 1 | Pound, Pummel, Smash |
| Blunt | 153 | Pound, Pummel |
| Polestaff | 16 | Bash, Pound |
| Spiked | 40 | Pound, Pummel, Spike |
| Staff | 124 | Bash, Pound |
| Axe | 56 | Chop, Hack, Smash |
| Unarmed | 424 | Punch, Kick |
| Scythe | 7 | Reap, Chop, Jab |
| Slash Sword | 84 | Chop, Slash, Lunge |
| Spear | 100 | Lunge, Swipe, Pound |
| Stab Sword | 112 | Stab, Lunge, Slash |
| Whip | 6 | Flick, Lash |
| Flail | 1 | Chop, Slash |

Spellbook attacks use **Spell**, while the supported built-in powered attacks use
**Accurate**. The latter now includes the Wiki +2 effective accuracy-level bonus
after prayer truncation and before Void, in
[getPlayerMaxMagicAttackRoll](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts).
This does not add an unmodelled powered weapon, change spells automatically,
or introduce manual-cast or defensive-casting controls.

Labels and melee eligibility now use the same verified table. Ranged search excludes
non-ranged categories and restricts Salamander to its actual Flare/Rapid option.
Unknown combinations use explicit diagnostics rather than fictional button names.

Tests compare each modelled type/stance against a pinned primary-source fixture,
and explicitly reject the former fictional halberd Crush, flail Crush,
Bludgeon Accurate and Bulwark Aggressive options. Powered tests check +2 order
before Void and unchanged ordinary-autocast math. Full RuneLite dependency-backed
regression and visual checks still need to run in IntelliJ.
