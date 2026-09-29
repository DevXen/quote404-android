#!/bin/bash
# Manual offline APK build pipeline (no Gradle).
set -e
set -o pipefail
HERE="$HOME/workspace/apkbuild"
PROJ="$HOME/workspace/quote-browser-app"
SDK="$HOME/workspace/android-sdk"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-36/android.jar"
AAPT2="$BT/aapt2"
JAVA="$HOME/workspace/tools/jdk17/bin/java"
BUILD="$HERE/build"
GEN="$BUILD/gen"
FLATS="$BUILD/flats"
CLS="$BUILD/classes"

export JAVA_HOME=$HOME/workspace/tools/jdk17

step() { echo "=== $1 ==="; }

step "aapt2 compile"
rm -rf "$FLATS" "$GEN"; mkdir -p "$FLATS" "$GEN"
compile_dir() {
  # $1 = res dir, $2 = output subdir
  if [ -d "$1" ] && [ -n "$(ls -A "$1" 2>/dev/null)" ]; then
    mkdir -p "$FLATS/$2"
    "$AAPT2" compile --dir "$1" -o "$FLATS/$2/" 2>/dev/null || \
      echo "  !! compile failed for $1"
  fi
}
compile_dir "$PROJ/app/src/main/res" app
i=0
for d in "$BUILD"/aar/*/; do
  if [ -d "${d}res" ]; then
    compile_dir "${d}res" "lib$i"
    i=$((i+1))
  fi
done
echo "compiled flats: $(find "$FLATS" -name '*.flat' | wc -l)"

step "aapt2 link"
mkdir -p "$BUILD/apk"
"$AAPT2" link -o "$BUILD/apk/base.apk" \
  -I "$PLATFORM" \
  --manifest "$BUILD/merged/AndroidManifest.xml" \
  --java "$GEN" \
  --min-sdk-version 26 --target-sdk-version 36 \
  -A "$PROJ/app/src/main/assets" \
  $(find "$FLATS" -name '*.flat' | sort | sed 's/^//') \
  --auto-add-overlay
echo "R files: $(find "$GEN" -name 'R.java' | wc -l)"

step "generate per-package R.java"
python3 "$HERE/gen_r.py"

step "collect classes.jars"
rm -rf "$BUILD/cp"; mkdir -p "$BUILD/cp"
CP=""
for j in $(find "$BUILD"/aar -name "classes.jar" | sort); do
  n=$(echo "$j" | md5sum | cut -d' ' -f1)
  cp "$j" "$BUILD/cp/lib-$n.jar"
  CP="$CP:$BUILD/cp/lib-$n.jar"
done
for j in $(find "$HERE/libs" -name "*.jar" ! -path "*/.poms/*" | sort); do
  case "$j" in
    *sources*|*javadoc*) continue;;
  esac
  n=$(echo "$j" | md5sum | cut -d' ' -f1)
  # skip duplicates of classes.jar content by name
  cp "$j" "$BUILD/cp/plain-$n.jar"
  CP="$CP:$BUILD/cp/plain-$n.jar"
done
echo "$CP" > "$BUILD/cp.txt"
echo "classpath entries: $(echo "$CP" | tr ':' '\n' | grep -c jar)"

step "kotlinc"
KOTLINC_JAR="$HERE/tools/kotlin-compiler-embeddable-1.9.24.jar"
STDLIB_JAR="$HERE/tools/kotlin-stdlib-1.9.24.jar"
COMPOSE_PLUGIN="$HERE/tools/compose-compiler-1.5.14.jar"
KAPT_JAR="$HERE/tools/kapt-embeddable-1.9.24.jar"
ROOM_CP=$(find "$HERE/libs" -path "*androidx.room/room-compiler*" -name "*.jar" | head -1)
KAPT_GEN="$BUILD/kapt"
rm -rf "$CLS" "$KAPT_GEN"; mkdir -p "$CLS" "$KAPT_GEN/sources" "$KAPT_GEN/classes" "$KAPT_GEN/stubs" "$BUILD/logs"
SRCS=$(find "$PROJ/app/src/main/java" -name "*.kt" -o -name "*.java" | sort)
RSRCS=$(find "$GEN" -name "R.java" | sort)
echo "kotlin sources: $(echo "$SRCS" | wc -l), R files: $(echo "$RSRCS" | wc -l)"
# android.jar first (like AGP), then stdlib 1.9.24 so it wins over 1.8.22 transitives
CPFULL="$PLATFORM:$STDLIB_JAR:${CP#:}"
# kapt phase: only Room-annotated sources; exclude icons-extended classes.jar
# (its presence crashes javac's TypeEnter during kapt stub processing on JDK 17)
ICONSMD5=$(md5sum "$BUILD/aar/androidx.compose.material_material-icons-extended-android/classes.jar" | cut -d' ' -f1)
CPKAPT="$PLATFORM:$STDLIB_JAR"
OIFS="$IFS"; IFS=":"
for j in $CP; do
  case "$j" in *"$ICONSMD5"*) continue;; esac
  CPKAPT="$CPKAPT:$j"
done
IFS="$OIFS"
ROOM_SRCS="$PROJ/app/src/main/java/com/quotebrowser/app/data/Quote.kt \
$PROJ/app/src/main/java/com/quotebrowser/app/data/QuoteDao.kt \
$PROJ/app/src/main/java/com/quotebrowser/app/data/QuoteDatabase.kt \
$PROJ/app/src/main/java/com/quotebrowser/app/data/MetaDatabase.kt \
$PROJ/app/src/main/java/com/quotebrowser/app/data/TranslationDatabase.kt"
TROVE_JAR="$HERE/tools/trove4j.jar"
ANNOT_JAR="$HERE/libs/org/jetbrains/annotations/13.0/annotations-13.0.jar"
"$JAVA" -cp "$KOTLINC_JAR:$STDLIB_JAR:$TROVE_JAR:$ANNOT_JAR" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -cp "$CPKAPT" \
  -Xplugin="$COMPOSE_PLUGIN" \
  -Xplugin="$KAPT_JAR" \
  -P plugin:org.jetbrains.kotlin.kapt3:sources="$KAPT_GEN/sources" \
  -P plugin:org.jetbrains.kotlin.kapt3:classes="$KAPT_GEN/classes" \
  -P plugin:org.jetbrains.kotlin.kapt3:stubs="$KAPT_GEN/stubs" \
  -P plugin:org.jetbrains.kotlin.kapt3:correctErrorTypes=true \
  -P plugin:org.jetbrains.kotlin.kapt3:apclasspath="$ROOM_CP" \
  -P plugin:org.jetbrains.kotlin.kapt3:mapDiagnosticLocations=true \
  -jvm-target 17 -no-stdlib -no-reflect \
  -d "$CLS" $ROOM_SRCS 2>&1 | tee "$BUILD/logs/kotlinc-kapt.log" | tail -40
echo "kapt phase done; generated: $(find "$KAPT_GEN/sources" -name '*.java' | wc -l)"
step "kotlinc (full, no kapt)"
GENSRCS=$(find "$KAPT_GEN/sources" -name "*.java" | sort)
"$JAVA" -cp "$KOTLINC_JAR:$STDLIB_JAR:$TROVE_JAR:$ANNOT_JAR" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -cp "$CPFULL" \
  -Xplugin="$COMPOSE_PLUGIN" \
  -jvm-target 17 -no-stdlib -no-reflect \
  -d "$CLS" $SRCS $RSRCS $GENSRCS 2>&1 | tee "$BUILD/logs/kotlinc-full.log" | tail -40
echo "classes out: $(find "$CLS" -name '*.class' | wc -l)"
