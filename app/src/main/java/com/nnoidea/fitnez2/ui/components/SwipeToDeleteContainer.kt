package com.nnoidea.fitnez2.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
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
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(enableSwipeRight) {
                detectHorizontalDragGestures(
                    onDragStart = { isTriggered = false },
                    onDragEnd = {
                        scope.launch {
                            val threshold = size.width * 0.35f
                            if (offsetX.value < -threshold) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                offsetX.animateTo(-size.width.toFloat(), spring(stiffness = Spring.StiffnessMedium))
                                onDelete()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium)) }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // Only consume drag when swiping LEFT (or already dragged left)
                        // When swiping RIGHT (dragAmount > 0 and offsetX == 0), we DO NOT consume
                        // so parent components (like ModalNavigationDrawer) can receive the gesture!
                        if (dragAmount < 0 || offsetX.value < 0 || enableSwipeRight) {
                            change.consume()
                            val minOffset = -size.width.toFloat()
                            val maxOffset = if (enableSwipeRight) size.width.toFloat() else 0f
                            val newOffset = (offsetX.value + dragAmount).coerceIn(minOffset, maxOffset)
                            scope.launch { offsetX.snapTo(newOffset) }

                            val threshold = size.width * 0.35f
                            val progress = -newOffset / threshold
                            if (progress >= 1f && !isTriggered) {
                                isTriggered = true
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                            } else if (progress < 1f && isTriggered) {
                                isTriggered = false
                            }
                        }
                    }
                )
            }
    ) {
        if (offsetX.value < 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val scale = (-offsetX.value / 80f).coerceIn(0.8f, 1.2f)
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = globalLocalization.labelDelete,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.scale(scale)
                )
            }
        }

        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            content()
        }
    }
}
