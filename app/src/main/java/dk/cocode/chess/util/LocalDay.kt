package dk.cocode.chess.util

import java.util.TimeZone

private const val DAY_MILLIS = 86_400_000L

/** The local calendar day as days since the epoch (java.time needs API 26; minSdk is 24). */
fun localEpochDay(nowMillis: Long = System.currentTimeMillis()): Long =
    (nowMillis + TimeZone.getDefault().getOffset(nowMillis)) / DAY_MILLIS
