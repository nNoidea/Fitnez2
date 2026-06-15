package com.nnoidea.fitnez2.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_record",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["exerciseId"])
    ]
)
data class WorkoutRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val workoutId: String,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val date: Long = System.currentTimeMillis(),
    val orderNumber: Int = 0
)
