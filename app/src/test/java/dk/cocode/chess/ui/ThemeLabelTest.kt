package dk.cocode.chess.ui

import dk.cocode.chess.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeLabelTest {
    @Test fun allModesHaveDistinctLabels() {
        assertEquals("Theme: Auto", themeLabel(ThemeMode.SYSTEM))
        assertEquals("Theme: Light", themeLabel(ThemeMode.LIGHT))
        assertEquals("Theme: Dark", themeLabel(ThemeMode.DARK))
    }
}
