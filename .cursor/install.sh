#!/usr/bin/env bash
# Idempotent Cloud Agent setup for Veil Messenger.
# Installs the Android SDK (needed to configure the KMP :shared module and build
# the Android app), prepares local dev config, and warms the Rust + Gradle caches.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
CMDLINE_TOOLS_VERSION="11076708"

install_android_sdk() {
  if [ -x "$ANDROID_HOME/platform-tools/adb" ] && [ -d "$ANDROID_HOME/platforms/android-35" ]; then
    echo "Android SDK already present at $ANDROID_HOME"
    return 0
  fi

  echo "Installing Android SDK into $ANDROID_HOME"
  if [ ! -w "$(dirname "$ANDROID_HOME")" ] && [ ! -d "$ANDROID_HOME" ]; then
    sudo mkdir -p "$ANDROID_HOME"
    sudo chown -R "$(id -u):$(id -g)" "$ANDROID_HOME"
  else
    mkdir -p "$ANDROID_HOME"
  fi
  mkdir -p "$ANDROID_HOME/cmdline-tools"

  if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    tmp="$(mktemp -d)"
    curl -fsSL -o "$tmp/cmdtools.zip" \
      "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
    unzip -q "$tmp/cmdtools.zip" -d "$tmp"
    rm -rf "$ANDROID_HOME/cmdline-tools/latest"
    mv "$tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm -rf "$tmp"
  fi

  yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null 2>&1 || true
  "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
    "platform-tools" "platforms;android-35" "build-tools;35.0.0"
}

install_android_sdk

# Local, gitignored dev config.
[ -f .env ] || cp .env.example .env
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Warm the Rust relay build.
cargo build -p veil-relay

# Warm the Gradle distribution + KMP/Android dependency caches and verify the
# project configures against the SDK (compiles shared JVM main+test; no tests run).
./gradlew --no-daemon --console=plain :shared:jvmTestClasses

echo "Veil Messenger environment ready."
