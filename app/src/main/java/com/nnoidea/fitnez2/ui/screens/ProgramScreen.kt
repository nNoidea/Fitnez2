package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.ScreenScaffold

@Composable
fun ProgramScreen(onOpenDrawer: () -> Unit) {
    ScreenScaffold(
        title = globalLocalization.labelProgram,
        onOpenDrawer = onOpenDrawer
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = globalLocalization.labelProgramPlaceholder,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
