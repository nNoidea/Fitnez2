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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.BasicTextField


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import androidx.compose.ui.geometry.Offset



import androidx.compose.ui.platform.LocalContext
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import androidx.compose.runtime.collectAsState
import com.nnoidea.fitnez2.ui.components.ExerciseHistoryList
import com.nnoidea.fitnez2.data.SettingsRepository

const val BUTTONHEIGHT = 45
const val PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP = 2*BUTTONHEIGHT + 70 + 15 // Approximately 40*2 + 70 + 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBottomSheet(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen
    val keyboardController = LocalSoftwareKeyboardController.current

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()

        // DB Access
        val database = remember { AppDatabase.getDatabase(context, scope) }
        val exerciseDao = database.exerciseDao()
        val recordDao = database.recordDao()
        val settingsRepository = remember { SettingsRepository(context) }
        
        val dbExercises by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())
        val weightUnit by settingsRepository.weightUnitFlow.collectAsState(initial = "kg")

        // Form state
        var selectedExerciseId by remember { mutableStateOf<Int?>(null) }
        var selectedExercise by remember { mutableStateOf<String?>(null) }
        var setsValue by remember { mutableStateOf("") }
        var repsValue by remember { mutableStateOf("") }
        var weightValue by remember { mutableStateOf("") }



        // Prefill with latest record
        LaunchedEffect(Unit) {
            val latest = recordDao.getLatestRecord()
            if (latest != null) {
                selectedExercise = latest.exerciseName
                selectedExerciseId = latest.record.exerciseId
                setsValue = latest.record.sets.toString()
                repsValue = latest.record.reps.toString()
                // Remove trailing .0 for cleaner display
                weightValue = latest.record.weight.toString().removeSuffix(".0")
            }
        }

        // --- GHOST VALUES (Placeholder Fallbacks) ---
        // These track the last valid inputs to use as fallbacks when fields are empty (e.g. during focus)
        var setsGhost by remember { mutableStateOf("") }
        var repsGhost by remember { mutableStateOf("") }
        var weightGhost by remember { mutableStateOf("") }

        LaunchedEffect(setsValue) { if (setsValue.isNotEmpty()) setsGhost = setsValue }
        LaunchedEffect(repsValue) { if (repsValue.isNotEmpty()) repsGhost = repsValue }
        LaunchedEffect(weightValue) { if (weightValue.isNotEmpty()) weightGhost = weightValue }

        // --- LAYOUT CONSTANTS ---
        // Peek height must be enough to show the input row (~160dp)
        // Drag Handle (30) + Title/Exercise/Add (60) + Inputs (70)
        val buttonHeight = BUTTONHEIGHT.dp
        
        // Peek height = DragHandle + TopPadding + ButtonRow1 + Spacing + ButtonRow2 + BottomPadding
        // Drag Handle area is approx 32dp, plus we have 24dp vertical padding around the columns
        val peekHeight = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp

        val peekHeightPx = with(density) { peekHeight.toPx() }
        val screenHeightPx = constraints.maxHeight.toFloat()
        
        // Respect status bar insets
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topPaddingPx = with(density) { topPadding.toPx() }

        // Use full screen height minus status bar for expanded state
        val expandedHeightPx = screenHeightPx - topPaddingPx
        val expandedHeight = with(density) { expandedHeightPx.toDp() }

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

        // Unified Settle Logic
        val settleSpring: suspend (Float) -> Unit = { velocity ->
            val targetOffset = if (velocity > 1000f || (velocity >= 0 && offsetY.value > maxOffset / 2)) {
                maxOffset // Collapse
            } else {
                minOffset // Expand
            }
            offsetY.animateTo(
                targetValue = targetOffset,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            )
        }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    // Dragging UP (delta < 0) and not yet fully expanded -> Consume
                    if (delta < 0 && offsetY.value > minOffset + 1f) {
                        draggableState.dispatchRawDelta(delta)
                        return available
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    // Dragging DOWN (delta > 0)
                    // Only allow if it's a direct user drag, NOT a fling
                    if (delta > 0 && source == NestedScrollSource.Drag) {
                        draggableState.dispatchRawDelta(delta)
                        return available
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (offsetY.value > minOffset + 1f && offsetY.value < maxOffset - 1f) {
                        settleSpring(available.y)
                        return available
                    }
                    return super.onPreFling(available)
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    return super.onPostFling(consumed, available)
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
                .nestedScroll(nestedScrollConnection)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        scope.launch { settleSpring(velocity) }
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

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                scope.launch {
                                    try {
                                        val exerciseId = selectedExerciseId
                                        val sets = setsValue.ifEmpty { setsGhost }.toIntOrNull()
                                        val reps = repsValue.ifEmpty { repsGhost }.toIntOrNull()
                                        val weight = weightValue.ifEmpty { weightGhost }.toDoubleOrNull()

                                        if (exerciseId != null && 
                                            sets != null && sets > 0 && 
                                            reps != null && reps > 0 && 
                                            weight != null) {
                                            val record = Record(
                                                exerciseId = exerciseId,
                                                sets = sets,
                                                reps = reps,
                                                weight = weight,
                                                date = System.currentTimeMillis()
                                            )
                                            recordDao.create(record)
                                            globalUiState.emitSignal(UiSignal.ScrollToTop)

                                            // Form values are preserved for multiple entries


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
                            onValueChange = { setsValue = it },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )

                        // Button 3: Reps
                        InputButton(
                            label = globalLocalization.labelReps,
                            value = repsValue,
                            onValueChange = { repsValue = it },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )

                        // Button 4: Weight
                        InputButton(
                            label = weightUnit,
                            value = weightValue,
                            onValueChange = { weightValue = it },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight),
                            keyboardType = KeyboardType.Decimal,
                            validate = { text ->
                                text.isEmpty() || text == "-" || text.toDoubleOrNull() != null
                            }
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
            exerciseDao = exerciseDao,
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
    onValueChange: (String) -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    validate: (String) -> Boolean = { it.all { char -> char.isDigit() } }
) {
    com.nnoidea.fitnez2.ui.common.SmartInputLogic(
        value = value,
        onValueChange = onValueChange,
        validate = validate 
    ) { displayValue, placeholder, interactionSource, onWrappedValueChange ->
        
        BasicTextField(
            value = displayValue,
            onValueChange = onWrappedValueChange,
            modifier = modifier
                .background(containerColor, RoundedCornerShape(24.dp)),
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = contentColor,
                textAlign = TextAlign.Start
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(contentColor),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    Text(
                        text = " | ",
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (displayValue.isEmpty()) {
                             Text(
                                text = placeholder.ifEmpty { " " }, 
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = contentColor.copy(alpha = 0.5f)
                                )
                             )
                        }
                        innerTextField()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
private fun PredictiveExerciseSelectionDialog(
    show: Boolean,
    exercises: List<Exercise>,
    exerciseDao: com.nnoidea.fitnez2.data.dao.ExerciseDao,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit
) {
    val globalLocalization = com.nnoidea.fitnez2.core.localization.globalLocalization

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
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    sortedExercises.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExerciseSelected(exercise) }
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { 
                                    exerciseToEdit = exercise
                                },
                            ) {
                                Icon(
                                    Icons.Default.Edit, 
                                    contentDescription = globalLocalization.labelEdit(exercise.name),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                            
                            IconButton(
                                onClick = { exerciseToDelete = exercise },
                            ) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = globalLocalization.labelDelete,
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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


