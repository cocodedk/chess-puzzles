# 01 — A screen reader can play the puzzles

## Why

A user wrote: "We as visually impaired users that use screen readers to interact with mobile phones
currently cannot benefit from the app." The board is one drawn canvas with no accessibility
information, so TalkBack sees nothing on it: a blind player can hear the buttons but can neither read
the position nor make a move. This spec makes the Puzzles tab fully usable with TalkBack. The
Openings tab is spec 02 and is out of scope here, except that it shares the board and so gets the
spoken squares for free.

## Goal

With TalkBack on, a player who cannot see the screen can open a puzzle, hear the whole position, pick
a piece, hear where it can go, play a move (promotion included), hear the verdict and the opponent's
reply, ask for a hint, reset, and move between puzzles and bands. Nothing changes for a sighted
player: the screens look exactly as they do now (`docs/screenshot.png`, `docs/screenshot-night.png`,
`docs/screenshot-solved.png`), and finger taps and drags on the board work as before.

All spoken text is English, like the rest of the app, and is written in Kotlin like the existing
labels. It is never shown on screen unless this spec says so.

## Design reference

There is no visual change, so the current screens are the design (the screenshots above, and the
landscape layouts in `BoardScaffold.kt`). What this spec defines is what TalkBack says and what its
gestures do. Every sentence below is exact: tests compare them character for character.

## Words for moves

One pure function turns a move in SAN into words, and every spoken move in this spec uses it. It is
shared with spec 02, so it lives outside the puzzle code (for example `viewmodel/SpokenMoves.kt`).

| SAN | Spoken |
|---|---|
| `e4` | `pawn e4` |
| `exd5` | `e pawn takes d5` |
| `e8=Q` | `pawn e8, promotes to queen` |
| `exd8=N+` | `e pawn takes d8, promotes to knight, check` |
| `Nf3` | `knight f3` |
| `Nxe5` | `knight takes e5` |
| `Nbd7` | `knight b d7` |
| `R1e2` | `rook 1 e2` |
| `Qh4xe1` | `queen h4 takes e1` |
| `O-O` | `castles kingside` |
| `O-O-O` | `castles queenside` |
| `Qxf7#` | `queen takes f7, checkmate` |
| `Bb5+` | `bishop b5, check` |

Piece letters: K king, Q queen, R rook, B bishop, N knight, none for a pawn. A disambiguating file or
rank is spoken as it is written. `+` adds `, check` and `#` adds `, checkmate` at the end.

The puzzle engine has no SAN today. `:core` gains it the same way the openings got it: chesslib, which
is already in `:core`, gives the SAN of a legal move in the current position. The SAN of the
opponent's first move (the one played before the player's turn), of the player's move (a wrong one
included, which is never applied), and of each reply must reach the view model. How it gets there is
the builder's choice, within the rule that chesslib stays confined to `ChessEngine.kt` and
`BoardMapper.kt`.

## The board

The board keeps drawing and taking touches exactly as now. On top of that, TalkBack sees 64 square
items and nothing else on the board (the frame and the coordinate letters are not read).

- **Order.** Squares are read in the order they are shown: the top row left to right, then the next
  row down. From White's side that is a8, b8 … h8, a7 … h1. From Black's side (the board flipped) it
  is h1, g1 … a1, h2 … a8.
- **Place.** Each square item covers exactly the square it names on screen (inside the frame), so
  exploring by touch reads the square under the finger.
- **What a square says.** `<square>, <contents>` then any states, each after a comma, in this order:
  `selected`, `legal move`, `last move`, `hint`.
  - contents: `empty`, or colour and piece: `white king`, `black knight`, `white pawn` …
  - `selected`: this is the selected square.
  - `legal move`: the selected piece can move here.
  - `last move`: the last move's from or to square.
  - `hint`: the hint's from or to square.
  - Examples: `e4, white knight, last move`; `f3, empty, legal move`; `g1, white knight, selected,
    hint`; `d5, black pawn, legal move`.
