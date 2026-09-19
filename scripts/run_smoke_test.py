#!/usr/bin/env python3
"""
Wally QualityGate Smoke Test Runner
Runs the exact test assertions mirroring QualityGateSmokeTest.kt
"""

def hamming_distance(hash1, hash2):
    if len(hash1) != len(hash2):
        return 999
    dist = 0
    for c1, c2 in zip(hash1, hash2):
        v1 = int(c1, 16)
        v2 = int(c2, 16)
        dist += bin(v1 ^ v2).count('1')
    return dist

class QualityGate:
    def __init__(self, min_long_edge=2560, min_ratio=0.40, max_ratio=0.62):
        self.min_long_edge = min_long_edge
        self.min_ratio = min_ratio
        self.max_ratio = max_ratio
        self.seen_hashes = set()
        self.seen_ids = set()

    def passes_quality(self, item):
        long_edge = max(item['width'], item['height'])
        if long_edge < self.min_long_edge:
            return False
        ratio = item['width'] / float(item['height'])
        if not (self.min_ratio <= ratio <= self.max_ratio):
            return False
        if item.get('source') == 'WALLHAVEN' and item.get('favorites', 0) < 5:
            return False
        if item.get('source') == 'UNSPLASH' and item.get('likes', 0) < 5:
            return False
        if item.get('source') == 'PIXABAY':
            if item.get('likes', 0) < 5 or (item['width'] * item['height']) < (1440 * 2560):
                return False
        return True

    def check_unique(self, item):
        cid = f"{item['source']}_{item['id']}"
        if cid in self.seen_ids:
            return False
        h = item.get('hash', '')
        if h:
            for existing in self.seen_hashes:
                if hamming_distance(h, existing) <= 4:
                    return False
            self.seen_hashes.add(h)
        self.seen_ids.add(cid)
        return True

def run_tests():
    qg = QualityGate()

    # Test 1: Valid high-res portrait wallpaper passes
    valid = {'id': '1', 'source': 'WALLHAVEN', 'width': 1440, 'height': 3200, 'favorites': 25, 'hash': '1234567890abcdef'}
    assert qg.passes_quality(valid), "Test 1 Failed: Valid item should pass"
    assert qg.check_unique(valid), "Test 1 Failed: Unique item should pass"

    # Test 2: Low-res rejected (<2560px)
    low_res = {'id': '2', 'source': 'WALLHAVEN', 'width': 1080, 'height': 1920, 'favorites': 50, 'hash': 'abcdef1234567890'}
    assert not qg.passes_quality(low_res), "Test 2 Failed: Low-res should be rejected"

    # Test 3: Landscape rejected
    land = {'id': '3', 'source': 'WALLHAVEN', 'width': 3840, 'height': 2160, 'favorites': 100, 'hash': 'fedcba0987654321'}
    assert not qg.passes_quality(land), "Test 3 Failed: Landscape should be rejected"

    # Test 4: Low engagement rejected
    low_eng = {'id': '4', 'source': 'WALLHAVEN', 'width': 1440, 'height': 2960, 'favorites': 2, 'hash': 'aabbccddeeff0011'}
    assert not qg.passes_quality(low_eng), "Test 4 Failed: Low engagement should be rejected"

    # Test 5: Duplicate perceptual hash detected
    dup = {'id': '5', 'source': 'UNSPLASH', 'width': 1440, 'height': 3200, 'likes': 20, 'hash': '1234567890abcdee'}
    assert qg.passes_quality(dup), "Test 5 item should pass quality floor"
    assert not qg.check_unique(dup), "Test 5 Failed: Duplicate hash should be rejected"

    print("All 5 QualityGate smoke tests PASSED successfully!")

if __name__ == "__main__":
    run_tests()
