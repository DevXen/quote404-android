import json, re, sys

CHUNK = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_69.jsonl'
OUT = '/home/hatch/workspace/translation-work/bashim/out/chunk_69.jsonl'

def merge(batch_module, id_list):
    import importlib
    mod = importlib.import_module(batch_module)
    EN = mod.EN
    src = {}
    order = []
    with open(CHUNK, encoding='utf-8') as f:
        for line in f:
            r = json.loads(line)
            src[r['id']] = r
            order.append(r['id'])
    errs = []
    lines = []
    for i in id_list:
        if i not in src:
            errs.append(f'{i}: id not in source'); continue
        if i not in EN:
            errs.append(f'{i}: missing translation'); continue
        ru = src[i]['text']
        en = EN[i]
        if not en or not en.strip():
            errs.append(f'{i}: empty en')
        if en.count('\n') != ru.count('\n'):
            errs.append(f"{i}: newline mismatch ru={ru.count(chr(10))} en={en.count(chr(10))}")
        for tok in set(re.findall(r'<[^<>\n]*>', ru)):
            if tok not in en:
                errs.append(f'{i}: token {tok!r} missing in en')
        # ru byte-identity check via json round trip
        obj = {"id": i, "ru": ru, "en": en}
        s = json.dumps(obj, ensure_ascii=False)
        back = json.loads(s)
        if back['ru'] != ru:
            errs.append(f'{i}: ru round-trip mismatch')
        if list(back.keys()) != ['id','ru','en']:
            errs.append(f'{i}: key order wrong')
        lines.append(s)
    if errs:
        print('ERRORS:')
        for e in errs: print(' ', e)
        sys.exit(1)
    with open(OUT, 'a', encoding='utf-8') as f:
        for s in lines:
            f.write(s + '\n')
    print(f'appended {len(lines)} records OK')

if __name__ == '__main__':
    mod = sys.argv[1]
    ids = [int(x) for x in sys.argv[2].split(',')]
    merge(mod, ids)
