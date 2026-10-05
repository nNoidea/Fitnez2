package com.nnoidea.fitnez2.ui.screens.graph

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.res.ResourcesCompat
import com.nnoidea.fitnez2.R
import com.nnoidea.fitnez2.ui.theme.GoogleSansFlexRounded
import com.nnoidea.fitnez2.ui.theme.adaptiveGold

/** Variable-font axis matching the theme: fully rounded, weight comes from the typeface. */
private const val ROUNDED_VARIATION = "'ROND' 100"

private fun DrawScope.drawYLabelPills(
    sessions: List<SessionPoint>,
    labelVals: List<Double>,
    yMap: Map<Double, Float>,
    barBottom: Float,
    stubHeight: Float,
    corners: List<Double>,
    maxLabelVal: Double?,
    width: Float,
    paddingLeft: Float,
    paddingRight: Float,
    slotWidth: Float,
    gap: Float,
    prColor: Color,
    labelPillColor: Color,
    onLabelPillColor: Color,
    roundedTypeface: android.graphics.Typeface? = null
) {
    collapseConsecutiveLabels(
        cornerLabelPositions(sessions, paddingLeft, slotWidth, gap).map { corner ->
            corner.copy(value = resolveCanonicalValue(corner.value, labelVals))
        }
    ).forEach { corner ->
        val canonical = corner.value
        val y = yMap[canonical] ?: (barBottom - stubHeight)

        val text = formatMaxTwoDecimals(canonical)
        val hasDistinctPeak = (maxLabelVal != null && corners.min() < corners.max())
        val isMax = hasDistinctPeak && (canonical == maxLabelVal)

        // Same binary choice as the theme: rounded font or system default.
        val paint = android.graphics.Paint().apply {
            color = if (isMax) Color(0xFF1C1B1F).toArgb() else onLabelPillColor.toArgb()
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = roundedTypeface?.let {
                if (isMax) android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD) else it
            } ?: if (isMax) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            if (roundedTypeface != null) fontVariationSettings = ROUNDED_VARIATION
        }

        val pill = centeredLabelPillRect(
            textWidth = paint.measureText(text),
            ascent = paint.fontMetrics.ascent,
            descent = paint.fontMetrics.descent,
            centerX = corner.x,
            lineY = y,
            liftGap = 10.dp.toPx(),
            paddingH = 5.dp.toPx(),
            paddingV = 2.dp.toPx(),
            minLeft = paddingLeft,
            maxRight = width - paddingRight
        )
        val pillH = pill.height

        drawRoundRect(
            color = if (isMax) prColor else labelPillColor,
            topLeft = Offset(pill.left, pill.top),
            size = Size(pill.right - pill.left, pillH),
            cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
        )

        drawContext.canvas.nativeCanvas.drawText(
            text,
            (pill.left + pill.right) / 2f,
            pill.bottom - 2.dp.toPx() - paint.fontMetrics.descent,
            paint
        )
    }
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

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d/M", Locale.getDefault()) }
    val fullDateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()) }
    val labelPillColor = MaterialTheme.colorScheme.tertiaryContainer
    val onLabelPillColor = MaterialTheme.colorScheme.onTertiaryContainer
    // Same binary choice as the theme: rounded font or system default.
    val context = LocalContext.current
    val useRoundedFont = MaterialTheme.typography.bodyLarge.fontFamily == GoogleSansFlexRounded
    val roundedTypeface = remember(useRoundedFont) {
        if (useRoundedFont) ResourcesCompat.getFont(context, R.font.google_sans_flex) else null
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth

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

            val paddingLeft = 8.dp.toPx()
            val paddingRight = 8.dp.toPx()
            val paddingTop = 28.dp.toPx()
            val paddingBottom = 32.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom
            val barBottom = paddingTop + chartHeight
            val stubHeight = 10.dp.toPx()

            val labelVals = heightLabelValues(corners)
            val maxLabelVal = labelVals.maxOrNull()

            val yMap = computeNonOverlappingY(
                distinctValues = labelVals,
                yTop = paddingTop,
                yBottom = barBottom - stubHeight,
                minGapPx = 18.dp.toPx()
            )

            // Battery-style solid gridlines: one per distinct silhouette corner,
            // with guaranteed minimum gap so close values cleanly stack under each other without overlap.
            labelVals.forEach { value ->
                val y = yMap[value] ?: (barBottom - stubHeight)

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
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
                val yL = yMap[resolveCanonicalValue(session.leftValue, labelVals)] ?: (barBottom - stubHeight)
                val yR = yMap[resolveCanonicalValue(session.rightValue, labelVals)] ?: (barBottom - stubHeight)

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
            }

            // Y labels sit directly on the graph: each gets a tiny pill backdrop
            // (gold for the peak, tonal otherwise) so it stays readable over bars.
            drawYLabelPills(
                sessions = sessions,
                labelVals = labelVals,
                yMap = yMap,
                barBottom = barBottom,
                stubHeight = stubHeight,
                corners = corners,
                maxLabelVal = maxLabelVal,
                width = width,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
                slotWidth = slotWidth,
                gap = gap,
                prColor = prColor,
                labelPillColor = labelPillColor,
                onLabelPillColor = onLabelPillColor,
                roundedTypeface = roundedTypeface
            )

            // Baseline ticks + date labels at bar corners and junctions
            data class DatePoint(val x: Float, val timestamp: Long, val align: android.graphics.Paint.Align)
            val datePoints = if (sessions.size == 1 && sessions[0].startDate == sessions[0].date) {
                listOf(
                    DatePoint(
                        x = paddingLeft + slotWidth / 2f,
                        timestamp = sessions[0].date,
                        align = android.graphics.Paint.Align.CENTER
                    )
                )
            } else {
                buildList {
                    // Leftmost corner of first bar
                    add(
                        DatePoint(
                            x = paddingLeft + gap / 2f,
                            timestamp = sessions.first().startDate,
                            align = android.graphics.Paint.Align.LEFT
                        )
                    )
                    // Junctions between adjacent bars
                    for (i in 1 until sessions.size) {
                        add(
                            DatePoint(
                                x = paddingLeft + i * slotWidth,
                                timestamp = sessions[i].startDate,
                                align = android.graphics.Paint.Align.CENTER
                            )
                        )
                    }
                    // Rightmost corner of last bar
                    add(
                        DatePoint(
                            x = paddingLeft + sessions.size * slotWidth - gap / 2f,
                            timestamp = sessions.last().date,
                            align = android.graphics.Paint.Align.RIGHT
                        )
                    )
                }
            }

            datePoints.forEach { pt ->
                drawLine(
                    color = textColor.copy(alpha = 0.4f),
                    start = Offset(pt.x, barBottom),
                    end = Offset(pt.x, barBottom + 5.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    dateFormatter.format(Instant.ofEpochMilli(pt.timestamp).atZone(ZoneId.systemDefault())),
                    pt.x,
                    height - 6.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.copy(alpha = 0.65f).toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = pt.align
                        typeface = roundedTypeface ?: android.graphics.Typeface.DEFAULT
                        if (roundedTypeface != null) fontVariationSettings = ROUNDED_VARIATION
                    }
                )
            }
        }

        // Floating Scrub Tooltip
        selectedIndex?.takeIf { it in sessions.indices }?.let { index ->
            val session = sessions[index]
            val dateStr = fullDateFormatter.format(Instant.ofEpochMilli(session.date).atZone(ZoneId.systemDefault()))

            val paddingLeft = 8.dp
            val paddingRight = 8.dp
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
