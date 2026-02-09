package com.nnoidea.fitnez2.data.models

import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record

data class BackupData(
    val version: Int = 1,
    val exercises: List<Exercise>,
    val records: List<Record>
)
