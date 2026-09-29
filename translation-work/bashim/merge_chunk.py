#!/usr/bin/env python3
"""Merge translated batch into chunk output with full validation.
Usage: merge_chunk.py <chunknum> <translations.json>
translations.json: { "<id>": "<en text>", ... }

SAFETY RULE: the whole batch is validated BEFORE anything is appended.
Any validation error aborts the run with zero appends -- a failed record
must never leave later records appended out of order.
"""
import json, re, sys, os
from collections import Counter

chunknum = sys.argv[1]
tr_path = sys.argv[2]
base = os.path.dirname(os.path.abspath(__file__))
inp = os.path.join(base, 'chunks', f'chunk_{chunknum}.jsonl')
outp = os.path.join(base, 'out', f'chunk_{chunknum}.jsonl')
os.makedirs(os.path.dirname(outp), exist_ok=True)

src = [json.loads(l) for l in open(inp, encoding='utf-8')]
tr = json.load(open(tr_path, encoding='utf-8'))
# keys may be str; normalize
tr = {int(k): v for k, v in tr.items()}

done = set()
if os.path.exists(outp):
    for l in open(outp, encoding='utf-8'):
        l = l.strip()
        if l:
            done.add(json.loads(l)['id'])

tok = re.compile(r'<[^>\n]*>')
errors = []
validated = []  # (record, en) in source order
for r in src:
    i = r['id']
    if i in done:
        continue
    if i not in tr:
        break  # resume point: remaining must be in input order
    en = tr[i]
    ru = r['text']
    if not isinstance(en, str) or not en.strip():
        errors.append((i, 'empty en')); continue
    if en.count('\n') != ru.count('\n'):
        errors.append((i, f"newline parity {en.count(chr(10))} vs {ru.count(chr(10))}")); continue
    st = tok.findall(ru); et = tok.findall(en)
    cs, ce = Counter(st), Counter(et)
    missing = [t for t in cs if ce[t] < cs[t]]
    if missing:
        errors.append((i, f'missing <...> tokens: {missing[:3]}')); continue
    validated.append((r, en))

if errors:
    print(f'ABORTED: {len(validated)} validated, {len(errors)} errors -- nothing appended')
    for i, e in errors[:20]:
        print('ERR', i, e)
    sys.exit(1)

with open(outp, 'a', encoding='utf-8') as f:
    for r, en in validated:
        f.write(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en}, ensure_ascii=False) + '\n')

print(f'written={len(validated)} errors=0')
