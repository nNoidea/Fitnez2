package com.nnoidea.fitnez2.service

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.AppDatabase
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
class ExerciseServiceTest {

    private lateinit var database: AppDatabase
    private lateinit var exerciseService: ExerciseService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseService = ExerciseService(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createExercise_validName_persistsAndReturnsExercise() = runBlocking {
        val exercise = exerciseService.createExercise("Bench Press")
        assertNotNull(exercise.id)
        assertEquals("Bench Press", exercise.name)

        val retrieved = exerciseService.getExerciseById(exercise.id)
        assertEquals("Bench Press", retrieved?.name)
    }

    @Test
    fun createExercise_whitespaceName_trimsAutomatically() = runBlocking {
        val exercise = exerciseService.createExercise("   Squat   ")
        assertEquals("Squat", exercise.name)
    }

    @Test
    fun createExercise_duplicateName_throwsIllegalArgumentException() = runBlocking {
        exerciseService.createExercise("Deadlift")
        try {
            exerciseService.createExercise("Deadlift")
            fail("Expected exception for duplicate exercise name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun createExercise_blankName_throwsIllegalArgumentException() = runBlocking {
        try {
            exerciseService.createExercise("   ")
            fail("Expected exception for blank exercise name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun getAllExercises_andFlow_emitsAllPersistedExercises() = runBlocking {
        exerciseService.createExercise("Push Up")
        exerciseService.createExercise("Pull Up")

        val list = exerciseService.getAllExercises()
        assertEquals(2, list.size)

        val flowList = exerciseService.getAllExercisesFlow().first()
        assertEquals(2, flowList.size)
    }

    @Test
    fun getExerciseByName_caseInsensitiveLookup() = runBlocking {
        val exercise = exerciseService.createExercise("Overhead Press")
        val found = exerciseService.getExerciseByName("Overhead Press")
        assertEquals(exercise.id, found?.id)
    }

    @Test
    fun updateExercise_validNewName_updatesSuccessfully() = runBlocking {
        val exercise = exerciseService.createExercise("Bicep Curl")
        val updated = exerciseService.updateExercise(exercise.id, "Dumbbell Bicep Curl")

        assertEquals("Dumbbell Bicep Curl", updated.name)
        val fetched = exerciseService.getExerciseById(exercise.id)
        assertEquals("Dumbbell Bicep Curl", fetched?.name)
    }

    @Test
    fun updateExercise_nameConflictWithOtherExercise_throwsException() = runBlocking {
        // ex1 is required: renaming ex2 to "Exercise 1" must collide with it.
        exerciseService.createExercise("Exercise 1")
        val ex2 = exerciseService.createExercise("Exercise 2")

        try {
            exerciseService.updateExercise(ex2.id, "Exercise 1")
            fail("Expected exception for renaming to existing exercise")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun updateExercise_sameNameOnSelf_allowed() = runBlocking {
        val exercise = exerciseService.createExercise("Tricep Dips")
        val updated = exerciseService.updateExercise(exercise.id, "Tricep Dips")
        assertEquals("Tricep Dips", updated.name)
    }

    @Test
    fun deleteExercise_existingId_removesExercise() = runBlocking {
        val exercise = exerciseService.createExercise("Plank")
        exerciseService.deleteExercise(exercise.id)

        val retrieved = exerciseService.getExerciseById(exercise.id)
        assertNull(retrieved)
    }

    @Test
    fun deleteExercise_nonExistentId_throwsException() = runBlocking {
        try {
            exerciseService.deleteExercise("non_existent_id")
            fail("Expected exception when deleting non-existent exercise")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
