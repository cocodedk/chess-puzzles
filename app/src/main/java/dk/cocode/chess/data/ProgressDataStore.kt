package dk.cocode.chess.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/** Production DataStore instance (one per app process). */
val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

private val SOLVED = intPreferencesKey("solved")
private val STREAK = intPreferencesKey("streak")
private val BEST = intPreferencesKey("best")

/** [ProgressRepository] backed by a Jetpack Preferences DataStore. */
class DataStoreProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : ProgressRepository {
    override suspend fun load(): Progress {
        val prefs = dataStore.data.first()
        return Progress(prefs[SOLVED] ?: 0, prefs[STREAK] ?: 0, prefs[BEST] ?: 0)
    }

    override suspend fun save(progress: Progress) {
        dataStore.edit { prefs ->
            prefs[SOLVED] = progress.solvedCount
            prefs[STREAK] = progress.currentStreak
            prefs[BEST] = progress.bestStreak
        }
    }
}
