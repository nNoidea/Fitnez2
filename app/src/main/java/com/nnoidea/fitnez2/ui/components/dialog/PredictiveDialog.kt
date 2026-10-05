package com.nnoidea.fitnez2.ui.components.dialog

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Foundation dialog shell (Level 1) providing:
 * - Responsive window sizing clamped to [DialogDefaults.MaxWidth] with [DialogDefaults.ScreenMarginHorizontal]
 * - Predictive Back gesture animation scaling
 * - Material 3 container surface, tonal elevation, and expressive corner shape
 * - Scrim dimming with customizable outside-click & back-press dismissal guards
 * - Single-point content padding management (avoiding double-padding)
 */
@Composable
internal fun CorePredictiveDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!show) return

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = remember(windowInfo, density) {
        with(density) { windowInfo.containerSize.height.toDp() }
    }
    val maxHeight = screenHeight * DialogDefaults.MaxHeightRatio

    Dialog(
        onDismissRequest = {
            if (dismissOnClickOutside || dismissOnBackPress) {
                onDismissRequest()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        var predictiveProgress by remember { mutableFloatStateOf(0f) }
        val progressAnim = remember { Animatable(0f) }

        // Handle Predictive Back
        if (dismissOnBackPress) {
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
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Scrim Layer (Independent of IME)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DialogDefaults.ScrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = dismissOnClickOutside
                    ) {
                        if (dismissOnClickOutside) {
                            onDismissRequest()
                        }
                    }
            )

            // 2. Content Layout Layer (Sensitive to IME if enabled)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = modifier
                        .systemBarsPadding()
                        .padding(
                            horizontal = DialogDefaults.ScreenMarginHorizontal,
                            vertical = DialogDefaults.ScreenMarginVertical
                        )
                        .fillMaxWidth()
                        .widthIn(max = DialogDefaults.MaxWidth)
                        .heightIn(max = maxHeight)
                        .wrapContentHeight()
                        .graphicsLayer {
                            val scale = 1f - (predictiveProgress * DialogDefaults.PredictiveScaleFactor)
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(DialogDefaults.Shape)
                        .clickable(enabled = false) { }
                        .background(DialogDefaults.ContainerColor),
                    shape = DialogDefaults.Shape,
                    color = DialogDefaults.ContainerColor,
                    tonalElevation = DialogDefaults.Elevation
                ) {
                    Box(modifier = Modifier.padding(DialogDefaults.ContentPadding)) {
                        content()
                    }
                }
            }
        }
    }
}
