package com.nnoidea.fitnez2.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.nnoidea.fitnez2.ui.components.dialog.LoadingDialog
import com.nnoidea.fitnez2.ui.components.dialog.RadioSelectionDialog
import com.nnoidea.fitnez2.ui.components.SettingsItem
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun SettingsScreen(onOpenDrawer: () -> Unit) {
    val globalState = LocalGlobalUiState.current
    val supportedLanguages = LocalizationManager.supportedLanguages
    
    val context = LocalContext.current
    val settingsService = LocalSettingsService.current
    val backupService = LocalBackupService.current
    val scope = rememberCoroutineScope()

    val defaultSets by settingsService.defaultSetsFlow.collectAsState(initial = "3")
    val defaultReps by settingsService.defaultRepsFlow.collectAsState(initial = "10")
    val defaultWeight by settingsService.defaultWeightFlow.collectAsState(initial = "20")

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showWeightUnitDialog by remember { mutableStateOf(false) }
    var showRotationDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    
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

    ScreenScaffold(
        title = globalLocalization.labelSettings,
        onOpenDrawer = onOpenDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider()

            // Language Setting
            SettingsItem(
                label = globalLocalization.labelLanguage,
                value = globalState.selectedLanguage?.languageName ?: globalLocalization.labelSystemLanguage,
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider()

            // Rotation Setting
            val rotationLabel = when (globalState.rotationMode) {
                RotationMode.SYSTEM -> globalLocalization.labelRotationSystem
                RotationMode.ON -> globalLocalization.labelRotationOn
                RotationMode.OFF -> globalLocalization.labelRotationOff
                else -> globalLocalization.labelRotationSystem
            }

            SettingsItem(
                label = globalLocalization.labelRotation,
                value = rotationLabel,
                icon = Icons.Default.ScreenRotation,
                onClick = { showRotationDialog = true }
            )

            HorizontalDivider()
            
            // Weight Unit Setting
            SettingsItem(
                label = globalLocalization.labelWeightUnit,
                value = globalState.weightUnit,
                icon = Icons.Default.FitnessCenter,
                onClick = { showWeightUnitDialog = true }
            )

            HorizontalDivider()

            // In-App Font Setting
            val fontLabel = when (globalState.fontMode) {
                "system" -> globalLocalization.labelFontSystemDefault
                "rounded" -> globalLocalization.labelFontGoogleSansFlexRounded
                else -> globalLocalization.labelFontSystemDefault
            }

            SettingsItem(
                label = globalLocalization.labelInAppFont,
                value = fontLabel,
                icon = Icons.Default.Edit,
                onClick = { showFontDialog = true }
            )

            HorizontalDivider()

            SettingsItem(
                label = globalLocalization.labelDefaultExerciseValues,
                value = "$defaultSets x $defaultReps @ $defaultWeight", 
                icon = Icons.Default.Star,
                onClick = { showDefaultsDialog = true }
            )

            HorizontalDivider()

            // Export Data
            SettingsItem(
                label = globalLocalization.labelExportData,
                value = "",
                icon = Icons.Default.Share,
                onClick = {
                    val timeStamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault()).format(Instant.now().atZone(java.time.ZoneId.systemDefault()))
                    val fileName = "Fitnez2-$timeStamp.json"
                    exportLauncher.launch(fileName)
                }
            )

            HorizontalDivider()

            // Import Data
            SettingsItem(
                label = globalLocalization.labelImportData,
                value = "",
                icon = Icons.Default.ArrowDownward,
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )

            HorizontalDivider()

            // Developer Settings
            SettingsItem(
                label = globalLocalization.labelDeveloperOptions,
                value = "",
                icon = Icons.Default.Build,
                onClick = {
                   val intent = android.content.Intent(context, com.nnoidea.fitnez2.MainActivity::class.java).apply {
                       putExtra(com.nnoidea.fitnez2.MainActivity.EXTRA_PAGE_ROUTE, "developer")
                   }
                   context.startActivity(intent)
                }
            )
        }
    }

    RadioSelectionDialog(
        show = showWeightUnitDialog,
        title = globalLocalization.labelWeightUnit,
        options = listOf(globalLocalization.unitKg, globalLocalization.unitLb),
        selectedValue = globalState.weightUnit,
        onValueSelected = {
            globalState.switchWeightUnit(it)
            showWeightUnitDialog = false
        },
        onDismissRequest = { showWeightUnitDialog = false },
        labelProvider = { it }
    )

    RadioSelectionDialog(
        show = showRotationDialog,
        title = globalLocalization.labelRotation,
        options = RotationMode.ALL,
        selectedValue = globalState.rotationMode,
        onValueSelected = {
            globalState.switchRotationMode(it)
            showRotationDialog = false
        },
        onDismissRequest = { showRotationDialog = false },
        labelProvider = {
            when (it) {
                RotationMode.SYSTEM -> globalLocalization.labelRotationSystem
                RotationMode.ON -> globalLocalization.labelRotationOn
                RotationMode.OFF -> globalLocalization.labelRotationOff
                else -> ""
            }
        }
    )

    // Prepare language options with "System Default" (null) at the top
    val languageOptions = listOf<EnStrings?>(null) + supportedLanguages
    RadioSelectionDialog(
        show = showLanguageDialog,
        title = globalLocalization.labelLanguage,
        options = languageOptions,
        selectedValue = globalState.selectedLanguage,
        onValueSelected = {
            globalState.switchLanguage(it)
            showLanguageDialog = false
        },
        onDismissRequest = { showLanguageDialog = false },
        labelProvider = { it?.languageName ?: globalLocalization.labelSystemLanguage },
        bodyText = globalLocalization.labelAiTranslationsDisclaimer
    )

    RadioSelectionDialog(
        show = showFontDialog,
        title = globalLocalization.labelInAppFont,
        options = listOf("system", "rounded"),
        selectedValue = globalState.fontMode,
        onValueSelected = {
            globalState.switchFontMode(it)
            showFontDialog = false
        },
        onDismissRequest = { showFontDialog = false },
        labelProvider = {
            when (it) {
                "system" -> globalLocalization.labelFontSystemDefault
                "rounded" -> globalLocalization.labelFontGoogleSansFlexRounded
                else -> ""
            }
        }
    )
    
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

