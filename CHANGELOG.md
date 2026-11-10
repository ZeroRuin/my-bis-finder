# Changelog

## 0.9.7.4 — Barrows Charge States, Melee Search Fix and Search Clean-up

- Barrows pieces at 0 charge are never recommended; they have no stats or set effect until repaired.
- Barrows pieces at 100, 75, 50 and 25 charge keep their set effects in every style. Karil's, Ahrim's and Verac's sets previously only counted when undamaged.
- Ahrim's staff at any working charge can autocast its spells.
- Fix melee recommendations missing a faster setup when many owned items had no effect on damage: equal lines no longer crowd better ones out of the search.
- Fix magic recommendations leaving out spells tied to one weapon: a Slayer's staff (e) with Magic Dart on task, and a Void knight mace with Claws of Guthix, are now considered.
- Empty slots are filled from every owned item in all styles; melee previously checked only the first eight.
- Item names match the same way in every search and effect, and no longer depend on the computer's language setting.
- The melee, ranged and magic searches share one copy of their common steps instead of three.
- Move the sidebar's results cache, stats text and target list rendering out of the main sidebar class.
- Replace tests that looked for exact lines of source code with tests of what the searches recommend.
- Add a test that compares all three searches with an exhaustive search on small fixed banks, and a tool that compares recommendations before and after a change.

## 0.9.7.3 — Sidebar Code Clean-up

- Move the sidebar's colours and styling into one shared class; the sidebar looks the same.
- Share the per-style optimiser caching instead of keeping two copies; results are unchanged.
- Remove raid number-box styling that was always overwritten.
- Build and test with Java 11 in CI, matching the Plugin Hub.

## 0.9.7.2 — Tormented Demon Variants, Scurrius Rats and Code Clean-up

- Add a Tormented Demon variant dropdown: Shielded, Shielded (Defenceless) and Unshielded, matching the Wiki DPS calculator.
  - Shielded variants take 20% less damage unless using demonbane or abyssal weapons.
  - Defenceless and Unshielded are always hit.
  - Unshielded takes attack speed² − 16 bonus damage from spells, crossbows, chinchompas and crush attacks, which also attack one tick faster.
- Giant rat (Scurrius) is always hit and dies to one hit; rat bone weapons still attack it every tick.
- Remove 12 unused classes, 32 unused or test-only methods, a duplicate Slayer flag, the unused Show sidebar setting and unused imports; recommendations are unchanged.
- Magic accuracy uses one shared roll-ordering step instead of two copies of the same logic.
- Replace deprecated RuneLite APIs (sprite IDs, inventory IDs, account type and menu entry creation) with their current versions; Unranked Group Ironman accounts can now include group storage.
- Tests check the code the plugin runs, including magic max-hit and accuracy boundaries, instead of unused copies.
- Run all 31 self-tests with `gradle test`, and run them with the Python checks on every push and pull request.
- List all 31 self-tests in the testing guide.
- Add an optional `-PruneLiteVersion` Gradle property for local builds; Plugin Hub builds still use the latest release.

## 0.9.7.1 — Salve and Slayer Helm Fixes, Bork Phases

- Sync bundled equipment and monster records with the supplied OSRS DPS Calculator snapshot.
- Offer one Bork target with a phase dropdown; stunned Bork reduces melee defence bonuses by 100 and rolls normal accuracy.
- Validate Slayer mappings when importing new monster data.
- Fix melee recommendations on undead Slayer tasks: the search also tries setups without a Slayer helm or black mask, so a faster Salve amulet setup is no longer missed and on-task results are never slower than off-task ones.
- Fix magic recommendations missing Salve amulets against undead and imbued Slayer helms on task: the gear search keeps setups with these effects until their real damage is calculated.

## 0.9.7 — Advanced Options, Bank Layout and The Fractured Archive

- Add The Fractured Archive target filters, provisional party HP scaling, phase profiles and defence-reduction floors; add supported raid equipment, Zeal and elemental fragments.
- Preserve rapid bank withdrawals without resetting generated loadouts or losing uncharged Ring of shadows entries.

- Add Vardorvis HP-dependent Defence and saved Starting HP controls; account for falling Defence throughout estimated TTK.
- Add Prayer or Defence fill under Advanced; Defence fill preserves accuracy, max hit, attack speed, DPS and TTK.
- Add optional owned-item defence reductions, with saved amounts, individual hide settings and a reset button. Effects are manually pre-applied and held fixed for the estimate.
- Add compatible ammo dropdowns for each bank-scanned blowpipe type; loaded contents and quantities remain unknown.
- Improve complete normal, mixed and elite Void loadout searches and ornamented set recognition.
- Match supported weapon attack labels to the game and correct powered-staff Accurate stance accuracy.
- Correct melee Salve damage rounding, Twisted bow scaling and fallback Dragon hunter crossbow ammo recognition.
- Keep special recommendations separate from ordinary loadout results and clarify charged item variants in tooltips.
- Tidy sidebar inputs, move modelling notes into tooltips and highlight available Advanced options with a red exclamation mark.
- Add a Defence reducing items bank section below equipment, including required Vulnerability runes; show each stack once.
- Add Hide Other items for the planner bank view, enabled by default.
- Reduce TTK calculation overhead and memory pressure, batch item ranking and add calculation cancellation.
- Correct Tome of Water wording to +20% accuracy and +10% damage.
- Remove temporary synthetic-bank testing controls; planning uses actual owned items.

