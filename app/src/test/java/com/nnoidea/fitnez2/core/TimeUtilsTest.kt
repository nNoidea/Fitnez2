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

    @Test
    fun isSameDay_withRolloverHour_acrossMidnightBelongsToSameDay() {
        val zone = ZoneId.systemDefault()
        val monday = LocalDate.of(2026, 10, 5)
        val mondayLate = monday.atTime(23, 30).atZone(zone).toInstant().toEpochMilli()
        val tuesdayEarly = LocalDate.of(2026, 10, 6).atTime(1, 30).atZone(zone).toInstant().toEpochMilli()

        // With 0 rollover (standard midnight), they are different days
        assertFalse(TimeUtils.isSameDay(mondayLate, tuesdayEarly, rolloverHour = 0, zoneId = zone))

        // With 4 hour rollover, both belong to Monday
        assertTrue(TimeUtils.isSameDay(mondayLate, tuesdayEarly, rolloverHour = 4, zoneId = zone))

        // Also test with 12 hour rollover
        assertTrue(TimeUtils.isSameDay(mondayLate, tuesdayEarly, rolloverHour = 12, zoneId = zone))
    }

    @Test
    fun isSameDay_withRolloverHour_pastRolloverHourBelongsToNextDay() {
        val zone = ZoneId.systemDefault()
        val tuesdayEarly = LocalDate.of(2026, 10, 6).atTime(3, 59).atZone(zone).toInstant().toEpochMilli()
        val tuesdayPastRollover = LocalDate.of(2026, 10, 6).atTime(4, 1).atZone(zone).toInstant().toEpochMilli()

        // Rollover at 4 AM: 03:59 is previous day (Monday), 04:01 is Tuesday
        assertFalse(TimeUtils.isSameDay(tuesdayEarly, tuesdayPastRollover, rolloverHour = 4, zoneId = zone))
    }

    @Test
    fun getWorkoutLocalDate_withRolloverHour_mapsCorrectly() {
        val zone = ZoneId.systemDefault()
        val tuesdayEarly = LocalDate.of(2026, 10, 6).atTime(1, 30).atZone(zone).toInstant().toEpochMilli()

        // Without rollover: Oct 6
        assertEquals(LocalDate.of(2026, 10, 6), TimeUtils.getWorkoutLocalDate(tuesdayEarly, rolloverHour = 0, zoneId = zone))

        // With 4h rollover: Oct 5
        assertEquals(LocalDate.of(2026, 10, 5), TimeUtils.getWorkoutLocalDate(tuesdayEarly, rolloverHour = 4, zoneId = zone))
    }

    @Test
    fun getWorkoutEpochMillis_and_dayStart() {
        val zone = ZoneId.systemDefault()
        val monday = LocalDate.of(2026, 10, 5)
        val dayStart = TimeUtils.getWorkoutDayStartEpochMillis(monday, rolloverHour = 4, zoneId = zone)
        assertEquals(monday.atTime(4, 0).atZone(zone).toInstant().toEpochMilli(), dayStart)

        val tuesdayEarly = LocalDate.of(2026, 10, 6).atTime(1, 30).atZone(zone).toInstant().toEpochMilli()
        val shifted = TimeUtils.getWorkoutEpochMillis(tuesdayEarly, rolloverHour = 4, zoneId = zone)
        val shiftedDate = Instant.ofEpochMilli(shifted).atZone(zone).toLocalDate()
        assertEquals(monday, shiftedDate)
    }
}
