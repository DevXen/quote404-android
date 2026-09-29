#!/usr/bin/env python3
"""Generate one R.java per AAR package by copying the app's merged R.java
(full final IDs) and swapping the package declaration.

Library bytecode references its own package's R via getstatic (AARs are
compiled with non-final R fields), so each package needs an R class with
the final merged IDs. Copying the full symbol set per package is the
transitive-R behavior: harmless and always value-correct.
"""
import os, re, sys
import xml.etree.ElementTree as ET

BUILD = os.path.expanduser("~/workspace/apkbuild/build")
AAR = os.path.join(BUILD, "aar")
APP_R = os.path.join(BUILD, "gen/com/quotebrowser/app/R.java")

def main():
    with open(APP_R) as f:
        src = f.read()
    # sanity: must contain final IDs
    assert "public static final int" in src
    pkgs = set()
    for d in os.listdir(AAR):
        mp = os.path.join(AAR, d, "AndroidManifest.xml")
        if not os.path.exists(mp):
            continue
        try:
            root = ET.parse(mp).getroot()
        except ET.ParseError:
            continue
        pkg = root.get("package")
        if pkg:
            pkgs.add(pkg)
    n = 0
    for pkg in sorted(pkgs):
        outdir = os.path.join(BUILD, "gen", *pkg.split("."))
        os.makedirs(outdir, exist_ok=True)
        # replace the package declaration (first line)
        new_src = re.sub(r"^package [^;]+;", f"package {pkg};", src, count=1,
                         flags=re.M)
        with open(os.path.join(outdir, "R.java"), "w") as f:
            f.write(new_src)
        n += 1
    print(f"generated R.java for {n} packages")

if __name__ == "__main__":
    main()
