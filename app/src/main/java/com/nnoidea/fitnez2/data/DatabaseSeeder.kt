package com.nnoidea.fitnez2.data

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseSeeder(
    private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    lateinit var database: AppDatabase

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("DatabaseSeeder", "Database created for the first time. Seeding...")
        applicationScope.launch(Dispatchers.IO) {
            populateDatabase(database)
        }
    }

    private suspend fun populateDatabase(database: AppDatabase) {
        val exerciseDao = database.exerciseDao()
        val recordDao = database.recordDao()

        val initialExercises = listOf(
            "Squat",
            "Bench Press",
            "Deadlift",
            "Overhead Press",
            "Barbell Row",
            "Pull Up",
            "Dips"
        )

        initialExercises.forEach { name ->
            exerciseDao.insertExercise(Exercise(name = name))
        }

        val exercises = exerciseDao.getAllExercises()
        if (exercises.isEmpty()) return

        val now = System.currentTimeMillis()
        val oneHour = 3600000L
        val oneDay = 86400000L
        val oneWeek = 604800000L

        val timePoints = listOf(
            now - oneHour,
            now - oneDay,
            now - (2 * oneDay),
            now - oneWeek
        )

        timePoints.forEach { timestamp ->
            repeat(5) { i ->
                val exercise = exercises[i % exercises.size]
                recordDao.insertRecord(
                    Record(
                        exerciseId = exercise.id,
                        date = timestamp,
                        weight = 20.0 + (i * 2.5),
                        sets = 1,
                        reps = 5 + i
                    )
                )
            }
        }

        com.nnoidea.fitnez2.ui.common.GlobalUiState.emitToAll(
            com.nnoidea.fitnez2.ui.common.UiSignal.DatabaseSeeded
        )

        Log.d("DatabaseSeeder", "Seeding complete.")
    }
}
