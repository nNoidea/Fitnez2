package com.nnoidea.fitnez2.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.core.localization.EnStrings
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.core.localization.TrStrings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.SettingsRepository
import kotlinx.coroutines.launch

// Simple global UI signals
sealed interface UiSignal {
    data class ScrollToTop(val recordId: Int? = null) : UiSignal
    // add more stuff here...
}

class GlobalUiState(
    val scope: CoroutineScope? = null,
    private val onLanguageChanged: ((EnStrings?) -> Unit)? = null,
    private val onWeightUnitChanged: ((String) -> Unit)? = null,
    private val onRotationModeChanged: ((String) -> Unit)? = null
) {
    companion object {
        // Global instance for non-Composable access (e.g., ValidateAndCorrect)
        var instance: GlobalUiState? = null
            private set
        
        fun setInstance(state: GlobalUiState) {
            instance = state
        }
    }
    // State: Is any overlay (Drawer, Dialog, etc.) currently masking the main content?
    var isOverlayOpen by mutableStateOf(false)

    // State: Current Language
    val language: EnStrings
        get() = LocalizationManager.currentLanguage
    
    val selectedLanguage: EnStrings?
        get() = LocalizationManager.selectedLanguage

    val strings by derivedStateOf {
        LocalizationManager.strings
    }

    // State: Weight Unit
    var weightUnit by mutableStateOf("kg")

    // State: BottomSheet Offset for Snackbars
    var bottomSheetSnackbarOffset by mutableStateOf(0.dp)

    // State: Rotation Mode
    var rotationMode by mutableStateOf("system")

    fun switchLanguage(newLanguage: EnStrings?) {
        LocalizationManager.setLanguage(newLanguage)
        onLanguageChanged?.invoke(newLanguage)
    }

    fun switchWeightUnit(unit: String) {
        weightUnit = unit
        onWeightUnitChanged?.invoke(unit)
    }

    fun switchRotationMode(mode: String) {
        rotationMode = mode
        onRotationModeChanged?.invoke(mode)
    }

    // Signals: One-off events (e.g. ScrollToTop)
    private val _signalFlow = MutableSharedFlow<UiSignal>()
    val signalFlow = _signalFlow.asSharedFlow()

    suspend fun emitSignal(signal: UiSignal) {
        _signalFlow.emit(signal)
    }


    // Snackbar State
    val snackbarHostState = SnackbarHostState()
    private var currentSnackbarJob: Job? = null

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onActionPerformed: () -> Unit = {}
    ) {
        currentSnackbarJob?.cancel()
        currentSnackbarJob = scope?.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                onActionPerformed()
            }
        }
    }

    // Tooltip State
    var tooltipMessage by mutableStateOf<String?>(null)
        private set
    var tooltipId by mutableLongStateOf(0L)
        private set
    private var currentTooltipJob: Job? = null

    fun showTooltip(message: String, durationMillis: Long = 3000) {
        currentTooltipJob?.cancel()
        // Set message immediately (synchronously) so it shows even if scope is null
        tooltipMessage = message
        tooltipId++
        // Schedule auto-dismiss if scope is available
        currentTooltipJob = scope?.launch {
            kotlinx.coroutines.delay(durationMillis)
            tooltipMessage = null
        }
    }
}

val LocalGlobalUiState = compositionLocalOf { GlobalUiState() }


@Composable
fun rememberGlobalUiState(): GlobalUiState {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val state = remember(settingsRepository, scope) {
        GlobalUiState(
            scope = scope,
            onLanguageChanged = { lang ->
                scope.launch {
                    settingsRepository.setLanguageCode(lang?.appLocale?.language)
                }
            },
            onWeightUnitChanged = { unit ->
                scope.launch {
                    settingsRepository.setWeightUnit(unit)
                }
            },
            onRotationModeChanged = { mode ->
                scope.launch {
                    settingsRepository.setRotationMode(mode)
                }
            }
        )
    }

    // Sync persistence -> State / LocalizationManager
    LaunchedEffect(state) {
        launch {
            settingsRepository.languageCodeFlow.collect { code ->
                val lang = if (code != null) LocalizationManager.getLanguageByCode(code) else null
                if (LocalizationManager.selectedLanguage != lang) {
                    LocalizationManager.setLanguage(lang)
                }
            }
        }
        launch {
            settingsRepository.weightUnitFlow.collect { unit ->
                state.weightUnit = unit
            }
        }
        launch {
            settingsRepository.rotationModeFlow.collect { mode ->
                state.rotationMode = mode
            }
        }
    }

    return state
}

@Composable
fun ProvideGlobalUiState(
    state: GlobalUiState = rememberGlobalUiState(),
    content: @Composable () -> Unit
) {
    // Set global instance for non-Composable access
    GlobalUiState.setInstance(state)
    
    CompositionLocalProvider(
        LocalGlobalUiState provides state
    ) {
        content()
    }
}
