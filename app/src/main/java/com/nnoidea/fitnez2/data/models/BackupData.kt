package com.nnoidea.fitnez2.data.models

import com.google.gson.annotations.SerializedName

data class ExportedExercise(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String,
    @SerializedName("records")
    val records: List<ExportedRecord>
)

data class ExportedRecord(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("sets")
    val sets: Int,
    @SerializedName("reps")
    val reps: Int,
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("date")
    val date: Long
)

data class ExportedWorkoutRecord(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("exerciseId")
    val exerciseId: String,
    @SerializedName("sets")
    val sets: Int,
    @SerializedName("reps")
    val reps: Int,
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("date")
    val date: Long = 0L
)

data class ExportedWorkout(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String,
    @SerializedName("records")
    val records: List<ExportedWorkoutRecord>
)

data class BackupData(
    @SerializedName("version")
    val version: Int = 3,
    @SerializedName("data")
    val data: List<ExportedExercise>,
    @SerializedName("workouts")
    val workouts: List<ExportedWorkout>? = null
)
