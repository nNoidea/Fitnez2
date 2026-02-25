package com.nnoidea.fitnez2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nnoidea.fitnez2.ui.screens.ProgramScreen
import com.nnoidea.fitnez2.ui.screens.HomeScreen
import com.nnoidea.fitnez2.ui.screens.SettingsScreen
import com.nnoidea.fitnez2.ui.screens.WorkoutScreen
import com.nnoidea.fitnez2.ui.screens.DeveloperOptionsScreen
import androidx.compose.material.icons.filled.Build
import com.nnoidea.fitnez2.core.localization.globalLocalization

enum class AppPage(
        val route: String,
        val label: () -> String,
        val icon: ImageVector,
        val content: @Composable (onOpenDrawer: () -> Unit) -> Unit
) {
    Home(
            route = "home",
            label = { globalLocalization.labelHome },
            icon = Icons.Default.Home,
            content = { HomeScreen(onOpenDrawer = it) }
    ),
    Program(
            route = "program",
            label = { globalLocalization.labelProgram },
            icon = Icons.AutoMirrored.Filled.List,
            content = { ProgramScreen(onOpenDrawer = it) }
    ),
    Settings(
            route = "settings",
            label = { globalLocalization.labelSettings },
            icon = Icons.Default.Settings,
            content = { SettingsScreen(onOpenDrawer = it) }
    ),
    Workout(
            route = "workout",
            label = { "Workout" },
            icon = Icons.AutoMirrored.Filled.List,
            content = { _ ->
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity
                WorkoutScreen(onBack = { activity?.finish() })
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
