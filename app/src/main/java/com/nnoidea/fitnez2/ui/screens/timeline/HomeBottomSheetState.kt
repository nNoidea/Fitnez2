package com.nnoidea.fitnez2.ui.screens.timeline

import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import com.nnoidea.fitnez2.service.ExerciseService
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
            workouts.find { it.id == selectedWorkout?.id }?.name ?: overrideExerciseName
        } else {
            exercises.find { it.id == selectedExerciseId }?.name ?: overrideExerciseName
        }
        set(value) {
            overrideExerciseName = value
        }

    init {
        scope.launch { initializeSession() }
        scope.launch { 
            workoutService.getAllWorkoutsFlow().collect { currentWorkouts -> 
                workouts = currentWorkouts 
                    if (selectedWorkout != null && currentWorkouts.none { it.id == selectedWorkout?.id }) {
                    selectedWorkout = null
                    selectedWorkoutRecords = emptyList()
                    overrideExerciseName = null
                }
            } 
        }
    }

    private suspend fun initializeSession() {
        val latest = recordService.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId, recordService::getLatestRecordByExerciseId)
        }
    }

    override fun onExerciseSelected(exercise: Exercise, closeDialog: Boolean) {
        super.onExerciseSelected(exercise, closeDialog)
        selectedWorkout = null
        selectedWorkoutRecords = emptyList()
        scope.launch { loadInputsForExercise(exercise.id, recordService::getLatestRecordByExerciseId) }
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
                    GlobalUiState.emitToAll(UiSignal.ScrollToRecord(lastInsertedId))
                    onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)
                    return@launch
                }

                val exerciseId = selectedExerciseId
                if (exerciseId == null) {
                    Toast.makeText(context, globalLocalization.labelSelectExercise, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val validatedSets = ValidateAndCorrect.sets(resolveSets()) ?: return@launch
                val validatedReps = ValidateAndCorrect.reps(resolveReps()) ?: return@launch
                val validatedWeight = ValidateAndCorrect.weight(resolveWeight()) ?: return@launch

                dismissInput()

                val newRecord = recordService.createRecord(
                    exerciseId = exerciseId,
                    sets = validatedSets,
                    reps = validatedReps,
                    weight = validatedWeight,
                    date = System.currentTimeMillis()
                )
                GlobalUiState.emitToAll(UiSignal.RecordInserted(newRecord.id))
                GlobalUiState.emitToAll(UiSignal.ScrollToRecord(newRecord.id))

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }
}
