#!/usr/bin/env python3
"""Stratified reservoir sampling of the Lichess puzzle DB into a trimmed 5-column asset.

Usage: sample_puzzles.py <input.csv|-> <output.csv>

Input  columns: PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes,GameUrl,OpeningTags
Output columns: PuzzleId,FEN,Moves,Rating,Themes

Puzzles are from the Lichess Open Database (https://database.lichess.org), CC0 1.0 public domain.
"""
import csv
import sys
import random

random.seed(20260628)

BANDS = [(0, 1000), (1000, 1400), (1400, 1800), (1800, 2200), (2200, 2600), (2600, 10000)]
PER_BAND = 400
MIN_POPULARITY = 85
MIN_PLAYS = 60
WANTED_THEMES = {
    "mate", "mateIn1", "mateIn2", "mateIn3", "fork", "pin", "skewer",
    "discoveredAttack", "doubleCheck", "sacrifice", "hangingPiece",
    "backRankMate", "deflection", "trappedPiece",
}


def band_of(rating):
    for low, high in BANDS:
        if low <= rating < high:
            return (low, high)
    return None


def main():
    source = sys.stdin if sys.argv[1] == "-" else open(sys.argv[1], newline="")
    reader = csv.reader(source)
    next(reader, None)  # drop header

    buckets = {b: [] for b in BANDS}
    seen = {b: 0 for b in BANDS}

    for row in reader:
        if len(row) < 8:
            continue
        pid, fen, moves, rating, _dev, popularity, plays, themes = row[:8]
        try:
            r, pop, n = int(rating), int(popularity), int(plays)
        except ValueError:
            continue
        if pop < MIN_POPULARITY or n < MIN_PLAYS or not moves.strip():
            continue
        if WANTED_THEMES.isdisjoint(themes.split()):
            continue
        band = band_of(r)
        if band is None:
            continue
        seen[band] += 1
        record = [pid, fen, moves, rating, themes]
        bucket = buckets[band]
        if len(bucket) < PER_BAND:
            bucket.append(record)
        else:
            j = random.randint(0, seen[band] - 1)
            if j < PER_BAND:
                bucket[j] = record

    selected = [rec for band in BANDS for rec in buckets[band]]
    selected.sort(key=lambda rec: int(rec[3]))  # easy -> hard

    with open(sys.argv[2], "w", newline="") as out_file:
        writer = csv.writer(out_file)
        writer.writerow(["PuzzleId", "FEN", "Moves", "Rating", "Themes"])
        writer.writerows(selected)
    sys.stderr.write(f"wrote {len(selected)} puzzles\n")


if __name__ == "__main__":
    main()
