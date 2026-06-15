package com.nnoidea.fitnez2.ui.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.RotationMode
import com.nnoidea.fitnez2.core.localization.EnStrings
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.service.SettingsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class GlobalUiState(
    val scope: CoroutineScope,
    private val settingsService: SettingsService
) {
    // State: Day Change Tracker Key (invalidated automatically at midnight)
    var midnightTransitionTimestamp by mutableLongStateOf(System.currentTimeMillis())
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
                midnightTransitionTimestamp = System.currentTimeMillis()
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
    var snackbarBottomInset by mutableStateOf(0.dp)

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
