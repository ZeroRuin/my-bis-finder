"""Compile positive location facts for Konar/Krystilia task families only.

The input is a pinned, local NPC-spawn research snapshot. No NPC config, map,
animation, coordinate or model data is redistributed in the plugin. Named area
footprints below are deliberately limited to interiors whose identity is known;
absence is not evidence that a target cannot appear elsewhere. Review the output
and coverage report before adopting a newer snapshot.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src/main/resources/slayer-data'
SOURCE_COMMIT = '6a3ca6f19d65c5609434b51cac8dee9d4af97c02'
SOURCE_BLOB = '1b01d6c11d298e275bd08a2823a81f2a17ba1e76'

# Inclusive tile rectangles, on any plane unless stated otherwise. These are
# research footprints, not a player-position check or game assignment boundary.
# Adjacent/uncertain areas are left out rather than assigned by nearest entrance.
AREAS = {
    'Brimhaven Dungeon': [(2624, 9408, 2751, 9599)],
    'Catacombs of Kourend': [(1600, 9984, 1791, 10111)],
    'Chasm of Fire': [(1408, 9984, 1471, 10111)],
    'Taverley Dungeon': [(2816, 9728, 3007, 9983)],
    'Karuulm Slayer Dungeon': [(1216, 10112, 1407, 10303)],
    'Fremennik Slayer Dungeon': [(2688, 9920, 2815, 10047)],
    'Stronghold Slayer Cave': [(2368, 9728, 2495, 9855)],
    'Iorwerth Dungeon': [(3136, 12288, 3327, 12479)],
    'Slayer Tower': [(3392, 3520, 3455, 3583), (3392, 9920, 3455, 9983)],
    'Smoke Dungeon': [(3200, 9344, 3327, 9407)],
    'Waterfall Dungeon': [(2560, 9856, 2623, 9919)],
    "Giants' Den": [(1408, 9856, 1471, 9919)],
    'Isle of Souls': [(2112, 9280, 2175, 9343)],
    'Myths\' Guild': [(1856, 8960, 1983, 9023)],
    'Ogre Enclave': [(2496, 9408, 2623, 9471)],
    'Witchaven Dungeon': [(2688, 9664, 2751, 9727)],
    'God Wars Dungeon': [(2816, 5248, 2943, 5439)],
    'Jormungand\'s Prison': [(2368, 10368, 2495, 10495)],
    'Lighthouse': [(2496, 9984, 2559, 10047)],
    'Waterbirth Island': [(2432, 10112, 2559, 10239), (1792, 4352, 1919, 4479)],
    'Kalphite Lair': [(3456, 9472, 3519, 9535)],
    'Kalphite Cave': [(3264, 9472, 3391, 9599)],
    'Ancient Cavern': [(1728, 5312, 1791, 5375), (1600, 5312, 1663, 5375)],
    'Kraken Cove': [(2240, 9984, 2303, 10047)],
    'Brine Rat Cavern': [(2688, 10112, 2751, 10175)],
    'Asgarnian Ice Dungeon': [(2944, 9472, 3135, 9599)],
    'Mourner Tunnels': [(1984, 4608, 2047, 4671)],
    'Wyvern Cave': [(3584, 10176, 3647, 10303)],
    'Lithkren Vault': [(1536, 5056, 1599, 5119)],
    'Smoke Devil Dungeon': [(2304, 9408, 2431, 9471)],
    'Stronghold of Security': [(2304, 5184, 2367, 5247)],
    'Abyss': [(3008, 4864, 3071, 4927)],
    'Meiyerditch Laboratories': [(3520, 9664, 3647, 9855)],
    'Lizardman Canyon': [(1408, 3648, 1535, 3775)],
    'Lizardman Settlement': [(1280, 3520, 1407, 3647), (1280, 9920, 1343, 9983)],
    'Molch': [(1280, 10048, 1343, 10111)],
    'Battlefront': [(1344, 3456, 1407, 3519)],
    'Death Plateau': [(2816, 3584, 2943, 3647)],
    'Troll Stronghold': [(2816, 10048, 2943, 10111)],
    'South of Mount Quidamortem': [(1216, 3456, 1279, 3583)],
    'Keldagrim': [(2752, 10112, 2815, 10175)],
    "Evil Chicken's Lair": [(2432, 4352, 2495, 4479)],
    'Forthos Dungeon': [(1792, 9920, 1855, 9983)],
    'Meiyerditch': [(3584, 3136, 3647, 3327)],
    'Slepe': [(3712, 3328, 3775, 3391)],
    'Wilderness': [
        (2944, 3520, 3391, 4095),  # surface, except Ferox safe area below
        (2944, 10048, 3071, 10175),  # Wilderness God Wars Dungeon
        (3072, 9920, 3135, 10047),  # northern Edgeville Dungeon
        (2944, 10304, 3135, 10431),  # Deep Wilderness Dungeon
        (3008, 10240, 3071, 10303),  # Lava Maze Dungeon
        (3136, 10048, 3263, 10239),  # Revenant Caves
        (3328, 9984, 3519, 10239),  # Wilderness Slayer Cave
    ],
}


def normalize(text):
    text = re.sub(r'\s+', ' ', text.lower().strip())
    return text[4:] if text.startswith('the ') else text


def singular(text):
    n = normalize(text)
    if n == 'aviansies': return 'aviansie'
    if n == 'zombies': return 'zombie'
    if n.endswith(' axes'): return n[:-1]
    if n.endswith('ies'): return n[:-3] + 'y'
    if n.endswith(('sses', 'xes')): return n[:-2]
    if n.endswith('s') and not n.endswith('ss'): return n[:-1]
    return n


def family_matches(name, roots):
    n = normalize(name)
    return any(re.search(r'(^| )' + re.escape(root) + r'($| |\-)', n) for root in roots if root)


def inside(spawn, area):
    x, y = spawn['x'], spawn['y']
    if area == 'Wilderness' and 3120 <= x <= 3167 and 3584 <= y <= 3647:
        return False  # conservative Ferox safe-area exclusion
    return any(x1 <= x <= x2 and y1 <= y <= y2 for x1, y1, x2, y2 in AREAS[area])


def assignment_masters(memberships, area, scope):
    return [m for m, listed in scope['masters'].items()
            if any(t in listed and (m == 'Krystilia' or area in scope['konarLocations'].get(t, []))
                   for t in memberships)
            and (area == 'Wilderness') == (m == 'Krystilia')]


def compile_rows(spawns, scope, tasks, monsters):
    families = sorted(set(sum(scope['masters'].values(), [])))
    aliases = {normalize(k): v for k, v in tasks['tasks'].items()}
    roots = {task: set([normalize(task), singular(task)]
                      + [normalize(a) for a in aliases.get(normalize(task), [])]
                      + [singular(a) for a in aliases.get(normalize(task), [])])
             for task in families}
    if 'Lesser Nagua' in roots: roots['Lesser Nagua'].add('earthen nagua')
    # Restrict research to variants actually represented in our combat pack.
    combat = {}
    by_id = {}
    for m in monsters:
        combat.setdefault((normalize(m['name']), m['level']), []).append(m)
        by_id.setdefault(m['id'], []).append(m)
    facts = {}
    for s in spawns:
        key = (normalize(s.get('name', '')), s.get('combatLevel', 0))
        if key[1] <= 0: continue
        # Wiki disambiguates some titles (e.g. Dagannoth (Waterbirth Island))
        # while the game uses the plain name. Only an exact ID AND combat level
        # may bridge that difference; never strip arbitrary title suffixes.
        records = combat.get(key, [])
        exact_title = [m for m in by_id.get(s['id'], []) if m['level'] == key[1]]
        if not records and exact_title:
            for m in exact_title:
                memberships = [t for t in families if family_matches(m['name'], roots[t])]
                for area in AREAS:
                    if not inside(s, area): continue
                    masters = assignment_masters(memberships, area, scope)
                    if not masters: continue
                    fact = facts.setdefault((normalize(m['name']), m['level'], area), {
                        'name': m['name'], 'level': m['level'], 'location': area,
                        'masters': set(), 'tasks': set(), 'evidenceNpcIds': set(), 'catalogueIds': set()})
                    fact['masters'].update(masters)
                    fact['tasks'].update(memberships)
                    fact['evidenceNpcIds'].add(s['id'])
                    fact['catalogueIds'].add(m['id'])
        if not records: continue
        # Equal names/levels are not enough when combat stats differ (e.g.
        # Lighthouse vs Catacombs level-74 Dagannoth). Require exact NPC-ID
        # evidence in that case, preserving only the matching combat group.
        def signature(m):
            return json.dumps({k: m.get(k) for k in ('skills', 'offensive', 'defensive',
                              'attributes', 'immunities', 'weakness', 'size', 'speed', 'style')}, sort_keys=True)
        signatures = {signature(m) for m in records}
        if len(signatures) > 1:
            exact = {signature(m) for m in records if m['id'] == s['id']}
            records = [m for m in records if signature(m) in exact]
        if not records: continue
        memberships = [task for task in families if family_matches(s['name'], roots[task])]
        if not memberships: continue
        for area in AREAS:
            if not inside(s, area): continue
            masters = assignment_masters(memberships, area, scope)
            if not masters: continue
            fact = facts.setdefault((key[0], key[1], area), {
                'name': s['name'], 'level': key[1], 'location': area,
                'masters': set(), 'tasks': set(), 'evidenceNpcIds': set(), 'catalogueIds': set()})
            fact['masters'].update(masters)
            fact['tasks'].update(memberships)
            fact['evidenceNpcIds'].add(s['id'])
            fact['catalogueIds'].update(m['id'] for m in records)
    rows = []
    for key in sorted(facts):
        fact = facts[key]
        for field in ('masters', 'tasks', 'evidenceNpcIds', 'catalogueIds'): fact[field] = sorted(fact[field])
        rows.append(fact)
    # A literal area-labelled Wiki combat version is another positive fact.
    # Do not parse arbitrary text or infer locations from monster names.
    version_areas = {normalize(a): a for a in AREAS}
    version_areas['wilderness slayer cave'] = 'Wilderness'
    for m in monsters:
        area = version_areas.get(normalize(m.get('version', '')))
        if area is None: continue
        memberships = [t for t in families if family_matches(m['name'], roots[t])]
        masters = assignment_masters(memberships, area, scope)
        if not masters: continue
        rows.append({'name': m['name'], 'level': m['level'], 'location': area,
                     'masters': masters, 'tasks': memberships, 'evidenceNpcIds': [m['id']],
                     'catalogueIds': [m['id']],
                     'evidence': 'Literal area-labelled Wiki combat version'})
    rows.sort(key=lambda r: (normalize(r['name']), r['level'], r['location']))
    return rows


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--spawns', type=Path, required=True)
    parser.add_argument('--commit', required=True, choices=[SOURCE_COMMIT])
    args = parser.parse_args()
    raw = args.spawns.read_bytes()
    blob = hashlib.sha1(b'blob ' + str(len(raw)).encode() + b'\0' + raw).hexdigest()
    if blob != SOURCE_BLOB:
        raise ValueError('Spawn input does not match the reviewed pinned Git blob')
    rows = compile_rows(json.loads(raw), json.loads((DATA / 'restricted-task-scope.json').read_text()),
                        json.loads((DATA / 'tasks.json').read_text()),
                        json.loads((ROOT / 'src/main/resources/wiki-data/monsters.json').read_text()))
    pack = {'source': f'https://github.com/mejrs/data_osrs/blob/{args.commit}/NPCList_OSRS.json',
            'sourceCommit': args.commit, 'sourceSha256': hashlib.sha256(raw).hexdigest(),
            'sourceBlob': blob,
            'combatSourceSha256': hashlib.sha256((ROOT / 'src/main/resources/wiki-data/monsters.json').read_bytes()).hexdigest(),
            'scopeSha256': hashlib.sha256((DATA / 'restricted-task-scope.json').read_bytes()).hexdigest(),
            'scope': ['Konar', 'Krystilia'], 'positiveMatchesOnly': True,
            'note': 'Researched spawn facts within named interior footprints; incomplete. No nearest-location inference.',
            'rows': rows}
    (DATA / 'restricted-location-matches.json').write_text(json.dumps(pack, indent=2) + '\n')
    print(f'Compiled {len(rows)} scoped name/level/location facts.')


if __name__ == '__main__':
    main()
