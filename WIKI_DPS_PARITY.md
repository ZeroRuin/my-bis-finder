# OSRS Wiki DPS parity programme

Reference snapshot supplied by the user: `weirdgloop/osrs-dps-calc` main-branch ZIP.
Upstream project: https://github.com/weirdgloop/osrs-dps-calc
Upstream license: GPL-3.0.

My BiS Finder keeps its RuneLite-specific bank, unlock, Slayer-task, optimizer, inventory and UI layers. The upstream calculator is used as the combat-math reference specification. Existing PBIS Wiki-locked regressions remain authoritative regression checks during the port.

## First source-to-source parity slice — alpha50.4.50.1

Ported from the upstream ratbane rules in `BaseCalc.ts` and `PlayerVsNPCCalc.ts`:

- Bone mace / Bone shortbow / Bone staff are the rat-bone weapon family.
- Rat-bone weapons receive a flat +10 max hit against monsters with the `rat` attribute.
- Rat-bone weapons cannot damage non-rat monsters.
- PBIS melee and ranged production calculators now enforce the above rules.
- Bone staff powered-magic calculation is intentionally deferred to the Magic parity slice so it can be ported with the upstream powered-staff max-hit and attack-speed pipeline rather than approximated.

## Audit queue

1. Target eligibility/immunity layer: vampyre tiers, silver/vampyrebane, Corp, flying/polearm/salamander, Guardian pickaxe, target-specific immunities.
2. Melee ordering: Gadderhammer/Verac distributions and charged Wilderness chainmace plus Avarice/Revenant ordering have landed through alpha50.4.50.32; live-HP/encounter-input effects such as Dharok remain queued.
3. Ranged special attacks remain outside the sustained-DPS optimizer. Chinchompa distance/stance accuracy, charged revenant weapon Wilderness modifiers, Avarice/Revenant exclusivity, Slayer additive ordering, and Tonalztics 75% max landed in alpha50.4.50.26.
4. Magic ordering: powered staves, Bone staff, Tumeken's shadow, tomes, demonbane/Mark of Darkness, spell-specific distributions.
5. Hit distributions and limiter ordering.
6. Special attacks (separate phase; PBIS currently optimizes auto-attacks).

## alpha50.4.50.2
Direct Wiki-source port: vampyre tier eligibility, silver/vampyrebane rules, Efaritay interactions, and melee accuracy/damage modifiers.

## alpha50.4.50.4 — Corporeal Beast
- Ported the Wiki `isWearingCorpbaneWeapon()` eligibility rules: Fang on Stab, halberds on Stab, spears (except Blue moon spear) on Stab, and all Magic.
- Ported Corp's integer `divisionTransformer(2)` ordering for ordinary single-hit damage: accurate-zero conversion happens first, then each non-corpbane hitsplat is floored after division by 2.
- Ranged enchanted-bolt EV now has a Corp-aware path. Ruby bolt effects remain full damage because the Wiki applies Corp reduction before the Ruby transform; other currently modelled bolt effects are reduced at the earlier transform stage.
- Complex non-corpbane multi-hit melee distributions (Scythe/split-hit/Soulreaper stateful ramp) are deliberately not force-fit in this slice; they remain queued for the shared HitDistribution port.

## alpha50.4.50.4
- Corrected the Corp Fang constrained-distribution regression fixture. For uniform raw hits 6..40, Wiki `divisionTransformer(2)` produces mean `394 / 35 = 11.257142857142858` after per-hitsplat integer truncation.
- Production Corp transformation code was already correct; this was a self-test expected-value error only.

## alpha50.4.50.4
Ported the shared PlayerVsNPCCalc.isImmune style/target layer for fixed-ID melee/ranged/magic immunities, flying melee restrictions and polearm/salamander exceptions, Zulrah polearm exception, Aviansie salamander exception, and Chambers of Xeric Guardian pickaxe-only eligibility. These checks are applied in the production calculators before a candidate can retain non-zero DPS.

## alpha50.4.50.6
- Ported Wiki `demonbaneFactor()` vulnerability scaling for Duke Sucellus (70%), Yama (120%), Yama void flare (200%), and Ice Demon (115%).
- Arclight/Emberlight and Silverlight/Darklight now scale their demonbane percentage through target vulnerability.
- Scorching bow +30% demonbane accuracy/damage is now represented through the same target-vulnerability stage.

