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
import com.nnoidea.fitnez2.ui.components.PredictiveModal
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
                label = globalLocalization.labelWeightUnit,
                value = globalState.weightUnit,
                onClick = { showWeightUnitDialog = true }
            )
        }
    }

    SelectionDialog(
        show = showWeightUnitDialog,
        title = globalLocalization.labelWeightUnit,
        options = listOf("kg", "lb"),
        selectedValue = globalState.weightUnit,
        onValueSelected = {
            globalState.switchWeightUnit(it)
            showWeightUnitDialog = false
        },
        onDismissRequest = { showWeightUnitDialog = false },
        labelProvider = { it }
    )

    // Prepare language options with "System Default" (null) at the top
    val languageOptions = listOf<com.nnoidea.fitnez2.core.localization.EnStrings?>(null) + supportedLanguages
    SelectionDialog(
        show = showLanguageDialog,
        title = globalLocalization.labelLanguage,
        options = languageOptions,
        selectedValue = globalState.selectedLanguage,
        onValueSelected = {
            globalState.switchLanguage(it)
            showLanguageDialog = false
        },
        onDismissRequest = { showLanguageDialog = false },
        labelProvider = { it?.languageName ?: globalLocalization.labelSystemLanguage }
    )
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun <T> SelectionDialog(
    show: Boolean,
    title: String,
    options: List<T>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    labelProvider: (T) -> String
) {
    if (show) {
        PredictiveModal(
            show = show,
            onDismissRequest = onDismissRequest
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        RadioOption(
                            text = labelProvider(option),
                            selected = option == selectedValue,
                            onClick = { onValueSelected(option) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(globalLocalization.labelCancel)
                }
            }
        }
    }
}

@Composable
fun RadioOption(
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
