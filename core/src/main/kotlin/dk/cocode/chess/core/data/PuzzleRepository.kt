package dk.cocode.chess.core.data

import dk.cocode.chess.core.model.Puzzle
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.random.Random

/** Read-only access to the bundled puzzle set. */
interface PuzzleRepository {
    fun count(): Int
    fun all(): List<Puzzle>
    fun getById(id: String): Puzzle?
    fun random(rng: Random = Random.Default): Puzzle
    fun next(afterId: String?): Puzzle?
    fun byRatingRange(range: IntRange): List<Puzzle>
    fun byTheme(theme: String): List<Puzzle>
}

/** In-memory [PuzzleRepository] backed by a parsed CSV stream. */
class CsvPuzzleRepository private constructor(private val puzzles: List<Puzzle>) : PuzzleRepository {
    private val byId = puzzles.associateBy { it.id }

    override fun count(): Int = puzzles.size

    override fun all(): List<Puzzle> = puzzles

    override fun getById(id: String): Puzzle? = byId[id]

    override fun random(rng: Random): Puzzle = puzzles[rng.nextInt(puzzles.size)]

    override fun next(afterId: String?): Puzzle? {
        if (afterId == null) return puzzles.firstOrNull()
        val index = puzzles.indexOfFirst { it.id == afterId }
        return if (index < 0 || index + 1 >= puzzles.size) null else puzzles[index + 1]
    }

    override fun byRatingRange(range: IntRange): List<Puzzle> = puzzles.filter { it.rating in range }

    override fun byTheme(theme: String): List<Puzzle> = puzzles.filter { it.hasTheme(theme) }

    companion object {
        /** The app supplies `{ context.assets.open("puzzles.csv") }`. */
        fun load(openStream: () -> InputStream): CsvPuzzleRepository {
            val reader = BufferedReader(InputStreamReader(openStream(), Charsets.UTF_8))
            try {
                return CsvPuzzleRepository(PuzzleCsvParser.parse(reader.lineSequence()).toList())
            } finally {
                reader.close()
            }
        }
    }
}
