# My BiS Finder

Account-aware best-in-slot gear planner for RuneLite using the player's bank,
levels, selected target and Slayer task.

## Features

- Finds the highest-DPS owned Melee, Ranged and Magic loadouts for a selected target.
- Uses the player's bank, equipped gear, inventory, combat levels, quest and
  prayer unlocks, and Slayer task.
- Supports target-specific equipment requirements, weapon passives, ammunition,
  elemental weaknesses and multi-hit weapons.
- Adds a native bank view for the selected loadout and recommended supplies.
- Uses an offline OSRS Wiki-derived equipment and monster data pack; it makes no
  live Wiki requests during play.

## Using the plugin

1. Open your bank so My BiS Finder can read the items owned by the logged-in account.
2. Select a target, or enable **Load Slayer Task**.
3. Click **Generate Loadout** to calculate and cache every attack style.
4. Choose an attack style to display its generated gear and bank layout; switching
   styles reuses the cached results without rerunning the optimisers.
5. Use the highlighted **BEST** style for the highest calculated sustained DPS.
6. Open the My BiS Finder bank view to gather the recommended gear and supplies.

## Privacy

All account, bank and loadout processing occurs locally in RuneLite. My BiS Finder
does not transmit account or bank data and does not make external network requests.

## Development

The project targets Java 11 and RuneLite `latest.release`. Run the plugin through
the Gradle `run` task and execute `mechanicsSelfTest` for the mechanics regression
suite before publishing a change.

## Development disclosure

My BiS Finder was developed with substantial assistance from AI coding tools.
Releases are reviewed, tested and submitted by the maintainer.

## Licence and data

Plugin source is provided under the BSD 2-Clause License. Details for the bundled
offline data pack are recorded in `WIKI-DATA-NOTES.txt` and its manifest.

## Version history

See the [full changelog](https://github.com/ZeroRuin/my-bis-finder/blob/main/CHANGELOG.md).
