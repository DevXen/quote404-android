import json, sys
b = int(sys.argv[1]); s=b*100
recs=[]
with open('/home/hatch/workspace/translation-work/bashim/chunks/chunk_32.jsonl', encoding='utf-8') as f:
    for line in f: recs.append(json.loads(line))
for i,r in enumerate(recs[s:s+100]):
    print(f"===== [{i}] id={r['id']} =====")
    print(r['text'])
    print()
