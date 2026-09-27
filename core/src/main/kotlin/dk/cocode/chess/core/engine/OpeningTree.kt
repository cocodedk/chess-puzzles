package dk.cocode.chess.core.engine

import dk.cocode.chess.core.model.BoardView
import dk.cocode.chess.core.model.MoveStep
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.util.Uci

/** One move of an opening line: [board] is the position right after [move] is played. */
data class Ply(val san: String, val move: MoveStep, val board: BoardView)

/**
 * Merges an [Opening]'s SAN [Opening.lines] into a prefix tree of plies, one node per position
 * reached. A node may have more than one child only when it is the opponent's turn to move — the
 * player, per [Opening.side], always has exactly one book move — which keeps [OpeningDrill] able to
 * pick a single "correct" reply for the player at every one of their turns.
 */
class OpeningTree(opening: Opening) {
    /** A merged tree node; keyed by the UCI move leading into each child. */
    internal class Node {
        val children = LinkedHashMap<String, Node>()
    }

    /** One opening line's SAN tokens, parsed UCI moves, and plies, kept together so the three
     * stay in lockstep instead of three separately-indexed parallel lists. */
    private class Line(val san: List<String>, val uci: List<String>, val plies: List<Ply>)

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }

    internal val root = Node()

    private val lines: List<Line> = opening.lines.map { line ->
        val uci = ChessEngine.sanLineToUci(line)
        val san = line.trim().split(WHITESPACE).filter { it.isNotBlank() }
        Line(san, uci, repliesFor(uci, san))
    }

    val start: BoardView = ChessEngine().boardView()

    init {
        opening.lines.forEachIndexed { i, line ->
            require(lines[i].uci.isNotEmpty()) { "Opening ${opening.id}: line ${i + 1} \"$line\" is empty" }
        }
        lines.forEach { merge(it.uci, opening) }
    }

    // Computed after the init block above, so the tree is fully merged before names are derived.
    val lineNames: List<String> =
        if (lines.size == 1) {
            listOf("Main line")
        } else {
            lines.map { line ->
                val ply = divergeIndex(line.uci)
                formatMoveName(ply, line.san[ply])
            }
        }

    fun plies(line: Int): List<Ply> = lines[line].plies

    private fun merge(ucis: List<String>, opening: Opening) {
        var node = root
        ucis.forEachIndexed { i, uci ->
            val sideToMove = if (i % 2 == 0) PieceColor.WHITE else PieceColor.BLACK
            node = node.children.getOrPut(uci) {
                require(sideToMove != opening.side || node.children.isEmpty()) {
                    "Opening ${opening.id}: two book moves for ${opening.side} at ply ${i + 1}"
                }
                Node()
            }
        }
    }

    /** The deepest branch node along [ucis]'s path — the ply where this line becomes unique. */
    private fun divergeIndex(ucis: List<String>): Int {
        var node = root
        var last = 0
        ucis.forEachIndexed { i, uci ->
            if (node.children.size > 1) last = i
            node = node.children.getValue(uci)
        }
        return last
    }

    private fun formatMoveName(ply: Int, san: String): String {
        val moveNumber = ply / 2 + 1
        return if (ply % 2 == 0) "$moveNumber.$san" else "$moveNumber…$san"
    }

    private fun repliesFor(ucis: List<String>, sans: List<String>): List<Ply> {
        val engine = ChessEngine()
        return ucis.mapIndexed { i, uci ->
            engine.applyUci(uci)
            Ply(sans[i], Uci.toMoveStep(uci), engine.boardView())
        }
    }
}
