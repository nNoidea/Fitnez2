package com.nnoidea.fitnez2.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.dao.WorkoutDao
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var recordDao: RecordDao
    private lateinit var workoutDao: WorkoutDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = db.exerciseDao()
        recordDao = db.recordDao()
        workoutDao = db.workoutDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun exerciseDao_insertAndQueryByIdAndName() = runBlocking {
        val exercise = Exercise(name = "Bench Press")
        exerciseDao.insertExercise(exercise)

        val retrievedById = exerciseDao.getExerciseById(exercise.id)
        assertNotNull(retrievedById)
        assertEquals("Bench Press", retrievedById?.name)

        // Case-insensitive query
        val retrievedByNameLower = exerciseDao.getExerciseByName("bench press")
        assertNotNull(retrievedByNameLower)
        assertEquals(exercise.id, retrievedByNameLower?.id)

        val retrievedByNameUpper = exerciseDao.getExerciseByName("BENCH PRESS")
        assertNotNull(retrievedByNameUpper)
        assertEquals(exercise.id, retrievedByNameUpper?.id)
    }

    @Test
    fun exerciseDao_getAllExercisesOrderedAlphabetically() = runBlocking {
        exerciseDao.insertExercise(Exercise(name = "Squat"))
        exerciseDao.insertExercise(Exercise(name = "Bench Press"))
        exerciseDao.insertExercise(Exercise(name = "Deadlift"))

        val list = exerciseDao.getAllExercises()
        assertEquals(3, list.size)
        assertEquals("Bench Press", list[0].name)
        assertEquals("Deadlift", list[1].name)
        assertEquals("Squat", list[2].name)
    }

    @Test
    fun exerciseDao_updateExercise() = runBlocking {
        val exercise = Exercise(name = "Push Up")
        exerciseDao.insertExercise(exercise)

        val modified = exercise.copy(name = "Weighted Push Up")
        exerciseDao.updateExercise(modified)

        val retrieved = exerciseDao.getExerciseById(exercise.id)
        assertEquals("Weighted Push Up", retrieved?.name)
    }

    @Test
    fun exerciseDao_deleteExercise() = runBlocking {
        val exercise = Exercise(name = "Pull Up")
        exerciseDao.insertExercise(exercise)

        exerciseDao.deleteExercise(exercise)
        assertNull(exerciseDao.getExerciseById(exercise.id))
    }

    @Test
    fun recordSortingAndCascadeDeletion() = runBlocking {
        exerciseDao.insertExercise(Exercise(name = "Squat"))
        val exerciseId = exerciseDao.getAllExercises()[0].id

        val now = 10000L
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 10.0, date = now - 1000L, orderNumber = 0))
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 20.0, date = now, orderNumber = 0))
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 30.0, date = now, orderNumber = 0))

        val allRecords = recordDao.getAllRecordsOrdered()
        assertEquals(3, allRecords.size)

        exerciseDao.deleteExercise(exerciseDao.getExerciseById(exerciseId)!!)
        assertEquals(0, exerciseDao.getAllExercises().size)
        assertEquals(0, recordDao.getAllRecordsOrdered().size)
    }

    @Test
    fun workoutAndWorkoutRecordOperations() = runBlocking {
        exerciseDao.insertExercise(Exercise(name = "Squat"))
        val exId = exerciseDao.getAllExercises()[0].id

        val workout = Workout(name = "Leg Routine")
        workoutDao.insertWorkout(workout)
        assertNotNull(workoutDao.getWorkoutById(workout.id))

        val wr1 = WorkoutRecord(workoutId = workout.id, exerciseId = exId, sets = 3, reps = 10, weight = 100.0)
        val wr2 = WorkoutRecord(workoutId = workout.id, exerciseId = exId, sets = 3, reps = 8, weight = 110.0)
        workoutDao.insertWorkoutRecord(wr1)
        workoutDao.insertWorkoutRecord(wr2)

        val workoutRecords = workoutDao.getRecordsForWorkout(workout.id)
        assertEquals(2, workoutRecords.size)

        workoutDao.deleteRecordsByWorkoutId(workout.id)
        assertEquals(0, workoutDao.getRecordsForWorkout(workout.id).size)

        workoutDao.deleteWorkout(workout)
        assertNull(workoutDao.getWorkoutById(workout.id))
    }
}
