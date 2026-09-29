#!/usr/bin/env python3
"""Assemble a batch of translations from a marker file into out/chunk_41.jsonl.
Marker file format: lines starting with \\x1eB41:<id>\\x1e begin a new translation;
subsequent lines are the English translation (may contain newlines).
"""
import json, os, sys, re

INP = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_41.jsonl"
OUT = "/home/hatch/workspace/translation-work/bashim/out/chunk_41.jsonl"

marker_file = sys.argv[1]
src = [json.loads(l) for l in open(INP, encoding="utf-8") if l.strip()]
by_id = {r["id"]: r for r in src}

trans = {}
cur = None
for line in open(marker_file, encoding="utf-8"):
    if line.startswith("¤B41:") and line.rstrip("\n").endswith("¤"):
        cur = int(line.strip()[len("¤B41:"):-len("¤")])
        trans[cur] = []
    elif cur is not None:
        trans[cur].append(line)
en_texts = {i: "".join(v).rstrip("\n") for i, v in trans.items()}

start_idx = int(sys.argv[2]) if len(sys.argv) > 2 else 0
end_idx = int(sys.argv[3]) if len(sys.argv) > 3 else start_idx + 100
batch = src[start_idx:end_idx]

existing = 0
if os.path.exists(OUT):
    existing = sum(1 for _ in open(OUT, encoding="utf-8") if _.strip())
if existing != start_idx:
    sys.exit(f"REFUSING: output has {existing} records but batch starts at {start_idx}")

lines = []
for r in batch:
    i = r["id"]
    if i not in en_texts:
        sys.exit(f"REFUSING: missing translation for id {i}")
    en = en_texts[i]
    if not en.strip():
        sys.exit(f"REFUSING: empty translation for id {i}")
    if en.count("\n") != r["text"].count("\n"):
        sys.exit(f"REFUSING: newline parity fail id {i} ru={r['text'].count(chr(10))} en={en.count(chr(10))}")
    ru_toks = set(re.findall(r"<[^<>\n]*>", r["text"]))
    for t in ru_toks:
        if t not in en:
            sys.exit(f"REFUSING: id {i} token {t!r} missing byte-identical in en")
    lines.append(json.dumps({"id": i, "ru": r["text"], "en": en}, ensure_ascii=False))
with open(OUT, "a", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")
print(f"appended {len(lines)} records (ids {batch[0]['id']}..{batch[-1]['id']})")
