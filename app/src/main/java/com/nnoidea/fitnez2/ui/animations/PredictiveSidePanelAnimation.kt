package com.nnoidea.fitnez2.ui.animations

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PredictiveSidePanelState {
    var progress by mutableFloatStateOf(0f)
}

@Composable
fun rememberPredictiveSidePanelState(): PredictiveSidePanelState {
    return remember { PredictiveSidePanelState() }
}

@Composable
fun SidePanelPredictiveBackHandler(
        predictiveState: PredictiveSidePanelState,
        drawerState: DrawerState,
        scope: CoroutineScope = rememberCoroutineScope()
) {
    PredictiveBackHandler(enabled = drawerState.isOpen) { progress ->
        try {
            progress.collect { backEvent -> predictiveState.progress = backEvent.progress }
            scope.launch {
                drawerState.close()
                predictiveState.progress = 0f
            }
        } catch (e: CancellationException) {
            predictiveState.progress = 0f
        }
    }
}

fun Modifier.predictiveSidePanelAnimation(state: PredictiveSidePanelState): Modifier =
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
    val predictiveSidePanelState = rememberPredictiveSidePanelState()

    SidePanelPredictiveBackHandler(
            predictiveState = predictiveSidePanelState,
            drawerState = drawerState,
            scope = scope
    )

    androidx.compose.foundation.layout.Box(
            modifier = Modifier.predictiveSidePanelAnimation(predictiveSidePanelState)
    ) { content() }
}
