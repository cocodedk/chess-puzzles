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
        repository.recordSolved()
        repository.recordSolved()
        repository.recordFailed()
        repository.setIndex(5)
        assertEquals(Progress(2, 0, 2, 5), repository.progress.first())
    }

    @Test
    fun corruptStoreReadsAsDefaultsInsteadOfCrashing() = runTest {
        val repository = DataStoreProgressRepository(corruptPreferencesStore("progress"))
        assertEquals(Progress(), repository.progress.first())
    }
}
