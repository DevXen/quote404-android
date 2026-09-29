#!/usr/bin/env python3
"""Merge translations for a chunk_47 batch, validate hard rules, append to out file."""
import json, re, sys

CHUNK = sys.argv[1]          # e.g. chunk_47
BATCH = sys.argv[2]          # e.g. b1  -> tr_b1.json
LO, HI = int(sys.argv[3]), int(sys.argv[4])  # slice of input records

BASE = '/home/hatch/workspace/translation-work/bashim'
recs = [json.loads(l) for l in open(f'{BASE}/chunks/{CHUNK}.jsonl')]
batch = recs[LO:HI]
tr = json.load(open(f'{BASE}/work/{CHUNK}/tr_{BATCH}.json'))

def nick_token(ru_line):
    """Exact leading nick token: '<...>' or 'xxx:' style.
    Returns None for narrative/stage-direction labels that are legitimately
    translatable (single-letter dialogue labels, pronouns, site attributions)."""
    m = re.match(r'(<[^>\n]*>)', ru_line)
    if m: return m.group(1)
    m = re.match(r'(\S+:)', ru_line)
    if m:
        tok = m.group(1)
        bare = tok[:-1]
        # single-char tokens are dialogue labels (А/Б/В/О/B), not handles
        if len(bare) == 1:
            return None
        # translatable narrative labels, not speaker handles
        if bare in {'я','он','она','Хабр','Хабра','Хором','хор','Заявка','Тело','Ответ','Мать','Вопрос','начальник','Новость','Чел','Коллеги','Ожидание','Реальность','Топик'}:
            return None
        return tok
    return None

out_lines = []
errors = []
for r in batch:
    sid = str(r['id'])
    if sid not in tr:
        errors.append(f'{r["id"]}: missing translation'); continue
    en = tr[sid]
    # resolve tricky-nick placeholders <NK:id:lineidx> from exact source bytes
    def _nk(m):
        rid, li = m.group(1), int(m.group(2))
        src = next(x for x in batch if str(x['id']) == rid)
        tok = nick_token(src['text'].split('\n')[li])
        if not tok:
            errors.append(f'{rid}: placeholder <NK:{rid}:{li}> has no nick token')
            return m.group(0)
        return tok
    en = re.sub(r'<NK:(\d+):(\d+)>', _nk, en)
    if '<NICK170>' in en:  # legacy batch-1 placeholders
        tok = nick_token(r['text'].split('\n')[0])
        en = en.replace('<NICK170>', tok)
    if '<NICK177>' in en:
        toks = [nick_token(l) for l in r['text'].split('\n')]
        en = en.replace('<NICK177>', toks[2] or toks[3])
    ru = r['text']
    # rule 4: newline parity
    if en.count('\n') != ru.count('\n'):
        errors.append(f'{r["id"]}: newline parity {en.count(chr(10))} vs {ru.count(chr(10))}')
    # rule 5: no empty en
    if not en.strip():
        errors.append(f'{r["id"]}: empty en')
    # rule 2: every <...> token byte-identical
    for tok in re.findall(r'<[^>\n]*>', ru):
        if tok not in en:
            errors.append(f'{r["id"]}: token {tok!r} missing in en')
    # nick-prefix byte-identical per line
    rl, el = ru.split('\n'), en.split('\n')
    if len(rl) == len(el):
        for i, (a, b) in enumerate(zip(rl, el)):
            t = nick_token(a)
            if t and not b.startswith(t):
                errors.append(f'{r["id"]} line{i}: nick {t!r} != {b[:len(t)+8]!r}')
    out_lines.append(json.dumps({'id': r['id'], 'ru': ru, 'en': en}, ensure_ascii=False))

if errors:
    print('ERRORS:'); [print(' ', e) for e in errors]
    sys.exit(1)

with open(f'{BASE}/out/{CHUNK}.jsonl', 'a') as f:
    for l in out_lines:
        f.write(l + '\n')
print(f'OK: appended {len(out_lines)} records (ids {batch[0]["id"]}..{batch[-1]["id"]})')
