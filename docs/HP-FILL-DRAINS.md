# HP scaling and Advanced planning options

This is a local development build based on commit
`6518d78f3f7b23e964a916593b0e7a299146e006`. Prepared locally as 0.9.7; not yet submitted to Plugin Hub.
It has not been pushed or submitted to Plugin Hub. The test ZIP contains source,
not a precompiled RuneLite client or plugin JAR.

## Vardorvis

A **Starting HP** control appears directly beneath the selected Vardorvis target.
Each variant defaults to its full maximum, accepts 1 through that maximum, and
saves an independent value in the current configuration profile. Generation models
only the selected remaining HP through zero. Lowering HP does not redefine the
variant's maximum or the Defence interpolation denominator. This is manual input,
not live NPC monitoring. Changes invalidate results and cancel superseded work.

Supported variants use these Defence endpoints:

| Variant | Starting HP | Defence at full HP | Defence at zero HP |
| --- | ---: | ---: | ---: |
| Quest | 500 | 180 | 130 |
| Post-quest | 700 | 215 | 145 |
| Awakened | 1,400 | 268 | 181 |

For remaining HP `h`, Defence is the truncated positive interpolation
`end + (start - end) * clamp(h, 0, maxHP) / maxHP`. The completed value is
truncated; the lost Defence is not rounded separately. Deadman and unrecognised
variants do not inherit these values.

Finite-fight TTK uses an attack distribution for each remaining-HP state. Each
attack, including its multi-hit distribution, uses Defence at the start of that
attack. Ruby bolts use the same remaining-HP context for both Defence and their
HP-dependent damage. Distribution totals are cached within each calculation to
avoid rebuilding the same multi-hit convolution at every HP value.

Vardorvis's displayed accuracy and sustained DPS are **selected-starting-HP snapshots**, not
fight averages. TTK allows Defence to fall throughout the fight. Magic uses the
target's Magic level for its defence roll, so the changing Defence level does
not change ordinary Magic accuracy. NPC Strength scaling is not needed because
incoming damage is outside this plugin's model.

Direct-special recommendations remain separate, selected-starting-HP burst comparisons in
the displayed armour. They do not insert a special into the TTK calculation or
apply a drain automatically. Ruby-special damage also uses selected starting target HP.

Soulreaper uses a joint HP/stack calculation for these Vardorvis variants, with
the existing ramp convention of one stack step per attempted swing, up to five.
Its displayed Vardorvis DPS/max-hit snapshot starts at zero stacks. The existing
non-Vardorvis Soulreaper approximation is unchanged. This does not add encounter
timing, movement, healing, downtime or incoming boss mechanics.

### Four-checkpoint shortlist

Melee and ranged partial gear searches retain candidates from four snapshots:
starting HP, floor(5/7 of starting HP), floor(3/7), and floor(1/7), minimum 1.
At 700 HP these are 700, 500, 300 and 100; at 350 they are 350, 250, 150 and 50.
Duplicate checkpoints at very low HP are evaluated once. Each intermediate beam
keeps up to 8 melee / 32 ranged states per checkpoint. The final union retains
the top two states per checkpoint (up to eight distinct sets per weapon/stance
or ammo branch). Those finalists receive the detailed remaining-fight TTK;
complete-set and legal slot-fill checks also retain detailed calculations.
Ruby checkpoint scores use that checkpoint's HP and Defence together. Magic
Defence is not HP-dependent and keeps its existing search. One fixed loadout is
recommended; no mid-fight switching is simulated.

This remains a bounded search heuristic, not proof of the global optimum.
Checkpoint-selection tests passed; a complete broader-search comparison and
in-client performance measurement remain unverified in this environment.

## Prayer fill and Defence fill

Prayer fill remains the default. Defence fill runs after the ordinary search
and examines the full owned candidate list, including replacements for occupied
slots. It does not claim to find a globally optimal defensive set.

Every accepted replacement must preserve the original **unrounded accuracy,
max hit, attack interval, sustained DPS and finite-fight TTK exactly**. The raw
attack roll and pre-transform maximum are also held equal, so forced accuracy
or a damage cap cannot hide a lost offensive stat. Lower accuracy is rejected
even if formatted DPS, integer damage or TTK happens to tie.

Weapons and ammunition remain fixed. Mandatory Slayer slots remain fixed.
Two-handed restrictions, item exclusions and known equipment requirements remain
in force. Magic replacements must retain rune availability for the selected
spell; removing a necessary tome is not allowed. Powered-weapon charge assumptions
remain as previously documented.

Defensive choices compare **equipment defence bonuses**, not incoming damage:

