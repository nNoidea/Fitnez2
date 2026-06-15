package com.nnoidea.fitnez2.ui.screens.timeline

import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.ui.components.recordlist.RecordDisplayItem
import com.nnoidea.fitnez2.ui.components.recordlist.prepareRecordDisplayItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class MonthlyToTimelineNavigationTest {

    private val exerciseMap = mapOf("a" to "Squat", "b" to "Bench Press", "c" to "Deadlift")
    private val zone = ZoneId.systemDefault()

    @Test
    fun `DateHeader matches MonthlyScreen epoch millis for today records`() {
        val today = LocalDate.now(zone)
        val monthlyTarget = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val recordTimestamp = System.currentTimeMillis()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, recordTimestamp, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val targetIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, monthlyTarget)
        }
        assertTrue(targetIndex >= 0)
        assertTrue(items[targetIndex] is RecordDisplayItem.DateHeader)
    }

    @Test
    fun `DateHeader matches for record created at start of day`() {
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, startOfToday, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val targetIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, startOfToday)
        }
        assertTrue(targetIndex >= 0)
    }

    @Test
    fun `DateHeader matches for record created at end of day`() {
        val today = LocalDate.now(zone)
        val endOfToday = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, endOfToday, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)
        val monthlyTarget = today.atStartOfDay(zone).toInstant().toEpochMilli()

        val targetIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, monthlyTarget)
        }
        assertTrue(targetIndex >= 0)
    }

    @Test
    fun `DateHeader returns -1 for day with no records`() {
        val yesterday = LocalDate.now(zone).minusDays(1)
        val yesterdayMillis = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, System.currentTimeMillis(), 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val targetIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, yesterdayMillis)
        }
        assertEquals(-1, targetIndex)
    }

    @Test
    fun `scroll target index is correct for multi-day data`() {
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val todayMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val yesterdayMillis = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
        val twoDaysAgoMillis = twoDaysAgo.atStartOfDay(zone).toInstant().toEpochMilli()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, todayMillis + 3600000, 0),
            Record("2", "b", 3, 10, 50.0, yesterdayMillis + 7200000, 0),
            Record("3", "c", 3, 10, 50.0, twoDaysAgoMillis + 10000000, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val yesterdayIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, yesterdayMillis)
        }
        assertTrue(yesterdayIndex >= 0)
        assertTrue(items[yesterdayIndex] is RecordDisplayItem.DateHeader)
    }

    @Test
    fun `MonthlyScreen epoch millis is consistent with isSameDay`() {
        val day = LocalDate.now(zone)
        val epochMillis = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val recordDate = System.currentTimeMillis()

        assertTrue(com.nnoidea.fitnez2.core.TimeUtils.isSameDay(recordDate, epochMillis))
    }

    @Test
    fun `epoch millis round-trips through MonthlyScreen and DateHeader`() {
        val targetDay = LocalDate.of(2025, 6, 15)
        val monthlyTarget = targetDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val recordDate = targetDay.atStartOfDay(zone).plusHours(14).toInstant().toEpochMilli()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, recordDate, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val header = items.filterIsInstance<RecordDisplayItem.DateHeader>().first()
        assertEquals(recordDate, header.date)
        assertTrue(com.nnoidea.fitnez2.core.TimeUtils.isSameDay(header.date, monthlyTarget))
    }

    @Test
    fun `target date not in first 100 records still finds DateHeader after loading more`() {
        val today = LocalDate.now(zone)
        val thirtyDaysAgo = today.minusDays(30)
        val thirtyDaysAgoMillis = thirtyDaysAgo.atStartOfDay(zone).plusHours(10).toInstant().toEpochMilli()
        val monthlyTarget = thirtyDaysAgo.atStartOfDay(zone).toInstant().toEpochMilli()

        // Simulate: recent records for days 0-99 (BUT skip day 30)
        val recentRecords = (0..99).mapNotNull { i ->
            val day = today.minusDays(i.toLong())
            if (i == 30) null  // skip the target day
            else Record(i.toString(), "a", 3, 10, 50.0, day.atStartOfDay(zone).plusHours(12).toInstant().toEpochMilli(), 0)
        }
        val oldRecord = Record("old", "a", 3, 10, 50.0, thirtyDaysAgoMillis, 0)

        // First 100 = recent days without the target date
        val first100 = recentRecords.filterNotNull().take(100)
        val first100Items = prepareRecordDisplayItems(first100, exerciseMap, true, 0)
        val notFound = first100Items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, monthlyTarget)
        }
        assertEquals(-1, notFound)

        // After "loading more" = recent + the old skipped-day record
        val allRecords = recentRecords.filterNotNull() + oldRecord
        val allItems = prepareRecordDisplayItems(allRecords, exerciseMap, true, 0)
        val found = allItems.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, monthlyTarget)
        }
        assertTrue(found >= 0)
    }

    @Test
    fun `prepareRecordDisplayItems contains date for records 6 months ago`() {
        val sixMonthsAgo = LocalDate.now(zone).minusMonths(6)
        val sixMonthsAgoMillis = sixMonthsAgo.atStartOfDay(zone).plusHours(8).toInstant().toEpochMilli()
        val monthlyTarget = sixMonthsAgo.atStartOfDay(zone).toInstant().toEpochMilli()

        val records = listOf(
            Record("1", "a", 3, 10, 50.0, sixMonthsAgoMillis, 0),
        )
        val items = prepareRecordDisplayItems(records, exerciseMap, true, 0)

        val targetIndex = items.indexOfFirst {
            it is RecordDisplayItem.DateHeader &&
                com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, monthlyTarget)
        }
        assertTrue(targetIndex >= 0)
    }
}
