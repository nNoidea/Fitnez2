package com.nnoidea.fitnez2.ui.components.bottomsheet

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.BottomSheetRepsField
import com.nnoidea.fitnez2.ui.components.BottomSheetSetsField
import com.nnoidea.fitnez2.ui.components.BottomSheetWeightField

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
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                    state.toggleExerciseSelection(true)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(buttonHeight),
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
        if (showInputs) {
            ConnectedInputGroup(
                spacing = 4.dp,
                outerCornerRadius = 24.dp,
                innerCornerRadius = 8.dp,
                items = listOf(
                    { ts, te, bs, be ->
                        BottomSheetSetsField(
                            value = state.sets,
                            onValidChange = { state.onSetsChange(it) },
                            onRawValueChange = { state.setsRaw = it },
                            modifier = Modifier.weight(1f).height(buttonHeight),
                            topStartRadius = ts,
                            topEndRadius = te,
                            bottomStartRadius = bs,
                            bottomEndRadius = be
                        )
                    },
                    { ts, te, bs, be ->
                        BottomSheetRepsField(
                            value = state.reps,
                            onValidChange = { state.onRepsChange(it) },
                            onRawValueChange = { state.repsRaw = it },
                            modifier = Modifier.weight(1f).height(buttonHeight),
                            topStartRadius = ts,
                            topEndRadius = te,
                            bottomStartRadius = bs,
                            bottomEndRadius = be
                        )
                    },
                    { ts, te, bs, be ->
                        BottomSheetWeightField(
                            value = state.weight.toDoubleOrNull() ?: 0.0,
                            label = state.weightUnit,
                            onValidChange = { state.onWeightChange(it) },
                            onRawValueChange = { state.weightRaw = it },
                            modifier = Modifier.weight(1f).height(buttonHeight),
                            topStartRadius = ts,
                            topEndRadius = te,
                            bottomStartRadius = bs,
                            bottomEndRadius = be
                        )
                    }
                )
            )
        } else {
            Spacer(modifier = Modifier.fillMaxWidth().height(buttonHeight))
        }
    }
}
