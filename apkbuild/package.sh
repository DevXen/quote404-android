#!/bin/bash
# package.sh - javac (R.java + Room impl) -> D8 -> dex into APK -> zipalign -> sign -> verify
set -e
HERE=~/workspace/apkbuild
export PATH="$HERE/../tools/jdk17/bin:$PATH"
BUILD=$HERE/build
PROJ=~/workspace/quote-browser-app
SDK=~/workspace/android-sdk
BT=$SDK/build-tools/34.0.0
PLATFORM=$SDK/platforms/android-36/android.jar
JAVA=$HERE/../tools/jdk17/bin/java
JAVAC=$HERE/../tools/jdk17/bin/javac

step() { echo "=== $1 ==="; }

step "javac (R.java + Room generated)"
mkdir -p $BUILD/classes
CPJARS=$(ls $BUILD/cp/*.jar | tr '\n' ':')
# classpath: our kotlin classes first, then deps, then android.jar
"$JAVAC" -source 8 -target 8 -nowarn -encoding UTF-8 \
  -cp "$BUILD/classes:${CPJARS%:}:$PLATFORM" \
  -d "$BUILD/classes" \
  $(find $BUILD/gen $BUILD/kapt/sources -name "*.java" | sort) 2>&1 | grep -v "bootstrap\|deprecat" | head -20
# v1.89.0 postmortem: without -encoding, javac falls back to the OS locale
# charset and dies on UTF-8 chars in generated R.java — and the pipe above
# used to swallow that failure, shipping a dex with NO R classes at all.
if [ "${PIPESTATUS[0]}" -ne 0 ]; then echo "ERROR: javac failed — aborting package"; exit 1; fi
echo "total classes: $(find $BUILD/classes -name '*.class' | wc -l)"

step "d8 dex"
# Exclude older duplicate stdlib jars (1.8.x); use 1.9.24 instead (matches kotlinc -cp order)
DEXIN="$BUILD/classes"
for j in $BUILD/cp/*.jar; do
  case "$j" in
    *kotlin-stdlib-1.8.22*|*kotlin-stdlib-common-1.8.22*|*kotlin-stdlib-jdk7-1.8.20*|*kotlin-stdlib-jdk8-1.8.20*) continue;;
  esac
  # find original name via md5 is overkill; match by content probe instead (done below)
  DEXIN="$DEXIN $j"
done
# Exclude old kotlin-stdlib jars by CONTENT md5 (cp jar filenames use a different hash)
# plus older/conflicting duplicate artifacts (keep the newer/better variant):
# guava 29.0 (keep 31.1), annotation 1.1.0 (keep annotation-jvm 1.8.0),
# runtime-saveable (keep -android), intellij annotations 12.0 (keep jetbrains 13.0),
# collection-ktx 1.2.0 + collection 1.0.0 (keep collection-jvm 1.4.0),
# lifecycle-viewmodel-ktx (keep -android), listenablefuture 1.0 (bundled in guava 31.1).
# D8 hard-fails on "defined multiple times" when the duplicates differ.
DROPMD5="89b453019c1f723ebd807e622cd0eb7c cd121f14e612ec4a897a47b96566da07 2097cb28602f5a6320bcc1bd74914db9 da3588af3bb0c9122dbae3d733dddf53 c87962fe0522b105531f37fe351b3b0c 959a76b836fff5d8cd80877c2626b79f 9e53b0302950895be3a139be34834d5c c18689d6a23452a0c0cf15b3cc312a2f ea448f9ee2c9cb76efa17e39d3a2eb01 032c68a2c5e93c1baac1524e105bbe88 4a0a79541fbc05bcae154c1d9663480f 2178c2b8a4772361ebd09348c60381f4"
DEXIN2="$BUILD/dex/app-classes.jar $HERE/tools/kotlin-stdlib-1.9.24.jar"
for j in $BUILD/cp/*.jar; do
  md=$(md5sum "$j" | cut -d' ' -f1)
  skip=0
  for m in $DROPMD5; do [ "$md" = "$m" ] && skip=1; done
  [ $skip -eq 0 ] && DEXIN2="$DEXIN2 $j"
done
rm -rf $BUILD/dex && mkdir -p $BUILD/dex
# This D8 build only accepts archives, not raw class dirs -> jar up our classes
(cd $BUILD/classes && zip -q -r $BUILD/dex/app-classes.jar .)
# NOTE: include ALL dependency jars (cp/*.jar) — a previous version of this
# script reset DEXIN2 here and silently dropped every library class from the dex.
DEXIN2="$BUILD/dex/app-classes.jar $HERE/tools/kotlin-stdlib-1.9.24.jar"
for j in $BUILD/cp/*.jar; do
  md=$(md5sum "$j" | cut -d' ' -f1)
  skip=0
  for m in $DROPMD5; do [ "$md" = "$m" ] && skip=1; done
  [ $skip -eq 0 ] && DEXIN2="$DEXIN2 $j"
done
echo "d8 inputs: $(echo "$DEXIN2" | wc -w) jars"
set -o pipefail
"$JAVA" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 \
  --lib "$PLATFORM" \
  --min-api 26 \
  --output "$BUILD/dex" \
  $DEXIN2 > $BUILD/d8.log 2>&1
D8RC=$?
set +o pipefail
if [ $D8RC -ne 0 ]; then
  echo "D8 FAILED (exit $D8RC):"
  grep -m3 "^Error" $BUILD/d8.log
  exit $D8RC
fi
echo "D8 OK"
ls -la $BUILD/dex/

step "insert dex into apk"
cd $BUILD/apk
cp base.apk unaligned.apk
(cd ../dex && zip -q ../apk/unaligned.apk classes*.dex)
# fail loudly if no dex files made it in
unzip -l unaligned.apk | grep -c "classes.*\.dex" | grep -q . || { echo "NO DEX FILES PACKAGED"; exit 1; }
# add any AAR assets / JNI if present
for d in $BUILD/aar/*/; do
  [ -d "${d}assets" ] && (cd "$d" && zip -q -r $BUILD/apk/unaligned.apk assets -x "assets/" 2>/dev/null || true)
done
unzip -l unaligned.apk | grep -E "classes.dex|lib/|assets/" | head -8

step "zipalign + sign"
"$BT/zipalign" -f 4 unaligned.apk aligned.apk
if [ ! -f $HERE/debug.keystore ]; then
  keytool -genkeypair -keystore $HERE/debug.keystore -storepass android \
    -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10950 \
    -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null
fi
"$BT/apksigner" sign --ks $HERE/debug.keystore --ks-pass pass:android \
  --key-pass pass:android --out app.apk aligned.apk
"$BT/apksigner" verify --verbose app.apk 2>&1 | head -8

step "final checks"
unzip -t app.apk > /dev/null && echo "zip OK"
"$BT/aapt2" dump badging app.apk | head -6
ls -la app.apk
