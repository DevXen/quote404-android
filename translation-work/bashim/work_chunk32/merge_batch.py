import json, sys, re
b = int(sys.argv[1]); s=b*100
src=[]
with open('/home/hatch/workspace/translation-work/bashim/chunks/chunk_32.jsonl', encoding='utf-8') as f:
    for line in f: src.append(json.loads(line))
batch = src[s:s+100]
tp = f'/home/hatch/workspace/translation-work/bashim/work_chunk32/trans/batch_{b}.txt'
raw = open(tp, encoding='utf-8').read()
if raw.endswith('\n'): raw = raw[:-1]
pieces = raw.split('\n<<<SPLIT>>>\n')
assert len(pieces)==100, f"expected 100 translations, got {len(pieces)}"
out=[]
problems=[]
for r,en in zip(batch,pieces):
    ru = r['text']
    if not en.strip(): problems.append((r['id'],'EMPTY EN'))
    if en.count('\n') != ru.count('\n'): problems.append((r['id'], f"newline parity ru={ru.count(chr(10))} en={en.count(chr(10))}"))
    # nick token check: every <...> in ru that is not clearly descriptive must appear byte-identical in en
    for tok in re.findall(r'<[^<>\n]{1,60}>', ru):
        low = tok.lower()
        descriptive = any(w in low for w in ['реклама','advertisement','проходит','минут','пауза','анекдот','конец','начало','далее','продолжение','занавес'])
        if not descriptive and tok not in en:
            problems.append((r['id'], f"token missing in en: {tok!r}"))
    out.append({"id": r['id'], "ru": ru, "en": en})
if problems:
    print("PROBLEMS:")
    for p in problems: print(" ", p)
    sys.exit(1)
with open('/home/hatch/workspace/translation-work/bashim/out/chunk_32.jsonl','a',encoding='utf-8') as f:
    for o in out:
        f.write(json.dumps(o, ensure_ascii=False)+'\n')
print(f"batch {b}: appended {len(out)} records, all validations passed")
