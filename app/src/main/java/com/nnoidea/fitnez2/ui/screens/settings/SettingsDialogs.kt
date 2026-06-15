package com.nnoidea.fitnez2.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveAlertDialog

data class Defaults(val sets: String, val reps: String, val weight: String)

@Composable
fun DefaultValuesEditorDialog(
    show: Boolean,
    currentDefaults: Defaults,
    onSave: (Int, Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var sets by remember { mutableStateOf(currentDefaults.sets) }
    var reps by remember { mutableStateOf(currentDefaults.reps) }
    var weight by remember { mutableStateOf(currentDefaults.weight) }

    LaunchedEffect(show) {
        if (show) {
            sets = currentDefaults.sets
            reps = currentDefaults.reps
            weight = currentDefaults.weight
        }
    }

    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.labelDefaultExerciseValues,
        confirmButton = {
            Button(
                onClick = {
                    val validSets = ValidateAndCorrect.sets(sets)
                    val validReps = ValidateAndCorrect.reps(reps)
                    val validWeight = ValidateAndCorrect.weight(weight)

                    if (validSets != null && validReps != null && validWeight != null) {
                        onSave(validSets, validReps, validWeight)
                    }
                }
            ) {
                Text(globalLocalization.labelSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(globalLocalization.labelCancel)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = sets,
                onValueChange = { sets = it },
                label = { Text(globalLocalization.labelDefaultSets) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text(globalLocalization.labelDefaultReps) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(globalLocalization.labelDefaultWeight) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
fun ImportConfirmationDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.titleImportWarning,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(globalLocalization.labelConfirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(globalLocalization.labelCancel)
            }
        }
    ) {
        Text(globalLocalization.msgImportWarning)
    }
}
