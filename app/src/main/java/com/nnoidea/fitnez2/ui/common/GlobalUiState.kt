package com.nnoidea.fitnez2.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.SettingsRepository
import kotlinx.coroutines.launch

// Simple global UI signals
sealed interface UiSignal {
    data object ScrollToTop : UiSignal
    // add more stuff here...
}

class GlobalUiState(
    val scope: CoroutineScope? = null,
    private val onLanguageChanged: ((EnStrings?) -> Unit)? = null,
    private val onWeightUnitChanged: ((String) -> Unit)? = null
) {
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

    fun switchLanguage(newLanguage: EnStrings?) {
        LocalizationManager.setLanguage(newLanguage)
        onLanguageChanged?.invoke(newLanguage)
    }

    fun switchWeightUnit(unit: String) {
        weightUnit = unit
        onWeightUnitChanged?.invoke(unit)
    }

    // Signals: One-off events (e.g. ScrollToTop)
    private val _signalChannel = Channel<UiSignal>(Channel.BUFFERED)
    val signalFlow = _signalChannel.receiveAsFlow()

    suspend fun emitSignal(signal: UiSignal) {
        _signalChannel.send(signal)
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
    }

    return state
}

@Composable
fun ProvideGlobalUiState(
    state: GlobalUiState = rememberGlobalUiState(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalGlobalUiState provides state
    ) {
        content()
    }
}
