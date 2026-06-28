package dk.cocode.chess.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
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
    fun defaultsThenAtomicUpdates() = runTest {
        val repository = DataStoreProgressRepository(newStore())
        assertEquals(Progress(0, 0, 0, 0), repository.progress.first())
        repository.recordSolved()
        repository.recordSolved()
        repository.recordFailed()
        repository.setIndex(5)
        assertEquals(Progress(2, 0, 2, 5), repository.progress.first())
    }
}
