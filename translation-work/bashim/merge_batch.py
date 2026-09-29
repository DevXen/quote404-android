import json, re, sys

CHUNK = sys.argv[1]          # e.g. 26
START = int(sys.argv[2])     # record index start
TF = sys.argv[3]             # translation file with "### id=..." headers

base = '/home/hatch/workspace/translation-work/bashim'
recs = [json.loads(l) for l in open(f'{base}/chunks/chunk_{CHUNK}.jsonl', encoding='utf-8')]

# parse translation file
trans = {}
cur = None; buf = []
def flush():
    global cur, buf
    if cur is not None:
        s = '\n'.join(buf)
        if s.endswith('\n\n'): pass
        trans[cur] = s
    cur = None; buf = []
for line in open(TF, encoding='utf-8'):
    line = line.rstrip('\n')
    if line.startswith('### id='):
        flush()
        cur = int(line.split('=',1)[1]); buf = []
    else:
        buf.append(line)
flush()
# strip the single trailing blank line added by paragraph separator
for k in trans:
    if trans[k].endswith('\n'):
        trans[k] = trans[k][:-1]

nick = re.compile(r'<[^<>]*>')
out_lines = []
n = 0
for i in range(START, START + len([k for k in trans])):
    r = recs[i]
    rid = r['id']
    assert rid in trans, f'missing translation for id {rid} (index {i})'
    en = trans[rid]
    ru = r['text']
    assert en.strip(), f'empty en for id {rid}'
    assert ru.count('\n') == en.count('\n'), f'newline mismatch id {rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}'
    for tok in nick.findall(ru):
        assert tok in en, f'nick token {tok!r} missing in en id {rid}'
    out_lines.append(json.dumps({'id': rid, 'ru': ru, 'en': en}, ensure_ascii=False) + '\n')
    n += 1
with open(f'{base}/out/chunk_{CHUNK}.jsonl', 'a', encoding='utf-8') as out:
    out.writelines(out_lines)
print(f'appended {n} records, total out lines:')
import subprocess
print(subprocess.run(['wc','-l',f'{base}/out/chunk_{CHUNK}.jsonl'],capture_output=True,text=True).stdout.strip())
