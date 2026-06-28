package dk.cocode.chess.core.data

import dk.cocode.chess.core.model.Puzzle

/** Parses Lichess puzzle CSV rows: the 10-column full export or the 5-column trimmed asset. */
object PuzzleCsvParser {
    private val UCI_MOVE = Regex("[a-h][1-8][a-h][1-8][qrbn]?")

    fun parseLine(line: String): Puzzle? {
        val text = line.trim()
        if (text.isEmpty() || text.startsWith("PuzzleId,")) return null
        val cols = text.split(',')
        if (cols.size < 5) return null
        val moves = cols[2].trim().split(' ').filter { it.isNotEmpty() }
        val rating = cols[3].toIntOrNull()
        if (cols[0].isBlank() || cols[1].isBlank() || rating == null) return null
        // A puzzle needs the opponent setup move plus at least one player move, all valid UCI.
        if (moves.size < 2 || !moves.all(UCI_MOVE::matches)) return null
        val themes = words(if (cols.size >= 10) cols[7] else cols[4])
        return Puzzle(id = cols[0], fen = cols[1], uciMoves = moves, rating = rating, themes = themes)
    }

    fun parse(lines: Sequence<String>): Sequence<Puzzle> = lines.mapNotNull(::parseLine)

    private fun words(field: String): List<String> =
        field.trim().split(' ').filter { it.isNotEmpty() }
}
