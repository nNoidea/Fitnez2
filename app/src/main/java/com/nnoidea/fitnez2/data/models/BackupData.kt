package com.nnoidea.fitnez2.data.models

import com.google.gson.annotations.SerializedName

data class ExportedExercise(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String,
    @SerializedName("records")
    val records: List<ExportedRecord>
)

data class ExportedRecord(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("sets")
    val sets: Int,
    @SerializedName("reps")
    val reps: Int,
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("date")
    val date: Long
)

data class BackupData(
    @SerializedName("version")
    val version: Int = 2,
    @SerializedName("data")
    val data: List<ExportedExercise>
)
