package dk.cocode.chess.data

import kotlinx.coroutines.flow.Flow

/** The player's day/night preference: follow the system setting, or force light/dark. */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    /** Whether the app should render dark, given the current system dark-mode setting. */
    fun resolvesToDark(systemDark: Boolean): Boolean = this == DARK || (this == SYSTEM && systemDark)

    /** The next mode in the toggle cycle System → Light → Dark → System. */
    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]
}

interface ThemeRepository {
    /** Emits the persisted mode and every subsequent change; defaults to [ThemeMode.SYSTEM]. */
    val mode: Flow<ThemeMode>

    /** Atomically advances to the next mode, so rapid toggles never read stale UI state. */
    suspend fun cycle()
}
