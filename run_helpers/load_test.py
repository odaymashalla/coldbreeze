import unittest
from random import random
from funkload.FunkLoadTestCase import FunkLoadTestCase

class Highload(FunkLoadTestCase):
    def setUp(self):
        self.server_url = self.conf_get('main', 'url')

    def test_simple(self):
        server_url = self.server_url
        res = self.get(server_url, description='Get url')
        self.assertEqual(res.code, 200)
        

if __name__ in ('main', '__main__'):
    unittest.main()