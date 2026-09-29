#!/usr/bin/env python3
"""Offline Maven dependency resolver for the manual APK build.

Reads a fixed list of direct dependencies (+ the Compose BOM for managed
versions), walks POMs recursively (google() then mavenCentral()), and
downloads every needed AAR/JAR with curl. Writes libs/ and deps.json.
"""
import os, re, sys, json, subprocess
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
LIBS = os.path.join(HERE, "libs")
REPOS = [
    "https://dl.google.com/dl/android/maven2",
    "https://repo1.maven.org/maven2",
]

DIRECT = [
    ("androidx.core", "core-ktx", "1.13.1"),
    ("androidx.lifecycle", "lifecycle-runtime-ktx", "2.8.3"),
    ("androidx.lifecycle", "lifecycle-runtime-compose", "2.8.3"),
    ("androidx.lifecycle", "lifecycle-viewmodel-compose", "2.8.3"),
    ("androidx.activity", "activity-compose", "1.9.2"),
    ("androidx.compose.ui", "ui", None),
    ("androidx.compose.ui", "ui-graphics", None),
    ("androidx.compose.ui", "ui-tooling-preview", None),
    ("androidx.compose.material3", "material3", None),
    ("androidx.compose.material", "material-icons-extended", None),
    ("androidx.compose.material", "material-icons-extended-android", "1.6.8"),  # Gradle-module-metadata variant; POM resolver can't infer it
    ("androidx.work", "work-runtime-ktx", "2.9.0"),
    ("androidx.navigation", "navigation-compose", "2.7.7"),
    ("androidx.paging", "paging-runtime", "3.3.2"),
    ("androidx.paging", "paging-compose", "3.3.2"),
    ("androidx.room", "room-runtime", "2.6.1"),
    ("androidx.room", "room-ktx", "2.6.1"),
    ("androidx.room", "room-paging", "2.6.1"),
    ("androidx.room", "room-compiler", "2.6.1"),
    ("com.google.mlkit", "text-recognition", "16.0.1"),
]
BOM = ("androidx.compose", "compose-bom", "2024.06.00")

pom_cache = {}
managed = {}  # (group, artifact) -> version from BOM

def gpath(g, a, v, ext):
    return f"{g.replace('.', '/')}/{a}/{v}/{a}-{v}.{ext}"

def fetch(url, dest):
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return True
    tmp = dest + ".part"
    r = subprocess.run(
        ["curl", "-sSL", "--max-time", "120", "-o", tmp, url],
        capture_output=True)
    if r.returncode != 0 or not os.path.exists(tmp) or os.path.getsize(tmp) == 0:
        if os.path.exists(tmp):
            os.remove(tmp)
        return False
    # reject HTML error pages (e.g. 404 from google maven) for any file type
    with open(tmp, "rb") as f:
        head = f.read(300).lower()
    if b"<html" in head or b"<!doctype html" in head:
        os.remove(tmp)
        return False
    os.rename(tmp, dest)
    return True

def get_pom_text(g, a, v):
    key = (g, a, v)
    if key in pom_cache:
        return pom_cache[key]
    rel = gpath(g, a, v, "pom")
    dest = os.path.join(LIBS, ".poms", rel)
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    text = None
    for repo in REPOS:
        if fetch(f"{repo}/{rel}", dest):
            with open(dest, encoding="utf-8", errors="replace") as f:
                text = f.read()
            break
    pom_cache[key] = text
    if text is None:
        print(f"  !! POM not found: {g}:{a}:{v}", file=sys.stderr)
    return text

NS = {"m": "http://maven.apache.org/POM/4.0.0"}
def q(tag):
    return f"m:{tag}"

def strip_ns(root):
    for el in root.iter():
        if "}" in el.tag:
            el.tag = el.tag.split("}", 1)[1]
    return root

def find(root, path):
    return root.find(path, NS)

def findtext(root, path, default=""):
    t = root.findtext(path, default=default, namespaces=NS)
    return t if t is not None else default

def findall(root, path):
    return root.findall(path, NS)

def parse_props(root):
    props = {}
    pe = find(root, q("properties"))
    if pe is not None:
        for c in pe:
            tag = c.tag.split("}", 1)[-1]
            props[tag] = (c.text or "").strip()
    return props

def subst(text, props, project_version, ga):
    def rep(m):
        k = m.group(1)
        if k == "project.version":
            return project_version
        return props.get(k, m.group(0))
    prev = None
    out = text or ""
    for _ in range(5):
        out = re.sub(r"\$\{([^}]+)\}", rep, out)
        if out == prev:
            break
        prev = out
    return out

