package com.nnoidea.fitnez2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.ExerciseHistoryList
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.PredictiveBottomSheet

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // HamburgerMenu now handles its own statusBarsPadding and proper M3 offset
        HamburgerMenu(onClick = onOpenDrawer, modifier = Modifier.align(Alignment.TopStart))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 56.dp) // Offset for HamburgerMenu
        ) {

            
            ExerciseHistoryList(
                modifier = Modifier.weight(1f)
            )
        }
        
        // Just drop the PredictiveBottomSheet in here, it handles its own interaction and "dragging" logic
        PredictiveBottomSheet(modifier = Modifier.fillMaxSize())
    }
}
