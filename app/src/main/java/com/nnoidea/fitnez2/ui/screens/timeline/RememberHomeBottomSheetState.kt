package com.nnoidea.fitnez2.ui.screens.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.service.LocalWorkoutService
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.bottomsheet.rememberBottomSheetLayoutParams

@Composable
fun rememberHomeBottomSheetState(): HomeBottomSheetState {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val globalUiState = LocalGlobalUiState.current
    val recordService = LocalRecordService.current
    val exerciseService = LocalExerciseService.current
    val workoutService = LocalWorkoutService.current
    val settingsService = LocalSettingsService.current
    val scope = rememberCoroutineScope()
    val layoutParams = rememberBottomSheetLayoutParams()
    val view = LocalView.current

    return remember(layoutParams) {
        HomeBottomSheetState(
            scope = scope,
            recordService = recordService,
            workoutService = workoutService,
            exerciseService = exerciseService,
            settingsService = settingsService,
            globalUiState = globalUiState,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = layoutParams.maxOffset,
            minOffset = layoutParams.minOffset,
            onHapticFeedback = { view.performHapticFeedback(it) }
        )
    }
}
