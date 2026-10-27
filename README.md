# My BiS Finder

My BiS Finder looks at your banked, equipped and inventory gear to recommend
loadouts for your chosen target. It compares Melee, Ranged
and Magic using your combat levels, available equipment and supported target
mechanics, then highlights the lowest estimated time to kill.

Calculations run locally in RuneLite using bundled OSRS Wiki-derived data.
You do not need to enter your bank into a separate calculator.

## Features

- **Loadouts from gear you own:** considers your scanned bank, equipped items
  and inventory, together with supported level and unlock requirements. Selects
  compatible ammunition and spells using available runes or equipment rune sources.
- **Five combat styles:** compares Slash, Stab, Crush, Ranged and Magic, then
  marks the style with the lowest estimated time to kill as **BEST**. Generated
  results are cached so you can switch styles without recalculating.
- **Time to kill and sustained DPS:** estimates finite-fight kill time from hit
  distributions, including misses, multi-hit attacks and overkill. Sustained DPS
  is also displayed and helps distinguish otherwise equal results.
- **Prayer or Defence fill option:** choose under **Advanced**, with Prayer fill
  as the default. Defence fill favours defensive gear while preserving calculated
  accuracy, max hit, attack speed, DPS and TTK.
- **Target-specific combat calculations:** accounts for supported defence stats,
  elemental weaknesses, immunities, weapon passives and multi-hit mechanics;
  also recommends the best supported special-attack weapon from your owned gear.
- **Vardorvis HP scaling:** set starting HP for supported variants and account
  for falling Defence throughout estimated TTK.
- **Optional defence reductions:** enter supported owned-item reductions under
  **Advanced**. Effects are assumed successful and pre-applied for the estimate;
  individual controls can be hidden or reset.
- **Blowpipe ammo selection:** choose compatible darts separately for each
  bank-scanned blowpipe type; internal contents and quantities remain unknown.
- **Void and attack styles:** improved normal and elite Void loadout searches,
  with supported weapon attack labels matching the game.
- **Enchanted bolts and ammunition:** models supported Ruby, Diamond, Onyx and
  Dragonstone bolt effects, including supported diary bonuses and target
  restrictions. Ruby bolt damage changes as enemy HP falls during TTK calculations;
  an option lets you exclude Ruby bolts from the search.
- **Spells, prayers and supplies:** selects supported spells and offensive prayers,
  and recommends owned food, combo food, dose-aware potions and target-specific
  supplies such as antifire, anti-venom, Slayer equipment and finishing tools.
- **Slayer task alternatives:** enable **Load Slayer Task** to use your detected
  assignment and choose from supported monsters, combat variants and boss
  alternatives. Konar choices respect the assigned area, while Krystilia choices
  use confirmed Wilderness locations. If a task has not been detected, check it
  with your Slayer gem, helmet or master; disable task mode to search manually.
- **Recommended-item bank view:** the bank button groups the selected loadout's
  equipment, ammunition and supplies so you can find and withdraw them using
  normal bank interactions. Selected defence-reduction items and required runes
  appear below equipment. **Other items** defaults hidden and can be shown in
  settings. Toggle the bank view off to restore the normal layout.
- **Optional GIM shared storage:** enable **Include group storage** under
  **Advanced**, then scan your personal bank and shared storage to include their
  gear and supplies. Both storage interfaces show recommendations and references
  to items held in the other store. The sidebar shows the shared scan time;
  scan both stores again after logging in.
- **Persistent item exclusions:** exclude equipment, ammunition, runes, food,
  potions and other recommended supplies from the sidebar or BiS bank view.
  Make several exclusions while keeping the current results visible, then
  regenerate to apply them. The editable list persists in your RuneLite profile.
- **Readable loadout summaries:** shows selected gear, combat statistics, spell
  or ammunition and prayer. The statistics panel expands to fit wrapped text,
  including longer spell names. Modelling notes are in tooltips; a red indicator
  beside **Advanced** highlights available ammo or reduction controls.
- **Calculation controls:** reduced calculation overhead and memory pressure,
  with an option to cancel a calculation.

## Getting started

Install **My BiS Finder** through RuneLite's Plugin Hub and open its sidebar.
This repository may include changes awaiting Plugin Hub review.

1. **Open your bank** to capture owned items. Equipped gear and inventory items
   are also considered.
2. **Choose a target**, or enable **Load Slayer Task** and select an alternative
   from your detected assignment.
3. Click **Generate Loadout** to calculate the supported combat styles.
4. Select **Slash**, **Stab**, **Crush**, **Ranged** or **Magic** to see its gear,
   estimated time to kill, sustained DPS, ammunition or spell, and prayer.
5. Open your bank and click the **My BiS Finder button** to find recommended
   equipment and supplies. Click it again to restore the normal bank layout.

![My BiS Finder button in the bank interface](docs/bank-ui-button.png)

