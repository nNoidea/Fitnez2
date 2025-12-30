package com.nnoidea.fitnez2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.nnoidea.fitnez2.ui.screens.FoodPrepScreen
import com.nnoidea.fitnez2.ui.screens.HomeScreen
import com.nnoidea.fitnez2.ui.screens.SettingsScreen
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
    FoodPrep(
            route = "food_prep",
            label = { globalLocalization.labelFoodPrep },
            icon = Icons.Default.Restaurant,
            content = { FoodPrepScreen(onOpenDrawer = it) }
    ),
    Settings(
            route = "settings",
            label = { globalLocalization.labelSettings },
            icon = Icons.Default.Settings,
            content = { SettingsScreen(onOpenDrawer = it) }
    )
}
