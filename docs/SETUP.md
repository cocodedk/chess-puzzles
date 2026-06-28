# SETUP — command-line toolchain, build, and emulator

Everything is user-local (no `sudo`, no Android Studio). Versions below are what this project is pinned to.

## 1. Toolchain (one-time)

Installed under `$HOME` and recorded in `~/.chess-env.sh` (sourced by `~/.bashrc`):

```bash
# JDK 17 (Temurin tarball)
curl -fL -o jdk.tar.gz "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
tar -xzf jdk.tar.gz -C ~/jdk            # -> ~/jdk/jdk-17.0.19+10

# Android command-line tools -> ~/android-sdk/cmdline-tools/latest/
curl -fL -o clt.zip "https://dl.google.com/android/repository/commandlinetools-linux-15641748_latest.zip"
unzip -q clt.zip -d ~/android-sdk/cmdline-tools && mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest

# Env
export JAVA_HOME=~/jdk/jdk-17.0.19+10
export ANDROID_HOME=~/android-sdk
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# SDK packages + licenses
yes | sdkmanager --licenses
sdkmanager "platform-tools" "emulator" "platforms;android-35" "build-tools;35.0.0" \
           "system-images;android-35;google_apis;x86_64"
```

The Gradle wrapper (8.9) is committed, so no global Gradle is needed; `./gradlew` downloads the
distribution on first run.

## 2. Build, test, coverage

```bash
source ~/.chess-env.sh
cd ~/projects/chess
./gradlew :core:test                 # pure chess/puzzle engine (JUnit5)
./gradlew :app:testDebugUnitTest     # ViewModel + Robolectric Compose UI tests
./gradlew koverVerify                # 100% coverage gate (see CLAUDE.md rule 5)
./gradlew assembleDebug lint         # APK -> app/build/outputs/apk/debug/app-debug.apk
```

## 3. Run on a headless emulator + screenshot

```bash
bash scripts/emu.sh                  # boots AVD, installs APK, launches, writes docs/screenshot.png
adb -s emulator-5554 emu kill        # shut down when done
```

`scripts/emu.sh` auto-detects KVM (`/dev/kvm`) for hardware acceleration and falls back to
`-no-accel`. It always uses `-gpu swiftshader_indirect` (software GL, no host display needed).

## 4. Versions

JDK 17 · Gradle 8.9 · AGP 8.7.3 · Kotlin 2.0.21 · Compose BOM 2024.09.03 · compileSdk/targetSdk 35 ·
minSdk 24 · chesslib 1.3.6 (JitPack) · Kover 0.8.3 · Robolectric 4.14.1.
