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
    showCreateWorkout: Boolean = true
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
        
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        onDismissRequest()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra(MainActivity.EXTRA_PAGE_ROUTE, AppPage.Workout.route)
                        }
                        context.startActivity(intent)
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = globalLocalization.labelWorkout,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ADDED: Create Button at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                    showCreateDialog = true 
                }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = globalLocalization.labelExercise,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
        // Labeled divider: "Workouts" (only if workouts exist)
        if (sortedWorkouts.isNotEmpty()) {
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
                    text = globalLocalization.labelWorkouts,
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

        if (sortedWorkouts.isNotEmpty()) {
            sortedWorkouts.forEach { workout ->
                val isSelected = workout.id == selectedWorkoutId
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                            onWorkoutSelected(workout)
                        }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (workoutService != null) {
                        IconButton(
                            onClick = { 
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                                onWorkoutEdit(workout)
                            },
                        ) {
                            Icon(
                                Icons.Default.Edit, 
                                contentDescription = globalLocalization.labelEdit(workout.name),
                                tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { 
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                                workoutToDelete = workout 
                            },
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

        }

        // Labeled divider: "Exercises" (only if exercises exist)
        if (sortedExercises.isNotEmpty()) {
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
                    text = globalLocalization.labelExercises,
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

        sortedExercises.forEach { exercise ->
            val isSelected = exercise.id == selectedExerciseId
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(containerColor, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        onExerciseSelected(exercise)
                    }
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        exerciseToEdit = exercise
                    },
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = globalLocalization.labelEdit(exercise.name),
                        tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        exerciseToDelete = exercise 
                    },
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

    DeleteExerciseDialog(
        show = exerciseToDelete != null,
        onDismiss = { exerciseToDelete = null },
        onConfirmDelete = { exercise ->
            scope.launch {
                exerciseService.deleteExercise(exercise.id)
                exerciseToDelete = null
            }
        },
        exercise = exerciseToDelete
    )

    EditExerciseDialog(
        show = exerciseToEdit != null,
        exercise = exerciseToEdit,
        onDismiss = { exerciseToEdit = null },
        onConfirmEdit = { exercise, newName ->
            scope.launch {
                try {
                    exerciseService.updateExercise(exercise.id, newName)
                    exerciseToEdit = null
                } catch (_: Exception) {
                }
            }
        }
    )

    DeleteWorkoutDialog(
        show = workoutToDelete != null,
        onDismiss = { workoutToDelete = null },
        onConfirmDelete = { workout ->
            scope.launch {
                workoutService?.deleteWorkout(workout)
                workoutToDelete = null
            }
        },
        workout = workoutToDelete
    )

    val createDialogContext = LocalContext.current
    CreateExerciseDialog(
        show = showCreateDialog,
        onDismiss = { showCreateDialog = false },
        onConfirmCreate = { newName ->
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
