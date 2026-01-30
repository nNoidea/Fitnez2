package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.nnoidea.fitnez2.ui.components.PredictiveDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
@Composable
fun SettingsScreen(onOpenDrawer: () -> Unit) {
    val globalState = LocalGlobalUiState.current
    val supportedLanguages = LocalizationManager.supportedLanguages

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showWeightUnitDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Hamburger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                HamburgerMenu(
                    onClick = onOpenDrawer,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = globalLocalization.labelSettings,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            HorizontalDivider()

            // Language Setting
            SettingsItem(
                label = globalLocalization.labelLanguage,
                value = globalState.selectedLanguage?.languageName ?: globalLocalization.labelSystemLanguage,
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider()

            // Weight Unit Setting
            SettingsItem(
                label = globalLocalization.labelWeightUnit, // Use the new string
                value = globalState.weightUnit,
                onClick = { showWeightUnitDialog = true }
            )
        }
    }

    if (showWeightUnitDialog) {
        PredictiveDialog(
            show = showWeightUnitDialog,
            onDismissRequest = { showWeightUnitDialog = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = globalLocalization.labelWeightUnit,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    listOf("kg", "lb").forEach { unit ->
                        LanguageOption(
                            text = unit,
                            selected = globalState.weightUnit == unit,
                            onClick = {
                                globalState.switchWeightUnit(unit)
                                showWeightUnitDialog = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = { showWeightUnitDialog = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(globalLocalization.labelCancel)
                }
            }
        }
    }

    if (showLanguageDialog) {
        PredictiveDialog(
            show = showLanguageDialog,
            onDismissRequest = { showLanguageDialog = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = globalLocalization.labelLanguage,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Option: System Language
                    LanguageOption(
                        text = globalLocalization.labelSystemLanguage,
                        selected = globalState.selectedLanguage == null,
                        onClick = {
                            globalState.switchLanguage(null)
                            showLanguageDialog = false
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Options: Supported Languages
                    supportedLanguages.forEach { lang ->
                        LanguageOption(
                            text = lang.languageName,
                            selected = globalState.selectedLanguage == lang,
                            onClick = {
                                globalState.switchLanguage(lang)
                                showLanguageDialog = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(
                    onClick = { showLanguageDialog = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(globalLocalization.labelCancel)
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LanguageOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by Row
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
