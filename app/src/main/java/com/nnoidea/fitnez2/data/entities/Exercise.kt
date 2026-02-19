package com.nnoidea.fitnez2.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
