package dk.cocode.chess.viewmodel

import dk.cocode.chess.core.model.Square

/** Learn: step through a line. Practise: play it back from memory. */
enum class OpeningMode { LEARN, PRACTISE }

/** The verdict on the last move submitted in Practise mode. */
enum class OpeningFeedback { NONE, CORRECT, WRONG, COMPLETE }

/**
 * The complete, immutable state the openings screens render. [openingId] null means the list is
 * shown. [lineNames]/[line]/[ply]/[moveSan] drive Learn's branch chips, stepping and move list;
 * [feedback]/[clean]/[bookSan] drive Practise's verdict text — [bookSan] names the book move after
 * a wrong one, cleared on the next correct move, Again, or a new drill. [cleanRuns] is collected
 * from the progress repository regardless of which opening (if any) is open.
 */
data class OpeningUiState(
    override val board: List<String> = List(8) { "        " },
    override val flipped: Boolean = false,
    override val selected: Square? = null,
    override val legalTargets: Set<Square> = emptySet(),
    override val lastMove: Highlight? = null,
    override val hint: Highlight? = null,
    val openingId: String? = null,
    val mode: OpeningMode = OpeningMode.LEARN,
    val lineNames: List<String> = emptyList(),
    val line: Int = 0,
    val ply: Int = 0,
    val moveSan: List<String> = emptyList(),
    val feedback: OpeningFeedback = OpeningFeedback.NONE,
    val clean: Boolean = true,
    val bookSan: String? = null,
    val cleanRuns: Map<String, Int> = emptyMap(),
) : BoardState
