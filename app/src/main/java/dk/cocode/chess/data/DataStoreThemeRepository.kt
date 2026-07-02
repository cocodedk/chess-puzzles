package dk.cocode.chess.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private val THEME = stringPreferencesKey("theme")

/** Unknown stored names (older versions, corruption) read as [ThemeMode.SYSTEM]. */
private fun themeModeOf(name: String?): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == name } ?: ThemeMode.SYSTEM

/** [ThemeRepository] backed by a Preferences DataStore. */
class DataStoreThemeRepository(
    private val dataStore: DataStore<Preferences>,
) : ThemeRepository {
    // catch: an unreadable prefs file must fall back to SYSTEM, not crash the splash-gated launch.
    // distinctUntilChanged: the store is shared with progress, so puzzle writes re-emit unchanged prefs.
    override val mode: Flow<ThemeMode> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> themeModeOf(prefs[THEME]) }
        .distinctUntilChanged()

    override suspend fun cycle() {
        // NonCancellable: a tapped toggle must land even if the activity is destroyed mid-write.
        withContext(NonCancellable) {
            dataStore.edit { prefs -> prefs[THEME] = themeModeOf(prefs[THEME]).next().name }
        }
    }
}
