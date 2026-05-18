package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.screenComponents.home.ExerciseHistoryList
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.screenComponents.home.HomeBottomSheet


@Composable
fun TimelineScreen(onOpenDrawer: () -> Unit) {
    val globalUiState = LocalGlobalUiState.current

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            headerContent = { HamburgerMenu(onClick = onOpenDrawer) },
        ) {
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ExerciseHistoryList(
                modifier = Modifier.weight(1f),
                extraBottomPadding = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp + navBarPadding,
                enableAutoHide = true
            )
        }

        HomeBottomSheet(modifier = Modifier.fillMaxSize())

        // Snackbar — positioned above the bottom sheet
        SnackbarHost(
            hostState = globalUiState.snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = globalUiState.bottomSheetSnackbarOffset)
        )
    }
}
