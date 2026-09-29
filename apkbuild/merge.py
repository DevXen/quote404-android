#!/usr/bin/env python3
"""Minimal AndroidManifest merger for the offline APK build.

Base: app manifest. Merges in application children, uses-permission,
uses-feature, uses-sdk from every extracted AAR manifest. Expands
${applicationId} and injects package="com.quotebrowser.app".
"""
import os, re, sys
import xml.etree.ElementTree as ET

APP_ID = "com.quotebrowser.app"
PROJ = os.path.expanduser("~/workspace/quote-browser-app")
AAR = os.path.expanduser("~/workspace/apkbuild/build/aar")
OUT = os.path.expanduser("~/workspace/apkbuild/build/merged/AndroidManifest.xml")

ANDROID = "http://schemas.android.com/apk/res/android"
TOOLS = "http://schemas.android.com/tools"
ET.register_namespace("android", ANDROID)
ET.register_namespace("tools", TOOLS)

def aname(el, attr):
    return el.get(f"{{{ANDROID}}}{attr}")

def parse(path):
    with open(path, encoding="utf-8") as f:
        text = f.read()
    text = text.replace("${applicationId}", APP_ID)
    return ET.fromstring(text)

def qual_name(name, pkg):
    if not name or name.startswith("."):
        return (pkg or "") + name
    if "." not in name:
        return (pkg or "") + "." + name
    return name

def main():
    base = parse(os.path.join(PROJ, "app/src/main/AndroidManifest.xml"))
    base.set("package", APP_ID)
    app_el = base.find("application")
    if app_el is None:
        app_el = ET.SubElement(base, "application")

    seen_comp = set()  # (tag, android:name)
    for child in list(app_el):
        n = aname(child, "name")
        if n:
            seen_comp.add((child.tag, qual_name(n, APP_ID)))

    perms, feats = set(), set()
    for child in list(base):
        if child.tag == "uses-permission":
            perms.add(aname(child, "name"))
        elif child.tag == "uses-feature":
            feats.add(aname(child, "name"))

    n_aar = 0
    for d in sorted(os.listdir(AAR)):
        mp = os.path.join(AAR, d, "AndroidManifest.xml")
        if not os.path.exists(mp):
            continue
        try:
            m = parse(mp)
        except ET.ParseError as e:
            print(f"  !! parse {d}: {e}", file=sys.stderr)
            continue
        pkg = m.get("package", "")
        n_aar += 1
        for child in list(m):
            tag = child.tag
            if tag == "uses-permission":
                n_ = aname(child, "name")
                if n_ and n_ not in perms:
                    perms.add(n_)
                    base.insert(list(base).index(app_el), ET.fromstring(ET.tostring(child)))
            elif tag == "uses-feature":
                n_ = aname(child, "name")
                if n_ and n_ not in feats:
                    feats.add(n_)
                    base.insert(list(base).index(app_el), ET.fromstring(ET.tostring(child)))
            elif tag == "application":
                for comp in list(child):
                    n_ = aname(comp, "name")
                    qn = qual_name(n_, pkg) if n_ else ""
                    # Skip androidx.startup provider: its EmojiCompat initializer runs
                    # before Application.onCreate and crashes in hand-built APKs.
                    # The app does not need downloadable emoji fonts.
                    if comp.tag == "provider" and qn.endswith("androidx.startup.InitializationProvider"):
                        continue
                    # Skip ML Kit's auto-init provider: like the startup
                    # provider above, it runs before Application.onCreate and
                    # crashes in hand-built APKs (v1.38 would not launch).
                    # ML Kit is initialized lazily via MlKit.initialize() on
                    # the OCR import screen instead.
                    if comp.tag == "provider" and qn.endswith("common.internal.MlKitInitProvider"):
                        continue
                    key = (comp.tag, qn if n_ else None)
                    if key in seen_comp:
                        continue
                    seen_comp.add(key)
                    # fix relative names to be fully qualified
                    if n_:
                        comp.set(f"{{{ANDROID}}}name", qual_name(n_, pkg))
                    # drop tools:node attrs (we always merge)
                    for k in list(comp.attrib):
                        if k.startswith(f"{{{TOOLS}}}"):
                            del comp.attrib[k]
                    app_el.append(comp)
            # ignore uses-sdk, queries handled below
            elif tag == "queries":
                # append queries blocks (rare); keep simple: append if absent
                base.insert(list(base).index(app_el), comp_copy(child))

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    tree = ET.ElementTree(base)
    tree.write(OUT, encoding="utf-8", xml_declaration=True)
    print(f"merged {n_aar} manifests -> {OUT}")
    print(f"permissions: {sorted(perms)}")

def comp_copy(el):
    return ET.fromstring(ET.tostring(el))

if __name__ == "__main__":
    main()
