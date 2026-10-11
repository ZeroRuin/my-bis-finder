# Slayer task and location data

## Completed user review — 11 October 2026

`reviewed-locations.json` imports every decision from the completed
`Slayer-Target-Review(1).xlsx`: 204 Konar decisions (26 Yes, 178 No) and
266 Krystilia decisions (22 Yes, 244 No). The user confirms that Krystilia No
means the exact creature/profile has no Wilderness spawn. Decisions are keyed
by task, assigned area, representative picker ID, name and level. They override
earlier unresolved evidence only in that scope; unrestricted tasks are unchanged.
Notes and supplied evidence are retained. These are user-confirmed findings,
not an independent verification of each Wiki page. The workbook and combat-pack
SHA-256 hashes preserve provenance. A changed combat snapshot disables the
reviewed profile mappings until they are checked again.

Reproduce with `python scripts/import_slayer_review.py --workbook FILE.xlsx
--audit AUDIT.json`, using the original test4 unresolved audit; the importer
rejects missing, duplicate, changed and unfinished decision rows.

Task-family aliases are bundled from RuneLite's Slayer plugin at commit
`42a6f17a6a2e8e478aa763890ecd0181a59dad38` (152 definitions). The source URL,
commit and blob SHA are recorded in `tasks.json`, with RuneLite's redistribution
notice in `RUNELITE-LICENSE.txt`. The enum is package-private; the plugin uses no
reflection or imports of internal RuneLite classes. There are no runtime network requests.

## Restricted research scope

`restricted-task-scope.json` covers Konar and Krystilia only. It includes optional
unlock families, current combined metal-dragon tasks and legacy individual
metal-dragon names. It is a research scope, not an assignment-probability or
unlock-requirement database. Konar's task/area pairs are recorded separately;
Krystilia's assignments use the Wilderness. Other masters do not use this registry.

Master values are 7 for Krystilia and 8 for Konar. The location text comes from
RuneLite's public `SlayerPluginService.getTaskLocation()`. An unresolved master
or Konar area stays closed. Named boss assignments with no area roll are handled
separately; an actual area restriction always takes precedence over the boss name.
Completed tasks clear the task-mode picker. Task/area changes require regeneration.

## Positive location evidence

