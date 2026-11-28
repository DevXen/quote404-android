#!/usr/bin/env bash
# Quote404 JVM unit tests — manual toolchain, no Gradle.
#
# Prerequisite: a completed build.sh run (build/classes + build/cp.txt).
# Compiles quote-browser-app/tests/*.kt against the built app classes and
# runs them with JUnit 4. The JUnit jars live in test-libs/ and are NEVER
# on the app classpath — they don't ship in the APK.
#
# Exit code is JUnit's: 0 when every test passes.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"

JAVA="$HOME/workspace/tools/jdk17/bin/java"
[ -x "$JAVA" ] || JAVA=java
KOTLINC_JAR="$HERE/tools/kotlin-compiler-embeddable-1.9.24.jar"
STDLIB_JAR="$HERE/tools/kotlin-stdlib-1.9.24.jar"
TROVE_JAR="$HERE/tools/trove4j.jar"
ANNOT_JAR="$HERE/libs/org/jetbrains/annotations/13.0/annotations-13.0.jar"
TEST_LIBS="$HERE/test-libs/junit-4.13.2.jar:$HERE/test-libs/hamcrest-core-1.3.jar"
TEST_SRC="$HOME/workspace/quote-browser-app/tests"

if [ ! -d build/classes ] || [ ! -f build/cp.txt ]; then
    echo "run_tests: build/classes or build/cp.txt missing — run build.sh first" >&2
    exit 2
fi
APP_CP="build/classes:$(cat build/cp.txt)"

OUT="build/test-classes"
rm -rf "$OUT"
mkdir -p "$OUT"

echo "=== compiling tests ==="
"$JAVA" -cp "$KOTLINC_JAR:$STDLIB_JAR:$TROVE_JAR:$ANNOT_JAR" \
    org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
    -cp "$APP_CP:$TEST_LIBS" -d "$OUT" "$TEST_SRC"/*.kt || exit 1

echo "=== running tests ==="
"$JAVA" -cp "$OUT:$APP_CP:$TEST_LIBS:$STDLIB_JAR" org.junit.runner.JUnitCore \
    com.quotebrowser.app.tests.CleanModeTest \
    com.quotebrowser.app.tests.KidSafeMaskTest \
    com.quotebrowser.app.tests.TextUtilsTest \
    com.quotebrowser.app.tests.ShareUtilsTest \
    com.quotebrowser.app.tests.QuoteCleanBreaksTest
