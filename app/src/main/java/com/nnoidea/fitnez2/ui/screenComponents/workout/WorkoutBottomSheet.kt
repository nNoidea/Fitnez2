package com.nnoidea.fitnez2.ui.screenComponents.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState

import com.nnoidea.fitnez2.ui.components.dialog.ExerciseSelectionDialog
import com.nnoidea.fitnez2.ui.screenComponents.home.ExerciseHistoryList
import com.nnoidea.fitnez2.ui.screenComponents.home.SheetFormRow

/**
 * Workout screen bottom sheet — pre-wired with:
 * - Exercise selector + sets/reps/weight form (shared [SheetFormRow])
 * - Exercise history list (expanded view)
 * - In-memory record creation via callback
 * - "Create Workout" hidden in exercise dialog (already on workout screen)
 */
@Composable
fun WorkoutBottomSheet(
    state: PredictiveBottomSheetState,
    modifier: Modifier = Modifier
) {
    PredictiveBottomSheet(state = state, modifier = modifier) {
        SheetFormRow(state = state)

        // Expanded: exercise history for selected exercise
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 24.dp)
        ) {
            if (state.hasBeenOpened) {
                val filterIds = if (state.selectedExerciseId != null) {
                    listOf(state.selectedExerciseId!!)
                } else null

                if (filterIds != null && filterIds.isNotEmpty()) {
                    ExerciseHistoryList(
                        modifier = Modifier.fillMaxSize(),
                        filterExerciseIds = filterIds,
                        useAlternatingColors = false
                    )
                }
            }
        }
    }

    ExerciseSelectionDialog(
        show = state.showExerciseSelection,
        exercises = state.exercises,
        selectedExerciseId = state.selectedExerciseId,
        exerciseDao = com.nnoidea.fitnez2.data.LocalAppDatabase.current.exerciseDao(),
        workoutDao = com.nnoidea.fitnez2.data.LocalAppDatabase.current.workoutDao(),
        onDismissRequest = { state.toggleExerciseSelection(false) },
        onExerciseSelected = { state.onExerciseSelected(it, closeDialog = true) },
        onExerciseCreated = { state.onExerciseSelected(it, closeDialog = false) },
        showCreateWorkout = false
    )
}
