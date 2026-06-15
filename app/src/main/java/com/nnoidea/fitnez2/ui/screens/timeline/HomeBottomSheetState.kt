package com.nnoidea.fitnez2.ui.screens.timeline

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.service.LocalWorkoutService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.service.WorkoutService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * BottomSheet state for the Home screen: persists records to the database
 * and emits a ScrollToTop signal so the history list scrolls up.
 */
@Stable
class HomeBottomSheetState(
    scope: kotlinx.coroutines.CoroutineScope,
    private val recordService: RecordService,
    private val workoutService: WorkoutService,
    exerciseService: ExerciseService,
    settingsService: SettingsService,
    private val globalUiState: GlobalUiState,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    maxOffset: Float,
    minOffset: Float,
    onHapticFeedback: (Int) -> Unit
) : PredictiveBottomSheetState(
    scope = scope,
    exerciseService = exerciseService,
    settingsService = settingsService,
    keyboardController = keyboardController,
    focusManager = focusManager,
    context = context,
    maxOffset = maxOffset,
    minOffset = minOffset,
    onHapticFeedback = onHapticFeedback
) {

    var workouts by mutableStateOf<List<Workout>>(emptyList())
    var selectedWorkout by mutableStateOf<Workout?>(null)
    var selectedWorkoutRecords by mutableStateOf<List<WorkoutRecordWithExercise>>(emptyList())

    override var selectedExerciseName: String?
        get() = if (selectedWorkout != null) {
            workouts.find { it.id == selectedWorkout?.id }?.name ?: selectedExerciseNameSnapshot
        } else {
            exercises.find { it.id == selectedExerciseId }?.name ?: selectedExerciseNameSnapshot
        }
        set(value) {
            selectedExerciseNameSnapshot = value
        }

    init {
        scope.launch { initializeSession() }
        scope.launch { 
            workoutService.getAllWorkoutsFlow().collect { currentWorkouts -> 
                workouts = currentWorkouts 
                    if (selectedWorkout != null && currentWorkouts.none { it.id == selectedWorkout?.id }) {
                    selectedWorkout = null
                    selectedWorkoutRecords = emptyList()
                    selectedExerciseNameSnapshot = null
                }
            } 
        }
    }

    private suspend fun initializeSession() {
        val latest = recordService.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId)
        }
    }

    private suspend fun loadInputsForExercise(exerciseId: String) {
        val latestForExercise = recordService.getLatestRecordByExerciseId(exerciseId)
        if (latestForExercise != null) {
            sets = latestForExercise.record.sets.toString()
            reps = latestForExercise.record.reps.toString()
            weight = latestForExercise.record.weight.toString()
        } else {
            sets = defaultSets
            reps = defaultReps
            weight = defaultWeight
        }
    }

    override fun onExerciseSelected(exercise: Exercise, closeDialog: Boolean) {
        super.onExerciseSelected(exercise, closeDialog)
        selectedWorkout = null
        selectedWorkoutRecords = emptyList()
        scope.launch { loadInputsForExercise(exercise.id) }
    }

    fun onWorkoutSelected(workout: Workout, closeDialog: Boolean) {
        selectedWorkout = workout
        selectedExerciseId = null
        selectedExerciseName = workout.name
        if (closeDialog) {
            showExerciseSelection = false
        }
        scope.launch {
            selectedWorkoutRecords = workoutService.getRecordsForWorkout(workout.id)
        }
    }

    override fun onAddClick() {
        scope.launch {
            try {
                val workout = selectedWorkout
                if (workout != null) {
                    if (selectedWorkoutRecords.isEmpty()) {
                        Toast.makeText(context, globalLocalization.errorWorkoutEmpty, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    dismissInput()
                    val timestamp = System.currentTimeMillis()
                    
                    // Insert all records in reverse order so the first item gets the highest ID
                    // and appears at the top in the history list (which orders by id DESC for same timestamps)
                    var lastInsertedId = ""
                    for (workoutRecord in selectedWorkoutRecords.reversed()) {
                        val record = recordService.createRecord(
                            exerciseId = workoutRecord.workoutRecord.exerciseId,
                            sets = workoutRecord.workoutRecord.sets,
                            reps = workoutRecord.workoutRecord.reps,
                            weight = workoutRecord.workoutRecord.weight,
                            date = timestamp
                        )
                        lastInsertedId = record.id
                        GlobalUiState.emitToAll(UiSignal.RecordInserted(lastInsertedId))
                    }
                    GlobalUiState.emitToAll(UiSignal.ScrollToTop(lastInsertedId))
                    onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)
                    return@launch
                }

                val exerciseId = selectedExerciseId
                if (exerciseId == null) {
                    Toast.makeText(context, globalLocalization.labelSelectExercise, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val validatedSets = ValidateAndCorrect.sets(resolvedSets()) ?: return@launch
                val validatedReps = ValidateAndCorrect.reps(resolvedReps()) ?: return@launch
                val validatedWeight = ValidateAndCorrect.weight(resolvedWeight()) ?: return@launch

                dismissInput()

                val newRecord = recordService.createRecord(
                    exerciseId = exerciseId,
                    sets = validatedSets,
                    reps = validatedReps,
                    weight = validatedWeight,
                    date = System.currentTimeMillis()
                )
                GlobalUiState.emitToAll(UiSignal.RecordInserted(newRecord.id))
                GlobalUiState.emitToAll(UiSignal.ScrollToTop(newRecord.id))

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }
}

@Composable
fun rememberHomeBottomSheetState(): HomeBottomSheetState {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val globalUiState = LocalGlobalUiState.current
    val recordService = LocalRecordService.current
    val exerciseService = LocalExerciseService.current
    val workoutService = LocalWorkoutService.current
    val settingsService = LocalSettingsService.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = androidx.compose.ui.platform.LocalView.current

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val peekHeight = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp + navBarPadding
    val peekHeightPx = with(density) { peekHeight.toPx() }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val topPaddingPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }

    val maxOffset = screenHeightPx - topPaddingPx - peekHeightPx
    val minOffset = 0f

    return remember(maxOffset, minOffset) {
        HomeBottomSheetState(
            scope = scope,
            recordService = recordService,
            workoutService = workoutService,
            exerciseService = exerciseService,
            settingsService = settingsService,
            globalUiState = globalUiState,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = maxOffset,
            minOffset = minOffset,
            onHapticFeedback = { view.performHapticFeedback(it) }
        )
    }
}
