package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.HamburgerMenu

@Composable
fun ProgramScreen(onOpenDrawer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HamburgerMenu(onClick = onOpenDrawer)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Programs",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.statusBarsPadding()
                )
            }
            
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Program Page Placeholder",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
