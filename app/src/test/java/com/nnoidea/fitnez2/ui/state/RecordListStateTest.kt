package com.nnoidea.fitnez2.ui.state

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.screens.timeline.RecordListStateImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import com.nnoidea.fitnez2.ui.components.recordlist.RecordDisplayItem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecordListStateTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var recordService: RecordService
    private lateinit var exerciseService: ExerciseService
    private lateinit var settingsService: SettingsService
    private lateinit var globalUiState: GlobalUiState
    private lateinit var testScope: CoroutineScope

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recordService = RecordService(database)
        exerciseService = ExerciseService(database)
        settingsService = SettingsService(context)
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        globalUiState = GlobalUiState(scope = testScope, settingsService = settingsService)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        database.close()
    }

    private fun createRecordListState(filterExerciseIds: List<String>? = null): RecordListStateImpl {
        return RecordListStateImpl(
            scope = testScope,
            recordService = recordService,
            globalUiState = globalUiState,
            onHapticFeedback = {},
            filterExerciseIds = filterExerciseIds
        )
    }

    @Test
    fun loadInitial_populatesUiItemsWithRecords() = runBlocking {
        val squat = exerciseService.createExercise("Squat")
        recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 100.0, date = 1000L)
        recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 105.0, date = 2000L)

        val state = createRecordListState()
        state.updateExerciseMap(mapOf(squat.id to squat.name))
        state.loadInitial()

        withTimeout(3000) {
            while (state.uiItems.isEmpty()) {
                delay(20)
            }
        }

        assertTrue("uiItems should not be empty after initial load", state.uiItems.isNotEmpty())
    }

    @Test
    fun showTimestampFor_updatesTimestampTokenMap() = runBlocking {
        val state = createRecordListState()
        val recordId = "rec_123"

        state.showTimestampFor(recordId)
        assertNotNull(state.timestampTokens[recordId])
    }

    @Test
    fun onDeleteRequest_deletesRecordFromService() = runBlocking {
        val squat = exerciseService.createExercise("Squat")
        val rec = recordService.createRecord(squat.id, 3, 10, 100.0, 1000L)

        val state = createRecordListState()
        state.onDeleteRequest(rec)

        withTimeout(3000) {
            while (recordService.getTotalRecordCount() > 0) {
                delay(20)
            }
        }

        assertEquals(0, recordService.getTotalRecordCount())
    }

    @Test
    fun onUpdateRequest_updatesRecordInService() = runBlocking {
        val squat = exerciseService.createExercise("Squat")
        val rec = recordService.createRecord(squat.id, 3, 10, 100.0, 1000L)

        val state = createRecordListState()
        val updated = rec.copy(sets = 5, reps = 15, weight = 110.0)
        state.onUpdateRequest(updated)

        withTimeout(3000) {
            while (recordService.getRecordById(rec.id)?.sets != 5) {
                delay(20)
            }
        }

        val fetched = recordService.getRecordById(rec.id)
        assertEquals(5, fetched?.sets)
        assertEquals(15, fetched?.reps)
        assertEquals(110.0, fetched?.weight ?: 0.0, 0.001)
    }

    @Test
    fun updateExerciseMap_whenExerciseDeleted_immediatelyRemovesItsRecords() = runBlocking {
        val squat = exerciseService.createExercise("Squat")
        val bench = exerciseService.createExercise("Bench")
        recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 100.0, date = 1000L)
        recordService.createRecord(bench.id, sets = 3, reps = 10, weight = 80.0, date = 2000L)

        val state = createRecordListState()
        state.updateExerciseMap(mapOf(squat.id to squat.name, bench.id to bench.name))
        state.loadInitial()

        withTimeout(3000) {
            while (state.uiItems.size < 3) {
                delay(20)
            }
        }

        // Delete squat: database cascades deletion, and exerciseMap updates without squat
        exerciseService.deleteExercise(squat.id)
        state.updateExerciseMap(mapOf(bench.id to bench.name))

        // uiItems must immediately exclude squat records without reloading/restarting
        val recordGroups = state.uiItems.filterIsInstance<RecordDisplayItem.RecordGroup>()
        val hasSquat = recordGroups.any { group -> group.records.any { it.record.exerciseId == squat.id } }
        assertFalse("uiItems must immediately exclude deleted exercise records", hasSquat)
    }
}
