package dk.cocode.chess.data

/** Persisted player progress across sessions. */
data class Progress(val solvedCount: Int, val currentStreak: Int, val bestStreak: Int)

interface ProgressRepository {
    suspend fun load(): Progress
    suspend fun save(progress: Progress)
}
