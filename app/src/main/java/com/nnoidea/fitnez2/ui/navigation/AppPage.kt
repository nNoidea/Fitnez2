package com.nnoidea.fitnez2.ui.navigation

import androidx.compose.runtime.Composable
import com.nnoidea.fitnez2.ui.screens.HomeScreen
import com.nnoidea.fitnez2.ui.screens.SettingsScreen

enum class AppPage(
        val route: String,
        val label: String,
        val content: @Composable (onOpenDrawer: () -> Unit) -> Unit
) {
    Home("home", "Home", { HomeScreen(onOpenDrawer = it) }),
    Settings("settings", "Settings", { SettingsScreen(onOpenDrawer = it) })
}
