package com.nnoidea.fitnez2.ui.screens.workout

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.service.LocalWorkoutService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.service.WorkoutService
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import com.nnoidea.fitnez2.ui.components.bottomsheet.rememberBottomSheetLayoutParams
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * BottomSheet state for the Workout screen: does NOT persist records.
 * Instead, it calls [onRecordCreated] to add to the in-memory workout list.
 */
@Stable
class WorkoutBottomSheetState(
    scope: kotlinx.coroutines.CoroutineScope,
    exerciseService: ExerciseService,
    private val recordService: RecordService,
    private val workoutService: WorkoutService,
    settingsService: SettingsService,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    maxOffset: Float,
    minOffset: Float,
    private val workoutId: String?,
    onHapticFeedback: (Int) -> Unit,
    var onRecordCreated: (WorkoutRecordWithExercise) -> Unit
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

    init {
        scope.launch {
            if (workoutId != null) {
                val records = workoutService.getRecordsForWorkout(workoutId)
                if (records.isNotEmpty()) {
                    val latestRecord = records.last()
                    selectedExerciseId = latestRecord.workoutRecord.exerciseId
                    selectedExerciseName = latestRecord.exerciseName
                    loadInputsForExercise(latestRecord.workoutRecord.exerciseId, recordService::getLatestRecordByExerciseId)
                } else {
                    initializeSession()
                }
            } else {
                initializeSession()
            }
        }
    }

    private suspend fun initializeSession() {
        val latest = recordService.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId, recordService::getLatestRecordByExerciseId)
        } else {
            committedSets = defaultSets
            committedReps = defaultReps
            committedWeight = defaultWeight
        }
    }

    override fun onExerciseSelected(exercise: Exercise, closeDialog: Boolean) {
        super.onExerciseSelected(exercise, closeDialog)
        scope.launch { loadInputsForExercise(exercise.id, recordService::getLatestRecordByExerciseId) }
    }

    override fun onAddClick() {
        scope.launch {
            try {
                val exerciseId = selectedExerciseId
                val exerciseName = selectedExerciseName ?: return@launch

                if (exerciseId == null) {
                    Toast.makeText(context, globalLocalization.labelSelectExercise, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val validatedSets = ValidateAndCorrect.sets(resolveSets()) ?: return@launch
                val validatedReps = ValidateAndCorrect.reps(resolveReps()) ?: return@launch
                val validatedWeight = ValidateAndCorrect.weight(resolveWeight()) ?: return@launch

                dismissInput()

                val record = WorkoutRecordWithExercise(
                    workoutRecord = WorkoutRecord(
                        workoutId = "", // Placeholder
                        exerciseId = exerciseId,
                        sets = validatedSets,
                        reps = validatedReps,
                        weight = validatedWeight
                    ),
                    exerciseName = exerciseName
                )

                onRecordCreated(record)

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) { }
        }
    }
}

@Composable
fun rememberWorkoutBottomSheetState(
    workoutId: String? = null,
    onRecordCreated: (WorkoutRecordWithExercise) -> Unit
): PredictiveBottomSheetState {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val exerciseService = LocalExerciseService.current
    val recordService = LocalRecordService.current
    val workoutService = LocalWorkoutService.current
    val settingsService = LocalSettingsService.current
    val scope = rememberCoroutineScope()
    val layoutParams = rememberBottomSheetLayoutParams()
    val view = androidx.compose.ui.platform.LocalView.current

    val state = remember(layoutParams, workoutId) {
        WorkoutBottomSheetState(
            scope = scope,
            exerciseService = exerciseService,
            recordService = recordService,
            workoutService = workoutService,
            settingsService = settingsService,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = layoutParams.maxOffset,
            minOffset = layoutParams.minOffset,
            workoutId = workoutId,
            onHapticFeedback = { view.performHapticFeedback(it) },
            onRecordCreated = onRecordCreated
        )
    }

    LaunchedEffect(onRecordCreated) {
        (state as WorkoutBottomSheetState).onRecordCreated = onRecordCreated
    }

    return state
}
