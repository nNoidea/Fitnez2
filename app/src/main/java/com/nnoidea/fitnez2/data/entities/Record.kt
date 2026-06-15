package com.nnoidea.fitnez2.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "record",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exerciseId", "date", "orderNumber", "id"]),
        Index(value = ["date", "orderNumber", "id"])
    ]
)
data class Record(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val date: Long,
    val orderNumber: Int = 0
)
