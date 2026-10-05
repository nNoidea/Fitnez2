package com.nnoidea.fitnez2.ui.screens.graph

import android.view.HapticFeedbackConstants
import com.nnoidea.fitnez2.ui.theme.adaptiveGold
import com.nnoidea.fitnez2.ui.theme.adaptiveGreen
import com.nnoidea.fitnez2.ui.theme.adaptiveRed
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.SnackbarHost
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.ui.components.dialog.RadioSelectionDialog
import com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheet
import com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState
import com.nnoidea.fitnez2.ui.screens.timeline.rememberHomeBottomSheetState
import kotlinx.coroutines.launch

@Composable
fun DirectionalTrendingArrow(
    direction: ArrowDirection,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeW = 3.5.dp.toPx()
        val wingLen = 10.dp.toPx()
        val wingAngle = 0.62f // ~35.5 degrees

        val startX = 2.dp.toPx()
        val endX = size.width - 2.dp.toPx()

        val (startY, endY) = when (direction) {
            ArrowDirection.NEUTRAL -> Pair(size.height / 2f, size.height / 2f)
            ArrowDirection.DOWN -> Pair(4.dp.toPx(), size.height - 4.dp.toPx())
            ArrowDirection.UP -> Pair(size.height - 4.dp.toPx(), 4.dp.toPx())
        }

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        val angle = kotlin.math.atan2(endY - startY, endX - startX)
        val wing1X = endX - wingLen * kotlin.math.cos(angle - wingAngle)
        val wing1Y = endY - wingLen * kotlin.math.sin(angle - wingAngle)
        val wing2X = endX - wingLen * kotlin.math.cos(angle + wingAngle)
        val wing2Y = endY - wingLen * kotlin.math.sin(angle + wingAngle)

        drawLine(
            color = color,
            start = Offset(endX, endY),
            end = Offset(wing1X, wing1Y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(endX, endY),
            end = Offset(wing2X, wing2Y),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun GraphScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToWorkout: ((String?) -> Unit)? = null,
    bottomSheetState: HomeBottomSheetState = rememberHomeBottomSheetState()
) {
    val exerciseService = LocalExerciseService.current
    val recordService = LocalRecordService.current
    val settingsService = LocalSettingsService.current
    val globalUiState = LocalGlobalUiState.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val exercises by exerciseService.getAllExercisesFlow().collectAsState(initial = emptyList())
    val weightUnit by settingsService.weightUnitFlow.collectAsState(initial = "kg")
    val savedRangeStr by settingsService.graphRangeFlow.collectAsState(initial = "ALL")
    val savedMetricStr by settingsService.graphMetricFlow.collectAsState(initial = "MAX_WEIGHT")

    var selectedRangeOverride by remember { mutableStateOf<SessionRange?>(null) }
    var compareMetricOverride by remember { mutableStateOf<CompareMetric?>(null) }
    val selectedRange = selectedRangeOverride ?: SessionRange.entries.find { it.name == savedRangeStr } ?: SessionRange.ALL
    val compareMetric = compareMetricOverride ?: CompareMetric.entries.find { it.name == savedMetricStr } ?: CompareMetric.MAX_WEIGHT
    var showMetricDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        globalUiState.isBottomSheetHidden = false
    }

    // Sync exercise selection with bottomSheetState:
    // Only resolve a default if the bottom sheet does not already have a valid selection.
    LaunchedEffect(exercises) {
        if (exercises.isEmpty()) return@LaunchedEffect
        if (bottomSheetState.selectedExerciseId == null || exercises.none { it.id == bottomSheetState.selectedExerciseId }) {
            val targetId = resolveDefaultExerciseId(
                exercises = exercises,
                latestRecordExerciseId = recordService.getLatestRecord()?.record?.exerciseId
            )
            if (targetId != null && bottomSheetState.selectedExerciseId != targetId) {
                exercises.find { it.id == targetId }?.let {
                    bottomSheetState.onExerciseSelected(it, closeDialog = false)
                }
            }
        }
    }

    val selectedExerciseId = bottomSheetState.selectedExerciseId

    // Fetch records chronologically ASC for graph mapping
    val records by remember(selectedExerciseId) {
        selectedExerciseId?.let { recordService.getRecordsByExerciseIdFlow(it) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    // Group records by calendar day
    val allDays: List<DaySession> = remember(records, globalUiState.nightModeHour) {
        groupRecordsByDay(records, rolloverHour = globalUiState.nightModeHour)
    }

    // Build chart bars for selected range and metric
    val displayBars: List<SessionPoint> = remember(allDays, selectedRange, compareMetric) {
        val bars = buildChartBars(allDays, selectedRange, compareMetric)
        markPrFlags(bars) { it.rightValue }
    }

    // Progress comparison over the visible window, in the selected metric.
    val comparison = remember(displayBars, allDays) {
        if (displayBars.isEmpty() || allDays.size <= 1) null
        else {
            val initial = roundToTwoDecimals(displayBars.first().leftValue)
            val latest = roundToTwoDecimals(displayBars.last().rightValue)
            val delta = roundToTwoDecimals(latest - initial)
            val percent = if (initial == 0.0 || delta == 0.0) 0.0 else roundToTwoDecimals(delta / initial * 100.0)
            MetricComparison(initial, latest, delta, percent)
        }
    }

    val delta = comparison?.delta
    val percent = comparison?.percent

    fun formatMetric(value: Double): String = formatMaxTwoDecimals(value)

    val deltaText = when {
        allDays.size <= 1 -> "--"
        delta != null && delta > 0.0 -> "+${formatMaxTwoDecimals(delta)}"
        delta != null && delta < 0.0 -> formatMaxTwoDecimals(delta)
        delta != null -> "0"
        else -> "--"
    }

    val percentText = when {
        percent != null && percent > 0.0 -> "+${formatMaxTwoDecimals(percent)}%"
        percent != null && percent < 0.0 -> "${formatMaxTwoDecimals(percent)}%"
        percent != null -> "0%"
        else -> ""
    }

    val heroBg = MaterialTheme.colorScheme.primaryContainer
    val deltaColor = when {
        allDays.size <= 1 -> MaterialTheme.colorScheme.onPrimaryContainer
        delta != null && delta > 0.0 -> adaptiveGreen(heroBg)
        delta != null && delta < 0.0 -> adaptiveRed(heroBg)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            onOpenDrawer = onOpenDrawer
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(28.dp)
            ) {
                if (exercises.isEmpty()) {
                    EmptyPlaceholder(message = globalLocalization.labelNoExercises)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. TOP VIEWING ZONE: Hero Progress Metric
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 20.dp)
                            ) {
                                if (allDays.isEmpty()) {
                                    Text(
                                        text = "--",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontSize = 58.sp,
                                            lineHeight = 58.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else if (allDays.size <= 1 || comparison == null) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = formatMetric(compareMetric.select(allDays.last())),
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontSize = 58.sp,
                                                lineHeight = 58.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = weightUnit,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                } else {
                                    val initialStr = formatMetric(comparison.initial)
                                    val latestStr = formatMetric(comparison.latest)
                                    val maxNumLength = maxOf(initialStr.length, latestStr.length)
                                    val heroFontSize = when {
                                        maxNumLength <= 3 -> 58.sp
                                        maxNumLength == 4 -> 48.sp
                                        maxNumLength == 5 -> 38.sp
                                        else -> 32.sp
                                    }
                                    val heroStyle = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = heroFontSize,
                                        lineHeight = heroFontSize,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = initialStr,
                                                style = heroStyle,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            DirectionalTrendingArrow(
                                                direction = calculateArrowDirection(delta),
                                                color = deltaColor,
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .size(width = 44.dp, height = 36.dp)
                                            )
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = latestStr,
                                                    style = heroStyle,
                                                    color = deltaColor,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = weightUnit,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(bottom = 6.dp),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$deltaText$weightUnit",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = deltaColor,
                                                maxLines = 1
                                            )
                                            if (percentText.isNotEmpty()) {
                                                Text(
                                                    text = "($percentText)",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = deltaColor,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. CENTER VIEWING ZONE: Capsule Graph Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                if (allDays.isEmpty()) {
                                    EmptyPlaceholder(message = globalLocalization.labelNoDataForExercise)
                                } else {
                                    BatteryStyleChart(
                                        sessions = displayBars,
                                        weightUnit = weightUnit,
                                        plotValue = { it.rightValue },
                                        primaryColor = MaterialTheme.colorScheme.primary,
                                        gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        barTonalColor = MaterialTheme.colorScheme.secondaryContainer,
                                        prColor = adaptiveGold(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    )
                                }
                            }
                        }

                        // 3. Range Toggle Buttons (7, 30, 90, ALL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val ranges = SessionRange.entries
                            ranges.forEachIndexed { index, range ->
                                val isSelected = selectedRange == range
                                val isFirst = index == 0
                                val isLast = index == ranges.lastIndex

                                val defaultTopStart = if (isFirst) 24.dp else 8.dp
                                val defaultBottomStart = if (isFirst) 24.dp else 8.dp
                                val defaultTopEnd = if (isLast) 24.dp else 8.dp
                                val defaultBottomEnd = if (isLast) 24.dp else 8.dp

                                val animatedTopStart by animateDpAsState(
                                    targetValue = if (isSelected) 24.dp else defaultTopStart,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeTopStart_${range.name}"
                                )
                                val animatedBottomStart by animateDpAsState(
                                    targetValue = if (isSelected) 24.dp else defaultBottomStart,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeBottomStart_${range.name}"
                                )
                                val animatedTopEnd by animateDpAsState(
                                    targetValue = if (isSelected) 24.dp else defaultTopEnd,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeTopEnd_${range.name}"
                                )
                                val animatedBottomEnd by animateDpAsState(
                                    targetValue = if (isSelected) 24.dp else defaultBottomEnd,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeBottomEnd_${range.name}"
                                )

                                val animatedBg by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeBg_${range.name}"
                                )
                                val animatedTextColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "rangeText_${range.name}"
                                )
                                val shape = RoundedCornerShape(
                                    topStart = animatedTopStart,
                                    topEnd = animatedTopEnd,
                                    bottomEnd = animatedBottomEnd,
                                    bottomStart = animatedBottomStart
                                )
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(shape)
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            selectedRangeOverride = range
                                            scope.launch { settingsService.setGraphRange(range.name) }
                                        },
                                    shape = shape,
                                    color = animatedBg
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = range.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = animatedTextColor
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Weight / Metric Picker (Right half of the screen)
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            MetricPickerCard(
                                modifier = Modifier.weight(1f),
                                metricLabel = compareMetric.label(),
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    showMetricDialog = true
                                }
                            )
                        }

                        // Bottom sheet clearance spacer so content isn't obscured by peek bar
                        Spacer(
                            modifier = Modifier.height(
                                PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp + navBarPadding + 16.dp
                            )
                        )
                    }
                }
            }
        }

        HomeBottomSheet(
            modifier = Modifier.fillMaxSize(),
            state = bottomSheetState,
            onNavigateToWorkout = onNavigateToWorkout
        )

        // Snackbar — positioned above the bottom sheet
        if (globalUiState.snackbarHostState.currentSnackbarData != null) {
            SnackbarHost(
                hostState = globalUiState.snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = globalUiState.snackbarBottomInset)
            )
        }
    }

    RadioSelectionDialog(
        show = showMetricDialog,
        title = globalLocalization.labelCompareBy,
        options = CompareMetric.entries,
        selectedValue = compareMetric,
        onValueSelected = {
            compareMetricOverride = it
            showMetricDialog = false
            scope.launch { settingsService.setGraphMetric(it.name) }
        },
        onDismissRequest = { showMetricDialog = false },
        labelProvider = { it.label() }
    )
}

@Composable
private fun MetricPickerCard(
    modifier: Modifier = Modifier,
    metricLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.testTag("metric_picker_card"),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = metricLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
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
