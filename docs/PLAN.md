# Plan: Android Chess Puzzle Game — built & run entirely from the CLI

## Context

We are creating a **brand-new Android chess *puzzle* game** in the empty directory
`/home/agent/projects/chess`. The defining constraint: **no Android Studio and no pre-installed
adb/SDK** — everything (toolchain install, project scaffolding, build, *and* running the app)
must be planned, set up, and executed from the command line on this Linux box.

The user confirmed four foundational choices:
- **Execution target:** build a real APK **and** stand up a **headless Android emulator** to actually
  boot the app and capture a screenshot (so "no adb" meant "not installed yet," not "never use it").
- **Chess rules:** use the **chesslib** library (Apache-2.0) rather than hand-writing move generation.
- **Stack:** **Kotlin + Jetpack Compose** (Material 3).
- **v1 scope (Standard):** bundled Lichess puzzle set, interactive board (tap + drag), validate the
  full solution line with automatic opponent replies, success/fail feedback, hint, next-puzzle, and
  on-device progress (solved count + streak).

**Outcome:** a clean, modular, fully buildable Kotlin/Compose chess-puzzle app, validated by a JVM
unit-test suite (chess logic + ViewModel state machine) and demonstrated by a screenshot taken from
the app running on a headless emulator — all reproducible from scripts in the repo, plus design docs
in `./docs`.

---

## Project rules

Binding engineering rules live in [`CLAUDE.md`](../CLAUDE.md) at the repo root: (1) 200-line file
cap; (2) `/loop /simplify` to a fixpoint before every commit; (3) `/loop /code-review --fix` to a
fixpoint before every commit; (4) a git pre-push hook running tests + coverage; (5) 100% test
coverage via a Kover gate. Phase 2 wires the enforcement (Kover, pre-push hook, file-length check);
these constraints shape Phases 3 and 5 (smaller files, exhaustive tests).

---

## Environment findings (verified this session)

| Fact | Reality | Consequence |
|---|---|---|
| Project dir | `/home/agent/projects/chess` is **empty** | True greenfield. |
| JDK / Gradle / Android SDK | **None present** (`ANDROID_HOME` unset) | Install all of it from zero. |
| `sudo` | Present but **password-gated** (only nmap/masscan/rustscan are NOPASSWD) | **No `apt-get install`.** Install JDK + SDK **user-local** under `$HOME`. |
| `/dev/kvm` | `crw-rw----+` (ACL grants `agent` rw; CPU has `vmx`) | **Hardware-accelerated emulator** is the primary path; software (`-no-accel`) is the fallback. |
| Emulator shared libs (libGL/EGL/X11/pulse/nss…) | Present; glibc 2.39 | Headless emulator runs with no extra packages. |
| Internet | dl.google.com, services.gradle.org, repo1.maven.org, jitpack.io, database.lichess.org all reachable | Can fetch toolchain, deps, and puzzle DB. |
| Tools | `curl wget unzip zip git zstd xz python3` present | Enough to bootstrap everything. |
| Resources | 12 cores, 31 GB RAM, 383 GB free | Ample. |

---

## Toolchain & version strategy

Versions are **pinned in a Gradle version catalog** and **verified at setup** against `sdkmanager --list`
and the official AGP↔Gradle↔JDK table. We commit to a **proven default stack** (maximizes first-try
success for headless build + emulator) and document a **latest stack** as an opt-in upgrade in the same
catalog (switch = edit `libs.versions.toml` + `gradle-wrapper.properties`).

| Component | **Default (proven) — primary** | Latest (opt-in) |
|---|---|---|
| JDK | Temurin **17** (tarball, user-local) | 17 |
| cmdline-tools | current `commandlinetools-linux-<build>_latest.zip` | same |
| Gradle (wrapper) | **8.9** | 9.2.1 |
| AGP | **8.7.x** | 9.2.0 |
| Kotlin (+ `kotlin.plugin.compose`) | **2.0.21** | 2.3.20 |
| Compose BOM | **2024.09.03** | 2026.06.00 |
| compileSdk / targetSdk | **35 / 35** | 36 / 36 |
| build-tools | **35.0.0** | 36.0.0 |
| Emulator system image | **`system-images;android-35;google_apis;x86_64`** | android-36 |
| minSdk | **24** | 24 |

Rationale for the conservative default: AGP 9.x changes the Kotlin integration (built-in Kotlin) and is
newer than several of these libraries' tested baselines; AGP 8.7 + Kotlin 2.0.21 + Compose BOM 2024.09 +
SDK 35 + the android-35 emulator image is the most battle-tested combination and removes avoidable
first-build risk. We verify actual availability at install time and only deviate deliberately.

