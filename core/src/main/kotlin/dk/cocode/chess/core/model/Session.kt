package dk.cocode.chess.core.model

enum class PuzzleStatus { IN_PROGRESS, SOLVED, FAILED }

/** A hint revealing the next expected player move (from -> to). */
data class Hint(val from: Square, val to: Square)

/** Immutable snapshot the UI renders and the app reads for progress tracking. */
data class PuzzleSessionState(
    val puzzleId: String,
    val board: BoardView,
    val sideToMove: PieceColor,
    val playerColor: PieceColor,
    val status: PuzzleStatus,
    val playerMovesDone: Int,
    val totalPlayerMoves: Int,
    val lastMove: MoveStep,
    val isCheck: Boolean,
)

/** Result of submitting a player move. */
sealed interface SubmitResult {
    /** Correct, non-final move; the opponent has a reply queued (call applyOpponentReply). */
    data class Continues(val state: PuzzleSessionState, val playerMove: MoveStep) : SubmitResult

    /** Correct final move; the puzzle is solved. */
    data class Solved(val state: PuzzleSessionState, val playerMove: MoveStep) : SubmitResult

    /** A legal chess move that is not the solution; the puzzle is now failed. */
    data class Wrong(val state: PuzzleSessionState, val expected: MoveStep) : SubmitResult

    /** Not a legal move (or missing promotion / puzzle finished); engine state is unchanged. */
    data class Illegal(val reason: String) : SubmitResult
}
