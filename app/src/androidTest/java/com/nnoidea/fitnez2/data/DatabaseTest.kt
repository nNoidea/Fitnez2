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
        exerciseDao.insertExercise(Exercise(name = baseName))

        // 1. Exact same name
        try {
            exerciseDao.insertExercise(Exercise(name = baseName))
            fail("Should have thrown exception for exact duplicate name")
        } catch (e: IllegalArgumentException) {
            assertEquals(LocalizationManager.strings.errorExerciseAlreadyExists(baseName), e.message)
        }

        // 2. Different capitalization
        val casingVariants = listOf("bench press", "BENCH PRESS", "BeNcH PrEsS")
        for (variant in casingVariants) {
            try {
                exerciseDao.insertExercise(Exercise(name = variant))
                fail("Should have thrown exception for casing duplicate: '$variant'")
            } catch (e: IllegalArgumentException) {
                assertTrue("Expected duplicate error for '$variant'", e.message!!.contains("already exists"))
            }
        }

        // 3. Whitespaces (leading/trailing)
        val whitespaceVariants = listOf(" $baseName", "$baseName ", "  $baseName  ")
        for (variant in whitespaceVariants) {
            try {
                exerciseDao.insertExercise(Exercise(name = variant))
                fail("Should have thrown exception for whitespace duplicate: '$variant'")
            } catch (e: IllegalArgumentException) {
                assertTrue("Expected duplicate error for '$variant'", e.message!!.contains("already exists"))
            }
        }
    }

    @Test
    fun exerciseNameIsTrimmedWhenSaved() = runTest {
        // Input has whitespace
        exerciseDao.insertExercise(Exercise(name = "  Pull Up  "))

        // Verify it was saved without whitespace
        val saved = exerciseDao.getAllExercises().first()
        assertEquals("Pull Up", saved.name)
    }

    @Test
    fun exerciseUpdateAllowsCaseChangeOnSameIdButPreventsDuplicateOnOtherId() = runTest {
        exerciseDao.insertExercise(Exercise(name = "Push Up"))
        val pushUp = exerciseDao.getAllExercises()[0]
        
        // 1. Changing case of itself should work
        exerciseDao.updateExercise(pushUp.copy(name = "PUSH UP"))
        assertEquals("PUSH UP", exerciseDao.getExerciseById(pushUp.id)!!.name)
        
        // 2. Renaming another exercise to an existing name (case-insensitive) should fail
        exerciseDao.insertExercise(Exercise(name = "Pull Up"))
        val pullUp = exerciseDao.getAllExercises().find { it.name == "Pull Up" }!!
        
        try {
            exerciseDao.updateExercise(pullUp.copy(name = "push up"))
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
                exerciseDao.insertExercise(Exercise(name = blank))
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
    fun recordSortingAndCascadeDeletion() = runTest {
        // 1. Setup Exercise
        exerciseDao.insertExercise(Exercise(name = "Squat"))
        val exerciseId = exerciseDao.getAllExercises()[0].id

        // 2. Create Records with intentional sorting triggers
        val now = 10000L

        // Older
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 10.0, date = now - 1000L, orderNumber = 0))
        // Newer (Base)
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 20.0, date = now, orderNumber = 0))
        // Same Time as Base, but created later (Higher ID) -> Should be first in DESC list
        recordDao.insertRecord(Record(exerciseId = exerciseId, sets = 1, reps = 5, weight = 30.0, date = now, orderNumber = 0))

        val allRecords = recordDao.getAllRecordsOrdered()
        assertEquals(3, allRecords.size)

        // 3. Verify Hard Delete Cascading
        exerciseDao.deleteExercise(exerciseDao.getExerciseById(exerciseId)!!)

        assertEquals(0, exerciseDao.getAllExercises().size)
        assertEquals(0, recordDao.getAllRecordsOrdered().size)
    }

    // ============================================================================================
    // GROUP INDEX TESTS
    // ============================================================================================

    /**
     * Helper: creates an exercise and returns its ID.
     */
    private suspend fun createExercise(name: String): String {
        exerciseDao.insertExercise(Exercise(name = name))
        return exerciseDao.getAllExercises().first { it.name == name }.id
    }

    /**
     * Helper: creates a record for a given exercise.
     */
    private suspend fun addRecord(exerciseId: String, date: Long): Record {
        val record = Record(
            exerciseId = exerciseId,
            sets = 3,
            reps = 10,
            weight = 50.0,
            date = date,
            orderNumber = 0
        )
        recordDao.insertRecord(record)
        return recordDao.getRecordById(record.id)!!
    }

    /**
     * Helper: gets all records sorted chronologically (ASC).
     */
    private suspend fun allRecordsChronological(): List<Record> {
        return recordDao.getAllRecordsOrdered()
    }

    // --- CREATION ---

    @Test
    fun creation_insertsRecordsCorrectly() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")

        var t = 1000L
        addRecord(ex1, t++)
        addRecord(ex1, t++)
        addRecord(ex2, t++)
        addRecord(ex2, t++)

        val records = allRecordsChronological()
        assertEquals(4, records.size)
        assertEquals(ex1, records[0].exerciseId)
        assertEquals(ex1, records[1].exerciseId)
        assertEquals(ex2, records[2].exerciseId)
        assertEquals(ex2, records[3].exerciseId)
    }

    // --- SINGLE RECORD DELETION ---

    @Test
    fun deleteSingleRecord_removesCorrectRecord() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")

        var t = 1000L
        val r1 = addRecord(ex1, t++)
        addRecord(ex1, t++)
        addRecord(ex2, t++)

        recordDao.deleteRecord(r1)

        val after = allRecordsChronological()
        assertEquals(2, after.size)
        assertEquals(ex1, after[0].exerciseId)
        assertEquals(ex2, after[1].exerciseId)
    }

    @Test
    fun deleteOnlyRecordInGroup_removesIt() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")
        val ex3 = createExercise("Exercise3")

        var t = 1000L
        addRecord(ex1, t++)
        val mid = addRecord(ex2, t++)
        addRecord(ex3, t++)

        recordDao.deleteRecord(mid)

        val after = allRecordsChronological()
        assertEquals(2, after.size)
        assertEquals(ex1, after[0].exerciseId)
        assertEquals(ex3, after[1].exerciseId)
    }

    // --- EXERCISE DELETION (CASCADE) ---

    @Test
    fun exerciseDeletion_cascadeDeletesRecords() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")
        val ex3 = createExercise("Exercise3")

        var t = 1000L
        addRecord(ex1, t++); addRecord(ex1, t++)
        addRecord(ex2, t++); addRecord(ex2, t++)
        addRecord(ex3, t++); addRecord(ex3, t++)

        exerciseDao.deleteExercise(exerciseDao.getExerciseById(ex2)!!)

        val after = allRecordsChronological()
        assertEquals(4, after.size)
        assertTrue(after.none { it.exerciseId == ex2 })
        assertTrue(after.filter { it.exerciseId == ex1 }.size == 2)
        assertTrue(after.filter { it.exerciseId == ex3 }.size == 2)
    }

    // --- UNDO (create with original id & date) ---

    @Test
    fun undoDelete_middleRecord_restoresRecords() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")
        val ex3 = createExercise("Exercise3")

        var t = 1000L
        addRecord(ex1, t++)
        val mid = addRecord(ex2, t++)
        addRecord(ex3, t++)

        val savedForUndo = recordDao.getRecordById(mid.id)!!
        recordDao.deleteRecord(mid)

        assertEquals(2, allRecordsChronological().size)

        recordDao.insertRecord(savedForUndo)

        val afterUndo = allRecordsChronological()
        assertEquals(3, afterUndo.size)
        assertEquals(ex1, afterUndo[0].exerciseId)
        assertEquals(ex2, afterUndo[1].exerciseId)
        assertEquals(ex3, afterUndo[2].exerciseId)
    }

    @Test
    fun undoDelete_lastRecord_restoresIt() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")

        var t = 1000L
        addRecord(ex1, t++)
        val last = addRecord(ex2, t++)

        val savedForUndo = recordDao.getRecordById(last.id)!!
        recordDao.deleteRecord(last)

        assertEquals(1, allRecordsChronological().size)

        recordDao.insertRecord(savedForUndo)

        val afterUndo = allRecordsChronological()
        assertEquals(2, afterUndo.size)
        assertEquals(ex1, afterUndo[0].exerciseId)
        assertEquals(ex2, afterUndo[1].exerciseId)
    }

    @Test
    fun undoDelete_firstRecord_restoresIt() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")

        var t = 1000L
        val first = addRecord(ex1, t++)
        addRecord(ex2, t++)

        val savedForUndo = recordDao.getRecordById(first.id)!!
        recordDao.deleteRecord(first)

        assertEquals(1, allRecordsChronological().size)

        recordDao.insertRecord(savedForUndo)

        val afterUndo = allRecordsChronological()
        assertEquals(2, afterUndo.size)
        assertEquals(ex1, afterUndo[0].exerciseId)
        assertEquals(ex2, afterUndo[1].exerciseId)
    }

    @Test
    fun undoDelete_onlyRecord_restoresFromEmpty() = runTest {
        val ex1 = createExercise("Exercise1")
        val only = addRecord(ex1, 1000L)

        val savedForUndo = recordDao.getRecordById(only.id)!!
        recordDao.deleteRecord(only)

        assertEquals(0, allRecordsChronological().size)

        recordDao.insertRecord(savedForUndo)

        val afterUndo = allRecordsChronological()
        assertEquals(1, afterUndo.size)
        assertEquals(ex1, afterUndo[0].exerciseId)
    }

    @Test
    fun multipleSequentialDeletes_thenUndo() = runTest {
        val ex1 = createExercise("Exercise1")
        val ex2 = createExercise("Exercise2")
        val ex3 = createExercise("Exercise3")

        var t = 1000L
        addRecord(ex1, t++)
        val r2 = addRecord(ex2, t++)
        val r3 = addRecord(ex3, t++)

        recordDao.deleteRecord(r2)

        val savedForUndo = recordDao.getRecordById(r3.id)!!
        recordDao.deleteRecord(r3)

        assertEquals(1, allRecordsChronological().size)

        recordDao.insertRecord(savedForUndo)

        val afterUndo = allRecordsChronological()
        assertEquals(2, afterUndo.size)
        assertEquals(ex1, afterUndo[0].exerciseId)
        assertEquals(ex3, afterUndo[1].exerciseId)
    }
}
