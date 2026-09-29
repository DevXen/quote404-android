"""Pre-merge check: line counts and §-alignment of b21_tr.txt vs source batch."""
import json, sys, re
sys.path.insert(0, '/home/hatch/workspace/translation-work/bashim/work')
from nickutil import record_tokens
BASE = '/home/hatch/workspace/translation-work/bashim'
START, END = int(sys.argv[1]), int(sys.argv[2])
recs = [json.loads(l) for l in open(BASE + '/chunks/chunk_21.jsonl', encoding='utf-8')]
batch = {r['id']: r['text'] for r in recs[START:END]}
# parse tr file
tr = {}
cur_id, cur_lines = None, []
for line in open(BASE + '/work/b21_tr.txt', encoding='utf-8'):
    line = line.rstrip('\n')
    if line.startswith('###ID###'):
        assert cur_id is None, f'unterminated {cur_id}'
        cur_id = int(line[8:].split()[0]); cur_lines = []
    elif line == '###END###':
        tr[cur_id] = cur_lines; cur_id = None
    elif cur_id is not None:
        cur_lines.append(line)
assert cur_id is None, f'unterminated {cur_id}'
errs = 0
for rid, src in batch.items():
    if rid not in tr:
        print(f'MISSING {rid}'); errs += 1; continue
    sl, tl = src.split('\n'), tr[rid]
    if len(sl) != len(tl):
        print(f'LINES {rid}: src {len(sl)} != tr {len(tl)}'); errs += 1; continue
    toks = [ln for ln, _, _ in record_tokens(src, rid)]
    marks = [i for i, l in enumerate(tl) if l == '§' or l.startswith('§ ')]
    if toks != marks:
        print(f'MARKS {rid}: tokens {toks} != § {marks}'); errs += 1
extra = set(tr) - set(batch)
if extra: print('EXTRA:', sorted(extra)); errs += 1
print('precheck', 'OK' if errs == 0 else f'{errs} ERRORS')
