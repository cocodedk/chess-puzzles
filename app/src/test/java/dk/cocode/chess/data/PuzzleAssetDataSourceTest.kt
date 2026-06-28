package dk.cocode.chess.data

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PuzzleAssetDataSourceTest {
    @Test
    fun loadsBundledPuzzles() {
        val repository = PuzzleAssetDataSource.load(RuntimeEnvironment.getApplication())
        assertTrue(repository.count() > 1000)
    }
}
