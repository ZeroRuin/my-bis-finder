"""Import a completed review workbook, validating every row against its audit."""
import argparse
import collections
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NS = {'s': 'http://schemas.openxmlformats.org/spreadsheetml/2006/main'}

def import_rows(workbook, audit):
    expected = {(r['master'], r['task'], area, r['id'], r['name'], r['level'])
                for r in audit for area in r['unknown']}
    found = {}
    with zipfile.ZipFile(workbook) as z:
        shared = [''.join(si.itertext()) for si in ET.fromstring(z.read('xl/sharedStrings.xml'))]
        for master, index in [('Konar', 1), ('Krystilia', 2)]:
            sheet = ET.fromstring(z.read(f'xl/worksheets/sheet{index}.xml'))
            for row in sheet.findall('s:sheetData/s:row', NS):
                if int(row.attrib['r']) < 8: continue
                cells = {}
                for cell in row:
                    value = cell.find('s:v', NS)
                    text = value.text if value is not None else ''
                    if cell.attrib.get('t') == 's': text = shared[int(text)]
                    if cell.attrib.get('t') == 'inlineStr': text = ''.join(cell.find('s:is', NS).itertext())
                    cells[cell.attrib['r'].rstrip('0123456789')] = text or ''
                if not cells.get('A'): continue
                key = (master, cells['C'], cells['D'], int(cells['H']), cells['A'], int(cells['B']))
                result = cells['E'].strip()
                if key not in expected or key in found or result not in ('Yes', 'No'):
                    raise ValueError(f'Invalid, duplicate or unfinished row: {key}, {result}')
                found[key] = dict(master=master, task=key[1], area=key[2], id=key[3], name=key[4],
                                  level=key[5], eligible=result == 'Yes',
                                  notes=cells.get('F', ''), evidence=cells.get('G', ''),
                                  reason='No Wilderness spawn' if master == 'Krystilia' and result == 'No' else 'User-reviewed task/area eligibility')
    if set(found) != expected: raise ValueError('Workbook does not cover every audit decision')
    return list(found.values())

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--workbook', type=Path, required=True)
    p.add_argument('--audit', type=Path, required=True)
    args = p.parse_args()
    rows = import_rows(args.workbook, json.loads(args.audit.read_text()))
    pack = dict(source='User-completed Slayer-Target-Review(1).xlsx', reviewedDate='2026-10-11',
                workbookSha256=hashlib.sha256(args.workbook.read_bytes()).hexdigest(),
                combatSourceSha256=hashlib.sha256((ROOT/'src/main/resources/wiki-data/monsters.json').read_bytes()).hexdigest(),
                note='Exact task/area/picker profiles; Krystilia No means no Wilderness spawn. Re-review after combat snapshot changes.', rows=rows)
    (ROOT/'src/main/resources/slayer-data/reviewed-locations.json').write_text(json.dumps(pack, indent=2)+'\n')
    print(collections.Counter((r['master'], r['eligible']) for r in rows))

if __name__ == '__main__': main()
