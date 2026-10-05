package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.theme.OnSwipeDeleteContainer
import com.nnoidea.fitnez2.ui.theme.SwipeDeleteContainer
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Creates a rounded corner shape whose radius dynamically adapts to the container's height,
 * capped at [maxRadius] (default 28.dp).
 *
 * To avoid "fully circled sides" (capsule/stadium pill where radius >= height / 2),
 * the corner radius is constrained to at most [heightRatio] (default 1/3 = 33.3%) of the height.
 * - Standard/tall cards (height >= 84dp): uses the full [maxRadius] (28dp).
 * - Compact cards/rows (e.g. height ~ 48dp): scales down proportionally (16dp),
 *   leaving a clean flat vertical edge of at least 1/3 of the height on the sides.
 */
fun heightDependentCornerShape(
    maxRadius: Dp = 28.dp,
    heightRatio: Float = 1f / 3f
): Shape = RoundedCornerShape(
    corner = object : CornerSize {
        override fun toPx(shapeSize: Size, density: Density): Float {
            if (shapeSize.height <= 0f) return 0f
            val maxRadiusPx = with(density) { maxRadius.toPx() }
            val proportionalRadius = shapeSize.height * heightRatio
            return minOf(maxRadiusPx, proportionalRadius)
        }

        override fun toString(): String = "HeightDependentCornerShape(maxRadius=$maxRadius, ratio=$heightRatio)"
    }
)

