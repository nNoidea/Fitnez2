package com.nnoidea.fitnez2.service

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupServiceTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupService: BackupService
    private lateinit var exerciseService: ExerciseService
    private lateinit var recordService: RecordService
    private lateinit var workoutService: WorkoutService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupService = BackupService(context, database)
        exerciseService = ExerciseService(database)
        recordService = RecordService(database)
        workoutService = WorkoutService(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportAndImport_roundTripRestoresAllData() {
        runBlocking {
            // 1. Seed data
            val squat = exerciseService.createExercise("Squat")
            val bench = exerciseService.createExercise("Bench Press")
            recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 100.0, date = 1000L)
            recordService.createRecord(bench.id, sets = 4, reps = 8, weight = 80.0, date = 2000L)

            val workout = workoutService.createWorkout("Strength Day")
            workoutService.addRecordToWorkout(workout.id, squat.id, sets = 3, reps = 10, weight = 100.0)

            // 2. Export to temporary file
            val tempFile = File(context.cacheDir, "test_backup.json")
            val uri = Uri.fromFile(tempFile)

            val exportResult = backupService.exportData(uri)
            assertTrue("Export should succeed", exportResult.isSuccess)
            assertTrue("Backup file must exist", tempFile.exists())
            assertTrue("Backup file must not be empty", tempFile.length() > 0)

            // 3. Clear database
            database.recordDao().deleteAllRecords()
            database.exerciseDao().deleteAllExercises()
            database.workoutDao().deleteAllWorkouts()

            assertEquals(0, exerciseService.getAllExercises().size)
            assertEquals(0, recordService.getTotalRecordCount())
            assertEquals(0, workoutService.getAllWorkouts().size)

            // 4. Import from file
            val importResult = backupService.importData(uri)
            assertTrue("Import should succeed", importResult.isSuccess)

            // 5. Verify restoration
            val restoredExercises = exerciseService.getAllExercises()
            assertEquals(2, restoredExercises.size)
            assertTrue(restoredExercises.any { it.name == "Squat" })
            assertTrue(restoredExercises.any { it.name == "Bench Press" })

            assertEquals(2, recordService.getTotalRecordCount())
            val restoredWorkouts = workoutService.getAllWorkouts()
            assertEquals(1, restoredWorkouts.size)
            assertEquals("Strength Day", restoredWorkouts[0].name)

            tempFile.delete()
        }
    }

    @Test
    fun importData_invalidJson_returnsFailure() {
        runBlocking {
            val corruptedFile = File(context.cacheDir, "corrupted.json")
            corruptedFile.writeText("INVALID NOT JSON DATA")
            val uri = Uri.fromFile(corruptedFile)

            val result = backupService.importData(uri)
            assertTrue("Import on invalid data should fail", result.isFailure)

            corruptedFile.delete()
        }
    }

    @Test
    fun importData_legacyMinifiedFormat_importsSuccessfully() {
        runBlocking {
            // Legacy JSON format structure: {"a":[{"a":"Deadlift","b":[{"a":3,"b":5,"c":120.0,"d":5000}]}]}
            val legacyJson = """
                {
                    "a": [
                        {
                            "a": "Deadlift",
                            "b": [
                                {"a": 3, "b": 5, "c": 120.0, "d": 5000}
                            ]
                        }
                    ]
                }
            """.trimIndent()

            val legacyFile = File(context.cacheDir, "legacy.json")
            legacyFile.writeText(legacyJson)
            val uri = Uri.fromFile(legacyFile)

            val result = backupService.importData(uri)
            assertTrue("Legacy import should succeed", result.isSuccess)

            val exercises = exerciseService.getAllExercises()
            assertEquals(1, exercises.size)
            assertEquals("Deadlift", exercises[0].name)
            assertEquals(1, recordService.getTotalRecordCount())

            legacyFile.delete()
        }
    }
}
