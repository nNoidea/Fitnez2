package com.nnoidea.fitnez2.ui.screens.graph

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class SessionPoint(
    val date: Long,
    val maxWeight: Double,
    val totalSets: Int,
    val totalReps: Int,
    val volume: Double = 0.0,
    val isPr: Boolean = false,
    val isLatest: Boolean = false,
    val leftValue: Double = maxWeight,
    val rightValue: Double = maxWeight
)

/**
 * Rounded-corner polygon: every vertex is trimmed along both adjacent edges and
 * rejoined with a quadratic, so all corners share one consistent radius.
 * Pure math over [Offset]s so it stays unit-testable; the Canvas just replays it.
 */
data class CornerOp(val lineTo: Offset, val control: Offset, val end: Offset)
data class RoundedPolygon(val start: Offset, val ops: List<CornerOp>)

fun roundedPolygon(points: List<Offset>, radius: Float): RoundedPolygon {
    data class Trim(val a: Offset, val p: Offset, val b: Offset)
    val trims = points.mapIndexed { i, p ->
        val prev = points[(i - 1 + points.size) % points.size]
        val next = points[(i + 1) % points.size]
        val ePrev = p - prev
        val eNext = next - p
        val lenPrev = ePrev.getDistance()
        val lenNext = eNext.getDistance()
        val r = minOf(radius, lenPrev / 2f, lenNext / 2f).coerceAtLeast(0f)
        val a = if (lenPrev > 0f && r > 0f) p - ePrev / lenPrev * r else p
        val b = if (lenNext > 0f && r > 0f) p + eNext / lenNext * r else p
        Trim(a, p, b)
    }
    val ordered = trims.drop(1) + trims.take(1)
    return RoundedPolygon(
        start = trims[0].b,
        ops = ordered.map { CornerOp(it.a, it.p, it.b) }
    )
}

/**
 * One height label per bar: distinct values each get their own gridline, while
 * equal (or unreadably close, under ~one label height apart) values share one.
 */
fun heightLabelValues(values: List<Double>, minGapFraction: Float = 0.055f): List<Double> {
    if (values.isEmpty()) return emptyList()
    val min = values.min()
    val max = values.max()
    if (max <= min) return listOf(values.first())
    val gap = (max - min) * minGapFraction
    // ponytail: sorted() is O(n log n) on at most 8 in-memory values; nothing to win here.
    return values.sorted().fold(emptyList()) { kept, v ->
        if (kept.isEmpty() || v - kept.last() >= gap) kept + v else kept
    }
}

/**
 * Silhouette corner heights in data units: the first bar's top-left plus every
 * bar's top-right. Labels drawn from these always sit exactly on the drawn silhouette.
 */
fun silhouetteCornerValues(bars: List<SessionPoint>): List<Double> {
    if (bars.isEmpty()) return emptyList()
    return listOf(bars.first().leftValue) + bars.map { it.rightValue }
}

