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
        SheetFormRow(state = state)

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

/**
 * Shared form row: exercise selector button + add button + sets/reps/weight fields.
 * Used by both Home and Workout bottom sheets.
 */
@Composable
internal fun SheetFormRow(state: PredictiveBottomSheetState) {
    val buttonHeight = BUTTONHEIGHT.dp
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row: Exercise Selector + Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    state.toggleExerciseSelection(true)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.selectedExerciseName ?: globalLocalization.labelSelectExercise,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Button(
                onClick = { state.onAddClick() },
                modifier = Modifier
                    .weight(1f)
                    .height(buttonHeight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(globalLocalization.labelAdd, maxLines = 1)
            }
        }

        // Row: Sets, Reps, Weight (Hide if workout is selected)
        if (state !is HomeBottomSheetState || state.selectedWorkout == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BottomSheetSetsField(
                    value = state.sets,
                    onValidChange = { state.onSetsChange(it) },
                    onRawValueChange = { state.setsRaw = it },
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )

                BottomSheetRepsField(
                    value = state.reps,
                    onValidChange = { state.onRepsChange(it) },
                    onRawValueChange = { state.repsRaw = it },
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )

                BottomSheetWeightField(
                    value = state.weight.toDoubleOrNull() ?: 0.0,
                    label = state.weightUnit,
                    onValidChange = { state.onWeightChange(it) },
                    onRawValueChange = { state.weightRaw = it },
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )
            }
        } else {
            Spacer(modifier = Modifier.fillMaxWidth().height(buttonHeight))
        }
    }
}
