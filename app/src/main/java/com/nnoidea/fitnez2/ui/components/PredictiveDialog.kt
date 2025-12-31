package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

/**
 * A highly customizable Dialog with Predictive Back support and smooth animations.
 */
@Composable
fun PredictiveDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allows us to use fillMaxWidth() correctly
            decorFitsSystemWindows = true
        )
    ) {
        val scope = rememberCoroutineScope()
        var predictiveProgress by remember { mutableFloatStateOf(0f) }
        val progressAnim = remember { Animatable(0f) }

        // Handle Predictive Back
        PredictiveBackHandler(enabled = show) { progress ->
            try {
                progress.collect { backEvent ->
                    progressAnim.snapTo(backEvent.progress)
                    predictiveProgress = progressAnim.value
                }
                
                // Commit: Dismiss
                onDismissRequest()
                
                // Reset visual distortion
                progressAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) {
                    predictiveProgress = value
                }
            } catch (e: Exception) {
                progressAnim.animateTo(0f) { predictiveProgress = value }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // Dim background
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() }, // Dismiss on outside click
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = show,
            ) {
                Surface(
                    modifier = modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .graphicsLayer {
                            // Apply visual scale-down during predictive back
                            val scale = 1f - (predictiveProgress * 0.2f)
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(enabled = false) { } // Prevent clicks through to background box
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        content()
                    }
                }
            }
        }
    }
}