**Package namespace:** app = `dk.cocode.chess`, core = `dk.cocode.chess.core` (matches the user's
`cocode.dk` domain).

---

## Architecture

Two-module Gradle project so the chess/puzzle logic is **pure JVM and fully testable without a device**:

```
chess/  (rootProject.name = "chess")
├─ docs/                      # design docs (PLAN.md + companions)
├─ scripts/                   # bootstrap.sh, emu.sh, sample_puzzles.py
├─ gradle/                    # libs.versions.toml + wrapper (jar+props)
├─ settings.gradle.kts, build.gradle.kts, gradle.properties, local.properties, .gitignore
├─ core/                      # PURE kotlin("jvm") — chess + puzzle logic, NO Android
│  └─ src/{main,test}/kotlin/dk/cocode/chess/core/{model,engine,data,util}
└─ app/                       # Android (Kotlin + Compose) — UI, ViewModel, persistence
   └─ src/{main,test}/java/dk/cocode/chess/{ui,viewmodel,data}
   └─ src/main/assets/puzzles.csv    # bundled Lichess subset (CC0)
```

Design principles:
- **chesslib is isolated to two files** in `:core` (`engine/ChessEngine.kt`, `engine/BoardMapper.kt`).
  The rest of the codebase — and all of `:app` — sees only a **neutral model** (`Square`, `Piece`,
  `BoardView`, `MoveIntent`/`MoveStep`, `Puzzle`). This keeps `:core`'s public API stable and swappable.
- **CSV seam:** `:core` owns all parsing; the repository is fed a `() -> InputStream`. `:app` supplies
  `{ context.assets.open("puzzles.csv") }`. `java.io` is JDK (not Android), so `:core` stays Android-free.
- **`:app` is a thin reactive shell:** Compose UI ← `StateFlow<PuzzleUiState>` ← `PuzzleViewModel` →
  `:core` `PuzzleSession` + DataStore persistence.

### Unified `:core` contract (reconciles the two app-side designs)

This is the single API `:core` exposes and `:app` compiles against. It blends the validated state
machine (mate-in-1 multi-solution rule, setup-move handling) with a **two-phase apply** so the UI can
show the player's move, pause, then animate the opponent reply.

```kotlin
// dk.cocode.chess.core.model
data class Square(val file: Int, val rank: Int) {      // file 0..7 = a..h, rank 0..7 = ranks 1..8
    val index get() = rank * 8 + file
    fun toAlgebraic(): String                           // "e2"
    companion object { fun of(alg: String): Square; fun of(file: Int, rank: Int): Square }
}
enum class PieceColor { WHITE, BLACK; fun opposite(): PieceColor }
enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
data class Piece(val color: PieceColor, val type: PieceType) { val fenChar: Char }
data class BoardView(val squares: List<List<Piece?>>) {  // squares[rank][file]; rank 0 == rank 1
    fun pieceAt(sq: Square): Piece?
    fun toRows(): List<String>                           // 8 rows [rank][file], FEN chars, ' ' = empty (for Compose)
}
data class MoveIntent(val from: Square, val to: Square, val promotion: PieceType? = null)  // UI → engine
data class MoveStep(val from: Square, val to: Square, val promotion: PieceType?, val uci: String) // engine → UI
data class Puzzle(val id: String, val fen: String, val uciMoves: List<String>, val rating: Int,
                  val themes: List<String> = emptyList(), /* + popularity/nbPlays/etc */) {
    val setupMoveUci: String; val solutionUciMoves: List<String>; val playerMoveCount: Int
}
enum class PuzzleStatus { IN_PROGRESS, SOLVED, FAILED }
enum class HintLevel { FROM_SQUARE, FROM_TO }
data class Hint(val level: HintLevel, val from: Square, val to: Square?, val pieceType: PieceType)
data class PuzzleSessionState(val puzzleId: String, val board: BoardView, val sideToMove: PieceColor,
    val playerColor: PieceColor, val status: PuzzleStatus, val playerMovesDone: Int,
    val totalPlayerMoves: Int, val lastMove: MoveStep?, val isCheck: Boolean)

sealed interface SubmitResult {
    data class Continues(val state: PuzzleSessionState, val playerMove: MoveStep) : SubmitResult // correct, non-final
    data class Solved(val state: PuzzleSessionState, val playerMove: MoveStep) : SubmitResult     // correct, final
    data class Wrong(val state: PuzzleSessionState, val expected: MoveStep) : SubmitResult         // legal but not solution → FAILED
    data class Illegal(val reason: String) : SubmitResult                                          // not legal / promo missing / finished; state unchanged
}

// dk.cocode.chess.core.engine
class PuzzleSession private constructor(/* … */) {
    val playerColor: PieceColor
    var state: PuzzleSessionState; private set
    companion object { fun start(puzzle: Puzzle): PuzzleSession }   // applies the opponent SETUP move (uciMoves[0])
    fun legalDestinations(from: Square): List<Square>
    fun requiresPromotion(from: Square, to: Square): Boolean
    fun submitPlayerMove(move: MoveIntent): SubmitResult           // applies ONLY the player's move
    fun applyOpponentReply(): MoveStep                             // precondition: last result was Continues
    fun hint(level: HintLevel = HintLevel.FROM_SQUARE): Hint
    fun reset(): PuzzleSessionState
    fun currentFen(): String
}

// dk.cocode.chess.core.data
interface PuzzleRepository { fun count(): Int; fun all(): List<Puzzle>; fun getById(id: String): Puzzle?
    fun random(rng: Random = Random.Default): Puzzle; fun next(afterId: String?): Puzzle?
    fun byRatingRange(range: IntRange): List<Puzzle>; fun byTheme(theme: String): List<Puzzle> }
object PuzzleCsvParser { fun parseLine(line: String): Puzzle?; fun parse(lines: Sequence<String>): Sequence<Puzzle> }
class CsvPuzzleRepository { companion object { fun load(openStream: () -> InputStream): CsvPuzzleRepository } }
```