- Supported Vardorvis variants: favour Slash defence for the ordinary attack.
- Vorkath IDs 8058–8061: equal weight to Slash, Magic and Ranged defence.
  Dragonfire protection and special mechanics are not scored by these bonuses.
- Other targets: reliable attack-style metadata is not bundled, so use equal
  weight across Stab, Slash, Crush, Magic and Ranged defence. Do not infer a
  target's attack style from its numerical offensive bonuses.

For multiple styles, the sum of the listed bonuses is compared. A tied sum
favours the higher weakest listed bonus, then Prayer. These are per-slot
preferences, with no assumptions about attack frequency, protection prayers or
survival. Unrecognised defensive item stats are not invented.

The preference is saved per RuneLite configuration profile. Changing it requires
regeneration and invalidates an in-progress calculation.

## Defensive Reductions

The Advanced section provides these manual inputs, initially zero/off:

| Input | Meaning |
| --- | --- |
| Elder maul / Dragon warhammer | Successful hit counts, up to 20 each |
| Arclight / Emberlight | Successful hit counts, up to 20 each |
| Tonalztics of Ralos | Individual successful hitsplats, up to 40; not spec activations |
| Bandos godsword / Seercull / Eye of Ayak | Total assumed special damage, 0–10,000 each |
| Accursed sceptre / Vulnerability | Alternative pre-applied curse toggles |

Weapon controls require the corresponding personal bank/inventory/equipment ID.
Charged Accursed sceptres, charged Eye of Ayak and active Arclight are required;
Tonalztics permits its charged/uncharged variants. Exclusions and known
requirements are rechecked at generation. GIM-only weapons do not reveal controls.
Vulnerability requires 66 base Magic and at least 5 loose Water, 5 loose Earth
and 1 Soul rune in personal planning ownership. Staff, combination-rune and pouch
substitution is not inferred. This is conservative visibility, not a casting
check. A prior standard-spellbook cast is explicitly assumed. The enhanced
Tome of Water curse is excluded; the control is labelled standard **10%**.

Values save per configuration profile. A known complete scan clears only inputs
whose required items/supplies disappeared. An unscanned bank is unknown and does
not erase saved values. The old exclusive DWH/maul save migrates to one hit.

All selected effects are assumed **already applied at the chosen starting HP**,
then held fixed for the remaining-fight estimate. No stat recovery, setup time,
energy, drain-special damage or rotation is simulated. Successful hits and damage
are supplied by the player, never predicted. Ordinary results still exclude the
separate recommended direct-damage special.

Order and rounding match the pinned Wiki `scaling/DefenceReduction.ts`:

1. Accursed: floor(current Defence × 17/20), floor(Magic × 17/20); alternatively
   standard Vulnerability: floor(current Defence × 9/10).
2. Each maul subtracts floor(current Defence × 35/100), then each warhammer
   subtracts floor(current Defence × 3/10).
3. Arclight then Emberlight subtract floor(original scaled skill × factor/20)+1
   per hit from Defence, Attack and Strength. Factor is 2 for Arclight against
   demons, 3 for Emberlight against demons, otherwise 1. The base reference
   predates the other selected drains.
4. Each Tonalztics hitsplat subtracts floor(current Magic/8) from Defence.
5. Seercull subtracts the entered damage from Magic, minimum zero.
6. BGS subtracts damage through Defence → Strength → Attack → Magic → Ranged.
   A positive Defence floor stops overflow. NPC Attack/Strength are retained and
   CoX-scaled as intermediate values for this operation, not incoming damage.
   If a nonstandard target lacks those values, excess beyond Defence is omitted
   with a result note rather than invented.
7. Eye of Ayak subtracts entered damage from a positive Magic defence **bonus**,
   minimum zero. This is separate from the target's Magic level.

Each Defence operation respects the existing target-specific floor. Vardorvis
and Verzik's **Defence levels** cannot be drained; a Defence floor is not silently
reused as a Magic-level or Magic-bonus floor. Raid scaling precedes reductions.
Zero Defence/Magic levels now stay zero in the ordinary accuracy rolls. The
controls are the ten inputs above, matching the supplied Wiki panel; other
weapons' drains and Tome-enhanced Vulnerability are not calculated by these inputs.

## Void and elite Void audit

Checked `PlayerVsNPCCalc.ts`, `BaseCalc.ts` and `Equipment.ts` at
`89c3e25b344aea90d0189746e4b5f73dde0f0383`:

| Set | Accuracy | Damage |
| --- | --- | --- |
| Melee Void / elite | ×11/10 effective Attack | ×11/10 effective Strength |
| Ranged Void | ×11/10 effective Ranged | ×11/10 effective damage level |
| Elite ranged | ×11/10 effective Ranged | ×9/8 effective damage level |
| Mage Void | ×29/20 effective Magic | No extra equipment damage |
| Elite mage | ×29/20 effective Magic | +5 percentage points equipment Magic damage |

