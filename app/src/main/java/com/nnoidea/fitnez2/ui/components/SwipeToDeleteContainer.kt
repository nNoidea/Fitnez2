package com.nnoidea.fitnez2.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
    enableSwipeRight: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = enableSwipeRight,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwipingLeft = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val backgroundColor = if (isSwipingLeft) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwipingLeft) {
                    val iconScale = if (dismissState.progress > 0.4f) 1.2f else 1.0f
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = globalLocalization.labelDelete,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.scale(iconScale)
                    )
                }
            }
        }
    ) {
        content()
    }
}