The button appears in the upper-right corner of the bank interface. Gear icons
and validation appear once a target, bank scan and generated style are available.
Regenerate after changes to the target, assignment, ownership or calculation
options. Switching generated styles reuses the current results.

## How to exclude items

1. **Generate a loadout** and select the combat style you want to view.
2. **Right-click an item** in the sidebar gear grid or the recommended section
   of the BiS bank view.
3. Select **Exclude from My BiS Finder**.
4. Repeat for any other items you want to exclude. Your current results stay
   visible, and a warning reminds you that regeneration is needed.
5. Click **Regenerate Loadout** to calculate a new setup using the exclusions.
   Supported owned alternatives are used where available.

Exclusions also apply to recommended ammunition, runes, food, potions and other
supplies, including references to items held in the other store. Ordinary bank
items and **Other items** keep their usual right-click menus.

### Restoring excluded items

1. Open **My BiS Finder's settings** in RuneLite.
2. Find the comma-separated **Excluded items** list and remove the name you
   want to restore. Clear the field to remove every exclusion.
3. **Regenerate the loadout** to include those items again.

You can also add names directly to this list. Names match exactly, ignoring
capitalisation and surrounding spaces. Potion doses and variants canonicalised
by RuneLite are treated together.

Excluded runes prevent spells that require them unless eligible equipment
provides those runes. Powered weapons' stored charges are separate from loose
rune ownership.

Search **`tfa`** (or **`tfa ocellus`**) to filter The Fractured Archive targets.
Supported bosses have a saved 2–8 player party selector. HP scaling is provisional;
phase estimates exclude movement, team DPS and encounter timing. Raid equipment,
Zeal and unlocked elemental fragments are included where their effects are modelled.
See [raid modelling notes](docs/RAID-OPTIONS.md) for the remaining limitations.

## Advanced options

Expand **Advanced** beneath the target to see available options. It starts
collapsed. A red **!** highlights available blowpipe ammo or defence reduction
controls; hover it for details.

- **Prayer or Defence fill:** Prayer fill is the default. Defence fill favours
  defensive gear while preserving calculated accuracy, max hit, attack speed,
  DPS and TTK. It compares equipment bonuses, not incoming damage or survival.
- **Include Ruby bolts (e):** appears when suitable bolts are owned. Disable it
  to leave Ruby bolts out of the search.
- **Blowpipe ammo:** each supported blowpipe type scanned in your personal bank
  gets its own compatible-dart dropdown. Select the darts you intend to use;
  an unselected blowpipe is excluded. Internal contents and quantities are not
  detected, and loose dart ownership does not determine the selection.
- **Defensive Reductions:** owned supported weapons or supplies reveal manual
  hit-count, damage or spell controls. All default off. Selected effects are
  assumed successful and pre-applied, then held fixed for the estimate; setup
  time, special damage, energy and recovery are excluded. **Reset reductions**
  clears them, and collapsed Advanced shows how many types are selected.
- **Include group storage:** appears on GIM accounts and is off by default.
  Scan both stores to include supported shared gear and supplies.

In RuneLite's plugin settings, individual reduction controls can be hidden;
hiding one also clears its selected value. **Hide Other items** is enabled by
default for the planner bank view and can be disabled to show unrelated items.
Selected reduction weapons and required runes appear below equipment in that view.

Preferences persist in the active RuneLite configuration profile. Regenerate
after changing calculation options; storage updates do not automatically
recalculate loadouts. Modelling details are available in control and stats tooltips.

## Understanding the results

Time to kill and DPS are estimates for the supported combat model. The optimizer
compares sustained attacks; it does not plan a full encounter strategy or
special-attack rotation. Movement, downtime and mechanics outside the model can
affect actual results.

Recommendations depend on captured ownership data and supported account unlocks.
Reopen storage and regenerate when something changes. New items, monsters or
mechanics may need a data or code update.

## Privacy and data updates

Account, bank and loadout processing stays on your computer. The plugin does
not transmit account or bank data or make external network requests.

Combat data ships with the plugin. Repository automation checks published
upstream data and proposes reviewed updates. Installed plugins receive changes
through releases, rather than live Wiki downloads. See the
[update documentation](scripts/WIKI-UPDATES.md).

## Development and testing

The project compiles for Java 11 and uses RuneLite `latest.release`. Open the
source folder in IntelliJ, reload Gradle and run the Gradle **run** task for the
development client. Disable the installed Plugin Hub copy while testing it.

The [testing guide](docs/TESTING.md) covers automated verification and in-game
checks. Data provenance and regeneration instructions are recorded in the
Slayer data notes and scripts documentation. Historical combat-math work remains
in the [Wiki DPS parity notes](WIKI_DPS_PARITY.md).

## Licence

Plugin source is provided under the [BSD 2-Clause License](LICENSE). Bundled
data provenance is recorded in [WIKI-DATA-NOTES.txt](WIKI-DATA-NOTES.txt), the
data manifest and the Slayer data notes. Third-party notices remain alongside
the relevant resources.

See the [changelog](CHANGELOG.md) for version history.
