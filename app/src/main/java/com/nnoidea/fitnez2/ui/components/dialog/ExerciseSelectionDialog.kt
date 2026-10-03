package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalContext
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.WorkoutService
import com.nnoidea.fitnez2.MainActivity
import com.nnoidea.fitnez2.ui.navigation.AppPage
import kotlinx.coroutines.launch

@Composable
fun ExerciseSelectionDialog(
    show: Boolean,
    exercises: List<Exercise>,
    workouts: List<Workout> = emptyList(),
    selectedExerciseId: String?,
    selectedWorkoutId: String? = null,
    exerciseService: ExerciseService,
    workoutService: WorkoutService? = null,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit,
    onWorkoutSelected: (Workout) -> Unit = {},
    onWorkoutEdit: (Workout) -> Unit = {},
    onExerciseCreated: (Exercise) -> Unit = {},
    showCreateWorkout: Boolean = true,
    onNavigateToWorkout: (() -> Unit)? = null
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }


    val sortedWorkouts = remember(workouts) {
        workouts.sortedBy { it.name.lowercase() }
    }

    val sortedExercises = remember(exercises) {
        exercises.sortedBy { it.name.lowercase() }
    }

    SelectionDialog(
        show = show,
        onDismissRequest = onDismissRequest
    ) {
        // Create Workout Button (hidden on workout screen)
        if (showCreateWorkout) {
            val context = LocalContext.current
            CreateActionRow(
                label = globalLocalization.labelWorkout,
                tint = MaterialTheme.colorScheme.primary,
                onClick = {
                    onDismissRequest()
                    if (onNavigateToWorkout != null) {
                        onNavigateToWorkout()
                    } else {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra(MainActivity.EXTRA_PAGE_ROUTE, AppPage.Workout.route)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        // Create Exercise Button at the top
        CreateActionRow(
            label = globalLocalization.labelExercise,
            tint = MaterialTheme.colorScheme.tertiary,
            onClick = { showCreateDialog = true }
        )

        // Workouts section
        if (sortedWorkouts.isNotEmpty()) {
            LabeledSectionDivider(label = globalLocalization.labelWorkouts)
            sortedWorkouts.forEach { workout ->
                SelectionItemRow(
                    name = workout.name,
                    isSelected = workout.id == selectedWorkoutId,
                    onSelect = { onWorkoutSelected(workout) },
                    onEdit = workoutService?.let { { onWorkoutEdit(workout) } },
                    onDelete = workoutService?.let { { workoutToDelete = workout } }
                )
            }
        }

        // Exercises section
        if (sortedExercises.isNotEmpty()) {
            LabeledSectionDivider(label = globalLocalization.labelExercises)
            sortedExercises.forEach { exercise ->
                SelectionItemRow(
                    name = exercise.name,
                    isSelected = exercise.id == selectedExerciseId,
                    testTag = "exercise_option_${exercise.name}",
                    onSelect = { onExerciseSelected(exercise) },
                    onEdit = { exerciseToEdit = exercise },
                    onDelete = { exerciseToDelete = exercise }
                )
            }
        }
    }

    PredictiveConfirmationDialog(
        show = exerciseToDelete != null,
        onDismissRequest = { exerciseToDelete = null },
        title = globalLocalization.labelDelete,
        message = globalLocalization.labelDeleteExerciseWarning,
        confirmLabel = globalLocalization.labelDelete,
        cancelLabel = globalLocalization.labelCancel,
        isDestructive = true,
        onConfirm = {
            exerciseToDelete?.let { exercise ->
                scope.launch {
                    exerciseService.deleteExercise(exercise.id)
                    exerciseToDelete = null
                }
            }
        }
    )

    PredictiveInputDialog(
        show = exerciseToEdit != null,
        title = globalLocalization.labelEditExercise,
        initialValue = exerciseToEdit?.name ?: "",
        label = globalLocalization.labelExerciseName,
        confirmLabel = globalLocalization.labelSave,
        cancelLabel = globalLocalization.labelCancel,
        onDismissRequest = { exerciseToEdit = null },
        onConfirm = { newName ->
            exerciseToEdit?.let { exercise ->
                scope.launch {
                    try {
                        exerciseService.updateExercise(exercise.id, newName)
                        exerciseToEdit = null
                    } catch (_: Exception) { }
                }
            }
        }
    )

    PredictiveConfirmationDialog(
        show = workoutToDelete != null,
        onDismissRequest = { workoutToDelete = null },
        title = globalLocalization.labelDelete,
        message = globalLocalization.labelDeleteWorkoutWarning,
        confirmLabel = globalLocalization.labelDelete,
        cancelLabel = globalLocalization.labelCancel,
        isDestructive = true,
        onConfirm = {
            workoutToDelete?.let { workout ->
                scope.launch {
                    workoutService?.deleteWorkout(workout)
                    workoutToDelete = null
                }
            }
        }
    )

    val createDialogContext = LocalContext.current
    PredictiveInputDialog(
        show = showCreateDialog,
        title = globalLocalization.labelCreateExercise,
        initialValue = "",
        label = globalLocalization.labelExerciseName,
        confirmLabel = globalLocalization.labelAdd,
        cancelLabel = globalLocalization.labelCancel,
        placeholder = globalLocalization.labelExerciseNamePlaceholder,
        onDismissRequest = { showCreateDialog = false },
        onConfirm = { newName ->
            scope.launch {
                try {
                    val newExercise = exerciseService.createExercise(newName)
                    onExerciseCreated(newExercise)
                    showCreateDialog = false
                } catch (e: Exception) {
                    android.widget.Toast.makeText(createDialogContext, e.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@Composable
private fun SelectionItemRow(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    testTag: String? = null
) {
    val view = LocalView.current
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable {
                DialogDefaults.performItemClickHaptic(view)
                onSelect()
            }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = contentColor,
            modifier = Modifier.weight(1f)
        )

        if (onEdit != null) {
            IconButton(
                onClick = {
                    DialogDefaults.performItemClickHaptic(view)
                    onEdit()
                }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = globalLocalization.labelEdit(name),
                    tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary
                )
            }
        }

        if (onDelete != null) {
            IconButton(
                onClick = {
                    DialogDefaults.performItemClickHaptic(view)
                    onDelete()
                }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = globalLocalization.labelDelete,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CreateActionRow(
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DialogDefaults.performItemClickHaptic(view)
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = tint,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LabeledSectionDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

