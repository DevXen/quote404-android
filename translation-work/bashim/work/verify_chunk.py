"""Final verification for chunk_21 output. Works on partial output too."""
import json, re, sys
sys.path.insert(0, '/home/hatch/workspace/translation-work/bashim/work')
from nickutil import record_tokens

BASE = '/home/hatch/workspace/translation-work/bashim'
fails = []

src_recs = [json.loads(l) for l in open(BASE + '/chunks/chunk_21.jsonl', encoding='utf-8')]
out_lines = open(BASE + '/out/chunk_21.jsonl', encoding='utf-8').read().split('\n')
if out_lines and out_lines[-1] == '':
    out_lines.pop()

out_recs = []
for i, l in enumerate(out_lines):
    try:
        r = json.loads(l)
    except Exception as e:
        fails.append(f'line {i}: invalid JSON: {e}')
        continue
    if list(r.keys()) != ['id', 'ru', 'en']:
        fails.append(f'line {i}: keys != [id,ru,en]: {list(r.keys())}')
    out_recs.append(r)

seen = set()
# (id, line): single-word lines that are message CONTENT, not nicks — safe to translate
STANDALONE_CONTENT = {
    (403512, 27),  # "читер" is yyy's message "cheater!", not a nick
    (403633, 3),  # "поезда" rhyme word transliterated to "poyezda" (rhymes with "zvezda")
    (403633, 4),  # "езда" rhyme word transliterated to "yezda" (rhymes with "zvezda")
}
for i, o in enumerate(out_recs):
    s = src_recs[i] if i < len(src_recs) else None
    if s is None or o['id'] != s['id']:
        fails.append(f'line {i}: id {o["id"]} != expected {s["id"] if s else None} (order break)')
        continue
    if o['id'] in seen:
        fails.append(f'duplicate id {o["id"]}')
    seen.add(o['id'])
    rid = o['id']
    if o['ru'] != s['text']:
        fails.append(f'id {rid}: ru != source text')
    if not o['en'] or not o['en'].strip():
        fails.append(f'id {rid}: empty en')
    if o['ru'].count('\n') != o['en'].count('\n'):
        fails.append(f'id {rid}: newline count ru={o["ru"].count(chr(10))} en={o["en"].count(chr(10))}')
    ru_t = re.findall(r'<[^>\n]*>', o['ru'])
    en_t = re.findall(r'<[^>\n]*>', o['en'])
    if ru_t != en_t:
        fails.append(f'id {rid}: <...> token mismatch ru={ru_t} en={en_t}')
    for ln, tok, sep in record_tokens(o['ru'], rid):
        eline = o['en'].split('\n')[ln]
        if not eline.startswith(tok):
            fails.append(f'id {rid} line {ln}: en missing token {tok!r}: {eline[:40]!r}')
    rl, el = o['ru'].split('\n'), o['en'].split('\n')
    for j, (a, b) in enumerate(zip(rl, el)):
        if re.fullmatch(r'[^\s:;!?.,"\'<>\[\]{}()]{1,40}', a.strip()) and a.strip() \
                and a.strip() != b.strip() and (rid, j) not in STANDALONE_CONTENT:
            fails.append(f'id {rid} line {j}: standalone word differs ru={a.strip()!r} en={b.strip()!r}')

if fails:
    print(f'FAIL ({len(fails)}):')
    for f in fails[:60]:
        print(' ', f)
else:
    print(f'OK: {len(out_recs)}/{len(src_recs)} records verified')
