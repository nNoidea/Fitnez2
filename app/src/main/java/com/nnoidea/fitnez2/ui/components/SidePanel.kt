package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.nnoidea.fitnez2.ui.navigation.AppPage

@Composable
fun SidePanel(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet {
        Column(
            modifier =
                Modifier.fillMaxHeight()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(32.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text(
                    text = "Fitnez2",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            item(
                label = AppPage.Home.label,
                icon = AppPage.Home.icon,
                selected = currentRoute == AppPage.Home.route,
                onClick = { onItemClick(AppPage.Home.route) }
            )
            item(
                label = AppPage.FoodPrep.label,
                icon = AppPage.FoodPrep.icon,
                selected = currentRoute == AppPage.FoodPrep.route,
                onClick = { onItemClick(AppPage.FoodPrep.route) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Section
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            item(
                label = AppPage.Settings.label,
                icon = AppPage.Settings.icon,
                selected = currentRoute == AppPage.Settings.route,
                onClick = { onItemClick(AppPage.Settings.route) }
            )

            item(
                label = "Help & Feedback",
                icon = Icons.AutoMirrored.Filled.Help,
                onClick = { /* Handle help */ }
            )

            item(
                label = "1.0.0",
                icon = Icons.Default.Info,
                onClick = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun item(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
            )
        },
        modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
