import json
import unittest
from compile_restricted_locations import compile_rows, inside, singular, DATA, ROOT


class RestrictedLocationTests(unittest.TestCase):
    def compile(self, spawns, monsters, masters=None):
        return compile_rows(spawns, {'masters': masters or {'Konar': ['Black demons'], 'Krystilia': ['Black demons']},
                                    'konarLocations': {'Black demons': ['Brimhaven Dungeon', 'Catacombs of Kourend']}},
                            {'tasks': {'Black demons': []}}, monsters)

    def test_brimhaven_variant_is_not_catacombs_variant(self):
        rows = self.compile([
            {'name': 'Black demon', 'combatLevel': 172, 'id': 2050, 'x': 2700, 'y': 9489},
            {'name': 'Black demon', 'combatLevel': 178, 'id': 7243, 'x': 1720, 'y': 10090}],
            [{'name': 'Black demon', 'level': 172, 'id': 240}, {'name': 'Black demon', 'level': 178, 'id': 7243}])
        self.assertEqual([(r['level'], r['location']) for r in rows],
                         [(172, 'Brimhaven Dungeon'), (178, 'Catacombs of Kourend')])

    def test_non_assignment_family_is_not_compiled(self):
        self.assertEqual(self.compile([{'name': 'Cow', 'combatLevel': 2, 'id': 81, 'x': 2700, 'y': 9489}],
                                     [{'name': 'Cow', 'level': 2, 'id': 81}]), [])

    def test_krystilia_gets_no_non_wilderness_matches(self):
        rows = self.compile([{'name': 'Black demon', 'combatLevel': 172, 'id': 240, 'x': 2700, 'y': 9489}],
                            [{'name': 'Black demon', 'level': 172, 'id': 240}], {'Krystilia': ['Black demons']})
        self.assertEqual(rows, [])

    def test_konar_gets_no_wilderness_matches(self):
        rows = self.compile([{'name': 'Black demon', 'combatLevel': 172, 'id': 240, 'x': 3100, 'y': 9950}],
                            [{'name': 'Black demon', 'level': 172, 'id': 240}], {'Konar': ['Black demons']})
        self.assertEqual(rows, [])

    def test_ambiguous_combat_variants_require_exact_id(self):
        rows = self.compile([{'name': 'Black demon', 'combatLevel': 172, 'id': 240, 'x': 2700, 'y': 9489}],
                            [{'name': 'Black demon', 'level': 172, 'id': 240, 'skills': {'hp': 100}},
                             {'name': 'Black demon', 'level': 172, 'id': 999, 'skills': {'hp': 200}}])
        self.assertEqual(rows[0]['catalogueIds'], [240])

    def test_unmatched_id_does_not_guess_ambiguous_stats(self):
        rows = self.compile([{'name': 'Black demon', 'combatLevel': 172, 'id': 123, 'x': 2700, 'y': 9489}],
                            [{'name': 'Black demon', 'level': 172, 'id': 240, 'skills': {'hp': 100}},
                             {'name': 'Black demon', 'level': 172, 'id': 999, 'skills': {'hp': 200}}])
        self.assertEqual(rows, [])

    def test_unknown_coordinates_are_not_nearest_entrance_matches(self):
        rows = self.compile([{'name': 'Black demon', 'combatLevel': 172, 'id': 240, 'x': 1000, 'y': 1000}],
                            [{'name': 'Black demon', 'level': 172, 'id': 240}])
        self.assertEqual(rows, [])

    def test_ferox_safe_area_excluded(self):
        self.assertFalse(inside({'x': 3140, 'y': 3620}, 'Wilderness'))
        self.assertTrue(inside({'x': 3100, 'y': 3800}, 'Wilderness'))

    def test_literal_wiki_area_version_is_positive_evidence(self):
        rows = self.compile([], [{'name': 'Black demon', 'level': 188, 'id': 7874,
                                   'version': 'Wilderness Slayer Cave'}])
        self.assertEqual(rows[0]['location'], 'Wilderness')
        self.assertEqual(rows[0]['catalogueIds'], [7874])

    def test_plural_fixes(self):
        self.assertEqual(singular('Magic axes'), 'magic axe')
        self.assertEqual(singular('Zombies'), 'zombie')

    def test_wiki_disambiguated_title_requires_exact_id_and_level(self):
        scope = {'masters': {'Krystilia': ['Zombies']}, 'konarLocations': {}}
        monsters = [{'name': 'Zombie (Wilderness)', 'level': 18, 'id': 45}]
        def run(npc, level):
            return compile_rows([{'name': 'Zombie', 'combatLevel': level, 'id': npc,
                                  'x': 3100, 'y': 3800}], scope, {'tasks': {}}, monsters)
        self.assertEqual(run(45, 18)[0]['catalogueIds'], [45])
        self.assertEqual(run(46, 18), [])
        self.assertEqual(run(45, 24), [])

    def test_bundled_registry_scope_and_catalogue_ids(self):
        pack = json.loads((DATA / 'restricted-location-matches.json').read_text())
        monsters = json.loads((ROOT / 'src/main/resources/wiki-data/monsters.json').read_text())
        scope = json.loads((DATA / 'restricted-task-scope.json').read_text())['masters']
        for row in pack['rows']:
            self.assertTrue(row['catalogueIds'])
            for master in row['masters']:
                self.assertIn(master, ('Konar', 'Krystilia'))
                self.assertTrue(set(row['tasks']) & set(scope[master]))
                self.assertEqual(row['location'] == 'Wilderness', master == 'Krystilia')
            for npc in row['catalogueIds']:
                self.assertTrue(any(m['id'] == npc and m['name'].lower() == row['name'].lower()
                                    and m['level'] == row['level'] for m in monsters))


if __name__ == '__main__':
    unittest.main()
