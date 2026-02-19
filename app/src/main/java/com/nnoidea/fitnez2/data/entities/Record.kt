package com.nnoidea.fitnez2.data.entities

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
        Index(value = ["exerciseId", "date", "id"]), // Composite index for filtered sorting
        Index(value = ["date", "id"]) // Keep for "All History" query
    ]
)
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseId: Int,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val date: Long
) {

}
