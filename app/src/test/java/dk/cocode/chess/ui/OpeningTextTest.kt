package dk.cocode.chess.ui

import dk.cocode.chess.core.model.OpeningGroup
import dk.cocode.chess.viewmodel.OpeningFeedback
import dk.cocode.chess.viewmodel.OpeningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pure text helpers behind the openings screens: group headings, move list, feedback. */
class OpeningTextTest {
    @Test fun everyGroupHasATitle() {
        assertEquals(
            listOf("As White", "As Black against 1.e4", "As Black against 1.d4"),
            OpeningGroup.entries.map(::groupTitle),
        )
    }

    @Test fun everyModeHasALabel() {
        assertEquals(listOf("Learn", "Practise"), OpeningMode.entries.map(::modeLabel))
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
        assertEquals("", practiseFeedback(OpeningFeedback.NONE, clean = true))
        assertTrue(practiseFeedback(OpeningFeedback.CORRECT, clean = true).contains("Book move"))
        assertTrue(practiseFeedback(OpeningFeedback.WRONG, clean = false).contains("Not the book move"))
        assertEquals("Line complete ✓", practiseFeedback(OpeningFeedback.COMPLETE, clean = true))
        assertEquals("Line complete — try it clean", practiseFeedback(OpeningFeedback.COMPLETE, clean = false))
    }
}
