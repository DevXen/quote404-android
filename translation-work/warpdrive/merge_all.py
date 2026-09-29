#!/usr/bin/env python3
"""Merge all chunk files into warpdrive_se_en.jsonl and validate."""
import json
import sys

BS = chr(92)
NL = chr(10)

# Load source numbers from DB dump
src_numbers = set()
src_texts = {}
with open('quotes_dump.tsv', encoding='utf-8') as f:
    for line in f:
        line = line.rstrip('\n')
        if not line:
            continue
        num, text = line.split('\t', 1)
        src_numbers.add(int(num))
        src_texts[int(num)] = text.replace(BS + 'n', NL)

print(f"Source: {len(src_numbers)} quotes")

# Merge chunks in order
CHUNKS = ['01','02','03','04','05','06','07','08','09','10','11','12','13','14','15','16','17','18','19','20','21','22','23','24']
all_recs = []
for c in CHUNKS:
    path = f'chunk_{c}.jsonl'
    try:
        with open(path, encoding='utf-8') as f:
            for i, line in enumerate(f, 1):
                try:
                    obj = json.loads(line)
                except json.JSONDecodeError as e:
                    print(f"FATAL: {path} line {i}: JSON error {e}")
                    sys.exit(1)
                # validate schema
                if set(obj.keys()) != {'n', 'en', 'sv'}:
                    print(f"FATAL: {path} line {i}: bad keys {set(obj.keys())}")
                    sys.exit(1)
                if not isinstance(obj['n'], int) or not obj['en'] or not isinstance(obj['en'], str):
                    print(f"FATAL: {path} line {i}: bad n/en")
                    sys.exit(1)
                if obj['sv'] is not None and not isinstance(obj['sv'], str):
                    print(f"FATAL: {path} line {i}: bad sv")
                    sys.exit(1)
                all_recs.append(obj)
    except FileNotFoundError:
        print(f"FATAL: {path} not found")
        sys.exit(1)
    print(f"  chunk_{c}: {len([r for r in all_recs])} total so far")

# Validate coverage
got_numbers = [r['n'] for r in all_recs]
got_set = set(got_numbers)
print(f"\nMerged: {len(all_recs)} records")
print(f"Unique numbers: {len(got_set)}")
print(f"Duplicates: {len(got_numbers) - len(got_set)}")

missing = src_numbers - got_set
extra = got_set - src_numbers
if missing:
    print(f"FATAL: Missing {len(missing)} numbers: {sorted(missing)[:20]}")
    sys.exit(1)
if extra:
    print(f"FATAL: Extra {len(extra)} numbers: {sorted(extra)[:20]}")
    sys.exit(1)
if len(got_numbers) != len(got_set):
    print("FATAL: Duplicate numbers present")
    sys.exit(1)

print("Coverage: OK - all 6025 source numbers present exactly once")

# Sort by quote number and write
all_recs.sort(key=lambda r: r['n'])
with open('warpdrive_se_en.jsonl', 'w', encoding='utf-8') as f:
    for r in all_recs:
        f.write(json.dumps(r, ensure_ascii=False) + '\n')

# Stats
sv_count = sum(1 for r in all_recs if r['sv'] is not None)
print(f"\nWrote warpdrive_se_en.jsonl: {len(all_recs)} records")
print(f"  With repaired Swedish (sv): {sv_count}")
print(f"  Null/untranslated (sv=null): {len(all_recs) - sv_count}")
print("\nDONE")
