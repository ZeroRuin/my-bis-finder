# My BiS Finder

Account-aware best-in-slot gear planner for RuneLite using the player's bank,
levels, selected target and Slayer task.

## Features

- Finds the highest-DPS owned Melee, Ranged and Magic loadouts for a selected target.
- Uses the player's bank, combat levels, quest and prayer unlocks, and Slayer task.
- Supports target-specific equipment requirements, weapon passives, ammunition,
  elemental weaknesses and multi-hit weapons.
- Adds a native bank view for the selected loadout and recommended supplies.
- Uses an offline OSRS Wiki-derived equipment and monster data pack; it makes no
  live Wiki requests during play.

## Using the plugin

1. Open your bank so My BiS Finder can read the items owned by the logged-in account.
2. Select a target, or enable **Load Slayer Task**.
3. Choose an attack style to display the best owned loadout for that style.
4. Use the highlighted **BEST** style for the highest calculated sustained DPS.
5. Open the My BiS Finder bank view to gather the recommended gear and supplies.

## Privacy

All account, bank and loadout processing occurs locally in RuneLite. My BiS Finder
does not transmit account or bank data and does not make external network requests.

## Development

The project targets Java 11 and RuneLite `latest.release`. Run the plugin through
the Gradle `run` task and execute `mechanicsSelfTest` for the mechanics regression
suite before publishing a change.

## Licence and data

Plugin source is provided under the BSD 2-Clause License. Details for the bundled
offline data pack are recorded in `WIKI-DATA-NOTES.txt` and its manifest.

## r0.9.0-alpha50.4.50.84

- Retains The Leviathan Quest, Post-quest and Awakened targets even though two
  variants share the same NPC ID.
- Condenses same-name, same-level targets when all combat-relevant stats and
  attributes are identical, regardless of model/location variant.
- Removes targets marked unused from the player-facing catalogue.
- Restores target selections by name, variant and level instead of NPC ID.

## r0.9.0-alpha50.4.50.83

- Renames the user-facing plugin identity from Personal BiS to My BiS Finder.
- Updates the sidebar title, RuneLite navigation tooltip, Plugin Hub metadata,
  configuration description and bank-view fallback headings.
- Keeps the established `com.personalbis` Java package and class names unchanged.

## r0.9.0-alpha50.4.50.82

- Removes the developer validation panel and diagnostics-copy control from the
  normal side panel while retaining the internal validation harness.
- Adds Plugin Hub standard-build metadata, a BSD 2-Clause license and a
  root-level plugin icon.
- Aligns the Gradle version with the release and refreshes bundled Wiki-data
  provenance notes.

## alpha50.4.50.7

- Migrates production Scythe and standard two-hit melee expected damage onto the shared Wiki-derived `HitDistribution` engine.
- Dual macuahuitl now models its second splat conditionally from the first accurate splat in the distribution itself.
- Torag's hammers, Sulphur blades, Glacial temotli, and Earthbound tecpatl use independent two-splat distributions.
- Adds Earthbound tecpatl to the Wiki-source standard two-hit family.
- Keeps the existing Wiki-locked Vorkath/Scythe regression checkpoints unchanged.

# Personal BiS alpha50.4.44

Inquisitor parity-audit build. Adds raw Crush/Strength totals and shield state to the synthetic Inquisitor diagnostics, plus an explicit full-armour/no-shield checkpoint matching the supplied Wiki screenshot. No Inquisitor formula change: the screenshot's +163 Crush/+144 Strength omitted Dragon defender; the synthetic fixed setup includes it (+23 Crush/+6 Strength).

# r0.9.0-alpha50.4.28 — Regression fixture correction

Corrects the Wiki-locked Vorkath enchanted-bolt regression fixture to use the validated account visible Ranged level (99). Production combat mechanics are unchanged.

## r0.9.0-alpha50.4.27 — Enchanted-bolt regression lock

- Locks the full Vorkath standard enchanted-bolt validation suite to the OSRS Wiki-verified EV/max-hit checkpoints gathered in alpha50.4.26.2.
- Locks active Onyx Life Leech against K'ril Tsutsaroth: 38.6216% accuracy, 6.933330 EV/attack, 2.311110 DPS, max 42.
- Adds deterministic regression coverage for Pearl Sea Curse on a non-fiery target and active Dragonstone Dragon's Breath on an eligible target.
- Production combat formulas, optimizer behaviour, bank ownership rules, and the synthetic DEV validation harness are unchanged.

## r0.9.0-alpha50.4.26.2 — Synthetic bolt validation render fix

- Prepares synthetic enchanted-ammo candidates alongside the normal intelligence snapshot before optimizer and Swing rendering.
- Bolt Validation no longer depends on the old calculated-alternatives render state.
- No production combat or optimizer formula changes.

## r0.9.0-alpha50.4.26.2 — Complete synthetic enchanted-bolt validation

- DEV Bolt Validation now creates virtual copies of all ten standard enchanted bolt types, so validation does not depend on bank ownership.
- Production optimizer/bank ownership rules are unchanged.
- Validation still enforces crossbow/ammo compatibility.
- Adds explicit proc chance/accuracy behaviour/direct-DPS notes for Opal, Jade, Pearl, Topaz, Sapphire, Emerald, Ruby, Diamond, Dragonstone and Onyx.
- Adds deterministic proc-max diagnostics for Opal, Pearl and Onyx, including target gating.
- Adds regression coverage for the complete synthetic validation suite.

## r0.9.0-alpha50.4.25 — Dragonstone bolt validation

- Adds target-aware Dragon's Breath diagnostics to the DEV bolt harness.
- Dragonfire-immune/dragon/fiery targets report the proc as blocked with 0.00% contribution.
- Eligible targets report the Kandarin-Hard 6.60% after-hit chance and visible-Ranged +20% bonus damage.
- Diagnostic proc max now shows normal max + Dragon's Breath bonus on eligible targets.
- Adds permanent blocked/eligible Dragonstone regressions.
- No optimizer architecture changes.

# r0.9.0-alpha50.4.24 — Onyx Life Leech validation

- Bolt Validation is now target-aware for Onyx bolts (e). On undead targets such as Vorkath it explicitly reports `Life Leech BLOCKED (undead) | proc 0.00%`.
- On eligible living targets it reports the actual PvM activation rate, including `12.10% after hit` when Kandarin Hard is complete.
- Added permanent regressions proving Vorkath receives ordinary Onyx projectile EV only and that the living-target diagnostic exposes the Kandarin-Hard proc rate.
- No optimizer, damage formula, or performance-path changes; this build hardens the Onyx validation harness before Wiki/in-game parity testing.

# r0.9.0-alpha50.4.23 — Diamond max-hit validation

- Bolt Validation now separates **normal max** from the **validated proc max** for Diamond and Ruby bolts.
- Diamond on the validated Vorkath/Rune-crossbow setup now displays `normal max 38 | proc max 43`, matching the in-game POH undead-dummy test.
- Added a permanent Diamond Vorkath regression for 35,824 attack roll / 20,070 defence roll / 71.9860% accuracy / 14.5567675 EV per attack / 4.8522558 DPS / 43 proc max with Kandarin Hard.
- Ruby retains its existing Vorkath mechanics and now has a regression confirming the 100-damage proc maximum.
- No optimizer or production DPS-formula changes in this build; this is diagnostic + regression hardening.

## r0.9.0-alpha50.4.22 — DEV validation cleanup + forced bolt harness

- Replaced the large historical optimizer/audit dump in the sidebar with a compact calculation/cache/selected-style result summary.
- Added a reusable Ranged Bolt Validation block. It finds the strongest reported owned crossbow setup, holds its weapon/armour fixed, and force-calculates every owned compatible enchanted bolt so bolt mechanics can be checked against the OSRS Wiki without requiring that ammo to be BiS.
- Detailed optimizer instrumentation remains in the optimizer code for future profiling, but is no longer printed permanently in the sidebar.
- No production combat formula or optimizer-selection changes in this release.

## r0.9.0-alpha50.4.19 — Ranged per-slot dominance pruning
- Precomputes each supporting-slot shortlist once per optimisation instead of rebuilding it for every weapon/ammo branch.
- Removes strictly dominated ordinary Ranged candidates before beam expansion when another candidate has equal-or-better Ranged Attack, Ranged Strength and Prayer.
- Dominance is mechanic-class aware: Crystal, Void, Salve and Slayer pieces retain distinct identities and cannot be pruned by ordinary raw-stat gear.
- Exact-stat ties keep the earlier Equipment Intelligence candidate deterministically.
- Adds DEV counters for supporting-slot candidates before/after dominance pruning.
- Keeps alpha50.4.18 bounded top-48 beam, alpha50.4.17 fast scorer, alpha50.4.16 combat-state dedup/cache and alpha50.4.14 result cache.
- No combat formula, weapon/ammo completeness, or beam-width changes.

## r0.9.0-alpha50.4.18 — Bounded Ranged beam top-K
- Replaces full sorting of every deduplicated Ranged beam state with a bounded 48-state priority queue.
- Scores each unique state once, retains only the best 48 by the existing DPS/Prayer ordering, then sorts only those survivors for deterministic beam order.
- Keeps alpha50.4.17 lightweight scoring, alpha50.4.16 dedup/cache, weapon+ammo anchoring, 6/48 beam, and alpha50.4.14 completed-result cache.
- Updates the alpha50.4 source-structure regression to recognise the lightweight target-aware beam scorer.
- No combat formula, ammo compatibility, candidate completeness, or final authoritative DPS changes.

## r0.9.0-alpha50.4.17 — Ranged lightweight beam scorer
- Replaces full RangedCombatResult construction in intermediate beam scoring with an allocation-light exact DPS scorer.
- Final surviving states still use the production RangedCombatCalculator.
- Preserves alpha50.4.16 deduplication/cache, 6/48 beam, weapon/ammo completeness and result cache.
- DEV profiler now separates full DPS calculations from lightweight fast-score calls.

## r0.9.0-alpha50.4.16 — Ranged hot-loop state dedup + score cache
- Deduplicates offensively equivalent partial Ranged beam states before full DPS scoring while preserving mechanic-sensitive identities (weapon/ammo, crystal, Void, Salve and Slayer-head state).
- Keeps the higher-Prayer representative when two partial states are offensively equivalent.
- Reuses exact DPS scores for equivalent combat states across beam trims during the same optimisation run.
- Adds DEV counters for unique states, deduplicated states and score-cache hits.
- Keeps the 6/48 beam, weapon+ammo anchored completeness, alpha50.4.14 unchanged-input result cache, and validated combat formulae unchanged.

# Personal BiS r0.4.0

Major architecture update.

- BiS-Gear-inspired compact sidebar workflow.
- Full monster selector is built from the current RuneLite NPC cache instead of only nearby NPCs.
- Slayer-task selection remains available.
- Reads the logged-in account's real and boosted skill levels using RuneLite Client APIs.
- Gear eligibility continues to block known level/quest requirements.
- Prayer recommendation uses the logged-in Prayer level and marks unlock-dependent prayers as such.
- Personal BiS bank button now toggles a native RuneLite bank-search filter so the normal OSRS bank grid and withdraw controls remain in use.
- Bank filter shows the best owned item currently selected for each equipment slot.

## Important r0.4.0 limitation

Monster cache data provides base NPC stats but not the complete OSRS Wiki defence-bonus/mechanics dataset required for exact target-specific DPS. Style ordering is therefore still heuristic until the dedicated DPS/mechanics data layer is added. The native bank filter is the first Quest-Helper-style integration step; section headers/layouts by combat style are planned for the next bank-layout pass.

## r0.4.1

- Re-skinned the sidebar with RuneScape fonts, dark brown panels, gold/cream text, OSRS-style borders, controls and equipment slots.
- Monster catalogue now scans a larger NPC-id range and loads the full deduplicated list into the selector.
- Duplicate NPC cache entries are collapsed when name, combat level, defence, magic, ranged and hitpoints are identical; genuine stat variants remain separate.
- Personal BiS bank mode now lays out recommended owned items under native-bank section headers for every combat style.
- Style sections are ordered best-to-worst using the current gear scoring engine and the top section is marked as the best style.
- Bank items remain withdrawable; clicks are remapped to the real bank index after the visual layout is rearranged.

Note: style ordering still uses the current heuristic equipment score. True monster-specific DPS is the next calculation layer.


## r0.4.2 UI / bank cleanup

- Compact sidebar sizing for the normal RuneLite panel width.
- Horizontal scrolling disabled; controls and equipment tiles reduced to fit the standard sidebar.
- Removed custom bank divider graphics and hides leftover native bank-content labels while Personal BiS view is active.
- Keeps the active recommended loadout stable while withdrawing items.
- Withdrawn recommended items now switch to the item's real OSRS placeholder variant instead of remaining as a normal-looking item.
- Placeholder/normal variants are canonicalized for filtering and click-index lookup.


## r0.4.3

- Bank section headers are aligned with the left-most gear item.
- Added a subtle divider line above every combat-style section.
- Expanded account-level equipment checks, including black/blessed dragonhide and other common ranged armour.
- Gear below the logged-in account's required combat level is excluded from the recommended setup.
- Slayer-task checkbox is left aligned and has a strong selected state.
- Enabling Slayer mode automatically selects the current Slayer task target when a matching monster is found.
- Slayer mode now actively prioritises owned Slayer helmet / black mask gear; imbued variants are considered for ranged and magic.
- Improved plural Slayer-task matching against singular NPC database names.


## r0.5.0-alpha1 — Option A: bundled Wiki data

This is the first data-layer migration build, not the final r0.5.0 optimiser.

- Adds offline JSON resources for equipment, monsters and spells.
- Adds `WikiDataPack`, `WikiEquipment`, `WikiMonster` and `WikiSpell`.
- Equipment ranking prefers bundled Wiki stats where present and falls back to RuneLite for everything else.
- Monster catalogue overlays bundled Wiki stats and falls back to the RuneLite NPC cache.
- No live Wiki request is made while RuneLite is running.
- Existing bank UI, Slayer mode and sidebar continue to work.

The alpha seed is intentionally small so the loader/fallback architecture can be tested before the full datasets are bundled. Current seed: 5 equipment entries, 2 monsters and 15 spells.


## r0.5.0-alpha3 — complete bundled data pack

Alpha2 replaces the small alpha1 seed resources with the complete supplied datasets:

- Equipment records: 5,436
- Monster records: 2,860
- Spell records: 61

The files are bundled under `src/main/resources/wiki-data/` and are loaded locally.
No Wiki/network request is required at RuneLite runtime.

The existing RuneLite fallback remains in place. The recommendation algorithm is
still the pre-DPS heuristic at this milestone; alpha2's purpose is to prove that
the complete data pack loads and works with the existing bank/Slayer UI before
the real DPS/loadout optimiser is switched on.


## r0.5.0-alpha3 — melee DPS shortlist optimiser

