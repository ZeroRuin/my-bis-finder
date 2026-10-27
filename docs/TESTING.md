# Local testing

Open the source folder in IntelliJ, reload Gradle and run **run**. Disable the
installed Plugin Hub copy while using the development copy. Use real accounts
and assignments for interface checks. Planning uses actual bank, inventory and
equipped ownership, with optional GIM shared storage. Temporary synthetic-bank
controls have been removed.

## Sidebar cleanup verification

1. Confirm no test-bank option exists, including in a profile that previously
   enabled it. Open the actual bank and verify recommendations use owned gear.
2. Scan a blowpipe: Advanced shows a red ! with an availability tooltip. Only
   bank-scanned blowpipe families show ammo controls; unknown ammo stays unknown.
3. Scan supported reduction gear/supplies. Controls default off and show only
   while owned and unhidden. Hide an active reduction in RuneLite settings:
   its value clears, the old result becomes stale, and its control disappears.
   Unhide it and confirm it returns at zero. Hiding all owned reductions removes
   their alert, unless a blowpipe control is still available.
4. Select two reduction types; collapse Advanced and check “2 selected”. Reset
   reductions and check all amounts clear without changing fill/ammo/raid settings.
5. Switch profiles and restart. Hide preferences and amounts remain independent.
   Logout/unknown bank state must not apply saved effects; confirmed missing
   ownership clears only affected amounts. No reduction paragraph appears in stats.
6. Check alert placement and tooltips at normal sidebar width in RuneLite.
   Headless Swing checks passed with an external config-service double; full
   client compilation/layout checks were unavailable locally.

## Bank section order and Other items setting

1. Generate with a selected drain: equipment appears above Defence reducing
   items, followed by Recommended supplies. A shared weapon/rune stack appears once.
2. In the planner bank view, Other items defaults hidden (heading and items).
   Disable Hide Other items in RuneLite settings and confirm they reappear
   immediately without regeneration. Toggle back, then switch config profiles.
3. Turn off the planner bank view and confirm the ordinary bank is restored.
   Repeat in supported GIM storage; opening/closing either view must preserve
   native withdrawal actions and normal item visibility.

## Stats and reduction bank section

1. Generate melee/ranged/magic results and confirm only the six stat fields are
   printed. Check Vardorvis, Dharok and supported raid/Guardian notes appear in
   the stats tooltip instead. Empty/missing-requirement warnings remain clear.
2. Select an owned reduction weapon, regenerate and open the planner bank view.
   The new section contains its real bank stack with normal native withdrawal
   actions. A weapon also recommended for direct specials appears once.
3. Select Vulnerability with known loose supplies: Water/Earth/Soul stacks appear
   in the section. Spell supplies shared with the normal magic loadout appear
   once. No new empty Recommended header should remain.
4. Reset/hide the effect or confirm ownership loss: the old section clears until
   regeneration. With all reductions off, ordinary gear and supplies remain.
5. Test alternate owned weapon IDs, exclusions, placeholders, carried/equipped
   items, profile changes and optional GIM views. No unavailable bank stack may
   gain a withdrawal entry. Verify layout and clicking in the real client.

## Automated verification

Run these Gradle tasks before releasing a code change:

