#!/usr/bin/env python3
"""Join translation batches (id->en) with source chunk, append to out file.
Usage: join.py <batch_jsonl>  — batch lines are {"id":..., "en": "..."}
Resumes: skips source records already written to out file.
"""
import json, sys

CHUNK = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_29.jsonl"
OUT = "/home/hatch/workspace/translation-work/bashim/out/chunk_29.jsonl"

def load_ids(path):
    ids = []
    try:
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line:
                    ids.append(json.loads(line)["id"])
    except FileNotFoundError:
        pass
    return ids

src = [json.loads(l) for l in open(CHUNK, encoding="utf-8")]
done_ids = set(load_ids(OUT))
tr = {}
for line in open(sys.argv[1], encoding="utf-8"):
    line = line.strip()
    if line:
        o = json.loads(line)
        if not o.get("en"):
            raise SystemExit(f"EMPTY en for id {o.get('id')}")
        tr[o["id"]] = o["en"]

n = 0
with open(OUT, "a", encoding="utf-8") as f:
    for rec in src:
        rid = rec["id"]
        if rid in done_ids:
            continue
        if rid not in tr:
            continue  # translation not in this batch yet; will come later
        ru = rec["text"]
        en = tr[rid]
        if ru.count("\n") != en.count("\n"):
            raise SystemExit(f"NEWLINE MISMATCH id {rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}")
        f.write(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
        n += 1
print(f"appended {n}; out total lines: {len(load_ids(OUT))}")
