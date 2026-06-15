package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.ui.components.ScreenScaffold

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


