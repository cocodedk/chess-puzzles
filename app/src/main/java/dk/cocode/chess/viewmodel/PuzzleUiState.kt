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
    override val board: List<String> = List(8) { "        " },
    override val flipped: Boolean = false,
    override val selected: Square? = null,
    override val legalTargets: Set<Square> = emptySet(),
    override val lastMove: Highlight? = null,
    override val hint: Highlight? = null,
    val pendingPromotion: PendingPromotion? = null,
    val status: PuzzleStatus = PuzzleStatus.IN_PROGRESS,
    val feedback: Feedback = Feedback.NONE,
    val rating: Int = 0,
    val position: Int = 0, // 1-based place in the difficulty band; the title's number
    val bandSize: Int = 0,
    val solvedCount: Int = 0,
    val hintFreeCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val dayStreak: Int = 0,
    val promptText: String = "",
) : BoardState
