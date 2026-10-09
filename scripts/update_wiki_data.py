"""Import published Wiki DPS JSON; never queries the Wiki or changes mechanics."""
import argparse
import collections
import datetime as dt
import hashlib
import json
import math
import os
from pathlib import Path
import time
import urllib.error
import urllib.request

UPSTREAM = 'weirdgloop/osrs-dps-calc'
FILES = ('equipment', 'monsters', 'spells')
UA = 'MyBiSFinder-data-updater/1.0 (+https://github.com/ZeroRuin/my-bis-finder)'


def fetch(url):
    headers = {'User-Agent': UA, 'Accept': 'application/vnd.github+json'}
    token = os.environ.get('GH_TOKEN')
    if token and url.startswith('https://api.github.com/'):
        headers['Authorization'] = 'Bearer ' + token
    for attempt in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=45) as response:
                data = response.read(12_000_001)
                if len(data) > 12_000_000:
                    raise ValueError('Download exceeds 12 MB limit')
                return data
        except urllib.error.HTTPError as error:
            if error.code not in (429, 500, 502, 503, 504) or attempt == 3:
                raise
            delay = error.headers.get('Retry-After', '')
            time.sleep(min(int(delay), 60) if delay.isdigit() else 2 ** (attempt + 1))


def canonical(value):
    return json.dumps(value, sort_keys=True, separators=(',', ':'), ensure_ascii=False)


def number(value):
    return type(value) in (int, float) and math.isfinite(value)


def validate(kind, rows, previous):
    if not isinstance(rows, list) or not rows:
        raise ValueError(f'{kind}: empty or non-list dataset')
    if previous and not .8 <= len(rows) / len(previous) <= 1.2:
        raise ValueError(f'{kind}: record count changed by more than 20%; inspect manually')
    nested = {
        'equipment': {'bonuses': ('str', 'ranged_str', 'magic_str', 'prayer'),
                      'offensive': ('stab', 'slash', 'crush', 'magic', 'ranged'),
                      'defensive': ('stab', 'slash', 'crush', 'magic', 'ranged')},
        'monsters': {'skills': ('atk', 'def', 'str', 'hp', 'ranged', 'magic'),
                     'defensive': ('stab', 'slash', 'crush', 'magic', 'light', 'standard', 'heavy', 'flat_armour')},
        'spells': {}}
    for row in rows:
        if not isinstance(row, dict) or not isinstance(row.get('name'), str) or not row['name']:
            raise ValueError(f'{kind}: invalid record/name')
        if kind != 'spells' and (type(row.get('id')) is not int or row['id'] < 0):
            raise ValueError(f'{kind}: invalid ID')
        for group, fields in nested[kind].items():
            if not isinstance(row.get(group), dict) or any(not number(row[group].get(k)) for k in fields):
                raise ValueError(f'{kind}: invalid numeric {group} for {row["name"]}')
        if kind == 'equipment' and (not isinstance(row.get('slot'), str) or not number(row.get('speed'))):
            raise ValueError('equipment: invalid slot/speed')
        if kind == 'monsters' and (not isinstance(row.get('attributes'), list) or not isinstance(row.get('immunities'), dict)):
            raise ValueError('monsters: invalid attributes/immunities')
        if kind == 'spells' and (not number(row.get('max_hit')) or not isinstance(row.get('spellbook'), str)):
            raise ValueError('spells: invalid damage/spellbook')


def equivalent(left, right):
    # Reordering or formatting alone should not open an update PR; preserve duplicate records.
    return collections.Counter(map(canonical, left)) == collections.Counter(map(canonical, right))


def run(root):
    directory = root / 'src/main/resources/wiki-data'
    manifest = json.loads((directory / 'manifest.json').read_text())
    head = json.loads(fetch(f'https://api.github.com/repos/{UPSTREAM}/commits/main'))['sha']
    if len(head) != 40 or any(c not in '0123456789abcdef' for c in head):
        raise ValueError('Invalid upstream commit SHA')
    staged, lines = {}, [f'Upstream: https://github.com/{UPSTREAM}/commit/{head}', '']
    for kind in FILES:
        filename = kind + '.json'
        old = json.loads((directory / filename).read_text())
        new = json.loads(fetch(f'https://raw.githubusercontent.com/{UPSTREAM}/{head}/cdn/json/{filename}'))
        validate(kind, new, old)
        if equivalent(old, new):
            lines.append(f'- {filename}: unchanged.')
            continue
        commits = json.loads(fetch(f'https://api.github.com/repos/{UPSTREAM}/commits?sha={head}&path=cdn/json/{filename}&per_page=1'))
        changed_at = commits[0]['commit']['committer']['date'][:10]
        previous_date = manifest.get('upstreamFiles', {}).get(filename, {}).get('changedAt', manifest.get('importBaselineDates', {}).get(filename, manifest['dataSnapshotDate']))
        if changed_at < previous_date:
            lines.append(f'- {filename}: skipped older upstream snapshot ({changed_at}; local {previous_date}).')
            continue
        encoded = (json.dumps(new, ensure_ascii=False, indent=2) + '\n').encode()
        staged[filename] = encoded
        before, after = collections.Counter(map(canonical, old)), collections.Counter(map(canonical, new))
        lines.append(f'- {filename}: {len(old)} → {len(new)} records; {sum((after-before).values())} new/changed rows, {sum((before-after).values())} removed/replaced rows.')
        manifest['counts'][kind] = len(new)
        manifest['sha256'][filename] = hashlib.sha256(encoded).hexdigest()
        manifest.setdefault('upstreamFiles', {})[filename] = {'commit': head, 'changedAt': changed_at}
    # Nothing is written until all downloads and validation succeed.
    if staged:
        manifest.setdefault('importBaselineDates', {kind + '.json': manifest['dataSnapshotDate'] for kind in FILES})
        manifest['dataSnapshotDate'] = dt.datetime.now(dt.timezone.utc).date().isoformat()
        manifest['source'] = 'OSRS Wiki DPS Calculator published JSON; see upstreamFiles for per-file provenance'
        for filename, encoded in staged.items():
            (directory / filename).write_bytes(encoded)
        (directory / 'manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
        notes = root / 'WIKI-DATA-NOTES.txt'
        notes.write_text('My BiS Finder offline Wiki data pack\n\nPublished OSRS Wiki DPS Calculator JSON datasets.\nSource: https://github.com/' + UPSTREAM + '\nNo live Wiki requests occur at plugin runtime.\n\nLast local import: ' + manifest['dataSnapshotDate'] + '\n\n' + '\n'.join(f'{k}: {v}' for k, v in manifest['counts'].items()) + '\n\nSee src/main/resources/wiki-data/manifest.json for hashes and per-file upstream commits/dates. Mechanics and special-case code require separate review.\n')
    report = '\n'.join(lines) + '\n\nValidation: JSON/schema/count checks passed. Mechanics self-test must pass before the workflow creates or updates its PR.\n'
    Path(os.environ.get('WIKI_UPDATE_REPORT', '/tmp/wiki-update-report.md')).write_text(report)
    if os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as out:
            out.write(report)
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'], 'a') as out:
            out.write('changed=' + str(bool(staged)).lower() + '\n')
    print(report)
    return bool(staged)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    run(parser.parse_args().root)
