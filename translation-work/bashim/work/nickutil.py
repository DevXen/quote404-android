"""Nick-token extraction shared by dump/skeleton/merge/verify for chunk_21."""
import re

BRACKET_COLON = re.compile(r'^(\s*<[^>\n]*>:)(\s*)')
BRACKET = re.compile(r'^(\s*<[^>\n]*>)(\s+)(?=\S)')
SQUARE = re.compile(r'^(\s*\[[^\]\s\n]*\][^\s:;!?.,"\'<>{}()]*\s*:)(\s*)(?=\S|$)')
PLAIN = r'[^\s:;!?,"\'<>\[\]{}()–—-]{1,40}'
QUAL = r'(?:\([^)\n]{1,40}\)|\{[^\}\n]{1,40}\})'
W = r'(?:' + PLAIN + r'(?:' + QUAL + r')*|' + QUAL + r')'
COLON = re.compile(r'^(\s*' + W + r'(?:\s+' + W + r'){0,2}\s*:)(?![\\/])(\s*)')
DASH = re.compile(r'^(\s*' + W + r'(?:\s+[-–—]|[-–—]\s+))(\s*)(?=\S)')

BRACE = re.compile(r'^(\s*\{[^\}\s\n]*\}[^\s:;!?,"\'<>\[\]()]*\s*:)(\s*)(?=\S|$)')
LCOLON = re.compile(r'^(\s*:[^:\n]{1,40}:)(\s*)')
QUOTE = re.compile(r'^(\s*>\s*' + W + r'(?:\s+' + W + r'){0,2}\s*:)(\s*)')
STAR = re.compile(r'^(\s*\*(?!\*)\s*' + W + r')(\s+)(?=\S)')
GT = re.compile(r'^(\s*\w{1,10}>)(\s*)(?=\S)')
NICKBR = re.compile(r'^(\s*' + PLAIN + r'(?:' + QUAL + r')*\s*\[[^\]\n]{1,40}\]\s*:)(\s*)')
HYPHEN = re.compile(r'^(\s*-?\w+(?:-\w+)+-?\s*:)(?![\\/])(\s*)(?=\S|$)')


def line_token(line):
    """Return (token, sep) if line starts with a speaker/nick token, else None.
    token = exact original prefix incl. any indent and colon/dash.
    sep = exact whitespace between token and the spoken text."""
    m = BRACE.match(line)
    if m:
        return m.group(1), m.group(2)
    m = LCOLON.match(line)
    if m:
        return m.group(1), m.group(2)
    m = QUOTE.match(line)
    if m:
        return m.group(1), m.group(2)
    m = STAR.match(line)
    if m:
        return m.group(1), m.group(2)
    m = BRACKET_COLON.match(line)
    if m:
        return m.group(1), m.group(2)
    m = BRACKET.match(line)
    if m:
        return m.group(1), m.group(2)
    m = SQUARE.match(line)
    if m:
        return m.group(1), m.group(2)
    m = NICKBR.match(line)
    if m:
        return m.group(1), m.group(2)
    m = HYPHEN.match(line)
    if m:
        return m.group(1), m.group(2)
    m = COLON.match(line)
    if m:
        token, sep, rest = m.group(1), m.group(2), line[m.end():]
        nickpart = re.sub(r'\s*:\s*$', '', token).strip()
        if ' ' in nickpart and not re.match(r'\s*\S', rest):
            # multi-word prefix at EOL: token only if nick-like, i.e. every
            # word starts with an uppercase letter (or a non-letter).
            # "Impreccable Silence:" -> nick; "Из женского форума:" -> header.
            words = nickpart.split()
            if not all(w[:1].isupper() or not w[:1].isalpha() for w in words):
                return None
        return token, sep
    m = DASH.match(line)
    if m:
        return m.group(1), m.group(2)
    m = GT.match(line)
    if m:
        return m.group(1), m.group(2)
    return None

