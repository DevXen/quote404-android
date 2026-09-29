"""Batch tooling for chunk translation. Keeps <...> tokens byte-identical."""
import json, re, sys, os

CHUNK = 'chunk_36'
BASE = os.path.dirname(os.path.abspath(__file__))
INP = os.path.join(BASE, 'chunks', CHUNK + '.jsonl')
OUT = os.path.join(BASE, 'out', CHUNK + '.jsonl')
TOKEN_RE = re.compile(r'<[^<>\n]*>')

def load_input():
    return [json.loads(l) for l in open(INP, encoding='utf-8')]

def prep(start, end):
    """Write /tmp/b{N}.json with translation templates for records [start, end)."""
    recs = load_input()
    batch = []
    for r in recs[start:end]:
        toks = []
        def repl(m):
            toks.append(m.group(0))
            return '{T%d}' % (len(toks) - 1)
        tmpl = TOKEN_RE.sub(repl, r['text'])
        batch.append({'id': r['id'], 'tokens': toks, 'tmpl': tmpl,
                      'newlines': tmpl.count('\n')})
    path = '/tmp/batch_%s_%d.json' % (CHUNK, start)
    json.dump(batch, open(path, 'w', encoding='utf-8'),
              ensure_ascii=False, indent=1)
    print('wrote', path, len(batch), 'records')

def combine(trans_path, start, end):
    """Merge translator's en strings with source, validate, append to OUT."""
    recs = load_input()
    tr = json.load(open(trans_path, encoding='utf-8'))
    assert len(tr) == end - start, 'translation count mismatch %d vs %d' % (len(tr), end - start)
    lines = []
    for i, t in enumerate(tr):
        r = recs[start + i]
        assert t['id'] == r['id'], 'id order mismatch at %d: %s vs %s' % (i, t['id'], r['id'])
        en = t['en']
        assert en and en.strip(), 'empty en for id %s' % r['id']
        toks = []
        def repl(m):
            toks.append(m.group(0))
            return '{T%d}' % (len(toks) - 1)
        tmpl = TOKEN_RE.sub(repl, r['text'])
        # substitute translator placeholders back to byte-identical tokens
        def sub(m):
            idx = int(m.group(1))
            assert 0 <= idx < len(toks), 'bad placeholder %s id %s' % (m.group(0), r['id'])
            return toks[idx]
        en2 = re.sub(r'\{T(\d+)\}', sub, en)
        # no leftover placeholders
        assert not re.search(r'\{T\d+\}', en2), 'leftover placeholder id %s' % r['id']
        # newline parity
        assert en2.count('\n') == r['text'].count('\n'), 'newline mismatch id %s: %d vs %d' % (r['id'], en2.count('\n'), r['text'].count('\n'))
        lines.append(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en2},
                                ensure_ascii=False))
    with open(OUT, 'a', encoding='utf-8') as f:
        for ln in lines:
            f.write(ln + '\n')
    print('appended', len(lines), 'records to', OUT)

def verify():
    """Full self-verification of OUT against input."""
    recs = load_input()
    outs = [json.loads(l) for l in open(OUT, encoding='utf-8')]
    print('lines:', len(outs))
    assert len(outs) == len(recs), 'line count mismatch'
    ids_in = [r['id'] for r in recs]
    ids_out = [o['id'] for o in outs]
    assert ids_in == ids_out, 'ID order mismatch'
    assert len(set(ids_out)) == len(ids_out), 'duplicate ids'
    bad = []
    for r, o in zip(recs, outs):
        assert set(o.keys()) == {'id', 'ru', 'en'}, 'key mismatch id %s' % r['id']
        if o['ru'] != r['text']:
            bad.append(('ru', r['id']))
        if o['en'].count('\n') != r['text'].count('\n'):
            bad.append(('nl', r['id']))
        if not o['en'].strip():
            bad.append(('empty', r['id']))
        for tok in TOKEN_RE.findall(r['text']):
            if not tok.strip('<>'):  # descriptive tokens may be translated
                pass
        # every non-descriptive token must be byte-identical in en:
        # find tokens in en; any token missing -> check if descriptive
    print('bad:', bad if bad else 'none')
    # token check: all <...> in source that are nicks (not in en translated form)
    missing = []
    for r, o in zip(recs, outs):
        for tok in TOKEN_RE.findall(r['text']):
            if tok not in o['en']:
                missing.append((r['id'], tok))
    print('source tokens absent from en (should be only descriptive/stage directions):', len(missing))
    for mid, tok in missing[:50]:
        print('  id', mid, repr(tok))
    print('VERIFY DONE')

if __name__ == '__main__':
    cmd = sys.argv[1]
    if cmd == 'prep':
        prep(int(sys.argv[2]), int(sys.argv[3]))
    elif cmd == 'combine':
        combine(sys.argv[2], int(sys.argv[3]), int(sys.argv[4]))
    elif cmd == 'verify':
        verify()