/**
 * Material 3 Expressive swipe-to-delete container with Android 16 slider physics.
 *
 * Implements:
 * 1. Continuous tactile ticks as the finger slides, like volume/brightness sliders.
 * 2. Threshold POP vibration and spring icon bounce when crossing the trigger point.
 * 3. Finger-down safety: The card is NEVER deleted while held; deletion only commits on release.
 * 4. Clocking it back: Sliding back right before letting go resets the POP and springs the card back cleanly without deleting.
 * 5. Full spring physics on cancellation and smooth sliding dismissal on commit.
 * 6. Contained action card sized accurately via matchParentSize().
 */
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = heightDependentCornerShape(28.dp),
    containerColor: Color = SwipeDeleteContainer,
    iconColor: Color = OnSwipeDeleteContainer,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    val thresholdPx = if (containerWidthPx > 0f) {
        containerWidthPx * SwipeHapticsConfig.thresholdFraction
    } else {
        with(density) { 120.dp.toPx() }
    }

    val isPastThreshold = abs(offsetX.value) >= thresholdPx

    // -------------------------------------------------------------------------
    // Crisp Icon Resolution & Scaling Conversion:
    // In Compose, scaling an icon beyond 1.0f via graphicsLayer/scale stretches
    // the GPU's rasterized texture, resulting in a blurry icon at 2x.
    //
    // To ensure 100% vector crispness:
    // 1. upscaleCrispnessFactor (e.g. 2x): We allocate layout bounds at 2x (48dp).
    // 2. targetVisualScale (1x idle, 2x popped): What the user sees on screen.
    // 3. Post-Math (targetVisualScale / upscaleCrispnessFactor):
    //    - 1x visual -> 1.0f / 2.0f = 0.5f on 48dp = 24dp (crisp, supersampled)
    //    - 2x visual -> 2.0f / 2.0f = 1.0f on 48dp = 48dp (crisp 1:1 vector resolution, no blur)
    // -------------------------------------------------------------------------
    val baseIconSize = 24.dp
    val upscaleCrispnessFactor = 2.0f
    val renderedIconSize = baseIconSize * upscaleCrispnessFactor // 48.dp base layout bounds

    val targetVisualScale = if (isPastThreshold) 2.0f else 1.25f
    val targetGpuScale = targetVisualScale / upscaleCrispnessFactor

    val animatedGpuScale by animateFloatAsState(
        targetValue = targetGpuScale,
        animationSpec = if (isPastThreshold) {
            // Scale-up: 2x faster spring pop (stiffness quadrupled from 300f to 1200f for 2x speed)
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow * 4f
            )
        } else {
            // Scale-down: relaxed, slower spring back to 1x
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "DeleteIconScale"
    )

    var lastNotch by remember { mutableIntStateOf(0) }
    var lastTickTimeMs by remember { mutableLongStateOf(0L) }
    var hasVibratedForThreshold by remember { mutableStateOf(false) }

    val dragModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = {
                val currentIntervalPx = with(density) {
                    SwipeHapticsConfig.tickIntervalDp.toPx().coerceAtLeast(4f)
                }
                lastNotch = (abs(offsetX.value) / currentIntervalPx).toInt()
                lastTickTimeMs = 0L
            },
            onDragEnd = {
                val maxDrag = if (containerWidthPx > 0f) containerWidthPx else 1000f
                val currentThresholdPx = if (containerWidthPx > 0f) {
                    containerWidthPx * SwipeHapticsConfig.thresholdFraction
                } else {
                    with(density) { 120.dp.toPx() }
                }
                coroutineScope.launch {
                    if (abs(offsetX.value) >= currentThresholdPx) {
                        // Release-to-commit: User lifted finger past threshold -> slide away and delete!
                        offsetX.animateTo(
                            targetValue = -maxDrag,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                        )
                        onDelete()
                        offsetX.snapTo(0f)
                    } else {
                        // "Clocked it back" or released before threshold: spring back to 0!
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    hasVibratedForThreshold = false
                }
            },
            onDragCancel = {
                coroutineScope.launch {
                    offsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    hasVibratedForThreshold = false
                }
            },
            onHorizontalDrag = { change, dragAmount ->
                val maxDrag = if (containerWidthPx > 0f) containerWidthPx else 1000f
                val newOffset = (offsetX.value + dragAmount).coerceIn(-maxDrag, 0f)
                if (newOffset != offsetX.value || dragAmount < 0f) {
                    change.consume()
                    coroutineScope.launch {
                        offsetX.snapTo(newOffset)
                    }

                    val currentThresholdPx = if (containerWidthPx > 0f) {
                        containerWidthPx * SwipeHapticsConfig.thresholdFraction
                    } else {
                        with(density) { 120.dp.toPx() }
                    }
                    val currentIntervalPx = with(density) {
                        SwipeHapticsConfig.tickIntervalDp.toPx().coerceAtLeast(4f)
                    }

                    val isOver = abs(newOffset) >= currentThresholdPx

                    // 1. Threshold POP and clock-back reset
                    if (isOver && !hasVibratedForThreshold) {
                        SwipeHapticsConfig.performPop(context, view)
                        hasVibratedForThreshold = true
                        lastNotch = (abs(newOffset) / currentIntervalPx).toInt()
                    } else if (!isOver && hasVibratedForThreshold) {
                        SwipeHapticsConfig.performClockBack(context)
                        hasVibratedForThreshold = false
                        lastNotch = (abs(newOffset) / currentIntervalPx).toInt()
                    } else if (!isOver) {
                        // 2. High-frequency micro-vibration notches before threshold
                        val currentNotch = (abs(newOffset) / currentIntervalPx).toInt()
                        if (currentNotch != lastNotch) {
                            val now = android.os.SystemClock.uptimeMillis()
                            // Minimum 16ms between ticks so LRA reverse-phase active braking waveform executes cleanly
                            if (now - lastTickTimeMs >= 16L) {
                                SwipeHapticsConfig.performSliderTick(context, view)
                                lastTickTimeMs = now
                                lastNotch = currentNotch
                            }
                        }
                    }
                }
            }
        )
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
            .then(dragModifier)
    ) {
        // Trailing background action card sized to match the parent card exactly
        val isSwipingLeft = offsetX.value < -0.5f
        val revealWidth = if (isSwipingLeft) with(density) { (-offsetX.value).toDp() } else 0.dp
        val containerAlpha = if (isSwipingLeft && revealWidth > 0.dp) 1f else 0f

        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .graphicsLayer(alpha = containerAlpha)
                    .fillMaxHeight()
                    .width(revealWidth),
                shape = shape,
                color = containerColor
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val iconAlpha = if (revealWidth > 0.dp) (revealWidth / 48.dp).coerceIn(0f, 1f) else 0f
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = globalLocalization.labelDelete,
                        tint = iconColor,
                        modifier = Modifier
                            .size(renderedIconSize)
                            .graphicsLayer(alpha = iconAlpha)
                            .scale(animatedGpuScale)
                    )
                }
            }
        }

        // Foreground content sliding with offsetX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(offsetX.value.roundToInt(), 0)
                }
        ) {
            content()
        }
    }
}
