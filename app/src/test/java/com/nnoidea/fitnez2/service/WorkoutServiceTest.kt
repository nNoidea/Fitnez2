package com.nnoidea.fitnez2.service

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Workout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkoutServiceTest {

    private lateinit var database: AppDatabase
    private lateinit var workoutService: WorkoutService
    private lateinit var exerciseService: ExerciseService
    private lateinit var exerciseId: String

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutService = WorkoutService(database)
        exerciseService = ExerciseService(database)

        runBlocking {
            val ex = exerciseService.createExercise("Bench Press")
            exerciseId = ex.id
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createWorkout_validName_persistsSuccessfully() = runBlocking {
        val workout = workoutService.createWorkout("Push Day A")
        assertNotNull(workout.id)
        assertEquals("Push Day A", workout.name)

        val retrieved = workoutService.getWorkoutById(workout.id)
        assertEquals("Push Day A", retrieved?.name)
    }

    @Test
    fun createWorkout_duplicateName_throwsIllegalArgumentException() = runBlocking {
        workoutService.createWorkout("Leg Day")
        try {
            workoutService.createWorkout("Leg Day")
            fail("Expected exception for duplicate workout name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun addRecordToWorkout_andGetRecordsForWorkout() = runBlocking {
        val workout = workoutService.createWorkout("Full Body")
        val wr = workoutService.addRecordToWorkout(workout.id, exerciseId, sets = 4, reps = 8, weight = 80.0)

        assertEquals(workout.id, wr.workoutId)
        assertEquals(exerciseId, wr.exerciseId)
        assertEquals(4, wr.sets)

        val recordsList = workoutService.getRecordsForWorkout(workout.id)
        assertEquals(1, recordsList.size)
        assertEquals("Bench Press", recordsList[0].exerciseName)

        val flowRecords = workoutService.getRecordsForWorkoutFlow(workout.id).first()
        assertEquals(1, flowRecords.size)
    }

    @Test
    fun updateWorkoutRecord_andDeleteWorkoutRecord() = runBlocking {
        val workout = workoutService.createWorkout("Chest Focus")
        val wr = workoutService.addRecordToWorkout(workout.id, exerciseId, sets = 3, reps = 10, weight = 70.0)

        val modified = wr.copy(weight = 75.0, reps = 12)
        workoutService.updateWorkoutRecord(modified)

        val updatedList = workoutService.getRecordsForWorkout(workout.id)
        assertEquals(75.0, updatedList[0].workoutRecord.weight, 0.001)
        assertEquals(12, updatedList[0].workoutRecord.reps)

        workoutService.deleteWorkoutRecord(modified)
        val afterDelete = workoutService.getRecordsForWorkout(workout.id)
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun updateWorkout_andDeleteWorkout() = runBlocking {
        val workout = workoutService.createWorkout("Upper Body")
        workoutService.updateWorkout(workout.copy(name = "Upper Body Heavy"))

        val retrieved = workoutService.getWorkoutByName("Upper Body Heavy")
        assertNotNull(retrieved)

        workoutService.deleteWorkout(retrieved!!)
        assertNull(workoutService.getWorkoutById(workout.id))
    }

    @Test
    fun updateWorkout_nameWithSurroundingWhitespace_persistsTrimmedName() = runBlocking {
        val workout = workoutService.createWorkout("Leg Day")
        workoutService.updateWorkout(workout.copy(name = "  Leg Day Hard  "))

        val retrieved = workoutService.getWorkoutById(workout.id)
        assertEquals("Leg Day Hard", retrieved?.name)
    }

    @Test
    fun updateWorkout_blankName_throws() = runBlocking {
        val workout = workoutService.createWorkout("Pull Day")
        try {
            workoutService.updateWorkout(workout.copy(name = "   "))
            fail("Expected IllegalArgumentException for blank name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun deleteAllWorkouts_clearsAll() = runBlocking {
        workoutService.createWorkout("Workout 1")
        workoutService.createWorkout("Workout 2")
        assertEquals(2, workoutService.getAllWorkouts().size)

        workoutService.deleteAllWorkouts()
        assertEquals(0, workoutService.getAllWorkouts().size)
    }
}
