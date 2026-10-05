package com.nnoidea.fitnez2.data.models

import androidx.room.Embedded
import com.nnoidea.fitnez2.data.entities.WorkoutRecord

data class WorkoutRecordWithExercise(
    @Embedded val workoutRecord: WorkoutRecord,
    val exerciseName: String
)
