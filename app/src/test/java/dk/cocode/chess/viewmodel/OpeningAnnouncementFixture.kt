package dk.cocode.chess.viewmodel

import androidx.lifecycle.SavedStateHandle
import dk.cocode.chess.FakeOpeningProgressRepository
import dk.cocode.chess.ZERO_RANDOM
import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.testBlackOpening
import dk.cocode.chess.testWhiteOpening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

/** The openings and helpers the announcement tests share, for a White opening and a Black one. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class OpeningAnnouncementFixture {
    protected val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    protected val white = testWhiteOpening() // e4 e5 Nf3 Nc6, played as White
    protected val black = testBlackOpening() // the same line, played as Black
    protected val short = Opening("short", "Short Line", "T01", OpeningGroup.WHITE, "Idea.", listOf("e4 e5 Nf3"))
    protected val whiteBranching = Opening(
        "wb", "White Branching", "T02", OpeningGroup.WHITE, "Idea.",
        listOf("e4 e5 Nf3 Nc6 Bc4 Bc5", "e4 e5 Nf3 Nc6 Bc4 Nf6 d3 Be7"),
    )
    protected val blackBranching = Opening(
        "bb", "Black Branching", "T03", OpeningGroup.BLACK_VS_E4, "Idea.", listOf("e4 c5 Nf3 d6", "e4 c5 c3 d5"),
    )

    protected fun vm(handle: SavedStateHandle = SavedStateHandle()) = OpeningViewModel(
        listOf(white, black, short, whiteBranching, blackBranching), FakeOpeningProgressRepository(), ZERO_RANDOM, handle,
    )

    protected fun OpeningViewModel.said() = state.value.announcement

    protected fun OpeningViewModel.tap(vararg squares: String) = squares.forEach { onSquareTapped(Square.of(it)) }

    protected fun OpeningViewModel.practise(opening: Opening) {
        onOpen(opening.id)
        onMode(OpeningMode.PRACTISE)
    }
}
