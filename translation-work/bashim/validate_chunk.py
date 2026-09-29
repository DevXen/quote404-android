#!/usr/bin/env python3
"""Validate output chunk_41.jsonl against input chunk_41.jsonl."""
import json, re, sys

INP = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_41.jsonl"
OUT = "/home/hatch/workspace/translation-work/bashim/out/chunk_41.jsonl"

src = [json.loads(l) for l in open(INP, encoding="utf-8") if l.strip()]
out = [json.loads(l) for l in open(OUT, encoding="utf-8") if l.strip()] if __import__("os").path.exists(OUT) else []

errs = []
src_ids = [r["id"] for r in src]
out_ids = [r["id"] for r in out]
if out_ids != src_ids[:len(out_ids)]:
    errs.append(f"ID order mismatch: first diff at index {next((i for i,(a,b) in enumerate(zip(out_ids, src_ids)) if a!=b), 'len')}")
if len(set(out_ids)) != len(out_ids):
    errs.append("duplicate IDs in output")
for i, o in enumerate(out):
    s = src[i]
    if set(o.keys()) != {"id","ru","en"}:
        errs.append(f"id {o.get('id')}: bad keys {set(o.keys())}")
    if o["ru"] != s["text"]:
        errs.append(f"id {o['id']}: ru != source text")
    if not o["en"] or not o["en"].strip():
        errs.append(f"id {o['id']}: empty en")
    if o["en"].count("\n") != o["ru"].count("\n"):
        errs.append(f"id {o['id']}: newline parity fail ru={o['ru'].count(chr(10))} en={o['en'].count(chr(10))}")
    # nick tokens: every <...> in ru must appear byte-identical in en
    ru_toks = re.findall(r"<[^<>\n]*>", o["ru"])
    for t in set(ru_toks):
        if t not in o["en"]:
            errs.append(f"id {o['id']}: token {t!r} missing byte-identical in en")
print(f"source={len(src)} output={len(out)}")
if errs:
    print("ERRORS:", len(errs))
    for e in errs[:40]:
        print(" -", e)
else:
    print("ALL CHECKS PASS (partial or complete)")
sys.exit(1 if errs else 0)
