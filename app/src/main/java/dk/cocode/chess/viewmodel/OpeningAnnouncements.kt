package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.PieceColor

// The sentences a screen reader says on the openings screens, and the words it reads for the move list
// and the line chips. Every move is spoken with spokenMove.

internal const val START_POSITION = "Start position."

/** A line chip's name in notation: "3…Bc5" (Black's third move) or "4.d3" (White's fourth). */
private val LINE_NAME = Regex("(\\d+)(…|\\.)(.+)")

private fun plyLabel(ply: Int) = "Move ${(ply - 1) / 2 + 1}, ${(if (ply % 2 == 1) PieceColor.WHITE else PieceColor.BLACK).label()}"

/** "Move 3, White: knight f3." for the [ply]-th move (1-based) of a line. */
internal fun learnMoveSentence(ply: Int, san: String) = "${plyLabel(ply)}: ${spokenMove(san)}."

/** An opening's ECO code as it is read: "C50", or "C50, 3 clean runs" (and "1 clean run"). */
internal fun spokenEco(eco: String, cleanRuns: Int): String = when {
    cleanRuns <= 0 -> eco
    cleanRuns == 1 -> "$eco, 1 clean run"
    else -> "$eco, $cleanRuns clean runs"
}

internal fun openedSentence(name: String) = "$name. Learn. $START_POSITION"

internal fun learnChosenSentence(shown: String) = "Learn. $shown"

/** A line chip in words, as the move it names: "Move 3, Black: bishop c5". Any other name is read as it is. */
internal fun spokenLineName(name: String): String {
    val (number, dots, san) = LINE_NAME.matchEntire(name)?.destructured ?: return name
    val side = if (dots == "…") PieceColor.BLACK else PieceColor.WHITE
    return "Move $number, ${side.label()}: ${spokenMove(san)}"
}

internal fun lineChosenSentence(name: String) = "Line: ${spokenLineName(name).replaceFirstChar(Char::lowercase)}. $START_POSITION"

/** [side] is who the player practises; [openingSan] is the book's first move when it is the opponent's. */
internal fun practiseSentence(side: PieceColor, openingSan: String?) =
    "Practise. Play ${side.label()}'s moves from memory." + playedBy(side.opposite(), openingSan)

internal fun bookMoveSentence(san: String, replySide: PieceColor, replySan: String?) =
    "Book move. You played ${spokenMove(san)}." + playedBy(replySide, replySan)

internal fun notBookSentence(playedSan: String, bookSan: String) =
    "You played ${spokenMove(playedSan)}. Not the book move — the book plays ${spokenMove(bookSan)}."

internal fun lineCompleteSentence(said: String, clean: Boolean) =
    "$said " + if (clean) "Line complete." else "Line complete — try it clean."

private fun playedBy(side: PieceColor, san: String?) = san?.let { " ${side.label()} played ${spokenMove(it)}." }.orEmpty()

/** "1. pawn e4, pawn e5. 2. knight f3." — the move list in words, numbered as it is shown; empty when it is. */
internal fun spokenMoveList(sans: List<String>): String =
    sans.chunked(2).mapIndexed { i, pair -> "${i + 1}. ${pair.joinToString(", ", transform = ::spokenMove)}." }.joinToString(" ")
