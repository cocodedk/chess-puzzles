package dk.cocode.chess.viewmodel

/**
 * What the player has spent on the puzzle currently on screen. The flags live from one puzzle load
 * to the next — a reset keeps them, so a hint already seen still disqualifies the eventual solve.
 */
internal class PuzzleAttempt {
    /** A wrong move not yet redeemed by a solve; skipping such a puzzle breaks the streak. */
    var failed = false

    /** The hint was revealed, so a solve from here is not hint-free. */
    var hintUsed = false
}
