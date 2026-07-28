package dk.cocode.chess.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/** The app-wide DataStore (progress + settings). On-disk name stays "progress" for compatibility. */
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")

private val SOLVED = intPreferencesKey("solved")
private val STREAK = intPreferencesKey("streak")
private val BEST = intPreferencesKey("best")
private val INDEX = intPreferencesKey("index")
private val DAY_STREAK = intPreferencesKey("dayStreak")
private val LAST_SOLVED_DAY = longPreferencesKey("lastSolvedDay")

private fun Preferences.toProgress() = Progress(
    this[SOLVED] ?: 0, this[STREAK] ?: 0, this[BEST] ?: 0, this[INDEX] ?: 0,
    this[DAY_STREAK] ?: 0, this[LAST_SOLVED_DAY] ?: 0L,
)

private fun MutablePreferences.write(progress: Progress) {
    this[SOLVED] = progress.solvedCount
    this[STREAK] = progress.currentStreak
    this[BEST] = progress.bestStreak
    this[INDEX] = progress.index
    this[DAY_STREAK] = progress.dayStreak
    this[LAST_SOLVED_DAY] = progress.lastSolvedDay
}

/** [ProgressRepository] backed by a Jetpack Preferences DataStore with atomic updates. */
class DataStoreProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : ProgressRepository {
    // catch: an unreadable prefs file must fall back to defaults, not crash the collector.
    // distinctUntilChanged: the store is shared with settings, so theme writes re-emit unchanged prefs.
    override val progress: Flow<Progress> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs.toProgress() }
        .distinctUntilChanged()

    override suspend fun recordSolved(epochDay: Long) {
        dataStore.edit { prefs -> prefs.write(prefs.toProgress().solvedOn(epochDay)) }
    }

    override suspend fun recordFailed() {
        dataStore.edit { prefs -> prefs[STREAK] = 0 }
    }

    override suspend fun setIndex(index: Int) {
        dataStore.edit { prefs -> prefs[INDEX] = index }
    }
}
