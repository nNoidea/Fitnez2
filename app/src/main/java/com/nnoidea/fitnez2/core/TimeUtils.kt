package com.nnoidea.fitnez2.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun getWorkoutLocalDate(
        epochMillis: Long,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .minusHours(rolloverHour.toLong())
            .toLocalDate()
    }

    fun getWorkoutEpochMillis(
        epochMillis: Long,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        if (rolloverHour == 0) return epochMillis
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .minusHours(rolloverHour.toLong())
            .toInstant()
            .toEpochMilli()
    }

    fun getWorkoutDayStartEpochMillis(
        date: LocalDate,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        return date.atTime(rolloverHour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun isSameDay(
        millis1: Long,
        millis2: Long,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val date1 = getWorkoutLocalDate(millis1, rolloverHour, zoneId)
        val date2 = getWorkoutLocalDate(millis2, rolloverHour, zoneId)
        return date1.isEqual(date2)
    }

    fun isToday(
        epochMillis: Long,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val today = getWorkoutLocalDate(System.currentTimeMillis(), rolloverHour, zoneId)
        val date = getWorkoutLocalDate(epochMillis, rolloverHour, zoneId)
        return date.isEqual(today)
    }

    fun isYesterday(
        epochMillis: Long,
        rolloverHour: Int = 0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        val today = getWorkoutLocalDate(System.currentTimeMillis(), rolloverHour, zoneId)
        val date = getWorkoutLocalDate(epochMillis, rolloverHour, zoneId)
        return date.isEqual(today.minusDays(1))
    }

    fun formatTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .format(timeFormatter)
    }
}
