package com.nnoidea.fitnez2.core

import java.time.Instant
import java.time.ZoneId

object TimeUtils {
    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val zone = ZoneId.systemDefault()
        val date1 = Instant.ofEpochMilli(millis1).atZone(zone).toLocalDate()
        val date2 = Instant.ofEpochMilli(millis2).atZone(zone).toLocalDate()
        return date1.isEqual(date2)
    }
}
