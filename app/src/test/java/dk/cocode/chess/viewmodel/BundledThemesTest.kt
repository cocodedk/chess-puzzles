package dk.cocode.chess.viewmodel

import dk.cocode.chess.data.PuzzleAssetDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** The themes offered are the ones the bundled puzzles can fill: the claim made in [PuzzleTheme]'s comment. */
@RunWith(RobolectricTestRunner::class)
class BundledThemesTest {
    private val lists = PuzzleLists(PuzzleAssetDataSource.load(RuntimeEnvironment.getApplication()).all())

    @Test fun everyThemeOfferedIsInTheBundleAndEveryTagIsDistinct() {
        assertEquals(PuzzleTheme.entries, lists.themes)
        assertEquals(PuzzleTheme.entries.size, PuzzleTheme.entries.map { it.tag }.toSet().size)
        assertTrue(PuzzleTheme.entries.size in 10..14)
    }

    @Test fun everyThemeHasAtLeastFifteenPuzzlesInEveryBand() {
        for (band in Difficulty.entries) for (theme in PuzzleTheme.entries) {
            assertTrue("$theme in $band", lists.of(band, theme).size >= 15)
        }
    }
}
