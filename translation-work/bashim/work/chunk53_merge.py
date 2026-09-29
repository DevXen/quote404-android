#!/usr/bin/env python3
"""Chunk 53 worker helper.
1. Dump a source batch to a readable display file:  chunk53_merge.py dump <start0> <end0>
2. Parse translator's marker file (@@@<id>@@@ then translation) to JSON and run merge_chunk.py:
   chunk53_merge.py merge <markerfile>
Marker file: lines of the form @@@123456@@@ followed by the English translation
(real newlines). Translation ends at next marker or EOF.
"""
import json, os, re, subprocess, sys

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
INP = os.path.join(BASE, 'chunks', 'chunk_53.jsonl')

def dump(a, b):
    src = [json.loads(l) for l in open(INP, encoding='utf-8')]
    path = os.path.join(BASE, 'work', f'chunk53_src_{a:04d}_{b:04d}.txt')
    with open(path, 'w', encoding='utf-8') as f:
        for i, r in enumerate(src[a:b], start=a + 1):
            f.write(f"===== [{i}] id={r['id']} date={r['date']} rating={r['rating']} =====\n")
            f.write(r['text'] + "\n")
    print('wrote', path, 'records', a + 1, '..', b)

def merge(marker):
    src = open(marker, encoding='utf-8').read().splitlines(keepends=True)
    cur = None
    buf = []
    tr = {}
    pat = re.compile(r'^@@@(\d+)@@@\s*$')
    for line in src:
        m = pat.match(line.rstrip('\n'))
        if m:
            if cur is not None:
                en = ''.join(buf)
                if en.endswith('\n'):
                    en = en[:-1]
                tr[int(cur)] = en
            cur = m.group(1)
            buf = []
        else:
            if cur is None:
                raise SystemExit('content before first marker')
            buf.append(line)
    if cur is not None:
        en = ''.join(buf)
        if en.endswith('\n'):
            en = en[:-1]
        tr[int(cur)] = en
    jpath = marker + '.json'
    with open(jpath, 'w', encoding='utf-8') as f:
        json.dump({str(k): v for k, v in tr.items()}, f, ensure_ascii=False)
    print(f'parsed {len(tr)} translations -> {jpath}')
    r = subprocess.run([sys.executable, os.path.join(BASE, 'merge_chunk.py'), '53', jpath],
                       capture_output=True, text=True)
    print(r.stdout)
    print(r.stderr, file=sys.stderr)
    sys.exit(r.returncode)

if __name__ == '__main__':
    if sys.argv[1] == 'dump':
        dump(int(sys.argv[2]), int(sys.argv[3]))
    elif sys.argv[1] == 'merge':
        merge(sys.argv[2])