## alpha50.4.50.6 — shared hit-distribution foundation
- Added Java `HitDistribution` / `Hitsplat` / `WeightedHit` primitives modelled on the Wiki calculator `src/lib/HitDist.ts` architecture.
- Supports linear hit distributions with explicit inaccurate outcomes, accurate-zero conversion, multi-hitsplat zip/cumulative operations, flattening, EV/min/max, and per-hitsplat transforms.
- Ported shared transformer primitives: flat limit, multiply/divide with integer truncation/minimum semantics, flat add, linear-min, and capped reroll.
- Corp ordinary/Fang average helpers now execute through the shared distribution + per-hitsplat divide transform instead of bespoke summation loops.
- Added regression locks for probability preservation, accurate-zero EV, multi-hit zip/cumulative behaviour, per-hitsplat division, cap transforms, and integer multiplier truncation.

## alpha50.4.50.7
Migrated Scythe, Dual macuahuitl, and standard two-hit melee EV scoring onto the shared HitDistribution layer. Added Earthbound tecpatl to the upstream standard two-hit family.

## alpha50.4.50.9.4 — Keris + enchanted bolt HitDistribution migration
- Keris critical branch now mirrors Wiki `standardHitDist.scaleDamage(3)` before accurate-zero conversion, rather than approximating the proc as a fresh uniform `0..3*max` roll.
- Enchanted bolt EV now runs through `HitDistribution` transforms in Wiki ordering: ordinary bolt transforms -> accurate-zero -> Corp per-hitsplat divide -> Ruby transform.
- This corrects tiny rounding/distribution differences in Opal, Pearl, Dragonstone and Onyx EV while preserving Ruby and Diamond behavior.


## alpha50.4.50.9.4 — Wiki NPC damage transforms
- Added shared post-attacker NPC hitsplat transforms from `PlayerVsNPCCalc.applyNpcTransforms`.
- Production Ranged and Magic now apply the shared target transform stage after attacker/bolt distributions.
- Added Zulrah capped reroll, Fragment of Seren limiter, Kraken ranged reduction, Tekton/CoX crystal/Olm reductions, Ice Demon non-fire reduction, Nightmare totem magic doubling, Slagilith reduction, and bundled Wiki `flat_armour` import/application.
- Melee migration remains intentionally incremental; complex melee distributions will move through this layer in the next slice.

### alpha50.4.50.9.4
- Removed the ambiguous `apply(..., String, ...)` overload from `WikiNpcTransforms`.
- Name-only Magic callers now use `applyByWeaponName(...)`; equipment-aware Ranged/tests retain `apply(...)`.
- This prevents bare `null` test arguments from matching two unrelated reference-type overloads.


## alpha50.4.50.10
Melee attacker distributions (standard, Fang, Keris, Scythe, Dual macuahuitl, standard independent two-hit, Soulreaper stack hits) now flow through Corp and the shared Wiki NPC post-hit transform stage before expected DPS is collapsed. This preserves per-hitsplat caps, armour and reductions.


## alpha50.4.50.11 — attack speed / rat-bone powered staff
- Ported the Wiki `Equipment.calculateAttackSpeed` Scurrius override: NPC id 7223 + Bone mace/Bone shortbow/Bone staff => 1 tick, after ordinary stance speed changes.
- Added Bone staff as a level-50 powered staff with base max `floor(Magic/3)+5`.
- Added its ratbane +10 max hit, rat-only eligibility, and 1-tick Scurrius interaction.
- Melee and ranged rat-bone weapons now use the same target-specific attack-speed helper.


## alpha50.4.50.11.1 — Wiki-authoritative monster catalogue
- Bundled Wiki DPS `monsters.json` is now the primary target catalogue.
- RuneLite NPC cache is fallback-only for NPC ids absent from the bundled Wiki pack.
- Wiki NPC id + version are preserved so mechanics keyed to exact target ids cannot be shadowed by a lower-detail cache record.
- Explicitly distinguishes Scurrius Solo (7222), Scurrius Group (7221), and Giant rat (Scurrius) (7223).
- Bone mace/shortbow/staff receive the 1-tick override only against NPC 7223; Scurrius himself and ordinary rats retain normal speed.

## alpha50.4.50.12 — signed accuracy-roll parity
- Added `WikiAccuracyRoll` from upstream `BaseCalc.getNormalAccuracyRoll` / `getFangAccuracyRoll`.
- Melee, ranged, standard magic and powered magic now share the same signed-roll hit-chance implementation.
- Preserves the Wiki calculator's `+2` normalization for negative rolls and both-negative roll reversal instead of clamping negative rolls away.
- Fang now uses the same signed-roll branches as upstream rather than a positive-only helper.
- Added permanent positive/negative roll regression locks.

## alpha50.4.50.13 — integer checkpoints + parity diagnostics
- Added `WikiIntegerStages` mirroring Wiki `trackFactor` and `trackMaxHitFromEffective` truncation checkpoints.
- Melee, Soulreaper and Ranged ordinary max-hit calculations now use the shared `(+320)/640` integer checkpoint.
- Copy diagnostics now exposes Wiki-style Ranged and Magic calculation stages alongside the existing Melee parity audit.
- Magic results retain effective Magic, attack ticks and pre-NPC-transform max for diagnostics.
- Ranged results retain both effective accuracy and effective damage levels.

