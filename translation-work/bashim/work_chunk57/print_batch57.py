#!/usr/bin/env python3
"""Print source records [start, end) (0-based indices) for translation."""
import json, sys

SRC = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_57.jsonl"
start, end = int(sys.argv[1]), int(sys.argv[2])
recs = [json.loads(l) for l in open(SRC, encoding="utf-8") if l.strip()]
for r in recs[start:end]:
    print(f"===== {r['id']} =====")
    print(r["text"])
    print()