- Stab, Slash and Crush now use target-specific Wiki defence bonuses.
- Owned/equippable weapons are cheaply ranked first; only the top 5 ordinary weapons advance to full optimisation.
- Relevant special-interaction weapons are protected from the top-5 cut and added as extra candidates.
- Each candidate weapon is evaluated with complete legal gear sets using a bounded beam search.
- Two-handed weapons correctly suppress the shield slot.
- Melee recommendations are ranked by calculated DPS, not the old summed heuristic.
- Sidebar shows estimated DPS; item tooltip shows DPS, accuracy and max hit.
- Ranged and Magic intentionally remain on the alpha2 heuristic until their dedicated calculators are added.

Alpha limitation: this first melee calculator handles the standard attack/defence roll, max hit, attack speed and Slayer helm/black mask task multiplier. Weapon-specific formulas (Fang, Scythe, Keris, demonbane etc.) are protected in the candidate shortlist but their unique damage mechanics are not yet applied. Prayer choice is displayed but not yet included in the numeric DPS result.


## r0.5.0-alpha4 — melee validation build

Alpha4 is deliberately a validation/debugging milestone before Ranged/Magic DPS.

For Stab, Slash and Crush it now displays the top weapon candidates after each
weapon has had its own complete loadout optimised. For each candidate the panel
shows:

- DPS
- accuracy
- max hit
- attack speed in ticks
- total style attack bonus
- total melee strength bonus
- player attack roll and monster defence roll
- effective Attack / Strength
- the stance used by the calculation

Alpha4 also fixes an alpha3 omission: melee stance bonuses are now explicit.
The calculator evaluates Accurate (+3 Attack), Aggressive (+3 Strength), and
Controlled (+1 Attack/+1 Strength), and retains the highest calculated DPS for
the candidate.

Known validation limitation: this is not yet a weapon attack-style capability
database, so alpha4 evaluates those three generic stance choices for melee
candidates. Prayer multipliers and unique weapon mechanics are still not in the
numeric DPS result. The point of this build is to expose discrepancies before
those systems are added.


## r0.5.0-alpha5
Readable full-width melee validation panel; offensive melee prayer multipliers are
now included according to Prayer level; Slayer task helm/mask and stance handling
remain in the visible calculation. Unique weapon mechanics remain a later step.


## r0.5.0-alpha6 — monster defence source fix

Root cause fixed: RuneLite-cache and bundled Wiki variants could coexist because
their deduplication keys included combat stats. Manual/Slayer target matching
could therefore return the cache copy first, including a zero Defence value.
When a bundled Wiki record exists, alpha6 now removes cache duplicates with the
same normalized name and combat level before inserting the richer Wiki variant.

The melee validation panel now prints the selected target's combat level,
Defence level, style defence bonus and calculated defence roll above the weapon
ranking. This makes future data-source fallbacks immediately visible.


## r0.5.0-alpha7 — Slayer variants + sidebar cleanup

- Slayer mode now treats the Slayer assignment as a category and populates the target combo with matching monster variants.
- Changing the monster variant recalculates Personal BiS automatically; the chosen variant is preserved while the task remains unchanged.
- Manual search is restored when Slayer mode is turned off.
- Removed the redundant Find best setup button.
- Removed the visible player combat-level summary.
- Gear slots are now icon-only; slot/item/requirement details remain available in hover tooltips.
- Increased the melee validation viewport substantially so more target and weapon diagnostics are visible before scrolling.


## r0.5.0-alpha8 — RuneLite sidebar cleanup

- Main sidebar and cards now use RuneLite dark ColorScheme backgrounds rather
  than the earlier brown-tinted custom panel background.
- Removed the visible "Best owned setup..." summary line from the sidebar.
- Removed the visible selected-monster summary line beneath it; the target and
  defensive data remain available in the validation section where they are
  actually useful.
- Automatic recalculation, icon-only equipment display, Slayer variant selector
  and alpha7 validation behaviour are unchanged.


## r0.5.0-alpha9 — automatic prayer/boost UI

- Removed the visible Prayer / Boost settings card; both remain automatic inputs.
- Added a dedicated prayer slot to the top-right of the equipment panel.
- The slot is driven from the prayer used by the melee DPS result when available,
  with a tooltip naming the prayer.
- No gameplay automation is performed.


## r0.5.0-alpha10 — compact equipment UI

- Reworked equipment/prayer display into a compact 4 x 3 grid (12 cells).
- Uses RuneLite's native prayer sprites for supported recommended prayers, including Piety.
- Prayer tooltip remains available on hover.
- Selected combat style is highlighted more strongly.
- Hidden the normal-sidebar developer/status line.
- Expanded the melee validation area using the vertical space recovered by the compact grid.


## r0.5.0-alpha11 — weapon attack-style legality

- Carries the Wiki equipment `category` into each equipment candidate.
- Melee DPS now evaluates only stances that can actually perform the selected
  Stab / Slash / Crush type for mapped weapon categories.
- Fixes the visible alpha10 error where Dragon scimitar could be evaluated as
  `Controlled` while calculating Slash DPS.
- Weapons known not to support the selected attack type are excluded from that
  melee comparison.
- Unknown/non-standard categories retain the previous permissive fallback for
  now, so unusual weapons are not silently lost before their mechanics are mapped.

## r0.5.0-alpha12 — passive weapon mechanics phase 1
- Arclight/Emberlight: +70% accuracy and damage against demons.
- Silverlight/Darklight: +60% damage against demons.
- Osmumten's fang: second independent accuracy roll on Stab (outside-ToA model).
- Fang's normal 15%-85% hit distribution keeps the same 50% mean for sustained DPS.
- Special attacks remain excluded pending explicit spec-energy/fight-duration modelling.

## r0.5.0-alpha13 — mechanics regression harness
- Adds a `mechanicsSelfTest` Gradle task; it does not launch RuneLite and needs no live bank/account state.
- Checks demonbane activation/non-activation, Silverlight/Darklight modifiers, Fang identification/double-roll math, and ordinary-weapon no-effect regression.
- Run in IntelliJ: Gradle > Tasks > verification > mechanicsSelfTest (or `gradlew mechanicsSelfTest`).

## r0.5.0-alpha14 — passive mechanics phase 2
- Dragon hunter lance: +20% accuracy and damage on draconic targets.
- Keris/Keris partisan family: scarab-target damage and 1/51 triple-damage proc included in expected sustained DPS.
- Keris partisan of breaching: +33% accuracy on Kalphites/Scabarites.
- Keris partisan of Amascut: 15% scarab damage variant.
- Scythe of vitur: target size now comes from the bundled monster dataset; 1x1/2x2/3x3+ targets receive 1/2/3 independent expected hits at 100%/50%/25% max-hit scaling.
- mechanicsSelfTest expanded with regression checks for these effects.

## r0.5.0-alpha15 — loadout/set effects phase 1
- Salve melee variants now affect undead accuracy/damage; enchanted versions use 20%, base/(i) use 1/6.
- Salve correctly takes precedence over Slayer helmet/black mask instead of stacking.
- Full Void melee set now adds +10% melee accuracy and damage.
- Inquisitor armour now boosts Crush accuracy/damage by 0.5% per piece plus +1% for the full set.
- With Inquisitor's mace, each armour piece gives 2.5% and the ordinary +1% full-set bonus is omitted.
- Synergy pieces are preserved by the optimiser even when their standalone stats fall outside the normal top-N slot shortlist.
- mechanicsSelfTest expanded for Salve, Void and Inquisitor regressions.

## r0.5.0-alpha16 — calculated Ranged phase 1
- Ranged is no longer ranked only by heuristic gear score: complete owned loadouts are evaluated by attack roll, target defence, max hit, attack speed and expected DPS.
- Accurate (+3 Ranged), Rapid (-1 attack tick) and Longrange are evaluated automatically.
- Automatic ranged prayer selection is included (Sharp/Hawk/Eagle Eye, Deadeye, Rigour by Prayer level; unlock-state verification remains future work).
- Dragon hunter crossbow: +30% accuracy / +25% damage against draconic targets.
- Bow of Faerdhinen: crystal helm/body/legs scale to +30% accuracy / +15% damage with the full set.
- Twisted bow outside-CoX target-Magic scaling is included. Current bundled monster model exposes Magic level, not the alternate NPC Magic attack value, so this is not yet exact for targets where Magic attack is higher.
- Full Void ranged: +10% accuracy / damage.
- Ranged validation now displays DPS, accuracy, max hit, speed, bonuses, rolls, effective Ranged, stance and prayer.
- Ammunition compatibility/selection and ammo-type defence (light/standard/heavy), enchanted bolt procs, Slayer/Salve exact ranged ordering, and unlock-state verification remain phase-2 work.

## r0.5.0-alpha16.1 — Ranged validation UI hotfix
- Renamed the sidebar section from "Melee validation — top weapons" to "Loadout validation — top weapons".
- Ranged validation now resolves its result directly from the calculated ranged loadout before the Swing UI update.
- Ranged selection displays DPS, accuracy, max hit, attack speed, Ranged attack/strength, rolls, effective Ranged, stance and prayer.
- Melee validation now identifies itself as melee loadout validation inside the shared panel.

## r0.5.0-alpha16.2 — Ranged optimiser integration fix
- Ranged optimisation now starts from an owned, usable weapon before expanding the remaining equipment slots.
- Every non-weapon equipment slot is optional during beam search, preventing an empty/blocked slot from collapsing the entire ranged search.
- Two-handed ranged weapons can no longer retain a shield chosen earlier in slot iteration.
- Invalid/non-finite candidate DPS values are safely ranked out rather than destabilising the beam.
- The live UI consistently uses the resolved ranged result for title, tooltips and Loadout validation.
- mechanicsSelfTest now includes regression guards for the weapon-first and optional-slot optimiser invariants.

## r0.5.0-alpha17 — Ranged ammunition compatibility phase 1
- Added ranged weapon ammunition classification: arrows, bolts, darts/internal/no-ammo.
- Bowfa/crystal bow are treated as self-contained; blowpipe is classified separately so equipped ammo-slot stats cannot leak into its calculation.
- Bow and crossbow loadout searches now reject incompatible ammo-slot items.
- Ammo-slot Ranged attack/strength is ignored when incompatible with the selected weapon.
- Ranged target defence now selects light defence for arrow/dart-class attacks and standard defence for bolt/default attacks.
- Loadout validation displays the selected weapon's ammo class.
- Enchanted bolt proc damage, blowpipe loaded-dart strength (not visible in bank equipment slot), exact individual bow/bolt tier restrictions, heavy projectile families, and ammo consumption remain later validation work.

## r0.5.0-alpha17.1 — Prayer unlock awareness
- Reads permanent account unlock varbits for Rigour (5451), Augury (5452), Deadeye (16097), and Mystic Vigour (16098).
- Ranged calculator only considers Rigour/Deadeye when the account has actually unlocked them.
- Prayer recommendation only displays Rigour/Augury/Deadeye/Mystic Vigour when both level and unlock state permit them.
- A Prayer 75 account without Rigour/Deadeye now falls back to Eagle Eye.

## r0.5.0-alpha18 — Ranged comparison & ammo validation
- Ranged validation now shows the top five owned weapon setups, mirroring melee validation.
- Each comparison shows weapon name, selected compatible ammo/internal ammo, DPS, accuracy, max hit, speed, bonuses, rolls, stance and prayer.
- Added common arrow and bolt tier compatibility guards so lower-tier bows/crossbows cannot borrow impossible high-tier ammunition.
- Preserves Bowfa/crystal bow internal-ammo handling and blowpipe loaded-dart separation.
- Enchanted bolt proc EV and exact blowpipe loaded-dart selection remain future work.

## r0.5.0-alpha18.1 — Ammo selection & compact validation
- For each ranged weapon, the optimiser now evaluates every usable owned ammo candidate rather than limiting ammo to the generic top-N equipment shortlist.
- Weapon/ammo compatibility and tier guards from alpha18 are applied before DPS comparison.
- The top-five Ranged validation display is compacted to make comparisons readable in the RuneLite sidebar.
- Enchanted bolt special-effect expected damage is intentionally not included yet; this build establishes reliable owned-ammo selection first.

## r0.5.0-alpha18.2 — Gear icon reliability hotfix
- Gear icons now use RuneLite ItemManager's asynchronous image callback instead of assuming the image is immediately available.
- Each equipment slot tracks a request generation so a delayed image from an older target/style cannot overwrite the current recommendation.
- Empty slots invalidate any pending image request.
- Fixed the Ranged validation header so the newline renders normally instead of displaying a literal `\\n`.
- No combat/DPS mechanics changed from alpha18.1.

## r0.5.0-alpha18.3 — Compile fix
- Corrected RuneLite `AsyncBufferedImage.addTo` usage: the API accepts `JLabel`/`JButton`, not a callback lambda.
- Retains asynchronous ItemManager image loading.
- No combat/DPS changes.

## r0.5.0-alpha19 — Enchanted bolt expected-value phase 1
- Adds expected target-DPS effects for Ruby, Diamond, Onyx and Dragonstone enchanted bolts.
- Ruby uses target full HP as a ranking approximation for current HP and respects the 100-damage cap.
- Diamond/Onyx damage uplifts are included as expected value.
- Dragonstone extra damage is conservatively disabled for dragon/fiery targets.
- Other enchanted bolt families remain stat-only until their mechanics can be represented accurately.
- Kandarin diary proc-rate modifiers and exact per-proc floor ordering are not yet modelled.

## r0.5.0-alpha19.1 — Enchanted bolt validation refinement
- Validation now labels a bolt EV only when that bolt's target-damage effect is actually modelled.
- Sapphire/Jade/Topaz/Emerald no longer misleadingly display `bolt EV` merely because they are enchanted.
- Added Opal Lucky Lightning expected damage: 5% base PvM activation and floor(visible Ranged × 10%) extra damage.
- Existing Ruby, Diamond, Dragonstone and Onyx phase-1 EV models remain.
- Hard Kandarin Diary activation bonus is still not included.
- Blowpipe loaded darts remain the next major Ranged data problem because darts stored inside a charged blowpipe are not ordinary bank ammo-slot items.

## r0.5.0-alpha20 — Ranged Salve / Slayer interaction
- Adds ranged Salve amulet handling against undead targets.
- Salve amulet (i): 7/6 ranged accuracy and damage.
- Salve amulet (ei): 20% ranged accuracy and damage.
- Prevents the imbued Slayer helm ranged multiplier from stacking with an active ranged Salve effect.
- Keeps enchanted-bolt UI conservative: unmodelled enchantments no longer receive an EV label.
- No blowpipe dependency: this build can be tested with the user's existing ranged gear.

## r0.5.0-alpha21 — Ranged defence-class pass
- Uses the bundled monster light / standard / heavy ranged-defence fields explicitly.
- Light projectiles: bows/arrows, darts, knives and blowpipe-family attacks.
- Standard projectiles: crossbows/bolts and default ranged weapons.
- Heavy projectiles: ballistae, javelins and thrown axes.
- Top-five Ranged validation now prints the defence class used for each weapon, making bad classifications visible during live testing.
- This is intentionally data-driven from the bundled monster defence fields; no monster-specific defence values are invented.

