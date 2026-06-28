package dk.cocode.chess.core.data

import dk.cocode.chess.core.fixtures.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

class DataTest {
    @Test fun parseFullRow() {
        val puzzle = PuzzleCsvParser.parseLine(Fixtures.CSV_FULL)!!
        assertEquals("00sHx", puzzle.id)
        assertEquals(listOf("e8d7", "a2e6", "d7d8", "f7f8"), puzzle.uciMoves)
        assertEquals(1760, puzzle.rating)
        assertTrue(puzzle.hasTheme("mateIn2"))
    }

    @Test fun parseTrimmedRow() {
        val puzzle = PuzzleCsvParser.parseLine("00sHx,fenpart,e2e4 e7e5,1500,mate fork")!!
        assertEquals("00sHx", puzzle.id)
        assertEquals("fenpart", puzzle.fen)
        assertEquals(listOf("e2e4", "e7e5"), puzzle.uciMoves)
        assertEquals(1500, puzzle.rating)
        assertEquals(listOf("mate", "fork"), puzzle.themes)
    }

    @Test fun parseRejectsBadLines() {
        assertNull(PuzzleCsvParser.parseLine("PuzzleId,FEN,Moves,Rating,Themes"))
        assertNull(PuzzleCsvParser.parseLine("   "))
        assertNull(PuzzleCsvParser.parseLine("a,b"))
        assertNull(PuzzleCsvParser.parseLine("id,fen,e2e4 e7e5,NaN,mate")) // bad rating
        assertNull(PuzzleCsvParser.parseLine("id,fen,,1500,mate")) // no moves
        assertNull(PuzzleCsvParser.parseLine(",fen,e2e4 e7e5,1500,mate")) // blank id
        assertNull(PuzzleCsvParser.parseLine("id,,e2e4 e7e5,1500,mate")) // blank fen
        assertNull(PuzzleCsvParser.parseLine("id,fen,e2e4,1500,mate")) // only the setup move
        assertNull(PuzzleCsvParser.parseLine("id,fen,e2e4 z9z9,1500,mate")) // invalid UCI token
    }

    @Test fun parseAcceptsPromotionMove() {
        val puzzle = PuzzleCsvParser.parseLine("id,fen,e2e4 e7e8q,1500,mate")!!
        assertEquals(listOf("e2e4", "e7e8q"), puzzle.uciMoves)
    }

    @Test fun parseSequenceDropsMalformed() {
        val lines = sequenceOf(
            "PuzzleId,FEN,Moves,Rating,Themes",
            "a,fenA,e2e4 e7e5,1500,mate",
            "garbage",
            "   ",
            "b,fenB,d2d4 d7d5,1600,fork",
        )
        val puzzles = PuzzleCsvParser.parse(lines).toList()
        assertEquals(2, puzzles.size)
        assertEquals("a", puzzles[0].id)
        assertEquals("b", puzzles[1].id)
    }

    private fun repo(): CsvPuzzleRepository {
        val csv = """
            PuzzleId,FEN,Moves,Rating,Themes
            a,fenA,e2e4 e7e5,1200,mate fork
            b,fenB,d2d4 d7d5,1600,pin
            c,fenC,g1f3 g8f6,2000,mate
        """.trimIndent()
        return CsvPuzzleRepository.load { csv.byteInputStream() }
    }

    @Test fun repositoryBasics() {
        val repository = repo()
        assertEquals(3, repository.count())
        assertEquals(3, repository.all().size)
        assertEquals("b", repository.getById("b")?.id)
        assertNull(repository.getById("zzz"))
    }

    @Test fun repositoryRandomDeterministic() {
        val repository = repo()
        assertEquals(repository.random(Random(42)).id, repository.random(Random(42)).id)
    }

    @Test fun repositoryNext() {
        val repository = repo()
        assertEquals("a", repository.next(null)?.id)
        assertEquals("b", repository.next("a")?.id)
        assertNull(repository.next("c"))
        assertNull(repository.next("zzz"))
    }

    @Test fun repositoryFilters() {
        val repository = repo()
        assertEquals(listOf("b"), repository.byRatingRange(1500..1800).map { it.id })
        assertEquals(listOf("a", "c"), repository.byTheme("mate").map { it.id })
        assertTrue(repository.byTheme("nonsense").isEmpty())
    }

    @Test fun parseTrimsExtraThemeSpaces() {
        val puzzle = PuzzleCsvParser.parseLine("id,fen,e2e4 e7e5,1500,mate  fork ")!!
        assertEquals(listOf("mate", "fork"), puzzle.themes)
    }

    @Test fun repositoryRandomUsesDefaultRng() {
        val repository: PuzzleRepository = repo()
        assertTrue(repository.all().contains(repository.random()))
    }

    @Test fun loadPropagatesStreamErrors() {
        val failing = object : InputStream() {
            override fun read(): Int = throw IOException("boom")
        }
        assertThrows(IOException::class.java) { CsvPuzzleRepository.load { failing } }
    }
}
