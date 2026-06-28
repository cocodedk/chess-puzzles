package dk.cocode.chess

import android.app.Application
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.data.DataStoreProgressRepository
import dk.cocode.chess.data.ProgressRepository
import dk.cocode.chess.data.PuzzleAssetDataSource
import dk.cocode.chess.data.progressDataStore

/** Composition root: lazily builds the puzzle and progress repositories on first use. */
class ChessApp : Application() {
    val puzzles: PuzzleRepository by lazy { PuzzleAssetDataSource.load(this) }
    val progress: ProgressRepository by lazy { DataStoreProgressRepository(progressDataStore) }
}
