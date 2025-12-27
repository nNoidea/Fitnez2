package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HamburgerMenu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // In Breezy Weather, the hamburger is part of a Toolbar that handles status bar insets.
    // We'll apply statusBarsPadding to ensure it's not overlapping with system icons,
    // and use the standard Material 3 navigation icon offset.
    IconButton(
            onClick = onClick,
            modifier =
                    modifier.statusBarsPadding()
                            .padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
    ) { Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Navigation Drawer") }
}
