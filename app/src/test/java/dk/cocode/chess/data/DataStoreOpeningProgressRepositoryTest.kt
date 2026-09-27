package dk.cocode.chess.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dk.cocode.chess.corruptPreferencesStore
import dk.cocode.chess.newPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreOpeningProgressRepositoryTest {
    @Test
    fun countsCleanRunsPerOpening() = runTest {
        val repository = DataStoreOpeningProgressRepository(newPreferencesStore("openings"))
        assertEquals(emptyMap<String, Int>(), repository.cleanRuns.first())
        repository.recordCleanRun("italian")
        repository.recordCleanRun("italian")
        repository.recordCleanRun("slav")
        assertEquals(mapOf("italian" to 2, "slav" to 1), repository.cleanRuns.first())
    }

    @Test
    fun ignoresThePuzzleProgressItSharesTheStoreWith() = runTest {
        val store = newPreferencesStore("shared")
        store.edit { it[intPreferencesKey("solved")] = 7 }
        val repository = DataStoreOpeningProgressRepository(store)
        repository.recordCleanRun("french")
        assertEquals(mapOf("french" to 1), repository.cleanRuns.first())
    }

    @Test
    fun corruptStoreReadsAsNoRunsInsteadOfCrashing() = runTest {
        val repository = DataStoreOpeningProgressRepository(corruptPreferencesStore("openings"))
        assertEquals(emptyMap<String, Int>(), repository.cleanRuns.first())
    }
}
