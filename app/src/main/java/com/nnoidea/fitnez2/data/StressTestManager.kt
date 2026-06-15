package com.nnoidea.fitnez2.data

import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

object StressTestManager {

    private val seedExercises = listOf(
        "Squat", "Bench Press", "Deadlift", "Overhead Press", "Barbell Row",
        "Pull Up", "Dips", "Lunge", "Bicep Curl", "Tricep Extension",
        "Leg Press", "Lat Pulldown", "Chest Fly", "Lateral Raise", "Shrug",
        "Calf Raise", "Hammer Curl", "Skull Crusher", "Face Pull", "Plank"
    )

    suspend fun performStressTest(database: AppDatabase, onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        onProgress(0f, "Clearing existing data...")
        database.recordDao().deleteAllRecords()
        database.workoutDao().deleteAllWorkouts()
        database.exerciseDao().deleteAllExercises()

        onProgress(0.05f, "Creating exercises...")
        val exercises = seedExercises.map { Exercise(name = it) }
        database.exerciseDao().insertAll(exercises)

        val savedExercises = database.exerciseDao().getAllExercises()
        if (savedExercises.isEmpty()) {
            throw IllegalStateException("Failed to create exercises")
        }

        val yesterday = LocalDate.now().minusDays(1)
        val daysToSimulate = 10000
        val recordsPerDay = 100
        val batchSize = 10000
        val records = ArrayList<Record>(batchSize)
        var totalInserted = 0

        for (day in 0 until daysToSimulate) {
            if ((day % 100) == 0) {
                val p = 0.1f + (0.9f * (day.toFloat() / daysToSimulate))
                val simulatedDate = yesterday.minusDays(day.toLong())
                val recordsSoFar = (day + 1) * recordsPerDay
                onProgress(p, "$recordsSoFar / ${daysToSimulate * recordsPerDay} records ($simulatedDate)")
            }

            val currentDate = yesterday.minusDays(day.toLong())
            val timestamp = currentDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

            repeat(recordsPerDay) {
                val exercise = savedExercises.random()
                records.add(
                    Record(
                        exerciseId = exercise.id,
                        date = timestamp,
                        sets = Random.nextInt(1, 6),
                        reps = Random.nextInt(5, 16),
                        weight = Random.nextDouble(10.0, 100.0)
                    )
                )
            }

            if (records.size >= batchSize) {
                database.recordDao().insertAll(records)
                totalInserted += records.size
                records.clear()
            }
        }

        if (records.isNotEmpty()) {
            database.recordDao().insertAll(records)
            totalInserted += records.size
        }

        onProgress(1.0f, "Complete. $totalInserted records across $daysToSimulate days (${yesterday.minusDays((daysToSimulate - 1).toLong())} → $yesterday)")
    }
}
