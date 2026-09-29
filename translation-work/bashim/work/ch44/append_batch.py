"""Append one translated batch to out/chunk_44.jsonl (creates file if needed)."""
import json, os, sys

BASE = '/home/hatch/workspace/translation-work/bashim'

def main(batch):
    src = [json.loads(l) for l in open(BASE + '/chunks/chunk_44.jsonl', encoding='utf-8')]
    en_map = json.load(open(f'{BASE}/work/ch44/en_b{batch}.json', encoding='utf-8'))
    recs = src[batch * 100:(batch + 1) * 100]
    outp = BASE + '/out/chunk_44.jsonl'
    have = 0
    if os.path.exists(outp):
        have = sum(1 for l in open(outp, encoding='utf-8') if l.strip())
    if have != batch * 100:
        sys.exit(f'REFUSE: out has {have} records, expected {batch * 100} before appending batch {batch}')
    lines = []
    for r in recs:
        rid = str(r['id'])
        en = en_map.get(rid)
        if en is None or not en.strip():
            sys.exit(f'REFUSE: missing/empty en for id {rid} in batch {batch}')
        lines.append(json.dumps({'id': r['id'], 'ru': r['text'], 'en': en}, ensure_ascii=False))
    with open(outp, 'a', encoding='utf-8') as f:
        for l in lines:
            f.write(l + '\n')
    print(f'batch {batch}: appended {len(lines)} records (total now {(batch + 1) * 100})')

if __name__ == '__main__':
    main(int(sys.argv[1]))
