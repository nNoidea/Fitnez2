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
import com.nnoidea.fitnez2.ui.navigation.AppPage

@Composable
fun SidePanel(
        currentRoute: String?,
        onItemClick: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
            modifier = modifier,
            drawerTonalElevation = 0.dp
    ) {
        Column(
                modifier =
                        Modifier.fillMaxHeight()
                                .padding(horizontal = 12.dp)
                                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(32.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                        text = "Fitnez"
                )
                Text(
                        text = "Version 1.0.0",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Items
            SidePanelItem(
                page = AppPage.Home,
                isSelected = currentRoute == AppPage.Home.route,
                onItemClick = { onItemClick(AppPage.Home.route) }
            )
            SidePanelItem(
                page = AppPage.FoodPrep,
                isSelected = currentRoute == AppPage.FoodPrep.route,
                onItemClick = { onItemClick(AppPage.FoodPrep.route) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Section
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            SidePanelItem(
                page = AppPage.Settings,
                isSelected = currentRoute == AppPage.Settings.route,
                onItemClick = { onItemClick(AppPage.Settings.route) }
            )

            NavigationDrawerItem(
                label = { Text("Help & Feedback") },
                selected = false,
                onClick = { /* Handle help */ },
                icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SidePanelItem(
    page: AppPage,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = page.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        selected = isSelected,
        onClick = onItemClick,
        icon = {
            Icon(
                imageVector = page.icon,
                contentDescription = page.label,
            )
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
