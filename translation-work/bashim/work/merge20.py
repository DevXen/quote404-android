#!/usr/bin/env python3
"""Merge translation batch files into out/chunk_20.jsonl. ru read from source."""
import json, re, sys, os

BASE = os.path.expanduser("~/workspace/translation-work/bashim")
chunk = os.path.join(BASE, "chunks/chunk_20.jsonl")
out = os.path.join(BASE, "out/chunk_20.jsonl")

# read source records
src = {}
order = []
with open(chunk, encoding="utf-8") as f:
    for line in f:
        r = json.loads(line)
        src[r["id"]] = r["text"]
        order.append(r["id"])

# parse translation batch files: lines "###<id>" then raw en text until next "###" or EOF
trans = {}
for fn in sorted(os.listdir(os.path.join(BASE, "work"))):
    if not (fn.startswith("trans_20_") and fn.endswith(".txt")):
        continue
    cur_id, buf = None, []
    def flush():
        if cur_id is not None:
            trans[cur_id] = "\n".join(buf)
    with open(os.path.join(BASE, "work", fn), encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            m = re.fullmatch(r"###(\d+)", line)
            if m:
                flush()
                cur_id, buf = int(m.group(1)), []
            else:
                buf.append(line)
    flush()

mode = "a" if os.path.exists(out) else "w"
done = set()
if os.path.exists(out):
    with open(out, encoding="utf-8") as f:
        for line in f:
            try:
                done.add(json.loads(line)["id"])
            except Exception:
                pass
n = 0
with open(out, mode, encoding="utf-8") as fo:
    for rid in order:
        if rid not in trans or rid in done:
            continue
        ru = src[rid]
        en = trans[rid]
        assert ru.count("\n") == en.count("\n"), f"newline mismatch id={rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}"
        assert en.strip(), f"empty en id={rid}"
        rec = {"id": rid, "ru": ru, "en": en}
        fo.write(json.dumps(rec, ensure_ascii=False) + "\n")
        n += 1
print(f"merged {n} records into out/chunk_20.jsonl")
