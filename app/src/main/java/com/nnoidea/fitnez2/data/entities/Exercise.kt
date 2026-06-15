package com.nnoidea.fitnez2.data.entities

import androidx.room.ColumnInfo
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
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String
)
