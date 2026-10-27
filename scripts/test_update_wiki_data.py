import copy
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import update_wiki_data as updater

class UpdateTests(unittest.TestCase):
    def setUp(self):
        self.rows = {
            'equipment': [{'id':1,'name':'Item','slot':'weapon','speed':4,'bonuses':dict.fromkeys(('str','ranged_str','magic_str','prayer'),0),'offensive':dict.fromkeys(('stab','slash','crush','magic','ranged'),0),'defensive':dict.fromkeys(('stab','slash','crush','magic','ranged'),0)}],
            'monsters': [{'id':1,'name':'Monster','skills':dict.fromkeys(('atk','def','str','hp','ranged','magic'),1),'defensive':dict.fromkeys(('stab','slash','crush','magic','light','standard','heavy','flat_armour'),0),'attributes':[],'immunities':{}}],
            'spells': [{'name':'Spell','max_hit':1,'spellbook':'standard'}]}

    def scenario(self, incoming, date='2026-10-09', fails=False):
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp);directory=root/'src/main/resources/wiki-data';directory.mkdir(parents=True)
            for kind,rows in self.rows.items(): (directory/(kind+'.json')).write_text(json.dumps(rows))
            manifest={'dataSnapshotDate':'2026-09-13','counts':dict.fromkeys(updater.FILES,1),'sha256':{}}
            (directory/'manifest.json').write_text(json.dumps(manifest))
            before={p.name:p.read_bytes() for p in directory.iterdir()}
            def fetch(url):
                if '/commits/main' in url:return json.dumps({'sha':'a'*40}).encode()
                if '/commits?' in url:return json.dumps([{'commit':{'committer':{'date':date+'T00:00:00Z'}}}]).encode()
                kind=url.rsplit('/',1)[-1][:-5];return json.dumps(incoming[kind]).encode()
            with patch.object(updater,'fetch',fetch),patch.dict('os.environ',{'WIKI_UPDATE_REPORT':str(root/'report.md'),'GITHUB_OUTPUT':str(root/'output'),'GITHUB_STEP_SUMMARY':str(root/'summary')},clear=True):
                if fails:
                    with self.assertRaises(ValueError):updater.run(root)
                    self.assertEqual(before,{p.name:p.read_bytes() for p in directory.iterdir()})
                    return
                changed=updater.run(root)
            return changed,json.loads((directory/'manifest.json').read_text()),json.loads((directory/'equipment.json').read_text())

    def test_identical_no_update(self):self.assertFalse(self.scenario(self.rows)[0])
    def test_format_and_order_ignored(self):self.assertTrue(updater.equivalent([{'a':1,'b':2}], [{'b':2,'a':1}]))
    def test_changed_updates_hash_and_provenance(self):
        new=copy.deepcopy(self.rows);new['equipment'][0]['speed']=5
        changed,manifest,rows=self.scenario(new)
        self.assertTrue(changed);self.assertEqual(rows[0]['speed'],5)
        self.assertEqual(manifest['importBaselineDates']['spells.json'],'2026-09-13')
        self.assertEqual(len(manifest['sha256']['equipment.json']),64)
    def test_older_upstream_not_imported(self):
        new=copy.deepcopy(self.rows);new['equipment'][0]['speed']=5
        self.assertFalse(self.scenario(new,'2026-09-02')[0])
    def test_failure_after_first_file_does_not_write(self):
        new=copy.deepcopy(self.rows);new['equipment'][0]['speed']=5;new['spells']=[]
        self.scenario(new,fails=True)
    def test_missing_numeric_field_rejected(self):
        new=copy.deepcopy(self.rows);del new['monsters'][0]['defensive']['heavy']
        self.scenario(new,fails=True)

class IncrementalJsonTests(unittest.TestCase):
    def render(self, kind, original, incoming):
        output = updater.incremental_json(kind, original, incoming)
        self.assertTrue(updater.equivalent(json.loads(output), incoming))
        return output

    def test_append_retains_original_crlf_bytes_and_no_final_newline(self):
        original = b'[\r\n  {\r\n    "id": 1,\r\n    "name": "Existing"\r\n  }\r\n]'
        incoming = json.loads(original) + [{'id': 2, 'name': 'New'}]
        output = self.render('equipment', original, incoming)
        self.assertTrue(output.startswith(original[:-3] + b',\r\n'))
        self.assertEqual(output.count(b'\n'), output.count(b'\r\n'))
        self.assertTrue(output.endswith(b'\r\n]'))

    def test_upstream_reorder_does_not_move_unchanged_records(self):
        original = b'[\n  {"id":1,"name":"First"},\n  { "id":2, "name":"Second" }\n]\n'
        rows = json.loads(original)
        self.assertEqual(self.render('equipment', original, rows[::-1]), original)

    def test_updated_stats_stay_in_place_and_other_record_is_verbatim(self):
        original = b'[\n  {"id":1,"speed":4},\n  { "id":2, "speed":5 }\n]\n'
        rows = json.loads(original)
        rows[0]['speed'] = 3
        rows.reverse()
        output = self.render('equipment', original, rows)
        self.assertEqual(json.loads(output)[0]['speed'], 3)
        self.assertIn(b'{ "id":2, "speed":5 }', output)

    def test_duplicate_id_exact_match_precedes_changed_identity(self):
        original = b'[{"id":1,"version":"","hp":10}, {"id":1,"version":"","hp":20}]'
        rows = [{'id':1,'version':'','hp':20}, {'id':1,'version':'','hp':11}]
        output = self.render('monsters', original, rows)
        self.assertEqual([r['hp'] for r in json.loads(output)], [11,20])
        self.assertIn(b'{"id":1,"version":"","hp":20}', output)

    def test_variants_and_identical_duplicates_are_preserved(self):
        original = b'[{"id":1,"version":"A"}, {"id":1,"version":"B"}, {"id":1,"version":"A"}]'
        rows = json.loads(original)
        self.assertEqual(self.render('monsters', original, rows[::-1]), original)
        rows.pop()
        self.assertEqual(len(json.loads(self.render('monsters', original, rows))), 2)

    def test_genuine_removal_does_not_rewrite_remaining_record(self):
        original = b'[\n  {"id":1},\n  { "id":2 }\n]\n'
        self.assertEqual(self.render('equipment', original, [{'id':2}]), b'[\n  { "id":2 }\n]\n')

    def test_spell_identity_includes_spellbook_and_preserves_unicode(self):
        original = '[{"name":"Éther","spellbook":"standard","max_hit":1}]'.encode()
        rows = json.loads(original) + [{'name':'Éther','spellbook':'ancient','max_hit':2}]
        output = self.render('spells', original, rows)
        self.assertTrue(output.startswith(original[:-1]))
        self.assertEqual(len(json.loads(output)), 2)

    def test_numeric_representation_change_is_not_silently_discarded(self):
        output = self.render('equipment', b'[{"id":1,"weight":1}]', [{'id':1,'weight':1.0}])
        self.assertIn(b'1.0', output)


if __name__=='__main__':unittest.main()
