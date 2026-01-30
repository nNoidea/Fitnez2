package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.animateColorAsState

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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    // Use a custom positional threshold (e.g., 40% of the item width)
    // This makes it harder to accidentally trigger the delete action.
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance ->
             // Trigger at 40% of the width. Default is often 56dp which is too small for wide items.
             totalDistance * 0.40f
        }
    )

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else Color.Transparent,
                label = "SwipeBackground"
            )
            
            // Adding a scale animation to the icon for a more "expressive" feel
            // We can determine scale based on progress.
            // SwipeToDismissBoxState exposes progress (0.0 to 1.0).
            // However, progress is only available in newer versions or via targetValue/currentValue.
            // A simpler way for the icon is to animate it appearing.
            
            val scale by animateFloatAsState(
                targetValue = if (state.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 1.0f,
                label = "IconScale"
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = color,
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
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        },
        content = { content() },
        modifier = modifier
    )
}
