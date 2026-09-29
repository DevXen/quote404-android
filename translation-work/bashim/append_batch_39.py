#!/usr/bin/env python3
"""Append translated batch to chunk_39 output. Verifies ru==source, newline counts, nick tokens."""
import json, sys, re

CHUNK = 39
SRC = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_39.jsonl"
OUT = "/home/hatch/workspace/translation-work/bashim/out/chunk_39.jsonl"

def main(batch_path):
    src_lines = open(SRC, encoding="utf-8").read().splitlines()
    src = {json.loads(l)["id"]: json.loads(l)["text"] for l in src_lines}
    batch = json.load(open(batch_path, encoding="utf-8"))
    done = set()
    try:
        with open(OUT, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    done.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    errs = []
    out = open(OUT, "a", encoding="utf-8")
    n = 0
    for rec in batch:
        rid, en = rec["id"], rec["en"]
        if rid in done:
            continue
        if rid not in src:
            errs.append(f"id {rid}: not in source")
            continue
        ru = src[rid]
        if not en or not en.strip():
            errs.append(f"id {rid}: empty en")
            continue
        if ru.count("\n") != en.count("\n"):
            errs.append(f"id {rid}: newline count ru={ru.count(chr(10))} en={en.count(chr(10))}")
            continue
        nicks = re.findall(r"<[^<>\n]*>", ru)
        bad = [k for k in nicks if k not in en]
        if bad:
            errs.append(f"id {rid}: nick(s) missing in en: {bad[:3]}")
            continue
        out.write(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
        n += 1
    out.close()
    print(f"appended {n}, errors {len(errs)}")
    for e in errs[:20]:
        print("ERR:", e)

if __name__ == "__main__":
    main(sys.argv[1])
