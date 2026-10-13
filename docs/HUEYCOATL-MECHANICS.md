# Hueycoatl targets and tail damage

The bundled monster pack has five states. The picker now shows the three
attackable parts: Body (phase 1), Head and Tail. The shielded head (14011) and
broken tail (14015) are omitted. Target names/attributes/stats in the JSON are
unchanged, including the tail's 300 HP; runtime part labels prevent deduplication
from clearing names. Head/body damage is unchanged.

The tail reroll transform is derived from the OSRS Wiki DPS calculator's `getNpcTransform()`:
https://github.com/weirdgloop/osrs-dps-calc/blob/main/src/lib/PlayerVsNPCCalc.ts
and `linearMinTransformer()`:
https://github.com/weirdgloop/osrs-dps-calc/blob/main/src/lib/HitDist.ts
Checked 13 October 2026. The strategy description is at:
https://oldschool.runescape.wiki/w/The_Hueycoatl/Strategies

Each hitsplat is limited by the minimum of its rolled damage and a uniform roll
from 0 to 4. A crush attack with total equipped crush attack bonus strictly
higher than stab, slash, ranged and magic instead uses 0 to 9; all resulting zero hits,
including misses, then become accurate 1s. All other attacks, including Earth
spells, use 0 to 4 and do not get the crush minimum. The Earth spell exception
in the upstream calculator is deliberately omitted following the user's
corrections on 13 October 2026. The comparison with ranged and magic bonuses
also follows the user's stated Equipment Stats requirement. This is not equivalent to clipping every high hit to
the maximum. Two-hit and Scythe distributions are transformed per hitsplat before
DPS and expected kill time are calculated. Displayed max can therefore exceed 9
when an attack produces multiple hitsplats.

Other melee bonuses are read from the canonical bundled equipment records,
rather than inferred from the selected-style candidate score. Missing equipment
records conservatively disable the special crush benefit. Default pillar state
is without a buff; pillars, thralls and special attacks are not added here.
Blood moon's attack-speed proc is not currently modeled, so full Blood moon set
recommendations should not be treated as complete parity with the Wiki.

Run `hueycoatlSelfTest` for catalogue, distribution and full melee pipeline
checks. Existing mechanics and Slayer checks cover regression outside Hueycoatl.
