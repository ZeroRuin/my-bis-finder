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

if __name__=='__main__':unittest.main()