**Lichess move convention (critical, baked into `PuzzleSession.start`):** the FEN is the position
*before* the opponent's setup move; `uciMoves[0]` is that setup move (applied automatically at start);
the player solves from `uciMoves[1]` onward (player, opponent, player, …). Mate-in-1 puzzles accept
**any** mating move on the final ply, not only the listed one. chesslib's `doMove(String)` is SAN, so
we always build `Move(uci, sideToMove)` from UCI; legality is pre-checked against `legalMoves()`.

### `:app` UI & ViewModel (Compose)

- **`ChessBoard` (Canvas)** — single-pass draw of squares, last-move/selected/hint tints, coordinate
  labels, pieces (Unicode glyphs `♚♛♜♝♞♟` tinted by color, behind a `PieceRenderer` interface so a
  vector piece set can be swapped in later), and legal-move dots/rings. Two coexisting `pointerInput`
  blocks: `detectTapGestures` (tap-select-then-tap-target) and `detectDragGestures` (drag a floating
  piece). Pure `BoardGeometry` (square↔pixel, flip when player is Black) is unit-tested on the JVM.
- **`PuzzleScreen`** = stateful wrapper + **stateless `PuzzleScreenContent`** (so `@Preview` and the
  headless first frame render with no ViewModel/assets/coroutines). Layout: top bar (rating chip),
  stats row (Solved / Streak / Best), prompt ("White to move — find the best move"), board, feedback
  banner, controls (Hint / Reset / Next). `PromotionDialog` shown when a pawn reaches the last rank.
- **`PuzzleViewModel`** holds `MutableStateFlow<PuzzleUiState>`, **seeded synchronously** in its
  initializer so the very first emission already paints a populated board (headless-screenshot
  requirement). Pipeline: tap/drag → `submitPlayerMove` → on `Continues` update board + `Correct`
  feedback, `launch { delay(opponentDelayMs); applyOpponentReply(); update board }`; on `Solved` →
  `progress.recordSolved()` (+ feedback); on `Wrong` → `progress.recordFailed()`. `opponentDelayMs` is
  injectable so tests are deterministic. Hint/Next/Reset/Promotion handlers as specified.
- **Persistence:** `androidx.datastore:datastore-preferences` storing `solvedCount`, `currentStreak`,
  `bestStreak`, `puzzleIndex`, behind a `ProgressRepository` interface (faked for ViewModel tests; the real DataStore impl covered via Robolectric).

---

## Execution plan (phase by phase)

> On go-ahead I'll create a task list and work through these. Each phase ends with a concrete check.

### Phase 0 — Docs (this file)
Create `/home/agent/projects/chess/docs/` and write the design docs (PLAN.md + companions
SETUP.md, ARCHITECTURE.md, DATA.md).

### Phase 1 — Toolchain (user-local, no sudo) → `scripts/bootstrap.sh`
1. Temurin **JDK 17** via `api.adoptium.net` tarball → `~/jdk/`; export `JAVA_HOME`.
2. **cmdline-tools** zip → `~/android-sdk/cmdline-tools/latest/` (the required layout); export
   `ANDROID_HOME`/`ANDROID_SDK_ROOT`/`PATH`. Persist vars in **both** `~/.bashrc` and fish universals.
