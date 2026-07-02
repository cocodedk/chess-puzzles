package dk.cocode.chess

import android.app.Application
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.data.DataStoreProgressRepository
import dk.cocode.chess.data.DataStoreThemeRepository
import dk.cocode.chess.data.ProgressRepository
import dk.cocode.chess.data.PuzzleAssetDataSource
import dk.cocode.chess.data.ThemeRepository
import dk.cocode.chess.data.appDataStore

/** Composition root: lazily builds the puzzle, progress and theme repositories on first use. */
class ChessApp : Application() {
    val puzzles: PuzzleRepository by lazy { PuzzleAssetDataSource.load(this) }
    val progress: ProgressRepository by lazy { DataStoreProgressRepository(appDataStore) }
    val theme: ThemeRepository by lazy { DataStoreThemeRepository(appDataStore) }
}