## alpha50.4.50.14 — cast-stance attack-speed parity
- Standard spell autocasts use 5 ticks by default.
- Harmonised Nightmare staff uses 4 ticks only with the standard spellbook.
- Harmonised Ancient autocasts remain 5 ticks.
- Twinflame staff spellcasting uses 6 ticks.
- These rules mirror `Equipment.calculateAttackSpeed()` in the upstream Wiki calculator.

## alpha50.4.50.16 — Wiki Equipment Engine migration
- `WikiEquipmentEngine` is the Java parity boundary for upstream `src/lib/Equipment.ts`.
- Bundles the upstream generated equipment dataset, 331 canonical equipment alias groups, and the exact ranged weapon→ammo ID table.
- EquipmentIntelligence resolves Wiki canonical aliases before raw-stat lookup.
- Ranged ammo compatibility now consults the Wiki ID table before legacy PBIS fallback classification.
- Production aggregate transforms now cover Tumeken's shadow (3x/4x ToA + 100% cap), Keris partisan of amascut outside-ToA raw reductions, Dinh's defensive-sum strength, Virtus Ancient magic damage, Elite Void Mage visible 5%, and charged Dizana quiver ammo bonuses.
- PBIS bank ownership, quest/unlock policy, optimizer/search and UI remain outside the Wiki engine.
- Source-derived port: weirdgloop/osrs-dps-calc, GPL-3.0. Preserve attribution/license obligations when distributing.


## alpha50.4.50.16 — Equipment.ts full API parity audit
- Added full data-driven equipment aggregation over canonical Wiki equipment records.
- Ammo-slot ranged stats are included only when Wiki ammoApplicability returns INCLUDED.
- Added blowpipe dart-strength hook matching itemVars.blowpipeDartId semantics.
- Centralized exact Equipment.ts attack-speed ordering and minimum 1-tick clamp.
- Added noStatExceptions, getHighestOffensiveStyle parity, weapon special-attack cost table, and Gauntlet/Corrupted Gauntlet equipment ID sets.
- Existing PBIS bank/unlock/optimizer policy remains outside the Wiki equipment engine.

## alpha50.4.50.18.1
Started structural PlayerVsNPCCalc.ts port via WikiPlayerVsNpcCalc.java: attacker->NPC transform ordering, expected damage/DPS timing, Giant rat guaranteed accuracy, and explicit Tormented Demon phase rules. Encounter phase remains an explicit input until PBIS UI/state exposes Wiki monster inputs.


## alpha50.4.50.19
- Migrates production melee attacker-distribution construction into `WikiPlayerVsNpcCalc` for standard, Scythe, Dual macuahuitl, ordinary two-hit, Fang, Keris and Obsidian paths.
- Routes Corp then generic NPC transforms through the central Wiki pipeline boundary.
- Corrects the current upstream `BLOWPIPE_IDS` set to 12926, 28688, 31575, 31579 and 31583; removes stale 12924/28691 from the aggregate dart hook.
- Adds parity regressions comparing migrated attacker distributions with the previously validated distributions.

## alpha50.4.50.20
- Migrates production ranged final-distribution construction into `WikiPlayerVsNpcCalc`.
- Splits enchanted-bolt processing into source-ordered stages: non-Ruby transforms, modern accurate-zero conversion, Corp per-hitsplat division, Ruby Blood Forfeit, then generic NPC transforms.
- Preserves the Wiki rule that Ruby Blood Forfeit remains full damage against Corp while the ordinary projectile is halved.
- Adds permanent ordering regressions for Corp+Ruby, non-Ruby pre-accurate-zero processing, and NPC transforms occurring last.

## alpha50.4.50.21
Magic and powered-Magic final hit distributions now route through WikiPlayerVsNpcCalc: base distribution, Magic Dart accurate-zero exception, modern accurate-zero conversion, then NPC transforms. Existing spell/loadout max-hit and accuracy stages remain unchanged.

## alpha50.4.50.21.1
Corrects the modern-Magic accurate-zero regression fixture. At 50% accuracy and max hit 10, converting only the successful zero roll to 1 produces expected damage `28/11 = 2.5454545...`; the production distribution was already correct.

