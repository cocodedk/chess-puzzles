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
- `engine/OpeningTree` — merges an `Opening`'s SAN lines (parsed via `ChessEngine.sanLineToUci`) into a
  prefix tree of `Ply(san, move, board)`, one node per position; the player's side may only ever have
  one book move per node. `start` is the initial `BoardView`; `plies(line)` gives one line's plies for
  Learn; `lineNames` labels each line by the move where it first diverges from the others.
- `engine/OpeningDrill` — the Practise state machine over an `OpeningTree` with a `Random` (picks the
  opponent's branch, and Black's opening move too). `submit(MoveIntent)` returns `Correct`/`Wrong`/
  `Complete(clean)`/`Illegal`; `bookMove()`, `legalDestinations`, `restart()`.
- `data/` — `PuzzleCsvParser` (10-column Lichess export or 5-column trimmed asset) and
  `CsvPuzzleRepository.load { inputStream }` (the seam that keeps `:core` Android-free).
  `data/openings/` holds the 20-opening catalogue (`Openings.all`), split by group into
  `WhiteOpenings`/`VsE4Openings`/`VsD4Openings` for the 200-line cap.

**Lichess move convention** is encoded once in `PuzzleSession.start` and covered by real-puzzle tests.

## `:app` (Kotlin + Jetpack Compose, Material 3)

- `viewmodel/` — `BoardState` (the board fields `ChessBoard` needs: board, flipped, selected,
  legalTargets, lastMove, hint) is implemented by both `PuzzleUiState` and `OpeningUiState`;
  `resolveTap(selected, targets, square)` is the tap rule (`Select`/`Clear`/`Move`) both ViewModels
  share. `PuzzleUiState` + `PuzzleViewModel` as before. `OpeningUiState` adds the opening id (null =
  list shown), `mode` (LEARN/PRACTISE), Learn's `lineNames`/`line`/`ply`/`moveSan`, and Practise's
  `feedback`/`clean`/`cleanRuns`. `OpeningViewModel` drives it: `onOpen`/`onBack`, `onMode` (a fresh
  `OpeningDrill` every time Practise starts), `onLine`/`onStep` for Learn, and the tap/drag handlers,
  which delegate to `OpeningPractise` — a small internal class holding one drill's live selection,
  hint and feedback, keeping that off the ViewModel's own state.
- `ui/board/` — `BoardGeometry` (pure square⇄pixel mapping inside the frame, flips for Black),
  `WoodGrain`, `BoardFrame` and `BrassCorner` (the maple-and-walnut board in its thin mahogany rim),
  `BoardCoordinates` (files and ranks written on the edge squares),
  `PieceArt` and `PieceDrawing` (vector Staunton pieces, ivory and ebony), `BoardDrawing` (pure
  `DrawScope` helpers), `ChessBoard` (`Canvas` + tap/drag gestures), `PromotionDialog`.
- `ui/BoardScaffold` — the three-way board layout (`boardLayout`: a board between two panels, beside
  one, or stacked in a column) shared by the puzzle and openings screens, so the `when` lives once.
  Takes a `tabs` slot (the `Puzzles | Openings` `ModeTabs` row, always first) plus `header`/`body`
  slots, each told whether it must lay its own rows out as a stacked column.
  `PuzzleScreen`/`PuzzleScreenContent` compose their existing panels through it unchanged.
  `PuzzlePanels` also exposes its `Lineup` row/column helper and compact button style (`internal`) for
  the openings panels to reuse.
- `ui/Opening*` — `OpeningScreen` routes list / one opening / About, mirroring `PuzzleScreen`;
  `OpeningListScreen` groups the catalogue by side with each row's clean-run count;
  `OpeningScreenContent` lays out one opening via `BoardScaffold`, with `OpeningHeader` (back, name,
  ECO/clean count, Learn|Practise chips) and either `OpeningLearnPanel` (idea, branch chips, ◀▶
  stepping, the numbered move list) or `OpeningPractisePanel` (the drill prompt, verdict, Again).
- `data/` — `PuzzleAssetDataSource` (reads `assets/puzzles.csv`), `ProgressRepository` +
  `DataStoreProgressRepository` (Preferences DataStore: solved count, hint-free solves, streaks),
  `OpeningProgressRepository` + `DataStoreOpeningProgressRepository` (clean-run counts, one
  `opening_clean_<id>` key per opening, in the same DataStore).
- `ChessApp` (composition root, lazy repos incl. `openingProgress`), `MainActivity` (`AppTab` in
  `rememberSaveable`, switching between a `PuzzleViewModel` and an `OpeningViewModel`).

## Testing & coverage (100%, headless)

- `:core` — JUnit 5 with hand-verified puzzle fixtures.
- `:app` — pure JUnit for `BoardGeometry`/`feedbackMessage`/openings text helpers; `PuzzleViewModel`
  and `OpeningViewModel`/`OpeningPractise` via `kotlinx-coroutines-test`; Robolectric for DataStore,
  asset loading, `MainActivity`, and the Compose UI (incl. the openings list/screen renders in both
  orientations, and the `Puzzles | Openings` tab switch). Compose draw code is covered by drawing the
  hosted view to a software `Canvas` under Robolectric `@GraphicsMode(NATIVE)` (see
  `TestSupport.renderToBitmap`) — Compose's `captureToImage` does not work under Robolectric.
- Kover enforces **100% line** coverage on all non-`@Composable` code (`koverVerify`). Branch
  coverage is high but not gate-enforced (idiomatic inline/synthetic Kotlin emits unreachable branch
  stubs). `@Composable` functions are excluded from the metric because the Compose compiler injects
  recomposition branches (`startRestartGroup`/`skipToGroupEnd`/`updateScope`) that no test can reach;
  they are still exercised by the Robolectric render tests and the emulator screenshot.
