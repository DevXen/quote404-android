#!/usr/bin/env python3
"""Append a translated batch to out/chunk_40.jsonl with hard validation.
Batch file: JSON array of {"id": int, "en": str}.
ru is copied character-for-character from source. Usage: append_batch40.py /tmp/b01.json
"""
import json, sys, re

CHUNK = "chunk_40"
SRC = f"/home/hatch/workspace/translation-work/bashim/chunks/{CHUNK}.jsonl"
OUT = f"/home/hatch/workspace/translation-work/bashim/out/{CHUNK}.jsonl"

def main(batch_path):
    src_lines = open(SRC, encoding="utf-8").read().splitlines()
    src = {}
    src_order = []
    for l in src_lines:
        r = json.loads(l)
        src[r["id"]] = r["text"]
        src_order.append(r["id"])
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
    # order check: batch ids must be the next undone source ids in order
    expected_next = [i for i in src_order if i not in done]
    batch_ids = [rec["id"] for rec in batch]
    new_ids = [i for i in batch_ids if i not in done]
    assert new_ids == expected_next[:len(new_ids)], \
        f"order mismatch: got {new_ids[:5]} expected {expected_next[:5]}"
    assert len(set(batch_ids)) == len(batch_ids), "dup ids in batch"
    errs, staged = [], []
    for rec in batch:
        rid, en = rec["id"], rec["en"]
        if rid in done:
            continue
        if rid not in src:
            errs.append(f"id {rid}: not in source"); continue
        ru = src[rid]
        if not en or not en.strip():
            errs.append(f"id {rid}: empty en"); continue
        if ru.count("\n") != en.count("\n"):
            errs.append(f"id {rid}: newline parity ru={ru.count(chr(10))} en={en.count(chr(10))}"); continue
        toks = re.findall(r"<[^<>\n]*>", ru)
        bad = [t for t in toks if t not in en]
        if bad:
            errs.append(f"id {rid}: token(s) missing in en: {bad[:4]}"); continue
        staged.append(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False))
    if errs:
        print(f"VALIDATION FAILED: {len(errs)} errors, nothing appended")
        for e in errs[:15]:
            print("ERR:", e)
        sys.exit(1)
    with open(OUT, "a", encoding="utf-8") as f:
        for s in staged:
            f.write(s + "\n")
    print(f"OK appended {len(staged)} -> total {len(done) + len(staged)}/1000")

if __name__ == "__main__":
    main(sys.argv[1])