## r0.5.0-alpha22 — Calculated Magic foundation
- Replaces the Magic gear-score-only path with a real target DPS calculation for standard combat spells.
- Uses account Magic level, owned/equippable magic gear, target Magic level/Defence/magic defence, magic attack bonus and magic-damage bonus.
- Checks required runes against the bank and recognises common elemental staff/battlestaff combination-rune substitutions.
- Prayer selection is account-aware: Augury is used only when its unlock varbit is present; otherwise Mystic Might/Lore/Will are selected by Prayer level.
- Imbued Slayer helm magic bonus is included on-task.
- Magic now participates in cross-style ranking using calculated DPS.
- Validation shows selected spell, DPS, accuracy, max hit, magic attack, prayer and attack/defence rolls.
- Phase-1 scope is the standard Strike/Bolt/Blast/Wave families. Powered staves, Ancient/Arceuus spells, elemental weaknesses, special spell requirements and exact spell-specific passives are intentionally deferred.

## r0.5.0-alpha23 — Joint Magic gear + spell optimiser
- Magic now searches weapon, gear and castable spell combinations together instead of choosing one heuristic gear set first.
- Weapon-first search evaluates up to 12 owned usable Magic weapons and a beam of legal gear combinations for each.
- Two-handed Magic weapons correctly suppress the shield slot during search.
- Standard combat spell availability remains gated by account Magic level and banked runes, including common elemental staff substitutions.
- Top-five Magic validation now shows weapon + spell combinations, DPS, accuracy, max hit, Magic attack, total Magic-damage bonus and prayer.
- Magic damage is surfaced explicitly so live testing can catch percentage/unit mistakes rather than hiding them behind max-hit rounding.
- Powered staves, elemental weaknesses, Ancient/Arceuus spellbooks and spell-specific weapon passives remain deferred.

## r0.5.0-alpha24 — Powered staves phase 1
- Powered staves now compete directly with normal staff + spell combinations in the calculated Magic optimiser.
- Added Trident of the seas, Trident of the swamp, Sanguinesti staff and Warped sceptre built-in spell scaling.
- Powered max hit uses the account's visible/boosted Magic level, then applies the selected loadout's Magic-damage bonus.
- Powered attacks use their 4-tick sustained attack speed.
- Powered weapons no longer require standard-spell rune checks because their casts come from weapon charges.
- Added real powered-staff formula assertions to mechanicsSelfTest.
- Charge quantity/state is not yet observable from the bank snapshot, so alpha24 treats an owned powered staff as usable; charge-state integration remains future work.
- Tumeken's shadow is intentionally deferred because its equipment-bonus multiplier requires separate handling.

## r0.5.0-alpha25 — Elemental weaknesses + Magic formula corrections
- Bundled monster elemental weakness element/severity is now carried into MonsterDefinition.
- Standard elemental spells receive +1% accuracy and +1% damage per point of matching weakness.
- Elemental weakness damage is added from base max damage after the ordinary equipment-damage floor.
- Strike/Bolt/Blast/Wave spells now scale their base max hit to the strongest unlocked elemental spell in the same tier.
- Standard autocast spell speed corrected to 5 ticks; powered staves remain 4 ticks.
- NPC Magic defence roll corrected to use monster Magic level + 9 with Magic defence bonus; the player-style 70% Magic / 30% Defence mixture is not used for NPCs.
- Validation labels matching weaknesses, e.g. Water Blast [water 50%].
- Powered staff attacks do not receive elemental weakness because they are not standard elemental spells.

## r0.5.0-alpha25.1
- Java 11 compatibility hotfix: the delegating MonsterDefinition constructor now calls `this(...)` as its first statement.
- No Magic mechanics changes from alpha25.

## r0.5.0-alpha26 — Magic compatibility + explainability
- Standard spell candidates now require an autocast-capable Magic weapon; powered staves remain on their own built-in-spell path.
- Top-five Magic validation explicitly explains matching elemental weakness bonuses.
- Powered-staff results explicitly identify the built-in 4-tick spell path.
- Elemental weakness max-hit ordering now applies the weakness and equipment Magic-damage percentages in the same combined multiplier before flooring.
- The selected spell name is kept clean; mechanic explanations are shown separately.
- This phase intentionally does not pretend to know charge state, special spellbook unlocks, or weapon-specific spell restrictions that are not yet modelled.

## r0.5.0-alpha27 — Magic weapon effects phase 1
- Added a dedicated MagicWeaponEffects layer so staff passives no longer have to be hidden inside the generic spell formula.
- Smoke battlestaff, mystic smoke staff and Twinflame staff now apply their standard-spell accuracy/damage passive in the optimiser and explain it in validation.
- Added Wind/Water/Earth/Fire Surge to the standard elemental spell catalogue for higher-level accounts.
- Accursed/Thammaron sceptres are explicitly excluded from ordinary standard-autocast treatment until Wilderness/charged-state context is available.
- Existing elemental weakness and powered-staff paths remain separate.
- Tumeken's shadow, tomes, Ancient/Arceuus spells and charge-state tracking remain deliberately deferred rather than approximated.

## r0.5.0-alpha28 — Dragon hunter wand + Twinflame mechanics
- Dragon hunter wand now receives its current dragonbane passive against bundled draconic targets: +75% accuracy and +40% damage.
- Twinflame staff now uses its 6-tick cast speed, +10% standard-spell accuracy/damage, and a 40% expected secondary hit for Bolt/Blast/Wave spells.
- Smoke staff handling remains +10% accuracy/damage but no longer incorrectly aliases Twinflame's special timing/echo behaviour.
- Magic validation explains Dragonbane/Twinflame effects and displays the weapon-specific attack speed.
- Added regression assertions for Dragon hunter wand and Twinflame behaviour.
- Twinflame automatic elemental spell substitution is not separately simulated yet; the optimiser already evaluates every castable elemental spell, so it can select the matching weakness spell directly.

## r0.5.0-alpha29 — Tumeken's shadow phase 1
- Tumeken's shadow is now a powered Magic weapon candidate with its built-in 5-tick attack and Magic-level base max-hit formula.
- Outside Tombs of Amascut, the optimiser triples the loadout's equipment Magic accuracy bonus and Magic-damage bonus for Shadow.
- Shadow-amplified equipment Magic damage is capped at +100%.
- Validation identifies the Shadow multiplier and 5-tick attack.
- Added self-test assertions for Shadow level requirement, speed and base max hits.
- Tombs of Amascut's 4x multiplier is deliberately not inferred because Personal BiS does not yet track encounter/location context; alpha29 uses the outside-ToA 3x rule.

## r0.5.0-alpha30 — Standard special spells + compatibility
- Added a dedicated spell/weapon compatibility layer based on the OSRS Wiki DPS calculator's current restrictions.
- Added Iban Blast, Magic Dart, Saradomin Strike, Claws of Guthix and Flames of Zamorak to the optimiser.
- Iban Blast requires Iban's staff / Iban's staff (u).
- Magic Dart requires a compatible Slayer/dead/light/balance staff and uses its Magic-level-scaled max hit.
- God spells are restricted to their supported weapons. Charge is deliberately not assumed, so their base max hit remains 20.
- Harmonised nightmare staff now casts standard elemental spells at 4 ticks.
- Validation explains special-spell restrictions and Harmonised timing.
- Added regression assertions for spell data, weapon compatibility, Magic Dart scaling and Harmonised speed.

## r0.5.0-alpha30.1 — elemental tier regression fix
- Fixed `scaledBaseMax()` so non-elemental special spells do not participate in elemental Strike/Bolt/Blast/Wave/Surge scaling.
- Restores the elemental tier-scaling regression test broken by alpha30's special-spell additions.

## r0.5.0-alpha30.2 — spell-name collision fix
- Corrected the remaining elemental tier regression: Iban Blast was being classified as a Blast solely because its name contains "Blast"; god spells such as Saradomin Strike had the same problem.
- `tier()` now only assigns Strike/Bolt/Blast/Wave/Surge tiers to elemental spells.
- Added a regression assertion ensuring Iban Blast and Saradomin Strike remain outside elemental tier scaling.

## r0.5.0-alpha30.3 — Black dragon live-regression fix
- Non-elemental spells are now explicitly excluded from elemental weakness bonuses.
- Recommendation refresh canonicalises the selected monster against the rebuilt bundled-Wiki catalogue, preventing a stale RuneLite-cache target with incomplete Magic stats from producing inflated accuracy.
- Added Black dragon regressions proving Iban Blast receives no water weakness while Water Blast does.

## r0.5.0-alpha31 — Ancient Magicks phase 1
- Added all 16 Ancient Magicks combat spells (Rush/Burst/Blitz/Barrage) with Magic levels, base max hits and rune costs.
- Added Ancient spellbook metadata and an aggregate combat-spell catalogue.
- Added Ancient autocast compatibility for Ancient staff/sceptres, Master/Kodai wand, Nightmare staff family and Shadow.
- Ancient spells compete in the same owned-gear optimiser as standard spells and powered staves.
- Ancient spells are explicitly non-elemental, so elemental weaknesses cannot leak into them.
- DPS is single-target only; poison/stat drain/healing/freeze and multi-target value are displayed as unvalued secondary effects.
- Desert Treasure I spellbook unlock is not yet inferred, so alpha31 phase 1 relies on owning an Ancient-capable weapon as its conservative availability gate.

## r0.5.0-alpha31.1 — compile fix
- Added the missing `MagicSpell.spellbook` field referenced by the new Ancient Magicks constructors and compatibility layer.
- No Ancient mechanics changed from alpha31; this is a compile-only correction.

## r0.5.0-alpha31.2 — constructor overload fix
- Removed the ambiguous public `(String, int, int, String, Object...)` spell constructor.
- Standard spells can no longer have their first rune name misinterpreted as a spellbook argument.
- Ancient spells now use an explicit internal factory.
- Added regression checks for both standard and Ancient spellbook/rune-map construction.

## r0.5.0-alpha32 — Ancient Magicks phase 2
- Ancient spell recommendations now require RuneLite to report Desert Treasure I as FINISHED.
- Added Ancient secondary-effect metadata without inflating raw single-target DPS.
- Burst/Barrage: marked as multi-target, up to 9 targets in the 3x3 area.
- Blood spells: display 25% damage healing.
- Ice: Rush 4.8s, Burst 9.6s, Blitz 14.4s, Barrage 19.2s freezes.
- Shadow: Rush/Burst -10% Attack; Blitz/Barrage -15% Attack.
- Smoke spells: poison starts at 4.
- Top-five remains ranked by single-target DPS so utility/AoE cannot falsely beat a stronger bossing spell.

## r0.5.0-alpha33 — Magic loadout effects
- Slayer helmet (i)/black mask (i): +15% Magic accuracy and damage on-task; replaces the older effective-level approximation.
- Salve amulet(i): +15% Magic accuracy/damage vs undead; Salve amulet(ei): +20%.
- Salve takes precedence over Slayer because the bonuses do not stack.
- Full Void Mage: +45% Magic accuracy.
- Full Elite Void Mage: +45% Magic accuracy and +5% Magic damage.
- Void effects stack with Salve.
- Effects apply to both normal spells and powered staves and are surfaced in validation notes.
- Added regression checks for Slayer, Salve precedence, Void and Elite Void.

## r0.5.0-alpha34 — modern Magic prayer/damage ordering
- Added Magic prayer damage: Mystic Lore +1%, Mystic Might +2%, Mystic Vigour +3%, Augury +4%.
- Mystic Vigour remains gated by its unlock varbit; Augury remains gated by its prayer-scroll unlock.
- Prayer Magic damage is additive with ordinary equipment Magic damage and elemental weakness before later target/loadout multipliers.
- Powered staves now receive Magic-prayer damage too.
- Added Virtus Ancient conditional damage: +3% per Virtus piece when casting Ancient Magicks.
- Shadow still multiplies equipment Magic bonuses only; prayer damage remains outside the Shadow multiplier.
- Added regression assertions for modern Mystic Might, Mystic Vigour and Augury values.

## r0.5.0-alpha34.1 — exact elemental weakness floors + prayer boundaries
- Corrected standard-spell max-hit ordering: floor(base * (1 + equipment + prayer)) + floor(base * elemental weakness), before later modifiers.
- Elemental weakness is no longer folded into the same floor as ordinary Magic damage.
- Added boundary regressions proving Mystic Might +2%, Mystic Vigour +3%, Augury +4%, powered-staff prayer damage, and Black dragon Water Blast rounding.
- Black dragon Water Blast at base 16, +2% Mystic Might and 50% water weakness remains max 24: floor(16*1.02)=16 plus floor(16*0.50)=8.

## r0.5.0-alpha35 — Ancient/Kodai equipment correctness
- Corrected Virtus Ancient Magicks damage from +3% to the current +5% per piece (+15% full set).
- Kodai wand now satisfies water-rune requirements for spell recommendations.
- Updated Ancient autocast list: Ancient staff/sceptres, Master/Kodai, Nightmare variants, Thammaron's sceptre (a), Accursed sceptre (a), and Blue moon spear.
- Tumeken's shadow is no longer treated as an Ancient-spell autocaster; its powered-staff path remains intact.
- Ancient sceptres now surface their +10% Ancient secondary-effect boost.
- Blood sceptre notes overheal capability; Ice notes its conditional +10% accuracy; Shadow notes extra Strength/Defence drain; Smoke notes healing reduction.
- Conditional Ice-sceptre accuracy is deliberately not included in DPS ranking because Personal BiS does not yet know whether the live target is freezable and currently unfrozen.

## r0.5.0-alpha36 — elemental tomes
- Charged Tome of fire: unlimited fire runes and +10% post-roll damage to standard fire elemental spells vs NPCs.
- Charged Tome of water: unlimited water runes and +10% post-roll accuracy/damage to standard water elemental spells vs NPCs.
- Charged Tome of earth: unlimited earth runes and +10% post-roll damage to standard earth elemental spells.
- Tome bonuses are multiplicative after visible Magic damage/weakness calculations, rather than added to the visible Magic-damage stat.
- Non-elemental spells that merely consume the matching rune (for example Iban Blast) do not receive tome combat damage.
- Added tome-specific regression tests.
- Current limitation: bank data does not expose whether a tome is actually charged, so an owned charged-form tome is assumed usable when selected. Charge-state tracking remains future work.

## r0.5.0-alpha36.1
- Compile fix: renamed the Tome regression-test Water Blast local to avoid collision with the existing powered-staff test variable.
- No gameplay/calculation changes from alpha36.

## r0.5.0-alpha36.2
- Corrected the Tome self-test source block itself: all four local spell variables now use unique tome-prefixed names.
- Verified the exact javac-reported declaration no longer exists in MechanicsSelfTest.java.
- No mechanics changes from alpha36.

