package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization

@Composable
fun HamburgerMenu(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
    ) { Icon(imageVector = Icons.Default.Menu, contentDescription = globalLocalization.labelOpenDrawer) }
}
