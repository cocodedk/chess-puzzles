#!/usr/bin/env bash
# Boots a headless Android emulator, installs the debug APK, launches it, and captures a screenshot.
set -uo pipefail
[ -f "$HOME/.chess-env.sh" ] && source "$HOME/.chess-env.sh"
export ANDROID_SDK_ROOT="${ANDROID_HOME:?ANDROID_HOME must be set}"
# avdmanager and the emulator must agree on the AVD location (cmdline-tools defaults differ).
export ANDROID_USER_HOME="${ANDROID_USER_HOME:-$HOME/.android}"
export ANDROID_AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.android/avd}"

AVD=chess
IMG="system-images;android-35;google_apis;x86_64"
APK=/home/agent/projects/chess/app/build/outputs/apk/debug/app-debug.apk
PKG=dk.cocode.chess
SHOT="${1:-/home/agent/projects/chess/docs/screenshot.png}"
SERIAL=emulator-5554

if ! avdmanager list avd 2>/dev/null | grep -q "Name: ${AVD}$"; then
  echo "[emu] creating AVD $AVD"
  echo "no" | avdmanager create avd -n "$AVD" -k "$IMG" --force >/dev/null
fi

if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
  echo "[emu] KVM available -> hardware acceleration"; ACCEL=(-accel on)
else
  echo "[emu] no KVM -> software rendering (slow)"; ACCEL=(-no-accel)
fi

adb start-server >/dev/null 2>&1
echo "[emu] booting..."
nohup emulator -avd "$AVD" -no-window -no-audio -no-boot-anim -no-snapshot \
  -gpu swiftshader_indirect -netdelay none -netspeed full \
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
