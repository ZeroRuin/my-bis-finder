"""Write a human review list from the live slayerUnresolvedAudit JSON export."""
import argparse
import collections
import json
from pathlib import Path
from urllib.parse import quote
ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--audit', type=Path, required=True)
    args = parser.parse_args()
    rows = json.loads(args.audit.read_text())
    if not rows:
        (ROOT / 'SLAYER-UNRESOLVED-VARIANTS.md').write_text(
            '# Slayer variants still needing verification\n\n'
            'The current Konar/Krystilia audit has zero unresolved entries.\n\n'
            'All 470 test4 review decisions were completed by the user on 11 October 2026: '
            'Konar 26 Yes / 178 No; Krystilia 22 Yes / 244 No. '
            'Krystilia No means no Wilderness spawn. Decisions apply to exact task, area '
            'and picker combat profile; see SLAYER-DATA-NOTES.md.\n\n'
            'This resolves the candidates in the current catalogue; it does not establish '
            'that every possible game target is present. Rerun after task/combat-data changes.\n')
        print('Konar/Krystilia: zero unresolved entries')
        return
    monsters = json.loads((ROOT / 'src/main/resources/wiki-data/monsters.json').read_text())
    by = collections.defaultdict(list)
    for m in monsters:
        by[m['id']].append(m)
    text = ['# Slayer variants still needing verification', '',
            'Based on your supplied Konar Wiki task/area/alternative table and the current bundled combat catalogue.', '',
            '133 of 281 table species/area rules resolve automatically: one positive combat level and identical complete combat data. This includes already-confirmed species. Multiple levels or differing same-level profiles still need exact evidence. Generic family names never establish the distribution of individual species.', '',
            'Construction never counts as a Slayer target. Konar now permits explicit listed species and supported superiors, excluding Nightmare Zone, unrelated broad aliases and area-specific alternatives assigned elsewhere.', '',
            '## Check these empty Konar areas first', '',
            '- Lizardmen at Kebos Swamp: verify level 53 and/or 62.',
            '- Trolls at Fremennik Isles: verify the Ice troll species and levels.',
            '- Trolls at Wyrmscraig Cavern: verify the troll species, levels and profiles.', '',
            '## How to review', '',
            'For each unresolved assigned area, record Yes, No or Still uncertain for the exact combat profile. Check both location and task credit. HP, Defence/Magic levels and defence bonuses distinguish same-level entries. IDs are representative picker entries; interchangeable model IDs may be merged. Source Wiki version labels are shown even when the picker merges them.', '',
            'An unresolved entry remains hidden. Already-confirmed areas are positive evidence, not an exhaustive location list. A populated area can still lack some variants.', '']
    for master in ('Konar', 'Krystilia'):
        groups = collections.defaultdict(list)
        for r in rows:
            if r['master'] == master:
                groups[r['id']].append(r)
        decisions = sum(len(r['unknown']) for rs in groups.values() for r in rs)
        text += [f'## {master}: {len(groups)} distinct entries, {decisions} unresolved area decisions', '']
        if master == 'Krystilia':
            text += ['Separate Wilderness review: the Konar table does not verify these. This list still uses broad task-name matching and may include unrelated or out-of-Wilderness creatures. All unresolved candidates stay hidden. A Krystilia task/alternative table can narrow it further.', '']
        text += ['| ID | Target | Level / Wiki version | Task | Areas needing verification | Already confirmed | Combat details | Wiki / your notes |', '|---|---|---|---|---|---|---|---|']
        for npc, rs in sorted(groups.items(), key=lambda x: (x[1][0]['task'].lower(), x[1][0]['name'].lower(), x[1][0]['level'], x[0])):
            r = rs[0]
            records = by[npc]
            versions = ', '.join(sorted({m.get('version', '') for m in records if m.get('version')})) or r['version']
            m = records[0] if records else {}
            sk, d = m.get('skills', {}), m.get('defensive', {})
            stats = f"HP {sk.get('hp', '?')}, Def {sk.get('def', '?')}, Magic {sk.get('magic', '?')}; defence stab/slash/crush/magic " + '/'.join(str(d.get(k, 0)) for k in ('stab', 'slash', 'crush', 'magic'))
            areas = '; '.join(x['task'] + ': ' + ', '.join(x['unknown']) for x in rs)
            link = 'https://oldschool.runescape.wiki/w/' + quote(r['name'].replace(' ', '_'), safe="_()'")
            vals = [npc, r['name'], str(r['level']) + (' — ' + versions if versions else ''), '; '.join(sorted({x['task'] for x in rs})), areas, '; '.join(sorted({a for x in rs for a in x['confirmed']})) or 'None', stats, f'[Wiki]({link}) — ']
            text.append('| ' + ' | '.join(str(v).replace('|', '/').replace('\n', ' ') for v in vals) + ' |')
        text.append('')
        print(master, len(groups), 'distinct entries;', decisions, 'area decisions')
    text += ['## Reproduce', '',
             'Run the Gradle task `slayerUnresolvedAudit`, then `python scripts/write_slayer_review.py --audit SLAYER-UNRESOLVED-VARIANTS.json`. After a combat-pack update, rerun `python scripts/compile_konar_targets.py`, inspect the diff and rerun tests/audits. The runtime snapshot hash disables stale automatic uniqueness conclusions.', '',
             'Membership source: your supplied Konar Wiki table, plus documented prior superior/location research. This report is a review list; its Wiki links do not imply that every page has been retrieved. See SLAYER-DATA-NOTES.md for provenance and limitations.', '']
    (ROOT / 'SLAYER-UNRESOLVED-VARIANTS.md').write_text('\n'.join(text))


if __name__ == '__main__':
    main()
