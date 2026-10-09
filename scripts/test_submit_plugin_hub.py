import copy
import unittest
from submit_plugin_hub import eligible, SOURCE

class EligibilityTests(unittest.TestCase):
    def setUp(self):
        self.pr={'merged':True,'base':{'ref':'main','repo':{'full_name':SOURCE}},'head':{'ref':'automation/wiki-data-update','repo':{'full_name':SOURCE}},'user':{'login':'github-actions[bot]'}}
        self.files=[{'filename':'src/main/resources/wiki-data/monsters.json'}]
    def test_merged_bot_data_accepted(self):self.assertTrue(eligible(self.pr,self.files))
    def test_unmerged_rejected(self):
        self.pr['merged']=False;self.assertFalse(eligible(self.pr,self.files))
    def test_fork_rejected(self):
        self.pr['head']['repo']['full_name']='other/repo';self.assertFalse(eligible(self.pr,self.files))
    def test_manual_pr_rejected(self):
        self.pr['user']['login']='ZeroRuin';self.assertFalse(eligible(self.pr,self.files))
    def test_code_change_rejected(self):
        self.files.append({'filename':'build.gradle'});self.assertFalse(eligible(self.pr,self.files))
    def test_empty_changes_rejected(self):self.assertFalse(eligible(self.pr,[]))

if __name__=='__main__':unittest.main()
