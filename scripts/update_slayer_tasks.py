"""Refresh the offline task-name snapshot from an explicitly pinned RuneLite commit.

Run regression checks and review the diff before releasing. This script runs only
during development; the plugin never contacts GitHub or the Wiki during play.
Location rules are separately reviewed and are not inferred from task names.
"""
import argparse
import datetime as dt
import hashlib
import json
from pathlib import Path
import re
import urllib.request

SOURCE_PATH = 'runelite-client/src/main/java/net/runelite/client/plugins/slayer/Task.java'


def parse(source):
    tasks = {}
    for line in source.splitlines():
        if not re.match(r'\s*[A-Z][A-Z0-9_]+\(', line):
            continue
        names = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', line)
        if not names:
            raise ValueError('Unrecognised task definition: ' + line.strip())
        names = [json.loads('"' + name + '"') for name in names]
        if names[0] in tasks:
            raise ValueError('Duplicate task: ' + names[0])
        tasks[names[0]] = names[1:]
    if len(tasks) < 100 or not {'Greater demons', 'Blue dragons', 'Hellhounds'} <= tasks.keys():
        raise ValueError('Incomplete or changed upstream task format; review manually')
    return tasks


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit', required=True, help='Full 40-character RuneLite commit SHA')
    args = parser.parse_args()
    if not re.fullmatch(r'[0-9a-f]{40}', args.commit):
        parser.error('--commit must be a full lower-case commit SHA')
    url = f'https://raw.githubusercontent.com/runelite/runelite/{args.commit}/{SOURCE_PATH}'
    request = urllib.request.Request(url, headers={'User-Agent': 'MyBiSFinder-slayer-updater/1.0'})
    with urllib.request.urlopen(request, timeout=30) as response:
        raw = response.read(200_001)
    if len(raw) > 200_000:
        raise ValueError('Unexpectedly large task source')
    source = raw.decode('utf-8')
    tasks = parse(source)
    blob = hashlib.sha1(b'blob ' + str(len(raw)).encode() + b'\0' + raw).hexdigest()
    directory = Path(__file__).resolve().parents[1] / 'src/main/resources/slayer-data'
    pack = {'source': f'https://github.com/runelite/runelite/blob/{args.commit}/{SOURCE_PATH}',
            'sourceBlob': blob, 'retrievedAt': dt.date.today().isoformat(),
            'tasks': tasks, 'sourceCommit': args.commit}
    license_text = source[:source.index('package ')].replace('/*', '').replace(' */', '')
    license_text = license_text.replace(' * ', '').replace(' *\n', '\n')
    (directory / 'tasks.json').write_text(json.dumps(pack, indent=2) + '\n', encoding='utf-8')
    (directory / 'RUNELITE-LICENSE.txt').write_text(license_text, encoding='utf-8')
    print(f'Imported {len(tasks)} task definitions. Review mappings and run slayerTaskSelfTest.')


if __name__ == '__main__':
    main()
