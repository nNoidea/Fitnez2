package com.nnoidea.fitnez2.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.service.ProvideAppServices
import com.nnoidea.fitnez2.service.SettingsService
import kotlinx.coroutines.launch

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
    SideEffect {
        GlobalUiState.setInstance(state)
    }

    DisposableEffect(state) {
        GlobalUiState.register(state)
        onDispose {
            GlobalUiState.unregister(state)
        }
    }

    val context = LocalContext.current
    SideEffect {
        ValidateAndCorrect.appContext = context.applicationContext
    }

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
