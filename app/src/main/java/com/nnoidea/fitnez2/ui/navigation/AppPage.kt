package com.nnoidea.fitnez2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector
import com.nnoidea.fitnez2.core.localization.globalLocalization

enum class AppPage(
    val route: String,
    val label: () -> String,
    val icon: ImageVector
) {
    Timeline(
        route = "timeline",
        label = { globalLocalization.labelTimeline },
        icon = Icons.Default.Timeline
    ),
    Monthly(
        route = "monthly",
        label = { globalLocalization.labelMonthly },
        icon = Icons.Default.CalendarViewMonth
    ),
    Graph(
        route = "graph",
        label = { globalLocalization.labelGraph },
        icon = Icons.AutoMirrored.Filled.ShowChart
    ),
    Settings(
        route = "settings",
        label = { globalLocalization.labelSettings },
        icon = Icons.Default.Settings
    ),
    Workout(
        route = "workout",
        label = { globalLocalization.labelWorkout },
        icon = Icons.AutoMirrored.Filled.List
    ),
    Developer(
        route = "developer",
        label = { globalLocalization.labelDeveloperOptions },
        icon = Icons.Default.Build
    )
}

