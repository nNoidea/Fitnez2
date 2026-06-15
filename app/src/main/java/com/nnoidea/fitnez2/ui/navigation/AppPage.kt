package com.nnoidea.fitnez2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nnoidea.fitnez2.ui.screens.timeline.TimelineScreen
import com.nnoidea.fitnez2.ui.screens.monthly.MonthlyScreen
import com.nnoidea.fitnez2.ui.screens.graph.GraphScreen
import com.nnoidea.fitnez2.ui.screens.settings.SettingsScreen
import com.nnoidea.fitnez2.ui.screens.workout.WorkoutScreen
import com.nnoidea.fitnez2.ui.screens.developer.DeveloperOptionsScreen
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.automirrored.filled.ShowChart
import com.nnoidea.fitnez2.core.localization.globalLocalization

enum class AppPage(
        val route: String,
        val label: () -> String,
        val icon: ImageVector,
        val content: @Composable (onOpenDrawer: () -> Unit) -> Unit
) {
    Timeline(
            route = "timeline",
            label = { globalLocalization.labelTimeline },
            icon = Icons.Default.Timeline,
            content = { TimelineScreen(onOpenDrawer = it) }
    ),
    Monthly(
            route = "monthly",
            label = { globalLocalization.labelMonthly },
            icon = Icons.Default.CalendarViewMonth,
            content = { MonthlyScreen(onOpenDrawer = it) }
    ),
    Graph(
            route = "graph",
            label = { globalLocalization.labelGraph },
            icon = Icons.AutoMirrored.Filled.ShowChart,
            content = { GraphScreen(onOpenDrawer = it) }
    ),
    Settings(
            route = "settings",
            label = { globalLocalization.labelSettings },
            icon = Icons.Default.Settings,
            content = { SettingsScreen(onOpenDrawer = it) }
    ),
    Workout(
            route = "workout",
            label = { globalLocalization.labelWorkout },
            icon = Icons.AutoMirrored.Filled.List,
            content = { _ ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity
                val workoutId = activity?.intent?.getStringExtra("extra_workout_id")
                WorkoutScreen(workoutId = workoutId, onBack = { activity?.finish() })
            }
    ),
    Developer(
            route = "developer",
            label = { globalLocalization.labelDeveloperOptions },
            icon = Icons.Default.Build,
            content = { _ ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity
                DeveloperOptionsScreen(onBack = { activity?.finish() })
            }
    )
}
