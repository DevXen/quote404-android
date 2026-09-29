"""Final verification for chunk_44 output (works on partial output too)."""
import json, re, sys
sys.path.insert(0, '/home/hatch/workspace/translation-work/bashim/work')
from nickutil import line_token

BASE = '/home/hatch/workspace/translation-work/bashim'

# (id, 0-based line) -> 'notoken': extractor found/would-find a token here but
# the line is a narrative header or stage direction, translated in full.
CH44_NOTOKEN = {
    (426125, 0),  # "С ВК Европа+" source header
    (426140, 1),  # "*жуткий гул...*" stage direction
    (426162, 0),  # "С форума:" source header
    (426197, 0),  # "Звонок:" header
    (426202, 0),  # "Вконтакте:" source header
    (426186, 1),  # "демократия - пока лучшее..." prose, not a speaker
    (426245, 0),  # "C Пикабу:" source header
    (426270, 2),  # "Решение:" ticket field label
    (426282, 0),  # "Обращается сотрудница (С):" narrative lead-in
    (426296, 3),  # "Достоинства:" review field label
    (426296, 4),  # "Недостатки:" review field label
    (426296, 5),  # "Комментарий:" review field label
    (426302, 0),  # 'Пересматривая "Аватар":' header
    (426312, 0),  # "Новость:" news label
    (426312, 1),  # "Комент:" news label
    (426319, 4),  # "Коммент:" wall-post label
    (426232, 5),  # "ответ - укладываюсь..." narrative, not a speaker
    (426243, 1),  # 'Первый камент: "Настучал"' narrative header
    (426380, 1),  # "Достоинства:" review field label
    (426380, 3),  # "Недостатки:" review field label
    (426402, 1),  # "Коммент:" photo label
    (426411, 0),  # "С ВиО:" Q&A source header
    (426393, 3),  # "мой рецепт:" narrative, not a speaker
    (426453, 1),  # "Комментарии:" comment-section label
    (426470, 0),  # "Из ЖЖ:" source header
    (426475, 0),  # "Из ВК: " source header
    (426437, 0),  # "новости на ixbt:" source header
    (426475, 3),  # "Реальная ситуация:" narrative lead-in
    (426483, 7),  # "* Во время..." footnote bullet, not a speaker
    (426529, 1),  # "Избранные темы:" topic-list label
    (426547, 0),  # "ВК:" source header
    (426540, 1),  # "Требования:" field label
    (426540, 11),  # "четкость -" mid-sentence dash, not a speaker
    (426544, 1),  # "Порадовало:" narrative lead-in
    (426551, 2),  # "Вот думаю:" narrative lead-in
    (426719, 0),  # "*обсуждения..." list lead-in, not a speaker
    (426784, 7),  # "государство -" mid-sentence dash, not a speaker
    (426974, 1),  # "Мир -" mid-sentence dash, not a speaker
    (427091, 11),  # "в таком случае:" conversational lead-in, not a speaker
    (427091, 14),  # "С днем рождения :" extractor split on smiley, not a speaker
}

# (id, line, exact_prefix): speaker prefixes the extractor cannot match
# (apostrophes, stray punctuation, spaced nicks) — must stay byte-identical.
MANUAL_PREFIXES = [
    (426228, 0, 'хxx:'), (426228, 1, 'хxx:'), (426228, 2, 'хxx:'),
    (426228, 3, 'хxx:'), (426228, 4, 'хxx:'),
    (426247, 0, "Don't panic!:"), (426247, 14, "Don't panic!:"),
    (426264, 0, "Ro'Ongshn:"), (426264, 1, "Ro'Ongshn:"),
    (426250, 3, 'Arvicco"'),
    (426295, 0, 'С и н о п т и к:'),
    (426329, 2, 'М.:'), (426329, 4, 'М.:'),
    (426344, 0, 'Андрей Е.:'), (426344, 1, 'Константин:'),
    (426344, 2, 'Андрей Е.:'), (426344, 3, 'Константин:'),
]

# (id, line) single-word lines that are message CONTENT or cast markers kept
# byte-identical, so no standalone-word check needed beyond equality.
STANDALONE_IDENTICAL_OK = {
    # (426540, 4..9): "Сообразительность" etc. are translatable list items,
    # not nick/cast-marker lines — flagged by the standalone heuristic.
    (426540, 4), (426540, 5), (426540, 6),
    (426540, 7), (426540, 8), (426540, 9),
    # (426761, 0): "Фотофорум" translatable section header
    (426761, 0),
}

def record_tokens44(text, rid):
    out = []
    for i, line in enumerate(text.split('\n')):
        if (rid, i) in CH44_NOTOKEN:
            continue
        t = line_token(line)
        if t:
            out.append((i, t[0], t[1]))
    return out

def main():
    fails = []
    src_recs = [json.loads(l) for l in open(BASE + '/chunks/chunk_44.jsonl', encoding='utf-8')]
    out_lines = open(BASE + '/out/chunk_44.jsonl', encoding='utf-8').read().split('\n')
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
        for ln, tok, sep in record_tokens44(o['ru'], rid):
            eline = o['en'].split('\n')[ln]
            if not eline.startswith(tok):
                fails.append(f'id {rid} line {ln}: en missing token {tok!r}: {eline[:50]!r}')
        for (mid, mln, mpref) in MANUAL_PREFIXES:
            if mid == rid:
                mline = o['en'].split('\n')[mln]
                if not mline.startswith(mpref):
                    fails.append(f'id {rid} line {mln}: en missing manual prefix {mpref!r}: {mline[:50]!r}')
        rl, el = o['ru'].split('\n'), o['en'].split('\n')
        for j, (a, b) in enumerate(zip(rl, el)):
            if re.fullmatch(r'[^\s:;!?.,"\'<>\[\]{}()]{1,40}', a.strip()) and a.strip() \
                    and a.strip() != b.strip() and (rid, j) not in STANDALONE_IDENTICAL_OK:
                fails.append(f'id {rid} line {j}: standalone word differs ru={a.strip()!r} en={b.strip()!r}')
    if fails:
        print(f'FAIL ({len(fails)}):')
        for f in fails[:80]:
            print(' ', f)
    else:
        print(f'OK: {len(out_recs)}/{len(src_recs)} records verified')

if __name__ == '__main__':
    main()