```text
compileJava compileTestJava
defenceReductionBankItemsSelfTest
ttkPerformanceSelfTest
salveRoundingSelfTest
combatStyleLabelsSelfTest
poweredStanceSelfTest
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
combatSearchSelfTest
specialAttackSelfTest
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
9. Check P2 Magic/Ranged show 100% accuracy and damage changes with accuracy gear.
   Core melee damage must always equal its displayed maximum per attack. Core TTK
   assumes continuous exposure; it does not simulate ejection windows.
10. Change average party Mining on a Guardian; regenerate and check target HP.
    Your own Mining and pickaxe tier determine per-hit damage.

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

## Combat and search test build

- Search `KBD`, `Cerb`, `Huey`, `Vorktha`, `ToA`, `CoX`, `ToB`, `ToA Zebak`
  and `ToB Verzik`. Raid prefixes must never return another raid or an ordinary
  non-raid target. Suggestions still require an explicit target/phase selection.
- Check charged and uncharged locked quivers retain their different Sunfire
  bonuses with eligible ammunition. Ordinary ancient capes retain their stats
  without receiving a different god cape's Charge effect; Deadman is excluded.
- Check all nine Vasilias forms preserve difficulty/style and use the actual NPC
  form identity. The source JSON and reviewed Slayer mappings remain unchanged.
- With Moons pieces, confirm ordinary stats apply without full-set speed or burn bonuses.
- Test complete Dharok at full and low HP. Generate again after HP changes; the
  result uses captured HP and does not assume deliberate damage during combat.
- Atlatl/Hunter's spear damage uses Strength, melee Strength gear and the matching
  Salve/task damage modifiers; accuracy remains Ranged. Eclipse burn is excluded
  from ranking. Special rotations and damage over time remain future work;
  thralls and defence-drain value are out of scope.

## Combat/search test 2 follow-up

- `tombs`, `amascut` and `tombs of amascut` show the same complete filter as `toa`.
  `chambers`, `xeric` and `chambers of xeric` match `cox`; `theatre`, `blood` and
  `theatre of blood` match `tob`. Check terms with a following boss name too.
- Change every raid number and both checkboxes. Log out/in, restart the plugin
  and restart RuneLite: values should remain. CoX and ToA values stay independent.
- Switch RuneLite configuration profiles: each profile should load its own saved
  settings, or defaults on first use. Changes still require loadout regeneration.

## Spec and attack row test 1

- Generate melee, Ranged and Magic gear. The fourth row shows Spec | Spell | a
  double-width attack-style/stance button. Spell is populated for Magic only.
- Hover Spec: check weapon, burst DPS, energy, special stance and required ammo.
  Only owned usable supported weapons are eligible; unmodelled owned specials
  are listed. Normal loadout DPS must remain unchanged.
- Check DDS/claws, Voidwaker, a two-handed switch and ranged ammo switches.
  Zaryte ruby assumes starting target HP; no full-fight ruby rotation is claimed.
- Confirm the special weapon/ammo are included in the selected bank setup.
- Exclude the best special, regenerate, and check that it is no longer selected.
- Change target/style and regenerate quickly: no spell sprite from an older
  generation should replace the current spell. Immune targets clear the row.
- `SpecialAttackSelfTest` covers distribution boundaries, Zaryte bolt effects,
  owned-weapon selection, score units and row proportions.


## Local HP/fill/drains test batch

See [HP-FILL-DRAINS.md](HP-FILL-DRAINS.md) for the modelling choices, five new
local self-test tasks, validation limits and real-client checklist. This source
build remains local and has not changed the Plugin Hub submission.

## Local test 4 comparison

See [combat styles](COMBAT-STYLES.md) and [mechanic notes](HP-FILL-DRAINS.md).
Use the Wiki share with the same item charge state and actual account boosts.
The supplied share selects uncharged Serp; use charged Serp to compare the
plugin's charged helmet. Known Charged/Uncharged dataset variants now appear in
sidebar gear tooltips, without inventing remaining charges.

Check Lunge on Fang, with Stab/Aggressive on hover. Test other modelled weapon
styles and powered Accurate/ordinary Spell labels. Normal loadout DPS excludes
the special weapon's separate burst score. Verify the final displayed Defence-fill
gear has the same accuracy, max hit, speed, ordinary DPS and TTK as its baseline.

Ten isolated Java suites and 34 Python tests passed in the local environment;
175 Java sources passed Java 11 syntax parsing. The Salve/style/powered suites
compiled production helpers using an EquipmentInventorySlot compile-only enum
double outside this project. A full compiler attempt remains blocked by missing
RuneLite/Gson/injection dependencies. The existing full Java regression suites,
including the new special-isolation assertions, and real RuneLite UI were not run.

## Local test 5 checks

Local tasks: `vardorvisHpSelfTest`, `vardorvisShortlistSelfTest`,
the then-temporary test-bank tier check (now removed), plus the previous focused checks. Thirteen isolated Java
suites passed (some with external compile-only API/UI doubles); all 34 Python
tests passed and 181 sources passed Java 11 syntax parsing. The full production
compile was attempted and blocked by missing RuneLite, Gson and injection
libraries. The dependency-backed full regression suite was not run.

In RuneLite, check Quest/Post-quest/Awakened HP defaults, lower HP generation,
variant changes, logout/restart and profile restoration. Check source changes,
cancellation and reduced-bank ownership controls. Compare a generated fixed set
at matching starting HP against an independent calculator. Measure real-bank
and test-bank first-generation times and responsiveness. A full broader-search
comparison of the checkpoint heuristic remains outstanding.

## Local test 6 checks

Passed 15 focused Java self-test suites (including expanded drain plans and
Void/elite Void), plus isolated profile/ownership Swing checks, CoX intermediate
skill scaling, and actual melee/ranged/powered calculator checks with an external
account API double. These doubles are outside the project and not packaged.
All 34 Python tests, Java 11 syntax parsing of 186 sources and whitespace checks
passed. Actual combat calculators and their plain-Java dependencies compiled in
that isolated harness. A full production compile was attempted but remains
blocked by missing RuneLite/Gson/injection libraries. Full client integration and
the original dependency-backed regression suite were not run.

For manual testing, first compare one drain at a time against the Wiki at the
same HP/raid settings, then combine effects in the documented order. Confirm
Tonalztics counts hitsplats, BGS/Seercull/Ayak use damage amounts, curse toggles
are alternatives, and ordinary stats still exclude special damage. Check missing
weapons, charged/active variants, exclusions, profile switches, logout/restart
and unknown versus complete bank scans. Check dropdown popup colours, typed
number values and arrows, Vard HP, raid controls and per-blowpipe ammo choices.
Standard 10% Vulnerability only; no enhanced Tome of Water assumption.

## Local test 7

The new `twistedBowRoundingSelfTest` passes against a fixture generated from
PlayerVsNPCCalc.tbowScaling at Wiki commit
89c3e25b344aea90d0189746e4b5f73dde0f0383, covering every Magic value 0–350.
The actual ranged calculator also reproduces SpicesFlowersDisposal with an
external account API test double (92 Ranged, Rigour, identical equipment):
Defence187, attack roll48405, defence roll28224, max82, accuracy70.84452341%,
DPS9.68493003 and TTK29.11521551s. The ordinary Void/drained-zero calculator
integration check still passes. Full RuneLite dependency-backed build remains
unverified here. Rerun the same share in the development client.

User-reported real-client checks: BGS controls operate with real ownership;
Vorkath with the test bank and two warhammer hits matches the Wiki. K'ril's
newly identified Tbow rounding discrepancy is fixed locally; awaiting retest.

## User testing follow-up — 19 October 2026

User reports matching Wiki calculations for K'ril using Elder maul, Dragon
warhammer, Arclight, Emberlight and Tonalztics in the tested setups/attack style.
Eye of Ayak and Seercull comparisons also match; the target/setup for those two
checks was not specified. These reports validate the tested cases, not every
possible combination or target.

Deferred to the next update phase: investigate Yama phases and target stats.
The user could not match defence-reduction results against the Wiki, but did
not check an unmodified/no-reduction baseline. Root cause remains unresolved:
first compare identical phase, base stats, gear and account settings with all
reductions off, then introduce individual reductions. Do not change Yama in
this batch based on this report.

## Local test 8 checks

`voidLoadoutFamiliesSelfTest` checks normal/mixed/elite melee and ranged families,
ornaments, missing/blocked pieces, required target gear, immutable seeds and
DHCB ammo fallback. Actual optimizer checks with external account/ownership API
doubles confirm independent melee families and a Vorkath fixture using 92 Ranged,
Rigour, Salve(ei), normal ammo and both weapons. Elite Void/DHCB wins that fixture
with Ruby included and excluded. This is not a global-optimum proof or a promise
for every level/gear/bolt/drain selection. A real-client comparison remains needed.

The temporary bank coverage check confirms all 218 neck IDs, 127 ring IDs and
nine drain-weapon families remain, while activity-only arrows/Gauntlet gear are
removed. Snapshot checks cover the new exclusions. Full RuneLite compilation
remains unverified in this environment without its dependencies.

### Rapid bank withdrawals

Run `bankWithdrawalSelfTest` for personal/shared bank click consistency. This checks native and low-priority widget operations, valid and stale slots, reused/hidden widgets, unknown bank state and inactive-planner behaviour.

In RuneLite, open the planner bank view and rapidly left-click several different equipment/supply stacks. Repeat while emptying stacks, with placeholders enabled and disabled, and with Other items visible and hidden. Verify each withdrawal matches the clicked item. Also check right-click Withdraw-1/5/10/X/All, noted withdrawal, an open menu during a bank update, personal/shared storage, and returning to ordinary tabs/search. A stale click may be ignored; it must never redirect to another item.

The October 2026 assistant check used an extracted handler with API doubles and Java 11 compilation settings; it was not a dependency-backed RuneLite build or an in-client reproduction. Run the Gradle task and client checks locally before release.

Follow-up bank test: generate a Bowfa loadout while a blowpipe with a saved dart selection is banked. Withdraw the blowpipe and confirm results stay available; deposit it and confirm the saved dart selection returns. A manual dart edit must still require regeneration. Test Ring of shadows charged and uncharged independently, withdrawing the final copy with placeholders enabled/disabled. Only an existing native placeholder may remain visible; no replacement withdrawable widget is created.


### Fractured Archive

Run `gradle fracturedArchiveSelfTest` alongside the existing mechanics, raid,
Slayer and bank-withdrawal suites. It covers the supplied HP tables, saved-party
migration, filters, floors, weapon distributions, ammunition and supported rules.
In RuneLite, check `tfa` and `tfa ocellus`, party changes and profile restoration;
compare charged/uncharged weapon ownership, Zeal and fragment unlocks; confirm
The Blade shows DPS with no finite kill time. Recheck rapid bank withdrawals,
loadout persistence and withdrawing/depositing an uncharged Ring of shadows.
In-client testing remains necessary; offline checks do not simulate the raid.