# (id, 0-based line_no) -> 'notoken': extractor found a token here but the
# line is narrative and should be translated in full (no § in translation).
OVERRIDES = {
    (403077, 1): 'notoken',  # "Вакансия: ..." header
    (403077, 2): 'notoken',  # "Должностные обязанности:..." header
    (403122, 0): 'notoken',  # "подруга - воспитательница в детсаде." scene-setting
    (403498, 0): 'forcetoken',  # "Молекула to EgoSumLex:" nick header
    (403785, 2): 'forcetoken',  # "edgar_hoover wrote:" nick header
    (403271, 0): 'notoken',  # "Из ЖЖ:" source attribution
    (403821, 0): 'notoken',  # "С инофорума:" source attribution
    (403246, 2): 'notoken',  # "препод - тот еще демон." narrative dash
    (403341, 4): 'notoken',  # "начальник отдела спрашивает:" narrative tag
    (403320, 0): 'notoken',  # "Объявление:" header
    (404041, 3): 'notoken',  # "*та вытаскивает наушник*:" stage direction
    (403456, 0): 'notoken',  # "топик:" forum topic header
    (403373, 2): 'notoken',  # "Предмет:" field header
    (403373, 3): 'notoken',  # "Задача:" field header
    (403382, 1): 'notoken',  # "Новость на ленте:" news lead-in
    (403518, 0): 'notoken',  # "Ситуация:" narrative lead-in
    (403525, 0): 'forcetoken',  # "--=xXx=--:" decorative nick
    (403525, 12): 'forcetoken',  # "--=xXx=--:" decorative nick
    (403577, 10): 'notoken',  # "Дебуга - древний обряд..." explanatory dash, not a speaker
    (403590, 0): 'forcetoken',  # "Shzoid from Estonia, 4.02.2008 19:32:21:" forum post header
    (403615, 0): 'forcetoken',  # "^_^:" decorative nick
    (403615, 2): 'forcetoken',  # "^_^:" decorative nick
    (403691, 1): 'notoken',  # "Так вот картина:" narrative lead-in
    (403711, 4): 'notoken',  # "А потом добавил:" narrative lead-in
    (403782, 1): 'notoken',  # "Вопрос:" narrative Q&A label
    (403782, 4): 'notoken',  # "Ответ:" narrative Q&A label
    (403799, 6): 'notoken',  # "писать:" narrative lead-in
    (403883, 1): 'notoken',  # "Название фильма на торрент трекере:" narrative header
    (403908, 0): 'notoken',  # "Один из комментов к рецепту..." narrative header
    (403918, 12): 'notoken',  # "Выглядит так:" narrative lead-in
    (403925, 2): 'notoken',  # "Самый популярный комментарий:" narrative header
    (403948, 0): 'forcetoken',  # "xxx &#8206;(16:54):" IRC nick+timestamp header
    (403948, 2): 'forcetoken',  # "AxA &#8206;(16:54):" IRC nick+timestamp header
    (403948, 5): 'forcetoken',  # "xxx &#8206;(16:55):" IRC nick+timestamp header
    (403884, 0): 'notoken',  # "Новость:" news header
    (403884, 1): 'notoken',  # "Комментарий:" comment header
    (403966, 2): 'notoken',  # "Комментарий программера:" narrative lead-in
    (403961, 1): 'notoken',  # "Правда жизни:" narrative lead-in
    (404019, 0): 'notoken',  # "С форума:" source attribution
    (404045, 0): 'forcetoken',  # "simple (06:13:41 21/07/2009):" chat header
    (404066, 0): 'notoken',  # "Письмо в отдел кадров:" letter header
    (404067, 0): 'notoken',  # "Взято из ЖЖ:" source attribution
    (404003, 1): 'notoken',  # "yyy(02:16) Ну бывает :)" emoticon misparsed as token
    (403982, 1): 'notoken',  # "Открыт:" Q&A metadata label
    (403990, 1): 'notoken',  # "Вопрос:" narrative Q&A label
    (403990, 2): 'notoken',  # "Ответ:" narrative Q&A label
    (404024, 1): 'notoken',  # "Заголовок:" headline label
    (404033, 9): 'notoken',  # "Бомж - король джунглей." punchline, not a speaker
    (404044, 4): 'notoken',  # "Мама вскакивает:" narrative tag
    (404058, 5): 'notoken',  # "Голос из группы:" narrative tag
    (403208, 4): 'notoken',  # "что то типа:" narrative lead-in, not a speaker
}


def record_tokens(text, rid=None):
    """Ordered list of (line_no, token, sep) for a record's text."""
    out = []
    lines = text.split('\n')
    for i, line in enumerate(lines):
        ov = OVERRIDES.get((rid, i)) if rid is not None else None
        if ov == 'notoken':
            continue
        t = line_token(line)
        if ov == 'forcetoken' and not t:
            m = re.match(r'^(\s*\S(?:.*\S)?\s*:)(\s*)', line)
            t = (m.group(1), m.group(2)) if m else None
        if t:
            out.append((i, t[0], t[1]))
    return out
