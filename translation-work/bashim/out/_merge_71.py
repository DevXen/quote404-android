import json, sys

SRC = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_71.jsonl'
OUT = '/home/hatch/workspace/translation-work/bashim/out/chunk_71.jsonl'
TRANS_PATH = sys.argv[1]

src_records = [json.loads(l) for l in open(SRC, encoding='utf-8') if l.strip()]
ns = {}
exec(open(TRANS_PATH, encoding='utf-8').read(), ns)
TRANS = ns['TRANS']

missing = [r['id'] for r in src_records if r['id'] not in TRANS and False]  # placeholder
written = 0
with open(OUT, 'a', encoding='utf-8') as f:
    for r in src_records:
        if r['id'] in TRANS:
            en = TRANS[r['id']]
            assert isinstance(en, str) and en.strip(), f"empty en for {r['id']}"
            assert en.count('\n') == r['text'].count('\n'), f"newline mismatch for {r['id']}"
            f.write(json.dumps({"id": r['id'], "ru": r['text'], "en": en}, ensure_ascii=False) + '\n')
            written += 1
print(f"wrote {written} from {TRANS_PATH}")
