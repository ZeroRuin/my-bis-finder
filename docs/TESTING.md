# Local testing

Open the source folder in IntelliJ, reload Gradle and run **run**. Disable the
installed Plugin Hub copy while using the development copy. Use real accounts
and assignments for interface checks; no simulation controls are included.

## Automated verification

Run these Gradle tasks before releasing a code change:

```text
compileJava compileTestJava
mechanicsSelfTest
gimStorageSelfTest
itemExclusionSelfTest
loadoutStatsSizingSelfTest
slayerTaskSelfTest
reviewedSlayerLocationsSelfTest
slayerGsonSelfTest
hueycoatlSelfTest
theatreOfBloodSelfTest
raidOptionsSelfTest
targetStyleImmunitySelfTest
```

Run the data-tool checks with:

```text
python -m unittest discover -s scripts -p "test_*.py"
```

`slayerLocationCoverage` refreshes the committed coverage report.
`slayerUnresolvedAudit` writes `SLAYER-UNRESOLVED-VARIANTS.json`; pass it to
`scripts/write_slayer_review.py --audit FILE.json` for a readable checklist.
These audit outputs are generated locally and ignored by Git.

The current review contains 470 completed location decisions. A changed monster
snapshot disables the reviewed profile mappings and Konar's automatic uniqueness
decisions. After combat-data updates, regenerate/review mappings and rerun audits
and tests. Do not just update hashes to bypass validation. See the
[data notes](../SLAYER-DATA-NOTES.md).

## Loadouts and bank view

1. Scan the bank, choose a target, generate and select a combat style.
2. Confirm gear, stats, ammunition/spell, prayer and validation use that target.
3. Switch styles; cached results and the BEST indicator should stay consistent.
4. Open the BiS bank view, check normal withdrawal actions, then toggle it off.
5. Select Magic with a long spell name. Confirm the stats panel grows without
   clipping, shrinks for shorter text and remains readable after resizing.

## Exclusions

1. Exclude several sidebar or recommended-bank items before regenerating.
   Results must stay visible with the warning, including after switching styles.
2. Regenerate; excluded gear and supplies must disappear. Supported owned
   alternatives should be selected where available. Excluded runes block spells
   requiring them unless allowed equipment supplies them. Stored powered weapon
   charges are separate.
3. Remove an entry in **Excluded items**, regenerate and confirm eligibility is
   restored. Clear the field to clear all exclusions.
4. Check potion-dose grouping and persistence across restart/configuration profile.
5. Confirm ordinary bank menus and **Other items** have no exclusion action.

## Group storage

1. On a GIM account, scan personal bank and shared storage with the option enabled.
2. Check recommendations and normal item icons in both storage interfaces.
3. Check cross-store references identify the other store and cannot withdraw items.
4. Check exclusions apply across both stores, including reference icons.
5. Check deposit/withdrawal updates and reopening after teammate changes.
6. Log out/in; preferences persist, but both snapshots require new scans.

## Slayer assignments

1. Check the task with the gem/helmet/master, then enable **Load Slayer Task**.
2. On an unrestricted Greater demons task, check regular variants and supported
   K'ril, Tstanon, Skotizo and Tormented demon alternatives. Select one, generate
   and confirm the loadout uses it.
3. With suitable real assignments, check these filters:
   - Catacombs Greater demons: levels 100/101/113 and Skotizo; no GWD alternatives.
   - Chasm of Fire Greater demons: level 92.
   - Brimhaven Black demons: level 172; no Catacombs or Wilderness profiles.
   - Krystilia Greater demons: Wilderness regular and Slayer Cave profiles;
     no Skotizo, Tormented demons or GWD alternatives.
   - Taverley Blue dragons: no Vorkath.
   - Hellhounds: Cerberus only for Taverley and its supported Slayer requirement.
4. Check supported superiors and alternatives against the
   [coverage report](../SLAYER-LOCATION-COVERAGE.md).
5. Change assignments/accounts; task, location and dropdown should refresh and
   require regeneration. Completing/cancelling a task clears task-mode choices.
