package dk.cocode.chess.data

import dk.cocode.chess.corruptPreferencesStore
import dk.cocode.chess.newPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreProgressRepositoryTest {
    @Test
    fun defaultsThenAtomicUpdates() = runTest {
        val repository = DataStoreProgressRepository(newPreferencesStore("progress"))
        assertEquals(Progress(0, 0, 0, 0), repository.progress.first())
        repository.recordSolved(10, hintFree = true)
        repository.recordSolved(10, hintFree = false)
        repository.recordFailed()
        repository.setIndex(5)
        // Only the first solve was hint-free, and that tally survives the round trip through disk.
        assertEquals(Progress(2, 0, 2, 5, 1, 10, 1), repository.progress.first())
    }

    @Test
    fun dayStreakExtendsOnConsecutiveDaysAndRestartsAfterAGap() = runTest {
        val repository = DataStoreProgressRepository(newPreferencesStore("days"))
        repository.recordSolved(10, hintFree = true)
        repository.recordSolved(11, hintFree = true)
        assertEquals(2, repository.progress.first().dayStreak)
        repository.recordSolved(13, hintFree = true)
        assertEquals(Progress(3, 3, 3, 0, 1, 13, 3), repository.progress.first())
    }

    @Test
    fun corruptStoreReadsAsDefaultsInsteadOfCrashing() = runTest {
        val repository = DataStoreProgressRepository(corruptPreferencesStore("progress"))
        assertEquals(Progress(), repository.progress.first())
    }
}
