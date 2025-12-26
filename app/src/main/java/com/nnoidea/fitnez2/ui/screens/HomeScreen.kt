package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.PlaceholderTestContent

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
    // Scaffold handled at Activity level for Drawer integration usually,
    // but the request asks for a hamburger button IN the components.
    // For simplicity, we can just have the content here if the Scaffold is top-level.
    // Or we can have a local Scaffold if the top-bar is per-screen.
    // Given the requirement "hamburger button will be a modular ui component... reusable from
    // settings or any other page",
    // it implies it should be part of the screen's layout or a common top bar.

    // I'll place the hamburger button in a top-left aligned box for now as requested.

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // The HamburgerMenu should probably be invoked here or passed up.
        // But the requirement says "create a hamburger component... that is reusable".
        // Integrating it into a shared top bar or just placing it.
        // Let's assume the Activity handles the Scaffold/Drawer, and passes the `onOpenDrawer`
        // callback.
        // BUT, we need to show the button.

        // Let's make a simple layout where the button is top-left.

        HamburgerMenu(
                onClick = onOpenDrawer,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 16.dp)
        )

        Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Home Page", style = MaterialTheme.typography.headlineMedium)

            PlaceholderTestContent()
        }
    }
}
