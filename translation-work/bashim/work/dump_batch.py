"""Dump a batch of records for translation + a skeleton marking speaker lines with §."""
import json, sys
sys.path.insert(0, '/home/hatch/workspace/translation-work/bashim/work')
from nickutil import record_tokens

START, END = int(sys.argv[1]), int(sys.argv[2])  # 0-based [START, END)
BASE = '/home/hatch/workspace/translation-work/bashim'
recs = [json.loads(l) for l in open(BASE + '/chunks/chunk_21.jsonl', encoding='utf-8')]
batch = recs[START:END]

with open(f'{BASE}/work/b21_src.txt', 'w', encoding='utf-8') as f, \
     open(f'{BASE}/work/b21_skel.txt', 'w', encoding='utf-8') as s:
    for r in batch:
        f.write(f'###ID###{r["id"]}\n{r["text"]}\n###END###\n')
        toks = record_tokens(r['text'], r['id'])
        lines = r['text'].split('\n')
        tokmap = {ln: (tok, sep) for ln, tok, sep in toks}
        s.write(f'###ID###{r["id"]}  [tokens: {len(toks)}]\n')
        for i, line in enumerate(lines):
            if i in tokmap:
                tok, sep = tokmap[i]
                s.write(f'§  <<<{tok!r}>>>\n')
            else:
                s.write('\n')
        s.write('###END###\n')
print(f'dumped {len(batch)} records')
