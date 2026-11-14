package dk.cocode.chess.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dk.cocode.chess.core.model.Square
import dk.cocode.chess.ui.board.ChessBoard
import dk.cocode.chess.viewmodel.BoardState

/**
 * The board layout (a full-height board between or beside panels of content, or one between rows above
 * and below it) shared by the puzzle and openings screens, so the `when (boardLayout(...))` lives in one place.
 * [tabs] draws at the very top of whichever panel/column comes first. [header] is the left panel /
 * top-of-screen slot, [body] the right panel / bottom-of-screen slot; each is told whether it must
 * lay its own rows out as a stacked column — only true when it stands alone beside the board in its
 * own panel ([BoardLayout.BETWEEN_PANELS]), never when it shares a panel or a column with the other.
 * In [BoardLayout.UPRIGHT] the board is as wide as the screen and the rows below it scroll; in
 * [BoardLayout.STACKED] it is the largest square that fits the height the rows leave, unscrolled, unless
 * [scrollWhenStacked] (for rows too many to leave the board room) asks for the full width and scrolling there too.
 * With the keyboard up ([keyboardHeight], read here in the same layout pass as the height it leaves) under an open
 * move field ([fieldOpen]), [BoardLayout.TYPING] takes the place of either, which [body] is told as its second argument.
 */
@Composable
fun BoardScaffold(
    state: BoardState,
    onSquareTap: (Square) -> Unit,
    onDragStart: (Square) -> Unit,
    onDragEnd: (Square) -> Unit,
    tabs: @Composable () -> Unit,
    header: @Composable ColumnScope.(stacked: Boolean) -> Unit,
    body: @Composable ColumnScope.(stacked: Boolean, typing: Boolean) -> Unit,
    scrollWhenStacked: Boolean = false,
    keyboardHeight: () -> Dp = { 0.dp },
    fieldOpen: Boolean = false,
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            when (val layout = boardLayout(maxWidth, maxHeight, keyboardHeight(), fieldOpen)) {
                BoardLayout.BETWEEN_PANELS -> Row(Modifier.fillMaxSize()) {
                    SidePanel { tabs(); header(true) }
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
                    SidePanel { body(true, false) }
                }
                BoardLayout.BESIDE_PANEL -> Row(Modifier.fillMaxSize()) {
                    ChessBoard(state, onSquareTap, onDragStart, onDragEnd, Modifier.fillMaxHeight())
                    SidePanel {
                        tabs()
                        header(false)
                        Spacer(Modifier.height(16.dp))
                        body(false, false)
                    }
                }
                // UPRIGHT, STACKED and TYPING, in one branch so the move field keeps its focus, and the keyboard stays
                // up, as the layout flips between them. An `else`: as the lambda's last expression an exhaustive
                // `when` hides an unreachable throw.
                else -> {
                    val typing = layout == BoardLayout.TYPING
                    val fullWidth = layout == BoardLayout.UPRIGHT || layout == BoardLayout.STACKED && scrollWhenStacked
                    // Both outside the `when`, so a flip to TYPING and back keeps the place. TYPING has its own, as its
                    // rows fit at most fonts, and a shared one would be pulled back to the top.
                    val scroll = rememberScrollState()
                    val typingScroll = rememberScrollState()
                    Column(
                        Modifier.fillMaxSize().then(
                            when {
                                fullWidth -> Modifier.verticalScroll(scroll).padding(vertical = 8.dp)
                                // No padding: every dp the keyboard leaves goes to the board. The board's weight shares
                                // out the screen's height (the scroll's minimum), so this scrolls only once the field's
                                // rows outgrow the screen at the largest fonts, the board gone by then.
                                typing -> Modifier.verticalScroll(typingScroll)
                                else -> Modifier.padding(vertical = 16.dp)
                            },
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (!typing) {
                            tabs()
                            header(false)
                            Spacer(Modifier.height(8.dp))
                        }
                        val board = when {
                            fullWidth -> Modifier.fillMaxWidth()
                            typing -> Modifier.weight(1f, fill = false).squareWithin()
                            else -> Modifier.weight(1f, fill = false)
                        }
                        ChessBoard(state, onSquareTap, onDragStart, onDragEnd, board)
                        Spacer(Modifier.height(8.dp)) // part of Hint's 44dp clearance under the board (see PuzzleControls)
                        body(false, typing)
                    }
                }
            }
        }
    }
}

/** One side of a board as tall as the screen: its rows centred, scrolling if the screen is too short. */
@Composable
private fun RowScope.SidePanel(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    content = content,
)

/**
 * The largest square the room allows, down to nothing: at the largest fonts the move field leaves the board no room,
 * and `aspectRatio` alone, which never measures to nothing, would draw it full width over the field's rows.
 */
private fun Modifier.squareWithin() = layout { measurable, constraints ->
    val side = minOf(constraints.maxWidth, constraints.maxHeight)
    val square = measurable.measure(Constraints.fixed(side, side))
    layout(side, side) { square.place(0, 0) }
}

/**
 * Where the board goes, given the room a board as tall as the screen leaves beside it. With no room, rows go
 * above and below it: on an upright phone ([UPRIGHT_RATIO] or taller) the board takes the full width and the
 * rows scroll; on a near-square screen, upright or not, a board that wide would push its own bottom off the
 * screen, so it shrinks to fit the height the rows leave instead, unless they are too many to leave it room
 * (see [BoardScaffold]'s `scrollWhenStacked`). The keyboard ([keyboardHeight], taken from [height]) never changes
 * which of those it is, so the move field stays put in the composition, keeping its focus and the keyboard; it
 * only turns rows above and below the board, under an open move field ([fieldOpen]), into [BoardLayout.TYPING]:
 * the largest board that fits above the verdict and the field alone.
 */
internal enum class BoardLayout { BETWEEN_PANELS, BESIDE_PANEL, UPRIGHT, STACKED, TYPING }

internal fun boardLayout(width: Dp, height: Dp, keyboardHeight: Dp = 0.dp, fieldOpen: Boolean = false): BoardLayout {
    val screen = height + keyboardHeight // the height the screen has with the keyboard down
    return when {
        width - screen >= NARROW_PANEL * 2 -> BoardLayout.BETWEEN_PANELS
        width - screen >= WIDE_PANEL -> BoardLayout.BESIDE_PANEL    // a 16:9 phone on its side
        fieldOpen && keyboardHeight > 0.dp -> BoardLayout.TYPING    // a move being typed
        screen >= width * UPRIGHT_RATIO -> BoardLayout.UPRIGHT      // a phone upright
        else -> BoardLayout.STACKED                                 // near-square: a tablet, a foldable's inner screen
    }
}

/** A side panel of stacked menus and buttons: wide enough for the five stats and the Theme and About links. */
private val NARROW_PANEL = 190.dp

/** A single panel keeping its rows: wide enough for the Level and Theme menus side by side. */
private val WIDE_PANEL = 280.dp

/**
 * How much taller than wide an upright phone is, as opposed to a near-square tablet or foldable: every phone
 * is at least 1.5 even after the system bars, and from 1.3 up a full-width board plus the rows above it fit
 * on a wide screen.
 */
private const val UPRIGHT_RATIO = 1.3f
