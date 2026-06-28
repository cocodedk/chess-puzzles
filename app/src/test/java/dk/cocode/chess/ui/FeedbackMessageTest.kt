package dk.cocode.chess.ui

import dk.cocode.chess.viewmodel.Feedback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackMessageTest {
    @Test fun allFeedbackValues() {
        assertEquals("", feedbackMessage(Feedback.NONE))
        assertTrue(feedbackMessage(Feedback.CORRECT).isNotEmpty())
        assertTrue(feedbackMessage(Feedback.SOLVED).contains("Solved"))
        assertTrue(feedbackMessage(Feedback.WRONG).isNotEmpty())
    }
}
