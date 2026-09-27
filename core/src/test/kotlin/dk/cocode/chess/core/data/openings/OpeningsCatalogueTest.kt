package dk.cocode.chess.core.data.openings

import dk.cocode.chess.core.engine.OpeningTree
import dk.cocode.chess.core.model.OpeningGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpeningsCatalogueTest {
    /** One row of Lichess's CC0 opening list (test resources only): its ECO code, name and moves in SAN. */
    private data class LichessRow(val eco: String, val name: String, val moves: List<String>)

    private val lichess: List<LichessRow> = "abcde".flatMap { file ->
        javaClass.getResourceAsStream("/lichess-openings/$file.tsv")!!.bufferedReader().readLines().drop(1).map { row ->
            val (eco, name, pgn) = row.split('\t')
            LichessRow(eco, name, pgn.split(' ').filterNot { it.first().isDigit() }) // drop "1." move numbers
        }
    }

    @Test fun twentyOpeningsWithDistinctIds() {
        assertEquals(20, Openings.all.size)
        assertEquals(20, Openings.all.map { it.id }.toSet().size)
    }

    @Test fun everyGroupIsCovered() {
        val sizes = Openings.all.groupingBy { it.group }.eachCount()
        assertEquals(mapOf(OpeningGroup.WHITE to 8, OpeningGroup.BLACK_VS_E4 to 7, OpeningGroup.BLACK_VS_D4 to 5), sizes)
    }

    /** Building the tree plays every line through the chess rules and enforces the one-book-move rule. */
    @Test fun everyLineIsLegalAndBranchesOnlyOnTheOpponentsMoves() {
        Openings.all.forEach { opening ->
            val tree = OpeningTree(opening)
            assertEquals(opening.lines.size, tree.lineNames.toSet().size, "${opening.id}: each branch has its own name")
        }
    }

    /**
     * The name and ECO code are Lichess's own, for a position every line of the opening passes through:
     * a row with that code and name whose moves open every line.
     */
    @Test fun everyNameAndEcoCodeIsLichesss() {
        Openings.all.forEach { opening ->
            val lines = opening.lines.map { it.split(' ') }
            val match = lichess.any { row ->
                row.eco == opening.eco && row.name == opening.name && lines.all { it.take(row.moves.size) == row.moves }
            }
            assertTrue(match, "${opening.id}: no Lichess row '${opening.eco} ${opening.name}' opens all its lines")
        }
    }

    @Test fun everyOpeningExplainsItsIdea() {
        Openings.all.forEach { assertTrue(it.idea.length in 40..120, "${it.id}: a one-sentence idea") }
    }
}
