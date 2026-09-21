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
class RecordServiceTest {

    private lateinit var database: AppDatabase
    private lateinit var exerciseService: ExerciseService
    private lateinit var recordService: RecordService
    private lateinit var testExerciseId: String

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseService = ExerciseService(database)
        recordService = RecordService(database)

        runBlocking {
            val ex = exerciseService.createExercise("Squat")
            testExerciseId = ex.id
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createRecord_validParams_createsAndIncrementsOrderNumber() = runBlocking {
        val now = 1000000L
        val r1 = recordService.createRecord(testExerciseId, sets = 3, reps = 10, weight = 100.0, date = now)
        assertEquals(0, r1.orderNumber)

        val r2 = recordService.createRecord(testExerciseId, sets = 3, reps = 8, weight = 105.0, date = now)
        assertEquals(1, r2.orderNumber)

        val retrieved = recordService.getRecordById(r1.id)
        assertNotNull(retrieved)
        assertEquals(100.0, retrieved!!.weight, 0.001)
    }

    @Test
    fun createRecord_invalidValues_throwsIllegalArgumentException() = runBlocking {
        try {
            recordService.createRecord(testExerciseId, sets = 0, reps = 10, weight = 50.0, date = System.currentTimeMillis())
            fail("Expected exception for sets = 0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }

        try {
            recordService.createRecord(testExerciseId, sets = 3, reps = -1, weight = 50.0, date = System.currentTimeMillis())
            fail("Expected exception for negative reps")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun getRecordsByExerciseId_andFlow_returnsFilteredRecords() = runBlocking {
        val ex2 = exerciseService.createExercise("Bench Press")
        val now = System.currentTimeMillis()

        recordService.createRecord(testExerciseId, 3, 10, 100.0, now)
        recordService.createRecord(ex2.id, 4, 8, 80.0, now)

        val squatRecords = recordService.getRecordsByExerciseId(testExerciseId)
        assertEquals(1, squatRecords.size)
        assertEquals(testExerciseId, squatRecords[0].exerciseId)

        val flowRecords = recordService.getRecordsByExerciseIdFlow(testExerciseId).first()
        assertEquals(1, flowRecords.size)

        val multiRecords = recordService.getRecordsByExerciseIds(listOf(testExerciseId, ex2.id))
        assertEquals(2, multiRecords.size)
    }

    @Test
    fun getLatestRecord_returnsRecordWithExerciseModel() = runBlocking {
        val now = System.currentTimeMillis()
        recordService.createRecord(testExerciseId, 3, 10, 100.0, now)

        val latest = recordService.getLatestRecord()
        assertNotNull(latest)
        assertEquals("Squat", latest?.exerciseName)
        assertEquals(100.0, latest?.record?.weight ?: 0.0, 0.001)

        val latestByExercise = recordService.getLatestRecordByExerciseId(testExerciseId)
        assertEquals("Squat", latestByExercise?.exerciseName)
    }

    @Test
    fun recordCounts_singleAndMultipleExercises() = runBlocking {
        val ex2 = exerciseService.createExercise("Deadlift")
        val now = System.currentTimeMillis()

        recordService.createRecord(testExerciseId, 3, 10, 100.0, now)
        recordService.createRecord(testExerciseId, 3, 10, 100.0, now + 100)
        recordService.createRecord(ex2.id, 1, 5, 140.0, now + 200)

        assertEquals(3, recordService.getTotalRecordCount())
        assertEquals(2, recordService.getRecordCountByExerciseId(testExerciseId))
        assertEquals(1, recordService.getRecordCountByExerciseId(ex2.id))
        assertEquals(3, recordService.getRecordCountByExerciseIds(listOf(testExerciseId, ex2.id)))
        assertEquals(3, recordService.getRecordCountFlow().first())
    }

    @Test
    fun dateRangeQueries_getRecordsByDateRangeFlow() = runBlocking {
        val t1 = 10000L
        val t2 = 20000L
        val t3 = 30000L

        recordService.createRecord(testExerciseId, 3, 10, 60.0, t1)
        recordService.createRecord(testExerciseId, 3, 10, 70.0, t2)
        recordService.createRecord(testExerciseId, 3, 10, 80.0, t3)

        val rangeList = recordService.getRecordsByDateRangeFlow(15000L, 35000L).first()
        assertEquals(2, rangeList.size)

        val aroundList = recordService.getRecordsAroundDate(15000L, 35000L)
        assertEquals(2, aroundList.size)
    }

    @Test
    fun updateRecord_modifiesExistingRecord() = runBlocking {
        val rec = recordService.createRecord(testExerciseId, 3, 10, 100.0, System.currentTimeMillis())
        val updated = recordService.updateRecord(rec.id, sets = 4, reps = 12, weight = 105.0)

        assertEquals(4, updated.sets)
        assertEquals(12, updated.reps)
        assertEquals(105.0, updated.weight, 0.001)

        val retrieved = recordService.getRecordById(rec.id)
        assertEquals(4, retrieved?.sets)
    }

    @Test
    fun updateRecord_nonExistent_throwsException() = runBlocking {
        try {
            recordService.updateRecord("missing_id", 3, 10, 50.0)
            fail("Expected exception for updating non-existent record")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun deleteAndRestoreRecord_restoresExactRecordData() = runBlocking {
        val rec = recordService.createRecord(testExerciseId, 3, 10, 100.0, 50000L)
        val snapshot = recordService.getRecordById(rec.id)!!

        recordService.deleteRecord(rec.id)
        assertNull(recordService.getRecordById(rec.id))
        assertEquals(0, recordService.getTotalRecordCount())

        recordService.restoreRecord(snapshot)
        val restored = recordService.getRecordById(rec.id)
        assertNotNull(restored)
        assertEquals(rec.id, restored?.id)
        assertEquals(rec.orderNumber, restored?.orderNumber)
        assertEquals(100.0, restored?.weight ?: 0.0, 0.001)
    }

    @Test
    fun deleteRecord_nonExistent_throwsException() = runBlocking {
        try {
            recordService.deleteRecord("invalid_id")
            fail("Expected exception for deleting non-existent record")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun pagination_getOlderRecordsAndNewerRecords() = runBlocking {
        val date = 1000L
        val r1 = recordService.createRecord(testExerciseId, 1, 5, 50.0, date)
        val r2 = recordService.createRecord(testExerciseId, 1, 5, 60.0, date)
        val r3 = recordService.createRecord(testExerciseId, 1, 5, 70.0, date)

        val older = recordService.getOlderRecords(offset = 0, limit = 2)
        assertEquals(2, older.size)

        val olderAfter = recordService.getOlderRecordsAfter(date = r3.date, orderNumber = r3.orderNumber, id = r3.id, limit = 10)
        assertEquals(2, olderAfter.size)

        val newerBefore = recordService.getNewerRecordsBefore(date = r1.date, orderNumber = r1.orderNumber, id = r1.id, limit = 10)
        assertEquals(2, newerBefore.size)
    }
}
