package com.nnoidea.fitnez2.data.models

import androidx.room.Embedded
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.core.localization.globalLocalization

data class RecordWithExercise(
    @Embedded val record: Record,
    val exerciseName: String
) {
    val formattedSets: String
        get() = "${record.sets} ${globalLocalization.labelSets}"

    val formattedReps: String
        get() = "${record.reps} ${globalLocalization.labelReps}"

    fun formattedWeight(unit: String): String {
        return "${record.weight} $unit"
    }
}
