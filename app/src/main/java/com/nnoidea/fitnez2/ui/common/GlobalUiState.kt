package com.nnoidea.fitnez2.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.RotationMode
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.EnStrings
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Simple global UI signals
sealed interface UiSignal {
    data class ScrollToTop(val recordId: Int? = null) : UiSignal
    data object DatabaseSeeded : UiSignal
}

class GlobalUiState(
    val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository
) {
    companion object {
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
    var rotationMode by mutableStateOf(RotationMode.SYSTEM)

    fun switchLanguage(newLanguage: EnStrings?) {
        LocalizationManager.setLanguage(newLanguage)
        scope.launch {
            settingsRepository.setLanguageCode(newLanguage?.appLocale?.language)
        }
    }

    fun switchWeightUnit(unit: String) {
        weightUnit = unit
        scope.launch {
            settingsRepository.setWeightUnit(unit)
        }
    }

    fun switchRotationMode(mode: String) {
        rotationMode = mode
        scope.launch {
            settingsRepository.setRotationMode(mode)
        }
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
        currentSnackbarJob = scope.launch {
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

val LocalGlobalUiState = staticCompositionLocalOf<GlobalUiState> {
    error("No GlobalUiState provided")
}

@Composable
fun rememberGlobalUiState(): GlobalUiState {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val state = remember(settingsRepository, scope) {
        GlobalUiState(scope, settingsRepository)
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
    GlobalUiState.setInstance(state)

    val context = LocalContext.current
    ValidateAndCorrect.appContext = context.applicationContext
    
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context, scope) }
    val settingsRepository = remember { SettingsRepository(context) }

    CompositionLocalProvider(
        LocalGlobalUiState provides state,
        LocalAppDatabase provides database,
        LocalSettingsRepository provides settingsRepository,
    ) {
        content()
    }
}
