package com.nnoidea.fitnez2.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState

import androidx.compose.ui.platform.LocalContext
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import androidx.compose.runtime.collectAsState
import com.nnoidea.fitnez2.ui.components.ExerciseHistoryList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBottomSheet(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()

        // DB Access
        val database = remember { AppDatabase.getDatabase(context, scope) }
        val exerciseDao = database.exerciseDao()
        val recordDao = database.recordDao()
        
        val dbExercises by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())

        // Form state
        var selectedExerciseId by remember { mutableStateOf<Int?>(null) }
        var selectedExercise by remember { mutableStateOf<String?>(null) }
        var setsValue by remember { mutableStateOf("") }
        var repsValue by remember { mutableStateOf("") }
        var weightValue by remember { mutableStateOf("") }

        // --- LAYOUT CONSTANTS ---
        // Peek height must be enough to show the input row (~160dp)
        // Drag Handle (30) + Title/Exercise/Add (60) + Inputs (70)
        val buttonHeight = 40.dp
        
        // Peek height = DragHandle + TopPadding + ButtonRow1 + Spacing + ButtonRow2 + BottomPadding
        // Drag Handle area is approx 32dp, plus we have 24dp vertical padding around the columns
        val peekHeight = buttonHeight * 2  + 70.dp + 15.dp

        val peekHeightPx = with(density) { peekHeight.toPx() }
        val screenHeightPx = constraints.maxHeight.toFloat()

        // Use full screen height for expanded state
        val expandedHeightPx = screenHeightPx
        val expandedHeight = with(density) { screenHeightPx.toDp() }

        // 0 = Expanded, maxOffset = Collapsed
        val maxOffset = expandedHeightPx - peekHeightPx
        val minOffset = 0f

        val offsetY = remember { Animatable(maxOffset) }

        // Predictive Back State
        var predictiveProgress by remember { mutableFloatStateOf(0f) }

        var showExerciseSelection by remember { mutableStateOf(false) }

        val isExpanded by remember {
            derivedStateOf { offsetY.value < maxOffset / 2 }
        }

        // Draggable State
        val draggableState = rememberDraggableState { delta ->
             if (!isOverlayOpen) {
                 scope.launch {
                     val newOffset = (offsetY.value + delta).coerceIn(minOffset, maxOffset)
                     offsetY.snapTo(newOffset)
                 }
             }
        }

        // Update GlobalUiState for Snackbar positioning
        // If expanded (open), offset is 0 (bottom of screen)
        // If collapsed (peek), offset is peekHeight (above the sheet)
        LaunchedEffect(isExpanded, peekHeight) {
            globalUiState.bottomSheetSnackbarOffset = if (isExpanded) 0.dp else peekHeight
        }

        // Predictive Back Handler
        val progressAnim = remember { Animatable(0f) }
        PredictiveBackHandler(enabled = isExpanded && !isOverlayOpen) { progress ->
            try {
                progress.collect { backEvent ->
                    progressAnim.snapTo(backEvent.progress)
                    predictiveProgress = progressAnim.value
                }
                scope.launch {
                    offsetY.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow))
                }
                scope.launch {
                    progressAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) {
                        predictiveProgress = value
                    }
                }
            } catch (e: Exception) {
                scope.launch {
                     progressAnim.animateTo(0f) { predictiveProgress = value }
                }
            }
        }

        // --- MAIN SHEET CONTAINER ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(expandedHeight)
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .graphicsLayer {
                     if (predictiveProgress > 0f) {
                         val scale = 1f - (predictiveProgress * 0.2f)
                         scaleX = scale
                         scaleY = scale
                         translationY = size.height * predictiveProgress * 0.2f
                     }
                }
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        val targetOffset = if (velocity > 1000f || (velocity >= 0 && offsetY.value > maxOffset / 2)) {
                            maxOffset
                        } else {
                            minOffset
                        }
                        scope.launch {
                            offsetY.animateTo(
                                targetValue = targetOffset,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                            )
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Drag Handle
                BottomSheetDefaults.DragHandle()

                // 2. Persistent Content (Always Visible in Peek)
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
                        // Button 1: Exercise Selector
                        FilledTonalButton(
                            onClick = { showExerciseSelection = true },
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
                                    text = selectedExercise ?: globalLocalization.labelSelectExercise,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        // Button 5: Add Button
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val exerciseId = selectedExerciseId
                                        val sets = setsValue.toIntOrNull()
                                        val reps = repsValue.toIntOrNull()
                                        val weight = weightValue.toDoubleOrNull()

                                        if (exerciseId != null && sets != null && reps != null && weight != null) {
                                            val record = Record(
                                                exerciseId = exerciseId,
                                                sets = sets,
                                                reps = reps,
                                                weight = weight,
                                                date = System.currentTimeMillis()
                                            )
                                            recordDao.create(record)

                                            // Clear form after successful save
                                            selectedExercise = null
                                            selectedExerciseId = null
                                            setsValue = ""
                                            repsValue = ""
                                            weightValue = ""
                                        }
                                    } catch (e: Exception) {
                                        // Handle error - could show toast or snackbar
                                    }
                                }
                            },
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
                        // Button 2: Sets
                        InputButton(
                            label = globalLocalization.labelSets,
                            value = setsValue,
                            placeholder = "3",
                            onValueChange = { setsValue = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )

                        // Button 3: Reps
                        InputButton(
                            label = globalLocalization.labelReps,
                            value = repsValue,
                            placeholder = "10",
                            onValueChange = { repsValue = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )

                        // Button 4: Weight
                        InputButton(
                            label = globalLocalization.labelWeight,
                            value = weightValue,
                            placeholder = "20",
                            onValueChange = { weightValue = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )
                    }


                }

                // 3. Expanded Content (Only Visible when pulled up)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes remaining space
                        .padding(top = 24.dp)
                ) {
                    ExerciseHistoryList(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        PredictiveExerciseSelectionDialog(
            show = showExerciseSelection,
            exercises = dbExercises,
            onDismissRequest = { showExerciseSelection = false },
            onExerciseSelected = { exercise ->
                selectedExercise = exercise.name
                selectedExerciseId = exercise.id
                showExerciseSelection = false
            }
        )
    }
}

@Composable
private fun InputButton(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Using OutlinedButton look-alike for inputs to match the requested "Button" style
    // But conceptually these are inputs.
    // For now, making them look like visual containers.

    // Custom Expressive Input Button
    // Uses BasicTextField to allow arbitrary sizing (e.g. 30dp) without M3 TextField constraints.
    // Behaves like a button that turns into an input.

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animate container color on focus
    val containerColor by animateColorAsState(
        targetValue = if (isFocused) 
            MaterialTheme.colorScheme.secondaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        label = "InputBackground"
    )
    
    val textColor = if (isFocused) 
        MaterialTheme.colorScheme.onSecondaryContainer 
    else 
        MaterialTheme.colorScheme.onSurface

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.labelLarge.copy(
            color = textColor,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(textColor),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerColor, RoundedCornerShape(24.dp))
            ) {
                if (value.isEmpty()) {
                    // Show Label as placeholder when empty for a clean "Button" look
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PredictiveExerciseSelectionDialog(
    show: Boolean,
    exercises: List<Exercise>,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    val globalLocalization = com.nnoidea.fitnez2.core.localization.globalLocalization

    PredictiveDialog(
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

            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(sortedExercises) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise) }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (sortedExercises.isEmpty()) {
                     item {
                         Text(
                             text = "No exercises found",
                             style = MaterialTheme.typography.bodyMedium,
                             modifier = Modifier.padding(16.dp)
                         )
                     }
                }
            }
        }
    }
}
