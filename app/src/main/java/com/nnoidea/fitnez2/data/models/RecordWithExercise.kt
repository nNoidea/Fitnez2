package com.nnoidea.fitnez2.data.models

import androidx.room.Embedded
import com.nnoidea.fitnez2.data.entities.Record

data class RecordWithExercise(
    @Embedded val record: Record,
    val exerciseName: String
)
