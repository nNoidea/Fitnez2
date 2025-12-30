package com.nnoidea.fitnez2.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.DrawerValue
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBottomSheet(
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen // Check if drawer or other overlay is open
    
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        
        // Constants (you can make these parameters)
        val peekHeight = 120.dp
        val expandedHeight = 400.dp
        
        val peekHeightPx = with(density) { peekHeight.toPx() }
        val expandedHeightPx = with(density) { expandedHeight.toPx() }
        val screenHeightPx = constraints.maxHeight.toFloat()
        
        // Offset Calculation
        // Anchor points: 0 (Expanded) to (expandedHeight - peekHeight) (Collapsed)
        // We want the sheet to sit at the bottom. 
        // Let's model offset from the "Expanded" position.
        // If offset is 0, it's fully expanded (visible height = expandedHeight).
        // If offset is (expandedHeight - peekHeight), visible height = peekHeight.
        
        val maxOffset = expandedHeightPx - peekHeightPx
        val minOffset = 0f
        
        val offsetY = remember { Animatable(maxOffset) } // Start collapsed
        
        // Predictive Back State
        // Predictive Back State
        var predictiveProgress by remember { mutableFloatStateOf(0f) }
        var showDialog by remember { mutableStateOf(false) }
        
        val isExpanded by remember {
            derivedStateOf { offsetY.value < maxOffset / 2 }
        }

        // Draggable State
        val draggableState = rememberDraggableState { delta ->
             if (!isOverlayOpen) { // Optional: disable drag if overlay is open? Maybe not needed, but good practice.
                 scope.launch {
                     val newOffset = (offsetY.value + delta).coerceIn(minOffset, maxOffset)
                     offsetY.snapTo(newOffset)
                 }
             }
        }

        // Predictive Back Handler
        // Predictive Back Handler
        val progressAnim = remember { Animatable(0f) }
        
        // Disable back handler if an overlay (e.g. side panel) or dialog is open
        PredictiveBackHandler(enabled = isExpanded && !isOverlayOpen && !showDialog) { progress ->
            try {
                progress.collect { backEvent ->
                    progressAnim.snapTo(backEvent.progress)
                    predictiveProgress = progressAnim.value
                }
                // Determine completion
                if (progressAnim.value > 0.1f) {
                     // Commit: Collapse (Layout) AND Revert Distortion (Visual)
                     scope.launch { 
                         offsetY.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow)) 
                     }
                     scope.launch {
                         progressAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) {
                             predictiveProgress = value
                         }
                     }
                } else {
                     // Cancel: Revert Distortion only
                     scope.launch {
                         progressAnim.animateTo(0f) {
                             predictiveProgress = value
                         }
                     }
                }
            } catch (e: Exception) {
                scope.launch {
                     progressAnim.animateTo(0f) {
                         predictiveProgress = value
                     }
                }
            }
        }



        // The Custom Bottom Sheet
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(expandedHeight)
                .fillMaxWidth()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                // Apply Predictive Animation Logic MANUALLY here to combine with drag
                .graphicsLayer {
                     if (predictiveProgress > 0f) {
                         val scale = 1f - (predictiveProgress * 0.1f)
                         scaleX = scale
                         scaleY = scale
                         translationY = size.height * predictiveProgress * 0.1f
                     }
                }
                .background(
                    MaterialTheme.colorScheme.surfaceContainerLow,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        // Snap logic
                        val targetOffset = if (velocity > 1000f || (velocity >= 0 && offsetY.value > maxOffset / 2)) {
                            maxOffset // Collapse
                        } else {
                            minOffset // Expand
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
            // Drag Handle & Content
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                androidx.compose.foundation.layout.Column(
                     horizontalAlignment = Alignment.CenterHorizontally
                ) {
                     Text("${globalLocalization.labelBottomSheetTitle}\n${globalLocalization.labelBottomSheetDesc}")
                     Button(onClick = { showDialog = true }) {
                         Text(globalLocalization.labelSayHello)
                     }
                }
                
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(globalLocalization.labelHelloTitle) },
                        text = { Text(globalLocalization.labelHelloText) },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(globalLocalization.labelOkay)
                            }
                        }
                    )
                }
                }
            }
        }
    }
}
