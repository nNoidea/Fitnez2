package com.nnoidea.fitnez2.ui.components.history

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise

// =============================================================================
// Shared utilities for building history-style lists.
// Used by ExerciseHistoryList, ExerciseHistoryState, and WorkoutExerciseList.
// =============================================================================

/**
 * Computes the alternating color parity for each record in a list.
 * Walks from the oldest record (bottom) upward, toggling when exerciseId changes.
 * This keeps colors stable when new records are added at the top.
 *
 * @return A BooleanArray where `true` = light variant, `false` = colored variant
 */
fun computeColorParity(records: List<Record>): BooleanArray {
    if (records.isEmpty()) return BooleanArray(0)

    val isLightArray = BooleanArray(records.size)
    var currentIsLight = true
    var lastExerciseId = records.last().exerciseId
    isLightArray[records.lastIndex] = currentIsLight

    for (i in records.lastIndex - 1 downTo 0) {
        if (records[i].exerciseId != lastExerciseId) {
            currentIsLight = !currentIsLight
            lastExerciseId = records[i].exerciseId
        }
        isLightArray[i] = currentIsLight
    }

    return isLightArray
}

/**
 * Computes the alternating color parity based on exercise names.
 * Used for in-memory lists (e.g. workout) where we group by exercise name.
 *
 * @return A BooleanArray where `true` = light variant, `false` = colored variant
 */
fun computeColorParityByName(items: List<RecordWithExercise>): BooleanArray {
    if (items.isEmpty()) return BooleanArray(0)

    val isLightArray = BooleanArray(items.size)
    var currentIsLight = true
    var lastExerciseName = items.last().exerciseName
    isLightArray[items.lastIndex] = currentIsLight

    for (i in items.lastIndex - 1 downTo 0) {
        if (items[i].exerciseName != lastExerciseName) {
            currentIsLight = !currentIsLight
            lastExerciseName = items[i].exerciseName
        }
        isLightArray[i] = currentIsLight
    }

    return isLightArray
}

@JvmName("computeColorParityByWorkoutName")
fun computeColorParityByName(items: List<com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise>): BooleanArray {
    if (items.isEmpty()) return BooleanArray(0)

    val isLightArray = BooleanArray(items.size)
    var currentIsLight = true
    var lastExerciseName = items.last().exerciseName
    isLightArray[items.lastIndex] = currentIsLight

    for (i in items.lastIndex - 1 downTo 0) {
        if (items[i].exerciseName != lastExerciseName) {
            currentIsLight = !currentIsLight
            lastExerciseName = items[i].exerciseName
        }
        isLightArray[i] = currentIsLight
    }

    return isLightArray
}

/**
 * Computes the rounded corner shape for a record card based on
 * whether adjacent items share the same group (color parity).
 */
fun recordCardShape(prevIsSame: Boolean, nextIsSame: Boolean): Shape = when {
    !prevIsSame && !nextIsSame -> RoundedCornerShape(28.dp)
    !prevIsSame && nextIsSame -> RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )
    prevIsSame && !nextIsSame -> RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 28.dp,
        bottomEnd = 28.dp
    )
    else -> RoundedCornerShape(4.dp)
}

/**
 * Builds the flat UI list from raw records (DESC order from DB).
 * Inserts date headers between days.
 *
 * @param section Used to generate unique header keys across batches.
 *                Section 0 = recent, 1+ = older batches.
 */
fun buildUiItems(
    records: List<Record>,
    exerciseMap: Map<Int, String>,
    useAlternatingColors: Boolean,
    section: Int = 0
): List<HistoryUiModel> {
    if (records.isEmpty()) return emptyList()

    val isLightArray = computeColorParity(records)

    val result = mutableListOf<HistoryUiModel>()
    for (i in records.indices) {
        val record = records[i]
        val exerciseName = exerciseMap[record.exerciseId] ?: globalLocalization.labelUnknownExercise
        val recordWithExercise = RecordWithExercise(record, exerciseName)
        val isLight = if (!useAlternatingColors) true else isLightArray[i]

        // Insert header if this is the first record or a new day
        if (i == 0 || !TimeUtils.isSameDay(records[i - 1].date, record.date)) {
            result.add(HistoryUiModel.Header(record.date, section))
        }

        result.add(HistoryUiModel.RecordItem(recordWithExercise, isLight))
    }

    return result
}
