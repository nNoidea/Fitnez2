package com.nnoidea.fitnez2.ui.animations

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CancellationException

class PredictiveRouteState {
    var progress by mutableFloatStateOf(0f)
}

@Composable
fun rememberPredictiveRouteState(): PredictiveRouteState {
    return remember { PredictiveRouteState() }
}

@Composable
fun RoutePredictiveBackHandler(
        predictiveState: PredictiveRouteState,
        navController: NavController,
        enabled: Boolean = true
) {
    PredictiveBackHandler(enabled = enabled) { progress ->
        try {
            progress.collect { backEvent -> predictiveState.progress = backEvent.progress }
            navController.popBackStack()
        } catch (e: CancellationException) {
            predictiveState.progress = 0f
        }
    }
}

fun Modifier.predictiveRouteAnimation(state: PredictiveRouteState): Modifier =
        this.graphicsLayer {
            if (state.progress > 0f) {
                val scale = 1f - (state.progress * 0.1f)
                scaleX = scale
                scaleY = scale

                shape = RoundedCornerShape(24.dp)
                clip = true
            }
        }

@Composable
fun PredictiveRouteBackground(
        state: PredictiveRouteState,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
) {
    if (state.progress > 0f) {
        Box(modifier = modifier) {
            Box(
                    modifier =
                            Modifier.fillMaxSize().graphicsLayer {
                                val progress = state.progress
                                // Anchor pivot to Center so it zooms evenly
                                transformOrigin = TransformOrigin(0.5f, 0.5f)

                                // Scale: Shrink from 1.0 to 0.9
                                val scale = 1f - (0.1f * progress)
                                scaleX = scale
                                scaleY = scale

                                // Shift content to the left
                                translationX = -size.width / 2
                            }
            ) {
                content()

                // Scrim Overlay
                // Darkens the background, fades away as we swipe (reveal more)
                val scrimAlpha = 0.5f * (2f - state.progress)
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = scrimAlpha))
                )
            }
        }
    }
}

fun routeEnterTransition():
        androidx.compose.animation.AnimatedContentTransitionScope<
                androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition =
        {
            androidx.compose.animation.EnterTransition.None
        }

fun routeExitTransition():
        androidx.compose.animation.AnimatedContentTransitionScope<
                androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition =
        {
            androidx.compose.animation.ExitTransition.None
        }

fun routePopEnterTransition():
        androidx.compose.animation.AnimatedContentTransitionScope<
                androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.EnterTransition =
        {
            androidx.compose.animation.EnterTransition.None
        }

fun routePopExitTransition():
        androidx.compose.animation.AnimatedContentTransitionScope<
                androidx.navigation.NavBackStackEntry>.() -> androidx.compose.animation.ExitTransition =
        {
            androidx.compose.animation.ExitTransition.None
        }