3. `yes | sdkmanager --licenses`, then install: `platform-tools`, `emulator`, `platforms;android-35`,
   `build-tools;35.0.0`, `system-images;android-35;google_apis;x86_64` (verify exact IDs via
   `sdkmanager --list` first).
4. **Bootstrap the Gradle wrapper with no Gradle installed:** download `gradle-8.9-bin.zip` once
   (sha256-verify), run `gradle wrapper --gradle-version 8.9 --distribution-type bin` in the project,
   discard the distribution. Add `distributionSha256Sum` to `gradle-wrapper.properties`.
   **Check:** `java -version` → 17; `sdkmanager --version`; `adb --version`; `./gradlew --version` → 8.9.

### Phase 2 — Project scaffold (Gradle)
Write exact files: `settings.gradle.kts` (repos: `google()`, `mavenCentral()`, `maven("https://jitpack.io")`
for chesslib; `include(":core", ":app")`), root `build.gradle.kts`, `gradle.properties`
(`android.useAndroidX=true`, `org.gradle.java.installations.auto-download=false`, config/build cache),
`local.properties` (`sdk.dir=/home/agent/android-sdk`, not committed), `gradle/libs.versions.toml`
(the catalog above), `core/build.gradle.kts` (`kotlin("jvm")`, chesslib, JUnit 5 + `useJUnitPlatform()`),
`app/build.gradle.kts` (AGP application + kotlin-android + `kotlin.plugin.compose`; Compose BOM, activity-
compose, lifecycle-viewmodel/runtime-compose, datastore-preferences; `:app` test deps JUnit4 +
coroutines-test + Turbine, **Robolectric + `compose.ui:ui-test-junit4` + Roborazzi** for headless
UI/entry-point coverage), `.gitignore`. Apply the **Kover** plugin to both modules with a **100%
`koverVerify`** rule. `git init` the repo.
**Check:** `./gradlew projects` lists `:core` and `:app`.

### Phase 3 — `:core` logic + tests (the heart)
Implement the unified contract: `model/` (Square, Piece, BoardView, MoveIntent/MoveStep, Puzzle,
SessionTypes), `engine/BoardMapper.kt` + `engine/ChessEngine.kt` (the only chesslib files;
`wouldBeMate` is side-effect-free via do/undo), `engine/PuzzleSession.kt` (setup-move application,
two-phase player/opponent apply, validation rule incl. mate-in-1 multi-solution, hints, reset),
`data/PuzzleCsvParser.kt` (+ 10-col Lichess and 5-col trimmed schemas), `data/CsvPuzzleRepository.kt`,
`util/Uci.kt`. Then the **JUnit 5 suite (~45 cases)**: board mapping/orientation, legal-move generation,
full solution-line playthrough for **real** Lichess fixtures (e.g. mate-in-2 `00sHx`), wrong-move →
FAILED, promotion (right/wrong/missing), mate-in-1 multi-solution accept + non-mate reject,
setup-move-applied-first, CSV parsing (valid/malformed/header/trimmed), repository (byId/random/next/
ratingRange/theme).
**Check:** `./gradlew :core:test` green.

### Phase 4 — Puzzle data → `app/src/main/assets/puzzles.csv`
`scripts/sample_puzzles.py`: stream `lichess_db_puzzle.csv.zst` via `zstdcat`, quality-filter
(popularity/plays), keep curated tactical themes, **stratified-sample across rating bands** (~2,000–3,000
puzzles), write a trimmed 5-column CSV (~300–450 KB). Add CC0/Lichess + chesslib Apache-2.0 attribution
to `docs/` and an in-app About/NOTICE.
**Check:** asset exists, parses via `:core` parser, row count sane.

### Phase 5 — `:app` UI + ViewModel + persistence (+ tests)
Implement `ui/board/` (ChessBoard, BoardGeometry, PieceRenderer, PromotionDialog), `ui/` (PuzzleScreen
+ stateless content + components + theme), `viewmodel/` (PuzzleUiState, PuzzleViewModel),
`data/` (PuzzleAssetDataSource, ProgressRepository + DataStore impl), `ChessPuzzleApplication`,
`MainActivity`, `AndroidManifest.xml`, minimal `res/` (theme, strings, launcher icon). **JVM tests**
(JUnit4 + coroutines-test + Turbine, fake ProgressRepository, real `:core` engine): select→targets,
correct→advance+auto-reply, final→Solved+streak++, wrong→FAILED+streak=0, hint, next, reset, promotion,
drag snap-back, BoardGeometry round-trips. Plus **Robolectric Compose UI tests** (`createComposeRule`,
in the `test` source set) and **Roborazzi screenshot tests** (`@GraphicsMode(NATIVE)`) covering the
board/screen/dialog, and Robolectric tests for `MainActivity`, `Application`, the DataStore impl, and
the asset loader — to reach **literal 100%** coverage.
**Check:** `./gradlew :app:testDebugUnitTest koverVerify` green (100%).

