package dk.cocode.chess.core.data.openings

import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup.BLACK_VS_D4

/** Openings played as Black against 1.d4, each branching on White's most common tries. */
internal val VsD4Openings = listOf(
    Opening(
        id = "queens-gambit-accepted",
        name = "Queen's Gambit Accepted",
        eco = "D20",
        group = BLACK_VS_D4,
        idea = "Take the c-pawn without trying to keep it, and use the time White spends winning it back to strike with ...c5.",
        lines = listOf(
            "d4 d5 c4 dxc4 Nf3 Nf6 e3 e6 Bxc4 c5 O-O a6",
            "d4 d5 c4 dxc4 e4 e5 Nf3 exd4 Bxc4 Nc6 O-O Be6",
            "d4 d5 c4 dxc4 e3 Nf6 Bxc4 e6 Nf3 c5 O-O a6",
        ),
    ),
    Opening(
        id = "slav",
        name = "Slav Defense",
        eco = "D10",
        group = BLACK_VS_D4,
        idea = "Defend d5 with ...c6 so the light-squared bishop stays free to come out to f5.",
        lines = listOf(
            "d4 d5 c4 c6 Nf3 Nf6 Nc3 dxc4 a4 Bf5 e3 e6 Bxc4 Bb4 O-O O-O",
            "d4 d5 c4 c6 Nc3 Nf6 Nf3 dxc4 a4 Bf5 e3 e6 Bxc4 Bb4 O-O O-O",
            "d4 d5 c4 c6 cxd5 cxd5 Nc3 Nf6 Bf4 Nc6 e3 Bf5",
        ),
    ),
    Opening(
        id = "kings-indian",
        name = "King's Indian Defense",
        eco = "E61",
        group = BLACK_VS_D4,
        idea = "Let White build the centre, play ...g6, put the bishop on g7 and castle, then strike with ...e5 and attack the kingside.",
        lines = listOf(
            "d4 Nf6 c4 g6 Nc3 Bg7 e4 d6 Nf3 O-O Be2 e5 O-O Nc6 d5 Ne7",
            "d4 Nf6 c4 g6 Nc3 Bg7 e4 d6 f3 O-O Be3 e5 d5 Nh5",
        ),
    ),
    Opening(
        id = "nimzo-indian",
        name = "Nimzo-Indian Defense",
        eco = "E20",
        group = BLACK_VS_D4,
        idea = "Pin the c3 knight to stop e4, ready to give the bishop for it and double White's pawns.",
        lines = listOf(
            "d4 Nf6 c4 e6 Nc3 Bb4 e3 O-O Bd3 d5 Nf3 c5 O-O Nc6",
            "d4 Nf6 c4 e6 Nc3 Bb4 Qc2 O-O a3 Bxc3+ Qxc3 b6 Bg5 Bb7",
        ),
    ),
    Opening(
        id = "grunfeld",
        name = "Grünfeld Defense",
        eco = "D80",
        group = BLACK_VS_D4,
        idea = "Invite White to build a big centre, then attack it with the g7 bishop, ...c5 and the queen.",
        lines = listOf(
            "d4 Nf6 c4 g6 Nc3 d5 cxd5 Nxd5 e4 Nxc3 bxc3 Bg7 Nf3 c5 Be3 Qa5 Qd2 O-O",
            "d4 Nf6 c4 g6 Nc3 d5 Nf3 Bg7 Bf4 O-O e3 c5 dxc5 Qa5",
        ),
    ),
)
