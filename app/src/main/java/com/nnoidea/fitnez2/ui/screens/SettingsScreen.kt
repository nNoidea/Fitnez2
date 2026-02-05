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
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onOpenDrawer: () -> Unit) {
    val globalState = LocalGlobalUiState.current
    val supportedLanguages = LocalizationManager.supportedLanguages
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.nnoidea.fitnez2.data.SettingsRepository(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val defaultSets by settingsRepository.defaultSetsFlow.collectAsState(initial = "3")
    val defaultReps by settingsRepository.defaultRepsFlow.collectAsState(initial = "10")
    val defaultWeight by settingsRepository.defaultWeightFlow.collectAsState(initial = "20")

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showWeightUnitDialog by remember { mutableStateOf(false) }
    
    var showSetsDialog by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

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
            
            // Weight Unit Setting
            SettingsItem(
                label = globalLocalization.labelWeightUnit,
                value = globalState.weightUnit,
                onClick = { showWeightUnitDialog = true }
            )

            HorizontalDivider()

            // Default Values Section
            Text(
                text = "Defaults", 
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            SettingsItem(
                label = globalLocalization.labelDefaultSets,
                value = defaultSets,
                onClick = { showSetsDialog = true }
            )
            SettingsItem(
                label = globalLocalization.labelDefaultReps,
                value = defaultReps,
                onClick = { showRepsDialog = true }
            )
            SettingsItem(
                label = globalLocalization.labelDefaultWeight,
                value = defaultWeight,
                onClick = { showWeightDialog = true }
            )

            HorizontalDivider()
            
            HapticsTestSection()
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
    
    // Default Values Dialogs
    SettingsInputDialog(
        show = showSetsDialog,
        title = globalLocalization.labelDefaultSets,
        initialValue = defaultSets,
        onDismissRequest = { showSetsDialog = false },
        onConfirm = { 
            scope.launch { 
                settingsRepository.setDefaultSets(it) 
                showSetsDialog = false
            }
        }
    )

    SettingsInputDialog(
        show = showRepsDialog,
        title = globalLocalization.labelDefaultReps,
        initialValue = defaultReps,
        onDismissRequest = { showRepsDialog = false },
        onConfirm = { 
            scope.launch { 
                settingsRepository.setDefaultReps(it) 
                showRepsDialog = false
            }
        }
    )

    SettingsInputDialog(
        show = showWeightDialog,
        title = globalLocalization.labelDefaultWeight,
        initialValue = defaultWeight,
        onDismissRequest = { showWeightDialog = false },
        onConfirm = { 
            scope.launch { 
                settingsRepository.setDefaultWeight(it) 
                showWeightDialog = false
            }
        }
    )
}

@Composable
fun SettingsInputDialog(
    show: Boolean,
    title: String,
    initialValue: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (show) {
        var text by remember { mutableStateOf(initialValue) }
        
        PredictiveModal(
            show = show,
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
                
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(globalLocalization.labelCancel)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(text) }) {
                        Text(globalLocalization.labelSave)
                    }
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

// --- Haptics Test Section ---

@Composable
fun HapticsTestSection() {
    val view = androidx.compose.ui.platform.LocalView.current
    
    // Define the available haptic types (Name -> Constant/Action)
    val hapticTypes = remember {
        listOf(
            "Clock Tick" to android.view.HapticFeedbackConstants.CLOCK_TICK,
            "Context Click" to android.view.HapticFeedbackConstants.CONTEXT_CLICK,
            "Keyboard Tap" to android.view.HapticFeedbackConstants.KEYBOARD_TAP,
            "Long Press" to android.view.HapticFeedbackConstants.LONG_PRESS,
            "Virtual Key" to android.view.HapticFeedbackConstants.VIRTUAL_KEY,
            "Confirm" to android.view.HapticFeedbackConstants.CONFIRM,
            "Reject" to android.view.HapticFeedbackConstants.REJECT,
            "Gesture Start" to android.view.HapticFeedbackConstants.GESTURE_START,
            "Gesture End" to android.view.HapticFeedbackConstants.GESTURE_END
        )
    }

    var sliderPosition by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    
    // Logic to snap and trigger
    // We want the slider to feel "steps".
    // When value changes, we find the closest index.
    val index = sliderPosition.roundToInt().coerceIn(0, hapticTypes.size - 1)
    val currentType = hapticTypes[index]

    // Trigger haptic only when the discrete index changes
    var lastIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    
    androidx.compose.runtime.LaunchedEffect(index) {
        if (index != lastIndex) {
            view.performHapticFeedback(currentType.second)
            lastIndex = index
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Haptics Test: ${currentType.first}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        androidx.compose.material3.Slider(
            value = sliderPosition,
            onValueChange = { 
                sliderPosition = it
                // We could also trigger continuous feedback here if we wanted "ticks" while dragging
                // But triggering on step change (via LaunchedEffect) is safer for distinct feel.
            },
            valueRange = 0f..(hapticTypes.size - 1).toFloat(),
            steps = hapticTypes.size - 2, // Steps are the ticks *between* min and max.
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Move slider to feel different vibrations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

