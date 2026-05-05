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
    settingsRepository: com.nnoidea.fitnez2.data.SettingsRepository,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    focusManager: androidx.compose.ui.focus.FocusManager,
    context: android.content.Context,
    maxOffset: Float,
    minOffset: Float,
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
            sets = defaultSets
            reps = defaultReps
            weight = defaultWeight
        }
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

                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)

            } catch (_: Exception) { }
        }
    }
}

@Composable
fun rememberWorkoutBottomSheetState(
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

    return remember(maxOffset, minOffset) {
        WorkoutBottomSheetState(
            scope = scope,
            exerciseDao = database.exerciseDao(),
            settingsRepository = settingsRepository,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = maxOffset,
            minOffset = minOffset,
            onHapticFeedback = { view.performHapticFeedback(it) },
            onRecordCreated = onRecordCreated
        )
    }
}
