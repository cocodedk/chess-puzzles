# Chess Puzzles

[![CI](https://github.com/cocodedk/chess-puzzles/actions/workflows/ci.yml/badge.svg)](https://github.com/cocodedk/chess-puzzles/actions/workflows/ci.yml)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

An Android chess **puzzle** game — Kotlin + Jetpack Compose, built and run entirely from the command
line (no Android Studio). Solve Lichess tactical puzzles with announced goals (e.g. "checkmate in 2"),
difficulty bands (Easy/Medium/Hard), day/night themes, and saved progress on-device.

## Download

<!-- cocode-apps:install:start -->
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/dk.cocode.chess/)
- [Download the APK from GitHub](https://github.com/cocodedk/chess-puzzles/releases/latest/download/ChessPuzzles.apk)
- [Auto-update the GitHub APK with Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/cocodedk/chess-puzzles)
<!-- cocode-apps:install:end -->

Android 7.0+, signed release.

<img src="docs/screenshot.png" alt="Chess Puzzles in light mode" width="250" /> <img src="docs/screenshot-night.png" alt="Chess Puzzles in night mode" width="250" /> <img src="docs/screenshot-solved.png" alt="A solved puzzle" width="250" /> <img src="docs/screenshot-openings.png" alt="Learning the Italian Game in the Openings tab" width="250" />

_Light mode with goal line and difficulty chips (left), night mode (center), solved puzzle (right)._

## Website

- [English](https://chess.cocode.dk/)
- [فارسی (Persian)](https://chess.cocode.dk/fa/)

## Features

- **Real Lichess puzzles.** Tactics from the Lichess Open Database (CC0 1.0), bundled with the app.
- **Announced goals.** Each puzzle says what to find, such as "checkmate in 2" or "win material"; the
  opponent's replies play out on their own.
- **Three difficulty bands.** Easy, Medium or Hard, with hints when you want a nudge.
- **Opening practice.** Twenty common openings, learned move by move and then played back from memory.
- **Saved progress.** Solved count, hint-free solves and streaks stay on your device.
- **Day and night themes.** Follows the system dark mode, or pick Auto, Light or Dark in the app.
- **Tap or drag.** Move pieces by tapping squares or dragging them on a custom Compose board.
- **Fully offline and free.** No account, no ads, no tracking. Apache-2.0.

## Privacy

Chess Puzzles collects, sends and shares no personal data. It requests no permissions (not even
internet access), and has no account, ads, analytics or tracking. Your progress is saved only on your
device. Read the full [privacy policy](https://chess.cocode.dk/privacy/).

## Build

```bash
source ~/.chess-env.sh                 # JAVA_HOME / ANDROID_HOME / PATH (see docs/SETUP.md)
./gradlew koverVerify                   # run all tests + the 100% coverage gate
./gradlew assembleDebug                 # build app/build/outputs/apk/debug/app-debug.apk
bash scripts/emu.sh                      # boot a headless emulator, install, screenshot -> docs/screenshot.png
```

## Modules

- **`:core`** — pure Kotlin/JVM: chess rules (via [chesslib](https://github.com/bhlangonijr/chesslib)),
  the `PuzzleSession` state machine, and CSV parsing. No Android dependencies; fully unit-tested.
- **`:app`** — Android UI: a Compose `Canvas` board (tap + drag), `PuzzleViewModel`, and DataStore
  persistence.

## Docs

- [`docs/PLAN.md`](docs/PLAN.md) — overview, decisions, status
- [`docs/SETUP.md`](docs/SETUP.md) — toolchain, build, and emulator commands
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — modules, the `:core` contract, testing approach
- [`docs/DATA.md`](docs/DATA.md) — puzzle pipeline and attribution

## Contributing

Issues and pull requests are welcome; see [CONTRIBUTING.md](CONTRIBUTING.md) for setup, the git hooks
and the pull request checklist.

The engineering rules are in [`CLAUDE.md`](CLAUDE.md): 200-line file cap, `/simplify` + `/code-review` before every commit, a
pre-push test+coverage hook, and 100% line coverage (enforced by Kover; `@Composable` functions are
excluded as their compiler-generated recomposition branches are unreachable by tests).

## Credits

Puzzles: [Lichess Open Database](https://database.lichess.org) (CC0 1.0). Chess engine:
chesslib (Apache-2.0).

## Author

**Babak Bandpey** — [cocode.dk](https://cocode.dk) | [LinkedIn](https://linkedin.com/in/babakbandpey) | [GitHub](https://github.com/cocodedk)

## License

Apache-2.0 | © 2026 [Cocode](https://cocode.dk) | Created by [Babak Bandpey](https://linkedin.com/in/babakbandpey)
