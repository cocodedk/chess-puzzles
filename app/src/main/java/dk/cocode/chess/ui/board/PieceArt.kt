package dk.cocode.chess.ui.board

/** Which of a piece set's detail inks a mark is drawn in: highlight, carved line, or the knight's eye. */
internal enum class Ink { HIGHLIGHT, LINE, EYE }

/** A carved line or glint drawn over a piece body, as SVG path data in the piece's 100x100 box. */
internal data class Mark(val path: String, val ink: Ink, val width: Float = 0f) {
    /** Zero-width marks are filled shapes; the rest are strokes. */
    val filled: Boolean get() = width == 0f
}

/**
 * One Staunton-style piece: [body] parts filled with the set's shading and outlined, back to front,
 * then [marks] drawn over them. All coordinates live in a 100x100 box lit from the upper left.
 */
internal class PieceArt(val body: List<String>, val marks: List<Mark>)

/** An SVG circle as path data, so round parts parse like every other part. */
internal fun circle(cx: Float, cy: Float, r: Float): String = "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

/** An SVG ellipse tilted by [degrees], as two half-arcs of path data. */
internal fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, degrees: Float): String {
    val rad = Math.toRadians(degrees.toDouble())
    val dx = (rx * Math.cos(rad)).toFloat()
    val dy = (rx * Math.sin(rad)).toFloat()
    return "M${cx - dx} ${cy - dy}A$rx $ry $degrees 1 0 ${cx + dx} ${cy + dy}A$rx $ry $degrees 1 0 ${cx - dx} ${cy - dy}z"
}

private const val BASE_WIDE = "M22 91c0-5 3.5-7.5 7.5-8.5h41c4 1 7.5 3.5 7.5 8.5z"
private const val BASE_ROYAL = "M20 91c0-5 4-7.5 8.5-8.5h43c4.5 1 8.5 3.5 8.5 8.5z"
private const val STEM_ROYAL = "M31 82.5c4.5-8 9.5-19 10.5-32h17c1 13 6 24 10.5 32z"
private val FOOT_LINE = Mark("M30.5 86.5h39", Ink.LINE, 1.1f)
private val ROYAL_LINES = Mark("M39.5 43h21M28.5 86.5h43", Ink.LINE, 1.1f)
private val ROYAL_GLINT = Mark("M42.6 55c-.6 10-3.3 19-7.4 26", Ink.HIGHLIGHT, 2f)

private val PAWN = PieceArt(
    body = listOf(
        "M24 91c0-5 3-7.5 7-8.5h38c4 1 7 3.5 7 8.5z",
        "M34 82.5c3-8 7.5-12.5 8.5-17.5h15c1 5 5.5 9.5 8.5 17.5z",
        "M37 65c-1-3 2-5.5 13-5.5s14 2.5 13 5.5z",
        circle(50f, 46f, 13.5f),
    ),
    marks = listOf(
        Mark(ellipse(45f, 41f, 3.4f, 5.4f, -28f), Ink.HIGHLIGHT),
        Mark("M38 70.5c-1.2 3.5-2.7 7-3.8 10", Ink.HIGHLIGHT, 1.6f),
        Mark("M31.5 86.5h37", Ink.LINE, 1.1f),
    ),
)

private val ROOK = PieceArt(
    body = listOf(
        BASE_WIDE,
        "M28 82.5c0-3.5 2.5-5 5.5-5.5h33c3 .5 5.5 2 5.5 5.5z",
        "M34.5 77c2-11 2.5-22 1.5-32h28c-1 10-.5 21 1.5 32z",
        "M32 45c0-3 2-4.5 5-4.5h26c3 0 5 1.5 5 4.5z",
        "M31 40.5V23h8v6h6.5v-6h9v6H61v-6h8v17.5z",
    ),
    marks = listOf(
        Mark("M39.3 48c.6 9.5.2 19-1.2 27.5", Ink.HIGHLIGHT, 2.4f),
        Mark("M34.6 25.5v12", Ink.HIGHLIGHT, 1.8f),
        Mark("M33 33.5h34M30.5 86.5h39", Ink.LINE, 1.1f),
    ),
)