## 0.9.6.3 — Combat, Search and Special Attacks

- Replace reflective spell-icon lookup with explicit RuneLite sprite constants for Plugin Hub compatibility.
- Add a fourth equipment row with a separate owned special-weapon recommendation, the selected magic spell and a double-width game-style attack button.
- Rank supported direct-damage specials using expected damage per attack interval in the displayed armour; show energy cost and required ammo in the tooltip. This is burst DPS, not an energy-regeneration or rotation simulation.
- Add supported melee specials, common ranged specials including Zaryte enchanted bolts, and Eye of Ayak / Nightmare staff specials. Unmodelled specials are explicitly excluded from the recommendation.
- Include the recommended special weapon and ammo in the selected bank setup.
- Remove Blood Moon full-set speed calculations and complete-set search candidates. Keep individual Moons equipment stats and ordinary weapon attacks; full-set effects remain out of scope.
- Keep thralls, defence-drain value and special rotations out of scope.

- Expand raid filters with tombs/amascut, chambers/xeric and theatre/blood, including full raid names and a following boss query.
- Persist all raid numbers and checkboxes in the active RuneLite configuration profile across logout and restarts; reload them when switching profiles.

- Add locked-quiver charge-state aliases and ordinary ancient cape aliases.
- Give generated Vasilias forms their real combat NPC IDs without changing the bundled monster snapshot.
- Add Warden P2 guaranteed hits and accuracy-based damage bounds, including its Twisted bow accuracy rule.
- Add guaranteed melee maximum damage against the exposed Warden core; timed exposure remains out of scope.
- Add Guardian Mining/pickaxe damage and average-party-Mining HP controls.
- Add Dharok's HP-snapshot damage and complete-set optimizer candidates.
- Correct Atlatl/Hunter's spear Strength-based damage and preserve melee-Strength supporting gear during search. Eclipse burns remain excluded.
- Add typo-tolerant target suggestions, common shorthand and ToA/CoX/ToB raid-prefix filtering.
- Keep special rotations and damage over time as separate future work; thralls and defence drains are out of scope.

## 0.9.6.2 — Raid Targets and Options

- Rerolls only Seren hits above 24 to 22–24; preserves hits at or below the cap.
- Preserves Entry Mode, Normal Mode and Hard Mode target labels for all six Theatre bosses, including equal-stat variants.
- Retains all nine Verzik phase targets and adds phase-one per-hitsplat damage limits.
- Adds selectable style-locked Nylocas Vasilias forms and labels Hard Mode Prinkipas forms.
- Adds catalogue, damage-boundary, multi-hit, DPS and TTK regression checks.
- Adds conditional CoX party HP/combat/size and Challenge Mode controls.
- Matches raid input borders to the sidebar gold palette.
- Skips searches for target-wide immune combat styles; muted style buttons remain clickable to show a target-specific explanation.
- Adds ToA raid level, path level, party size and Smelling salts exclusion controls.
- Applies raid scaling to generated targets and applicable defence rolls; includes raid context in result caching.
- Preserves CoX/ToA phase labels and adds vulnerable-style choices for Akkha and active Wardens.
- Uses the ToA-specific Fang accuracy formula and supported owned boosts when salts are excluded.

## 0.9.6.1 — Hueycoatl and Code Cleanup

- Separates Hueycoatl Body (phase 1), Head and Tail targets; hides non-attackable states.
- Models the tail damage transform per hitsplat, including multi-hit attacks and expected time to kill.
- Requires a Crush attack style and Crush bonus strictly higher than Stab, Slash, Ranged and Magic for the tail’s higher damage limit and minimum-hit benefit.
- Keeps Earth spells on the standard tail damage limit.
- Removes unused diagnostics, comparison reports and profiling bookkeeping while preserving optimizer search caches.
- Expands README feature descriptions and explains Prayer-bonus preferences.
- Removes the historical local cleanup report and adds permanent Hueycoatl regression coverage.

## 0.9.6 — Slayer Task Alternatives

