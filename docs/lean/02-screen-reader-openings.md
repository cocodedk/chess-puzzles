# 02 — A screen reader can learn and practise the openings

## Why

Spec 01 made the board and the Puzzles tab usable with TalkBack. The Openings tab shares the board, so
its squares are already spoken and double-tap already moves, but nothing tells a blind player what
happened: stepping through a line, the book's reply, or a wrong move are silent, and the move list is
read as raw notation ("N f 3"). This spec finishes the Openings tab.

## Goal

With TalkBack on, a player who cannot see the screen can pick an opening from the list, step through
its lines in Learn and hear each move, switch lines, practise the line from memory, and hear every
verdict and every book reply. Nothing changes for a sighted player: the screens look exactly as now
(`docs/screenshot-openings.png` and the running app).

## Design reference

No visual change: the current Openings screens are the design. What this spec defines is what
TalkBack says. Every sentence below is exact, and tests compare them character for character. Moves
are spoken with spec 01's SAN-to-words function (`Nf3` → `knight f3`). The square items and the
selection sentences (`Knight g1 selected. Moves: f3, h3.`, `No moves from e5.`, `Selection cleared.`)
are spec 01's, reused unchanged.

## The list

- The three group titles ("As White", "As Black against 1.e4", "As Black against 1.d4") are headings.
- Each opening row is one item: `Italian Game, C50`, and with clean runs `Italian Game, C50, 3 clean
  runs` (`1 clean run` for one). The visible `×3` is not read on its own. Double-tap opens it, as a tap
  does now.

## An open opening

- The Back button says only `Back` (and "button"): the arrow is not read.
- The opening's name is a heading.
- The line under it says `C50` or `C50, 3 clean runs` (`1 clean run` for one), not "×3 clean".
- The Learn and Practise chips already read correctly.
- Line chips appear only when an opening has more than one line. Each shows where its line leaves
  the others, in notation (`3…Bc5`, `4.d3`), and is read in words, as the move it names:
  `Move 3, Black: bishop c5`, `Move 4, White: pawn d3`. Its selected state reads as now.
- The Previous move and Next move buttons say only their names: the arrows are not read.
- The move list is read in words, numbered as shown: `1. pawn e4, pawn e5. 2. knight f3, knight c6.
  3. bishop c4.` A move list that is empty is not read.
- The idea text is read as it is written.

## What is said, and when

The openings screen gains one spoken announcement, set by the view model on each event below and read
by a polite live region, the same mechanism as spec 01. In Learn it sits on the move list; in Practise
it sits on the verdict line (the line that shows "Book move ✓" and the like). Both keep their visible
text. When there is no announcement, the node is read as described above. `<Side>` is `White` or
`Black`: whoever made that move. `<own side>` is the side the player practises.

| Event | Announcement |
|---|---|
| An opening is opened from the list | `Italian Game. Learn. Start position.` |
| Learn: a step forward or back shows a move | `Move 3, White: knight f3.` (move number and side of the move now on the board) |
| Learn: a step back reaches the start | `Start position.` |
| Learn: a line chip is chosen | `Line: move 3, Black: bishop c5. Start position.` (the chip's spoken name after `Line: `, its first letter lowered) |
| Learn is chosen | `Learn. Start position.`, or `Learn. Move 3, White: knight f3.` when Learn was left partway |
| Practise is chosen, or Again | `Practise. Play White's moves from memory.`; when the book's first move is the opponent's, it follows: `Practise. Play Black's moves from memory. White played pawn e4.` |
| A book move that the book answers | `Book move. You played knight f3. Black played knight c6.`; the reply is the opponent's, so in a Black opening: `Book move. You played pawn c5. White played knight f3.` |
| A move that is not the book's | `You played pawn d4. Not the book move — the book plays knight f3.` |
| The move that ends the line | `Book move. You played bishop c4. Line complete.`, or `… Line complete — try it clean.` when a mistake was made; when the book answers that move first, its reply comes before `Line complete` as in the row above |
| Back to the list | nothing new |

Two identical sentences in a row may be spoken only once; that is accepted.

## Acceptance tests

1. The list: group titles are headings; a row is one node reading `Italian Game, C50` and, with
   clean runs, `…, 3 clean runs` and `…, 1 clean run`; clicking it opens the opening.
2. The header: Back exposes no arrow text; the name is a heading; the clean-run line reads as above.
3. The move list's spoken text for a line with an odd and with an even number of moves, and the
   spoken names of a White-move chip and a Black-move chip.
4. View-model tests for every row of the announcement table, exact sentences, for a White opening and
   for a Black one.
5. The Learn move list and the Practise verdict line each have a polite live region exposing the
   announcement.

No existing test's assertion changes. A test may change how it finds a node (for example
`useUnmergedTree = true`) where merging a row into one item changes which node a finder returns.

## Done when

The profile's suite passes: every test above, the 200-line cap, and 100% line coverage (CLAUDE.md
rules 1, 4 and 5). The screens look unchanged.

## Out of scope

- Anything spec 01 already did.
- New openings, new modes, hints in Practise beyond what exists.
- Any visual change, any new setting, any translation.
