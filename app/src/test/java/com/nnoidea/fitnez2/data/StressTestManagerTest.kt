package com.nnoidea.fitnez2.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StressTestManagerTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun performStressTest_clearsAndSeedsDatabase() = runBlocking {
        val progressUpdates = mutableListOf<Pair<Float, String>>()

        StressTestManager.performStressTest(database) { progress, message ->
            progressUpdates.add(progress to message)
        }

        assertTrue("Should have reported progress", progressUpdates.isNotEmpty())
        assertEquals(1.0f, progressUpdates.last().first, 0.01f)

        val exercises = database.exerciseDao().getAllExercises()
        assertTrue("Exercises should be populated", exercises.isNotEmpty())

        val totalRecords = database.recordDao().getTotalRecordCount()
        assertTrue("Total records should be populated", totalRecords > 0)
    }
}
