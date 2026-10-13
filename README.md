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
- **Prayer-aware equipment choices:** favours higher equipment Prayer bonus when
  combat results tie. Eligible owned items can fill unused slots when they preserve
  the calculated offensive result; weapon, shield and ammunition restrictions
  still apply. This is a preference within the search, rather than a guarantee
  of the highest possible Prayer bonus in every slot.
- **Target-specific combat calculations:** accounts for supported defence stats,
  elemental weaknesses, immunities, weapon passives and multi-hit mechanics.
  Examples include Fang, Scythe, demonbane and dragonbane weapons, and separate
  Hueycoatl Body, Head and Tail targets with per-hitsplat tail damage rules.
- **Enchanted bolts and ammunition:** models supported Ruby, Diamond, Onyx and
  Dragonstone bolt effects, including supported diary bonuses and target
  restrictions. Ruby bolt damage changes as enemy HP falls during TTK calculations;
  an option lets you exclude Ruby bolts from the search.
- **Spells, prayers and supplies:** selects supported spells and offensive prayers,
  and recommends owned food, combo food, dose-aware potions and target-specific
  supplies such as antifire, anti-venom, Slayer equipment and finishing tools.
- **Slayer task alternatives:** detects your assignment and offers supported
  monsters, combat variants and boss alternatives. Konar choices are filtered by
  assigned area, and Krystilia choices by confirmed Wilderness locations; manual
  target search remains available outside task mode.
- **Recommended-item bank view:** the bank button groups the selected loadout's
  equipment, ammunition and supplies so you can find and withdraw them using
  normal bank interactions. Toggle it off to restore the normal bank layout.
- **Optional GIM shared storage:** includes scanned shared gear and supplies in
  calculations. Both storage interfaces show recommendations and references to
  items held in the other store, with the shared snapshot time shown in the sidebar.
- **Persistent item exclusions:** right-click recommended items to exclude them,
  or edit the exclusions list in settings. Results stay visible while you make
  several exclusions, with a warning to regenerate before applying the changes.
- **Readable loadout summaries:** shows selected gear, combat statistics, spell
  or ammunition and prayer. The statistics panel expands to fit wrapped text,
  including longer spell names.

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

## Slayer tasks and locations

Enable **Load Slayer Task** to use your detected assignment. The dropdown lists
matching combat profiles and supported alternatives from the bundled catalogue.
For example, an unrestricted Greater demons task can offer Greater demons,
K'ril Tsutsaroth, Tstanon Karlak, Skotizo and Tormented demons.

- **Konar:** targets are filtered to the assigned area and its permitted species.
- **Krystilia:** targets are filtered to confirmed Wilderness variants.
- **Other masters:** task-family matching applies without an assigned-area filter.

The note below the picker shows the detected assignment location. If RuneLite
has not detected the task, check it with your Slayer gem, helmet or master.
Supported Slayer level requirements apply; quest access and special killing
requirements still matter.

Only confirmed targets appear for restricted assignments. Unknown variants stay
hidden with a note, and an undetected area keeps the picker empty. Named Konar
boss assignments without an area roll remain available. Task changes refresh the
choices and require regeneration. Disable task mode to return to manual search.

The current catalogue has confirmed targets for all **159 researched task/area
combinations**. The completed review resolves all **470 previously outstanding
location decisions**. These counts do not guarantee every possible game variant
is represented. Combat-data changes can require new verification. See the
[coverage report](SLAYER-LOCATION-COVERAGE.md) and
[Slayer data notes](SLAYER-DATA-NOTES.md).

## Item exclusions

Right-click a generated sidebar gear icon or a recommended item in the BiS bank
view and choose **Exclude from My BiS Finder**. Exclusions cover equipment,
ammunition, runes, food, potions and other supplies, including references to items
held in the other store. Ordinary bank items and **Other items** keep their usual menus.

Exclude several items before recalculating if needed. Existing results remain
visible, including when switching styles, and a warning asks you to regenerate.
The next generation applies the complete exclusion list.

Edit the comma-separated **Excluded items** field in RuneLite's plugin settings
to remove an exclusion, or clear the field to clear all exclusions. Regenerate
to restore eligibility. Names match exactly, ignoring case and surrounding
spaces. Potion doses and variants canonicalised by RuneLite are treated together.
Exclusions persist in the current RuneLite configuration profile.

Excluded supplies use supported owned alternatives where available. Excluded
runes block spells requiring them unless eligible equipment supplies those runes.
Powered weapons' stored charges are separate from loose rune ownership.

## Advanced options

Expand **Advanced** beneath the target to see available options. It starts
collapsed, and checked options appear green.

- **Include Ruby bolts (e)** appears when suitable bolts are owned in the checked
  ownership sources. Disable it to omit them from calculations.
- **Include group storage** appears on GIM accounts and is off by default.

Preferences persist across restarts. Changing an option requires regeneration;
storage updates do not automatically recalculate loadouts.

## Group Ironman shared storage

Open your personal bank, enable **Include group storage**, then open group
storage to capture its contents. The sidebar shows the shared snapshot's scan
time. Generate a loadout and select a combat style as usual.

The My BiS Finder button is available in both storage interfaces. Recommended
items in the open store retain normal withdrawal actions. Items held elsewhere
appear under **Stored in group storage** or **Stored in personal bank**. These
reference icons show where to find an item; open that store to withdraw it.
Recommended supplies can span both stores.

Shared gear, ammunition, runes, food and potions are included when enabled.
Deposits and withdrawals update the snapshot while storage is open. Changes
made by teammates may require reopening it. Scan both stores after logging in:
snapshots are session-only, while preferences and exclusions persist.

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
