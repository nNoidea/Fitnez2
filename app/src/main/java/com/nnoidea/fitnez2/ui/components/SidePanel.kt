package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.navigation.AppPage

@Composable
fun SidePanel(
        items: List<AppPage>,
        currentRoute: String?,
        onItemClick: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(modifier = Modifier.fillMaxHeight().width(300.dp).padding(16.dp)) {
            Text(
                    text = "Fitnez2",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
            )

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                Text(
                        text = item.label,
                        style =
                                if (isSelected) MaterialTheme.typography.titleMedium
                                else MaterialTheme.typography.bodyLarge,
                        color =
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { onItemClick(item.route) }
                                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun SidePanelPreview() {
    SidePanel(items = AppPage.entries, currentRoute = AppPage.Home.route, onItemClick = {})
}