- Expands current Slayer assignments into selectable combat variants, monsters and boss alternatives.
- Filters Konar choices by the assigned area and Krystilia choices by confirmed Wilderness spawns.
- Uses explicit Konar task/species/area rules, supported superiors and 470 completed profile/location review decisions.
- Provides confirmed targets for all 159 researched task/area combinations in the current catalogue.
- Hides unverified variants and waits for unresolved assignment locations instead of offering unrestricted choices.
- Refreshes target choices when tasks or assigned locations change; preserves named Konar boss assignments without an area roll.
- Keeps manual target search available when Slayer task mode is disabled.
- Preserves location evidence for merged combat profiles and disables snapshot-specific conclusions after combat-data changes.
- Adds Slayer regression checks, coverage audits and maintained data-import tools.
- Rewrites the README and consolidates local testing instructions; removes obsolete checklists and ignores generated caches.

## 0.9.5 — Item Exclusions

- Automatically expands Loadout stats to fit wrapped spell names and all stat lines.
- Right-click generated sidebar gear or recommended bank items to exclude them.
- Applies exclusions to equipment, ammunition, runes, food, potions and other recommended supplies.
- Keeps generated results and bank layouts available while multiple exclusions are added.
- Shows a persistent regeneration warning until the current list is applied.
- Adds an editable Excluded items settings list; erase entries or empty the box to restore items.
- Handles potion doses and RuneLite canonical variants together.
- Adds regression checks for supply fallback, rune eligibility and bank menu scope.

## 0.9.4 — Group Ironman Storage

- Adds optional shared-storage gear and supply calculations for GIM accounts.
- Captures shared snapshots on storage updates and displays the last scan time.
- Adds BiS views to both storage interfaces, with references to items in the other store.
- Adds a collapsed Advanced section and saves checkbox preferences.
- Improves sidebar font sizes, wrapping, spacing and scrolling.
- Removes displayed calculation timings and in-game simulation controls.


## 0.9.3 — Time-to-Kill Update

### New
- Optimises final Melee, Ranged and Magic loadouts for the lowest expected
  time to kill (TTK), rather than ranking final choices by sustained DPS alone.
- Displays estimated TTK alongside sustained DPS in loadout summaries.
- Calculates TTK from attack damage distributions, accounting for misses,
  overkill, multi-hit damage and supported target damage transformations.
- Models Ruby bolts (e) Blood Forfeit damage as enemy HP decreases; offers
  an optional checkbox to exclude Ruby bolts (e) from calculations.

### Improved
- Speeds up exact TTK calculations by caching reusable damage distributions
  and reusing Ruby bolt damage calculations across HP buckets.
- Shows clearer first-use instructions with the bank button and loadout steps.
- Detects Ruby bolts (e) in the bank, inventory and equipped ammunition.
- Simplifies the Ruby bolts (e) checkbox explanation and presentation.
- Shows generation timing information to assist with performance diagnostics.
- Retains sustained DPS for tie-breaking and candidate pruning.


## 0.9.3-test

- Adds distribution-based expected time-to-kill for finite monster HP.
- Uses expected TTK for exact finalist, weapon, stance and attack-style ranking
  while retaining DPS for the broad performance-sensitive search stages.
- Models Ruby bolt Blood Forfeit from the target's remaining HP during TTK.
- Displays estimated TTK alongside sustained DPS in the loadout summary.
- Caches equivalent TTK calculations and uses an exact linear-distribution fast
  path without reducing the optimizer's equipment coverage.
- Reuses identical Ruby-bolt distributions across five-HP ranges and above its
  100-damage cap, preserving exact Blood Forfeit results with less rebuilding.
- Builds each Ruby-bolt candidate's ordinary crossbow distribution once before
  applying its exact remaining-HP proc states.
- Replaces the initial hint with a concise target, generation and bank-withdrawal
  guide using the actual My BiS bank icon.
- Shows temporary end-to-end and per-style generation timing in the sidebar for test builds.
- Adds an **Include Ruby bolts (e)** option only when they are present in the bank,
  allowing faster generation when exact Blood Forfeit comparisons are unnecessary.

## 0.9.2

- Keeps generated loadouts and all cached attack styles intact when the bank is
  opened, closed or changed; bank events update ownership for the next run.
- Keeps the action visible as **Regenerate Loadout** after a completed run, with
  cache invalidation occurring only when the player deliberately regenerates.

## 0.9.1

- Moves the detailed release history out of the Plugin Hub description and into
  this dedicated changelog.
- Documents bundled Wiki data, local calculations, and release testing and review.
- Publishes the 13 September 2026 date of the bundled equipment and monster data
  snapshot.

## Earlier development builds

Pre-release development builds (r0.4.x to the r0.9.0 alphas) are recorded in
[CHANGELOG-ARCHIVE.md](CHANGELOG-ARCHIVE.md).
