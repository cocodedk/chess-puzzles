package dk.cocode.chess.core.data.openings

import dk.cocode.chess.core.model.Opening
import dk.cocode.chess.core.model.OpeningGroup.BLACK_VS_E4

/** Openings played as Black against 1.e4, each branching on White's most common tries. */
internal val VsE4Openings = listOf(
    Opening(
        id = "najdorf",
        name = "Sicilian Defense: Najdorf Variation",
        eco = "B90",
        group = BLACK_VS_E4,
        idea = "With ...a6, keep b5 covered and stay flexible, ready for ...e5 or ...e6 depending on White's setup.",
        lines = listOf(
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 a6 Be3 e5 Nb3 Be6 f3 Be7 Qd2 O-O O-O-O Nbd7",
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 a6 Bg5 e6 f4 Be7 Qf3 Qc7 O-O-O Nbd7",
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 a6 Be2 e5 Nb3 Be7 O-O O-O Be3 Be6",
        ),
    ),
    Opening(
        id = "dragon",
        name = "Sicilian Defense: Dragon Variation",
        eco = "B70",
        group = BLACK_VS_E4,
        idea = "Play ...g6 and put the bishop on g7 to cover the long diagonal, castle kingside and counterattack on the queenside.",
        lines = listOf(
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 g6 Be3 Bg7 f3 O-O Qd2 Nc6 O-O-O d5",
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 g6 Be3 Bg7 f3 O-O Qd2 Nc6 Bc4 Bd7 O-O-O Rc8",
            "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 g6 Be2 Bg7 O-O O-O Be3 Nc6 Nb3 Be6",
        ),
    ),
    Opening(
        id = "french",
        name = "French Defense",
        eco = "C00",
        group = BLACK_VS_E4,
        idea = "Build a solid chain with ...e6 and ...d5, then strike at White's centre with ...c5 and ...f6.",
        lines = listOf(
            "e4 e6 d4 d5 e5 c5 c3 Nc6 Nf3 Qb6 a3 c4 Nbd2 Na5",
            "e4 e6 d4 d5 Nc3 Nf6 Bg5 Be7 e5 Nfd7 Bxe7 Qxe7 f4 O-O Nf3 c5",
            "e4 e6 d4 d5 exd5 exd5 Nf3 Nf6 Bd3 Bd6 O-O O-O",
            "e4 e6 d4 d5 Nd2 Nf6 e5 Nfd7 Bd3 c5 c3 Nc6 Ne2 cxd4 cxd4 f6",
        ),
    ),
    Opening(
        id = "caro-kann",
        name = "Caro-Kann Defense",
        eco = "B12",
        group = BLACK_VS_E4,
        idea = "Support d5 with ...c6 so the light-squared bishop can come out before ...e6 shuts it in.",
        lines = listOf(
            "e4 c6 d4 d5 Nc3 dxe4 Nxe4 Bf5 Ng3 Bg6 h4 h6 Nf3 Nd7 h5 Bh7 Bd3 Bxd3 Qxd3 e6",
            "e4 c6 d4 d5 e5 Bf5 Nf3 e6 Be2 c5 Be3 cxd4 Nxd4 Ne7",
            "e4 c6 d4 d5 exd5 cxd5 Bd3 Nc6 c3 Nf6 Bf4 Bg4 Qb3 Qd7 Nd2 e6",
            "e4 c6 d4 d5 Nd2 dxe4 Nxe4 Bf5 Ng3 Bg6",
        ),
    ),
    Opening(
        id = "scandinavian",
        name = "Scandinavian Defense: Main Line",
        eco = "B01",
        group = BLACK_VS_E4,
        idea = "Take on d5 at once, tuck the queen on a5 and develop smoothly with ...c6, ...Bf5 and ...e6.",
        lines = listOf(
            "e4 d5 exd5 Qxd5 Nc3 Qa5 d4 Nf6 Nf3 c6 Bc4 Bf5 Bd2 e6",
        ),
    ),
    Opening(
        id = "two-knights",
        name = "Italian Game: Two Knights Defense",
        eco = "C55",
        group = BLACK_VS_E4,
        idea = "Counterattack e4 with the knight instead of copying White's bishop, and meet Ng5 with ...d5.",
        lines = listOf(
            "e4 e5 Nf3 Nc6 Bc4 Nf6 Ng5 d5 exd5 Na5 Bb5+ c6 dxc6 bxc6 Be2 h6 Nf3 e4 Ne5 Bd6",
            "e4 e5 Nf3 Nc6 Bc4 Nf6 d3 Be7 O-O O-O Re1 d6 c3",
            "e4 e5 Nf3 Nc6 Bc4 Nf6 d4 exd4 O-O Nxe4 Re1 d5 Bxd5 Qxd5 Nc3 Qa5",
        ),
    ),
    Opening(
        id = "russian",
        name = "Petrov's Defense",
        eco = "C42",
        group = BLACK_VS_E4,
        idea = "Answer White's attack on e5 with one on e4; after the trades, develop into a solid, level position.",
        lines = listOf(
            "e4 e5 Nf3 Nf6 Nxe5 d6 Nf3 Nxe4 d4 d5 Bd3 Nc6 O-O Be7 c4 Nb4",
            "e4 e5 Nf3 Nf6 d4 Nxe4 Bd3 d5 Nxe5 Nd7 Nxd7 Bxd7 O-O Bd6 c4 c6",
        ),
    ),
)
