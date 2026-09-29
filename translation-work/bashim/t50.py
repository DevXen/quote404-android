#!/usr/bin/env python3
"""Helper for translating chunk_50 in 10 batches of 100.
Usage: python3 t50.py prep      -> writes /tmp/t50_B0..B9.json templates
       python3 t50.py combine   -> validates translations in /tmp/t50_en_B*.json
                                   and writes out/chunk_50.jsonl
Translator workflow: read /tmp/t50_Bk.json (fields: id, tokens, tmpl, newlines),
write /tmp/t50_en_Bk.json as a JSON list of {"id": N, "en": "..."} in same order,
using {T0}..{Tn} placeholders where the source had <...> tokens.
"""
import json, re, sys, os

BASE = '/home/hatch/workspace/translation-work/bashim'
INP = os.path.join(BASE, 'chunks', 'chunk_50.jsonl')
OUT = os.path.join(BASE, 'out', 'chunk_50.jsonl')
TOKEN_RE = re.compile(r'<[^<>\n]*>')

def load_input():
    return [json.loads(l) for l in open(INP, encoding='utf-8')]

def prep():
    recs = load_input()
    for b in range(10):
        batch = []
        for r in recs[b*100:(b+1)*100]:
            toks = []
            def repl(m):
                toks.append(m.group(0))
                return '{T%d}' % (len(toks) - 1)
            tmpl = TOKEN_RE.sub(repl, r['text'])
            batch.append({'id': r['id'], 'tokens': toks, 'tmpl': tmpl,
                          'newlines': tmpl.count('\n')})
        path = '/tmp/t50_B%d.json' % b
        json.dump(batch, open(path, 'w', encoding='utf-8'),
                  ensure_ascii=False, indent=1)
        print('wrote', path, len(batch), 'records')

def combine():
    recs = load_input()
    lines = []
    for b in range(10):
        tpath = '/tmp/t50_en_B%d.json' % b
        tr = json.load(open(tpath, encoding='utf-8'))
        assert len(tr) == 100, 'count mismatch batch %d: %d' % (b, len(tr))
        for i, t in enumerate(tr):
            r = recs[b*100 + i]
            assert t['id'] == r['id'], 'id order mismatch b%d i%d' % (b, i)
            en = t['en']
            assert en and en.strip(), 'empty en id %s' % r['id']
            toks = []
            def repl(m):
                toks.append(m.group(0))
                return '{T%d}' % (len(toks) - 1)
            src_tmpl = TOKEN_RE.sub(repl, r['text'])
            # all placeholders used in en must be valid; each token should appear
            for m in re.finditer(r'\{T(\d+)\}', en):
                assert 0 <= int(m.group(1)) < len(toks), 'bad placeholder id %s' % r['id']
            en2 = re.sub(r'\{T(\d+)\}',
                         lambda m: toks[int(m.group(1))], en)
            assert len(toks) == src_tmpl.count('{T'), 'token count'
            # newline parity
            assert en2.count('\n') == src_tmpl.count('\n'), \
                'newline mismatch id %s: %d vs %d' % (r['id'], en2.count('\n'), src_tmpl.count('\n'))
            lines.append(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en2},
                                    ensure_ascii=False))
    assert len(lines) == 1000
    keys_ok = all(set(json.loads(l).keys()) == {'id', 'ru', 'en'} for l in lines)
    assert keys_ok, 'key mismatch'
    open(OUT, 'w', encoding='utf-8').write('\n'.join(lines) + '\n')
    print('wrote', OUT, len(lines), 'lines')

if __name__ == '__main__':
    if sys.argv[1] == 'prep':
        prep()
    elif sys.argv[1] == 'combine':
        combine()
    else:
        print('usage: prep|combine')
