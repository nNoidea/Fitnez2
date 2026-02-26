package com.nnoidea.fitnez2.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val BUTTONHEIGHT = 45
const val PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP = 2 * BUTTONHEIGHT + 70 + 15

/**
 * Public entry point for the history-based Predictive Bottom Sheet.
 */
@Composable
fun PredictiveBottomSheet(
    modifier: Modifier = Modifier
) {
    val state = rememberPredictiveBottomSheetState()
    PredictiveBottomSheet(state = state, modifier = modifier)
}

/**
 * Modular and stateless UI for the Predictive Bottom Sheet.
 * Operates solely on the [PredictiveBottomSheetState] interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBottomSheet(
    state: PredictiveBottomSheetState,
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topPaddingPx = with(density) { topPadding.toPx() }
        val expandedHeight = with(density) { constraints.maxHeight.toDp() } - topPadding

        // Sync global UI state for snackbar positioning
        LaunchedEffect(state.isExpanded) {
            globalUiState.bottomSheetSnackbarOffset = if (state.isExpanded) 0.dp else PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp
        }

        // Predictive Back Handler
        PredictiveBackHandler(enabled = state.isExpanded && !isOverlayOpen) { progress ->
            try {
                progress.collect { state.onPredictiveBackProgress(it.progress) }
                scope.launch { state.onPredictiveBackCommit() }
            } catch (e: Exception) {
                state.onPredictiveBackCancel()
            }
        }

        // Overshoot Buffer
        val overshootBuffer = 150.dp
        val sheetTotalHeight = expandedHeight + overshootBuffer

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(sheetTotalHeight)
                .fillMaxWidth()
                .offset { IntOffset(0, (state.offsetY.value + topPaddingPx).roundToInt()) }
                .graphicsLayer {
                    if (state.predictiveProgress > 0f) {
                        val scale = 1f - (state.predictiveProgress * 0.2f)
                        scaleX = scale
                        scaleY = scale
                        translationY = size.height * state.predictiveProgress * 0.2f
                    }
                }
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .nestedScroll(state.nestedScrollConnection)
                .draggable(
                    state = androidx.compose.foundation.gestures.rememberDraggableState { delta ->
                        if (!isOverlayOpen) {
                            scope.launch {
                                val newOffset = (state.offsetY.value + delta).coerceIn(state.minOffset, state.maxOffset)
                                state.offsetY.snapTo(newOffset)
                            }
                        }
                    },
                    orientation = Orientation.Vertical,
                    onDragStarted = { /* handled by state focus management if needed */ },
                    onDragStopped = { velocity ->
                        scope.launch { state.settleSpring(velocity) }
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()

                SheetFormLayout(state = state)

                // Expanded History View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp)
                ) {
                    if (state.hasBeenOpened && state.selectedExerciseId != null) {
                        ExerciseHistoryList(
                            modifier = Modifier.fillMaxSize(),
                            filterExerciseIds = listOfNotNull(state.selectedExerciseId),
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
            exerciseDao = com.nnoidea.fitnez2.data.LocalAppDatabase.current.exerciseDao(), // Still need a way to pass DAO for details
            onDismissRequest = { state.toggleExerciseSelection(false) },
            onExerciseSelected = { state.onExerciseSelected(it) }
        )
    }
}

@Composable
private fun SheetFormLayout(state: PredictiveBottomSheetState) {
    val buttonHeight = BUTTONHEIGHT.dp
    
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
                onClick = { state.toggleExerciseSelection(true) },
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

        // Row: Sets, Reps, Weight
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
    }
}
