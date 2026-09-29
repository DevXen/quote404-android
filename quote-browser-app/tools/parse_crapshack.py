#!/usr/bin/env python3
"""Parse the #CrapXen CrapShack IRC quote dump into clean JSONL.

Input : ~/workspace/user/files/crap.txt
Output: ~/workspace/quote-browser-app/tools/crapshack_quotes.jsonl
        one JSON object per line: {"number": n, "rating": null, "text": "...", "url": ""}

Each input line is one quote of the form:
    <quote body> -- Added By <author>
    <quote body> \x03--\x034 Added: <date> \x03--\x0312 By <author>
    <quote body> \x03--\x034 <date> \x03-\x0314 <author>
(with embedded IRC formatting codes throughout).

Also:
  - drops exact-duplicate bodies (keeps first occurrence)
  - merges the numbered "You know you're addicted to Filling the Botz with
    Crap When: NN - ..." list items into a single quote
"""

import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.normpath(os.path.join(HERE, "..", "..", "user", "files", "crap.txt"))
DST = os.path.join(HERE, "crapshack_quotes.jsonl")

IRC_COLOR = re.compile(r"\x03\d{0,2}(,\d{1,2})?")
IRC_CTRL = re.compile(r"[\x02\x1f\x0f\x16]")

# Line 499 (1-based): body trailer is mid-line, followed by a meta note.
SPECIAL = re.compile(
    r"^This is a Date Test\.\s*--\s*Added:\s*(\d{2}/\d{2}/\d{2,4})\s*--\s*By (\S+)"
)

TRAILER_PATTERNS = [
    # (a) -- Added By <author>
    (re.compile(r"\s*--\s*Added By (\S+)\s*$"), ("author",)),
    # (b) -- Added: <date> -- By <author>
    (
        re.compile(r"\s*--\s*Added:\s*(\d{2}/\d{2}/\d{2,4})\s*--\s*By (\S+)\s*$"),
        ("date", "author"),
    ),
    # (c) -- <date> - <author>
    (
        re.compile(r"\s*--\s*(\d{2}/\d{2}/\d{2,4})\s*-\s*(\S+)\s*$"),
        ("date", "author"),
    ),
]

ADDICTED_ITEM = re.compile(r"^(.*?):\s*(\d{1,3})\s*[-\u2013]\s*(.*)$", re.DOTALL)
BOTZ_PREFIX = "you know you're addicted to filling the botz with crap when"
BOT_PREFIX = "you know your addicted to filling the bot with crap when"


def strip_irc(s: str) -> str:
    s = IRC_COLOR.sub("", s)
    s = IRC_CTRL.sub("", s)
    return s


def parse_line(line: str, lineno: int):
    """Returns (body, date, author) or None for blank lines. Warns+skips unparseable."""
    s = strip_irc(line).strip()
    if not s:
        return None
    m = SPECIAL.match(s)
    if m:
        return "This is a Date Test.", m.group(1), m.group(2)
    for pat, fields in TRAILER_PATTERNS:
        m = pat.search(s)
        if m:
            info = dict(zip(fields, m.groups()))
            body = s[: m.start()].strip()
            return body, info.get("date"), info.get("author")
    print(f"WARNING line {lineno}: no trailer matched, skipping: {s[:120]!r}",
          file=sys.stderr)
    return None


def main() -> None:
    entries = []  # (body, date, author) in file order
    with open(SRC, encoding="utf-8", errors="replace") as fh:
        for lineno, line in enumerate(fh, start=1):
            parsed = parse_line(line, lineno)
            if parsed is None:
                continue
            body, date, author = parsed
            if not body:
                continue
            entries.append((body, date, author))
    print(f"lines parsed: {len(entries)}")

    # Dedupe exact-duplicate bodies, keep first occurrence.
    seen = set()
    unique = []
    dupes = 0
    for e in entries:
        if e[0] in seen:
            dupes += 1
            continue
        seen.add(e[0])
        unique.append(e)
    print(f"exact-duplicate bodies dropped: {dupes}")

    # Merge the numbered "addicted" list (botz prefix) into one quote.
    botz_items = []  # (file_index, number, item_text)
    botz_indexes = set()
    first_botz_index = None
    for idx, (body, date, author) in enumerate(unique):
        m = ADDICTED_ITEM.match(body)
        if not m:
            continue
        prefix = m.group(1).strip().lower()
        if prefix == BOTZ_PREFIX:
            num = int(m.group(2))
            text = m.group(3).strip()
            text = re.sub(r"^(when|item)\s*:\s*", "", text, flags=re.IGNORECASE)
            botz_items.append((idx, num, text))
            botz_indexes.add(idx)
            if first_botz_index is None:
                first_botz_index = idx
        elif prefix == BOT_PREFIX:
            print(f"note: standalone '{BOT_PREFIX}' item #{m.group(2)} kept as its own quote")

    merged = None
    if botz_items:
        lines = ["You know you're addicted to Filling the Botz with Crap When:"]
        for _, num, text in botz_items:
            lines.append(f"{num:02d} - {text}")
        merged = "\n".join(lines)
        print(f"merged {len(botz_items)} 'addicted' list items into one quote")

    # Assemble final list in file order; merged quote takes the first item's slot.
    final_texts = []
    for idx, (body, date, author) in enumerate(unique):
        if idx in botz_indexes:
            if idx == first_botz_index:
                final_texts.append(merged)
            continue
        final_texts.append(body)

    with open(DST, "w", encoding="utf-8") as out:
        for n, text in enumerate(final_texts, start=1):
            out.write(
                json.dumps(
                    {"number": n, "rating": None, "text": text, "url": ""},
                    ensure_ascii=False,
                )
                + "\n"
            )
    print(f"final quote count: {len(final_texts)}")
    print(f"wrote {DST}")


if __name__ == "__main__":
    main()
