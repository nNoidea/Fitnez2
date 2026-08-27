package com.nnoidea.fitnez2.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val zone = ZoneId.systemDefault()
        val date1 = Instant.ofEpochMilli(millis1).atZone(zone).toLocalDate()
        val date2 = Instant.ofEpochMilli(millis2).atZone(zone).toLocalDate()
        return date1.isEqual(date2)
    }

    fun formatTime(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zoneId)
            .format(timeFormatter)
    }
}
