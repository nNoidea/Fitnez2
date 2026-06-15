package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.data.entities.Record
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun InteractiveBezierChart(
    records: List<Record>,
    weightUnit: String,
    primaryColor: Color,
    gridColor: Color,
    textColor: Color
) {
    // Interactive drag/hover state
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableFloatStateOf(0f) }

    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(records) {
                    detectTapGestures(
                        onPress = { selectedPointIndex = null }
                    )
                }
                .pointerInput(records) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            touchX = offset.x
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            touchX = change.position.x
                        },
                        onDragEnd = { selectedPointIndex = null },
                        onDragCancel = { selectedPointIndex = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            if (records.isEmpty()) return@Canvas

            val minWeight = records.minOf { it.weight }
            val maxWeight = records.maxOf { it.weight }
            val weightDelta = (maxWeight - minWeight).coerceAtLeast(1.0)

            // Padding boundaries
            val paddingLeft = 40.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 24.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Draw Y-Axis Horizontal Grid Lines
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val fraction = i.toFloat() / gridSteps
                val y = paddingTop + chartHeight * (1f - fraction)

                // Draw dashed grid lines
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Label
                val labelValue = minWeight + fraction * weightDelta
                drawContext.canvas.nativeCanvas.drawText(
                    String.format(Locale.getDefault(), "%.1f", labelValue),
                    8.dp.toPx(),
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 10.sp.toPx()
                    }
                )
            }

            // Map data to coordinates
            val points = records.mapIndexed { index, record ->
                val xFraction = if (records.size > 1) index.toFloat() / (records.size - 1) else 0.5f
                val yFraction =
                    if (weightDelta > 0) (record.weight - minWeight) / weightDelta else 0.5
                Offset(
                    x = paddingLeft + xFraction * chartWidth,
                    y = paddingTop + chartHeight * (1f - yFraction.toFloat())
                )
            }

            // Calculate closest point during user interaction
            if ((touchX >= paddingLeft) && (touchX <= width - paddingRight)) {
                var closestIndex = 0
                var minDistance = Float.MAX_VALUE
                points.forEachIndexed { index, offset ->
                    val distance = abs(offset.x - touchX)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestIndex = index
                    }
                }
                selectedPointIndex = closestIndex
            }

            // Draw Bezier Line Path
            if (points.size > 1) {
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(
                            x1 = controlX, y1 = p0.y,
                            x2 = controlX, y2 = p1.y,
                            x3 = p1.x, y3 = p1.y
                        )
                    }
                }

                // Draw flowing background gradient under the curve
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, paddingTop + chartHeight)
                    lineTo(points.first().x, paddingTop + chartHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = paddingTop,
                        endY = paddingTop + chartHeight
                    )
                )

                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = null
                    )
                )
            } else if (points.size == 1) {
                // If only 1 point, draw a solid point in the center
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = points.first()
                )
            }

            // Draw normal node points
            points.forEachIndexed { index, offset ->
                if (index != selectedPointIndex) {
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                }
            }

            // Draw highlighted selected point
            selectedPointIndex?.let { index ->
                val selectedOffset = points[index]

                // Draw vertical highlight guide line
                drawLine(
                    color = primaryColor.copy(alpha = 0.5f),
                    start = Offset(selectedOffset.x, paddingTop),
                    end = Offset(selectedOffset.x, paddingTop + chartHeight),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw glowing node
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = selectedOffset
                )
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = selectedOffset
                )
            }
        }

        // Float interactive Tooltip composable cleanly over the canvas
        selectedPointIndex?.let { index ->
            val record = records[index]
            val dateStr = formatter.format(Instant.ofEpochMilli(record.date).atZone(ZoneId.systemDefault()))

            val paddingLeftPx = 40.dp
            val paddingRightPx = 16.dp
            val chartWidth = 300.dp - paddingLeftPx - paddingRightPx

            val xFraction = if (records.size > 1) index.toFloat() / (records.size - 1) else 0.5f
            val alignOffsetDp = paddingLeftPx + (chartWidth * xFraction) - 60.dp

            Card(
                modifier = Modifier
                    .padding(start = alignOffsetDp.coerceAtLeast(0.dp), top = 8.dp)
                    .width(135.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${record.weight} $weightUnit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${record.sets} sets x ${record.reps} reps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
