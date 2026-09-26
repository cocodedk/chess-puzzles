package dk.cocode.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R
import dk.cocode.chess.core.model.Piece
import dk.cocode.chess.core.model.PieceColor
import dk.cocode.chess.core.model.PieceType

/** Each promotion choice with its spoken name, for TalkBack. */
private val PROMOTION_CHOICES = listOf(
    PieceType.QUEEN to R.string.piece_queen,
    PieceType.ROOK to R.string.piece_rook,
    PieceType.BISHOP to R.string.piece_bishop,
    PieceType.KNIGHT to R.string.piece_knight,
)

/** The cream chip each choice sits on, as in the reference's chooser, so ivory reads on any dialog. */
private val CHIP = arrayOf(0f to Color(0xFFFFF8E8), 1f to Color(0xFFECDCB8))

/** Lets the player pick a promotion piece. */
@Composable
fun PromotionDialog(onSelect: (PieceType) -> Unit, onDismiss: () -> Unit) {
    val palette = LocalBoardPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Promote to") },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PROMOTION_CHOICES.forEach { (type, name) ->
                    val spoken = stringResource(name)
                    TextButton(
                        onClick = { onSelect(type) },
                        modifier = Modifier.semantics { contentDescription = spoken },
                    ) {
                        Canvas(Modifier.size(44.dp)) {
                            drawCircle(Brush.radialGradient(*CHIP))
                            drawPiece(Piece(PieceColor.WHITE, type).fenChar, Offset.Zero, size.width, palette)
                        }
                    }
                }
            }
        },
    )
}
