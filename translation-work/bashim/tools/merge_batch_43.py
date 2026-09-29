#!/usr/bin/env python3
"""Merge my translations (id->en) with source chunk lines, validate, append to out."""
import json, re, sys

src_path = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_43.jsonl"
out_path = "/home/hatch/workspace/translation-work/bashim/out/chunk_43.jsonl"

def main(trans_path, batch_start, batch_end):
    src = [json.loads(l) for l in open(src_path, encoding="utf-8")]
    batch = src[batch_start:batch_end]  # 0-based slice
    mine = {}
    for l in open(trans_path, encoding="utf-8"):
        l = l.strip()
        if not l:
            continue
        o = json.loads(l)
        mine[o["id"]] = o["en"]
    token_re = re.compile(r"<[^>\n]*>")
    errs = []
    lines_out = []
    for r in batch:
        rid = r["id"]; ru = r["text"]
        if rid not in mine:
            errs.append(f"missing translation for id {rid}"); continue
        en = mine[rid]
        if not en:
            errs.append(f"empty en for id {rid}")
        if en.count("\n") != ru.count("\n"):
            errs.append(f"newline parity fail id {rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}")
        for tok in token_re.findall(ru):
            if tok not in en:
                errs.append(f"token {tok!r} missing byte-identical in en id {rid}")
        lines_out.append(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False))
    if errs:
        print("ERRORS:"); [print(" ", e) for e in errs]
        sys.exit(1)
    with open(out_path, "a", encoding="utf-8") as f:
        for l in lines_out:
            f.write(l + "\n")
    print(f"appended {len(lines_out)} records (ids {batch[0]['id']}..{batch[-1]['id']})")

main(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
