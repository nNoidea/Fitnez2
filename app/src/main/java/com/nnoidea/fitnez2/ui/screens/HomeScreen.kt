package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.HomeBottomSheet

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // HamburgerMenu now handles its own statusBarsPadding and proper M3 offset
        HamburgerMenu(onClick = onOpenDrawer, modifier = Modifier.align(Alignment.TopStart))

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Home Page")
        }
        
        // Just drop the BottomSheet in here, it handles its own interaction and "dragging" logic
        HomeBottomSheet(modifier = Modifier.fillMaxSize())
    }
}
