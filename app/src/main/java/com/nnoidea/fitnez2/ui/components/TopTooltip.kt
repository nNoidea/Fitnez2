package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.nnoidea.fitnez2.ui.common.GlobalUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTooltip(globalUiState: GlobalUiState) {
    if (globalUiState.tooltipMessage != null) {
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val savedStateRegistryOwner = androidx.compose.ui.platform.LocalSavedStateRegistryOwner.current
        val view = androidx.compose.ui.platform.LocalView.current
        val token = view.applicationWindowToken

        androidx.compose.runtime.key(globalUiState.tooltipId) {
            androidx.compose.runtime.DisposableEffect(Unit) {
                val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
                val params = android.view.WindowManager.LayoutParams().apply {
                    width = android.view.WindowManager.LayoutParams.MATCH_PARENT
                    height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
                    format = android.graphics.PixelFormat.TRANSLUCENT
                    flags = android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    type = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                    gravity = android.view.Gravity.TOP
                    this.token = token
                }

                val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
                    setViewTreeLifecycleOwner(lifecycleOwner)
                    setViewTreeViewModelStoreOwner(lifecycleOwner as? androidx.lifecycle.ViewModelStoreOwner)
                    setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                    
                    setContent {
                        // Apply specific theme/style if needed, but context usually carries it.
                        // We recreate the TopTooltip UI here.
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut()
                        ) {
                             Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 50.dp) // Approximate status bar + padding
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.TopCenter
                             ) {
                                androidx.compose.material3.Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = globalUiState.tooltipMessage ?: "",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                             }
                        }
                    }
                }

                windowManager.addView(composeView, params)

                onDispose {
                    try {
                        windowManager.removeViewImmediate(composeView)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }
}