## r0.5.0-alpha37 — Eye of ayak / powered-staff closeout
- Added Eye of ayak as a level-83 powered staff: 3-tick attacks, base max 21 at 83 Magic scaling to 27 at 99.
- Eye of ayak participates in normal powered-staff Magic prayer, gear, Slayer/Salve/Void calculations.
- Added Soul Rend metadata/regressions: 50% special energy, doubled accuracy, +30% max hit, 5-tick special, successful damage drains equal Magic Defence.
- Soul Rend is deliberately informational and excluded from sustained-DPS ranking because Personal BiS does not yet model special-attack rotations or persistent target defence drains.
- Powered-staff validation notes now report each staff's actual attack speed rather than assuming every non-Shadow powered staff is 4 ticks.

## r0.5.0-alpha38 — 2026 elemental amulets
- Added explicit effects for Amulet of Air, Water, Earth, Fire and the combined Elemental amulet.
- The bundled equipment snapshot already contains all five items with their +10 Magic accuracy, so normal gear scoring/accuracy automatically sees that stat.
- Matching standard elemental spells gain +2 base max hit; the combined Elemental amulet applies it to all four elements.
- The flat +2 is applied to elemental base max before percentage Magic damage and elemental weakness, allowing the bonus to scale through those systems.
- Non-matching elements and non-elemental spells (such as Iban Blast) receive no +2 passive.
- Added regression tests for all five amulets, mismatch/non-elemental exclusions, and prayer/weakness ordering.

## r0.5.0-alpha39 — simple bank recommendations
- Adds a compact `Recommended` section at the bottom of the Personal BiS bank layout; the sidebar remains unchanged.
- Magic recommends runes for 100 casts of the selected best spell. Runes supplied by its staff or Tome are omitted.
- Adds one style-appropriate combat potion, two Prayer potions and 12 food, selecting sensible items actually present in the scanned bank.
- No teleports, Slayer utility, rune-pouch optimisation, trip calculations or other deep inventory planning.

## r0.5.0-alpha40 — Recommended follows the real best style
- Fixed an alpha39 integration bug visible in the live bank screenshot: the bank correctly labelled Melee (Stab) as `Best style`, but `Recommended` could still be populated from the sidebar's selected style (for example Water Blast runes).
- `Recommended` now follows the first/highest-DPS style in the same ordered style ranking used by the bank UI.
- Therefore a Melee best style recommends a super combat potion + Prayer potions + food, Ranged recommends a ranging potion + Prayer potions + food, and Magic adds the best spell's required runes.
- Hardened Magic supply detection so powered staves are detected from the equipped weapon rather than from the spell label.
- Added regression coverage that Melee recommendations cannot leak Magic runes.

## r0.5.0-alpha41 — combat formula accuracy audit, phase 1
- Audited core effective-level ordering against current OSRS Wiki formula documentation.
- Fixed Void melee placement: the 1.10 bonus now multiplies and floors effective Attack/Strength before attack roll and max-hit calculation. Previously Personal BiS multiplied the final attack roll and average damage, which could be wrong at floor boundaries.
- Added `CombatEffectiveLevels` to centralise exact prayer/style/Void ordering and provide regression boundaries for melee, ranged and Magic.
- Added tests using the account's familiar 82 Attack / 90 Strength / 66 Ranged / 73 Magic levels.
- No new UI features; alpha41 is deliberately a recommendation-accuracy release.

## r0.5.0-alpha41.1 — self-test correction
- Corrected the alpha41 Void Magic regression expectation. At 73 Magic with a 1.15 prayer multiplier: floor(73×1.15)=83, floor(83×1.45)=120, then floor(83×1.45)=120 and +9 gives 129. The alpha41 test incorrectly expected 128; the implementation itself was not the cause of this failure.

## r0.5.0-alpha42 — Ranged accuracy/mechanics audit
- Corrected Void Ranged placement: accuracy and damage now modify effective Ranged level with integer floors instead of multiplying final roll/damage.
- Elite Void Ranged now uses its 12.5% damage effective-level modifier while retaining the normal 10% accuracy modifier.
- Corrected imbued Slayer helmet/black mask ordering: +15% Ranged accuracy/damage is now applied after the base attack roll/max hit and does not stack with Salve.
- Crystal armour, Salve, dragon-hunter and Twisted Bow modifiers now pass through explicit integer-floor stages rather than one combined floating-point multiplier.
- Extended bundled monster parsing to retain NPC Magic attack bonus.
- Twisted Bow now scales from max(NPC Magic level, NPC Magic attack bonus), capped at 250 normally and 350 for Xerician targets, matching the current Wiki DPS calculator implementation.
- Added regression boundaries for regular/Elite Void and Twisted Bow target-Magic selection/caps.
- UI unchanged in this release.

## r0.5.0-alpha43 — progressive side panel
- Gear and Loadout validation are hidden on startup.
- Target selection is now explicit; populating/searching the target list no longer silently counts the first NPC as selected.
- Combat-style controls appear only after a target is selected and the bank has been checked.
- Selecting/changing a target clears the previous combat-style choice.
- Gear icons and Loadout validation appear only after target + bank + explicit combat-style selection.
- Recommendation calculation is gated by the same readiness state.
- Added a target-data sanity guard so transient/incomplete NPC definitions are not presented as finished recommendations.
- Added readiness regression tests.

## r0.5.0-alpha43.1 — side-panel interaction fix
- Attack-style buttons appear immediately after an explicit target selection.
- Clicking a style triggers a fresh bank scan, then populates gear and Loadout validation.
- The active Personal BiS bank layout refreshes from the same style selection.
- Changing target clears the previous style/results until another style is selected.

## r0.5.0-alpha44 — selected-style bank view
- Personal BiS bank mode now shows only the combat style selected in the sidebar.
- Other combat-style sections are omitted from the bank view.
- Recommended remains at the bottom and is generated for the selected style, not the overall best style.
- Switching Stab/Slash/Crush/Ranged/Magic rebuilds the bank view for that selection.
- Sidebar behaviour and combat calculations are unchanged from alpha43.1.

## r0.5.0-alpha44.1 — sidebar redesign + genuine best style
- Redesigned sidebar hierarchy to match the approved compact mockup.
- Attack style section is clearly labelled and uses uniform bordered controls.
- Loadout is a true 4x3 grid: 11 equipment slots plus recommended prayer.
- Gear tiles use the same dark individual-box treatment as attack-style controls; no enclosing gear-grid box.
- Sidebar distinguishes selected style from the calculated best style; best style gets a green highlight/tooltip and explicit label.
- Bank `Best style` appears only when the currently displayed style is genuinely the calculated best style.
- Bank still shows only the selected combat style plus Recommended.

## r0.5.0-alpha45 — approved sidebar state redesign
- Added a deliberate logged-out state: active target/style/loadout controls stay hidden and the panel asks the player to log in.
- Logged-in/no-target state shows target controls and a compact instruction card.
- Target-selected state exposes five attack styles in one horizontal row.
- Attack styles now have compact graphical icons above their labels, with selected/best border highlighting retained.
- Target + style state reveals the existing 4x3 gear grid and Loadout validation.
- Loadout heading includes the selected style.
- Monster catalogue count is displayed above the target card after login.
- Existing selected-style bank filtering, genuine Best style logic, gear icons and mechanics remain intact.

## r0.5.0-alpha45.1 — real-width sidebar polish
- Removed the outer Personal BiS panel border/padding.
- Section/header text is left aligned.
- Logged-out mode hides the Attack style heading as well as its controls.
- Attack styles remain in one evenly distributed five-column row.
- Stab, Slash, Crush, Ranged and Magic use the user-provided OSRS-style icon artwork bundled as plugin resources.
- Attack-style tiles are borderless with a darker grey background; selected and genuine-best states use background/text emphasis instead of outlines.
- The 4x3 loadout grid expands evenly across the available sidebar width.
- All 12 loadout tiles are borderless with the same darker grey tile treatment.
- Best-style copy is split across two lines so it fits the real RuneLite sidebar width.
- Mechanics, 4x3 slot semantics and selected-style bank filtering are unchanged.

## r0.5.0-alpha45.2 — real-width alignment pass
- Removed the remaining outer/viewport and card borders.
- Target controls and Slayer toggle now expand from the left.
- Attack styles use the full width with even spacing.
- Supplied style icons render larger at 34x34 with nearest-neighbour scaling.
- 4x3 gear grid uses the full width, is left aligned, and has larger evenly distributed tiles.
- Borderless darker-grey attack-style and gear tiles are retained.
- Header text remains left aligned.
- Mechanics and bank behaviour are unchanged.

## r0.5.0-alpha45.2.1 — compile hotfix
- Added missing `java.awt.Graphics2D` and `java.awt.RenderingHints` imports required by the alpha45.2 attack-style icon scaler.
- No UI behaviour, mechanics, or bank logic changes from alpha45.2.

## r0.5.0-alpha45.3 — final sidebar visual cleanup
- Replaced temporary attack-style artwork with the requested OSRS icons from the supplied references:
  Steel dagger (Stab), Steel scimitar (Slash), Steel warhammer (Crush), Ranged skill icon, Magic skill icon.
- Icons are bundled as native 34x34 transparent resources so Swing no longer resizes them at runtime.
- Strengthened the top-level/sidebar scroll-container zero-border treatment and explicitly paints all enclosing surfaces RuneLite dark grey.
- Fixed the melee validation header where a literal `\\n` caused `VALIDATION\\nTARGET` to run together.
- Added cleaner spacing after Melee/Ranged/Magic validation headings.
- Gear grid, attack-style positioning, mechanics, best-style logic and bank behaviour are unchanged.

## r0.5.0-alpha45.3.1 — startup hotfix
- Removed the unsupported `JViewport.setBorder(...)` call introduced in alpha45.3.
- Retains the supported `JScrollPane.setBorder(...)` and `setViewportBorder(...)` zero-border treatment.
- No changes to the new OSRS attack-style icons, layout, mechanics, or bank behaviour.

## r0.5.0-alpha45.4 — native panel + icon correction
- Removed the nested full-height JScrollPane so Personal BiS uses RuneLite PluginPanel's native panel surface; this targets the persistent white outer frame.
- Removed the fixed 205px content width so the content follows the actual RuneLite sidebar width.
- Slayer checkbox now lives in an explicit left-aligned FlowLayout row.
- Re-cropped the supplied Steel dagger, Steel scimitar, Steel warhammer, Magic skill and Ranged skill assets so the subjects fill their 34x34 icon canvases rather than remaining tiny.
- Gear grid and combat/bank mechanics are unchanged.

## r0.5.0-alpha45.4.1 — compile hotfix
- Added the missing `java.awt.FlowLayout` import required by the alpha45.4 left-aligned Slayer row.
- No UI, mechanics, bank, icon, or layout changes from alpha45.4.

## r0.5.0-alpha45.5 — Slayer workflow + final edge cleanup
- Ticking Use slayer task now promotes the resolved Slayer monster variant to the active target immediately; no manual dropdown re-selection is required.
- Slayer checkbox row has zero left content padding and no decorative checkbox border.
- Removes both the PluginPanel border and its supported viewport border (without calling unsupported JViewport.setBorder).
- Cleaned interior black contamination from the steel portions of the three supplied melee style icons while retaining their pixel-art outlines.
- Attack-style spacing, 4x3 gear grid, combat mechanics and bank behaviour are unchanged.

## r0.5.0-alpha45.5.1 — compile hotfix
- Removed the invalid `setViewportBorder(null)` call from `PersonalBisPanel`.
- `PluginPanel` does not expose JScrollPane's `setViewportBorder` method.
- Retains the Slayer auto-target fix, left-aligned Slayer row, cleaned melee icons, and the valid zero panel border from alpha45.5.

## r0.5.0-alpha45.6 — native PluginPanel shell
- Stops overriding RuneLite PluginPanel with a custom outer BorderLayout.
- Adds the content surface directly using `PluginPanel.PANEL_WIDTH`.
- Uses only a zero EmptyBorder; no JScrollPane/JViewport border calls.
- Slayer checkbox is anchored WEST in a fixed-width BorderLayout row.
- Slayer auto-target remains intact.
- Attack-style icons/spacing, 4x3 gear grid, mechanics and bank behaviour are unchanged.

## r0.5.0-alpha45.6.1
- Uses `super(false)` so RuneLite does not create PluginPanel's default wrapped JScrollPane.
- Adds an explicit borderless JScrollPane (`setBorder(null)` and `setViewportBorder(null)`) around the content.
- Slayer row now expands to full available width and anchors its checkbox WEST.
- Selecting an attack style no longer scans the bank.
- `refreshAll()` no longer silently marks the bank as checked on login.
- Best-style/loadout results remain unavailable until `refreshBankItems()` has actually loaded bank contents.
- Clears stale best-style state when the target changes.

## r0.5.0-alpha45.7
- Removes the Slayer wrapper row and adds the checkbox directly to the Target card with LEFT alignment.
- Bank `ItemContainerChanged` now performs only a lightweight item-ID/quantity snapshot.
- Removed per-item ItemComposition/name lookups and sorting from the bank-open scan path.
- Expensive BiS recommendation work and bank-filter layout are deferred until after the bank-open event/UI cycle.
- The borderless `super(false)` PluginPanel shell from alpha45.6.1 is retained.

## r0.5.0-alpha45.8
- Replaces the player-facing top-5 validation report with compact `Loadout stats`.
- Shows only the winning selected-style loadout: DPS, Max Hit, Accuracy, Style, and total Prayer Bonus.
- Top candidate reports remain internal to the optimizers and are no longer rendered in the sidebar.
- Slayer checkbox is placed at the start of a full-width horizontal BoxLayout row with trailing glue.
- Retains alpha45.7's fast bank-open snapshot/deferred recommendation path and borderless panel.

## r0.5.0-alpha45.9
- Enlarges Loadout stats using RuneLite's normal RuneScape font, matching the target-selection text more closely.
- Removes the Loadout stats JScrollPane and its visible border/box; stats render directly on the sidebar surface.
- Unifies the main sidebar/card backgrounds onto the lighter RuneLite grey used by the rest of Personal BiS.
- Retains alpha45.7/45.8 fast bank opening, compact final-loadout stats, borderless outer panel and existing combat mechanics.

## r0.5.0-alpha45.10
- Personal BiS bank mode now behaves as a dedicated full-bank view: selected-style gear and Recommended at the top, followed by `Other items`.
- Stops using the bank-search callback to hide non-recommended items.
- Reuses native bank item widgets for recommendations and Other items instead of manufacturing replacement withdraw widgets.
- While the Personal BiS bank view is active, bank quantity changes no longer trigger a full bank scan/BiS recalculation, removing the withdrawal hitch.
- Slayer checkbox moved into an explicit fixed-width BorderLayout row anchored WEST and made transparent.

## r0.5.0-alpha45.11
- Splits Slayer target loading from Slayer gear calculations.
- Target section now has `Load target from Slayer task`; it populates the target dropdown with eligible task NPC variants only.
- Attack style section now has a separate `On Slayer Task` checkbox controlling Slayer-specific equipment/effect calculations.
- Target heading and Slayer target control are explicitly left aligned.
- Recommended bank supplies now render even when the recommended food/potion/rune is not currently owned, using Quest-Helper-style placeholder widgets.
- Retains alpha45.10's responsive full-bank view and `Other items` section.

