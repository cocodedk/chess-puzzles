# Lessons

What agents keep rediscovering about this project, one fact per line with its evidence. Hints to
check, never proof.

- `@Composable` functions are left out of the coverage gate, so every decision and every string a test must reach belongs in a plain function (CLAUDE.md rule 5; `feedbackMessage`, `practiseFeedback`, `moveListText`).
- An exhaustive `when` as the last expression of a lambda compiles to an unreachable throw that fails the 100% line gate; end it with `else ->` (the openings feature, PR #62).
- A `private val` in a companion object leaves an uncovered getter; put it at the top level of the file (`WHITESPACE` in `OpeningTree.kt`, PR #62).
- A Robolectric font scale only takes effect from `@Config(fontScale = …)` on the test; setting it after the activity starts does nothing (`LandscapeLayoutTest`, PR #61).
- Layout assertions use `getUnclippedBoundsInRoot()`; test viewports must be the ones that fail on the old code, or the test proves nothing (`LandscapeLayoutTest`, `OpeningLearnSizeTest`).
- `FlowRow` is still experimental in this Compose version and needs `@OptIn(ExperimentalLayoutApi::class)` (`StatsRow.kt`).
- chesslib is used only in `ChessEngine.kt` and `BoardMapper.kt`; everything else sees the `:core` model types (`docs/ARCHITECTURE.md`).
