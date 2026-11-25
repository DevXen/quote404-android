import json, os, zipfile, sys
HERE = os.path.expanduser("~/workspace/apkbuild")
deps = json.load(open(os.path.join(HERE, "deps.json")))
out_root = os.path.join(HERE, "build", "aar")
os.makedirs(out_root, exist_ok=True)
ok, missing, jars = 0, [], 0
for d in deps:
    g, a, v = d["group"], d["artifact"], d["version"]
    base = os.path.join(HERE, "libs", *g.split("."), a, v)
    aar = os.path.join(base, f"{a}-{v}.aar")
    jar = os.path.join(base, f"{a}-{v}.jar")
    if os.path.exists(aar):
        dest = os.path.join(out_root, f"{g}_{a}")
        if os.path.exists(os.path.join(dest, "classes.jar")):
            ok += 1; continue
        os.makedirs(dest, exist_ok=True)
        with zipfile.ZipFile(aar) as z:
            z.extractall(dest)
        ok += 1
    elif os.path.exists(jar):
        jars += 1
    else:
        missing.append(f"{g}:{a}:{v}")
print(f"aars extracted: {ok}, jar-only deps: {jars}, missing: {missing}")
