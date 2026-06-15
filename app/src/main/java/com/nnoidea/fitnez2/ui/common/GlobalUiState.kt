package com.nnoidea.fitnez2.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.service.ProvideAppServices
import com.nnoidea.fitnez2.service.SettingsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Simple global UI signals
sealed interface UiSignal {
    data class ScrollToTop(val recordId: String? = null) : UiSignal
    data class RecordInserted(val recordId: String) : UiSignal
    data class RecordUpdated(val record: com.nnoidea.fitnez2.data.entities.Record) : UiSignal
    data class RecordDeleted(val recordId: String) : UiSignal
    data object DatabaseSeeded : UiSignal
}

class GlobalUiState(
    val scope: CoroutineScope,
    private val settingsService: SettingsService
) {
    // State: Day Change Tracker Key (invalidated automatically at midnight)
    var currentDayKey by mutableLongStateOf(System.currentTimeMillis())
        private set

    var isScrollToTopButtonVisible by mutableStateOf(false)

    init {
        startDayChangeTracker()
    }

    private fun startDayChangeTracker() {
        scope.launch {
            while (true) {
                val delayMillis = getMillisUntilNextMidnight()
                kotlinx.coroutines.delay(delayMillis)
                currentDayKey = System.currentTimeMillis()
            }
        }
    }

    private fun getMillisUntilNextMidnight(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        return (calendar.timeInMillis - now).coerceAtLeast(1000)
    }

    companion object {
        var instance: GlobalUiState? = null
            private set

        fun setInstance(state: GlobalUiState) {
            instance = state
        }

        private val activeInstances =
            java.util.Collections.synchronizedList(mutableListOf<GlobalUiState>())

        fun register(state: GlobalUiState) {
            activeInstances.add(state)
        }

        fun unregister(state: GlobalUiState) {
            activeInstances.remove(state)
        }

        suspend fun emitToAll(signal: UiSignal) {
            val instances = synchronized(activeInstances) { activeInstances.toList() }
            instances.forEach { instance ->
                instance.emitSignal(signal)
            }
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

    // State: BottomSheet Hide (for auto-hide on scroll)
    var isBottomSheetHidden by mutableStateOf(false)

    // State: Rotation Mode
    var rotationMode by mutableStateOf(RotationMode.SYSTEM)

    // State: Font Mode
    var fontMode by mutableStateOf("rounded")

    fun switchLanguage(newLanguage: EnStrings?) {
        LocalizationManager.setLanguage(newLanguage)
        scope.launch {
            settingsService.setLanguageCode(newLanguage?.appLocale?.language)
        }
    }

    fun switchWeightUnit(unit: String) {
        weightUnit = unit
        scope.launch {
            settingsService.setWeightUnit(unit)
        }
    }

    fun switchRotationMode(mode: String) {
        rotationMode = mode
        scope.launch {
            settingsService.setRotationMode(mode)
        }
    }

    fun switchFontMode(mode: String) {
        fontMode = mode
        scope.launch {
            settingsService.setFontMode(mode)
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
fun rememberGlobalUiState(settingsService: SettingsService): GlobalUiState {
    val scope = rememberCoroutineScope()

    val state = remember(settingsService, scope) {
        GlobalUiState(scope, settingsService)
    }

    // Sync persistence -> State / LocalizationManager
    LaunchedEffect(state) {
        launch {
            settingsService.languageCodeFlow.collect { code ->
                val lang = if (code != null) LocalizationManager.getLanguageByCode(code) else null
                if (LocalizationManager.selectedLanguage != lang) {
                    LocalizationManager.setLanguage(lang)
                }
            }
        }
        launch {
            settingsService.weightUnitFlow.collect { unit ->
                state.weightUnit = unit
            }
        }
        launch {
            settingsService.rotationModeFlow.collect { mode ->
                state.rotationMode = mode
            }
        }
        launch {
            settingsService.fontModeFlow.collect { mode ->
                state.fontMode = mode
            }
        }
    }

    return state
}

@Composable
fun ProvideGlobalUiState(
    database: AppDatabase,
    settingsService: SettingsService,
    state: GlobalUiState = rememberGlobalUiState(settingsService),
    content: @Composable () -> Unit
) {
    GlobalUiState.setInstance(state)

    DisposableEffect(state) {
        GlobalUiState.register(state)
        onDispose {
            GlobalUiState.unregister(state)
        }
    }

    val context = LocalContext.current
    ValidateAndCorrect.appContext = context.applicationContext

    ProvideAppServices(
        context = context,
        database = database
    ) {
        CompositionLocalProvider(
            LocalGlobalUiState provides state,
            LocalSettingsService provides settingsService,
        ) {
            content()
        }
    }
}
