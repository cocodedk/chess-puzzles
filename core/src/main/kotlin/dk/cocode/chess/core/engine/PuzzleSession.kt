package dk.cocode.chess.core.engine

import dk.cocode.chess.core.model.Hint
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.core.model.PuzzleSessionState
import dk.cocode.chess.core.model.PuzzleStatus
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.model.SubmitResult
import dk.cocode.chess.core.util.Uci
import dk.cocode.chess.core.util.toUci

/**
 * Pure state machine for solving one [Puzzle]. On [start] the opponent's setup move is applied,
 * leaving the player to move; player moves are validated against the solution and the opponent's
 * scripted replies are applied via [applyOpponentReply].
 */
class PuzzleSession private constructor(
    val puzzle: Puzzle,
    private val engine: ChessEngine,
    val playerColor: PieceColor,
    private val finalMoveIsMate: Boolean,
) {
    private var cursor = 1
    private var status = PuzzleStatus.IN_PROGRESS
    private var lastMove: MoveStep = Uci.toMoveStep(puzzle.setupMoveUci)
    private var pendingReply: String? = null

    var state: PuzzleSessionState = buildState()
        private set

    fun legalDestinations(from: Square): List<Square> =
        if (status == PuzzleStatus.IN_PROGRESS) engine.legalDestinations(from) else emptyList()

    fun requiresPromotion(from: Square, to: Square): Boolean = engine.requiresPromotion(from, to)

    fun currentFen(): String = engine.fen()

    fun hint(): Hint {
        val step = Uci.toMoveStep(puzzle.uciMoves[cursor])
        return Hint(step.from, step.to)
    }

    fun submitMove(move: MoveIntent): SubmitResult {
        if (status != PuzzleStatus.IN_PROGRESS) return SubmitResult.Illegal("Puzzle already $status")
        if (engine.requiresPromotion(move.from, move.to) && move.promotion == null) {
            return SubmitResult.Illegal("Promotion piece required")
        }
        if (!engine.isLegal(move)) return SubmitResult.Illegal("Illegal move")

        val candidate = move.toUci()
        val expected = puzzle.uciMoves[cursor]
        val isFinalPly = cursor == puzzle.uciMoves.lastIndex
        val accepted = candidate == expected ||
            (isFinalPly && finalMoveIsMate && engine.wouldBeMate(candidate))
        if (!accepted) {
            status = PuzzleStatus.FAILED
            state = buildState()
            return SubmitResult.Wrong(state, Uci.toMoveStep(expected))
        }

        engine.applyUci(candidate)
        lastMove = Uci.toMoveStep(candidate)
        cursor += 1
        return if (cursor > puzzle.uciMoves.lastIndex) {
            status = PuzzleStatus.SOLVED
            state = buildState()
            SubmitResult.Solved(state, lastMove)
        } else {
            pendingReply = puzzle.uciMoves[cursor]
            state = buildState()
            SubmitResult.Continues(state, lastMove)
        }
    }

    fun applyOpponentReply(): MoveStep {
        val replyUci = checkNotNull(pendingReply) { "No opponent reply pending" }
        engine.applyUci(replyUci)
        lastMove = Uci.toMoveStep(replyUci)
        pendingReply = null
        cursor += 1
        state = buildState()
        return lastMove
    }

    fun reset(): PuzzleSessionState {
        engine.loadFen(puzzle.fen)
        engine.applyUci(puzzle.setupMoveUci)
        cursor = 1
        status = PuzzleStatus.IN_PROGRESS
        lastMove = Uci.toMoveStep(puzzle.setupMoveUci)
        pendingReply = null
        state = buildState()
        return state
    }

    private fun buildState(): PuzzleSessionState =
        PuzzleSessionState(
            puzzleId = puzzle.id,
            board = engine.boardView(),
            sideToMove = engine.sideToMove(),
            playerColor = playerColor,
            status = status,
            playerMovesDone = cursor / 2,
            totalPlayerMoves = puzzle.playerMoveCount,
            lastMove = lastMove,
            isCheck = engine.isCheck(),
        )

    companion object {
        fun start(puzzle: Puzzle): PuzzleSession {
            val engine = ChessEngine()
            engine.loadFen(puzzle.fen)
            engine.applyUci(puzzle.setupMoveUci)
            return PuzzleSession(puzzle, engine, engine.sideToMove(), finalMoveIsMate(puzzle))
        }

        private fun finalMoveIsMate(puzzle: Puzzle): Boolean {
            val probe = ChessEngine()
            probe.loadFen(puzzle.fen)
            puzzle.uciMoves.dropLast(1).forEach { probe.applyUci(it) }
            return probe.wouldBeMate(puzzle.uciMoves.last())
        }
    }
}