`restricted-location-matches.json` contains 281 researched name/level/location
facts, restricted to the above families and assignment areas. Spawn research
uses [Mejrs' NPC dataset](https://github.com/mejrs/data_osrs) at commit
`6a3ca6f19d65c5609434b51cac8dee9d4af97c02` (2 July 2026). The input Git blob,
SHA-256, combat-pack hash and scope hash are recorded. No upstream code, NPC
configurations, model/animation data, maps or full spawn/coordinate arrays are
redistributed in the plugin. The compiler is our own code and emits scoped factual
associations with attribution and evidence NPC IDs.

The compiler uses explicitly named interior footprints, never proximity to an
entrance. Surface/dungeon areas remain separate. Wilderness includes its relevant
underground areas and excludes the Ferox safe area. These research footprints are
not a player-position check or a definition of the game's full assignment boundary.
Missing spawns, dynamic/transformed NPCs, newer areas and undocumented footprints
remain gaps. A literal area-labelled Wiki combat version can provide another
positive fact. Arbitrary version strings are not parsed as locations.

Equal names/levels do not establish equal combat stats. For ambiguous combat
groups, exact NPC-ID evidence is required. Otherwise equivalent model/ID variants
can share a mapping. Each compiled row lists the permitted combat-catalogue IDs.
This distinguishes the two level-74 Dagannoth records at the Lighthouse/Catacombs.
Identical combat entries retain their original records after display merging, so
any confirmed original can establish eligibility for that shared combat setup.

`locations.json` records reviewed exceptions and explicit IDs, including bosses,
superiors, newer Nagua/Venator/Strykewyrm families, transformed Wyrmlings, and
variant-specific alternatives. Source pages are retained in that file. Reference
facts from [Slayer Simplified](https://github.com/Wulfic/slayer-simplified) were
checked at commit `20e0aa9902f1a3eb1f061ed409646418b1a59e7c`; its BSD 2-Clause
notice is retained in `SLAYER-SIMPLIFIED-LICENSE.txt`. Its broad single-level and
combined-level lists are **not** imported wholesale or treated as proof: they can
mix variants across locations. In particular, level-109 Fire giants do not belong
in Brimhaven, and level-93 Dust devils do not belong in the Catacombs.

The source Wiki combat pack itself contains only representative NPC IDs and some
location-labelled versions, not a complete spawn-location database. Sources are
snapshots, not guarantees of complete/current membership. Missing evidence never
becomes an eligible target. Inspect both provenance and the coverage report when
updating the pack; do not relabel an unresolved candidate as confirmed.

## Selection policy and coverage

Location checks remain eligible/ineligible/unknown internally. In restricted task
mode only ELIGIBLE targets enter the picker. Unknowns stay hidden with an incomplete
coverage note. New/unrecognised task areas and unresolved area names offer no
fallback targets. There is no editable-field bypass while Load Slayer Task is enabled.
Slayer-level requirements still apply. Quest/dungeon access, player position,
special killing methods and superior-spawn availability are not enforced by this
location registry.

The development audit currently populates **159/159 researched task/area pairs**
at Slayer level 99. This means at least one confirmed target per pair, **not** all
variants verified. [SLAYER-LOCATION-COVERAGE.md](SLAYER-LOCATION-COVERAGE.md) lists
actual choices and hidden unverified options for each pair. Future assignments can
extend this research scope and must be reviewed. Unrestricted masters retain the
RuneLite family baseline; it is not an exhaustive Wiki alternative database.
Earthen Nagua is a reviewed supplementary alias. Magic axes/Zombies have explicit
singular handling. Reanimated, Scar and quest-only fixtures are excluded.

## Updating and verification

Refresh task families with:

```
python scripts/update_slayer_tasks.py --commit <full RuneLite commit SHA>
```

The spawn compiler accepts a local research snapshot matching its reviewed pinned
Git blob. It will reject a file with a mismatched blob/commit. To regenerate:

```
python scripts/compile_restricted_locations.py --spawns /path/to/NPCList_OSRS.json --commit 6a3ca6f19d65c5609434b51cac8dee9d4af97c02
```

A newer source requires deliberate review of the source pin, footprints, task/area
scope, representative IDs and exceptions. Review the generated diff. Run:

```
python -m unittest discover -s scripts -p 'test_*.py'
```

Then compile and run `slayerTaskSelfTest`, `mechanicsSelfTest`,
`gimStorageSelfTest`, `itemExclusionSelfTest`, `loadoutStatsSizingSelfTest`, and
`slayerLocationCoverage`. The last task rewrites the coverage report using the real
selector. No simulation controls are present in the client. Weekly combat-data
updates do not refresh task/location research automatically; re-audit coverage
and ambiguous stat groups when that data changes.

## Historical research — test build 3

The counts below describe the earlier research pass, before the completed user
review documented above. They are retained for provenance, not current gaps.

The complete picker audit found 423 distinct representative IDs with at least one
unresolved task/area decision (477 task/variant rows, 917 area decisions).
The reviewed changes resolve all remaining area decisions for 35 of those IDs.
388 IDs remain unresolved in at least one area (433 task/variant rows, 789
area decisions). This is not a complete location database. The checklist names
every remaining ID, its unresolved areas and already-confirmed areas.

Exact spawn ID plus equal combat level now bridges Wiki-disambiguated titles
such as Dagannoth (Waterbirth Island) and Zombie (Wilderness), with no general
suffix stripping. Reviewed name entries cover the three Waterbirth Dagannoth
Kings, Night beast and the Wilderness Revenant maledictus. Nightmare Zone and
Construction variants are explicitly outside all supported assigned areas;
empty location lists express that reviewed decision. These exclusions apply
only when a location restriction is active. Provenance is in auditReviews in
locations.json. Broad name aliases may also capture unrelated species; the
checklist flags these for task-credit review rather than enabling them.

Direct Wiki page requests were blocked by robots rules during this review;
indexed primary Wiki tables and the pinned exact-ID spawn source were used.
Unretrieved pages and missing spawn evidence are not treated as proof of absence.

## Historical research — test build 4

The remaining candidates and empty selectors described below were resolved by
the completed user review. Current coverage is in the coverage report.

The user supplied the Wiki Konar table, including explicit alternatives per area.
`konar-target-rules.json` transcribes those memberships and supplements reviewed
superiors. A task-aware gate excludes unrelated broad alias matches, Nightmare
Zone and non-listed alternatives. Construction variants are now excluded from
all Slayer matching because they do not count. Standalone Venators and legacy
individual metal-dragon research names remain supported separately.

`compile_konar_targets.py` resolves an explicitly listed species/area only when
its non-fixture catalogue records have one positive combat level and one complete
combat signature. Same-level Blue dragon profiles differ and remain unresolved.
Interchangeable model IDs may share one combat setup; these automatic IDs are not
a physical-spawn claim for each model. Generic Troll/Dagannoth/Lizardman families
do not infer species distribution from a family location alone. Their specific
levels and profiles still need exact spawn/manual evidence. Post-quest bosses
remain valid; quest, idle and whirlpool fixtures do not become automatic targets.

133 of 281 species/area rules meet this uniqueness test, including already
confirmed mappings. Bloodveld superiors exclude God Wars Dungeon; mutated
Bloodveld superiors follow only the mutated species locations. The rules add
Amoxliatl for Lesser Nagua, Venator for Vampyres at Vampyrium, Dagannoth spawn
at Waterbirth, and Wyrmling at Wyrmscraig Cavern.

The reduced checklist has 64 distinct Konar entries (70 task/variant rows,
204 remaining area decisions). It separately lists 256 Krystilia candidates
(266 rows); no Konar inference is applied to Wilderness eligibility. Konar has
three entirely unresolved areas: Lizardmen at Kebos Swamp, and Trolls at
Fremennik Isles/Wyrmscraig Cavern. Those selectors stay empty pending review.
A populated area is not evidence that every variant is covered.

After any monster-pack change, regenerate with:

```
python scripts/compile_konar_targets.py
```

A runtime SHA-256 check disables automatic conclusions if the combat pack
differs from the one analysed. The explicit membership gate remains active.
Run the self-tests and audit again before publishing any regenerated mapping.

Baby black dragons also have different level-83 Normal/Myths combat profiles.
Their old broad name mapping was removed; exact spawn/version evidence is kept,
and the remaining profile/area combinations now appear in the checklist.
