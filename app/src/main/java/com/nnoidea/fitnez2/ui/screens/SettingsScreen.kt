package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.ui.components.HamburgerMenu

@Composable
fun SettingsScreen(onOpenDrawer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HamburgerMenu(onClick = onOpenDrawer, modifier = Modifier.align(Alignment.TopStart))

        Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = "Settings",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            com.nnoidea.fitnez2.ui.components.PlaceholderTestContent()
        }
    }
}
