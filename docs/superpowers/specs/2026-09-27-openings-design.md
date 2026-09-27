# Openings mode — design and build plan (2026-09-27)

## What the owner asked for

A separate mode for learning and practising chess openings: learn an opening by stepping through it,
then play it back from memory. The owner left the choice of openings to us ("add the most useful ones")
and chose: a count of clean run-throughs per opening, a `Puzzles | Openings` tab switch above the title,
data kept in code with a small drill engine, and branches on the opponent's common replies.

## Catalogue

20 openings, you play the side the opening belongs to.

- As White: Italian Game, Ruy Lopez, Scotch Game, Queen's Gambit Declined, London System, English
  Opening, Catalan, Sicilian Alapin.
- As Black vs 1.e4: Sicilian Najdorf, Sicilian Dragon, French Advance, Caro-Kann Classical,
  Scandinavian, Two Knights Defence, Petrov's Defence.
- As Black vs 1.d4: Queen's Gambit Accepted, Slav, King's Indian Classical, Nimzo-Indian Rubinstein,
  Grünfeld Exchange.

Each opening: id, name and ECO code taken verbatim from Lichess's CC0 opening list, side, group, one
sentence on its idea, and 1–4 lines in SAN that branch only on the opponent's moves (so the player
always has exactly one book move). Lines run about 6–10 moves.

## Screens and flow

- Tabs `Puzzles | Openings` above the title (portrait) / at the top of the left panel (landscape).
- Openings list grouped by side, each row: name, ECO, clean count (`×3`).
- Opening screen, same layouts as the puzzle screen: name, idea, count, `Learn | Practise`, back.
  Board turned to the player's side.
- Learn: branch chips, ◀ ▶ through the line, last move highlighted, move list with the current move
  in bold.
- Practise: player moves from memory; at a branch the app picks the reply at random. A wrong move is
  not played; the book move is highlighted and named; the player then plays it; the run is no longer
  clean. At the end: `Line complete ✓` and `Again`; a clean run adds one to the count.

## Build

### `:core` (pure JVM; chesslib stays inside `engine/`)

- `ChessEngine.sanLineToUci(san: String): List<String>` — parses a space-separated SAN line from the
  start position (chesslib `MoveList.loadFromSan`); throws `IllegalArgumentException` on a bad move.
- `model/Opening.kt` — `enum class OpeningGroup { WHITE, BLACK_VS_E4, BLACK_VS_D4 }`;
  `data class Opening(id, name, eco, group, idea, lines: List<String>)`; `side` derived from group.
- `engine/OpeningTree.kt` — merges an opening's lines into a tree of plies (`san`, `uci`, children);
  rejects a tree where the player's side has two different moves at one node; names each line by its
  first move that differs from the others (`3…Nf6`, `6.Bg5`); gives each line's plies with the board
  after each (for Learn).
- `engine/OpeningDrill.kt` — the practice state machine over the tree with a `Random`: starts at the
  initial position (plays the opponent's first move when the player is Black); `legalDestinations`;
  `submit(MoveIntent)` returns `Correct(reply)`, `Wrong(book)`, `Complete(clean)` or `Illegal`;
  `bookMove()`; `clean` flag.
- `data/openings/` — the catalogue split into `WhiteOpenings.kt`, `VsE4Openings.kt`,
  `VsD4Openings.kt`, joined in `Openings.all`.
- Tests: every line parses; the branch rule holds; every name + ECO matches a row of Lichess's
  `a..e.tsv` (test resources only) whose moves are a prefix of each of the opening's lines; drill
  behaviour (right, wrong, clean, complete, branch choice by seed).

### `:app`

- `BoardState` interface (board, flipped, selected, legalTargets, lastMove, hint) implemented by
  `PuzzleUiState` and `OpeningUiState`; `ChessBoard` and its draw helpers take `BoardState`.
- `resolveTap(selected, targets, square)` — the shared tap rule (select / clear / move) used by both
  ViewModels.
- `OpeningProgressRepository` — clean counts per opening id in the existing DataStore, keys
  `opening_clean_<id>`.
- `OpeningViewModel` + `OpeningUiState` — list, open, learn stepping, practise via `OpeningDrill`.
- UI: tabs in `PuzzleScreen`, `OpeningListScreen`, `OpeningScreen` (reusing the layouts and panels).
- Tests: ViewModel flows, Robolectric renders in both orientations, 100% line coverage.

### Order

1. `:core` engine (SAN, tree, drill) and, in parallel, the `:app` board refactor (`BoardState`,
   `resolveTap`) — they touch different modules.
2. The catalogue with its oracle tests.
3. The `:app` openings mode.
4. `/simplify` and `/code-review` to a fixpoint, emulator check, commit.
