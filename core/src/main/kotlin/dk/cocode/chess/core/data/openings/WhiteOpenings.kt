package dk.cocode.chess.core.data.openings

import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup.WHITE

/** Openings played as White, each branching on Black's most common replies. */
internal val WhiteOpenings = listOf(
    Opening(
        id = "italian",
        name = "Italian Game",
        eco = "C50",
        group = WHITE,
        idea = "Develop fast, aim the bishop at f7, and build slowly with c3 and d3 before opening the centre.",
        lines = listOf(
            "e4 e5 Nf3 Nc6 Bc4 Bc5 c3 Nf6 d3 d6 O-O O-O Re1 a6 Bb3 Ba7",
            "e4 e5 Nf3 Nc6 Bc4 Nf6 d3 Be7 O-O O-O Re1 d6 c3",
        ),
    ),
    Opening(
        id = "ruy-lopez",
        name = "Ruy Lopez",
        eco = "C60",
        group = WHITE,
        idea = "Pressure the knight that guards e5, castle, and keep a lasting edge with c3, h3 and a later d4.",
        lines = listOf(
            "e4 e5 Nf3 Nc6 Bb5 a6 Ba4 Nf6 O-O Be7 Re1 b5 Bb3 d6 c3 O-O h3",
            "e4 e5 Nf3 Nc6 Bb5 Nf6 d3 Bc5 c3 O-O O-O d6 h3",
        ),
    ),
    Opening(
        id = "scotch",
        name = "Scotch Game",
        eco = "C45",
        group = WHITE,
        idea = "Open the centre at once with d4, then trade on c6 or develop quickly against the bishop on c5.",
        lines = listOf(
            "e4 e5 Nf3 Nc6 d4 exd4 Nxd4 Nf6 Nxc6 bxc6 e5 Qe7 Qe2 Nd5 c4 Ba6",
            "e4 e5 Nf3 Nc6 d4 exd4 Nxd4 Bc5 Be3 Qf6 c3 Nge7 Bc4 O-O O-O",
        ),
    ),
    Opening(
        id = "queens-gambit",
        name = "Queen's Gambit",
        eco = "D06",
        group = WHITE,
        idea = "Offer the c-pawn to draw Black's d-pawn aside, then build a broad centre whichever way Black answers.",
        lines = listOf(
            "d4 d5 c4 e6 Nc3 Nf6 Bg5 Be7 e3 O-O Nf3 Nbd7 Rc1 c6 Bd3",
            "d4 d5 c4 c6 Nf3 Nf6 Nc3 dxc4 a4 Bf5 e3 e6 Bxc4",
            "d4 d5 c4 dxc4 Nf3 Nf6 e3 e6 Bxc4 c5 O-O a6",
        ),
    ),
    Opening(
        id = "london",
        name = "Queen's Pawn Game: Accelerated London System",
        eco = "D00",
        group = WHITE,
        idea = "Bring the bishop to f4 before e3, then set up the same solid pawn triangle against almost anything.",
        lines = listOf(
            "d4 d5 Bf4 Nf6 e3 e6 Nf3 c5 c3 Nc6 Nbd2 Bd6 Bg3 O-O Bd3",
            "d4 d5 Bf4 c5 e3 Nc6 c3 Nf6 Nd2 e6 Ngf3 Bd6 Bg3 O-O Bd3",
        ),
    ),
    Opening(
        id = "english",
        name = "English Opening",
        eco = "A10",
        group = WHITE,
        idea = "Start with c4 to hold d5 from the side, fianchetto the bishop on g2 and play on the queenside.",
        lines = listOf(
            "c4 e5 Nc3 Nf6 Nf3 Nc6 g3 d5 cxd5 Nxd5 Bg2 Nb6 O-O Be7 d3 O-O",
            "c4 c5 Nc3 Nc6 g3 g6 Bg2 Bg7 Nf3 e6 O-O Nge7 d3 O-O",
        ),
    ),
    Opening(
        id = "catalan",
        name = "Catalan Opening",
        eco = "E00",
        group = WHITE,
        idea = "Fianchetto the bishop on g2 to bear down the long diagonal, even if the c-pawn is lost for a while.",
        lines = listOf(
            "d4 Nf6 c4 e6 g3 d5 Bg2 dxc4 Nf3 Be7 O-O O-O Qc2 a6 Qxc4 b5 Qc2 Bb7",
            "d4 Nf6 c4 e6 g3 d5 Bg2 Be7 Nf3 O-O O-O Nbd7 Qc2 c6 Nbd2 b6 e4",
        ),
    ),
    Opening(
        id = "alapin",
        name = "Sicilian Defense: Alapin Variation",
        eco = "B22",
        group = WHITE,
        idea = "Meet the Sicilian with c3 to prepare d4 and a full pawn centre, avoiding the sharp open lines.",
        lines = listOf(
            "e4 c5 c3 Nf6 e5 Nd5 d4 cxd4 Nf3 Nc6 cxd4 d6 Bc4 Nb6 Bb5",
            "e4 c5 c3 d5 exd5 Qxd5 d4 Nf6 Nf3 Bg4 Be2 e6 O-O Nc6 h3 Bh5 c4",
        ),
    ),
)
