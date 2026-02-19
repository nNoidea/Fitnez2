package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.ExerciseHistoryList
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.TopHeader


@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
    val globalUiState = LocalGlobalUiState.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            TopHeader {
                HamburgerMenu(onClick = onOpenDrawer)
            }

            ExerciseHistoryList(
                modifier = Modifier.weight(1f),
                extraBottomPadding = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp
            )
        }
        
        // Just drop the PredictiveBottomSheet in here, it handles its own interaction and "dragging" logic
        PredictiveBottomSheet(modifier = Modifier.fillMaxSize())

        // Snackbar — positioned above the bottom sheet
        SnackbarHost(
            hostState = globalUiState.snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = globalUiState.bottomSheetSnackbarOffset)
        )
    }
}