Prayer truncation and stance/+8 or +9 happen before the Void effective-level
factor. Missing gloves, mismatched helmets or missing robes disable the set.
Mixed normal/elite robes retain ordinary Void, but require both elite pieces
for the elite ranged/Magic damage benefit. Magic's exact-name checks and seed
lookup now accept the supported ornament-kit suffix. The old generic Magic
level helper now adds +9 before Void, matching the already-correct production
accuracy pipeline. Elite Magic damage is additive with equipment damage, not a
second multiplicative +5%; it is added before Shadow equipment scaling.

Normal/mixed/elite melee and ranged Void now have independent protected-family
searches, so ordinary partial raw-stat gear does not eliminate them before their
set bonus exists. Pieces are locked while remaining slots are searched, and
completed family results compete by finite TTK. This does not force Void or DHCB
to win; account levels, ammunition, boosts, drains and available supporting gear
still matter. Both elite melee pieces share normal melee Void offensive factors.

Focused tests cover actual melee/ranged/powered calculators using an external
account API double, plus set detection, ornaments, mixed/incomplete sets,
rounding, additive damage and Shadow ordering. This does not establish that the
bounded gear search always selects a globally optimal Void loadout. Full-client
and dependency-backed regression tests remain required.

## Blowpipe ammo assumptions

Advanced shows a separate named Ammo dropdown for each supported blowpipe type
in a **personal bank snapshot**. Inventory/equipment/GIM-only ownership does not
reveal these controls. Empty and charged forms of the same type share one choice;
multiple copies with different contents cannot be distinguished by this setting.

The choices are unpoisoned dart types, capped by weapon:

| Blowpipe | Highest offered dart |
| --- | --- |
| Camphor | Mithril |
| Ironwood | Adamant |
| Rosewood | Rune |
| Toxic / Blazing / Drygore | Dragon, including Amethyst |

Drygore remains subject to existing world/item eligibility limitations; a menu
choice does not add league mechanics or make a weapon generally obtainable.

The default is **Not selected**, which excludes that blowpipe from calculated
ordinary and supported direct-special recommendations. Loose darts are never
used to infer its internal ammo. A chosen compatible dart may be selected even
when none are loose in the bank: it is an explicit player assertion, not proof
of ownership or detected contents. Item exclusions still apply to chosen darts.

Selections persist per profile. A missing weapon hides its control and removes
its active assumption; its preference is remembered for a later scan. Logout
clears bank-based availability until another bank scan. No loaded count, scales,
per-item identity or automatic detection is claimed. Bank supplies only offer
genuinely owned spare darts, separately labelled from the internal assumption.

## Sources and review boundary

- [Wiki Vardorvis scaling](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/scaling/Vardorvis.ts)
  and [interpolation](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/Math.ts).
- [Wiki drain formulas/floors](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/scaling/DefenceReduction.ts),
  [target IDs](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/constants.ts)
  and [scaling order](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/MonsterScaling.ts).
