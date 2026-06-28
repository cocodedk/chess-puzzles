package dk.cocode.chess.data

import android.content.Context
import dk.cocode.chess.core.data.CsvPuzzleRepository
import dk.cocode.chess.core.data.PuzzleRepository

/** Loads the bundled `assets/puzzles.csv` into a [PuzzleRepository]. */
object PuzzleAssetDataSource {
    fun load(context: Context): PuzzleRepository =
        CsvPuzzleRepository.load { context.assets.open("puzzles.csv") }
}
