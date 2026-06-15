package com.nnoidea.fitnez2.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TimeUtilsTest {

    @Test
    fun `isSameDay same day same millisecond`() {
        val millis = System.currentTimeMillis()
        assertTrue(TimeUtils.isSameDay(millis, millis))
    }

    @Test
    fun `isSameDay same day different times`() {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.of(2024, 6, 15)
        val morningMillis = date.atTime(LocalTime.of(8, 0)).atZone(zone).toInstant().toEpochMilli()
        val eveningMillis = date.atTime(LocalTime.of(20, 0)).atZone(zone).toInstant().toEpochMilli()
        assertTrue(TimeUtils.isSameDay(morningMillis, eveningMillis))
    }

    @Test
    fun `isSameDay different days`() {
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2024, 6, 15).atStartOfDay(zone).toInstant().toEpochMilli()
        val day2 = LocalDate.of(2024, 6, 16).atStartOfDay(zone).toInstant().toEpochMilli()
        assertFalse(TimeUtils.isSameDay(day1, day2))
    }

    @Test
    fun `isSameDay one day apart`() {
        val zone = ZoneId.systemDefault()
        val day1 = LocalDate.of(2024, 6, 15).atStartOfDay(zone).toInstant().toEpochMilli()
        val day3 = LocalDate.of(2024, 6, 17).atStartOfDay(zone).toInstant().toEpochMilli()
        assertFalse(TimeUtils.isSameDay(day1, day3))
    }

    @Test
    fun `isSameDay boundary end of day vs start of next day`() {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.of(2024, 6, 15)
        val endOfDayMillis = date.atTime(LocalTime.of(23, 59, 59, 999_999_999))
            .atZone(zone).toInstant().toEpochMilli()
        val startOfNextDayMillis = date.plusDays(1).atStartOfDay(zone)
            .toInstant().toEpochMilli()
        assertFalse(TimeUtils.isSameDay(endOfDayMillis, startOfNextDayMillis))
    }

    @Test
    fun `isSameDay same epoch millisecond`() {
        assertTrue(TimeUtils.isSameDay(0L, 0L))
    }

    @Test
    fun `isSameDay epoch boundary`() {
        val zone = ZoneId.systemDefault()
        val jan1Millis = LocalDate.of(1970, 1, 1).atStartOfDay(zone)
            .toInstant().toEpochMilli()
        val jan2Millis = LocalDate.of(1970, 1, 2).atStartOfDay(zone)
            .toInstant().toEpochMilli()
        assertFalse(TimeUtils.isSameDay(jan1Millis, jan2Millis))
    }
}