private val KNIGHT = PieceArt(
    body = listOf(
        BASE_WIDE,
        "M27 82.5c0-3.5 2.5-5 5.5-5.5h35c3 .5 5.5 2 5.5 5.5z",
        "M33.5 77c1-9 4-15 8-19.5c-5 1.5-10 2.5-13.5.5c-4-2.2-5-6.3-2.6-9.5c4.2-5.8 9.6-11.3 13.4-16.4" +
            "c1.2-5 3.2-8.6 5.8-11.8l1.6-6.3c3 1.7 5.1 4.3 6.2 7.2c9.2 2.2 16.6 9.6 18.4 21.3" +
            "c1.8 12-1.7 23.3-5.6 34.5z",
    ),
    marks = listOf(
        Mark(ellipse(44.5f, 32.5f, 2.3f, 1.6f, -20f), Ink.EYE),
        Mark("M28.8 50.8c.8-.3 1.6-.2 2.2.3", Ink.EYE, 1.3f),
        Mark(
            "M56 22.5c6.5 3 11.3 8.3 13.2 15.4M59.5 31c4.5 3.4 7 8.6 7.6 14.6M60.5 42.5c2.3 4.6 2.8 9.6 1.8 14.4",
            Ink.LINE, 1.2f,
        ),
        Mark("M39 37.5c-3.6 4.4-7.6 8.4-10.8 12.4", Ink.HIGHLIGHT, 2f),
        Mark("M41 60c-2.4 4-4 9-4.6 14", Ink.HIGHLIGHT, 1.8f),
        FOOT_LINE,
    ),
)

private val BISHOP = PieceArt(
    body = listOf(
        BASE_WIDE,
        "M33 82.5c3.5-7 8-14 9-22h16c1 8 5.5 15 9 22z",
        "M35.5 60.5c-.5-3 3-4.8 14.5-4.8s15 1.8 14.5 4.8z",
        "M50 20.5c-8.8 6.5-14 15.4-14 23.6c0 7.2 5.2 11.2 14 11.7c8.8-.5 14-4.5 14-11.7c0-8.2-5.2-17.1-14-23.6z",
        circle(50f, 16.3f, 4.5f),
    ),
    marks = listOf(
        Mark("M55 29.5L45.5 43", Ink.LINE, 2.6f),
        Mark("M41.2 35.5c-1.6 3.6-2.2 7.2-1.7 10.4", Ink.HIGHLIGHT, 2.2f),
        Mark(circle(48.4f, 14.6f, 1.3f), Ink.HIGHLIGHT),
        Mark("M41.5 66c-1.4 5-3.6 10-6 14.5", Ink.HIGHLIGHT, 1.7f),
        FOOT_LINE,
    ),
)

private val QUEEN = PieceArt(
    body = listOf(
        BASE_ROYAL,
        STEM_ROYAL,
        "M35.5 50.5c-.5-3 3.5-4.8 14.5-4.8s15 1.8 14.5 4.8z",
        "M38 46L30 26.5l10 8.2L43 19.5l7 9.8l7-9.8l3 15.2l10-8.2L62 46z",
        circle(30f, 24.3f, 2.9f), circle(43f, 17.3f, 2.9f), circle(57f, 17.3f, 2.9f), circle(70f, 24.3f, 2.9f),
        circle(50f, 24f, 4.6f),
    ),
    marks = listOf(
        ROYAL_GLINT,
        Mark("M36.5 40.5l-3-8", Ink.HIGHLIGHT, 1.6f),
        Mark(circle(48.6f, 22.4f, 1.4f), Ink.HIGHLIGHT),
        ROYAL_LINES,
    ),
)

private val KING = PieceArt(
    body = listOf(
        BASE_ROYAL,
        STEM_ROYAL,
        "M35 50.5c-.5-3 4-4.8 15-4.8s15.5 1.8 15 4.8z",
        "M38.5 46c-3.5-7-4.8-13.5-.8-16.8c3.6-3 8.6-1.6 12.3 1.6c3.7-3.2 8.7-4.6 12.3-1.6c4 3.3 2.7 9.8-.8 16.8z",
        "M47.2 30.5V22.5h-5.4v-5.2h5.4V11h5.6v6.3h5.4v5.2h-5.4v8z",
    ),
    marks = listOf(
        ROYAL_GLINT,
        Mark("M38.3 33.2c-.8 2.8-.2 6 1.4 9.4", Ink.HIGHLIGHT, 1.8f),
        Mark("M48.8 13v6", Ink.HIGHLIGHT, 1.3f),
        ROYAL_LINES,
    ),
)

/** The piece art for each FEN letter, case-insensitive. */
internal val PIECE_ART: Map<Char, PieceArt> =
    mapOf('p' to PAWN, 'r' to ROOK, 'n' to KNIGHT, 'b' to BISHOP, 'q' to QUEEN, 'k' to KING)