- [Wiki player-vs-NPC defence rolls](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts).
- OSRS Wiki [Vardorvis](https://oldschool.runescape.wiki/w/Vardorvis) and
  [Vorkath](https://oldschool.runescape.wiki/w/Vorkath) attack-style summaries.
  Reviewed mappings are intentionally limited; all other targets use the explicit
  unknown-offence policy.
- OSRS Wiki [Blowpipe](https://oldschool.runescape.wiki/w/Blowpipe), bundled
  item/alias data, and Jagex's [Summer Sweep-Up 2026](https://secure.runescape.com/m=news/summer-sweep-up-2026?oldschool=1)
  Rosewood change. Direct access to some Wiki pages was blocked in this environment;
  wooden dart caps should also be confirmed in the real-client checks below.
- Current [Plugin Hub requirements](https://github.com/runelite/plugin-hub),
  [rejected/restricted features](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
  and linked [Jagex guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1).

All runtime work remains local and player-triggered. No new runtime dependency,
network request, reflection, member lookup, client script, game action, menu action,
file access or automation was added. Passing checks does not establish Plugin Hub
approval. Reviewer acceptance of the new manual planning controls remains a
separate decision.

## Local verification and in-client checks

Completed here: five standalone Java self-tests, 34 Python tests, Java 11 syntax
parsing of all sources, and an isolated headless Swing check with a configuration
test double. The Swing check covers component behaviour, not real RuneLite profile
events. Bundled data/images were checked against the pinned upstream blob hashes.

Full RuneLite compilation and the existing 13 Java suites could not run here:
Gradle and cached RuneLite dependencies are absent, and direct dependency/Git
downloads are blocked. A Java compiler module is available and was used for the
standalone checks. No full-build or in-client pass is claimed.

On Windows/IntelliJ, extract the ZIP, open `my-bis-finder`, reload Gradle and run
the compile tasks and existing suites in [TESTING.md](TESTING.md), plus:

```text
vardorvisSelfTest
hpStateTtkSelfTest
defenceFillSelfTest
manualDefenceReductionSelfTest
blowpipeAmmoPlanSelfTest
```

Then run the development client with the installed Hub copy disabled:

1. Compare all Vardorvis variants and ordinary weapons, Fang, Scythe, Soulreaper,
   Ruby bolts and supported direct specials. Check full-HP accuracy/DPS labels,
   changing-Defence TTK and time to generate with a realistic bank.
2. At the boosted Strength threshold where Elite black platelegs tie Bandos
   tassets offensively, compare Prayer/Defence fill. Check exact displayed combat
   values, required Slayer equipment, two-handed weapons and tome-supplied spells.
3. Scan each drain weapon in bank, inventory and equipment. Check absent options,
   Off defaults, exclusive selection, known target floors/immunities, exclusions,
   changing targets and removing the last owned copy. Compare the disclosed fixed
   pre-applied model with the Wiki calculator's same settings.
4. Scan each owned blowpipe type, including empty/charged forms. Check separate
   names and compatible caps, Not selected behaviour, no loose-dart inference,
   absent loose darts, spare-dart supplies and excluded selected darts.
5. Change each new option while generation is running; old results must not publish.
   Confirm settings across restart and configuration-profile changes. Log out/in,
   rescan personal bank, and check that another account's bank availability is not
   retained. Check GIM recommendations and existing exclusions/restoration.
6. Recheck Seren, Hueycoatl Tail, Verzik P1 and the documented raid controls. No
   changes to their approved transforms or raid settings were intended.

## Verified local comparison: ordinary Fang/Salve and defensive headgear

Current primary checkpoints are pinned to the Wiki calculator commit
89c3e25b344aea90d0189746e4b5f73dde0f0383,
[PlayerVsNPCCalc.getMeleeMaxHit](https://github.com/weirdgloop/osrs-dps-calc/blob/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib/PlayerVsNPCCalc.ts):
truncate the ordinary base max, apply Crystal blessing, then apply Salve 6/5
or 7/6 and truncate again. The previous PBIS implementation incorrectly kept
the fractional base through Salve. Fang then shrinks the completed integer max
by its 15% integer floor. This affects ordinary damage as well as separate
special calculations which obtain their base from the ordinary stat pipeline.

The Wiki share ElysianBurningPool was inspected in the actual calculator. It uses
Attack92, Strength99, Piety and Lunge. The Serp item image explicitly identifies
its **uncharged** variant. Its total Strength is143; charged Serp adds5 (148),
while Faceguard adds6 (149). At that effective Strength132, the integer base
maxima are43/44/44, Salve maxima51/52/52 and Fang maxima44/45/45. At attack
roll29311 and defence roll20070, ordinary DPS is7.171452/7.312069/7.312069.
Finite TTK rounds to106.6/104.6/104.6 seconds. These checkpoints now have an
isolated regression using production effect/distribution/TTK helpers.

The earlier plugin's charged-Serp DPS7.4527/max46 is reproduced by its incorrect
fractional Salve order. Correcting that order gives the charged-Serp/Faceguard tie
above. Defence fill can prefer charged Serp's defensive bonuses without sacrificing
ordinary damage. Uncharged Serp's worse output must not be accepted as a tie.

See [combat-style audit](COMBAT-STYLES.md) for label and option coverage. The special
recommender calculates separately using copies of worn armour for weapon switches.
Ordinary results do not add its burst DPS. Final defensive-fill gear is reevaluated
before pairing the displayed items with ordinary results.

## Stats and bank presentation

Calculated Loadout stats contains only the six result fields. Hover the stats
area for modelling assumptions, including Vardorvis snapshot DPS versus changing
Defence during TTK. Separate requirement/regeneration warnings remain visible.

After generation the bank view groups eligible selected reduction weapons under
**Defence reducing items**. Vulnerability includes the available bank stacks for
one standard cast: 5 Water, 5 Earth and 1 Soul rune. Counts do not imply repeat
casts or guaranteed successful hits. Weapon charge contents remain unknown.
Only positive-quantity native bank items are shown for withdrawal. A weapon or
rune stack already carried/equipped needs no invented bank entry. A stack shared
with ordinary gear, special recommendations or spell supplies appears once in
the reduction section. No selected withdrawable items means no section.

Changing/resetting/hiding selections or invalidating results clears this section
until regeneration. The ordinary bank gear/supplies view remains available.