def load_pom_chain(g, a, v):
    """Return (root element, merged props, packaging, project_version)."""
    chain = []
    cg, ca, cv = g, a, v
    for _ in range(6):
        text = get_pom_text(cg, ca, cv)
        if text is None:
            break
        try:
            root = ET.fromstring(text)
        except ET.ParseError as e:
            print(f"  !! parse error {cg}:{ca}:{cv}: {e}", file=sys.stderr)
            break
        chain.append((root, cg, ca, cv))
        pe = find(root, q("parent"))
        if pe is None:
            break
        pg = (findtext(pe, q("groupId")) or "").strip()
        pa = (findtext(pe, q("artifactId")) or "").strip()
        pv = (findtext(pe, q("version")) or "").strip()
        if not (pg and pa and pv):
            break
        cg, ca, cv = pg, pa, pv
    if not chain:
        return None
    # merge props: child overrides parent
    props = {}
    for root, cg, ca, cv in reversed(chain):
        props.update(parse_props(root))
    root, cg, ca, cv = chain[0]
    gv = (findtext(root, q("version")) or "").strip()
    if not gv:
        # inherit from parent
        pe = find(root, q("parent"))
        gv = (findtext(pe, q("version")) or "").strip() if pe is not None else v
    packaging = (findtext(root, q("packaging")) or "jar").strip()
    return root, props, packaging, gv or v

def load_bom():
    g, a, v = BOM
    text = get_pom_text(g, a, v)
    root = ET.fromstring(text)
    dm = find(root, f"{q('dependencyManagement')}/{q('dependencies')}")
    for d in findall(dm, q("dependency")):
        dg = (findtext(d, q("groupId")) or "").strip()
        da = (findtext(d, q("artifactId")) or "").strip()
        dv = (findtext(d, q("version")) or "").strip()
        if dg and da and dv:
            managed[(dg, da)] = dv
    print(f"BOM loaded: {len(managed)} managed versions")

def resolve():
    load_bom()
    needed = {}  # (g,a) -> version (first wins, like Gradle's default)
    queue = []
    for g, a, v in DIRECT:
        if v is None:
            v = managed.get((g, a))
            if v is None:
                print(f"!! no managed version for {g}:{a}", file=sys.stderr)
                continue
        queue.append((g, a, v))
    seen = set()
    results = []  # (g, a, v, packaging)
    while queue:
        g, a, v = queue.pop(0)
        if (g, a) in seen:
            continue
        seen.add((g, a))
        needed[(g, a)] = v
        info = load_pom_chain(g, a, v)
        if info is None:
            continue
        root, props, packaging, proj_v = info
        results.append((g, a, v, packaging))
        deps_el = find(root, q("dependencies"))
        if deps_el is None:
            continue
        for d in findall(deps_el, q("dependency")):
            scope = (findtext(d, q("scope")) or "compile").strip()
            if scope in ("test", "provided"):
                continue
            if (findtext(d, q("optional")) or "").strip() == "true":
                continue
            typ = (findtext(d, q("type")) or "jar").strip()
            if typ == "test-jar":
                continue
            dg = subst(findtext(d, q("groupId")) or "", props, proj_v, (g, a)).strip()
            da = subst(findtext(d, q("artifactId")) or "", props, proj_v, (g, a)).strip()
            dv = subst(findtext(d, q("version")) or "", props, proj_v, (g, a)).strip()
            if not dv:
                dv = managed.get((dg, da))
            # Maven exact-version range syntax: [1.2.3] means exactly 1.2.3
            m = re.match(r"^\[([^\],()]+)\]$", dv or "")
            if m:
                dv = m.group(1).strip()
            if not (dg and da and dv) or "${" in dv:
                continue
            excl = set()
            ex_el = find(d, q("exclusions"))
            if ex_el is not None:
                for e in findall(ex_el, q("exclusion")):
                    eg = (findtext(e, q("groupId")) or "*").strip()
                    ea = (findtext(e, q("artifactId")) or "*").strip()
                    excl.add((eg, ea))
            if any((eg in ("*", dg) and ea in ("*", da)) for eg, ea in excl):
                continue
            if (dg, da) not in seen and (dg, da) not in needed:
                queue.append((dg, da, dv))
    return results

def download_all(results):
    ok, fail = 0, []
    for g, a, v, packaging in results:
        ext = "aar" if packaging == "aar" else "jar"
        rel = gpath(g, a, v, ext)
        dest = os.path.join(LIBS, rel)
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        got = False
        for repo in REPOS:
            if fetch(f"{repo}/{rel}", dest):
                got = True
                break
        # fallback: aar missing -> jar (some poms lie)
        if not got and ext == "aar":
            rel2 = gpath(g, a, v, "jar")
            dest2 = os.path.join(LIBS, rel2)
            for repo in REPOS:
                if fetch(f"{repo}/{rel2}", dest2):
                    got = True
                    dest = dest2
                    ext = "jar"
                    break
        if got:
            ok += 1
        else:
            fail.append((g, a, v, ext))
    return ok, fail, results

def main():
    os.makedirs(LIBS, exist_ok=True)
    results = resolve()
    print(f"resolved {len(results)} artifacts")
    ok, fail, results = download_all(results)
    print(f"downloaded {ok}/{len(results)}")
    if fail:
        print("FAILED:")
        for g, a, v, ext in fail:
            print(f"  {g}:{a}:{v} ({ext})")
    with open(os.path.join(HERE, "deps.json"), "w") as f:
        json.dump([{"group": g, "artifact": a, "version": v} for g, a, v, _ in results],
                  f, indent=1)
    total = sum(os.path.getsize(os.path.join(LIBS, gpath(g, a, v, "aar")))
                for g, a, v in [] )  # placeholder
    print("done")

if __name__ == "__main__":
    main()
