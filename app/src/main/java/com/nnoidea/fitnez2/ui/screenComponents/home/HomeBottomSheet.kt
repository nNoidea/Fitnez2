package com.nnoidea.fitnez2.ui.screenComponents.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.BottomSheetRepsField
import com.nnoidea.fitnez2.ui.components.BottomSheetSetsField
import com.nnoidea.fitnez2.ui.components.BottomSheetWeightField
import com.nnoidea.fitnez2.ui.components.bottomsheet.BUTTONHEIGHT
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState
import com.nnoidea.fitnez2.ui.components.bottomsheet.SheetFormRow

import com.nnoidea.fitnez2.ui.components.dialog.ExerciseSelectionDialog
import com.nnoidea.fitnez2.ui.screenComponents.home.ExerciseHistoryList

/**
 * Home screen bottom sheet — pre-wired with:
 * - Exercise selector + sets/reps/weight form
 * - Exercise history list (expanded view)
 * - DB persistence via [rememberHomeBottomSheetState]
 * - "Create Workout" visible in exercise dialog
 */
@Composable
fun HomeBottomSheet(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state = rememberHomeBottomSheetState()

    PredictiveBottomSheet(state = state, modifier = modifier) {
        SheetFormRow(
            state = state,
            showInputs = state.selectedWorkout == null
        )

        // Expanded: exercise history for selected exercise or workout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 24.dp)
        ) {
            if (state.hasBeenOpened) {
                val filterIds = if (state.selectedWorkout != null) {
                    state.selectedWorkoutRecords.map { it.workoutRecord.exerciseId }.distinct()
                } else if (state.selectedExerciseId != null) {
                    listOf(state.selectedExerciseId!!)
                } else null

                if (filterIds != null && filterIds.isNotEmpty()) {
                    ExerciseHistoryList(
                        modifier = Modifier.fillMaxSize(),
                        filterExerciseIds = filterIds,
                        useAlternatingColors = state.selectedWorkout != null
                    )
                }
            }
        }
    }

    ExerciseSelectionDialog(
        show = state.showExerciseSelection,
        exercises = state.exercises,
        workouts = state.workouts,
        selectedExerciseId = state.selectedExerciseId,
        selectedWorkoutId = (state as? HomeBottomSheetState)?.selectedWorkout?.id,
        exerciseDao = com.nnoidea.fitnez2.data.LocalAppDatabase.current.exerciseDao(),
        workoutDao = com.nnoidea.fitnez2.data.LocalAppDatabase.current.workoutDao(),
        onDismissRequest = { state.toggleExerciseSelection(false) },
        onExerciseSelected = { state.onExerciseSelected(it, closeDialog = true) },
        onExerciseCreated = { state.onExerciseSelected(it, closeDialog = false) },
        onWorkoutSelected = { 
            state.onWorkoutSelected(it, closeDialog = true)
        },
        onWorkoutEdit = { workout ->
            state.toggleExerciseSelection(false)
            val intent = android.content.Intent(context, com.nnoidea.fitnez2.MainActivity::class.java).apply {
                putExtra(com.nnoidea.fitnez2.MainActivity.EXTRA_PAGE_ROUTE, com.nnoidea.fitnez2.ui.navigation.AppPage.Workout.route)
                putExtra("extra_workout_id", workout.id)
            }
            context.startActivity(intent)
        },
        showCreateWorkout = true
    )
}


