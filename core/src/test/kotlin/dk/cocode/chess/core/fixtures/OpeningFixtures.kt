package dk.cocode.chess.core.fixtures

import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup

/** Small, hand-verified [Opening] fixtures for the openings-engine test suite. */
object OpeningFixtures {
    /** White opening with two lines that branch on Black's 3rd move (opponent's turn). */
    val ITALIAN = Opening(
        id = "italian",
        name = "Italian Game",
        eco = "C50",
        group = OpeningGroup.WHITE,
        idea = "Fast, natural development aiming straight at f7.",
        lines = listOf(
            "e4 e5 Nf3 Nc6 Bc4 Bc5 c3 Nf6",
            "e4 e5 Nf3 Nc6 Bc4 Nf6 d3 Be7",
        ),
    )

    /** Black-vs-1.e4 opening with two lines that branch on White's 2nd move (opponent's turn). */
    val SICILIAN_DEMO = Opening(
        id = "sicilian-demo",
        name = "Sicilian Demo",
        eco = "B20",
        group = OpeningGroup.BLACK_VS_E4,
        idea = "Fight for the center from the flank.",
        lines = listOf(
            "e4 c5 Nf3 d6",
            "e4 c5 c3 d5",
        ),
    )

    /** Single-line White opening whose line ends on the player's own move (3 plies). */
    val SINGLE_LINE = Opening(
        id = "single",
        name = "Quick Line",
        eco = "A00",
        group = OpeningGroup.WHITE,
        idea = "A short line ending on White's move, for testing.",
        lines = listOf("e4 e5 Nf3"),
    )

    /** Invalid: the two lines differ on White's own first move, which is not allowed to branch. */
    val INVALID_PLAYER_BRANCH = Opening(
        id = "bad",
        name = "Invalid",
        eco = "A00",
        group = OpeningGroup.WHITE,
        idea = "Should be rejected.",
        lines = listOf("e4 e5 Nf3", "d4 d5 Nf3"),
    )

    /** Invalid: the second line is blank and parses to zero moves. */
    val INVALID_EMPTY_LINE = Opening(
        id = "bad-empty",
        name = "Invalid",
        eco = "A00",
        group = OpeningGroup.WHITE,
        idea = "Should be rejected.",
        lines = listOf("e4 e5 Nf3", "   "),
    )
}
