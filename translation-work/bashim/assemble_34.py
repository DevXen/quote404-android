#!/usr/bin/env python3
"""Assemble translated batch blocks into chunk_34.jsonl output with full validation."""
import json, re, sys, os

CH = "34"
IN = os.path.expanduser(f"~/workspace/translation-work/bashim/chunks/chunk_{CH}.jsonl")
OUT = os.path.expanduser(f"~/workspace/translation-work/bashim/out/chunk_{CH}.jsonl")
TR = sys.argv[1]  # translations scratch file with blocks: ===ID=== <id>\n<text>\n===END===

def parse_translations(path):
    data = open(path, encoding="utf-8").read()
    # strip UTF-8 BOM if present
    data = data.lstrip("\ufeff")
    blocks = data.split("\n===END===\n")
    out = {}
    for b in blocks:
        b = b.strip("\n")
        if not b.strip():
            continue
        assert b.startswith("===ID=== "), f"bad block start: {b[:60]!r}"
        nl = b.index("\n")
        rid = int(b[len("===ID=== "):nl].strip())
        text = b[nl+1:]
        if rid in out:
            print(f"ERROR: duplicate translation id {rid}")
            sys.exit(1)
        out[rid] = text.replace("<SOFTBREAK>", "­")
    return out

TOK = re.compile(r"<[^<>]*>")

def main():
    src = [json.loads(l) for l in open(IN, encoding="utf-8")]
    tr = parse_translations(TR)
    done = set()
    if os.path.exists(OUT):
        for l in open(OUT, encoding="utf-8"):
            l = l.strip()
            if l:
                done.add(json.loads(l)["id"])
    n_appended = 0
    fh = open(OUT, "a", encoding="utf-8")
    for r in src:
        rid = r["id"]
        if rid in done:
            continue
        if rid not in tr:
            break  # stop at first missing (preserve input order)
        ru = r["text"]
        en = tr[rid]
        # hard validations
        if not en.strip():
            print(f"ERROR: empty en for id {rid}"); sys.exit(1)
        if en.count("\n") != ru.count("\n"):
            print(f"ERROR: newline parity id {rid}: ru={ru.count(chr(10))} en={en.count(chr(10))}")
            sys.exit(1)
        src_toks = TOK.findall(ru)
        for t in src_toks:
            if t not in en:
                print(f"WARN: token {t!r} from id {rid} not byte-identical in en (check if stage direction)")
        fh.write(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
        done.add(rid)
        n_appended += 1
    fh.close()
    total = sum(1 for l in open(OUT, encoding="utf-8") if l.strip())
    print(f"appended: {n_appended}, total in output: {total}")
    # duplicate check
    ids = [json.loads(l)["id"] for l in open(OUT, encoding="utf-8") if l.strip()]
    if len(ids) != len(set(ids)):
        print("ERROR: duplicate ids in output"); sys.exit(1)
    # order check vs source prefix
    sids = [r["id"] for r in src]
    if ids != sids[:len(ids)]:
        print("ERROR: output order diverges from source order"); sys.exit(1)
    print("order OK, no dups")

if __name__ == "__main__":
    main()
