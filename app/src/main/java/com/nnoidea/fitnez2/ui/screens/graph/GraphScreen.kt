package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun GraphScreen(onOpenDrawer: () -> Unit) {
    val exerciseService = LocalExerciseService.current
    val recordService = LocalRecordService.current
    val settingsService = LocalSettingsService.current

    val exercises by exerciseService.getAllExercisesFlow().collectAsState(initial = emptyList())
    val weightUnit by settingsService.weightUnitFlow.collectAsState(initial = "kg")

    var selectedExerciseId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(value = false) }

    // Auto-select first exercise once loaded
    LaunchedEffect(exercises) {
        if (selectedExerciseId == null && exercises.isNotEmpty()) {
            selectedExerciseId = exercises.first().id
        }
    }

    val selectedExercise = exercises.find { it.id == selectedExerciseId }

    // Fetch records reactively for the selected exercise (sorted chronologically ASC for graph mapping)
    val records by remember(selectedExerciseId) {
        selectedExerciseId?.let { recordService.getRecordsByExerciseIdFlow(it) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    ScreenScaffold(
        title = globalLocalization.labelGraph,
        onOpenDrawer = onOpenDrawer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (exercises.isEmpty()) {
                        EmptyPlaceholder(message = globalLocalization.labelNoExercises)
                    } else {
                        // Exercise Dropdown Selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.TopStart)
                                .padding(bottom = 16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = globalLocalization.labelSelectExercise,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = selectedExercise?.name ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                exercises.forEach { exercise ->
                                    DropdownMenuItem(
                                        text = { Text(exercise.name) },
                                        onClick = {
                                            selectedExerciseId = exercise.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (records.isEmpty()) {
                            EmptyPlaceholder(message = globalLocalization.labelNoDataForExercise)
                        } else {
                            // Metrics Cards
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val maxWeight = records.maxOfOrNull { it.weight } ?: 0.0
                                val latestWeight = records.lastOrNull()?.weight ?: 0.0
                                val oldestWeight = records.firstOrNull()?.weight ?: 0.0
                                val delta = latestWeight - oldestWeight

                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = globalLocalization.labelMaxWeight,
                                    value = "$maxWeight $weightUnit",
                                    icon = Icons.Default.Star,
                                    color = Color(0xFFFFB300) // Gold
                                )

                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = globalLocalization.labelCurrentWeight,
                                    value = "$latestWeight $weightUnit",
                                    icon = Icons.Default.Speed,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val isPositive = delta >= 0
                                MetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = globalLocalization.labelProgress,
                                    value = "${if (isPositive) "+" else ""}$delta $weightUnit",
                                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }

                            // Interactive Custom Graph Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    InteractiveBezierChart(
                                        records = records,
                                        weightUnit = weightUnit,
                                        primaryColor = MaterialTheme.colorScheme.primary,
                                        gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.15f
                                        ),
                                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

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

    val formatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

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
            val dateStr = formatter.format(Date(record.date))

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
