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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBottomSheet(
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen
    
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        
        // --- LAYOUT CONSTANTS ---
        // Peek height must be enough to show the input row (~220dp)
        // Drag Handle (30) + Title/Exercise (60) + Inputs (70) + Add Button (60)
        val peekHeight = 270.dp 
        
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
                    
                    // Button 1: Exercise Selector
                    FilledTonalButton(
                        onClick = { /* TODO: Open selection */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = globalLocalization.labelSelectExercise,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
                            value = "", 
                            placeholder = "3",
                            modifier = Modifier.weight(1f)
                        )

                        // Button 3: Reps
                        InputButton(
                            label = globalLocalization.labelReps,
                            value = "", 
                            placeholder = "10",
                            modifier = Modifier.weight(1f)
                        )

                        // Button 4: Weight
                        InputButton(
                            label = globalLocalization.labelWeightWithUnit(globalUiState.weightUnit),
                            value = "", 
                            placeholder = "20",
                            modifier = Modifier.weight(1.2f) // Slightly wider for unit
                        )
                    }

                    // Button 5: Add Button
                    Button(
                        onClick = { /* TODO: Add to DB */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(32.dp) // Fully rounded
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(globalLocalization.labelAddExercise)
                    }
                }

                // 3. Expanded Content (Only Visible when pulled up)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Takes remaining space
                        .padding(top = 24.dp)
                ) {
                   // Placeholder for list
                   Text(
                       text = globalLocalization.labelHistoryListPlaceholder, 
                       modifier = Modifier.align(Alignment.Center),
                       color = MaterialTheme.colorScheme.onSurfaceVariant
                   )
                }
            }
        }
    }
}

@Composable
private fun InputButton(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    // Using OutlinedButton look-alike for inputs to match the requested "Button" style
    // But conceptually these are inputs. 
    // For now, making them look like visual containers.
    
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = {},
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
