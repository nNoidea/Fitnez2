package com.nnoidea.fitnez2.data

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DatabaseSeeder(
    private val applicationScope: CoroutineScope,
    private val databaseProvider: () -> AppDatabase
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("DatabaseSeeder", "Database created for the first time. Seeding...")
        applicationScope.launch(Dispatchers.IO) {
            try {
                populateDatabase(databaseProvider())
            } catch (e: Exception) {
                Log.e("DatabaseSeeder", "Error populating database", e)
            }
        }
    }

    private suspend fun populateDatabase(database: AppDatabase) {
        val exerciseDao = database.exerciseDao()
        val recordDao = database.recordDao()
        val now = System.currentTimeMillis()

        val starterLifts = listOf(
            "Squat",
            "Bench Press",
            "Deadlift",
            "Overhead Press",
            "Barbell Row",
            "Pull Up",
            "Dips"
        )
        starterLifts.forEach { name ->
            exerciseDao.insertExercise(Exercise(name = name))
        }
        val idsByName = exerciseDao.getAllExercises().associateBy({ it.name }, { it.id })

        suspend fun log(exercise: String, daysAgo: Int, sets: Int, reps: Int, weight: Double, order: Int = 0) {
            recordDao.insertRecord(
                Record(
                    exerciseId = idsByName.getValue(exercise),
                    sets = sets,
                    reps = reps,
                    weight = weight,
                    date = trainingDayTimestamp(daysAgo, now),
                    orderNumber = order
                )
            )
        }

        // A week of Squat, heavier back-to-back so the graph climbs.
        log("Squat", daysAgo = 4, sets = 3, reps = 5, weight = 60.0)
        log("Squat", daysAgo = 3, sets = 3, reps = 5, weight = 62.5)
        log("Squat", daysAgo = 2, sets = 3, reps = 5, weight = 65.0)
        log("Squat", daysAgo = 1, sets = 3, reps = 5, weight = 67.5)
        // Today's Squat shares its timestamp with other lifts; pin it as the
        // latest record so the graph opens on the main lift.
        log("Squat", daysAgo = 0, sets = 3, reps = 5, weight = 70.0, order = 1)
        // Bench climbs back-to-back too.
        log("Bench Press", daysAgo = 2, sets = 3, reps = 8, weight = 40.0)
        log("Bench Press", daysAgo = 0, sets = 3, reps = 8, weight = 42.5)
        // A few other lifts so the timeline looks lived-in. Ten records total,
        // all within the last week, so they are easy to wipe.
        log("Deadlift", daysAgo = 1, sets = 3, reps = 5, weight = 100.0)
        log("Overhead Press", daysAgo = 0, sets = 3, reps = 8, weight = 30.0)
        log("Barbell Row", daysAgo = 3, sets = 3, reps = 8, weight = 50.0)

        com.nnoidea.fitnez2.ui.common.GlobalUiState.emitToAll(
            com.nnoidea.fitnez2.ui.common.UiSignal.DatabaseSeeded
        )

        Log.d("DatabaseSeeder", "Seeding complete.")
    }
}

/** Stable timestamp for a training day N days ago: midday, safely clear of the night-mode rollover hour. */
fun trainingDayTimestamp(daysAgo: Int, nowMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = nowMillis
    calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
