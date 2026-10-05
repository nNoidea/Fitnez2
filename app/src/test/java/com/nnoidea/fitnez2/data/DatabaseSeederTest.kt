package com.nnoidea.fitnez2.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.core.TimeUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseSeederTest {

    @Test
    fun firstStartSeed_staysSmallRecentAndProgressing() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var dbRef: AppDatabase? = null
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(DatabaseSeeder(this, { dbRef!! }))
            .build()
        dbRef = db
        // First access triggers onCreate; the seeder populates asynchronously.
        db.exerciseDao().getAllExercises()
        val deadline = System.currentTimeMillis() + 10000
        var exercises = db.exerciseDao().getAllExercises()
        while (exercises.size < 7 && System.currentTimeMillis() < deadline) {
            kotlinx.coroutines.delay(200)
            exercises = db.exerciseDao().getAllExercises()
        }
        val names = exercises.map { it.name }
        assertTrue(names.contains("Squat"))
        assertTrue(names.contains("Bench Press"))

        val squatRecords = db.recordDao()
            .getRecordsByExerciseId(exercises.first { it.name == "Squat" }.id, limit = 100)
            .sortedBy { it.date }
        val benchWeights = db.recordDao()
            .getRecordsByExerciseId(exercises.first { it.name == "Bench Press" }.id, limit = 100)
            .sortedBy { it.date }
            .map { it.weight }
        val total = db.recordDao().getTotalRecordCount()
        val latest = db.recordDao().getLatestRecord()
        db.close()

        // A handful of back-to-back Squat sessions, strictly increasing.
        assertEquals(5, squatRecords.size)
        assertEquals(5, squatRecords.map { TimeUtils.getWorkoutLocalDate(it.date, 0) }.toSet().size)
        val squatWeights = squatRecords.map { it.weight }
        assertEquals(squatWeights.sorted(), squatWeights)
        assertTrue(squatWeights.last() > squatWeights.first())

        // A second lift also climbs back-to-back.
        assertTrue(benchWeights.size >= 2)
        assertEquals(benchWeights.sorted(), benchWeights)
        assertTrue(benchWeights.last() > benchWeights.first())

        // Small and recent: easy to wipe, nothing older than a week.
        assertEquals(10, total)
        assertTrue(squatRecords.all { it.date > System.currentTimeMillis() - 7 * 86400000L })

        // Today's Squat is the latest record so the graph opens on it.
        assertEquals("Squat", latest?.exerciseName)
    }
}
