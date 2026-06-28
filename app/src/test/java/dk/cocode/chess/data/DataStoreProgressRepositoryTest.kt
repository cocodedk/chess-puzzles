package dk.cocode.chess.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DataStoreProgressRepositoryTest {
    private fun newStore(): DataStore<Preferences> {
        val dir = RuntimeEnvironment.getApplication().cacheDir
        val file = File.createTempFile("progress", ".preferences_pb", dir).apply { delete() }
        return PreferenceDataStoreFactory.create { file }
    }

    @Test
    fun defaultsToZerosThenPersists() = runTest {
        val repository = DataStoreProgressRepository(newStore())
        assertEquals(Progress(0, 0, 0), repository.load())
        repository.save(Progress(4, 2, 5))
        assertEquals(Progress(4, 2, 5), repository.load())
    }
}