- **Double-tap** on a square does exactly what a finger tap on that square does now (select, clear,
  or move). TalkBack's own hint ("double-tap to activate") is enough; no custom action label.
- The board as a whole has no other description.

The square text is built by a pure function of the board state and the square, so it is tested
without a screen.

## What is said, and when

The puzzle screen gains one spoken announcement, a sentence the view model sets on each event below.
It is read aloud by a polite live region on the verdict line (the line under the prompt that shows
"Correct — keep going" and the like). That line still shows the same visible text as now; the
announcement is what a screen reader hears from it. When there is no announcement, the line is read
as its visible text. `<Side>` is `White` or `Black`: whoever made that move.

| Event | Announcement |
|---|---|
| A puzzle is shown (first launch, Next, Previous, a band chip, and after the app is restored) | `Puzzle 4 of 812, rating 1520. Black played pawn e5. White to move — checkmate in 2.` (the title, the rating, the opponent's first move, then the prompt text as shown) |
| A piece is selected by a tap or the start of a drag | `Knight g1 selected. Moves: f3, h3.` (piece name with a capital, no colour; the legal target squares in the order a1, b1 … h8) |
| A tap selects nothing (an empty square, an opponent's piece, a piece with no moves) | `No moves from e5.` |
| The selected square is tapped again | `Selection cleared.` |
| A correct move that the opponent answers | `Correct. You played knight f3. Black played pawn takes d5.` |
| A wrong move | `You played knight f3. Not the best move — try again.` |
| The solving move | `Solved. You played queen takes f7, checkmate.` |
| Hint | `Hint: knight g1 to f3.` |
| Reset | `Puzzle reset. White to move — checkmate in 2.` (then the prompt text as shown) |
| The promotion chooser opens | nothing new: the dialog is announced by TalkBack itself, and its pieces are already named |

A promotion move is announced when the piece is chosen, as any other move (`You played pawn e8,
promotes to queen.` and so on). Cancelling the chooser is `Selection cleared.`

Two identical sentences in a row may be spoken only once; that is accepted. Every move is preceded by
a selection, whose sentence differs, so a repeated move is still heard.

## The rest of the Puzzles screen

- "Puzzle 4 of 812" is a heading, so TalkBack's heading navigation reaches it.
- Each of the five stats is one item that says its label and number: `Day streak, 4`, `Solved, 1441`,
  `Solved without a hint, 1101`, `Current streak, 53`, `Best streak, 120` (labels as in
  `strings.xml`).
- The ◀ and ▶ buttons say only `Previous puzzle` and `Next puzzle` (and "button"): the arrow characters
  are not read.
- Hint, Reset, the band chips, the theme button, About and the tabs already read correctly and stay as
  they are.

## Acceptance tests

Robolectric and JVM tests, added to the suite:

1. The SAN-to-words function gives every row of the table above.
2. `:core`: the SAN of a quiet move, a capture, a check, a mate, castling both ways, and a promotion,
   from a known position.
3. The square text for each state combination in the examples above, and `empty`.
4. On the rendered puzzle screen from White's side, the 64 square items exist, in the order a8 … h1,
   each inside the board's bounds and each centred on its own square; from Black's side the order is
   h1 … a8 and e4's item sits where e4 is drawn flipped.
5. Performing the accessibility click on the item `e2, white pawn` calls the tap handler with e2.
6. View-model tests for every row of the announcement table, with the exact sentences.
7. The verdict line has a polite live region and exposes the current announcement.
8. The title node is a heading; each stat is one node with label and number; the Previous and Next
   buttons expose no ◀ or ▶ text.

No existing test's assertion changes. A test may change how it finds a node (for example
`useUnmergedTree = true`) where merging a stat into one item changes which node a finder returns.

## Done when

The profile's suite passes: every test above, the 200-line cap, and 100% line coverage (CLAUDE.md
rules 1, 4 and 5). The screens look unchanged.

## Out of scope

- The Openings tab's own sentences (spec 02).
- Typing or speaking moves, sounds, haptics, Braille-specific work.
- Any visual change, any new setting, any translation.
- The About screen, the website and the store listing.
