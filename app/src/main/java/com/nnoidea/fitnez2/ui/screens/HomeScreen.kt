package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.ExerciseHistoryList
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.PredictiveBottomSheet
import com.nnoidea.fitnez2.ui.components.TopHeader

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
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
                modifier = Modifier.weight(1f)
            )
        }
        
        // Just drop the PredictiveBottomSheet in here, it handles its own interaction and "dragging" logic
        PredictiveBottomSheet(modifier = Modifier.fillMaxSize())

        val snackbarHostState = LocalGlobalUiState.current.snackbarHostState
        val snackbarBottomPadding = LocalGlobalUiState.current.bottomSheetSnackbarOffset

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Intelligently position based on bottom sheet state
                // Add a little padding (16.dp) when above sheet so it doesn't touch exactly
                .padding(bottom = snackbarBottomPadding)
        )
    }
}