## r0.5.0-alpha45.12
- Fixed the root cause of missing Recommended supplies and missed Slayer headgear: the alpha45.7 fast bank snapshot had stripped every bank item's name.
- Bank snapshots now preserve cached RuneLite item names without restoring the old sorting/composition-heavy scan.
- `SimpleSupplyRecommender` can again select only owned food/potions and owned runes required by the chosen Magic spell.
- Slayer helmet/black mask detection now receives real item names again, so `On Slayer Task` can correctly shortlist and apply their task bonuses.
- Removed alpha45.11's unowned supply placeholders; Recommended contains owned bank items only.
- Added regression checks for Black mask / Slayer helmet bank-name semantics.
- Retains the responsive alpha45.10 full-bank layout and no recalculation on each withdrawal while Personal BiS bank mode is active.

## r0.5.0-alpha45.13
- Reordered the visible 4x3 loadout grid exactly as requested:
  - Head | Neck | Ammo | Prayer
  - Body | Cape | Weapon | Shield
  - Legs | Feet | Hands | Ring
- `On Slayer Task` is hidden while logged out and whenever the Attack style controls are not available.
- Widened the logged-out/state message panel and text area so `Log in to begin` copy is not clipped.
- Retains alpha45.12's corrected owned Recommended supplies and Slayer headgear calculations.

## r0.5.0-alpha45.14
- Rebuilt the Target section with GridBagLayout instead of BoxLayout.
- Target heading and `Load target from Slayer task` are anchored WEST at the far-left usable sidebar edge; search and NPC dropdown retain full width.
- Personal BiS bank mode now hides thin native bank-tab divider decorations whose original positions caused stray lines through `Other items`.
- Native divider visibility is restored when Personal BiS bank mode closes.
- Personal BiS retains its own intentional matching dividers for the selected combat-style section, `Recommended`, and `Other items`.
- Preserves alpha45.13's loadout order and alpha45.12's owned supplies/Slayer mechanics.

## r0.5.0-alpha45.15
- Personal BiS section separators now use a thin, understated native-bank-style divider for combat style, Recommended and Other items.
- Replaced the wide `Personal BiS` bank button with a compact icon-only square.
- The bank button is positioned at the right end of the title bar immediately left of the native close-button area and sized to visually match it.
- Preserves the 45.14 Target rebuild, native-divider cleanup, bank responsiveness and all mechanics.

## r0.5.0-alpha45.16
- New Personal BiS identity: approved silver combat helm with crossed gold swords.
- The same icon is used by the RuneLite sidebar NavigationButton and the compact bank button.
- Bank button moved into the requested compact slot immediately left of the bank close button, while leaving adjacent plugin-button space intact.
- Preserves alpha45.15's native-style section dividers and all existing mechanics/UI behaviour.

## r0.5.0-alpha45.17
- Added 4 px horizontal inset to the Personal BiS sidebar content so controls no longer sit flush against the panel edges.
- Nudged only the compact Personal BiS bank button left/up to align with neighbouring native/plugin bank controls.
- Sidebar logo, bank logo, bank dividers, recommendations and combat/Slayer mechanics are unchanged.

## r0.5.0-alpha45.18
- Moved only the compact Personal BiS bank button 24 px left.
- Vertical position is unchanged from alpha45.17.
- No sidebar, bank-layout, recommendation, Slayer, or combat-mechanics changes.

## r0.5.0-alpha45.19
- Fine-tuned only the Personal BiS bank button position from alpha45.18.
- Moved the button 12 px right and 1 px up.
- No icon, sidebar, bank layout, recommendation, Slayer, or combat-mechanics changes.

## r0.5.0-alpha45.20
- Final bank-button alignment pass: moved Personal BiS 3 px right from alpha45.19.
- Vertical position is unchanged.
- No other UI, bank, recommendation, Slayer, or combat-mechanics changes.

## r0.5.0-alpha45.21
- Corrected the bank-button adjustment direction.
- Personal BiS bank button moved 6 px left from alpha45.20 (3 px left of alpha45.19).
- Vertical position unchanged.
- No other changes.

## r0.5.0-alpha45.22
- Final bank-button alignment tweak: moved Personal BiS 2 px left from alpha45.21.
- Vertical position unchanged.
- No other changes.

## r0.6.0-alpha46 — Equipment Intelligence Audit
- Freezes the alpha45.22 UI baseline; no intentional visual changes.
- Melee optimiser now permits empty optional equipment slots instead of forcing a potentially negative-bonus owned item.
- Explicit 2H/shield audit invariant added.
- Ranged optimiser now always preserves owned Salve (i)/(ei) candidates so undead-specific effects cannot be lost to the ordinary top-N shortlist.
- Magic optimiser beam widened from 24 to 80 and now preserves effect/set candidates outside raw top-N: Salve, imbued Slayer headgear, Void Mage, Virtus and elemental tomes.
- Existing monster-specific melee effects audited: Arclight/Emberlight, Silverlight/Darklight, Dragon hunter lance, Keris variants, Fang and Scythe.
- Existing ranged effects audited: DHCB, Twisted bow, Bowfa/crystal, Void, ammo compatibility and enchanted-bolt EV.
- Requirements remain enforced before candidates enter final optimisation.
- Prayer selection remains part of the DPS calculation for melee/ranged/magic, not display-only.
- Added alpha46 mechanics regression invariants for 2H shields, imbued Slayer rules, monster-specific weapon retention, ranged Salve and Magic Salve/Virtus synergy retention.

## r0.6.0-alpha46.1 — Controlled Matchup Validation
- Adds controlled target-attribute regression checks on top of alpha46.
- Undead: verifies melee Salve(e) is active only against undead and ranged Salve(ei) increases both accuracy and damage only on eligible undead.
- Demon: verifies Emberlight is retained as a monster-specific weapon and verifies imbued Slayer headgear produces a Magic task effect when on-task.
- Dragon: verifies Dragon hunter lance retention and uses the same Salve setup as a non-undead negative control.
- Kalphite: verifies Keris retention.
- These checks exercise production effect/policy layers rather than duplicating the formulas inside the tests.
- No UI changes; alpha45.22 remains the frozen visual baseline.

## r0.6.0-alpha46.2 — Final Loadout Selection Audit
- Adds a shared final-winner policy used by Melee, Ranged and Magic optimizers.
- Rejects non-finite DPS candidates and only replaces the current winner with genuinely higher DPS.
- Adds regression coverage for 2H weapon/shield exclusion.
- Adds final-selection ammo compatibility checks: crossbow/bolts, crossbow/arrows rejection and self-ammo weapons.
- Re-validates Salve, imbued Slayer headgear and Virtus retention through the final candidate-selection layer.
- No UI or bank-layout changes; alpha45.22 remains the frozen visual baseline.

## r0.7.0-alpha47 — Recommended Inventory Intelligence
- Keeps the alpha45.22 visual/bank layout baseline unchanged.
- Recommended remains deliberately simple: runes, one combat potion, prayer potion and food.
- Magic recommendations follow the spell selected by the Magic optimiser.
- Rune quantities are calculated for 100 casts, while respecting infinite-rune staves/Kodai/tome providers.
- Every recommended quantity is now capped to the amount actually owned in the bank snapshot.
- Unowned supplies are never invented or shown as placeholders.
- Food selection remains strongest-first and now includes Anglerfish and Dark crab ahead of the existing food list.
- Potion selection accepts the best owned dose; melee retains Super combat first with Super attack as a simple fallback.
- Added alpha47 regression tests for owned-only supplies, rune quantity caps, lower-dose potions and strongest-owned food.

## r0.7.0-alpha47.2 — Elemental Weakness Fix
- Fixes elemental spell base-hit isolation: Air/Water/Earth/Fire spells no longer inherit the strongest other element's spell from the same tier.
- Centralises elemental weakness detection and severity for Magic DPS.
- Weakness continues to affect both accuracy and damage during the optimiser's real DPS comparison.
- Adds a Dharok the Wretched regression: 50% Air weakness applies to Air spells and not Fire spells.
- Adds base-hit regression checks so Wind Blast remains Wind Blast rather than inheriting Fire Blast's base max.
- No UI or Recommended bank-layout changes.

## r0.7.0-alpha47.3 — Loadout Validation Mode
- Restores a temporary development-only Loadout Validation section beneath the existing Loadout stats.
- Magic shows the top production optimizer comparisons with weapon, spell, DPS, accuracy, max hit and effect notes (including elemental weakness notes).
- Melee shows competing weapons with DPS, accuracy and max hit.
- Ranged shows weapon/ammo combinations with DPS, accuracy, max hit and effect labels.
- Uses the actual optimizer reports that choose the live loadout; this is verification UI, not a second calculation path.
- Existing target/style/loadout UI and bank layout remain unchanged.
- Intended to be removed/hidden for the full release after validation is complete.

## r0.7.0-alpha47.3.1 — Compile fix
- Fixes Loadout Validation to use MagicCombatResult.getMechanicNote(), the existing diagnostic accessor.
- No mechanics or UI-layout changes.

## r0.7.0-alpha47.3.2 — Magic Validation Expansion
- Fixes literal `%n` text in the DEV validation panel.
- Magic validation now shows total Magic attack for each evaluated weapon/spell result.
- DEV Magic report retains every evaluated weapon winner instead of truncating to five, so owned alternatives such as Mystic earth staff can be verified directly.
- This does not force Mystic earth staff or change the BiS winner; it exposes whether it was evaluated and its real DPS.
- Production Magic winner selection and the approved normal UI/bank layout are unchanged.

## r0.7.0-alpha47.3.3 — Full Magic Weapon Evaluation
- Removes the top-12 generic-score cutoff from the Magic weapon stage.
- Every usable owned Magic weapon now reaches contextual evaluation.
- Final selection still uses real calculated DPS; no weapon is artificially preferred.
- This allows lower raw-score weapons such as Mystic elemental staves to compete when spell compatibility, bank runes or elemental weakness make them relevant.
- DEV validation continues to retain every evaluated weapon winner.
- Adds a regression invariant preventing Magic weapon evaluation from silently returning to a top-N cutoff.
- No normal sidebar or bank-layout changes.

## r0.7.0-alpha47.3.4 — Magic Eligibility Audit
- DEV validation now reports base/boosted Magic and whether Augury is unlocked.
- Reports every ranked owned Magic weapon and its requirement status.
- For target-relevant elemental spells, reports rejection reasons: Magic level, autocast compatibility, or missing runes.
- Surge spells above the account's base Magic are explicitly shown as level-blocked.
- Diagnostic only: winner calculation and frozen normal UI/bank layout are unchanged.

## r0.7.0-alpha47.3.5 — Visible Magic Weapon Audit
- DEV validation is now independently scrollable so the complete eligibility audit can be inspected.
- Adds an explicit `Magic weapon audit: N eligible owned weapons checked | M blocked` summary.
- The full per-weapon/per-spell eligibility audit remains below the calculated alternatives.
- Diagnostic/UI-only change; combat calculations, winner selection, normal sidebar layout and bank layout are unchanged.

## r0.8.0-alpha48.0 — Melee / Dragon Validation
- Begins the Alpha 48 broad combat-validation phase.
- Every usable owned melee weapon now reaches real DPS evaluation; the old generic top-5 weapon cutoff is removed.
- This ensures matchup-specific weapons such as Dragon Hunter Lance, Fang, Keris, Arclight/Emberlight and Scythe cannot be hidden by raw equipment score.
- Melee DEV validation now exposes DPS, accuracy, max hit, attack bonus, strength bonus, weapon speed, chosen stance and prayer for every evaluated weapon.
- Adds a regression invariant preventing the melee weapon stage from silently returning to a top-N cutoff.
- Keeps the Alpha 47 Magic eligibility audit for cross-checking.
- No changes to the approved normal sidebar or bank layout.

## r0.8.0-alpha48.1 — Demonbane Mechanics Fix
- Corrects Darklight and Silverlight against demon targets: +60% accuracy as well as +60% damage.
- Arclight and Emberlight remain +70% accuracy and +70% damage against normal demonbane-affinity targets.
- The previous implementation applied Darklight/Silverlight's damage multiplier but omitted their accuracy multiplier.
- Keeps exhaustive Alpha 48 melee weapon evaluation and DEV comparison output.
- No approved normal UI or bank-layout changes.

## r0.8.0-alpha48.1.1 — Compile Fix
- Removes the scope-invalid `demon` fixture references accidentally added to MechanicsSelfTest in 48.1.
- The actual Darklight/Silverlight +60% demon accuracy fix from 48.1 is unchanged.
- No combat/UI/bank changes beyond that 48.1 mechanics fix.

## r0.8.0-alpha48.2 — Magic Weapon / Spell Compatibility Validation
- Adds regression checks for powered staves, Iban Blast, Magic Dart, Ancient spells and standard elemental spells.
- Powered staves such as Warped sceptre/Trident are protected from being treated as ordinary autocast staves.
- Iban Blast remains restricted to Iban's staff variants.
- Magic Dart remains restricted to its supported weapon family.
- Ancient spells remain restricted to Ancient-autocast-capable weapons.
- Mystic elemental staves remain eligible for standard elemental combat spells.
- DEV Magic winners now explicitly state that their weapon/spell compatibility rule accepted the pairing.
- No normal sidebar or bank-layout changes.

## r0.8.0-alpha48.3 — Ranged Weapon / Ammo Validation
- Removes the ordinary Ranged weapon top-N cutoff: every usable owned Ranged weapon reaches contextual DPS evaluation.
- Adds explicit DEV output for ammo type, target defence class, and accepted weapon/ammo compatibility.
- Corrects common enchanted gem-bolt metal tiers so Rune/Dragon crossbow compatibility is not accidentally treated as unrestricted.
- Adds regression checks for Rune crossbow + ruby/diamond bolts and dragon-bolt rejection, plus Dragon crossbow + dragon bolts.
- No approved sidebar/bank layout changes.

## r0.8.0-alpha48.4 — Mechanics-aware performance shortlist
- Restores bounded weapon evaluation after Alpha 48 full-scan validation.
- Melee: top 12 ordinary weapons plus all matchup-specific and synergy weapons.
- Ranged: top 12 ordinary weapons plus special/synergy weapons; ammo is still filtered for compatibility before evaluation.
- Magic: top 12 ordinary weapons plus powered/restricted/special weapons, set synergies, and elemental rune-provider staves matching the target weakness.
- Existing beam limits remain in place; combat formulae are unchanged.
- Goal: dramatically reduce expensive loadout combinations without losing Keris, demonbane, Iban, elemental-weakness, Slayer/set, or other contextual winners.

## r0.8.0-alpha48.4.1 — Self-test correction
- Replaces the obsolete Alpha 47 assertion that required unlimited Magic weapon evaluation.
- The self-test now verifies the intentional Alpha 48.4 bounded ordinary candidate policy (12 for Melee, Ranged and Magic).
- No combat formula, shortlist, bank or UI changes from 48.4.

