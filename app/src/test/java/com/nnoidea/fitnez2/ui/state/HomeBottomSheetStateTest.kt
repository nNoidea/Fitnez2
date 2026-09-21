package com.nnoidea.fitnez2.ui.state

import android.content.Context
import androidx.compose.ui.focus.FocusManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.service.WorkoutService
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeBottomSheetStateTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var recordService: RecordService
    private lateinit var exerciseService: ExerciseService
    private lateinit var workoutService: WorkoutService
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
        workoutService = WorkoutService(database)
        settingsService = SettingsService(context)
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        globalUiState = GlobalUiState(scope = testScope, settingsService = settingsService)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        database.close()
    }

    private fun createSheetState(): HomeBottomSheetState {
        val dummyFocusManager = object : FocusManager {
            override fun clearFocus(force: Boolean) {}
            override fun moveFocus(focusDirection: androidx.compose.ui.focus.FocusDirection): Boolean = true
        }

        return HomeBottomSheetState(
            scope = testScope,
            recordService = recordService,
            workoutService = workoutService,
            exerciseService = exerciseService,
            settingsService = settingsService,
            globalUiState = globalUiState,
            keyboardController = null,
            focusManager = dummyFocusManager,
            context = context,
            maxOffset = 1000f,
            minOffset = 0f,
            onHapticFeedback = {}
        )
    }

    @Test
    fun exerciseSelection_updatesSelectedExerciseAndName() = runBlocking {
        val squat = exerciseService.createExercise("Squat")
        val state = createSheetState()

        state.onExerciseSelected(squat, closeDialog = true)
        assertEquals(squat.id, state.selectedExerciseId)
        assertEquals("Squat", state.selectedExerciseName)
        assertNull(state.selectedWorkout)
    }

    @Test
    fun workoutSelection_updatesSelectedWorkoutAndExerciseName() = runBlocking {
        val bench = exerciseService.createExercise("Bench Press")
        val workout = workoutService.createWorkout("Chest Day")
        workoutService.addRecordToWorkout(workout.id, bench.id, sets = 3, reps = 10, weight = 80.0)

        val state = createSheetState()
        state.onWorkoutSelected(workout, closeDialog = true)

        assertNotNull(state.selectedWorkout)
        assertEquals(workout.id, state.selectedWorkout?.id)
        assertEquals("Chest Day", state.selectedExerciseName)
    }

    @Test
    fun collapseAndExpand_updatesOffsetAndIsExpanded() = runBlocking {
        val state = createSheetState()

        state.offsetY.snapTo(state.minOffset) // expanded position
        assertTrue(state.isExpanded)

        state.offsetY.snapTo(state.maxOffset) // collapsed position
        assertEquals(false, state.isExpanded)
    }

    @Test
    fun formFields_setPendingAndCommittedValues() = runBlocking {
        val state = createSheetState()

        state.onCommittedSetsChange("4")
        assertEquals("4", state.committedSets)

        state.onCommittedRepsChange("12")
        assertEquals("12", state.committedReps)

        state.onCommittedWeightChange("90")
        assertEquals("90", state.committedWeight)
    }
}
