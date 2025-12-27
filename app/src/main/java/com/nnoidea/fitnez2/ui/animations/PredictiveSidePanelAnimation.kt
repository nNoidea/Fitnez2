package com.nnoidea.fitnez2.ui.animations

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private class PredictiveSidePanelState {
    var progress by mutableFloatStateOf(0f)
}

private fun Modifier.predictiveSidePanelAnimation(state: PredictiveSidePanelState): Modifier =
        this.graphicsLayer {
            if (state.progress > 0f) {
                val scale = 1f - (state.progress * 0.05f)
                scaleX = scale
                scaleY = scale
                // Slight slide out effect
                translationX = -size.width * state.progress * 0.1f
                shape = RoundedCornerShape(16.dp * state.progress)
                clip = true
            }
        }

@Composable
fun PredictiveSidePanelContainer(
        drawerState: DrawerState,
        scope: CoroutineScope,
        content: @Composable () -> Unit
) {
    val predictiveSidePanelState = remember { PredictiveSidePanelState() }

    PredictiveBackHandler(enabled = drawerState.isOpen) { progress ->
        try {
            progress.collect { backEvent -> predictiveSidePanelState.progress = backEvent.progress }
            scope.launch {
                drawerState.close()
                predictiveSidePanelState.progress = 0f
            }
        } catch (e: CancellationException) {
            predictiveSidePanelState.progress = 0f
        }
    }

    androidx.compose.foundation.layout.Box(
            modifier = Modifier.predictiveSidePanelAnimation(predictiveSidePanelState)
    ) { content() }
}
