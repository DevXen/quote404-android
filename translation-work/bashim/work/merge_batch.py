"""Merge my translation file into out/chunk_21.jsonl (append mode).

My translation file format (work/b21_tr.txt):
  ###ID###<id>
  <translation lines; speaker lines start with '§ ' — the § is replaced
   by the exact original nick token + original separator>
  ###END###
  ...repeated...

OVERRIDES: {(id, line_no): 'notoken'} -> treat an extractor-found token line
as a regular line (translate fully). The translation line then has no §.
"""
import json, re, sys
sys.path.insert(0, '/home/hatch/workspace/translation-work/bashim/work')
from nickutil import record_tokens

BASE = '/home/hatch/workspace/translation-work/bashim'


def parse_tr(path):
    recs = []
    cur_id, cur_lines = None, None
    with open(path, encoding='utf-8') as f:
        for raw in f:
            line = raw.rstrip('\n')
            if line.startswith('###ID###'):
                cur_id, cur_lines = int(line[8:].split()[0]), []
            elif line == '###END###':
                recs.append((cur_id, cur_lines))
                cur_id, cur_lines = None, None
            else:
                assert cur_lines is not None, 'line outside record'
                cur_lines.append(line)
    assert cur_id is None, 'unterminated record'
    return recs

def main():
    chunk = [json.loads(l) for l in open(BASE + '/chunks/chunk_21.jsonl', encoding='utf-8')]
    by_id = {r['id']: r['text'] for r in chunk}
    tr_recs = parse_tr(BASE + '/work/b21_tr.txt')
    out_lines = []
    for rid, tlines in tr_recs:
        assert rid in by_id, f'unknown id {rid}'
        src = by_id[rid]
        src_lines = src.split('\n')
        assert len(tlines) == len(src_lines), \
            f'id {rid}: line count {len(tlines)} != {len(src_lines)}'
        assert '§' not in src, f'id {rid}: source contains §'
        toks = record_tokens(src, rid)
        mark_lines = [i for i, l in enumerate(tlines) if l.startswith('§')]
        assert [ln for ln, _, _ in toks] == mark_lines, \
            f'id {rid}: token lines {[ln for ln,_,_ in toks]} != § lines {mark_lines}'
        en_lines = []
        ti = 0
        for i, l in enumerate(tlines):
            if i in mark_lines:
                ln, tok, sep = toks[ti]; ti += 1
                rest = l[1:]
                if rest.startswith(' '):
                    rest = rest[1:]
                en_lines.append(tok + sep + rest)
            else:
                assert not l.startswith('§'), f'id {rid} line {i}: stray §'
                en_lines.append(l)
        en = '\n'.join(en_lines)
        assert en.strip(), f'id {rid}: empty en'
        assert en.count('\n') == src.count('\n'), f'id {rid}: newline mismatch'
        out_lines.append(json.dumps({'id': rid, 'ru': src, 'en': en},
                                    ensure_ascii=False))
    with open(BASE + '/out/chunk_21.jsonl', 'a', encoding='utf-8') as f:
        for l in out_lines:
            f.write(l + '\n')
    print(f'merged {len(out_lines)} records')

if __name__ == '__main__':
    main()
