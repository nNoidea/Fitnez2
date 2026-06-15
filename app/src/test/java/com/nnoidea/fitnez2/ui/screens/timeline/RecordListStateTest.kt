package com.nnoidea.fitnez2.ui.screens.timeline

import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.components.recordlist.RecordDisplayItem
import com.nnoidea.fitnez2.ui.components.recordlist.prepareRecordDisplayItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordListStateTest {

    private val exerciseMap = mapOf("a" to "Squat", "b" to "Bench Press", "c" to "Deadlift")

    // ---------------------------------------------------------------------------
    // prepareRecordDisplayItems
    // ---------------------------------------------------------------------------

    @Test
    fun `prepareRecordDisplayItems returns empty for empty input`() {
        val result = prepareRecordDisplayItems(emptyList(), exerciseMap, useAlternatingColors = true)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `prepareRecordDisplayItems inserts DateHeader before records`() {
        val records = listOf(
            record("1", "a", now()),
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)
        assertEquals(2, result.size) // DateHeader + RecordGroup
        assertTrue(result[0] is RecordDisplayItem.DateHeader)
        assertTrue(result[1] is RecordDisplayItem.RecordGroup)
    }

    @Test
    fun `prepareRecordDisplayItems groups same exercise consecutively`() {
        val now = now()
        val records = listOf(
            record("1", "a", now),    // newest: Squat
            record("2", "a", now),    // Squat
            record("3", "b", now),    // Bench (oldest)
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)

        // Display order (newest first): Squat(x2), Bench(x1)
        assertEquals(3, result.size)
        assertTrue(result[0] is RecordDisplayItem.DateHeader)

        val group1 = result[1] as RecordDisplayItem.RecordGroup
        assertEquals(2, group1.records.size) // Squat x2, at top (newest)
        assertEquals("Squat", group1.records[0].exerciseName)

        val group2 = result[2] as RecordDisplayItem.RecordGroup
        assertEquals(1, group2.records.size) // Bench x1, below
        assertEquals("Bench Press", group2.records[0].exerciseName)
    }

    @Test
    fun `prepareRecordDisplayItems resets color parity at day boundary`() {
        val today = now()
        val yesterday = today - 86400000L

        // Oldest first (will be reversed internally)
        val records = listOf(
            record("1", "a", today),      // newest: Squat
            record("2", "a", today),      // Squat
            record("3", "b", yesterday),  // Bench (oldest)
            record("4", "c", yesterday),  // Deadlift
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)

        // Expected (newest first display order):
        // DateHeader(today)  →  RecordGroup(Squat x2, isLight = true)
        // DateHeader(yesterday) → RecordGroup(Deadlift x1, isLight = true)
        //                        → RecordGroup(Bench x1, isLight = false)

        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        val todayGroups = groups.filter {
            result.indexOf(it) > 0 && result[result.indexOf(it) - 1] is RecordDisplayItem.DateHeader &&
            TimeUtils.isSameDay((result[result.indexOf(it) - 1] as RecordDisplayItem.DateHeader).date, records[0].date)
        }

        // The first group (newest day) should start with isLight = true
        assertTrue(groups[0].isLight)

        // Both days should have their first group as light
        val headers = result.filterIsInstance<RecordDisplayItem.DateHeader>()
        assertEquals(2, headers.size)
    }

    @Test
    fun `prepareRecordDisplayItems toggles isLight on exercise change within same day`() {
        val now = now()
        val records = listOf(
            record("1", "a", now), // Squat
            record("2", "b", now), // Bench
            record("3", "a", now), // Squat
            record("4", "c", now), // Deadlift
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()

        // Groups should be (oldest→newest, then reversed for display):
        // oldest: Deadlift → light, Squat → dark, Bench → light, Squat(again) → dark
        // display: Squat(dark), Bench(light), Squat(dark), Deadlift(light)
        // Wait, the second Squat at position 2 and first Squat at position 0...
        // oldest: Deadlift(light), Squat(dark), Bench(light), Squat(dark)
        // display: Squat(dark), Bench(light), Squat(dark), Deadlift(light)

        assertEquals(4, groups.size) // Each exercise change creates a new group
        assertFalse(groups[0].isLight) // Squat (2nd from bottom, toggled)
        assertTrue(groups[1].isLight)  // Bench (toggled)
        assertFalse(groups[2].isLight) // Squat (toggled)
        assertTrue(groups[3].isLight)  // Deadlift (bottom of day = light)
    }

    @Test
    fun `prepareRecordDisplayItems with useAlternatingColors false makes all groups light`() {
        val now = now()
        val records = listOf(
            record("1", "a", now),
            record("2", "b", now),
            record("3", "c", now),
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = false)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertTrue(groups.all { it.isLight })
    }

    @Test
    fun `prepareRecordDisplayItems assigns unknown label to exercises not in map`() {
        val now = now()
        val records = listOf(
            record("1", "unknown", now), // not in exerciseMap
            record("2", "a", now),   // Squat
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertEquals(2, groups.size)
        // Unknown exercise gets the localized unknown label
        assertTrue(groups.any { it.records.any { r -> r.exerciseName != "Squat" } })
        assertTrue(groups.any { it.records.any { r -> r.exerciseName == "Squat" } })
    }

    @Test
    fun `prepareRecordDisplayItems handles multiple days correctly`() {
        val today = now()
        val yesterday = today - 86400000L
        val twoDaysAgo = yesterday - 86400000L

        val records = listOf(
            record("1", "a", today),
            record("2", "a", yesterday),
            record("3", "b", yesterday),
            record("4", "c", twoDaysAgo),
            record("5", "a", twoDaysAgo),
        )
        val result = prepareRecordDisplayItems(records, exerciseMap, useAlternatingColors = true)

        val headers = result.filterIsInstance<RecordDisplayItem.DateHeader>()
        assertEquals(3, headers.size)

        // Oldest group of each day (bottom group for that day) should be isLight = true
        // Display order is newest→oldest, so the bottom group of each day is the group just before the next day's header (or end of list)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertEquals(5, groups.size)
    }

    // ---------------------------------------------------------------------------
    // prepareRecordDisplayItems (workout overload)
    // ---------------------------------------------------------------------------

    @Test
    fun `prepareRecordDisplayItems workout empty returns empty`() {
        val result = prepareRecordDisplayItems(emptyList<WorkoutRecordWithExercise>())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `prepareRecordDisplayItems workout single record returns 1 group`() {
        val wr = WorkoutRecord(workoutId = "w1", exerciseId = "a", sets = 3, reps = 10, weight = 50.0, date = System.currentTimeMillis(), orderNumber = 0)
        val item = WorkoutRecordWithExercise(wr, "Squat")
        val result = prepareRecordDisplayItems(listOf(item))
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertEquals(1, groups.size)
    }

    @Test
    fun `prepareRecordDisplayItems workout same exercise grouped together`() {
        val now = System.currentTimeMillis()
        val wr1 = WorkoutRecord(workoutId = "w1", exerciseId = "a", sets = 3, reps = 10, weight = 50.0, date = now, orderNumber = 0)
        val wr2 = WorkoutRecord(workoutId = "w1", exerciseId = "a", sets = 3, reps = 8, weight = 55.0, date = now, orderNumber = 0)
        val items = listOf(
            WorkoutRecordWithExercise(wr1, "Squat"),
            WorkoutRecordWithExercise(wr2, "Squat")
        )
        val result = prepareRecordDisplayItems(items)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].records.size)
    }

    @Test
    fun `prepareRecordDisplayItems workout different exercises create separate groups`() {
        val now = System.currentTimeMillis()
        val wr1 = WorkoutRecord(workoutId = "w1", exerciseId = "a", sets = 3, reps = 10, weight = 50.0, date = now, orderNumber = 0)
        val wr2 = WorkoutRecord(workoutId = "w1", exerciseId = "b", sets = 5, reps = 5, weight = 60.0, date = now, orderNumber = 0)
        val items = listOf(
            WorkoutRecordWithExercise(wr1, "Squat"),
            WorkoutRecordWithExercise(wr2, "Bench Press")
        )
        val result = prepareRecordDisplayItems(items)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertEquals(2, groups.size)
    }

    @Test
    fun `prepareRecordDisplayItems workout useAlternatingColors false makes all groups light`() {
        val now = System.currentTimeMillis()
        val wr1 = WorkoutRecord(workoutId = "w1", exerciseId = "a", sets = 3, reps = 10, weight = 50.0, date = now, orderNumber = 0)
        val wr2 = WorkoutRecord(workoutId = "w1", exerciseId = "b", sets = 5, reps = 5, weight = 60.0, date = now, orderNumber = 0)
        val items = listOf(
            WorkoutRecordWithExercise(wr1, "Squat"),
            WorkoutRecordWithExercise(wr2, "Bench Press")
        )
        val result = prepareRecordDisplayItems(items, useAlternatingColors = false)
        val groups = result.filterIsInstance<RecordDisplayItem.RecordGroup>()
        assertTrue(groups.all { it.isLight })
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun record(uuid: String, exerciseId: String, date: Long) = Record(
        id = uuid,
        exerciseId = exerciseId,
        date = date,
        sets = 3,
        reps = 10,
        weight = 50.0,
        orderNumber = 0
    )

    private fun now(): Long = System.currentTimeMillis()
}
