package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.HamburgerMenu

@Composable
fun FoodPrepScreen(onOpenDrawer: () -> Unit) {
    val dummyMeals =
            listOf(
                    "Grilled Chicken Salad",
                    "Quinoa and Black Beans",
                    "Oatmeal with Blueberries",
                    "Sweet Potato and Kale Hash",
                    "Protein Pancakes",
                    "Tofu Stir-fry",
                    "Salmon with Asparagus"
            )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(bottom = 16.dp), // Removed top/start padding here
                    verticalAlignment = Alignment.CenterVertically
            ) {
                HamburgerMenu(onClick = onOpenDrawer)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                        text = "Weekly Meal Prep",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.statusBarsPadding() // Align title with icon text-level
                )
            }

            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                            text = "Current Plan",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(dummyMeals) { meal ->
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceVariant
                                    )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = meal, style = MaterialTheme.typography.titleMedium)
                            Text(
                                    text = "Macro balanced • 450 kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
