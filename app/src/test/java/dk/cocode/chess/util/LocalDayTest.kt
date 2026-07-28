package dk.cocode.chess.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class LocalDayTest {
    @Test fun convertsWallClockMillisToLocalCalendarDays() {
        val default = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"))
            assertEquals(0L, localEpochDay(0L)) // epoch = 02:00 local, Jan 1 1970
            assertEquals(1L, localEpochDay(82_800_000L)) // 23:00 UTC Jan 1 = 01:00 local Jan 2
        } finally {
            TimeZone.setDefault(default)
        }
    }

    @Test fun defaultsToNow() {
        assertTrue(localEpochDay() > 20_000) // any real "now" is decades past the epoch
    }

    /** Differential check vs java.time (host JVM only) — pins the DST-aware offset. */
    @Test fun matchesJavaTimeAcrossDstTransitions() {
        val default = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Copenhagen"))
            val instants = listOf(
                "2026-07-27T22:30:00Z", // 00:30 CEST Jul 28 — a raw-offset regression lands a day early
                "2026-03-29T01:30:00Z", // inside the spring-forward hour
                "2026-10-25T01:30:00Z", // inside the fall-back hour
            )
            for (iso in instants) {
                val instant = java.time.Instant.parse(iso)
                val expected = java.time.LocalDate.ofInstant(instant, java.time.ZoneId.of("Europe/Copenhagen"))
                assertEquals(iso, expected.toEpochDay(), localEpochDay(instant.toEpochMilli()))
            }
        } finally {
            TimeZone.setDefault(default)
        }
    }
}
