package com.nnoidea.fitnez2.ui.components.recordlist

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.launch

@Composable
fun RecordList(
    items: List<RecordDisplayItem>,
    weightUnit: String,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    extraBottomPadding: Dp = 0.dp,
    enableAutoHide: Boolean = false,
    showHeaders: Boolean = true,
    showCollapse: Boolean = true,
    showSwipe: Boolean = true,
    showTimestamps: Boolean = true,
    expandedRecordIds: SnapshotStateMap<String, Boolean> = remember { mutableStateMapOf() },
    timestampTokens: SnapshotStateMap<String, Long> = remember { mutableStateMapOf() },
    onShowTimestamp: (String) -> Unit = { _ -> },
    onUpdateRequest: ((Record) -> Unit)? = null,
    onDeleteRequest: ((Record) -> Unit)? = null,
    onDeleteGroupRequest: ((List<Record>) -> Unit)? = null,
    onScrollToTopClick: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val effectiveShowTimestamp: (String) -> Unit = if (showTimestamps) onShowTimestamp else { _ -> }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = globalLocalization.labelHistoryEmpty,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                RecordListContent(
                    items = items,
                    weightUnit = weightUnit,
                    listState = listState,
                    extraBottomPadding = extraBottomPadding,
                    enableAutoHide = enableAutoHide,
                    showHeaders = showHeaders,
                    showCollapse = showCollapse,
                    showSwipe = showSwipe,
                    expandedRecordIds = expandedRecordIds,
                    timestampTokens = timestampTokens,
                    onShowTimestamp = effectiveShowTimestamp,
                    onUpdateRequest = onUpdateRequest,
                    onDeleteRequest = onDeleteRequest,
                    onDeleteGroupRequest = onDeleteGroupRequest
                )
            }
        }

        ScrollToTopButton(
            listState = listState,
            onClick = {
                scope.launch {
                    onScrollToTopClick?.invoke() ?: listState.scrollToItem(0)
                }
            },
            extraBottomPadding = extraBottomPadding,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun ScrollToTopButton(
    listState: LazyListState,
    onClick: () -> Unit,
    extraBottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val view = LocalView.current
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(showButton) {
        globalUiState.isScrollToTopButtonVisible = showButton
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val animatedBottomPadding by animateDpAsState(
        targetValue = if (globalUiState.isBottomSheetHidden) navBarPadding else extraBottomPadding,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fabBottomPadding"
    )

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.padding(bottom = animatedBottomPadding)
    ) {
        FloatingActionButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                onClick()
            },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
