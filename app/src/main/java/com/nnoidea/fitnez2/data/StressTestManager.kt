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
        database.workoutDao().deleteAllWorkouts()
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
        val startDate = LocalDate.of(2000, 1, 1) // Starting from 2000 as dialog says
        val daysToSimulate = 10000
        val recordsPerDay = 100
        val batchSize = 10000
        val records = ArrayList<Record>(batchSize)

        for (day in 0 until daysToSimulate) {
            if (day % 100 == 0) {
                val p = 0.2f + (0.8f * (day.toFloat() / daysToSimulate.toFloat()))
                onProgress(p, "Generating and inserting day ${day + 1}...")
            }

            val currentDate = startDate.plusDays(day.toLong())
            val timestamp = currentDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

            repeat(recordsPerDay) {
                val exercise = savedExercises.random()
                
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
                        weight = weight
                    )
                )
            }

            // Insert in batches to prevent OutOfMemory crashes and massive SQL transactions
            if (records.size >= batchSize) {
                database.recordDao().insertAll(records)
                records.clear()
            }
        }
        
        // Insert any remaining records
        if (records.isNotEmpty()) {
            database.recordDao().insertAll(records)
        }

        onProgress(1.0f, "Stress test complete. Created 20 exercises and ${records.size} records.")
    }
}
