# ARCHITECTURE

Two Gradle modules so the chess/puzzle logic is pure-JVM and fully unit-testable without a device.

```
:core   pure kotlin("jvm")   — chess rules (chesslib) + puzzle state machine + CSV parsing
:app    Android + Compose     — UI, ViewModel, DataStore persistence
```

## `:core` (no Android dependencies)

- `model/` — neutral types: `Square` (file/rank 0..7), `Piece`/`PieceColor`/`PieceType`,
  `BoardView` (8x8 snapshot, `toRows()` for the UI), `MoveIntent`/`MoveStep`, `Puzzle`,
  `PuzzleSessionState`, `SubmitResult`, `Hint`.
- `engine/` — **chesslib is isolated to two files**: `BoardMapper` (neutral ⇄ chesslib) and
  `ChessEngine` (thin wrapper: legal moves, apply-from-UCI, `wouldBeMate` via do/undo). Nothing else
  in the codebase imports chesslib.
- `engine/PuzzleSession` — the state machine. `start()` applies the opponent's **setup move**
  (`uciMoves[0]`); the player then solves from `uciMoves[1]`. `submitMove()` validates the player's
  move (accepting any mating move on a mate-in-1 final ply); `applyOpponentReply()` plays the scripted
  reply. Reports `IN_PROGRESS / SOLVED / FAILED`.
- `data/` — `PuzzleCsvParser` (10-column Lichess export or 5-column trimmed asset) and
  `CsvPuzzleRepository.load { inputStream }` (the seam that keeps `:core` Android-free).

**Lichess move convention** is encoded once in `PuzzleSession.start` and covered by real-puzzle tests.

## `:app` (Kotlin + Jetpack Compose, Material 3)

- `viewmodel/` — `PuzzleUiState` (immutable screen state) and `PuzzleViewModel` (a `StateFlow`;
  maps tap/drag/buttons to `PuzzleSession` calls; opponent reply applied synchronously; progress
  counters updated in-memory and persisted asynchronously).
- `ui/board/` — `BoardGeometry` (pure square⇄pixel mapping, flips for Black), `PieceGlyph` (Unicode
  glyphs), `BoardDrawing` (pure `DrawScope` helpers), `ChessBoard` (`Canvas` + tap/drag gestures),
  `PromotionDialog`.
- `ui/` — `PuzzleScreen` (stateful) + `PuzzleScreenContent` (stateless); `theme/`.
- `data/` — `PuzzleAssetDataSource` (reads `assets/puzzles.csv`), `ProgressRepository` +
  `DataStoreProgressRepository` (Preferences DataStore: solved count, streaks).
- `ChessApp` (composition root, lazy repos), `MainActivity`.

## Testing & coverage (100%, headless)

- `:core` — JUnit 5 with hand-verified puzzle fixtures.
- `:app` — pure JUnit for `BoardGeometry`/`PieceGlyph`/`feedbackMessage`; `PuzzleViewModel` via
  `kotlinx-coroutines-test`; Robolectric for DataStore, asset loading, `MainActivity`, and the
  Compose UI. Compose draw code is covered by drawing the hosted view to a software `Canvas` under
  Robolectric `@GraphicsMode(NATIVE)` (see `TestSupport.renderToBitmap`) — Compose's `captureToImage`
  does not work under Robolectric.
- Kover enforces **100% line** coverage on all non-`@Composable` code (`koverVerify`). Branch
  coverage is high but not gate-enforced (idiomatic inline/synthetic Kotlin emits unreachable branch
  stubs). `@Composable` functions are excluded from the metric because the Compose compiler injects
  recomposition branches (`startRestartGroup`/`skipToGroupEnd`/`updateScope`) that no test can reach;
  they are still exercised by the Robolectric render tests and the emulator screenshot.
