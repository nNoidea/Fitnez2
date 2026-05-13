package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val BUTTONHEIGHT = 45
const val PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP = 2 * BUTTONHEIGHT + 70 - 9

/**
 * Generic, reusable Predictive Bottom Sheet animation shell.
 *
 * Handles ALL animation concerns:
 * - Drag-to-expand / drag-to-collapse with spring physics
 * - Predictive back gesture (scale + translate)
 * - Nested scroll integration
 * - Snackbar offset syncing
 *
 * The [content] slot receives the full available height via [ColumnScope]
 * and a drag handle is rendered automatically. Place your form fields,
 * lists, or any other UI in the content slot.
 *
 * Usage:
 * ```
 * PredictiveBottomSheet(state = myState) {
 *     MyFormRow(state)
 *     MyExpandedContent(modifier = Modifier.weight(1f))
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBottomSheet(
    state: PredictiveBottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val globalUiState = LocalGlobalUiState.current
    val isOverlayOpen = globalUiState.isOverlayOpen
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val topPaddingPx = with(density) { topPadding.toPx() }
        val expandedHeight = with(density) { constraints.maxHeight.toDp() } - topPadding

        // Sync global UI state for snackbar positioning
        LaunchedEffect(state.isExpanded) {
            globalUiState.bottomSheetSnackbarOffset =
                if (state.isExpanded) 0.dp else PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp
        }

        // Predictive Back Handler
        PredictiveBackHandler(enabled = state.isExpanded && !isOverlayOpen) { progress ->
            try {
                progress.collect { state.onPredictiveBackProgress(it.progress) }
                scope.launch { state.onPredictiveBackCommit() }
            } catch (e: Exception) {
                state.onPredictiveBackCancel()
            }
        }

        // Overshoot buffer so spring can bounce past the edge
        val overshootBuffer = 150.dp
        val sheetTotalHeight = expandedHeight + overshootBuffer

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .height(sheetTotalHeight)
                .fillMaxWidth()
                .offset { IntOffset(0, (state.offsetY.value + topPaddingPx).roundToInt()) }
                .graphicsLayer {
                    if (state.predictiveProgress > 0f) {
                        val scale = 1f - (state.predictiveProgress * 0.2f)
                        scaleX = scale
                        scaleY = scale
                        translationY = size.height * state.predictiveProgress * 0.2f
                    }
                }
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .nestedScroll(state.nestedScrollConnection)
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (!isOverlayOpen) {
                            scope.launch {
                                val newOffset = (state.offsetY.value + delta)
                                    .coerceIn(state.minOffset, state.maxOffset)
                                state.offsetY.snapTo(newOffset)
                            }
                        }
                    },
                    orientation = Orientation.Vertical,
                    onDragStarted = { },
                    onDragStopped = { velocity ->
                        scope.launch { state.settleSpring(velocity) }
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom Drag Handle with halved vertical padding (10.dp instead of standard 20-22.dp)
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                // Screen-specific content goes here
                content()
            }
        }
    }
}
