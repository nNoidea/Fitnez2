package com.nnoidea.fitnez2.ui.screenComponents.workout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState

import com.nnoidea.fitnez2.ui.components.dialog.ExerciseSelectionDialog
import com.nnoidea.fitnez2.ui.screenComponents.home.SheetFormRow

/**
 * Workout screen bottom sheet — pre-wired with:
 * - Exercise selector + sets/reps/weight form (shared [SheetFormRow])
 * - In-memory record creation via callback
 * - "Create Workout" hidden in exercise dialog (already on workout screen)
 * - No history list in expanded area
 */
@Composable
fun WorkoutBottomSheet(
    state: PredictiveBottomSheetState,
    modifier: Modifier = Modifier
) {
    PredictiveBottomSheet(state = state, modifier = modifier) {
        SheetFormRow(state = state)
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
