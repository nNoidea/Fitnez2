package com.nnoidea.fitnez2.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var recordDao: RecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        exerciseDao = db.exerciseDao()
        recordDao = db.recordDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // ============================================================================================
    // EXERCISE UNICITY & VALIDATION TESTS
    // ============================================================================================

    @Test
    fun exerciseCannotHaveDuplicateName() = runTest {
        val baseName = "Bench Press"
        exerciseDao.create(Exercise(name = baseName))

        // 1. Exact same name
        try {
            exerciseDao.create(Exercise(name = baseName))
            fail("Should have thrown exception for exact duplicate name")
        } catch (e: IllegalArgumentException) {
            assertEquals(LocalizationManager.strings.errorExerciseAlreadyExists(baseName), e.message)
        }

        // 2. Different capitalization
        val casingVariants = listOf("bench press", "BENCH PRESS", "BeNcH PrEsS")
        for (variant in casingVariants) {
            try {
                exerciseDao.create(Exercise(name = variant))
                fail("Should have thrown exception for casing duplicate: '$variant'")
            } catch (e: IllegalArgumentException) {
                assertTrue("Expected duplicate error for '$variant'", e.message!!.contains("already exists"))
            }
        }

        // 3. Whitespaces (leading/trailing)
        val whitespaceVariants = listOf(" $baseName", "$baseName ", "  $baseName  ")
        for (variant in whitespaceVariants) {
            try {
                exerciseDao.create(Exercise(name = variant))
                fail("Should have thrown exception for whitespace duplicate: '$variant'")
            } catch (e: IllegalArgumentException) {
                assertTrue("Expected duplicate error for '$variant'", e.message!!.contains("already exists"))
            }
        }
    }

    @Test
    fun exerciseNameIsTrimmedWhenSaved() = runTest {
        // Input has whitespace
        exerciseDao.create(Exercise(name = "  Pull Up  "))

        // Verify it was saved without whitespace
        val saved = exerciseDao.getAllExercises().first()
        assertEquals("Pull Up", saved.name)
    }

    @Test
    fun exerciseUpdateAllowsCaseChangeOnSameIdButPreventsDuplicateOnOtherId() = runTest {
        exerciseDao.create(Exercise(name = "Push Up"))
        val pushUp = exerciseDao.getAllExercises()[0]
        
        // 1. Changing case of itself should work
        exerciseDao.update(pushUp.copy(name = "PUSH UP"))
        assertEquals("PUSH UP", exerciseDao.getExerciseById(pushUp.id)!!.name)
        
        // 2. Renaming another exercise to an existing name (case-insensitive) should fail
        exerciseDao.create(Exercise(name = "Pull Up"))
        val pullUp = exerciseDao.getAllExercises().find { it.name == "Pull Up" }!!
        
        try {
            exerciseDao.update(pullUp.copy(name = "push up"))
            fail("Should have prevented renaming to an existing exercise name (case-insensitive)")
        } catch (e: IllegalArgumentException) {
            assertEquals(LocalizationManager.strings.errorExerciseRenameConflict("push up"), e.message)
        }
    }

    @Test
    fun exerciseCannotHaveEmptyOrBlankName() = runTest {
        val blanks = listOf("", " ", "   ")
        for (blank in blanks) {
            try {
                exerciseDao.create(Exercise(name = blank))
                fail("Should have thrown exception for blank name: '$blank'")
            } catch (e: IllegalArgumentException) {
                assertEquals(LocalizationManager.strings.errorExerciseNameBlank, e.message)
            }
        }
    }

    // ============================================================================================
    // RECORD & RELATIONSHIP TESTS
    // ============================================================================================

    @Test
    fun recordSortingAndDeletion() = runTest {
        // 1. Setup Exercise
        exerciseDao.create(Exercise(name = "Squat"))
        val exerciseId = exerciseDao.getAllExercises()[0].id

        // 2. Create Records with intentional sorting triggers
        // We want to test Date DESC, then ID DESC
        val now = 10000L
        
        // Older
        recordDao.create(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 10.0, date = now - 1000L))
        // Newer (Base)
        recordDao.create(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 20.0, date = now))
        // Same Time as Base, but created later (Higher ID) -> Should be first in list (Stable Sort)
        recordDao.create(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 30.0, date = now))

        val sorted = recordDao.getSortedOne(exerciseId)
        
        assertEquals(3, sorted.size)
        
        // Top one should be the one with weight 30 (Same date as 20, but higher ID)
        assertEquals(30.0, sorted[0].record.weight, 0.1)
        assertEquals(20.0, sorted[1].record.weight, 0.1)
        assertEquals(10.0, sorted[2].record.weight, 0.1)
        
        // Verify JOIN worked
        assertEquals("Squat", sorted[0].exerciseName)

        // 3. Verify Hard Delete Cascading
        exerciseDao.delete(exerciseId)
        
        assertEquals(0, exerciseDao.getAllExercises().size)
        // Records should be gone from everywhere
        assertEquals(0, recordDao.getSortedOne(exerciseId).size)
    }
}
