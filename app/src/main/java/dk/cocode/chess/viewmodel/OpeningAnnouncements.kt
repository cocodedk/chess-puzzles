package dk.cocode.chess.viewmodel

import dk.cocode.chess.R
import dk.cocode.chess.core.model.PieceColor

// The sentences a screen reader says on the openings screens, and the words it reads for the move list
// and the line chips. Every move is spoken with spokenMove.

internal fun Texts.startPosition() = string(R.string.sentence_start_position)

/** A line chip's name in notation: "3…Bc5" (Black's third move) or "4.d3" (White's fourth). */
private val LINE_NAME = Regex("(\\d+)(…|\\.)(.+)")

private fun plySide(ply: Int) = if (ply % 2 == 1) PieceColor.WHITE else PieceColor.BLACK

/** "Move 3, White: knight f3." for the [ply]-th move (1-based) of a line. */
internal fun Texts.learnMoveSentence(ply: Int, san: String) =
    string(R.string.sentence_learn_move, (ply - 1) / 2 + 1, side(plySide(ply)), spokenMove(san))

/** An opening's code as shown and read: "Opening code C50", then ", 3 runs without mistakes" (or "1 run"). */
internal fun Texts.openingCodeLine(eco: String, runs: Int): String =
    if (runs <= 0) openingCode(eco) else plural(R.plurals.opening_code_runs, runs, eco, runs)

internal fun Texts.openingCode(eco: String): String = string(R.string.opening_code, eco)

internal fun Texts.openedSentence(name: String) = string(R.string.sentence_opened, name, startPosition())

internal fun Texts.learnChosenSentence(shown: String) = string(R.string.sentence_learn_chosen, shown)

/** A line chip in words, as the move it names: "Move 3, Black: bishop c5". Any other name is read as it is. */
internal fun Texts.spokenLineName(name: String): String {
    val (number, dots, san) = LINE_NAME.matchEntire(name)?.destructured ?: return name
    val mover = if (dots == "…") PieceColor.BLACK else PieceColor.WHITE
    return string(R.string.spoken_line_name, number.toInt(), side(mover), spokenMove(san))
}

internal fun Texts.lineChosenSentence(name: String) =
    string(R.string.sentence_line_chosen, spokenLineName(name).replaceFirstChar(Char::lowercase), startPosition())

/** "Play White's moves from memory." */
internal fun Texts.practisePrompt(side: PieceColor) = string(R.string.practise_prompt, side(side))

/** [side] is who the player practises; [openingSan] is the book's first move when it is the opponent's. */
internal fun Texts.practiseSentence(side: PieceColor, openingSan: String?) =
    string(R.string.sentence_practise, practisePrompt(side)) + playedBy(side.opposite(), openingSan)

internal fun Texts.bookMoveSentence(san: String, replySide: PieceColor, replySan: String?) =
    string(R.string.sentence_book_move, spokenMove(san)) + playedBy(replySide, replySan)

internal fun Texts.notBookSentence(playedSan: String, bookSan: String) =
    string(R.string.sentence_not_book, spokenMove(playedSan), spokenMove(bookSan))

internal fun Texts.lineCompleteSentence(said: String, clean: Boolean) =
    "$said " + string(if (clean) R.string.sentence_line_complete else R.string.sentence_line_complete_unclean)

private fun Texts.playedBy(side: PieceColor, san: String?) =
    san?.let { " " + string(R.string.sentence_played_by, side(side), spokenMove(it)) }.orEmpty()

/** "1. pawn e4, pawn e5. 2. knight f3." — the move list in words, numbered as it is shown; empty when it is. */
internal fun Texts.spokenMoveList(sans: List<String>): String = sans.chunked(2).mapIndexed { i, pair ->
    string(R.string.spoken_move_list_entry, i + 1, pair.joinToString(", ", transform = ::spokenMove))
}.joinToString(" ")
