package com.nnoidea.fitnez2.ui.components.recordlist

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise

sealed class RecordDisplayItem {
    data class DateHeader(val date: Long, val section: Int = 0) : RecordDisplayItem()
    data class RecordGroup(
        val records: List<RecordWithExercise>,
        val isLight: Boolean
    ) : RecordDisplayItem()
    data class BatchSeparator(val index: Int) : RecordDisplayItem()
    data class EvictedBatch(val index: Int, val heightDp: Int) : RecordDisplayItem()
    data object LoadingMore : RecordDisplayItem()
}

fun recordCardShape(prevSame: Boolean, nextSame: Boolean): Shape = when {
    !prevSame && !nextSame -> RoundedCornerShape(28.dp)
    !prevSame && nextSame -> RoundedCornerShape(28.dp, 28.dp, 4.dp, 4.dp)
    prevSame && !nextSame -> RoundedCornerShape(4.dp, 4.dp, 28.dp, 28.dp)
    else -> RoundedCornerShape(4.dp)
}

fun prepareRecordDisplayItems(
    records: List<Record>,
    exerciseMap: Map<String, String>,
    useAlternatingColors: Boolean,
    section: Int = 0
): List<RecordDisplayItem> {
    if (records.isEmpty()) return emptyList()

    val dayGroups = mutableListOf<Triple<Long, List<RecordWithExercise>, Boolean>>()

    val oldestFirst = records.reversed()

    var currentExerciseName: String? = null
    var currentGroup = mutableListOf<RecordWithExercise>()
    var isLight = true
    var currentDayDate: Long? = null

    fun flushGroup() {
        if (currentGroup.isNotEmpty()) {
            dayGroups.add(Triple(currentDayDate!!, currentGroup.toList(), isLight))
            isLight = !isLight
            currentGroup = mutableListOf()
        }
    }

    for (record in oldestFirst) {
        val exerciseName = exerciseMap[record.exerciseId] ?: globalLocalization.labelUnknownExercise

        if (currentDayDate == null || !TimeUtils.isSameDay(currentDayDate, record.date)) {
            flushGroup()
            currentDayDate = record.date
            isLight = true
            currentExerciseName = null
        }

        if (exerciseName != currentExerciseName && currentGroup.isNotEmpty()) {
            flushGroup()
        }

        currentExerciseName = exerciseName
        currentGroup.add(RecordWithExercise(record, exerciseName))
    }

    flushGroup()

    val result = mutableListOf<RecordDisplayItem>()
    var lastDayDate: Long? = null

    for ((dayDate, groupRecords, groupIsLight) in dayGroups.reversed()) {
        if (lastDayDate == null || !TimeUtils.isSameDay(lastDayDate, dayDate)) {
            result.add(RecordDisplayItem.DateHeader(dayDate, section))
        }
        lastDayDate = dayDate
        result.add(RecordDisplayItem.RecordGroup(
            records = groupRecords.reversed(),
            isLight = if (useAlternatingColors) groupIsLight else true
        ))
    }

    return result
}

fun prepareRecordDisplayItems(
    workoutItems: List<WorkoutRecordWithExercise>,
    useAlternatingColors: Boolean = true
): List<RecordDisplayItem> {
    if (workoutItems.isEmpty()) return emptyList()

    val records = workoutItems.map { item ->
        Record(
            id = item.workoutRecord.id,
            exerciseId = item.workoutRecord.exerciseId,
            sets = item.workoutRecord.sets,
            reps = item.workoutRecord.reps,
            weight = item.workoutRecord.weight,
            date = item.workoutRecord.date
        )
    }
    val exerciseMap = workoutItems.associate { it.workoutRecord.exerciseId to it.exerciseName }
    return prepareRecordDisplayItems(
        records = records,
        exerciseMap = exerciseMap,
        useAlternatingColors = useAlternatingColors,
        section = 0
    )
}