## r0.8.0-alpha48.4.2 — Complete obsolete-test cleanup
- Audited MechanicsSelfTest for remaining temporary Alpha 47/48 full-scan assertions.
- Removes the remaining Alpha 48.0 assertion requiring unlimited Melee weapon evaluation.
- Updates stale full-Magic/full-Melee PASS labels to the current mechanics-aware shortlist policy.
- No optimiser, combat, UI or bank behaviour changes from 48.4.1.

## r0.8.0-alpha48.4.3 — Anti-freeze hot-loop optimisation + prayer fallback
- Reduces Melee beam 80 -> 32 and ordinary per-slot choices 6 -> 4.
- Reduces Ranged beam 100 -> 32 and ordinary non-ammo per-slot choices to 4.
- Reduces Magic beam 80 -> 32 and ordinary per-slot choices 5 -> 4.
- Melee/Ranged beam sorting now calculates each candidate DPS once and caches it for that trim instead of repeatedly recalculating inside the sort comparator.
- Always retains the best positive-Prayer candidate in each slot, and Melee/Ranged use Prayer bonus as the tie-break when DPS is identical. This restores useful Prayer gear instead of leaving zero-impact slots empty.
- Contextual/synergy retention from 48.4 remains intact.
- DEV validation now reports total calculation time in milliseconds.
- Combat formulae and approved UI layout are unchanged.

## r0.8.0-alpha48.4.4 — Ranged regression-test correction
- Updates the source-structure regression hook for the new bounded Ranged weapon shortlist signature introduced by 48.4.3.
- The test still verifies weapon-first optimisation; it no longer expects the obsolete exact method call text.
- No optimiser, combat, prayer fallback, UI, or performance behaviour changes from 48.4.3.

## r0.8.0-alpha48.4.4 — Ranged regression-test correction
- Updates the weapon-first regression hook to recognise the new bounded Ranged weapon shortlist call introduced by 48.4.3.
- The optimiser is still weapon-first; only the source-text signature changed.
- Keeps all 48.4.3 anti-freeze, DPS-cache, smaller-beam and Prayer fallback changes.

## r0.8.0-alpha48.5 — Background optimiser
- Splits recommendation refresh into a RuneLite client-thread snapshot stage and a dedicated daemon optimisation worker.
- Client-thread stage captures ranked equipment candidates while RuneLite/ItemManager/requirement APIs are safe to access.
- Melee, Ranged and Magic beam/DPS searches now run on `personal-bis-optimizer`, off the RuneLite client thread.
- Adds ranked-snapshot optimiser entry points so background calculation does not call client-backed equipment ranking.
- Generation IDs discard stale results when target/style/task/bank state changes during a calculation.
- Bank-filter updates are marshalled back to RuneLite's client thread; Swing result rendering remains on the EDT.
- Keeps 48.4.4's 32-wide beams, cached trim DPS, mechanics-aware shortlists and Prayer fallback.
- DEV `Calculation` timing now measures the background optimisation stage.

## r0.8.0-alpha48.5.1 — Compile fix
- Restores `styleScore()` to use its original live ranking source.
- The 48.5 background calculation path still uses captured ranked snapshots.
- Fixes the out-of-scope `rankedSnapshot` reference introduced during the 48.5 refactor.

## r0.8.0-alpha48.6 — Prayer/unlock intelligence
- Piety and Chivalry are no longer inferred from Prayer level alone.
- Captures Knight Waves Training Grounds state from RuneLite varbit 3909; completion state 8 unlocks Chivalry/Piety.
- Melee optimiser only evaluates Piety/Chivalry when the account has the required Prayer level AND Knight Waves unlock.
- Existing Rigour, Augury, Deadeye and Mystic Vigour permanent-unlock checks remain intact.
- Adds pure regression policy tests for Piety, Chivalry, Rigour and Augury.
- DEV validation prints Piety/Chivalry/Rigour/Augury unlock state for live verification.
- Keeps 48.5.1 background optimiser and performance behaviour unchanged.

## r0.8.0-alpha48.7 — Exact Slayer melee ordering
- Corrects Black mask/Slayer helmet melee handling from an effective-level approximation to the OSRS gear-bonus ordering.
- On-task x7/6 accuracy is now applied to the completed ordinary attack roll.
- On-task x7/6 damage is now applied after the ordinary max-hit calculation, then floored.
- Salve remains mutually exclusive with the Slayer head bonus and takes precedence.
- Adds regression tests for on-task, off-task, missing Slayer head, flooring, and Salve precedence.
- No shortlist, UI, prayer-unlock, or background-thread changes.

## r0.8.0-alpha48.7.1 — Self-test compile fix
- Replaces the accidentally introduced nonexistent `eq()` test helper with the existing `yes()` assertion helper.
- No combat formula or runtime behaviour changes from 48.7.

## r0.8.0-alpha48.8 — Melee attack-interface eligibility
- Fixes Keris being evaluated for Crush despite having no Crush attack option.
- Adds explicit `Stab Sword` and `Partisan` mappings; Keris/Keris partisan are Stab/Slash-capable, not Crush-capable.
- Expands mappings for the bundled melee weapon categories.
- Unknown/unmapped weapon categories now fail closed instead of being granted every melee stance.
- Melee optimiser already checks `MeleeAttackStyleResolver.supports()` before evaluating a weapon; this fix makes that gate authoritative.
- Adds regression checks for Keris Stab vs Crush, blunt weapons, slash swords and unknown categories.
- Keeps 48.7.1 Slayer ordering and 48.5 background optimiser unchanged.

## r0.8.0-alpha48.8.1 — Complete neutral-slot fallback
- Keeps 48.8's strict melee weapon attack-interface validation.
- Adds a post-optimisation completion pass for empty melee equipment slots.
- An owned/equippable item may fill an empty slot only when adding it does not reduce the winning loadout's DPS.
- Among equal-DPS fillers, higher Prayer is preferred.
- Completion happens after the offensive beam search, so neutral gear cannot distort weapon or style ranking.
- Two-handed weapons still leave the shield slot empty.
- Adds regression invariants for the completion hook and no-DPS-loss rule.

## r0.8.0-alpha48.8.3.1 — Keris integer damage-stage audit
- Applies Keris's 33% Kalphite damage modifier to the integer max hit before averaging, preserving OSRS floor/rounding order.
- The 1/51 vicious blow then triples the already modified Keris hit distribution.
- Removes the old floating-point shortcut that multiplied average damage by 1.33 * 53/51.
- DEV melee audit now exposes Keris modified max, proc max and average successful-hit damage.
- Attack-roll/accuracy, attack-interface eligibility, Slayer ordering and background optimizer are unchanged.

## r0.8.0-alpha48.8.3.1 — compile hotfix
- Fixes the DEV Keris audit compile error by using the canonical `monster` already captured by the calculation/UI update path instead of the out-of-scope `selectedMonster` local.
- No combat mechanics, optimizer, UI layout, or 48.8.3 Keris calculation logic changed.

## r0.8.0-alpha48.8.4 — Exact melee expected-damage parity
- Preserves 48.8.3.1 equipment selection, attack-style eligibility and background optimisation.
- Models OSRS successful-hit zero conversion: expected ordinary damage is `max/2 + 1/(max+1)`.
- Keris ordinary and 1/51 triple-hit distributions are now averaged separately, including zero-to-one conversion for each distribution.
- For the validated max-34 Keris case, expected successful-hit damage becomes ~17.69487 instead of 17.66667.
- Keeps Keris max 34 and proc max 102 unchanged.
- Adds regression checks for ordinary max-34 expected damage, Keris expected damage and proc max.

## r0.8.0-alpha48.8.4.1 — Regression-test hotfix
- Updates the stale alpha48.8.3 Keris average-hit assertion to the alpha48.8.4 successful-hit formula.
- Expected max-34 Keris average is now 17.694868238557557.
- No combat-calculation, equipment-selection, UI, or optimiser behaviour changed from 48.8.4.

## r0.8.0-alpha48.8.5 — Ranged successful-hit parity
- Applies the same OSRS successful zero-to-one expected-hit correction validated for melee to ordinary Ranged hits.
- A max hit of 12 now averages 6 + 1/13 on a successful accuracy roll instead of exactly 6.
- Preserves Ranged attack roll, max-hit, ammo compatibility, enchanted-bolt EV, optimizer, and UI behaviour.
- Adds a max-12 ranged expected-damage regression check for the Greater demon / Rune dart reference case.

## r0.8.0-alpha48.8.6 — Magic successful-hit parity
- Applies the validated successful-hit zero-to-one expectation to standard/autocast Magic spells.
- Applies the same rule to powered-staff built-in spells.
- Does not alter Magic accuracy, max-hit construction, elemental weakness, tomes, spell compatibility, or optimizer selection.
- Dharok + Iban Blast live reference: 97.99% accuracy, max 25, target ~4.09558 DPS.
- Adds max-25 standard Magic and max-12 powered-Magic expected-hit regressions.

## r0.8.0-alpha48.8.7 — Ranged/Magic neutral-slot completion
- Extends the proven Melee post-optimization completion rule to Ranged and Magic.
- Empty slots are filled from owned/equippable gear only when DPS does not decrease.
- Equal-DPS fillers prefer higher Prayer bonus.
- Ranged preserves required ammo compatibility and does not insert ammo for internal-ammo weapons.
- Two-handed shield restrictions remain enforced.
- Magic completion preserves the already-selected spell/powered staff and recalculates DPS before accepting filler.
- No combat-formula changes from 48.8.6.

## r0.8.0-alpha48.8.8 — Ranged ammo-slot occupancy intelligence
- Separates projectile source from equipment-slot occupancy.
- Thrown weapons such as darts/knives leave the Ammo equipment slot free for neutral Prayer gear.
- Internally supplied/loaded weapons such as Toxic blowpipe also leave the Ammo slot free for neutral Prayer gear.
- Bows and crossbows that require equipped arrows/bolts reserve the Ammo slot for compatible ammunition.
- Neutral completion still refuses any filler that reduces DPS and prefers Prayer on equal DPS.
- No combat-formula changes from 48.8.7.

## r0.8.0-alpha48.8.8.1 — Ammo regression-test hotfix
- Updates the legacy Ranged optimiser source regression for 48.8.8 ammo-slot occupancy semantics.
- External-ammo weapons still exhaustively evaluate owned usable arrows/bolts.
- Free-ammo-slot weapons defer that slot to neutral completion for blessings/Prayer gear.
- No combat, optimiser, equipment-selection, or UI behaviour changed from 48.8.8.

## r0.9.0-alpha49.0 — Bank & Recommended Inventory audit
- Starts Alpha 49 without changing the validated Alpha 48 combat formulas.
- DEV validation now lists every Recommended supply with recommended quantity, current bank quantity, and selection reason.
- Supply objects carry an audit reason while remaining backward-compatible with existing bank UI.
- Owned-only behaviour and quantity caps are regression-tested.
- Gear recommendations continue to come directly from the final selected optimized loadout.
- Bank presentation, 45.22 button position, and background optimiser architecture are unchanged.

## r0.9.0-alpha49.1 — Full rune stacks + combo food
- Required spell runes now surface the player's full owned bank stack instead of an arbitrary 100-cast quantity.
- Infinite-rune providers still suppress the rune they provide.
- Food is split into two simple independent recommendations: strongest owned normal food and strongest owned combo food.
- Cooked karambwan is the initial combo-food category.
- Normal and combo food quantities remain capped by the simple food target and actual bank ownership.
- Potions and Prayer potion behaviour are unchanged.
- Alpha 49 Recommended audit instrumentation is retained.
- Alpha 48 combat mechanics remain untouched.

## r0.9.0-alpha49.1.1 — legacy test hotfix
- Test-only correction: alpha39's obsolete 100-cast rune quantity assertion now expects Alpha 49.1 full owned rune stacks.
- Production recommendation logic is unchanged from 49.1.
- Combat mechanics are unchanged.

## r0.9.0-alpha49.2 — Ranged ammo recommendation correctness
- Fixes Bowfa/internal/self-ammo weapons incorrectly accepting bolts/arrows/darts as neutral Ammo-slot fillers.
- Free Ammo slots now accept only non-ammunition neutral gear such as blessings.
- Bows/crossbows that require equipped ammo now surface the exact selected compatible ammo in Recommended.
- Required external ammo uses the player's full owned bank stack, matching the Alpha 49 rune-stack rule.
- Internal/thrown/self-ammo weapons do not create a separate ammo supply recommendation.
- Existing bank UI/layout and Alpha 48 combat formulas are unchanged.

## r0.9.0-alpha49.2.1 — Osmumten's fang parity correction
- Fang Stab accuracy now uses the current outside-ToA same-defence-roll double-accuracy formula.
- Fang normal displayed max is floor(85% of true max); successful damage rolls from floor(15%) through floor(85%).
- A true max of 53 therefore displays max 45 and averages 26 damage on a successful hit.
- Replaced the obsolete squared-miss Fang regression with exact current-formula tests.
- DEV validation exposes Fang true max, range, and successful average.
- Other validated combat and bank mechanics are unchanged.

## alpha49.2.1.1 test-only hotfix
- Fixed MechanicsSelfTest compile failure: this test harness has no eq(String,int,int) helper.
- Replaced the two new integer assertions with the existing yes(String,boolean) helper.
- Production source is byte-identical to alpha49.2.1.

## r0.9.0-alpha49.2.3 — Fang parity instrumentation
- Diagnostic-only follow-up based on the confirmed 49.2.1.1 mechanics baseline.
- Fang DEV row now prints effective Attack, total attack bonus, final attack roll,
  monster Defence level, Stab defence bonus, final defence roll, and six-decimal accuracy.
- No combat formula changes in this build.

## r0.9.0-alpha49.2.4 — effective-level pipeline instrumentation
- Diagnostic-only build; no combat formula changes.
- Fang DEV output now shows raw and boosted Attack, prayer multiplier and floored result,
  stance Attack bonus, +8 stage, Void stage, and final effective Attack.
- Vorkath defence pipeline now shows Defence level, +9 stage, Stab defence, +64 stage, and final roll.

## r0.9.0-alpha49.2.5 — Salve integer-stage correction
- Corrects melee Salve ordering: Salve now multiplies the completed base attack roll and base max hit.
- Does not alter ordinary non-Salve melee, Fang's double-roll formula, ranged, magic, Slayer, Keris, or demonbane.
- Controlled Vorkath/Fang reference: base attack roll 24,426 -> Salve(e) 29,311 -> 84.3700237% accuracy.
- With Fang successful-hit average 26.0 at 5 ticks, reference DPS is 7.3120687.

## r0.9.0-alpha49.2.6 — Salve damage rounding correction
- Keeps the already validated 49.2.5 Fang/Salve accuracy path unchanged.
- Corrects Salve melee damage rounding by carrying the base max-hit numerator through
  the Salve multiplier before the final /640 truncation.
