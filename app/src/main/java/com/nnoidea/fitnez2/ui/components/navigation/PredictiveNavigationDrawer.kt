package com.nnoidea.fitnez2.ui.components.navigation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Stable
class PredictiveDrawerState(
    initialValue: DrawerValue = DrawerValue.Closed
) {
    var drawerWidthPx by mutableFloatStateOf(0f)
    val offsetX = Animatable(if (initialValue == DrawerValue.Open) 0f else -1000f)

    val isOpen: Boolean get() = offsetX.value > -drawerWidthPx * 0.5f
    val isClosed: Boolean get() = !isOpen

    val currentValue: DrawerValue get() = if (isOpen) DrawerValue.Open else DrawerValue.Closed
    val targetValue: DrawerValue get() = if (offsetX.targetValue > -drawerWidthPx * 0.5f) DrawerValue.Open else DrawerValue.Closed

    val progress: Float
        get() {
            if (drawerWidthPx <= 0f) return if (isOpen) 1f else 0f
            return (1f + offsetX.value / drawerWidthPx).coerceIn(0f, 1f)
        }

    suspend fun open() {
        offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
    }

    suspend fun close() {
        offsetX.animateTo(-drawerWidthPx, spring(stiffness = Spring.StiffnessMediumLow))
    }

    suspend fun snapTo(value: DrawerValue) {
        offsetX.snapTo(if (value == DrawerValue.Open) 0f else -drawerWidthPx)
    }

    suspend fun settle(velocity: Float) {
        val target = if (velocity > 800f || (velocity >= -400f && offsetX.value > -drawerWidthPx * 0.65f)) {
            0f // Settle Open
        } else {
            -drawerWidthPx // Settle Closed
        }
        offsetX.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
    }
}

@Composable
fun rememberPredictiveDrawerState(
    initialValue: DrawerValue = DrawerValue.Closed
): PredictiveDrawerState {
    return remember { PredictiveDrawerState(initialValue) }
}

/**
 * A smooth, progressive navigation drawer container.
 *
 * Provides true 1-to-1 finger tracking for rightward swipes across the entire screen
 * (including over cards, lists, and empty space) without blocking child taps or vertical scrolling.
 */
@Composable
fun PredictiveNavigationDrawer(
    drawerState: PredictiveDrawerState,
    gesturesEnabled: Boolean = true,
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val viewConfig = LocalViewConfiguration.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = with(density) { 320.dp.toPx() }

        LaunchedEffect(widthPx) {
            drawerState.drawerWidthPx = widthPx
            if (drawerState.isClosed && drawerState.offsetX.value < -widthPx) {
                drawerState.offsetX.snapTo(-widthPx)
            }
        }

        val touchSlop = viewConfig.touchSlop

        // Content slot with gesture detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (gesturesEnabled) {
                        Modifier.pointerInput(gesturesEnabled, widthPx) {
                            awaitEachGesture {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                                val tracker = VelocityTracker()
                                tracker.addPosition(down.uptimeMillis, down.position)
                                var isDragging = false
                                val startPos = down.position

                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull() ?: break
                                    tracker.addPosition(change.uptimeMillis, change.position)

                                    val totalDx = change.position.x - startPos.x
                                    val totalDy = change.position.y - startPos.y

                                    if (!isDragging) {
                                        if (drawerState.isClosed) {
                                            // Detect rightward swipe: horizontal movement > slop and horizontally dominant
                                            if (totalDx > touchSlop && totalDx > abs(totalDy) * 1.15f) {
                                                isDragging = true
                                                change.consume()
                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                                            }
                                        } else if (drawerState.isOpen) {
                                            // Detect leftward swipe to close
                                            if (totalDx < -touchSlop && abs(totalDx) > abs(totalDy) * 1.15f) {
                                                isDragging = true
                                                change.consume()
                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                                            }
                                        }
                                    }

                                    if (isDragging) {
                                        val delta = change.position.x - change.previousPosition.x
                                        val newOffset = (drawerState.offsetX.value + delta).coerceIn(-widthPx, 0f)
                                        scope.launch { drawerState.offsetX.snapTo(newOffset) }
                                        change.consume()
                                    }

                                    if (!change.pressed && change.previousPressed) {
                                        if (isDragging) {
                                            val velocity = tracker.calculateVelocity().x
                                            scope.launch { drawerState.settle(velocity) }
                                        }
                                        break
                                    }
                                }
                            }
                        }
                    } else Modifier
                )
        ) {
            content()
        }

        // Scrim overlay
        if (drawerState.progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = drawerState.progress * 0.45f }
                    .background(Color.Black)
                    .clickable(
                        enabled = drawerState.isOpen,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch { drawerState.close() }
                    }
            )
        }

        // Side Panel Drawer Sheet
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(320.dp)
                .offset { IntOffset(drawerState.offsetX.value.roundToInt(), 0) }
        ) {
            drawerContent()
        }
    }
}
