package com.nnoidea.fitnez2.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.Channel

import kotlinx.coroutines.flow.receiveAsFlow

// Simple global UI signals
sealed interface UiSignal {
    data object ScrollToTop : UiSignal
    // add more stuff here...
}

class GlobalUiState {
    // State: Is any overlay (Drawer, Dialog, etc.) currently masking the main content?
    var isOverlayOpen by mutableStateOf(false)

    // Signals: One-off events (e.g. ScrollToTop)
    private val _signalChannel = Channel<UiSignal>(Channel.BUFFERED)
    val signalFlow = _signalChannel.receiveAsFlow()

    suspend fun emitSignal(signal: UiSignal) {
        _signalChannel.send(signal)
    }
}

val LocalGlobalUiState = compositionLocalOf { GlobalUiState() }

@Composable
fun rememberGlobalUiState(): GlobalUiState {
    return remember { GlobalUiState() }
}
