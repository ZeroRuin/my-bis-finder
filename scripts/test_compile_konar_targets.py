import unittest
from compile_konar_targets import resolve


class KonarProfileTests(unittest.TestCase):
    def test_single_level_with_different_defences_stays_unresolved(self):
        monsters = [{'id': 1, 'name': 'Blue dragon', 'level': 111, 'defensive': {'stab': 20}},
                    {'id': 2, 'name': 'Blue dragon', 'level': 111, 'defensive': {'stab': 40}}]
        self.assertEqual(resolve(monsters, {'name': 'Blue dragon'})['automaticIds'], [])

    def test_multiple_levels_stay_unresolved(self):
        self.assertEqual(resolve([{'id': 1, 'name': 'Ankou', 'level': 75},
                                  {'id': 2, 'name': 'Ankou', 'level': 82}],
                                 {'name': 'Ankou'})['automaticIds'], [])

    def test_interchangeable_models_share_one_combat_profile(self):
        rows = [{'id': 1, 'name': 'Kurask', 'level': 106, 'version': 'Big'},
                {'id': 2, 'name': 'Kurask', 'level': 106, 'version': 'Normal'}]
        self.assertEqual(resolve(rows, {'name': 'Kurask'})['automaticIds'], [1, 2])

    def test_generic_family_never_guesses_species_location(self):
        self.assertEqual(resolve([{'id': 1, 'name': 'Mountain troll', 'level': 71}],
                                 {'name': 'Mountain troll', 'automatic': False})['automaticIds'], [])

    def test_post_quest_repeatable_boss_is_not_a_quest_fixture(self):
        rows = [{'id': 1, 'name': 'Amoxliatl', 'level': 263, 'version': 'Post-quest'},
                {'id': 2, 'name': 'Amoxliatl', 'level': 226, 'version': 'Quest'}]
        self.assertEqual(resolve(rows, {'name': 'Amoxliatl'})['automaticIds'], [1])

    def test_unrelated_names_quest_idle_and_whirlpool_are_not_propagated(self):
        rows = [{'id': 1, 'name': 'Kraken', 'level': 291},
                {'id': 2, 'name': 'Kraken', 'level': 0, 'version': 'Whirlpool'},
                {'id': 3, 'name': 'Kraken', 'level': 291, 'version': 'Quest'},
                {'id': 4, 'name': 'Kraken', 'level': 291, 'version': 'Idle'},
                {'id': 5, 'name': 'Armoured kraken', 'level': 180}]
        self.assertEqual(resolve(rows, {'name': 'Kraken'})['automaticIds'], [1])


if __name__ == '__main__':
    unittest.main()
