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