6. Disable task mode; manual search returns. Other masters must not inherit a
   stale Konar restriction. Named Konar boss tasks without an area roll remain usable.
7. An undetected area must keep the picker empty. Unknown variants stay hidden
   with a readable note; restricted tasks must not fall back to unrestricted choices.

## Hueycoatl targets

Search for The Hueycoatl. Confirm three targets: Body (phase 1), Head and Tail.
Generate melee setups for each, including Crush on the Tail. A qualifying single
hit is limited to 9; two-hit weapons can show a combined max of 18. Crush must
strictly exceed the equipped stab, slash, ranged and magic attack bonuses. Other attacks are
limited to 4 per hitsplat, including Earth spells, without the crush zero-to-one
benefit. Body/Head retain normal damage. Test switching parts
and regenerating, and both bank recommendation views. No push is authorized.

## Seren and Theatre of Blood

1. Search each main Theatre boss: Maiden, Bloat, Nylocas Vasilias, Sotetseg,
   Xarpus and Verzik. Confirm Entry Mode, Normal Mode and Hard Mode are separate.
2. Check all three Verzik phases in each difficulty. Phase 1 has melee max 10
   and ranged/magic max 3 per hitsplat; phases 2 and 3 have no phase-one limiter.
   Multi-hit weapons may display a total max above the per-hitsplat limit.
3. Select each Nylocas Vasilias form. Only its matching style should be available
   as a damaging result; check all three Hard Mode Prinkipas forms too.
4. Switch equal-stat Normal/Hard targets, then regenerate. Check the selected
   mode/form remains labelled correctly and previous results are invalidated.
5. Check Seren with a setup whose pre-transform max is 24 or higher. Damage
   through 24 is unchanged; only higher hits reroll to 22–24.

These are selected-state comparisons at bundled HP, without party-size scaling,
whole-raid timing, room downtime, damage from teammates or Dawnbringer.

## CoX and ToA options

1. Select a non-raid target: no raid controls should appear. Select a CoX target:
   only party HP 99, combat 126, size 1 and unchecked Challenge Mode should appear.
2. Select a ToA boss: only raid level 0, path level 0, size 1 and unchecked Exclude
   Smelling salts should appear. Values must remain separate between the raids.
3. Generate, change any raid input, and confirm the existing result becomes stale
   and needs regeneration. Test changing an input while generation is running too.
4. Compare Tekton solo normal/CM, Olm hands/head and parties of 2–3. Confirm HP,
   target Defence, resulting accuracy and TTK change appropriately after generation.
5. Compare ToA levels 0/150/300 and path levels 0/2. Path changes should not affect
   Warden HP. Check the target HP line and the resulting DPS/TTK.
6. Compare salts on/off with supported potions present, then excluded. Excluding
   salts should use the supported owned boost or base levels, without retaining an
   observed salt boost. Confirm non-ToA targets retain their captured levels.
7. Check all Akkha vulnerability phases, final melee phase, Warden phase labels,
   Zebak Normal/Enraged, and CoX part/variant labels. Confirm wrong styles cannot win.
8. Check input bounds and invalid typed values; an invalid edit must block generation.
   Resize the sidebar and confirm all fields, checkbox labels and stats remain visible.
9. Check phase-two Warden and core-ejected notices. These snapshots still lack
   phase-two accuracy-based damage and actual exposed-core attack mechanics.

## Style immunity and raid input styling

1. Select Tekton and generate. Ranged should be grey, without a calculated Ranged
   loadout; allowed styles must still produce a BEST indicator when available.
2. Click grey Ranged. The gear/stats should be replaced with Tekton's immunity
   message, and the bank view must not retain the previous style's recommendations.
3. Switch to an allowed style, then repeat target/phase switches with Nylocas,
   Akkha and Guardians. Check all three melee buttons where melee is immune.
4. Confirm Zulrah's polearm exception, Guardian pickaxes and leafy weapon/spell
   restrictions are not blanket-labelled immune. No owned equipment should still
   show the existing no-usable-loadout state rather than an immunity message.
5. Check CoX and ToA number fields: outer borders should be gold with dark input
   backgrounds and subdued arrow borders, matching the sidebar rather than white.
