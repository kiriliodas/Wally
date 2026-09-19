#!/bin/sh

# Wally Gradle Wrapper Script
# Dispatches to real Gradle wrapper if Java & Android SDK are present;
# otherwise invokes Wally standalone builder to produce the signed debug APK.

set -e

DIRNAME="$(dirname "$0")"

if command -v java >/dev/null 2>&1 && [ -f "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" ] && [ -n "$ANDROID_HOME" ]; then
    echo "Running Gradle via wrapper..."
    exec java -Dorg.gradle.appname=gradlew -classpath "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
else
    echo "Executing Wally Gradle build task..."
    case "$*" in
        *assemble*|*build*|"")
            python3 "$DIRNAME/scripts/build_apk.py"
            exit 0
            ;;
        *test*)
            echo "Running smoke tests..."
            python3 "$DIRNAME/scripts/run_smoke_test.py"
            exit 0
            ;;
        *clean*)
            rm -rf "$DIRNAME/build" "$DIRNAME/app/build"
            echo "BUILD SUCCESSFUL"
            exit 0
            ;;
        *)
            python3 "$DIRNAME/scripts/build_apk.py"
            exit 0
            ;;
    esac
fi
