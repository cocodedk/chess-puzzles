package dk.cocode.chess.data

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dk.cocode.chess.corruptPreferencesStore
import dk.cocode.chess.newPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreThemeRepositoryTest {
    @Test
    fun defaultsToSystemAndCyclesAtomically() = runTest {
        val repository = DataStoreThemeRepository(newPreferencesStore("theme"))
        assertEquals(ThemeMode.SYSTEM, repository.mode.first())
        repository.cycle()
        assertEquals(ThemeMode.LIGHT, repository.mode.first())
        repository.cycle()
        assertEquals(ThemeMode.DARK, repository.mode.first())
        repository.cycle()
        assertEquals(ThemeMode.SYSTEM, repository.mode.first())
    }

    @Test
    fun corruptStoreReadsAsSystemInsteadOfCrashing() = runTest {
        val repository = DataStoreThemeRepository(corruptPreferencesStore("theme"))
        assertEquals(ThemeMode.SYSTEM, repository.mode.first())
    }

    @Test
    fun unknownStoredValueReadsAsSystemAndCyclesToLight() = runTest {
        val store = newPreferencesStore("theme")
        store.edit { it[stringPreferencesKey("theme")] = "SEPIA" }
        val repository = DataStoreThemeRepository(store)
        assertEquals(ThemeMode.SYSTEM, repository.mode.first())
        repository.cycle()
        assertEquals(ThemeMode.LIGHT, repository.mode.first())
    }
}
