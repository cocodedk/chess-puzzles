package dk.cocode.chess

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import dk.cocode.chess.core.data.CsvPuzzleRepository
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.data.Progress
import dk.cocode.chess.data.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Draws the hosted content to a software canvas so Compose Canvas draw code runs under Robolectric. */
fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.renderToBitmap() {
    waitForIdle()
    val view = activity.findViewById<View>(android.R.id.content)
    val bitmap = Bitmap.createBitmap(maxOf(1, view.width), maxOf(1, view.height), Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
}

/** In-memory [ProgressRepository] with the same atomic semantics as the DataStore implementation. */
class FakeProgressRepository(initial: Progress = Progress()) : ProgressRepository {
    private val state = MutableStateFlow(initial)
    override val progress: Flow<Progress> = state

    fun current(): Progress = state.value

    override suspend fun recordSolved() = state.update {
        val streak = it.currentStreak + 1
        it.copy(solvedCount = it.solvedCount + 1, currentStreak = streak, bestStreak = maxOf(it.bestStreak, streak))
    }

    override suspend fun recordFailed() = state.update { it.copy(currentStreak = 0) }

    override suspend fun setIndex(index: Int) = state.update { it.copy(index = index) }
}

/** Four hand-verified puzzles (mate-in-1 white, mate-in-2, promotion, mate-in-1 black). */
fun testPuzzleRepository(): PuzzleRepository {
    val csv = buildString {
        appendLine("PuzzleId,FEN,Moves,Rating,Themes")
        appendLine("M1,6k1/1Q6/6K1/8/8/8/8/8 b - - 0 1,g8h8 b7g7,800,mate mateIn1")
        appendLine("M2,q3k1nr/1pp1nQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 b k - 0 17,e8d7 a2e6 d7d8 f7f8,1500,mate")
        appendLine("PR,8/p3P3/8/8/8/2k5/8/6K1 b - - 0 1,a7a6 e7e8q,1000,promotion")
        appendLine("BK,8/8/8/8/8/6k1/1q6/6K1 w - - 0 1,g1h1 b2g2,900,mate mateIn1")
    }
    return CsvPuzzleRepository.load { csv.byteInputStream() }
}
