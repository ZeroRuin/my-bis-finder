"""Resolve table-listed Konar combat profiles, without guessing species from aliases.

Only one positive combat level AND one complete combat signature permits automatic
resolution. This identifies an interchangeable combat setup, not physical spawn IDs.
Multiple levels/stats stay on the exact spawn/manual evidence path. Rules marked
non-automatic (generic Troll/Lizardman/Dagannoth families) always require review.
"""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src/main/resources/slayer-data'
FIELDS = ('level', 'skills', 'offensive', 'defensive', 'attributes', 'immunities',
          'weakness', 'size', 'speed', 'style', 'max_hit')


def combat_records(monsters, name):
    def fixture(m):
        version = m.get('version', '').lower()
        return (version == 'quest' or version.startswith('quest,') or '(quest)' in version
                or version == 'whirlpool' or version.startswith('idle') or 'unused' in version)
    return [m for m in monsters if m['name'].casefold() == name.casefold()
            and m['level'] > 0 and not fixture(m)]


def resolve(monsters, rule):
    records = combat_records(monsters, rule['name'])
    levels = sorted({m['level'] for m in records})
    signatures = {json.dumps({k: m.get(k) for k in FIELDS}, sort_keys=True) for m in records}
    safe = rule.get('automatic', True) and len(levels) == 1 and len(signatures) == 1
    return dict(rule, levels=levels, combatProfiles=len(signatures),
                automaticIds=sorted({m['id'] for m in records}) if safe else [],
                resolution='Single combat level and identical combat data' if safe
                else 'Exact spawn/manual evidence or remaining review required')


def main():
    raw = (DATA / 'konar-target-rules.json').read_bytes()
    pack = json.loads(raw)
    combat = (ROOT / 'src/main/resources/wiki-data/monsters.json').read_bytes()
    monsters = json.loads(combat)
    pack['rulesSha256'] = hashlib.sha256(raw).hexdigest()
    pack['combatSourceSha256'] = hashlib.sha256(combat).hexdigest()
    pack['note'] = 'Automatic IDs identify equivalent combat profiles for an explicitly listed species/area, not a spawn-location claim for each model ID. No propagation to Krystilia.'
    for areas in pack['tasks'].values():
        for area, rules in areas.items():
            areas[area] = [resolve(monsters, rule) for rule in rules]
    (DATA / 'konar-targets.json').write_text(json.dumps(pack, indent=2) + '\n')
    rules = [r for a in pack['tasks'].values() for rs in a.values() for r in rs]
    print(f'{sum(bool(r["automaticIds"]) for r in rules)}/{len(rules)} species/area rules automatically resolved.')


if __name__ == '__main__':
    main()
