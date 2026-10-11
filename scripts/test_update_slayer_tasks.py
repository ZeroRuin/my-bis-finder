import unittest
from update_slayer_tasks import parse


class TaskParserTest(unittest.TestCase):
    @staticmethod
    def source():
        return '\n'.join(f'TASK_{i}("Task {i}", ItemID.ITEM),' for i in range(100)) + '''
GREATER_DEMONS("Greater demons", ItemID.ITEM, "K'ril Tsutsaroth", "Tormented Demon"),
BLUE_DRAGONS("Blue dragons", ItemID.ITEM, "Vorkath"),
HELLHOUNDS("Hellhounds", ItemID.ITEM, "Cerberus"),
GARGOYLES("Gargoyles", ItemID.ITEM, 9, ItemID.HAMMER, "Dawn", "Dusk");'''

    def test_alternatives_and_weakness_parameters(self):
        tasks = parse(self.source())
        self.assertEqual(tasks['Greater demons'], ["K'ril Tsutsaroth", 'Tormented Demon'])
        self.assertEqual(tasks['Gargoyles'], ['Dawn', 'Dusk'])
        self.assertEqual(len(tasks), 104)

    def test_reject_incomplete_source(self):
        with self.assertRaises(ValueError):
            parse('TASK("Greater demons", ItemID.ITEM);')

    def test_reject_duplicate_task(self):
        with self.assertRaises(ValueError):
            parse(self.source() + '\nDUPLICATE("Hellhounds", ItemID.ITEM);')

    def test_reject_changed_format(self):
        with self.assertRaises(ValueError):
            parse(self.source() + '\nNEW_TASK(NAME_CONSTANT, ItemID.ITEM);')


if __name__ == '__main__':
    unittest.main()
