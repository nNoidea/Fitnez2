package com.nnoidea.fitnez2.ui.screenComponents.home

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import kotlinx.coroutines.launch

/**
 * BottomSheet state for the Home screen: persists records to the database
 * and emits a ScrollToTop signal so the history list scrolls up.
 */
@Stable
class HomeBottomSheetState(
    scope: kotlinx.coroutines.CoroutineScope,
    private val dao: com.nnoidea.fitnez2.data.dao.RecordDao,
    private val workoutDao: com.nnoidea.fitnez2.data.dao.WorkoutDao,
    exerciseDao: com.nnoidea.fitnez2.data.dao.ExerciseDao,
    settingsRepository: com.nnoidea.fitnez2.data.SettingsRepository,
    private val globalUiState: GlobalUiState,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    maxOffset: Float,
    minOffset: Float,
    onHapticFeedback: (Int) -> Unit
) : PredictiveBottomSheetState(
    scope = scope,
    exerciseDao = exerciseDao,
    settingsRepository = settingsRepository,
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
        scope.launch { workoutDao.getAllWorkoutsFlow().collect { workouts = it } }
    }

    private suspend fun initializeSession() {
        val latest = dao.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId)
        }
    }

    private suspend fun loadInputsForExercise(exerciseId: Int) {
        val latestForExercise = dao.getLatestRecordByExerciseId(exerciseId)
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
            selectedWorkoutRecords = workoutDao.getRecordsForWorkout(workout.id)
        }
    }

    override fun onAddClick() {
        scope.launch {
            try {
                val workout = selectedWorkout
                if (workout != null) {
                    if (selectedWorkoutRecords.isEmpty()) {
                        Toast.makeText(context, "Workout is empty", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    dismissInput()
                    val timestamp = System.currentTimeMillis()
                    var lastId: Long = 0
                    
                    // Insert all records in reverse order so the first item gets the highest ID
                    // and appears at the top in the history list (which orders by id DESC for same timestamps)
                    for (workoutRecord in selectedWorkoutRecords.reversed()) {
                        val record = Record(
                            exerciseId = workoutRecord.workoutRecord.exerciseId,
                            sets = workoutRecord.workoutRecord.sets,
                            reps = workoutRecord.workoutRecord.reps,
                            weight = workoutRecord.workoutRecord.weight,
                            date = timestamp
                        )
                        lastId = dao.create(record)
                    }
                    if (lastId > 0) {
                        globalUiState.emitSignal(UiSignal.ScrollToTop(lastId.toInt()))
                        onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)
                    }
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

                val record = Record(
                    exerciseId = exerciseId,
                    sets = validatedSets,
                    reps = validatedReps,
                    weight = validatedWeight,
                    date = System.currentTimeMillis()
                )

                val newId = dao.create(record)
                globalUiState.emitSignal(UiSignal.ScrollToTop(newId.toInt()))

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)

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
    val database = LocalAppDatabase.current
    val settingsRepository = LocalSettingsRepository.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = androidx.compose.ui.platform.LocalView.current

    val peekHeight = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp
    val peekHeightPx = with(density) { peekHeight.toPx() }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val topPaddingPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }

    val maxOffset = screenHeightPx - topPaddingPx - peekHeightPx
    val minOffset = 0f

    return remember(maxOffset, minOffset) {
        HomeBottomSheetState(
            scope = scope,
            dao = database.recordDao(),
            workoutDao = database.workoutDao(),
            exerciseDao = database.exerciseDao(),
            settingsRepository = settingsRepository,
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
