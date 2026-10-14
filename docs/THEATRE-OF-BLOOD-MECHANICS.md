# Seren and Theatre of Blood target rules

## Damage rules

Seren now follows the requested Wiki prose rule: a rolled hitsplat of 0–24
is unchanged; a hitsplat above 24 is rerolled uniformly to 22, 23 or 24.
This intentionally differs from the current upstream calculator's
`linearMinTransformer(2, 22)`, which can also reduce a 23 or 24.
Recoil and Vengeance are not included in sustained attack calculations.

Verzik phase 1, in all three difficulties, follows the supplied above-cap reroll
rule: melee hits above 10 reroll uniformly to 0–10; ranged/magic hits above 3
reroll uniformly to 0–3. Hits at or below the threshold and misses are unchanged.
This also intentionally differs from upstream's linear-min transform, which
rerolls a limiter even for low hits. Phases 2 and 3 do not receive this limiter.
The transform is applied per hitsplat before DPS and expected TTK. The displayed
max for multi-hit weapons is the total attack and can exceed the individual cap.

Dawnbringer is not added as a supported powered weapon or special attack.
The cap helper retains its weapon-name exemption, without implementing its
accuracy, damage or special-attack behaviour.

## Catalogue

The upstream snapshot already contains each main boss's three difficulties,
including Verzik's nine phase records. Runtime labels normalise Entry/Normal/Hard
Mode and preserve them in the catalogue key so equal-stat modes are not merged.
The source JSON and data hashes are unchanged.

Nylocas Vasilias expands each difficulty's source profile into Melee, Ranged and
Magic form choices. These are runtime combat states sharing the source profile's
ID and stats, not newly invented NPC IDs. The version label participates in the
picker identity and generated-result cache key. Hard Mode Prinkipas's existing
three form records receive explicit labels. Non-matching combat styles cannot
produce a damaging recommendation.

Estimates use the source profile's full bundled HP (not a player's current boss
HP). There is no party-size control/scaling, team DPS, phase-switch timing, Bloat
sleep/walking timing, Xarpus downtime, incoming damage, or complete raid simulation.
A selected Nylocas form is a comparison for that form, not a full kill forecast
that alternates forms. Mode identity is preserved even when offensive stats match.

## Sources and checks

Checked 14 October 2026:

- https://oldschool.runescape.wiki/w/Fragment_of_Seren
- https://oldschool.runescape.wiki/w/Maximum_damage_cap
- https://oldschool.runescape.wiki/w/Verzik_Vitur
- https://oldschool.runescape.wiki/w/Nylocas_Vasilias
- https://github.com/weirdgloop/osrs-dps-calc/blob/main/src/lib/PlayerVsNPCCalc.ts
- https://github.com/weirdgloop/osrs-dps-calc/blob/main/src/lib/constants.ts

The Wiki prose for the requested rerolls was supplied by the maintainer.
The upstream calculator is used to cross-check phase-one IDs and identify the
intentional distribution differences documented above, not as evidence of full
encounter parity. All skill, defence, HP, weakness and attribute data continue to
come from the existing bundled monster records.

Run `theatreOfBloodSelfTest` and the existing mechanics, Hueycoatl and Slayer
suites. In-game picker/style checks are listed in TESTING.md.
