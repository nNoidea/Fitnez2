package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.runtime.Composable
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Workout

@Composable
fun DeleteExerciseDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (Exercise) -> Unit,
    exercise: Exercise?
) {
    PredictiveConfirmationDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.labelDelete,
        message = globalLocalization.labelDeleteExerciseWarning,
        confirmLabel = globalLocalization.labelDelete,
        cancelLabel = globalLocalization.labelCancel,
        isDestructive = true,
        onConfirm = {
            exercise?.let { onConfirmDelete(it) }
        }
    )
}

@Composable
fun EditExerciseDialog(
    show: Boolean,
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onConfirmEdit: (Exercise, String) -> Unit
) {
    PredictiveInputDialog(
        show = show,
        title = globalLocalization.labelEditExercise,
        initialValue = exercise?.name ?: "",
        label = globalLocalization.labelExerciseName,
        confirmLabel = globalLocalization.labelSave,
        cancelLabel = globalLocalization.labelCancel,
        onDismissRequest = onDismiss,
        onConfirm = { newName ->
            exercise?.let { onConfirmEdit(it, newName) }
        }
    )
}

@Composable
fun DeleteWorkoutDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (Workout) -> Unit,
    workout: Workout?
) {
    PredictiveConfirmationDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.labelDelete,
        message = globalLocalization.labelDeleteWorkoutWarning,
        confirmLabel = globalLocalization.labelDelete,
        cancelLabel = globalLocalization.labelCancel,
        isDestructive = true,
        onConfirm = {
            workout?.let { onConfirmDelete(it) }
        }
    )
}

@Composable
fun CreateExerciseDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirmCreate: (String) -> Unit
) {
    PredictiveInputDialog(
        show = show,
        title = globalLocalization.labelCreateExercise,
        initialValue = "",
        label = globalLocalization.labelExerciseName,
        confirmLabel = globalLocalization.labelAdd,
        cancelLabel = globalLocalization.labelCancel,
        placeholder = globalLocalization.labelExerciseNamePlaceholder,
        onDismissRequest = onDismiss,
        onConfirm = { newName ->
            onConfirmCreate(newName)
        }
    )
}
