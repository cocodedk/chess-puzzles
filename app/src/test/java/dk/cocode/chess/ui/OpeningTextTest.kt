package dk.cocode.chess.ui

import dk.cocode.chess.EnglishTexts
import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pure text helpers behind the openings screens: group headings, move list, feedback. */
class OpeningTextTest : EnglishTexts() {
    @Test fun everyGroupHasATitle() {
        assertEquals(
            listOf("As White", "As Black against 1.e4", "As Black against 1.d4"),
            OpeningGroup.entries.map { groupTitle(it) },
        )
    }

    @Test fun everyModeHasALabel() {
        assertEquals(listOf("Learn", "Practise"), OpeningMode.entries.map { modeLabel(it) })
    }

    @Test fun moveListNumbersOnlyWhiteAndBoldsTheCurrentPly() {
        val text = moveListText(listOf("e4", "e5", "Nf3", "Nc6"), currentPly = 3)
        assertEquals("1.e4 e5 2.Nf3 Nc6", text.text)
        val bold = text.spanStyles.single()
        assertEquals(text.text.indexOf("Nf3"), bold.start)
        assertEquals(text.text.indexOf("Nf3") + 3, bold.end)
    }

    @Test fun moveListAtTheStartHasNoBoldMove() {
        assertTrue(moveListText(listOf("e4", "e5"), currentPly = 0).spanStyles.isEmpty())
    }

    @Test fun allFeedbackValues() {
        assertEquals("", practiseFeedback(OpeningFeedback.NONE, clean = true, bookSan = null))
        assertTrue(practiseFeedback(OpeningFeedback.CORRECT, clean = true, bookSan = null).contains("Correct opening move"))
        assertEquals(
            "Not the move this opening teaches. Play Nf3.",
            practiseFeedback(OpeningFeedback.WRONG, clean = false, bookSan = "Nf3"),
        )
        assertEquals("Line complete ✓", practiseFeedback(OpeningFeedback.COMPLETE, clean = true, bookSan = null))
        assertEquals(
            "Line complete. Try again without mistakes.",
            practiseFeedback(OpeningFeedback.COMPLETE, clean = false, bookSan = null),
        )
    }

    @Test fun wrongFeedbackFallsBackWithoutABookSan() {
        assertEquals(
            "Not the move this opening teaches. Play the highlighted move.",
            practiseFeedback(OpeningFeedback.WRONG, clean = false, bookSan = null),
        )
    }
}
