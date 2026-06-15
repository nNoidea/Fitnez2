package com.nnoidea.fitnez2.ui.screens.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalWorkoutService
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.bottomsheet.PredictiveBottomSheetState

import com.nnoidea.fitnez2.ui.components.dialog.ExerciseSelectionDialog
import com.nnoidea.fitnez2.ui.components.recordlist.RecordList
import com.nnoidea.fitnez2.ui.screens.timeline.rememberRecordListState
import com.nnoidea.fitnez2.ui.components.bottomsheet.SheetFormRow

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
        SheetFormRow(state = state, showInputs = true)

        // Expanded: exercise history for selected exercise
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 12.dp)
                .graphicsLayer {
                    val progress = (state.offsetY.value - state.minOffset) / (state.maxOffset - state.minOffset)
                    alpha = (1f - progress).coerceIn(0f, 1f)
                }
        ) {
            if (state.hasBeenOpened) {
                val filterIds = if (state.selectedExerciseId != null) {
                    listOf(state.selectedExerciseId!!)
                } else null

                if (filterIds != null && filterIds.isNotEmpty()) {
                    val historyState = rememberRecordListState(filterIds, useAlternatingColors = false)
                    RecordList(
                        items = historyState.uiItems,
                        weightUnit = historyState.weightUnit,
                        listState = historyState.listState,
                        expandedRecordIds = historyState.expandedRecordIds,
                        timestampTokens = historyState.timestampTokens,
                        onShowTimestamp = { historyState.showTimestampFor(it) },
                        modifier = Modifier.fillMaxSize(),
                        onUpdateRequest = { historyState.onUpdateRequest(it) },
                        onDeleteRequest = { historyState.onDeleteRequest(it) },
                        onDeleteGroupRequest = { historyState.onDeleteGroupRequest(it) }
                    )
                }
            }
        }
    }

    ExerciseSelectionDialog(
        show = state.showExerciseSelection,
        exercises = state.exercises,
        selectedExerciseId = state.selectedExerciseId,
        exerciseService = LocalExerciseService.current,
        workoutService = LocalWorkoutService.current,
        onDismissRequest = { state.toggleExerciseSelection(false) },
        onExerciseSelected = { state.onExerciseSelected(it, closeDialog = true) },
        onExerciseCreated = { state.onExerciseSelected(it, closeDialog = false) },
        showCreateWorkout = false
    )
}
