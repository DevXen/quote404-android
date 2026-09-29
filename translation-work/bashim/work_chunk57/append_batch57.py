#!/usr/bin/env python3
"""Append a translated batch to out/chunk_57.jsonl with hard validation.
Batch file: JSON array of {"id": int, "en": str}.
ru is copied character-for-character from source. Usage: append_batch57.py /tmp/b57_01.json
"""
import json, sys, re, os

SRC = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_57.jsonl"
OUT = "/home/hatch/workspace/translation-work/bashim/out/chunk_57.jsonl"

def main(batch_path):
    src_order, src = [], {}
    for l in open(SRC, encoding="utf-8"):
        l = l.strip()
        if l:
            r = json.loads(l)
            src[r["id"]] = r["text"]
            src_order.append(r["id"])
    batch = json.load(open(batch_path, encoding="utf-8"))
    done = set()
    if os.path.exists(OUT):
        for l in open(OUT, encoding="utf-8"):
            l = l.strip()
            if l:
                done.add(json.loads(l)["id"])
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
        if not isinstance(en, str) or not en.strip():
            errs.append(f"id {rid}: empty en"); continue
        if ru.count("\n") != en.count("\n"):
            errs.append(f"id {rid}: newline parity ru={ru.count(chr(10))} en={en.count(chr(10))}"); continue
        toks = re.findall(r"<[^<>\n]*>", ru)
        bad = [t for t in toks if t not in en]
        if bad:
            errs.append(f"id {rid}: tokens missing {bad!r}"); continue
        staged.append({"id": rid, "ru": ru, "en": en})
    if errs:
        print("BATCH ERRORS:", len(errs))
        for e in errs:
            print(" -", e)
        sys.exit(1)
    with open(OUT, "a", encoding="utf-8") as f:
        for rec in staged:
            f.write(json.dumps(rec, ensure_ascii=False) + "\n")
    print(f"appended {len(staged)} records (total done: {len(done)+len(staged)})")

if __name__ == "__main__":
    main(sys.argv[1])
