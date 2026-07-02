#!/usr/bin/env bash
# Boots a headless Android emulator, installs the debug APK, launches it, and captures a screenshot.
# Usage: scripts/emu.sh [screenshot-path]   Env overrides: AVD, IMG, ANDROID_HOME.
set -uo pipefail
[ -f "$HOME/.chess-env.sh" ] && source "$HOME/.chess-env.sh"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
# avdmanager and the emulator must agree on the AVD location (cmdline-tools defaults differ).
export ANDROID_USER_HOME="${ANDROID_USER_HOME:-$HOME/.android}"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.android/avd}"
PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

AVD="${AVD:-chess}"
IMG="${IMG:-system-images;android-35;google_apis;x86_64}"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
PKG=dk.cocode.chess
SHOT="${1:-$ROOT/docs/screenshot.png}"
SERIAL=emulator-5554

[ -f "$APK" ] || { echo "[emu] no debug APK at $APK — run ./gradlew assembleDebug first"; exit 1; }

if ! emulator -list-avds 2>/dev/null | grep -qx "$AVD"; then
  echo "[emu] creating AVD $AVD from $IMG"
  echo "no" | avdmanager create avd -n "$AVD" -k "$IMG" --force >/dev/null || {
    echo "[emu] could not create AVD $AVD. Installed system images:"
    sdkmanager --list_installed 2>/dev/null | grep system-images || true
    echo "[emu] pick an image via IMG=... or an existing AVD via AVD=... ; existing AVDs:"
    emulator -list-avds
    exit 1
  }
fi

if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
  echo "[emu] KVM available -> hardware acceleration"; ACCEL=(-accel on)
else
  echo "[emu] no KVM -> software rendering (slow)"; ACCEL=(-no-accel)
fi

adb start-server >/dev/null 2>&1
echo "[emu] booting $AVD..."
# -memory 4096: stock AVD RAM is low enough that lowmemorykiller reaps the app mid-run.
nohup emulator -avd "$AVD" -no-window -no-audio -no-boot-anim -no-snapshot \
  -memory 4096 -cores 4 -gpu swiftshader_indirect -netdelay none -netspeed full \
  -camera-back none -camera-front none "${ACCEL[@]}" >/tmp/emulator.log 2>&1 &

timeout 180 adb -s "$SERIAL" wait-for-device || { echo "[emu] device never came online"; tail -30 /tmp/emulator.log; exit 1; }
deadline=$(( $(date +%s) + 300 ))
until [ "$(adb -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
  if [ "$(date +%s)" -gt "$deadline" ]; then echo "[emu] BOOT TIMEOUT"; tail -30 /tmp/emulator.log; exit 1; fi
  sleep 3
done
adb -s "$SERIAL" shell input keyevent 82 >/dev/null 2>&1 || true
echo "[emu] booted; installing APK"

adb -s "$SERIAL" uninstall "$PKG" >/dev/null 2>&1 || true # avoid debug-signature mismatch with an old install
adb -s "$SERIAL" install -r "$APK"
adb -s "$SERIAL" shell am start -n "${PKG}/.MainActivity"
sleep 15 # let the cold-start splash dismiss and the first Compose frame render (software GPU)
adb -s "$SERIAL" exec-out screencap -p > "$SHOT"
echo "[emu] screenshot -> $SHOT ($(stat -c%s "$SHOT") bytes)"
