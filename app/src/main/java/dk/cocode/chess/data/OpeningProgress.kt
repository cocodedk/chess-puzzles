package dk.cocode.chess.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/** How many times each opening has been played through without a mistake. */
interface OpeningProgressRepository {
    /** Clean run-throughs by opening id; an opening never played cleanly is absent. */
    val cleanRuns: Flow<Map<String, Int>>

    suspend fun recordCleanRun(openingId: String)
}

private const val PREFIX = "opening_clean_"

private fun cleanKey(openingId: String) = intPreferencesKey(PREFIX + openingId)

/** [OpeningProgressRepository] in the app's shared DataStore, one `opening_clean_<id>` key per opening. */
class DataStoreOpeningProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : OpeningProgressRepository {
    // As for puzzle progress: an unreadable file reads as empty, and other writes to the shared store
    // must not re-emit an unchanged map.
    override val cleanRuns: Flow<Map<String, Int>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs.asMap().entries
                .filter { it.key.name.startsWith(PREFIX) }
                .associate { it.key.name.removePrefix(PREFIX) to it.value as Int }
        }
        .distinctUntilChanged()

    override suspend fun recordCleanRun(openingId: String) {
        dataStore.edit { prefs -> prefs[cleanKey(openingId)] = (prefs[cleanKey(openingId)] ?: 0) + 1 }
    }
}
