package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

data class BottomSheetLayoutParams(
    val maxOffset: Float,
    val minOffset: Float
)

@Composable
fun rememberBottomSheetLayoutParams(): BottomSheetLayoutParams {
    val density = LocalDensity.current
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val peekHeight = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp + navBarPadding
    val peekHeightPx = with(density) { peekHeight.toPx() }
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val topPaddingPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }
    val maxOffset = screenHeightPx - topPaddingPx - peekHeightPx
    return BottomSheetLayoutParams(maxOffset = maxOffset, minOffset = 0f)
}
