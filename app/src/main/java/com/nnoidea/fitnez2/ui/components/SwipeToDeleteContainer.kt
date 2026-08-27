package com.nnoidea.fitnez2.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val state = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance ->
            totalDistance * 0.35f
        }
    )

    // Trigger haptic when dragging crosses the deletion threshold ("letting go will delete")
    var hasVibratedForThreshold by remember { mutableStateOf(false) }

    LaunchedEffect(state.targetValue) {
        if (state.targetValue == SwipeToDismissBoxValue.EndToStart) {
            if (!hasVibratedForThreshold) {
                val feedbackConstant = if (android.os.Build.VERSION.SDK_INT >= 34) {
                    HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
                } else {
                    HapticFeedbackConstants.VIRTUAL_KEY
                }
                view.performHapticFeedback(feedbackConstant)
                hasVibratedForThreshold = true
            }
        } else {
            // Reverted back under threshold: reset so dragging back across will vibrate again
            hasVibratedForThreshold = false
        }
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else Color.Transparent,
                label = "SwipeBackground"
            )

            val scale by animateFloatAsState(
                targetValue = if (state.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 1.0f,
                label = "IconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = globalLocalization.labelDelete,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.scale(scale)
                )
            }
        },
        content = { content() },
        modifier = modifier
    )
}