- This specifically addresses one-max-hit losses caused by truncating the ordinary
  max hit before applying Salve.

## r0.9.0-alpha49.3 — Recommended Inventory consolidation
- Built directly from the validated 49.2.6 melee baseline; combat formulas are unchanged.
- Locks in Recommended invariants: only owned supplies, quantities never exceed bank ownership.
- Regression coverage now checks full owned Magic rune stacks, infinite-rune suppression,
  full selected external Ranged ammo stacks, no external ammo for Bowfa/internal weapons,
  strongest owned normal food plus independent karambwan combo food, and owned potions.
- Existing bank UI remains deliberately simple and follows the final optimized loadout.

## r0.9.0-alpha49.3.1 — Magic weakness + Void set evaluation
- Elemental weakness now applies only to eligible standard elemental Strike/Bolt/Blast/Wave/Surge spells.
  Ancient Magicks such as Smoke Barrage no longer inherit Air/Water/Earth/Fire weakness.
- Full owned/equippable Void Mage and Elite Void Mage bundles are explicitly evaluated so the optimizer
  cannot prune individual low-raw-stat pieces before their complete set bonus exists.
- Combat baselines outside these Magic-specific paths are unchanged.

## r0.9.0-alpha49.3.1.1 — test-constructor hotfix
- Test-only fix: corrected the Dharok elemental-weakness regression fixture to use the existing
  MonsterDefinition constructor signature (including size before weakness element/severity).
- Production Java is byte-identical to alpha49.3.1.

## r0.9.0-alpha49.3.2 — Magic max-hit pipeline diagnostic
- Diagnostic-only build. No Magic, melee, ranged, bank, or optimizer formula changes from 49.3.1.1.
- The winning Magic loadout now prints spellbook/element/tier, scaled base max, flat max,
  equipment Magic damage, prayer damage, elemental weakness, ordinary max, weapon/loadout/tome
  multipliers, final max, and detected Void set pieces/activation.

## r0.9.0-alpha49.3.3 — Wiki Magic-damage unit correction
- Fixed bundled Wiki equipment `magic_str` normalization: the dataset stores tenths of a percent,
  so 50 means 5.0%, not 50%.
- This was inflating the observed Dharok loadout to +90% equipment Magic damage and Smoke Barrage max 52.
- DEV now prints each equipped item's normalized Magic-damage contribution.
- Elemental-weakness eligibility and the validated melee/ranged/Recommended baselines are unchanged.

## r0.9.0-alpha49.3.4 — Elemental Magic accuracy ordering
- Elemental weakness now modifies the Magic attack roll before the ordinary hit-chance formula,
  rather than multiplying the final hit chance and capping it at 100%.
- DEV exposes base attack roll, weakness-adjusted attack roll, defence roll and final accuracy.
- Standard elemental max-hit keeps weakness as a separately floored base-damage contribution.
- No melee, ranged, bank or Recommended Inventory mechanics changed.

## r0.9.0-alpha49.3.4.1 — regression expectation hotfix
- Test-only correction: the exact ordinary hit-chance result for attack roll 27,553
  versus defence roll 530 is 0.9903462292226174 (99.0346229%).
- Production Java is byte-identical to alpha49.3.4.

## r0.9.0-alpha49.3.5 — Wind Surge max-hit diagnostic
- Diagnostic-only follow-up to the validated elemental accuracy correction.
- No combat/optimizer mechanics changed from 49.3.4.1.
- DEV now exposes primary raw/floored damage, weakness raw/floored damage,
  current ordinary max, combined raw damage, and final max for the winning Magic spell.
- Intended reference: Dharok + Wind Surge, to isolate the remaining max 32 vs Wiki max 34 discrepancy.

## r0.9.0-alpha49.3.6 — Elemental spell tier scaling correction
- Corrected elemental spell scaling to current OSRS rules: Wind/Water/Earth spells scale
  to the strongest unlocked spell in the same Strike/Bolt/Blast/Wave/Surge tier,
  regardless of element.
- At Magic 87, Wind Surge now uses base max 22 (Water Surge unlocked), not 21.
- Dharok regression: base 22 + 8% equipment Magic damage + 50% Air weakness -> max 34
  in the currently observed Personal BiS calculation path.
- The already validated elemental Magic accuracy ordering is unchanged.
- No melee, ranged, bank, or Recommended Inventory mechanics changed.

## r0.9.0-alpha49.3.6.1 — stale tier-scaling test hotfix
- Test-only: updated the old Water Blast level-73 assertion from 14 to 16.
  At level 73, Fire Blast is unlocked, so same-tier elemental scaling makes Water Blast base 16.
- Production Java is byte-identical to alpha49.3.6.

## r0.9.0-alpha49.3.6.2 — remaining stale elemental-scaling test hotfix
- Test-only: updated the old alpha47.2 Wind Blast level-73 assertion from 13 to 16.
  At level 73, Fire Blast is unlocked, so all Blast-tier elemental spells scale to base 16.
- Production Java is byte-identical to alpha49.3.6.1 / alpha49.3.6.

## r0.9.0-alpha50.0 — Optimizer completeness phase
- New frozen baseline: alpha49.3.6.2 combat formulas and alpha49.3 Recommended Inventory.
- Magic full-set synergies (currently Void Mage / Elite Void Mage) are now seeded before slot pruning.
- Set pieces remain locked on their set branch while the optimizer fills and compares the remaining
  neck/cape/shield/feet/ring/etc. slots normally.
- This fixes the previous weakness where Void was injected only after beam construction and therefore
  competed as a sparse four-piece loadout rather than as a complete optimized setup.
- Ordinary non-set branches remain in the same search and compete by calculated DPS.
- No melee, ranged, Magic combat formula, bank, or Recommended Inventory mechanics changed.

## r0.9.0-alpha50.1 — Void optimizer audit
- Diagnostic follow-up; no combat formula changes.
- Magic optimizer records whether a complete owned/equippable Void Mage bundle is seeded for each weapon branch.
- DEV reports the evaluated Magic loadout's Void accuracy/damage multipliers and whether the Void set effect was recognized.
- This distinguishes bundle construction/pruning failures from set-effect recognition failures.

## r0.9.0-alpha50.1.1 — compile hotfix
- Removed an invalid DEV-only call to MagicLoadoutComparison.getLoadout(); that summary type does not expose a loadout.
- Alpha 50.1 optimizer VOID AUDIT seed diagnostics remain intact.
- No optimizer or combat mechanics changed from alpha50.1.

## r0.9.0-alpha50.2 — complete-loadout family search
- Replaces the shared weapon-first Magic armour beam with independent complete-loadout family beams.
- For each eligible weapon, Ordinary, Void Mage, and Elite Void Mage families are expanded/pruned independently.
- Complete set families cannot be discarded by higher raw-stat ordinary armour before real combat DPS is calculated.
- Only completed family winners compete globally by calculated DPS.
- DEV audit now reports family count and each family's spell/DPS/accuracy/max/effect multipliers.
- Validated combat formula classes are unchanged.

## r0.9.0-alpha50.2.1 — self-test hotfix
- Updated the alpha48.8.7 source-shape regression to recognize alpha50.2's familyBest neutral-slot completion.
- The invariant is unchanged: Magic family winners still pass through completeNeutralSlots before competing globally.
- Production Java is byte-identical to alpha50.2.

## r0.9.0-alpha50.3 — attack-first Magic intelligence
- Magic now ranks up to 15 viable attacks/spells for the selected target before choosing weapons.
- Every owned/equippable weapon capable of autocasting a shortlisted attack is evaluated; Magic is no longer gated by the raw top-12 weapon score.
- Blue moon spear is explicitly recognized for Standard elemental, Arceuus, and Ancient autocasting.
- Added Arceuus Inferior/Superior/Dark Demonbane candidates.
- Added Mark of Darkness variants for Superior/Dark Demonbane, gated by A Kingdom Divided and rune availability.
- Demonbane: +20% base accuracy; with Mark +40% accuracy/+25% damage; Purging staff + Mark +80% accuracy/+50% damage.
- Mark rune requirements (Soul + Cosmic) are carried into the selected spell resource package.
- Existing complete Ordinary/Void/Elite Void family optimization remains intact.
- Previously validated elemental/weakness/Fang/Recommended Inventory formulas were not altered.

## r0.9.0-alpha50.4 — Melee/Ranged optimizer completeness
- Melee now evaluates every owned/equippable weapon that supports the chosen attack style instead of allowing the raw top-12 score to exclude candidates.
- Ranged now evaluates every owned/equippable ranged weapon instead of allowing the raw top-12 score to exclude candidates.
- Existing exact target-DPS beam ranking remains in place for both styles.
- Ranged ammo compatibility, internal/external ammo rules, and neutral-slot completion remain unchanged.
- Validated Fang/Keris/Ranged combat formulas are unchanged.
- Added alpha50.4 regression checks for all-owned weapon evaluation, exact-DPS beam ranking, and ranged ammo compatibility.

## r0.9.0-alpha50.4.1 — self-test hotfix
- Updated the legacy Ranged weapon-first source-shape regression for 50.4's exhaustive allUsable weapon source.
- It still verifies weapon-first optimization order and now also verifies the old raw-rank top-N gate is absent.
- Production Java is byte-identical to alpha50.4.

## r0.9.0-alpha50.4.2 — dedicated Ranged ammo families
- Hunters' crossbow: Kebbit bolts / Long kebbit bolts only.
- Hunters' sunlight crossbow: Sunlight antler / Moonlight antler bolts only; explicitly rejects kebbit and ordinary metal/gem bolts.
- Ordinary crossbows reject kebbit, antler, bolt-rack and bone-bolt special families.
- Karil's crossbow and Dorgeshuun crossbow receive dedicated bolt-rack / bone-bolt handling.
- Alpha 50.4.50.25 ports the Wiki calculator's sustained ranged hit transforms: charged Tonalztics and Dark bow independent double hits, full Karil/Amulet half-hit proc, and Seeking ammunition's accurate minimum hit of three.
- Alpha 50.4.50.26 completes the remaining sustained ranged ordering pass: chinchompa fuse/distance accuracy, charged Craw's/Webweaver Wilderness bonuses, Revenant/Avarice exclusivity, additive Slayer/revenant damage, and Tonalztics' 75% per-hit max. PBIS uses the Wiki default chinchompa distance of four tiles and activates Wilderness weapon bonuses only for conservatively identified Wilderness targets.
- Alpha 50.4.50.27 ports the next sustained melee distribution family: Gadderhammer's Shade-only 95%/5% damage branches and full Verac's 25% guaranteed `1..max+1` branch. Complete Verac ownership is seeded before optimizer pruning; live-current-HP effects such as Dharok remain deferred until PBIS exposes that input explicitly.
- Alpha 50.4.50.28 ports charged Viggora's/Ursine chainmace Wilderness accuracy and damage, plus Revenant-only Amulet of avarice precedence over Salve and Slayer. Uncharged and off-Wilderness controls remain unchanged.
- Alpha 50.4.50.29 ports the ordinary sustained 5% demonbane accuracy/damage branch for Bone claws and Burning claws; special-attack-only claw mechanics remain deferred.
- Alpha 50.4.50.30 ports the sustained melee Crystal blessing max-hit stage, including the Wiki 1/3/2 helm/body/legs piece weights.
- Alpha 50.4.50.31 seeds a complete owned Crystal blessing melee bundle before optimizer pruning.
- Generic metal/gem bolt tier logic remains for ordinary crossbows.
- Added permanent ammo-family regressions.

## r0.9.0-alpha50.4.3 — Rigour max-hit correction
- Split Ranged prayer accuracy and ranged-strength multipliers.
- Rigour remains +20% accuracy but now correctly uses +23% ranged strength for max-hit calculations.
- Accuracy pipeline is intentionally unchanged.
- Added exact regression for the observed Vorkath reference: effective ranged strength 137 and max hit 44 at the captured 105 Ranged / +142 ranged-strength state.


## alpha50.4.6 — Vorkath/ruby diagnostic pass
- Ranged validation now exposes attack/defence rolls, effective Ranged level, attack speed, ordinary expected damage, total EV/attack, stance and prayer for every weapon candidate.
- Ruby candidates include a dedicated proc-rate/proc-hit/recomputed-EV line so Gearscape's Vorkath regression target can be compared directly.
- Ranged search breadth increased from 4 candidates/slot + beam 32 to 12 candidates/slot + beam 128 to test whether early beam pruning was hiding the best Rune-crossbow supporting loadout.
- Regression target from Gearscape: Vorkath, Rune crossbow + ruby bolts (e): 70.33% accuracy, max 37, EV/attack 18.7697, 6.25657 DPS.


## alpha50.4.8
- Ranged optimisation now anchors each weapon + compatible ammo pair before armour/jewellery beam search.
- Each compatible bolt/arrow family therefore receives an independent complete supporting-gear optimisation.
- Prevents ammo-dependent expected damage (notably ruby/diamond enchanted bolts) from entering the search too late.
- Crystal armour special accuracy/damage bonuses now correctly support both Bow of faerdhinen and Crystal bow; they remain disabled for crossbows and all unrelated weapons.
- Keeps alpha50.4.7 ranged diagnostic output for regression comparison.


## alpha50.4.9
- Fixed mechanicsSelfTest regression detection for the alpha50.4.8 weapon/ammo-anchored ranged optimizer.
- The compatibility invariant is now recognized in both the legacy completion path and the anchored path: incompatible ammo is rejected before beam search and remains guarded during neutral-slot completion.
- No combat formula changes from alpha50.4.8.

## alpha50.4.11 — Wiki-validated Vorkath ranged regressions
- Locked the validated Vorkath Rune crossbow + Ruby bolts (e) expected-value cases into mechanicsSelfTest.
- Without Kandarin Hard: EV/attack 18.8740578, DPS 6.2913526 at the captured 35,824 attack roll / 20,070 defence roll state.
- With Kandarin Hard: EV/attack 19.3918830, DPS 6.4639610 (Wiki displays 19.4 / 6.464).
- Added a permanent guard that Rune crossbow rejects Ruby dragon bolts (e), while Dragon crossbow accepts them.
- Fixed dragon gem-bolt tier recognition so names such as "Ruby dragon bolts (e)" are classified as dragon-tier bolts rather than ordinary ruby bolts.
- Added a permanent guard that full crystal armour applies its 30% accuracy / 15% damage bonus to Bowfa and Crystal bow, but not Rune crossbow.


## alpha50.4.12 — ranged optimiser performance pass

- Retires the temporary 12-candidate / 128-state diagnostic ranged beam used during the Vorkath Ruby-bolt investigation.
- Uses 6 supporting candidates per slot and a 48-state beam now that weapon+ammo anchoring prevents late-ammo pruning.
- Still evaluates every owned usable ranged weapon and every compatible owned ammo candidate before supporting-gear beam search.
- Keeps the alpha50.4.11 Wiki-validated Vorkath/Bowfa/Ruby-bolt mechanics regressions unchanged.
- Adds self-tests guarding both the reduced beam and weapon/ammo completeness architecture.


