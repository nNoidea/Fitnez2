package com.nnoidea.fitnez2.data

import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

object StressTestManager {

    suspend fun performStressTest(database: AppDatabase, onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        onProgress(0f, "Clearing existing data...")
        database.recordDao().deleteAllRecords()
        database.exerciseDao().deleteAllExercises()

        onProgress(0.1f, "Creating 20 exercises...")
        val exercises = (1..20).map { i ->
            Exercise(id = 0, name = "Exercise $i")
        }
        database.exerciseDao().insertAll(exercises)

        // Retrieve exercises to get their auto-generated IDs
        val savedExercises = database.exerciseDao().getAllExercises()
        if (savedExercises.isEmpty()) {
            throw IllegalStateException("Failed to create exercises")
        }

        onProgress(0.2f, "Generating records...")
        val startDate = LocalDate.of(1990, 1, 1) // Starting from 2020
        val daysToSimulate = 10000
        val recordsPerDay = 100
        val records = ArrayList<Record>()

        var currentGroupIndex = 0
        var lastExerciseId = -1

        for (day in 0 until daysToSimulate) {
            if (day % 10 == 0) {
                val p = 0.2f + (0.3f * (day.toFloat() / daysToSimulate.toFloat()))
                onProgress(p, "Generating day ${day + 1}...")
            }

            val currentDate = startDate.plusDays(day.toLong())
            // Use UTC start of day for consistency
            val timestamp = currentDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

            repeat(recordsPerDay) {
                val exercise = savedExercises.random()
                
                // Calculate groupIndex (increment if exercise changes)
                if (lastExerciseId != -1 && exercise.id != lastExerciseId) {
                    currentGroupIndex++
                }
                lastExerciseId = exercise.id
                
                // Randomize data slightly
                val sets = Random.nextInt(1, 6)
                val reps = Random.nextInt(5, 16)
                val weight = Random.nextDouble(10.0, 100.0)

                records.add(
                    Record(
                        id = 0,
                        exerciseId = exercise.id,
                        date = timestamp,
                        sets = sets,
                        reps = reps,
                        weight = weight,
                        groupIndex = currentGroupIndex
                    )
                )
            }
        }
        
        onProgress(0.5f, "Inserting ${records.size} records...")
        database.recordDao().insertAll(records)

        onProgress(1.0f, "Stress test complete. Created 20 exercises and ${records.size} records.")
    }
}
