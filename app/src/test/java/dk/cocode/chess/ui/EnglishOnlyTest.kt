package dk.cocode.chess.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

/** What the move field keeps of what is typed into it; the field itself is in [TypeMoveTest]. */
class EnglishOnlyTest {
    @Test fun keepsAMoveAsItIsWritten() = assertEquals("Nf3 e2-e4 O-O e8=Q 0-0", englishOnly("Nf3 e2-e4 O-O e8=Q 0-0"))

    @Test fun readsNumbersWrittenInOtherScripts() {
        val persian = "e\u06F2e\u06F4" // ۲ ۴
        val arabic = "e\u0662e\u0664" // ٢ ٤
        val fullWidth = "\uFF45\uFF12\uFF45\uFF14" // ｅ２ｅ４
        val chinese = "e\u4E8Ce\u56DB" // 二 四
        listOf(persian, arabic, fullWidth, chinese).forEach { assertEquals(it, "e2e4", englishOnly(it)) }
        assertEquals("a1 b2 c3 d4 e5 f6 g7 h8", englishOnly("a\u4E00 b\u4E8C c\u4E09 d\u56DB e\u4E94 f\u516D g\u4E03 h\u516B"))
    }

    /** A selection over dropped letters keeps the same English ones selected. */
    @Test fun aSelectionKeepsTheSameLetters() {
        val typed = TextFieldValue("\u0436Nf\u04363", TextRange(1, 4)) // жNfж3, "Nfж" selected
        assertEquals(TextFieldValue("Nf3", TextRange(0, 2)), englishOnly(typed))
    }
}
