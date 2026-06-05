#!/usr/bin/env bash
set -euo pipefail

echo "Setting up local.properties for Android SDK..."

if [ -n "${ANDROID_HOME-}" ]; then
  SDK_PATH="$ANDROID_HOME"
  echo "Using ANDROID_HOME: $SDK_PATH"
elif [ -d "$HOME/Android/Sdk" ]; then
  SDK_PATH="$HOME/Android/Sdk"
  echo "Found SDK at $SDK_PATH"
else
  SDK_PATH="$HOME/Android/Sdk"
  echo "ANDROID_HOME not set and $SDK_PATH not found. Creating local.properties with default path."
  echo "If build fails, install Android SDK or update local.properties with the correct path."
fi

cat > local.properties <<EOF
sdk.dir=$SDK_PATH
EOF

echo "Wrote local.properties -> sdk.dir=$SDK_PATH"
echo "Run './gradlew assembleDebug' to attempt a build."
