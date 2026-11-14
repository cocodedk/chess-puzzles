package dk.cocode.chess.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Where the board goes and which rows stay with it, as [boardLayout] decides. */
internal enum class BoardLayout(
    /** A move is being typed, the keyboard up: only the board, the verdict and the move field stay. */
    val typing: Boolean = false,
) {
    BETWEEN_PANELS, BESIDE_PANEL, UPRIGHT, STACKED, TYPING(true), BETWEEN_PANELS_TYPING(true), BESIDE_PANEL_TYPING(true),
}

/**
 * Where the board goes, given the room a board as tall as the screen leaves beside it. With no room, rows go
 * above and below it: on an upright phone ([UPRIGHT_RATIO] or taller) the board takes the full width and the
 * rows scroll; on a near-square screen, upright or not, a board that wide would push its own bottom off the
 * screen, so it shrinks to fit the height the rows leave instead, unless they are too many to leave it room
 * (see [BoardScaffold]'s `scrollWhenStacked`). The keyboard ([keyboardHeight], taken from [height]) never changes
 * which of those families it is, so the move field stays put in the composition, keeping its focus and the keyboard.
 * Under an open move field ([fieldOpen]) it only turns rows above and below the board into [BoardLayout.TYPING], the
 * largest board that fits above the verdict and the field alone, and a board beside panels into
 * [BoardLayout.BETWEEN_PANELS_TYPING] or [BoardLayout.BESIDE_PANEL_TYPING]: the board as tall as the keyboard leaves,
 * beside one panel of the verdict and the field alone.
 */
internal fun boardLayout(width: Dp, height: Dp, keyboardHeight: Dp = 0.dp, fieldOpen: Boolean = false): BoardLayout {
    val screen = height + keyboardHeight // the height the screen has with the keyboard down
    val typing = fieldOpen && keyboardHeight > 0.dp // a move being typed
    return when {
        width - screen >= NARROW_PANEL * 2 ->
            if (typing) BoardLayout.BETWEEN_PANELS_TYPING else BoardLayout.BETWEEN_PANELS
        width - screen >= WIDE_PANEL ->                             // a 16:9 phone on its side
            if (typing) BoardLayout.BESIDE_PANEL_TYPING else BoardLayout.BESIDE_PANEL
        typing -> BoardLayout.TYPING
        screen >= width * UPRIGHT_RATIO -> BoardLayout.UPRIGHT      // a phone upright
        else -> BoardLayout.STACKED                                 // near-square: a tablet, a foldable's inner screen
    }
}

/**
 * A box that pads itself with [insets] and decides the board's layout, both as it is measured, then composes
 * [content] with it: the layout from the width and the height the insets leave it, which must be bounded (a screen's
 * always are), the [keyboard]'s height and [fieldOpen]. On a phone a new inset reaches the layout a frame before
 * anything is composed again. Read while composing, as a `BoxWithConstraints` reads its size or a `Scaffold` its
 * padding, the height would be a frame older than the keyboard's: a screen as tall as both could flip the layout
 * family for that frame, and the move field, composed elsewhere, would lose its focus and the keyboard. [content] is
 * composed again only when the layout changes, or what it shows does.
 */
@Composable
internal fun BoxWithBoardLayout(
    insets: WindowInsets,
    keyboard: WindowInsets,
    fieldOpen: Boolean,
    content: @Composable (BoardLayout) -> Unit,
) = SubcomposeLayout(Modifier.fillMaxSize().windowInsetsPadding(insets)) { constraints ->
    val keyboardHeight = keyboard.getBottom(this).toDp()
    val arrangement = boardLayout(constraints.maxWidth.toDp(), constraints.maxHeight.toDp(), keyboardHeight, fieldOpen)
    val placeables = subcompose(Unit) { content(arrangement) }.map { it.measure(constraints) }
    layout(constraints.maxWidth, constraints.maxHeight) { placeables.forEach { it.place(0, 0) } }
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