### Phase 6 — Build the APK
`./gradlew assembleDebug` (+ `lint`). APK at `app/build/outputs/apk/debug/app-debug.apk`.
**Check:** APK produced; lint has no errors.

### Phase 7 — Headless emulator run + screenshot → `scripts/emu.sh`
Create AVD `chess` from the android-35 image; boot headless
(`emulator -avd chess -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect`,
auto-detect KVM → `-accel on` else `-no-accel`); `adb wait-for-device` + poll `sys.boot_completed`
(300 s deadline); `adb install -r app-debug.apk`; `am start -n dk.cocode.chess/.MainActivity`;
`adb exec-out screencap -p > screen.png`.
**Check:** read `screen.png` to visually confirm a populated chessboard renders. Then `adb emu kill`.

---

## Docs deliverable (`./docs`)

- `docs/PLAN.md` — this plan (overview + phases + status).
- `docs/SETUP.md` — exact, copy-pasteable toolchain bootstrap + build + emulator commands.
- `docs/ARCHITECTURE.md` — modules, the unified `:core` contract, UI/ViewModel/persistence design.
- `docs/DATA.md` — puzzle pipeline, Lichess move convention, CC0/Apache-2.0 attribution.

---

## Verification (end-to-end, no device required for logic)

1. `java -version` → 17 · `./gradlew --version` → Gradle 8.9.
2. `./gradlew :core:test` — pure chess/puzzle logic (the correctness core).
3. `./gradlew :app:testDebugUnitTest` — ViewModel state machine, board geometry, Robolectric Compose
   UI + Roborazzi screenshots, entry points.
4. `./gradlew koverVerify` — **100%** coverage gate across both modules (UI included).
5. `./gradlew assembleDebug lint` — APK builds and packages; lint clean.
5. `bash scripts/emu.sh` — boots the app on the emulator and writes `screen.png`; read the PNG to
   confirm the board renders the first puzzle. (Optional: tap via `adb shell input tap` to sanity-check
   a move, re-screenshot.)

---

## Top risks & mitigations

| Risk | Mitigation |
|---|---|
| No passwordless sudo | Entire toolchain is user-local (Temurin tarball, SDK under `$HOME`); never `apt`. |
| AGP/Gradle/JDK/Kotlin/Compose mismatch | Pin via catalog + wrapper; default to the proven stack; verify at setup; documented fallback. |
| JitPack flakiness for chesslib | Pin exact release tag; put `jitpack.io` last; re-run on first 404 (then cached). Maven Central fallback if needed. |
| SDK licenses not accepted → build aborts | `yes \| sdkmanager --licenses` before installing packages. |
| cmdline-tools wrong layout → "sdkmanager not found" | Must live at `$ANDROID_HOME/cmdline-tools/latest/bin`. |
| Emulator boot timeout / no KVM | KVM auto-detect (`-accel on` else `-no-accel`); `-no-snapshot`; 300 s boot-poll; tail `/tmp/emulator.log`. |
| Headless GPU | Always `-gpu swiftshader_indirect` (CPU GL, no host display needed). |
| Lichess move-convention bugs | Encoded once in `PuzzleSession.start` + covered by real-fixture tests; mate-in-1 multi-solution handled explicitly. |
| Cold-start ANR from parsing | Bundled CSV is small (~hundreds–few thousand rows); synchronous parse is a few ms; first frame seeded eagerly. |

---

## Status

- [x] Phase 0 — Plan saved to `docs/PLAN.md`
- [x] Phase 1 — Toolchain bootstrap (JDK 17, Android SDK, Gradle 8.9 wrapper)
- [x] Phase 2 — Gradle scaffold (modules, version catalog, Kover gate, git + pre-push hook)
- [x] Phase 3 — `:core` logic + tests (100% coverage)
- [x] Phase 4 — Puzzle data asset (2,400 Lichess puzzles, ~330 KB)
- [x] Phase 5 — `:app` UI + ViewModel + tests (100% coverage of non-`@Composable` code)
- [x] Phase 6 — APK build + lint
- [ ] Phase 7 — Emulator boot + screenshot (in progress)

_Note: interactive play beyond the screenshot uses the same emulator + `adb shell input`; full
gameplay on a physical device is a later, out-of-scope step._
