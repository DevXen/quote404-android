#!/usr/bin/env python3
"""Generic applier: import a module named by argv[1] containing EN dict,
append validated records to out/chunk_25.jsonl."""
import json, re, sys, importlib.util

BASE = "/home/hatch/workspace/translation-work/bashim"
mod_name = sys.argv[1]
spec = importlib.util.spec_from_file_location("enmod", f"{BASE}/work/{mod_name}")
enmod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(enmod)
EN = enmod.EN

src = {}
with open(f"{BASE}/chunks/chunk_25.jsonl", encoding="utf-8") as f:
    for line in f:
        r = json.loads(line)
        src[r["id"]] = r["text"]

out_path = f"{BASE}/out/chunk_25.jsonl"
done = set()
try:
    with open(out_path, encoding="utf-8") as f:
        for line in f:
            if line.strip():
                done.add(json.loads(line)["id"])
except FileNotFoundError:
    pass

nick_re = re.compile(r"<[^<>\n]*>")
errors = []
new = 0
with open(out_path, "a", encoding="utf-8") as out:
    for id_, en in EN.items():
        if id_ not in src:
            errors.append(f"id {id_} not in source"); continue
        if id_ in done:
            errors.append(f"id {id_} already translated (skipped)"); continue
        ru = src[id_]
        if not en or not en.strip():
            errors.append(f"id {id_} empty en"); continue
        if en.count("\n") != ru.count("\n"):
            errors.append(f"id {id_} newline mismatch ru={ru.count(chr(10))} en={en.count(chr(10))}")
            continue
        missing = [t for t in nick_re.findall(ru) if t not in en]
        if missing:
            errors.append(f"id {id_} missing nick tokens: {missing}")
            continue
        out.write(json.dumps({"id": id_, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
        done.add(id_); new += 1

print(f"appended={new} total_done={len(done)}")
if errors:
    print("ERRORS:")
    for e in errors: print(" ", e)
