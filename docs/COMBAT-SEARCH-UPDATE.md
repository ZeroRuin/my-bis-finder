# Combat and search update — test 1

Base: 314821cb79a646057e0f44e8b4756d43f92d8c28. Version and approved README unchanged.

## Primary references

- OSRS Wiki DPS calculator, commit `89c3e25b344aea90d0189746e4b5f73dde0f0383`:
  `src/lib/PlayerVsNPCCalc.ts` (Warden bounds/Twisted bow/core, Guardian, Blood Moon,
  Dharok and Strength-based ranged); `src/lib/scaling/ChambersOfXeric.ts` (Guardian HP).
  https://github.com/weirdgloop/osrs-dps-calc/tree/89c3e25b344aea90d0189746e4b5f73dde0f0383/src/lib
- RuneLite gameval `ItemID.java` / `NpcID.java` checked 16 October 2026:
  https://github.com/runelite/runelite/tree/master/runelite-api/src/main/java/net/runelite/api/gameval

Existing GPL attribution/licensing remains applicable. Other plugins were comparison
inputs, not the authority for the implemented rules. Approved Huey/Seren/Verzik
exceptions remain unchanged.

## Coverage decisions

- 28949 → 28947 (uncharged), 28953 → 28951 (charged).
- Ordinary ancient MA2 capes 34633/34637 and max cape 34625 reuse MA2 equipment
  stats. Original owned item identity/name remains intact for menus, exclusions,
  requirements and god-spell Charge. Deadman-only 34607 is not enabled.
- Vasilias: Normal 8355/8356/8357, Entry 10787/10788/10789, Hard
  10808/10809/10810 (melee/magic/ranged respectively). These replace the shared
  base IDs on the already-generated forms; they do not add duplicate targets.
- No bulk import of unverified NPC aliases, DMM/Echo/arena/historical states or
  inactive NPCs. Dataset hashes and reviewed Slayer-location evidence are intact.

## Scope and limits

Warden P2 bounds retain integer stages, scale with the selected raid defence roll,
and precede the existing ranged bolt and Magic proc distributions. Core melee
uses a guaranteed maximum attack, without special attacks, timed ejection cycles,
body-damage transfer or a complete fight simulation. Guardian damage uses your
base Mining level; target HP uses the new average-party-Mining control.

Moons full-set effects are intentionally excluded; individual pieces retain their
ordinary stats and weapons retain their ordinary attacks. Dharok scales rolled
hits before accurate-zero and NPC mitigation using captured HP. Regenerate after
HP changes.

Atlatl/Hunter's spear damage uses Strength and melee Strength bonus, with Ranged
accuracy/prayers/Void and the supported melee Salve/task damage modifiers. The
optimizer keeps Strength-based supporting candidates and scores these branches
with the production calculator. Moons set effects are not ranked.

Thralls and defence-draining utility are out of scope by request. Special rotations
and damage over time remain excluded.

## Verification

- All main/test Java sources compile with Java 17 targeting Java 11 bytecode.
- All 11 pre-existing Java self-test suites and the new `combatSearchSelfTest` pass.
- All 34 Python tests pass. Gradle `assemble` succeeds.
- New CoX Mining control rendered at 220px width and visually checked.
- Standard Gradle remote dependency resolution was blocked in this environment;
  validation used an external temporary Gradle init script pointing to downloaded
  RuneLite 1.13.2/support JARs. No repository dependency or CI configuration was
  changed for that workaround. Normal IntelliJ Gradle resolution and live game
  interaction still need the user's local check.

## Test 2 follow-up

Raid filters also accept tombs/amascut, chambers/xeric and theatre/blood,
including full names and a following boss query. All raid settings persist in
RuneLite's configuration profile across logout and restarts; profile changes
reload the appropriate values without triggering save callbacks during restore.
First use or malformed saved data safely restores defaults.

All Java sources recompiled targeting Java 11. Combat/search, raid options and
target-style immunity self-tests pass after this follow-up, including all six
new terms, saved-setting round trips and recreated Swing panel restore checks.

## Spec and attack row test 1

The fourth equipment row shows a spec weapon, the selected magic spell (or built-in
weapon icon), and a double-width combat-style/stance button using RuneLite's combat
button sprite. It is an informational recommendation, not a client combat control.

Specials are scored separately from ordinary DPS as expected immediate damage /
attack interval in the displayed armour, using owned usable weapons and compatible
owned ammo. Melee may select another melee attack type for the special. No armour
switch, defence-drain benefit, energy-regeneration cycle, healing, burn or rotation
is assumed. Ruby uses the selected target's initial HP. The tooltip states energy,
ammo, burst scope and any owned specials omitted because they are unmodelled.

Validated coverage: Dragon/Abyssal dagger, Dragon claws, four original godswords,
Voidwaker, Dragon mace/sword/longsword/scimitar/warhammer, Abyssal whip, Elder maul,
Barrelchest anchor, Dragon/Crystal halberd, Granite hammer, Fang; Magic shortbow
and imbued variant, Dark bow, Toxic blowpipe, Webweaver bow, Heavy/Light ballista,
Dragon knife and Zaryte crossbow; Eye of Ayak, Volatile/Eldritch Nightmare staff.
Other specials are excluded, not substituted with normal attacks.

Sources: the same pinned primary Wiki source above, PlayerVsNPCCalc.ts,
dists/claws.ts and dists/bolts.ts. Existing GPL attribution applies.
