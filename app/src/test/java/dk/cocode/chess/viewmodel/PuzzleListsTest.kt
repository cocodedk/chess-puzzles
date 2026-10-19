package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Which puzzles a band and a theme hold, on hand-made puzzles (the lists never look at the position). */
class PuzzleListsTest {
    private fun puzzle(id: String, rating: Int, themes: String) =
        Puzzle(id, "fen", listOf("e2e4", "e7e5"), rating, themes.split(' '))

    // Easy 0-2, Medium 3-4, Hard 5. Fork is in every band; pin is in none of Hard.
    private val lists = PuzzleLists(
        listOf(
            puzzle("e0", 800, "fork pin"), puzzle("e1", 900, "fork"), puzzle("e2", 1000, "pin"),
            puzzle("m0", 1500, "pin Fork"), puzzle("m1", 1600, "fork"), puzzle("h0", 2100, "fork"),
        ),
    )

    @Test fun onlyTheBandsAndThemesEveryBandHasAreOffered() {
        assertEquals(listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD), lists.difficulties)
        assertEquals(listOf(PuzzleTheme.FORK), lists.themes) // pin is missing from Hard
        val easyOnly = PuzzleLists(listOf(puzzle("e0", 800, "fork")))
        assertEquals(listOf(Difficulty.EASY), easyOnly.difficulties)
        assertEquals(listOf(PuzzleTheme.FORK), easyOnly.themes)
    }

    @Test fun aBandIsItsPuzzlesInBundleOrderAndAThemeTightensIt() {
        assertEquals(listOf(0, 1, 2), lists.of(Difficulty.EASY, null))
        assertEquals(listOf(0, 1), lists.of(Difficulty.EASY, PuzzleTheme.FORK))
        assertEquals(listOf(3, 4), lists.of(Difficulty.MEDIUM, null))
        assertEquals(listOf(3, 4), lists.of(Difficulty.MEDIUM, PuzzleTheme.FORK)) // the tag's case does not matter
        assertEquals(listOf(3), lists.of(Difficulty.MEDIUM, PuzzleTheme.PIN))
        assertEquals(emptyList<Int>(), lists.of(Difficulty.HARD, PuzzleTheme.PIN))
    }

    @Test fun aBandWithNoPuzzlesIsEmpty() {
        val easyOnly = PuzzleLists(listOf(puzzle("e0", 800, "fork")))
        assertEquals(emptyList<Int>(), easyOnly.of(Difficulty.HARD, null))
        assertEquals(emptyList<Int>(), easyOnly.of(Difficulty.HARD, PuzzleTheme.FORK))
        assertNull(easyOnly.start(Difficulty.HARD, null, emptySet()))
    }

    @Test fun aBandStartsAtItsFirstPuzzleWhatEverWasSolved() {
        assertEquals(0, lists.start(Difficulty.EASY, null, setOf(0, 1)))
    }

    @Test fun aThemeStartsAtItsFirstPuzzleNotSolvedYetOrTheFirstWhenAllAre() {
        assertEquals(0, lists.start(Difficulty.EASY, PuzzleTheme.FORK, emptySet()))
        assertEquals(1, lists.start(Difficulty.EASY, PuzzleTheme.FORK, setOf(0, 2)))
        assertEquals(0, lists.start(Difficulty.EASY, PuzzleTheme.FORK, setOf(0, 1)))
        assertNull(lists.start(Difficulty.HARD, PuzzleTheme.PIN, emptySet()))
    }
}
