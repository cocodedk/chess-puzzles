package dk.cocode.chess

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dk.cocode.chess.core.data.CsvPuzzleRepository
import dk.cocode.chess.core.data.PuzzleRepository
import dk.cocode.chess.data.Progress
import dk.cocode.chess.data.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.robolectric.RuntimeEnvironment
import java.io.File

/** Draws the hosted content to a software canvas so Compose Canvas draw code runs under Robolectric. */
fun <A : ComponentActivity> AndroidComposeTestRule<*, A>.renderToBitmap(): Bitmap {
    waitForIdle()
    val view = activity.findViewById<View>(android.R.id.content)
    val bitmap = Bitmap.createBitmap(maxOf(1, view.width), maxOf(1, view.height), Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
    return bitmap
}

/** True when any sampled pixel is exactly [argb] — flat fills like board squares are found reliably. */
fun Bitmap.containsColor(argb: Int): Boolean =
    (0 until width step 4).any { x -> (0 until height step 4).any { y -> getPixel(x, y) == argb } }

/** Robolectric screen large enough that the rows below the square board stay on-screen. */
const val PHONE_QUALIFIERS = "w360dp-h800dp"

/** A fresh on-disk Preferences DataStore in the Robolectric app's cache dir. */
fun newPreferencesStore(prefix: String): DataStore<Preferences> {
    val dir = RuntimeEnvironment.getApplication().cacheDir
    val file = File.createTempFile(prefix, ".preferences_pb", dir).apply { delete() }
    return PreferenceDataStoreFactory.create { file }
}

/** A Preferences DataStore whose backing file is garbage, so reads hit the corruption path. */
fun corruptPreferencesStore(prefix: String): DataStore<Preferences> {
    val dir = RuntimeEnvironment.getApplication().cacheDir
    val file = File.createTempFile(prefix, ".preferences_pb", dir)
    file.writeBytes(byteArrayOf(-1, -1, -1, -1))
    return PreferenceDataStoreFactory.create { file }
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

/** Five hand-verified puzzles (mate-in-1 white, mate-in-2, promotion, mate-in-1 black, hard mate-in-2). */
fun testPuzzleRepository(): PuzzleRepository {
    val csv = buildString {
        val mateIn2 = "q3k1nr/1pp1nQpp/3p4/1P2p3/4P3/B1PP1b2/B5PP/5K2 b k - 0 17,e8d7 a2e6 d7d8 f7f8" // M2 == HD
        appendLine("PuzzleId,FEN,Moves,Rating,Themes")
        appendLine("M1,6k1/1Q6/6K1/8/8/8/8/8 b - - 0 1,g8h8 b7g7,800,mate mateIn1")
        appendLine("M2,$mateIn2,1500,mate")
        appendLine("PR,8/p3P3/8/8/8/2k5/8/6K1 b - - 0 1,a7a6 e7e8q,1000,promotion")
        appendLine("BK,8/8/8/8/8/6k1/1q6/6K1 w - - 0 1,g1h1 b2g2,900,mate mateIn1")
        appendLine("HD,$mateIn2,2100,mate")
    }
    return CsvPuzzleRepository.load { csv.byteInputStream() }
}
