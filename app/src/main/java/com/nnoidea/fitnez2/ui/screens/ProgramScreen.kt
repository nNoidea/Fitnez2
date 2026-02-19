package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.TopHeader

@Composable
fun ProgramScreen(onOpenDrawer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopHeader {
            HamburgerMenu(onClick = onOpenDrawer)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = globalLocalization.labelProgram,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = globalLocalization.labelProgramPlaceholder,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
