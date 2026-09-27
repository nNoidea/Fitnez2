package com.nnoidea.fitnez2.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.dialog.LoadingDialog
import com.nnoidea.fitnez2.ui.components.SettingsPageScaffold
import com.nnoidea.fitnez2.ui.components.SettingsGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Build

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.service.LocalBackupService
import com.nnoidea.fitnez2.service.LocalSettingsService

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.nnoidea.fitnez2.core.localization.EnStrings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.nnoidea.fitnez2.core.RotationMode
import android.widget.Toast
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToDeveloper: (() -> Unit)? = null
) {
    val globalState = LocalGlobalUiState.current
    val supportedLanguages = LocalizationManager.supportedLanguages
    
    val context = LocalContext.current
    val settingsService = LocalSettingsService.current
    val backupService = LocalBackupService.current
    val scope = rememberCoroutineScope()

    val defaultSets by settingsService.defaultSetsFlow.collectAsState(initial = "3")
    val defaultReps by settingsService.defaultRepsFlow.collectAsState(initial = "10")
    val defaultWeight by settingsService.defaultWeightFlow.collectAsState(initial = "20")

    var showDefaultsDialog by remember { mutableStateOf(false) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importingTitle by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isExporting = true
            scope.launch {
                val result = backupService.exportData(uri)
                isExporting = false
                if (result.isSuccess) {
                    Toast.makeText(context, globalLocalization.labelExportSuccess, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, globalLocalization.labelExportFailed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            importUri = it
            showImportConfirmation = true
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val primaryContainer = colorScheme.primaryContainer
    val onPrimaryContainer = colorScheme.onPrimaryContainer
    val secondaryContainer = colorScheme.secondaryContainer
    val onSecondaryContainer = colorScheme.onSecondaryContainer
    val tertiaryContainer = colorScheme.tertiaryContainer
    val onTertiaryContainer = colorScheme.onTertiaryContainer
    val errorContainer = colorScheme.errorContainer
    val onErrorContainer = colorScheme.onErrorContainer
    val surfaceVariant = colorScheme.surfaceVariant
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val primary = colorScheme.primary

    SettingsPageScaffold(
        title = globalLocalization.labelSettings,
        onOpenDrawer = onOpenDrawer
    ) {
        // Group 1: General Preferences & Appearance
        SettingsGroup {
            // Language Setting
            val languageOptions = listOf<EnStrings?>(null) + supportedLanguages
            radioItem(
                label = globalLocalization.labelLanguage,
                value = globalState.selectedLanguage?.languageName ?: globalLocalization.labelSystemLanguage,
                icon = Icons.Default.Language,
                iconContainerColor = primaryContainer,
                iconTint = onPrimaryContainer,
                options = languageOptions,
                selected = globalState.selectedLanguage,
                onSelected = { globalState.switchLanguage(it) },
                labelProvider = { it?.languageName ?: globalLocalization.labelSystemLanguage },
                bodyText = globalLocalization.labelAiTranslationsDisclaimer
            )

            // Rotation Setting
            val rotationLabel = when (globalState.rotationMode) {
                RotationMode.SYSTEM -> globalLocalization.labelRotationSystem
                RotationMode.ON -> globalLocalization.labelRotationOn
                RotationMode.OFF -> globalLocalization.labelRotationOff
                else -> globalLocalization.labelRotationSystem
            }

            radioItem(
                label = globalLocalization.labelRotation,
                value = rotationLabel,
                icon = Icons.Default.ScreenRotation,
                iconContainerColor = secondaryContainer,
                iconTint = onSecondaryContainer,
                options = RotationMode.ALL,
                selected = globalState.rotationMode,
                onSelected = { globalState.switchRotationMode(it) },
                labelProvider = {
                    when (it) {
                        RotationMode.SYSTEM -> globalLocalization.labelRotationSystem
                        RotationMode.ON -> globalLocalization.labelRotationOn
                        RotationMode.OFF -> globalLocalization.labelRotationOff
                        else -> ""
                    }
                }
            )

            // Weight Unit Setting
            radioItem(
                label = globalLocalization.labelWeightUnit,
                value = globalState.weightUnit,
                icon = Icons.Default.FitnessCenter,
                iconContainerColor = tertiaryContainer,
                iconTint = onTertiaryContainer,
                options = listOf(globalLocalization.unitKg, globalLocalization.unitLb),
                selected = globalState.weightUnit,
                onSelected = { globalState.switchWeightUnit(it) },
                labelProvider = { it }
            )

            // In-App Font Setting
            val fontLabel = when (globalState.fontMode) {
                "system" -> globalLocalization.labelFontSystemDefault
                "rounded" -> globalLocalization.labelFontGoogleSansFlexRounded
                else -> globalLocalization.labelFontSystemDefault
            }

            radioItem(
                label = globalLocalization.labelInAppFont,
                value = fontLabel,
                icon = Icons.Default.Edit,
                iconContainerColor = errorContainer.copy(alpha = 0.8f),
                iconTint = onErrorContainer,
                options = listOf("system", "rounded"),
                selected = globalState.fontMode,
                onSelected = { globalState.switchFontMode(it) },
                labelProvider = {
                    when (it) {
                        "system" -> globalLocalization.labelFontSystemDefault
                        "rounded" -> globalLocalization.labelFontGoogleSansFlexRounded
                        else -> ""
                    }
                }
            )

            // Default Exercise Values Setting
            item(
                label = globalLocalization.labelDefaultExerciseValues,
                value = "$defaultSets x $defaultReps @ $defaultWeight",
                icon = Icons.Default.Star,
                iconContainerColor = primaryContainer.copy(alpha = 0.6f),
                iconTint = primary,
                onClick = { showDefaultsDialog = true }
            )
        }

        // Group 2: Data Management
        SettingsGroup {
            // Export Data
            item(
                label = globalLocalization.labelExportData,
                value = "",
                icon = Icons.Default.Share,
                iconContainerColor = secondaryContainer,
                iconTint = onSecondaryContainer,
                onClick = {
                    val timeStamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault()).format(Instant.now().atZone(java.time.ZoneId.systemDefault()))
                    val fileName = "Fitnez2-$timeStamp.json"
                    exportLauncher.launch(fileName)
                }
            )

            // Import Data
            item(
                label = globalLocalization.labelImportData,
                value = "",
                icon = Icons.Default.ArrowDownward,
                iconContainerColor = tertiaryContainer,
                iconTint = onTertiaryContainer,
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )
        }

        // Group 3: Advanced
        SettingsGroup {
            // Developer Settings
            item(
                label = globalLocalization.labelDeveloperOptions,
                value = "",
                icon = Icons.Default.Build,
                iconContainerColor = surfaceVariant,
                iconTint = onSurfaceVariant,
                showChevron = true,
                onClick = {
                    if (onNavigateToDeveloper != null) {
                        onNavigateToDeveloper()
                    } else {
                        val intent = android.content.Intent(context, com.nnoidea.fitnez2.MainActivity::class.java).apply {
                            putExtra(com.nnoidea.fitnez2.MainActivity.EXTRA_PAGE_ROUTE, "developer")
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
    
    DefaultValuesEditorDialog(
        show = showDefaultsDialog,
        currentDefaults = Defaults(defaultSets, defaultReps, defaultWeight),
        onSave = { validSets, validReps, validWeight ->
            scope.launch {
                settingsService.setDefaultSets(validSets.toString())
                settingsService.setDefaultReps(validReps.toString())
                settingsService.setDefaultWeight(validWeight.toString())
                showDefaultsDialog = false
            }
        },
        onDismiss = { showDefaultsDialog = false }
    )

    ImportConfirmationDialog(
        show = showImportConfirmation,
        onConfirm = {
            showImportConfirmation = false
            isImporting = true
            importingTitle = globalLocalization.labelClearingDatabase
            importUri?.let { uri ->
                scope.launch {
                    val result = backupService.importData(uri)
                    isImporting = false
                    if (result.isSuccess) {
                        com.nnoidea.fitnez2.ui.common.GlobalUiState.emitToAll(UiSignal.DatabaseSeeded)
                        Toast.makeText(context, globalLocalization.labelImportSuccess, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, globalLocalization.labelImportFailed, Toast.LENGTH_SHORT).show()
                    }
                    importUri = null
                }
            }
        },
        onDismiss = { showImportConfirmation = false }
    )

    LoadingDialog(
        show = isExporting,
        title = globalLocalization.labelExportingData,
        progress = null
    )

    LoadingDialog(
        show = isImporting,
        title = importingTitle,
        progress = null
    )

}

