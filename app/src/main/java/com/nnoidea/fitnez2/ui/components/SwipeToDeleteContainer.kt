package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enableSwipeRight: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    
    // Offset state
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    // Thresholds
    // User requested using Screen Width instead of Item Width.
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val dismissThreshold = screenWidthPx * 0.4f
    
    // We still track itemWidth for visual reveal progress if needed, or just remove if unused for logic.
    // But existing code uses itemWidth for revealProgress and target animation (`-itemWidth`).
    // So we keep itemWidth for visuals/animation targets, but use screenWidth for the *Decision* threshold.
    var itemWidth by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemWidth = it.width.toFloat() }
    ) {
        // 1. Background (Red Delete)
        // Only show if we are swiping left (offset < 0)
        val isSwipingLeft by remember { derivedStateOf { offsetX.value < 0 } }

        if (isSwipingLeft) {
             val backgroundColor = MaterialTheme.colorScheme.errorContainer
             val iconScale = if ((-offsetX.value) > dismissThreshold) 1.2f else 1.0f

             Surface(
                 modifier = Modifier.matchParentSize(),
                 color = backgroundColor,
                 contentColor = MaterialTheme.colorScheme.onErrorContainer
             ) {
                 Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .padding(horizontal = 24.dp),
                     contentAlignment = Alignment.CenterEnd
                 ) {
                     Icon(
                         imageVector = Icons.Default.Delete,
                         contentDescription = "Delete",
                         modifier = Modifier.scale(iconScale)
                     )
                 }
             }
        }

        // 2. Content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enableSwipeRight) {
                     awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        
                        var dragStarted = false
                        var hasVibrated = false
                        
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            
                            if (!dragStarted) {
                                val dx = change.position.x - down.position.x
                                val dy = change.position.y - down.position.y
                                
                                if (kotlin.math.abs(dx) > viewConfiguration.touchSlop) {
                                    // Slop passed. Horizontal?
                                    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)*5) {
                                        // Horizontal.
                                        if (dx < 0) {
                                            // Swipe Left -> active!
                                            dragStarted = true
                                            change.consume()
                                        } else {
                                            // Swipe Right -> 
                                            // If enableSwipeRight is true, we track it.
                                            // If false, we IGNORE it (do not consume), effectively letting parent/other components handle it.
                                            if (enableSwipeRight) {
                                                dragStarted = true
                                                change.consume()
                                            } else {
                                                // We treat this as NOT our gesture.
                                                // We break the loop to release control back?
                                                // If we break, we stop processing this gesture stream.
                                                // The event wasn't consumed, so it should propagate up?
                                                // Actually, propagation in Compose is: Children get first chance.
                                                // If child doesn't consume, parent can act (via PointerInputScope).
                                                
                                                // So if we don't consume and break, we effectively allow parent.
                                                break
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Drag IS active, we update offset.
                                if (change.pressed && change.positionChange() != Offset.Zero) {
                                    val dragAmount = change.positionChange().x
                                    val proposed = offsetX.value + dragAmount
                                    
                                    // Constraint: Can't swipe right past 0 if disabled
                                    if (!enableSwipeRight && proposed > 0) {
                                        // Clamp to 0
                                        scope.launch { offsetX.snapTo(0f) }
                                    } else {
                                        // Apply
                                        scope.launch { offsetX.snapTo(proposed) }
                                        change.consume()
                                        
                                        // Haptic Feedback Logic
                                        if ((-proposed) >= dismissThreshold && !hasVibrated) {
                                            // Just crossed threshold
                                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            hasVibrated = true
                                        } else if ((-proposed) < dismissThreshold && hasVibrated) {
                                            // Crossed back
                                            hasVibrated = false
                                        }
                                    }
                                }
                            }
                            
                        } while (event.changes.any { it.pressed })
                        
                        // On Up
                        if (dragStarted) {
                            val finalOffset = offsetX.value
                            if ((-finalOffset) >= dismissThreshold) {
                                // Delete
                                scope.launch {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    // Animate away
                                    // User requested 50% faster animation. Default is usually ~300-400ms.
                                    // using tween(200) ensures a snappy exit.
                                    offsetX.animateTo(
                                        targetValue = -itemWidth,
                                        animationSpec = tween(durationMillis = 50)
                                    )
                                    
                                    // Trigger callback
                                    onDelete()
                                    offsetX.snapTo(0f)
                                }
                            } else {
                                // Reset
                                scope.launch { offsetX.animateTo(0f) }
                            }
                        }
                     }
                }
        ) {
            content()
        }
    }
}