## alpha50.4.13 — optimiser stage profiler

- Adds DEV timing for each combat-style optimiser independently: Stab, Slash, Crush, Ranged and Magic.
- Adds a separate post-process timing covering best-style selection, bank recommendation preparation and selected-loadout assembly.
- Keeps alpha50.4.12's 6/48 ranged beam, weapon+ammo anchoring and all combat mechanics unchanged.
- Profiling is read-only/local to each background calculation; it does not alter candidate ordering, DPS, accuracy or winner selection.

## alpha50.4.15 — unchanged-input optimiser result cache
- Reuses completed Melee, Ranged and Magic optimisation reports when only the visible attack style changes and target/account/bank/Slayer inputs are unchanged.
- Cache key includes target, Slayer mode, real/boosted combat levels, combat-prayer/quest unlock flags, Kandarin Hard state, and bank item quantities.
- Bank refreshes, player-stat refreshes and full account refreshes invalidate the cache.
- DEV validation reports `Optimizer cache: HIT/MISS` alongside the existing per-style profiler.
- Combat formulae, 6/48 ranged beam, weapon/ammo anchoring and validated Vorkath results are unchanged.

## r0.9.0-alpha50.4.20 — Selected-style full optimisation
- Replaces five full combat-style optimisations on a cache miss with a cheap cross-style equipment-intelligence ranking pass.
- Runs the expensive Melee/Ranged/Magic optimiser only for the attack style explicitly selected in the sidebar.
- Completed per-style reports remain cached, so revisiting a previously calculated style remains instant.
- The Best style hint is now derived from the same cheap cross-style ranking used by the existing gear intelligence layer; it no longer blocks the selected loadout behind four unrelated full searches.
- Ranged 6/48 beam, weapon/ammo anchoring, dominance pruning, fast beam scorer, combat mechanics and final selected-style calculation are unchanged.


## r0.9.0-alpha50.4.21 — Enchanted-bolt PvM mechanics pass
- Adds Pearl Sea Curse EV: 6%/6.6%, accuracy-bypassing, floor(Ranged/20) extra or floor(Ranged/15) against fiery targets.
- Corrects Onyx Life Leech so it cannot proc against undead; living-target PvM rate remains 11%/12.1% and requires a successful hit.
- Retains exact Ruby, Diamond, Opal and Dragonstone PvM branches and Kandarin Hard scaling.
- Explicitly labels Jade/Topaz/Sapphire utility and Emerald poison without inventing direct DPS for stateful/non-damage effects.
- Adds enchanted-bolt regressions for diary scaling, fiery Pearl targets, undead Onyx immunity, dragonstone immunity and utility conservatism.
- Preserves alpha50.4.20 selected-style optimisation and performance architecture.


## r0.9.0-alpha50.4.28 — Melee Parity Audit
- Adds DEV melee calculation checkpoints: effective Attack/Strength, equipment bonuses, pre-weapon and final attack rolls, NPC defence roll, pre-weapon and final max hit, selected prayer, and attack speed.
- No production combat formula or optimizer behaviour changed in this diagnostic build.


## r0.9.0-alpha50.4.29 — KQ Variant + Melee Optimizer Diagnostic
- Preserves Wiki monster variants that share the same name/level/base stats instead of collapsing them. Kalphite Queen now exposes Crawling and Airborne explicitly in the target picker.
- Melee parity audit now prints target variant, NPC id, defence level/style defence, and selected body/legs offensive stats.
- No melee damage or optimizer-selection formula changed in this diagnostic build.


## r0.9.0-alpha50.4.30 — KQ Melee Candidate Audit
- Adds DEV-only melee optimizer candidate diagnostics without changing combat formulas or selection.
- Explicitly reports every Keris-family weapon that reaches the melee weapon pool, its item ID/category/style eligibility and exact best-loadout DPS.
- Reports top weapon candidates after exact target-DPS optimization.
- Reports BODY/LEGS intelligence ranks, including Bandos and Mixed hide pieces, and marks whether each is inside the four-choice beam shortlist.
- Intended to diagnose Keris partisan of the sun recognition and neutral-armour pruning on Kalphite Queen before any production mechanic is changed.


## r0.9.0-alpha50.4.31 — Keris EV Audit + Copy Diagnostics

- Adds a one-click **Copy diagnostics** button under Loadout Validation (DEV). It copies the target, visible loadout stats, and the complete scrollable DEV report to the system clipboard.
- The melee parity report now includes the complete selected equipment list with item IDs.
- Keris candidates now expose the full damage chain in the optimizer audit: pre-weapon max, Kalphite-modified normal max, 1/51 triple-hit proc max, successful-hit average, and EV per attack.
- Production melee scoring is intentionally unchanged in this build: alpha50.4.30 proved the Keris partisan of the sun is already detected, receives the 1.33 Kalphite damage stage, and is evaluated at 4 ticks. This build makes those stages directly auditable before any formula change.

## r0.9.0-alpha50.4.32 — Keris Variant Accuracy Audit
- Expanded every Keris optimizer diagnostic with effective Attack/Strength, aggregate Stab/Strength bonuses, pre-weapon attack roll, weapon accuracy multiplier, final attack roll, NPC defence roll, base max, Kalphite max, proc max, successful-hit average and EV/attack.
- Added regression locks that ordinary Keris and Keris partisan of the sun receive no general Kalphite accuracy multiplier.
- Added regression locks that Keris partisan of breaching receives x1.33 accuracy on Kalphite targets and no such bonus on non-Kalphites.
- No production combat formula or optimizer ranking changes in this audit build.


## r0.9.0-alpha50.4.34 — Per-Weapon Melee Stance Audit
- Keeps production melee scoring unchanged: each weapon candidate independently evaluates every legal stance and keeps its exact-DPS winner.
- Keris diagnostics now print Accurate and Aggressive results side-by-side (effective levels, roll, accuracy, base/max/proc, EV and DPS).
- Adds regression locks for the Keris partisan Stab interface and stance bonuses.
- This specifically distinguishes a true stance-scoring bug from comparing PBIS's best Keris stance with a Wiki calculator set to a different stance.


## r0.9.0-alpha50.4.38 — Fang integer damage-range parity
- Corrects Fang normal damage endpoints to `min=floor(trueMax*0.15)`, `max=trueMax-min`.
- Locks 46 -> 6..40 (average 23), 53 -> 7..46 (average 26.5), and canonical 60 -> 9..51 (average 30).
- Synthetic melee diagnostics now print Fang range and successful-hit average.
- Fang accuracy logic is unchanged.

## r0.9.0-alpha50.4.38 — Scythe Multi-Hit Synthetic Validation
- Adds charged Scythe of vitur (item 22325) to the DEV synthetic melee suite without granting ownership in production.
- Scythe target size is surfaced as 1x1 / 2x2 / 3x3+, with 1 / 2 / 3 independently rolled hits.
- Diagnostics expose component hit caps, combined maximum, successful-hit EV, accuracy, attack speed and DPS.
- Component caps are locked to base / floor(base/2) / floor(base/4), including Wiki examples 47 -> 47/23/11 (81 total) and 48 -> 48/24/12 (84 total).
- Each component uses the modern successful-zero conversion independently when computing expected damage.


### alpha50.4.38 — Synthetic Scythe injection fix
- Fixes the DEV synthetic melee weapon ID/name arrays so charged Scythe of vitur (22325) is actually injected.
- Two-handed synthetic weapons omit the fixed Dragon defender; diagnostics explicitly show `2H — shield omitted`.
- No production combat formula or ownership changes.

## r0.9.0-alpha50.4.39 — Soulreaper Axe Stack Validation

- Adds a DEV-only Soulreaper axe 0→5 stack parity harness using the established fixed melee gear.
- Uses the current 2026 Soulreaper axe data already bundled by PBIS: +134 Slash, +125 Strength, 5-tick speed, two-handed.
- Models auto-attack Soul stacks as +6 percentage points to Strength level per stack, additive with prayer, with the stack gained by an attack applying to the following attack.
- Reports effective Strength, max hit, successful-hit EV, per-attack EV and DPS at every stack count.
- Keeps production optimizer scoring unchanged until the stack rows are compared against the current Wiki calculator.
- Adds self-tests for stack clamping and effective-Strength rounding/order.


## r0.9.0-alpha50.4.40 — Soulreaper Axe Rounding Lock

- Corrects Soulreaper stack Strength ordering: prayer and the +6%-per-stack Soul contribution are floored independently before stance/+8.
- Fixes the Wiki-observed four-stack boundary from max 57 to max 56.
- Adds permanent Vorkath Wiki regression locks for the full max-hit sequence: 48 / 50 / 52 / 54 / 56 / 59.
- Leaves production optimizer scoring at the zero-stack baseline for now; stateful fight-length ramp scoring is intentionally a separate next step.

## r0.9.0-alpha50.4.41 — Soulreaper Production Ramp Scoring
- Integrates the Wiki-validated Soulreaper Axe 0→5 stack sequence into the production melee calculator and optimizer.
- Starts every optimization comparison at zero stacks: attacks 1–5 use stack states 0/1/2/3/4; later attacks use 5 stacks.
- Scores the ramp over the selected monster's HP using expected damage, with a fractional final expected attack to avoid artificial whole-hit ranking cliffs.
- Keeps Soulreaper accuracy constant across stacks; only Strength/max hit changes.
- Displays the five-stack potential max hit while DPS reflects the full zero-stack ramp.
- Adds regression locks for the Vorkath ramp score and short-target zero-stack behavior.

## r0.9.0-alpha50.4.49 — Obsidian Wiki lock + leafy target eligibility
- Wiki-locks full Obsidian armour + Berserker necklace + Toktz-xil-ak against post-quest Vorkath: attack roll 18,500, accuracy 46.09%, max 42, DPS 3.96273 (Wiki displays 3.963).
- Corrects Obsidian/Berserker-necklace expected damage: the combined damage modifier is applied to each raw damage roll with integer truncation, rather than rebuilding a uniform distribution from the modified max hit.
- Adds Kurask/Turoth `leafy` damage eligibility across combat styles: leaf-bladed melee weapons, broad arrows/bolts, and Magic Dart can damage them; unsupported attacks retain accuracy diagnostics but deal zero damage.
- Prevents the production optimizer from recommending otherwise-high-DPS melee weapons such as Noxious halberd against Kurasks/Turoths when those weapons cannot actually damage the target.
- Adds permanent self-tests for the exact Vorkath Obsidian checkpoint and leafy melee/ranged eligibility rules.

## alpha50.4.50.6
Wiki-source demonbane vulnerability scaling and Scorching bow demonbane support.

### alpha50.4.50.6
Introduces the Wiki-parity shared hit-distribution foundation. Existing Corp distribution helpers now use this common engine; additional weapon/target mechanics will migrate onto it incrementally behind regression tests.

### alpha50.4.50.9.4
Migrates Keris and enchanted-bolt expected damage to the shared Wiki-derived HitDistribution pipeline. This also adopts the Wiki source's exact transform ordering for accurate-zero, Corp, and Ruby bolts.

## r0.9.0-alpha50.4.50.21.1 — Magic accurate-zero self-test hotfix
- Corrects the expected value in the modern-Magic accurate-zero regression from `3.0` to `28/11` (`2.5454545...`).
- Production Magic distribution code is unchanged; the failing assertion was a test-fixture arithmetic error.

## r0.9.0-alpha50.4.50.22 — Wiki Magic accuracy ordering
- Ports the upstream Magic attack-roll ordering into `WikiMagicAccuracy` with integer truncation at every Wiki factor stage.
- Wires both standard and powered Magic production calculations through the shared pipeline.
- Covers prayer/effective level, Magic Void, Salve/Smoke additive accuracy, dragon-hunter weapons, Slayer imbue, demonbane/Mark of Darkness vulnerability, Tome of Water and elemental weakness.
- Corrects applicable Tome of Water accuracy from 10% to the upstream 20%; its damage bonus remains 10%.
- Leaves unavailable Forinthry Surge and manual-cast stance inputs disabled rather than assuming them.

## r0.9.0-alpha50.4.50.23 — Wiki Magic max/min-hit ordering
- Adds the shared `WikiMagicDamage` integer-stage pipeline and wires standard and powered Magic through it.
- Preserves upstream order for flat spell bonuses, equipment/prayer damage, Smoke/Twinflame, Salve/Slayer, dragon-hunter damage, elemental weakness, Sunfire minimum and Tome damage.
- Prevents legacy Magic loadout multipliers from double-counting effects already represented by the Wiki equipment/damage stages.
- Corrects Sanguinesti (`floor(Magic/3)`) and Warped sceptre (`floor((8*Magic+96)/37)`) scaling.
- Ensures Bone staff's built-in ratbane maximum is applied once rather than twice in production.
- Live Charge/Sunfire/Forinthry states and Mark of Darkness per-hit damage are intentionally left for explicit inputs and the next distribution slice.

## r0.9.0-alpha50.4.50.23.1 — self-test scope hotfix
- Fixes the Dragon hunter wand regression's target fixture scope so `compileTestJava` succeeds.
- No production mechanics changed.

## r0.9.0-alpha50.4.50.24 — sustained Magic per-hit distributions
- Migrates Mark of Darkness/Purging staff, Sanguinesti, full Ahrim, Twinflame and Brimstone ring into the shared attacker-distribution pipeline.
- Mark damage now uses two Wiki integer truncations on every damage roll and respects Duke/Yama/Ice Demon vulnerability.
- Twinflame is represented as a separate truncated hitsplat before NPC transforms instead of a final `×1.40` EV approximation.
- Sanguinesti's 20% `+8` branch and Ahrim's 25% `floor(hit×1.3)` branch now affect both DPS and displayed distribution maximums.
- Brimstone ring mixes ordinary accuracy with the 10%-reduced NPC defence roll at 75%/25%.
- The Magic optimizer seeds the complete owned Ahrim/Amulet of the damned family for exact-DPS comparison.
- Special attacks and encounter-state branches remain outside sustained auto-attack optimisation.


## alpha50.4.50.9.4 — Wiki NPC damage transforms
- Added shared post-attacker NPC hitsplat transforms from `PlayerVsNPCCalc.applyNpcTransforms`.
- Production Ranged and Magic now apply the shared target transform stage after attacker/bolt distributions.
- Added Zulrah capped reroll, Fragment of Seren limiter, Kraken ranged reduction, Tekton/CoX crystal/Olm reductions, Ice Demon non-fire reduction, Nightmare totem magic doubling, Slagilith reduction, and bundled Wiki `flat_armour` import/application.
- Melee migration remains intentionally incremental; complex melee distributions will move through this layer in the next slice.


## alpha50.4.50.10
Melee attacker distributions (standard, Fang, Keris, Scythe, Dual macuahuitl, standard independent two-hit, Soulreaper stack hits) now flow through Corp and the shared Wiki NPC post-hit transform stage before expected DPS is collapsed. This preserves per-hitsplat caps, armour and reductions.
