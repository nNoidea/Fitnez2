package com.nnoidea.fitnez2.ui.screenComponents.workout

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
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import kotlinx.coroutines.launch

/**
 * BottomSheet state for the Workout screen: does NOT persist records.
 * Instead, it calls [onRecordCreated] to add to the in-memory workout list.
 */
@Stable
class WorkoutBottomSheetState(
    scope: kotlinx.coroutines.CoroutineScope,
    exerciseDao: com.nnoidea.fitnez2.data.dao.ExerciseDao,
    private val dao: com.nnoidea.fitnez2.data.dao.RecordDao,
    private val workoutDao: com.nnoidea.fitnez2.data.dao.WorkoutDao,
    settingsRepository: com.nnoidea.fitnez2.data.SettingsRepository,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    maxOffset: Float,
    minOffset: Float,
    private val workoutId: Int?,
    onHapticFeedback: (Int) -> Unit,
    private val onRecordCreated: (WorkoutRecordWithExercise) -> Unit
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

    init {
        scope.launch {
            if (workoutId != null) {
                val records = workoutDao.getRecordsForWorkout(workoutId)
                if (records.isNotEmpty()) {
                    val latestRecord = records.last()
                    selectedExerciseId = latestRecord.workoutRecord.exerciseId
                    selectedExerciseName = latestRecord.exerciseName
                    loadInputsForExercise(latestRecord.workoutRecord.exerciseId)
                } else {
                    initializeSession()
                }
            } else {
                initializeSession()
            }
        }
    }

    private suspend fun initializeSession() {
        val latest = dao.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId)
        } else {
            sets = defaultSets
            reps = defaultReps
            weight = defaultWeight
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
        scope.launch { loadInputsForExercise(exercise.id) }
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

                val validatedSets = ValidateAndCorrect.sets(resolvedSets()) ?: return@launch
                val validatedReps = ValidateAndCorrect.reps(resolvedReps()) ?: return@launch
                val validatedWeight = ValidateAndCorrect.weight(resolvedWeight()) ?: return@launch

                dismissInput()

                val record = WorkoutRecordWithExercise(
                    workoutRecord = WorkoutRecord(
                        id = System.nanoTime().toInt(), // Dummy ID
                        workoutId = 0, // Placeholder
                        exerciseId = exerciseId,
                        sets = validatedSets,
                        reps = validatedReps,
                        weight = validatedWeight
                    ),
                    exerciseName = exerciseName
                )

                onRecordCreated(record)

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

            } catch (_: Exception) { }
        }
    }
}

@Composable
fun rememberWorkoutBottomSheetState(
    workoutId: Int? = null,
    onRecordCreated: (WorkoutRecordWithExercise) -> Unit
): PredictiveBottomSheetState {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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

    return remember(maxOffset, minOffset, workoutId) {
        WorkoutBottomSheetState(
            scope = scope,
            exerciseDao = database.exerciseDao(),
            dao = database.recordDao(),
            workoutDao = database.workoutDao(),
            settingsRepository = settingsRepository,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = maxOffset,
            minOffset = minOffset,
            workoutId = workoutId,
            onHapticFeedback = { view.performHapticFeedback(it) },
            onRecordCreated = onRecordCreated
        )
    }
}
