package com.nnoidea.fitnez2.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class TimeUtilsTest {

    @Test
    fun isSameDay_sameTimestamp_returnsTrue() {
        val now = System.currentTimeMillis()
        assertTrue(TimeUtils.isSameDay(now, now))
    }

    @Test
    fun isSameDay_differentTimesSameDay_returnsTrue() {
        val zone = ZoneId.systemDefault()
        val baseDate = LocalDate.of(2026, 6, 15)
        val morning = baseDate.atTime(8, 30).atZone(zone).toInstant().toEpochMilli()
        val evening = baseDate.atTime(21, 45).atZone(zone).toInstant().toEpochMilli()

        assertTrue(TimeUtils.isSameDay(morning, evening))
    }

    @Test
    fun isSameDay_consecutiveDays_returnsFalse() {
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2026, 6, 15).atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
        val day2 = LocalDate.of(2026, 6, 16).atTime(0, 1).atZone(zone).toInstant().toEpochMilli()

        assertFalse(TimeUtils.isSameDay(day1, day2))
    }

    @Test
    fun isSameDay_differentYearsSameDayOfMonth_returnsFalse() {
        val zone = ZoneId.systemDefault()
        val y2025 = LocalDate.of(2025, 6, 15).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val y2026 = LocalDate.of(2026, 6, 15).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

        assertFalse(TimeUtils.isSameDay(y2025, y2026))
    }

    @Test
    fun formatTime_formatsHourMinuteSecondCorrectly() {
        val utcZone = ZoneOffset.UTC
        val timestamp = Instant.parse("2026-06-15T14:35:42Z").toEpochMilli()
        val formatted = TimeUtils.formatTime(timestamp, utcZone)

        assertEquals("14:35:42", formatted)
    }

    @Test
    fun formatTime_midnight_formatsAsZeros() {
        val utcZone = ZoneOffset.UTC
        val timestamp = Instant.parse("2026-01-01T00:00:00Z").toEpochMilli()
        val formatted = TimeUtils.formatTime(timestamp, utcZone)

        assertEquals("00:00:00", formatted)
    }
}
