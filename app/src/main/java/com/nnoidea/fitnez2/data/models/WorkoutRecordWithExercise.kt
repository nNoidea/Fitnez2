package com.nnoidea.fitnez2.data.models

import androidx.room.Embedded
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.core.localization.globalLocalization

data class WorkoutRecordWithExercise(
    @Embedded val workoutRecord: WorkoutRecord,
    val exerciseName: String
) {
    val formattedSets: String
        get() = "${workoutRecord.sets} ${globalLocalization.labelSets}"

    val formattedReps: String
        get() = "${workoutRecord.reps} ${globalLocalization.labelReps}"

    fun formattedWeight(unit: String): String {
        return "${workoutRecord.weight} $unit"
    }
}
