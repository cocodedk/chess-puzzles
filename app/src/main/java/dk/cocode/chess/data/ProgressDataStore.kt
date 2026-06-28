package dk.cocode.chess.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Production DataStore instance (one per app process). */
val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

private val SOLVED = intPreferencesKey("solved")
private val STREAK = intPreferencesKey("streak")
private val BEST = intPreferencesKey("best")
private val INDEX = intPreferencesKey("index")

/** [ProgressRepository] backed by a Jetpack Preferences DataStore with atomic updates. */
class DataStoreProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : ProgressRepository {
    override val progress: Flow<Progress> = dataStore.data.map { prefs ->
        Progress(prefs[SOLVED] ?: 0, prefs[STREAK] ?: 0, prefs[BEST] ?: 0, prefs[INDEX] ?: 0)
    }

    override suspend fun recordSolved() {
        dataStore.edit { prefs ->
            val streak = (prefs[STREAK] ?: 0) + 1
            prefs[SOLVED] = (prefs[SOLVED] ?: 0) + 1
            prefs[STREAK] = streak
            prefs[BEST] = maxOf(prefs[BEST] ?: 0, streak)
        }
    }

    override suspend fun recordFailed() {
        dataStore.edit { prefs -> prefs[STREAK] = 0 }
    }

    override suspend fun setIndex(index: Int) {
        dataStore.edit { prefs -> prefs[INDEX] = index }
    }
}
