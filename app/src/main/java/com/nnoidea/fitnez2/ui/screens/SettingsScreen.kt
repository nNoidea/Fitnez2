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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.TopHeader
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import com.nnoidea.fitnez2.core.localization.EnStrings
import com.nnoidea.fitnez2.data.SettingsRepository
import com.nnoidea.fitnez2.ui.components.PredictiveAlertDialog
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.ui.components.TopTooltip
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onOpenDrawer: () -> Unit) {
    val globalState = LocalGlobalUiState.current
    val supportedLanguages = LocalizationManager.supportedLanguages
    
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val defaultSets by settingsRepository.defaultSetsFlow.collectAsState(initial = "3")
    val defaultReps by settingsRepository.defaultRepsFlow.collectAsState(initial = "10")
    val defaultWeight by settingsRepository.defaultWeightFlow.collectAsState(initial = "20")

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showWeightUnitDialog by remember { mutableStateOf(false) }
    var showRotationDialog by remember { mutableStateOf(false) }
    
    var showDefaultsDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header with Hamburger
            TopHeader {
                HamburgerMenu(
                    onClick = onOpenDrawer
                )
                // HamburgerMenu has 16dp end padding built-in? No
                // HamburgerMenu has: start=8, top=8, end=16, bottom=8.
                // So we don't need a Spacer(16.dp) if we rely on that, but ProgramScreen ADDED a Spacer(16.dp).
                // If HamburgerMenu has 16dp end padding, adding Spacer(16.dp) makes 32dp gap.
                // Let's check ProgramScreen again.
                // ProgramScreen: HamburgerMenu() -> Spacer(16.dp) -> Text.
                // I should duplicate that structure for consistency.
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = globalLocalization.labelSettings,
                    style = MaterialTheme.typography.titleLarge
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

            HorizontalDivider()

            // Rotation Setting
            val rotationLabel = when (globalState.rotationMode) {
                "system" -> globalLocalization.labelRotationSystem
                "on" -> globalLocalization.labelRotationOn
                "off" -> globalLocalization.labelRotationOff
                else -> globalLocalization.labelRotationSystem
            }

            SettingsItem(
                label = globalLocalization.labelRotation,
                value = rotationLabel,
                onClick = { showRotationDialog = true }
            )

            HorizontalDivider()
            
            // Weight Unit Setting
            SettingsItem(
                label = globalLocalization.labelWeightUnit,
                value = globalState.weightUnit,
                onClick = { showWeightUnitDialog = true }
            )

            HorizontalDivider()

            SettingsItem(
                label = globalLocalization.labelDefaultExerciseValues,
                value = "$defaultSets x $defaultReps @ $defaultWeight", 
                onClick = { showDefaultsDialog = true }
            )

            HorizontalDivider()
            
            HorizontalDivider()

            // Developer Settings
            // Developer Settings
            SettingsItem(
                label = "Developer Options",
                value = "",
                onClick = {
                   val intent = android.content.Intent(context, com.nnoidea.fitnez2.MainActivity::class.java).apply {
                       putExtra(com.nnoidea.fitnez2.MainActivity.EXTRA_PAGE_ROUTE, "developer")
                   }
                   context.startActivity(intent)
                }
            )
        }

        TopTooltip(globalUiState = globalState)
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

    SelectionDialog(
        show = showRotationDialog,
        title = globalLocalization.labelRotation,
        options = listOf("system", "on", "off"),
        selectedValue = globalState.rotationMode,
        onValueSelected = {
            globalState.switchRotationMode(it)
            showRotationDialog = false
        },
        onDismissRequest = { showRotationDialog = false },
        labelProvider = {
            when (it) {
                "system" -> globalLocalization.labelRotationSystem
                "on" -> globalLocalization.labelRotationOn
                "off" -> globalLocalization.labelRotationOff
                else -> ""
            }
        }
    )

    // Prepare language options with "System Default" (null) at the top
    val languageOptions = listOf<EnStrings?>(null) + supportedLanguages
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
    
    // Unified Default Values Dialog
    if (showDefaultsDialog) {
        var sets by remember { mutableStateOf(defaultSets) }
        var reps by remember { mutableStateOf(defaultReps) }
        var weight by remember { mutableStateOf(defaultWeight) }

        PredictiveAlertDialog(
            show = showDefaultsDialog,
            onDismissRequest = { showDefaultsDialog = false },
            title = globalLocalization.labelDefaultExerciseValues,
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val validSets = ValidateAndCorrect.sets(sets)
                            val validReps = ValidateAndCorrect.reps(reps)
                            val validWeight = ValidateAndCorrect.weight(weight)

                            if (validSets != null && validReps != null && validWeight != null) {
                                settingsRepository.setDefaultSets(validSets.toString())
                                settingsRepository.setDefaultReps(validReps.toString())
                                settingsRepository.setDefaultWeight(validWeight.toString())
                                showDefaultsDialog = false
                            } else {
                                // Ideally show error, for now just don't close
                            }
                        }
                    }
                ) {
                    Text(globalLocalization.labelSave)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefaultsDialog = false }) {
                    Text(globalLocalization.labelCancel)
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sets
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text(globalLocalization.labelDefaultSets) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Reps
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(globalLocalization.labelDefaultReps) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Weight
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(globalLocalization.labelDefaultWeight) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
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



