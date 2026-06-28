# DATA — puzzles

## Source

Puzzles come from the **Lichess Open Database** (https://database.lichess.org), file
`lichess_db_puzzle.csv.zst`, released into the public domain under **CC0 1.0**.

## Pipeline

`scripts/sample_puzzles.py` streams the database and produces the bundled asset:

```bash
curl -fL -o lichess_db_puzzle.csv.zst https://database.lichess.org/lichess_db_puzzle.csv.zst
zstdcat lichess_db_puzzle.csv.zst | python3 scripts/sample_puzzles.py - app/src/main/assets/puzzles.csv
```

It keeps community-approved puzzles (popularity ≥ 85, plays ≥ 60) carrying common tactical themes
(mate/fork/pin/skewer/…), **stratified-samples up to 400 per rating band** across six bands, and
writes a trimmed 5-column CSV sorted easy → hard:

```
PuzzleId,FEN,Moves,Rating,Themes
```

Result: **2,400 puzzles, ~330 KB**, ratings ≈ 400–3100. Re-running reproduces the same set
(`random.seed(20260628)`).

## Move convention

The `FEN` is the position **before** the opponent's setup move. `Moves` is a UCI sequence whose
**first move is the opponent's** (applied automatically on load); the player solves from the second
move onward (player, opponent, player, …). Mate-in-1 puzzles accept any mating move.

## Attribution / licenses

- Puzzles — Lichess Open Database, **CC0 1.0** (public domain).
- Chess rules — **chesslib** © Ben-Hur Carlos Vieira Langoni Junior, **Apache License 2.0**.
