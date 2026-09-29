#!/bin/bash
# Standalone Kotlin compile check for v1.65 work (mirrors build.sh's kotlinc
# phases). Writes to build/logs/kotlinc-check-kapt.log and
# build/logs/kotlinc-check-full.log. Exit 0 only when both phases are clean.
set -u
HERE="$(cd "$(dirname "$0")" && pwd)"
PROJ="$HOME/workspace/quote-browser-app"
BUILD="$HERE/build"
PLATFORM="$HOME/workspace/android-sdk/platforms/android-36/android.jar"
GEN="$BUILD/gen"
CLS="$BUILD/check-classes"
JAVA="$HOME/workspace/tools/jdk17/bin/java"
mkdir -p "$BUILD/logs" "$CLS"

KOTLINC_JAR="$HERE/tools/kotlin-compiler-embeddable-1.9.24.jar"
STDLIB_JAR="$HERE/tools/kotlin-stdlib-1.9.24.jar"
COMPOSE_PLUGIN="$HERE/tools/compose-compiler-1.5.14.jar"
KAPT_JAR="$HERE/tools/kapt-embeddable-1.9.24.jar"
ROOM_CP=$(find "$HERE/libs" -path "*androidx.room/room-compiler*" -name "*.jar" | head -1)
TROVE_JAR="$HERE/tools/trove4j.jar"
ANNOT_JAR="$HERE/libs/org/jetbrains/annotations/13.0/annotations-13.0.jar"
KAPT_GEN="$BUILD/check-kapt"
rm -rf "$CLS" "$KAPT_GEN"; mkdir -p "$CLS" "$KAPT_GEN/sources" "$KAPT_GEN/classes" "$KAPT_GEN/stubs"

# Rebuild the dependency classpath the same way build.sh does.
if [ ! -f "$BUILD/cp.txt" ]; then
  echo "no $BUILD/cp.txt — run build.sh once first" >&2
  exit 2
fi
CP="$(cat "$BUILD/cp.txt")"
CPFULL="$PLATFORM:$STDLIB_JAR:${CP#:}"

SRCS=$(find "$PROJ/app/src/main/java" -name "*.kt" -o -name "*.java" | sort)
RSRCS=$(find "$GEN" -name "R.java" | sort)

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

echo "--- kapt phase ---"
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
  -d "$CLS" $ROOM_SRCS >"$BUILD/logs/kotlinc-check-kapt.log" 2>&1
KAPT_EXIT=$?
echo "kapt exit: $KAPT_EXIT"
if [ $KAPT_EXIT -ne 0 ]; then
  grep -c "error:" "$BUILD/logs/kotlinc-check-kapt.log" | xargs echo "kapt errors:"
  exit 1
fi

echo "--- full phase ---"
GENSRCS=$(find "$KAPT_GEN/sources" -name "*.java" | sort)
"$JAVA" -cp "$KOTLINC_JAR:$STDLIB_JAR:$TROVE_JAR:$ANNOT_JAR" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -cp "$CPFULL" \
  -Xplugin="$COMPOSE_PLUGIN" \
  -jvm-target 17 -no-stdlib -no-reflect \
  -d "$CLS" $SRCS $RSRCS $GENSRCS >"$BUILD/logs/kotlinc-check-full.log" 2>&1
FULL_EXIT=$?
echo "full exit: $FULL_EXIT"
ERRS=$(grep -c "error:" "$BUILD/logs/kotlinc-check-full.log" || true)
echo "error count: $ERRS"
if [ $FULL_EXIT -ne 0 ] || [ "$ERRS" != "0" ]; then
  grep "error:" "$BUILD/logs/kotlinc-check-full.log" | head -20
  exit 1
fi
echo "CLEAN"
