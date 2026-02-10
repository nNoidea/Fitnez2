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

        onProgress(0.01f, "Creating exercises...")
        val exercises = (1..20).map { i ->
            Exercise(
                id = 0, // 0 for auto-generate
                name = "Stress Test Exercise $i"
            )
        }
        database.exerciseDao().insertAll(exercises)

        // Retrieve exercises to get their IDs
        val savedExercises = database.exerciseDao().getAllExercises()
        if (savedExercises.isEmpty()) {
             throw IllegalStateException("Failed to create exercises")
        }

        onProgress(0.02f, "Generating records...")
        val startDate = LocalDate.of(2000, 1, 1)
        val endDate = LocalDate.of(2025, 12, 31)
        
        // Calculate total days
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        val recordsPerDay = 110
        val expectedTotalRecords = totalDays * recordsPerDay
        
        var currentDate = startDate
        var processedDays = 0L

        val batchSize = 5000
        val recordsBatch = ArrayList<Record>(batchSize)
        var totalRecords = 0

        while (!currentDate.isAfter(endDate)) {
            // 110 records per day
            repeat(recordsPerDay) {
                val exercise = savedExercises.random()
                val sets = Random.nextInt(1, 6)
                val reps = Random.nextInt(5, 16)
                val weight = Random.nextDouble(10.0, 100.0)

                // Use epoch seconds for timestamp
                val timestamp = currentDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond()

                recordsBatch.add(
                    Record(
                        id = 0,
                        exerciseId = exercise.id,
                        date = timestamp, 
                        sets = sets,
                        reps = reps,
                        weight = weight
                    )
                )

                if (recordsBatch.size >= batchSize) {
                    database.recordDao().insertAll(recordsBatch)
                    totalRecords += recordsBatch.size
                    
                    val progress = 0.02f + (0.98f * (processedDays.toFloat() / totalDays.toFloat()))
                    onProgress(progress, "Inserted $totalRecords / $expectedTotalRecords records...")
                    recordsBatch.clear()
                }
            }
            currentDate = currentDate.plusDays(1)
            processedDays++
        }

        if (recordsBatch.isNotEmpty()) {
            database.recordDao().insertAll(recordsBatch)
            totalRecords += recordsBatch.size
            onProgress(1.0f, "Inserted $totalRecords records...")
        }

        onProgress(1.0f, "Stress test complete. Total records: $totalRecords")
    }
}