## alpha50.4.50.22
- Adds `WikiMagicAccuracy`, a source-ordered Java port boundary for `PlayerVsNPCCalc.getPlayerMaxMagicAttackRoll()`.
- Production standard-spell and powered-staff calculations now share prayer/effective-level, Magic Void, additive Salve/Smoke, dragon-hunter, Slayer, demonbane, Tome of Water and elemental-weakness integer stages.
- Elemental weakness adds a percentage of the original base roll at the final stage, matching upstream rather than multiplying the already-modified roll.
- Corrects Tome of Water's applicable accuracy factor to the upstream `6/5` (20%); its damage factor remains `11/10`.
- Forinthry Surge and manual-cast Accurate stance remain explicit future account/stance inputs rather than being assumed.

## alpha50.4.50.23
- Adds `WikiMagicDamage`, the shared source-ordered boundary for standard-spell and powered-staff maximum/minimum hits.
- Ports flat spell additions, combined equipment/prayer thousandths, Smoke/Twinflame, Salve/Slayer precedence, dragon-hunter factors, elemental weakness from the saved base max, Sunfire minimum ordering and charged Tome `11/10` ordering.
- Production Magic no longer reapplies the legacy loadout damage multiplier after Wiki equipment aggregation, preventing Elite Void/Virtus and Salve/Slayer double counting.
- Corrects Sanguinesti and Warped sceptre base-max formulae and removes the second production Bone staff ratbane addition.
- Charge, Sunfire activation, Forinthry Surge and encounter-specific minimum-hit inputs remain disabled until PBIS exposes their live state. Mark of Darkness remains a distribution transform for the next slice.

## alpha50.4.50.23.1
Test-only compile hotfix: the Dragon hunter wand regression now creates its dragon target inside `supplyChecks()` instead of referring to the `main()` method's local fixture. Production mechanics are unchanged.

## alpha50.4.50.24
- Ports sustained Magic `getAttackerDist()` effects into `WikiPlayerVsNpcCalc` before generic NPC transforms.
- Mark of Darkness now modifies every accurate Demonbane damage roll with the Wiki's two integer truncations; Purging staff uses 50% instead of 25%, and target demonbane vulnerability is applied at the second truncation.
- Adds Sanguinesti/Holy sanguinesti 20% `+8` damage branches, full Ahrim + Amulet of the damned 25% `floor(hit*13/10)` branches, Twinflame's separate `floor(hit*4/10)` hitsplat, and Brimstone ring's 75/25 normal/reduced-defence accuracy mixture.
- These effects now occur before NPC hitsplat limiters, allowing multi-hitsplat target transforms to operate in source order.
- Removes the previous aggregate Mark/Twinflame EV multipliers from production. Special attacks and encounter-state-only branches remain excluded.
- Seeds a complete owned Ahrim/Amulet of the damned optimizer family when Ahrim's staff is evaluated, so ordinary four-choice beam pruning cannot hide the set effect.

## alpha50.4.50.27
- Ports Gadderhammer's Shade-only 95% `floor(hit*5/4)` and 5% `hit*2` branches.
- Ports full Verac's 75% ordinary and 25% guaranteed `1..max+1` distribution.
- Both branches run before accurate-zero conversion and shared NPC hitsplat transforms.

## alpha50.4.50.28

- Ports the sustained melee Wilderness path for charged Viggora's chainmace and Ursine chainmace: each receives the Wiki `3/2` accuracy and max-hit stages only against conservatively identified Wilderness targets.
- Ports Amulet of avarice's Revenant-only `24/20` accuracy and damage stage as the exclusive necklace/task branch, taking precedence over Salve and Slayer bonuses.
- Charged/uncharged and Wilderness/non-Wilderness controls are locked into the mechanics self-test.

## alpha50.4.50.29

- Ports the Wiki's ordinary sustained demonbane claw branch: Bone claws and Burning claws add the 5% demonbane vulnerability stage to both accuracy and damage against demon targets.
- Non-demon controls remain unchanged; special-attack-only claw effects are intentionally outside this release.

## alpha50.4.50.30

- Ports the Wiki Crystal blessing melee max-hit stage: blessing plus Crystal helm/body/legs contributes 1/3/2 pieces, yielding the integer `(40 + pieces) / 40` multiplier.
- The stage is applied before later melee-exclusive modifiers and does not affect accuracy or ranged Crystal armour calculations.

## alpha50.4.50.32

- Adds the Wiki Dharok HP-dependent sustained multiplier as a reusable boundary with clamped current-HP inputs. Production scoring remains deferred until live current HP is exposed.

## alpha50.4.50.31

- Seeds a complete owned Crystal blessing + Crystal helm/body/legs bundle into the melee optimizer beam, so the new max-hit stage cannot be pruned before exact target evaluation.
- Seeds the complete Verac armour/flail family before melee beam pruning.

## alpha50.4.50.34

- Removes the Dharok HP planning control and HP-dependent set multiplier from production BiS scoring.
- Dharok armour pieces remain ordinary individually scored equipment; no Dharok four-piece bundle is forced or treated as a BiS requirement.