@Composable
fun BatteryStyleChart(
    sessions: List<SessionPoint>,
    weightUnit: String,
    plotValue: (SessionPoint) -> Double = { it.rightValue },
    primaryColor: Color,
    gridColor: Color,
    textColor: Color,
    barTonalColor: Color,
    prColor: Color = adaptiveGold(MaterialTheme.colorScheme.surfaceContainerHigh),
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableFloatStateOf(0f) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()) }
    val fullDateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val totalWidthPx = with(LocalDensity.current) { totalWidth.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sessions) {
                    detectTapGestures(
                        onPress = { selectedIndex = null }
                    )
                }
                .pointerInput(sessions) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset -> touchX = offset.x },
                        onDrag = { change, _ ->
                            change.consume()
                            touchX = change.position.x
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            if (sessions.isEmpty()) return@Canvas

            val corners = silhouetteCornerValues(sessions)
            val minWeight = corners.min()
            val maxWeight = corners.max()

            val paddingLeft = 8.dp.toPx()
            val paddingRight = 42.dp.toPx()
            val paddingTop = 28.dp.toPx()
            val paddingBottom = 32.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom
            val barBottom = paddingTop + chartHeight
            val stubHeight = 10.dp.toPx()

            fun yForFraction(fraction: Float): Float =
                barBottom - (stubHeight + fraction * (chartHeight - stubHeight))

            fun fractionForValue(value: Double): Float =
                if (maxWeight <= minWeight) 1f
                else ((value - minWeight) / (maxWeight - minWeight)).toFloat()

            // Battery-style solid gridlines: one per silhouette corner at its own
            // height (first left edge + every right edge), sharing a line when corners sit at the same level.
            val labelVals = heightLabelValues(corners)
            val maxLabelVal = labelVals.maxOrNull()

            labelVals.forEach { value ->
                val fraction = fractionForValue(value)
                val y = yForFraction(fraction)

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )

                val text = formatMaxTwoDecimals(value)
                val isMax = (value == maxLabelVal)

                val paint = android.graphics.Paint().apply {
                    color = if (isMax) Color(0xFF1C1B1F).hashCode() else textColor.hashCode()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    typeface = if (isMax) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                }

                if (isMax) {
                    val textWidth = paint.measureText(text)
                    val fontMetrics = paint.fontMetrics
                    val pillPaddingH = 5.dp.toPx()
                    val pillPaddingV = 2.dp.toPx()
                    val textBaselineY = y + 4.dp.toPx()
                    val pillRight = width - 8.dp.toPx() + pillPaddingH
                    val pillLeft = pillRight - textWidth - 2 * pillPaddingH
                    val pillTop = textBaselineY + fontMetrics.ascent - pillPaddingV
                    val pillBottom = textBaselineY + fontMetrics.descent + pillPaddingV
                    val pillH = pillBottom - pillTop

                    drawRoundRect(
                        color = prColor,
                        topLeft = Offset(pillLeft, pillTop),
                        size = Size(pillRight - pillLeft, pillH),
                        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    width - 8.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }

            val slotWidth = chartWidth / sessions.size
            val gap = (4.dp.toPx()).coerceAtMost(slotWidth * 0.16f)
            val cornerRadius = 6.dp.toPx()

            // Find closest index if touch active
            if (touchX >= paddingLeft && touchX <= width - paddingRight) {
                val hovered = ((touchX - paddingLeft) / slotWidth).toInt().coerceIn(0, sessions.lastIndex)
                if (selectedIndex != hovered) {
                    selectedIndex = hovered
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }

            // Draw grounded segments with rounded top corners following the slant
            sessions.forEachIndexed { index, session ->
                val x0 = paddingLeft + index * slotWidth + gap / 2f
                val x1 = paddingLeft + (index + 1) * slotWidth - gap / 2f
                val yL = yForFraction(fractionForValue(session.leftValue))
                val yR = yForFraction(fractionForValue(session.rightValue))

                val isHovered = (index == selectedIndex)
                val barColor = when {
                    isHovered -> primaryColor
                    session.isLatest -> primaryColor
                    else -> barTonalColor
                }

                // Consistently rounded on all 4 edges; sharp slants and short stubs just clamp smaller
                val poly = roundedPolygon(
                    listOf(Offset(x0, barBottom), Offset(x0, yL), Offset(x1, yR), Offset(x1, barBottom)),
                    cornerRadius
                )
                val segment = Path().apply {
                    moveTo(poly.start.x, poly.start.y)
                    poly.ops.forEach { op ->
                        lineTo(op.lineTo.x, op.lineTo.y)
                        quadraticTo(op.control.x, op.control.y, op.end.x, op.end.y)
                    }
                    close()
                }
                drawPath(segment, barColor)

                // Baseline tick + date label per segment (at most 7 bars, so label them all)
                val xCenter = (x0 + x1) / 2f
                drawLine(
                    color = textColor.copy(alpha = 0.4f),
                    start = Offset(xCenter, barBottom),
                    end = Offset(xCenter, barBottom + 5.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    dateFormatter.format(Instant.ofEpochMilli(session.date).atZone(ZoneId.systemDefault())),
                    xCenter,
                    height - 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.copy(alpha = 0.65f).hashCode()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // Floating Scrub Tooltip
        selectedIndex?.takeIf { it in sessions.indices }?.let { index ->
            val session = sessions[index]
            val dateStr = fullDateFormatter.format(Instant.ofEpochMilli(session.date).atZone(ZoneId.systemDefault()))

            val paddingLeft = 8.dp
            val paddingRight = 42.dp
            val chartWidthDp = totalWidth - paddingLeft - paddingRight
            val slotWidthDp = chartWidthDp / sessions.size

            val tooltipWidth = 145.dp
            val targetX = paddingLeft + (slotWidthDp * (index + 0.5f)) - (tooltipWidth / 2)
            val clampedX = targetX.coerceIn(8.dp, (totalWidth - tooltipWidth - 8.dp).coerceAtLeast(8.dp))

            Card(
                modifier = Modifier
                    .padding(start = clampedX, top = 2.dp)
                    .width(tooltipWidth)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    val plotted = plotValue(session)
                    val weightFormatted = formatMaxTwoDecimals(plotted)
                    Text(
                        text = "$weightFormatted $weightUnit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${session.totalSets} sets • ${session.totalReps} reps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
