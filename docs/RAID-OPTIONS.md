# CoX and ToA options — test build

## Calculation scope

The sidebar shows only the controls for the selected raid, directly below the
picker. CoX defaults: highest party HP 99 (1–99), highest combat 126 (3–126),
party size 1 (1–100), average party Mining 99 (1–99), Challenge Mode off. Scaling internally applies the game's
minimum HP/combat scaling floors. ToA defaults: raid level 0 (0–700), path level
0 (0–6), party size 1 (1–8), Exclude Smelling salts off. Settings are saved in the active RuneLite configuration profile and restored
after logout, plugin restart and client restart. They remain separate between
raids and are captured immutably for each generation. Switching configuration
profiles reloads that profile's settings; missing or malformed values use defaults.

Generated CoX profiles use the Wiki calculator's separate solo/group scaling,
linked offensive/defensive stats, integer stage ordering, CM exceptions for
Tekton/crystals and special Olm HP/mage-hand scaling. Only fields relevant to
player-versus-NPC combat are scaled; incoming attack damage is not simulated.
Guardian base HP is 151 plus average party Mining before ordinary CoX scaling.
Guardian damage scales each rolled hit by (50 + your base Mining + pickaxe tier) / 150,
with integer truncation before accurate-zero conversion.

Generated ToA profiles scale HP for invocation, applicable path level and party
size with the specified rounding stages. Core-ejected HP uses its separate
invocation factor. Path level does not scale Wardens or the Obelisk. Applicable
NPC defence rolls scale by (250 + raid level) / 250, rather than mutating their
Defence skill. Overlord defence-roll exceptions are retained. Final Akkha HP is
one fifth of the fully scaled/rounded boss HP. Fang uses its ToA reroll branch.

The current context is used for exact melee/ranged/magic calculations and the
ranged/magic shortlisting estimates. A generated target is a copy; the bundled
JSON/catalogue remains unscaled. Repeated generation always starts with the
canonical base target. The cache includes scaled combat fields, labels and all
active raid settings/boost assumptions. Editing settings cancels in-flight
results, hides stale results and requires regeneration.

## Boosts

Salts default to the full base-level + floor(16% of base level) + 11 for Attack,
Strength, Ranged and Magic, retaining a higher currently captured boost if present.
Ownership of raid-only salts is not required. When excluded, explicitly remove
the salt assumption and choose supported owned potion/heart boosts from the
same filtered ownership list as generation. No matching boost means base levels;
captured active salts cannot leak into the excluded calculation. Supported
fallbacks cover ordinary/super/divine melee potions, ranging potions, Magic
potions, ancient/forgotten brews and imbued/saturated hearts. This is an assumed
full boost, without potion drain or brew-side-effect simulation.

## Target states and remaining gaps

CoX part/variant names remain distinct; Olm uses Head, Left claw (Melee hand)
and Right claw (Magic hand), with mode controlled by the CM checkbox. ToA keeps
Kephri Shielded/Unshielded, Zebak Normal/Enraged, Obelisk Phase 1, both Wardens'
phase-two vulnerable styles, core-ejected snapshot, Phase 3 and Phase 3 (Enraged).
Akkha offers Magic-, Ranged- and Melee-vulnerable states and a melee-only final
state. Vulnerability names describe the player's allowed attack style.
Invulnerable Warden records are not offered as attackable targets.

Warden phase two always hits. Its pre-proc damage bounds are 15–35% through
40–60% of the ordinary maximum, based on attack roll less one third of scaled
defence roll. The integer modifier interpolates over 0–42,000 remaining attack
roll. Twisted bow accuracy scaling applies twice in P2. Melee against the exposed
core always deals the attack's combined maximum damage.

Remaining gaps include timed core exposure/body-damage transfer, Keris sun's
below-quarter-HP accuracy, raid special-attack rotations, encounter downtime,
team DPS and complete raid simulations. Core TTK assumes continuous exposure,
not the elapsed time to finish the complete Warden fight.

## Reference and validation

Primary reference checked 14 October 2026:
https://github.com/weirdgloop/osrs-dps-calc/tree/main/src/lib/scaling
and PlayerVsNPCCalc.ts, constants.ts, MonsterContainer.tsx, and the upstream CoX
and ToA scaling tests. Equations are implemented using the existing bundled
snapshot; new upstream monster data is not imported as part of this change.

`raidOptionsSelfTest` checks defaults/bounds, independent input snapshots, golden
CoX/ToA scaling fixtures, CM/Olm/core/overlord exceptions, salts exclusion and
owned fallbacks, phase restrictions, defence-roll plumbing and conditional UI.
The UI can be rendered using an optional output prefix argument to that test.
See TESTING.md for in-game checks and the full regression suite.

## Target-wide immunity display

The same existing target eligibility rules determine grey style buttons and
search skipping for all targets, including supported raid phase restrictions.
Immune searches are cached as completed empty reports so they cannot hold up
BEST selection. The rank preparation, optimizer searches and supplies are skipped
for those styles. Grey controls remain enabled after generation so a click can
show the target/phase-specific immunity explanation without invoking an optimizer.
Gear/stats and stale bank recommendations are cleared for that selection.
Weapon-dependent exceptions (including Zulrah polearms and Guardian pickaxes)
and lack of owned equipment do not constitute unconditional style immunity.
This does not add previously unmodelled NPC immunity mechanics.
