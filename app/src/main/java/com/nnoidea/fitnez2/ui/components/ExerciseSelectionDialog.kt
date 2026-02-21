package com.nnoidea.fitnez2.ui.components

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
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.entities.Exercise
import kotlinx.coroutines.launch

@Composable
fun ExerciseSelectionDialog(
    show: Boolean,
    exercises: List<Exercise>,
    selectedExerciseId: Int?,
    exerciseDao: ExerciseDao,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }


    PredictiveModal(
        show = show,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = globalLocalization.labelSelectExercise,
                style = MaterialTheme.typography.headlineSmall
            )

            // List - Sorted alphabetically (case-insensitive)
            val sortedExercises = remember(exercises) {
                exercises.sortedBy { it.name.lowercase() }
            }

            // Custom Scrollbar Logic
            val scrollState = rememberScrollState()
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            var columnHeightPx by remember { mutableFloatStateOf(0f) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .heightIn(max = screenHeight * 0.6f)
                        .padding(end = 4.dp) // Space for scrollbar
                        .onGloballyPositioned { coordinates ->
                             columnHeightPx = coordinates.size.height.toFloat()
                        }
                        .verticalScroll(scrollState)
                ) {

                    // ADDED: Create Button at the top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                showCreateDialog = true 
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
                            text = globalLocalization.labelCreateExercise,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

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
                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
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
                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
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
                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
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

                // Vertical Scrollbar Overlay
                // Show ONLY if reached max height (implied by content overflowing -> maxValue > 0)
                val scrollbarVisible = scrollState.maxValue > 0

                if (scrollbarVisible && columnHeightPx > 0f) {
                    val scrollbarHeight by remember(scrollState.maxValue, columnHeightPx) {
                        derivedStateOf {
                            val viewportHeight = columnHeightPx
                            val contentHeight = viewportHeight + scrollState.maxValue
                            // Ratio: Viewport / Content
                            (viewportHeight * (viewportHeight / contentHeight)).coerceAtLeast(40f) 
                        }
                    }

                    val scrollbarOffset by remember(scrollState.value, scrollState.maxValue, columnHeightPx, scrollbarHeight) {
                        derivedStateOf {
                            val maxScroll = scrollState.maxValue.toFloat()
                            if (maxScroll == 0f) return@derivedStateOf 0f
                            
                            val availableTrack = columnHeightPx - scrollbarHeight
                            val scrollProgress = scrollState.value.toFloat() / maxScroll
                            
                            availableTrack * scrollProgress
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(4.dp)
                            // Track height = Viewport height
                            .height(with(LocalDensity.current) { columnHeightPx.toDp() }) 
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh) // Track color
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(with(LocalDensity.current) { scrollbarHeight.toDp() })
                                .offset(y = with(LocalDensity.current) { scrollbarOffset.toDp() })
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
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
                    exerciseDao.delete(exercise.id)
                    exerciseToDelete = null
                }
            }
        }
    )

    // Edit Dialog
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
                        exerciseDao.update(exercise.copy(name = newName))
                        exerciseToEdit = null
                    } catch (e: Exception) {
                        // Error handling could be added here
                    }
                }
            }
        }
    )

    // Create Dialog
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
                    val newExercise = Exercise(name = newName)
                    exerciseDao.create(newExercise)
                    showCreateDialog = false
                } catch (e: Exception) {
                    // Handle existing name error if needed
                }
            }
        }
    )
}
