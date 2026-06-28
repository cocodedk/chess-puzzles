package dk.cocode.chess.core.fixtures

import dk.cocode.chess.core.engine.PuzzleSession
import dk.cocode.chess.core.model.MoveIntent
import dk.cocode.chess.core.model.PieceType
import dk.cocode.chess.core.model.Puzzle
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.core.model.SubmitResult

/** Shared, hand-verified puzzle fixtures and helpers for the core test suite. */
object Fixtures {
    /** Real Lichess puzzle 00sHx (mateIn2). Player = WHITE. */
    val MATE_IN_2 = Puzzle(
        id = "00sHx",
        fen = "q3k1nr/1pp1nQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 b k - 0 17",
        uciMoves = listOf("e8d7", "a2e6", "d7d8", "f7f8"),
        rating = 1760,
        themes = listOf("mate", "mateIn2", "short"),
    )

    /** Constructed mate-in-1 with two distinct mating moves (b7g7 and b7h7). Player = WHITE. */
    val MATE_IN_1_MULTI = Puzzle(
        id = "M1MUL",
        fen = "6k1/1Q6/6K1/8/8/8/8/8 b - - 0 1",
        uciMoves = listOf("g8h8", "b7g7"),
        rating = 800,
        themes = listOf("mate", "mateIn1"),
    )

    /** Constructed promotion puzzle (not mate). Player = WHITE. */
    val PROMOTION = Puzzle(
        id = "PROMO",
        fen = "8/p3P3/8/8/8/2k5/8/6K1 b - - 0 1",
        uciMoves = listOf("a7a6", "e7e8q"),
        rating = 1000,
        themes = listOf("promotion", "endgame"),
    )

    /** Constructed mate-in-1 where the player is BLACK (FEN is white-to-move). */
    val MATE_IN_1_BLACK = Puzzle(
        id = "M1BLK",
        fen = "8/8/8/8/8/6k1/1q6/6K1 w - - 0 1",
        uciMoves = listOf("g1h1", "b2g2"),
        rating = 800,
        themes = listOf("mate", "mateIn1"),
    )

    const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    const val CSV_FULL =
        "00sHx,q3k1nr/1pp1nQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 b k - 0 17," +
            "e8d7 a2e6 d7d8 f7f8,1760,80,83,72,mate mateIn2 short," +
            "https://lichess.org/abcd/black#34,Italian_Game"
}

fun intentOf(uci: String): MoveIntent {
    val u = uci.lowercase()
    val promotion = if (u.length == 5) {
        when (u[4]) {
            'q' -> PieceType.QUEEN
            'r' -> PieceType.ROOK
            'b' -> PieceType.BISHOP
            'n' -> PieceType.KNIGHT
            else -> null
        }
    } else {
        null
    }
    return MoveIntent(Square.of(u.substring(0, 2)), Square.of(u.substring(2, 4)), promotion)
}

/** Plays a puzzle's full solution line, asserting it reaches [SubmitResult.Solved]. */
fun solveFully(session: PuzzleSession) {
    val solution = session.puzzle.solutionUciMoves
    var i = 0
    while (i < solution.size) {
        when (val result = session.submitMove(intentOf(solution[i]))) {
            is SubmitResult.Continues -> {
                session.applyOpponentReply()
                i += 2
            }
            is SubmitResult.Solved -> return
            else -> error("Unexpected $result at solution index $i (${solution[i]})")
        }
    }
}
