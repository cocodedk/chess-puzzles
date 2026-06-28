package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square

enum class Feedback { NONE, CORRECT, SOLVED, WRONG }

/** A from -> to pair used for last-move and hint highlighting. */
data class Highlight(val from: Square, val to: Square)

/** A pending pawn promotion awaiting the user's piece choice. */
data class PendingPromotion(val from: Square, val to: Square)

/** The complete, immutable state the puzzle screen renders. */
data class PuzzleUiState(
    val board: List<String> = List(8) { "        " },
    val flipped: Boolean = false,
    val selected: Square? = null,
    val legalTargets: Set<Square> = emptySet(),
    val lastMove: Highlight? = null,
    val hint: Highlight? = null,
    val pendingPromotion: PendingPromotion? = null,
    val status: PuzzleStatus = PuzzleStatus.IN_PROGRESS,
    val feedback: Feedback = Feedback.NONE,
    val rating: Int = 0,
    val solvedCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val promptText: String = "",
)
