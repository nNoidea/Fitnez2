package com.nnoidea.fitnez2.ui.screens.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.recordlist.RecordList
import com.nnoidea.fitnez2.ui.components.HamburgerMenu
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import kotlinx.coroutines.launch


@Composable
fun TimelineScreen(
    targetDate: Long? = null,
    onOpenDrawer: () -> Unit,
    onNavigateToWorkout: ((String?) -> Unit)? = null,
    bottomSheetState: HomeBottomSheetState = rememberHomeBottomSheetState()
) {
    val globalUiState = LocalGlobalUiState.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            headerContent = { HamburgerMenu(onClick = onOpenDrawer) },
        ) {
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val recordListState = rememberRecordListState(targetDate = targetDate)

            RecordList(
                items = recordListState.uiItems,
                weightUnit = recordListState.weightUnit,
                listState = recordListState.listState,
                hasNewer = recordListState.hasNewer,
                expandedRecordIds = recordListState.expandedRecordIds,
                timestampTokens = recordListState.timestampTokens,
                onShowTimestamp = { recordListState.showTimestampFor(it) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("main_timeline_record_list"),
                extraBottomPadding = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp + navBarPadding,
                enableAutoHide = true,
                onScrollToTopClick = {
                    scope.launch { recordListState.scrollToTop(null) }
                },
                onUpdateRequest = { recordListState.onUpdateRequest(it) },
                onDeleteRequest = { recordListState.onDeleteRequest(it) },
                onDeleteGroupRequest = { recordListState.onDeleteGroupRequest(it) }
            )
        }

        HomeBottomSheet(
            modifier = Modifier.fillMaxSize(),
            state = bottomSheetState,
            onNavigateToWorkout = onNavigateToWorkout
        )

        // Snackbar — positioned above the bottom sheet
        if (globalUiState.snackbarHostState.currentSnackbarData != null) {
            SnackbarHost(
                hostState = globalUiState.snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = globalUiState.snackbarBottomInset)
            )
        }
    }
}
