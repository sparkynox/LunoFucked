#!/bin/sh
#
# Gradle start up script for UN*X
#
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0" 2>/dev/null || realpath "$0" 2>/dev/null || echo "$0")")
APP_HOME="$SCRIPT_DIR"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

# If wrapper jar missing or broken, tell user to run: gradle wrapper --gradle-version 8.8
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: gradle-wrapper.jar not found."
  echo "Run: gradle wrapper --gradle-version 8.8"
  exit 1
fi

exec java -jar "$WRAPPER_JAR" "$@"
