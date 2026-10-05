package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.input.SetsRepsWeightGroup

/**
 * Shared form row: exercise selector button + add button + sets/reps/weight fields.
 * Used by both Home and Workout bottom sheets.
 */
@Composable
internal fun SheetFormRow(
    state: PredictiveBottomSheetState,
    showInputs: Boolean
) {
    val buttonHeight = BUTTONHEIGHT.dp + 6.dp
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row: Exercise Selector + Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    com.nnoidea.fitnez2.ui.components.haptics.HapticEngine.performClick(view.context)
                    state.toggleExerciseSelection(true)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(buttonHeight)
                    .testTag("exercise_selector_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.selectedExerciseName ?: globalLocalization.labelSelectExercise,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedExerciseName == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
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
                    .height(buttonHeight)
                    .testTag("add_record_button"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(globalLocalization.labelAdd, maxLines = 1, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }

        // Row: Sets, Reps, Weight (Hide if workout is selected)
        if (showInputs) {
            SetsRepsWeightGroup(
                sets = state.committedSets,
                reps = state.committedReps,
                weight = state.committedWeight,
                weightUnit = state.weightUnit,
                showLabels = true,
                height = buttonHeight,
                onSetsChange = { state.onCommittedSetsChange(it.toString()) },
                onRepsChange = { state.onCommittedRepsChange(it.toString()) },
                onWeightChange = { state.onCommittedWeightChange(it.toString()) },
                onPendingSetsChange = { state.pendingSets = it },
                onPendingRepsChange = { state.pendingReps = it },
                onPendingWeightChange = { state.pendingWeight = it }
            )
        } else {
            Spacer(modifier = Modifier.fillMaxWidth().height(buttonHeight))
        }
    }
}
