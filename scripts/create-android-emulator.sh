#!/usr/bin/env bash
# Create a default Android emulator AVD for Veil Messenger development.
# Requires Android SDK with cmdline-tools, platform-tools, emulator, and a system image.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$ANDROID_HOME" ]; then
  if [ -d "$HOME/Library/Android/sdk" ]; then
    ANDROID_HOME="$HOME/Library/Android/sdk"
  elif [ -d "$HOME/Android/Sdk" ]; then
    ANDROID_HOME="$HOME/Android/Sdk"
  elif [ -d "/opt/android-sdk" ]; then
    ANDROID_HOME="/opt/android-sdk"
  else
    echo "ERROR: Set ANDROID_HOME to your Android SDK path."
    exit 1
  fi
fi

export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
AVD_NAME="${VEIL_AVD_NAME:-Veil_Pixel_7}"
SYSTEM_IMAGE="${VEIL_SYSTEM_IMAGE:-system-images;android-35;google_apis;x86_64}"
DEVICE="${VEIL_AVD_DEVICE:-pixel_7}"

SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
AVDMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"

if [ ! -x "$SDKMANAGER" ]; then
  echo "ERROR: sdkmanager not found at $SDKMANAGER"
  echo "Install Android Studio or Android command-line tools first."
  exit 1
fi

echo "Using Android SDK: $ANDROID_HOME"
echo "AVD name: $AVD_NAME"
echo "System image: $SYSTEM_IMAGE"

yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" "platform-tools" "emulator" "platforms;android-35" "build-tools;35.0.0" "$SYSTEM_IMAGE"

if "$AVDMANAGER" list avd | grep -q "Name: $AVD_NAME"; then
  echo "AVD '$AVD_NAME' already exists."
else
  echo "no" | "$AVDMANAGER" create avd \
    --name "$AVD_NAME" \
    --device "$DEVICE" \
    --package "$SYSTEM_IMAGE" \
    --force
  echo "Created AVD '$AVD_NAME'."
fi

echo ""
echo "Start the emulator:"
echo "  $ANDROID_HOME/emulator/emulator -avd $AVD_NAME"
echo ""
echo "Or in Android Studio: Device Manager → Play (▶) next to $AVD_NAME"
