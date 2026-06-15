package com.nnoidea.fitnez2.ui.components.recordlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.components.bottomsheet.autoHideBottomSheet

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordListContent(
    items: List<RecordDisplayItem>,
    weightUnit: String,
    listState: LazyListState,
    expandedRecordIds: SnapshotStateMap<String, Boolean>,
    timestampTokens: SnapshotStateMap<String, Long>,
    onShowTimestamp: (String) -> Unit,
    extraBottomPadding: Dp,
    enableAutoHide: Boolean,
    showHeaders: Boolean,
    showCollapse: Boolean,
    showSwipe: Boolean,
    onUpdateRequest: ((Record) -> Unit)?,
    onDeleteRequest: ((Record) -> Unit)?,
    onDeleteGroupRequest: ((List<Record>) -> Unit)?
) {
    val firstGroupIndex = remember(items) {
        items.indexOfFirst { it is RecordDisplayItem.RecordGroup }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .autoHideBottomSheet(enableAutoHide),
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
    ) {
        item(key = "top_spacer_anchor") {
            Spacer(modifier = Modifier.height(1.dp))
        }

        itemsIndexed(
            items = items,
            key = { _, model ->
                when (model) {
                    is RecordDisplayItem.DateHeader -> "header_${model.section}_${model.date}"
                    is RecordDisplayItem.RecordGroup -> "record_group_${model.records.first().record.id}"
                    is RecordDisplayItem.BatchSeparator -> "separator_${model.index}"
                    is RecordDisplayItem.EvictedBatch -> "evicted_${model.index}"
                    is RecordDisplayItem.LoadingMore -> "loading_more"
                }
            },
            contentType = { _, model ->
                when (model) {
                    is RecordDisplayItem.DateHeader -> "header"
                    is RecordDisplayItem.RecordGroup -> "record_group"
                    is RecordDisplayItem.BatchSeparator -> "separator"
                    is RecordDisplayItem.EvictedBatch -> "evicted"
                    is RecordDisplayItem.LoadingMore -> "loading"
                }
            }
        ) { index, item ->
            when (item) {
                is RecordDisplayItem.DateHeader -> {
                    if (showHeaders) {
                        RecordDateHeader(
                            date = item.date,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                is RecordDisplayItem.BatchSeparator -> {
                    OlderRecordsSeparator(modifier = Modifier.animateItem())
                }
                is RecordDisplayItem.EvictedBatch -> {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(item.heightDp.dp)
                    )
                }
                is RecordDisplayItem.LoadingMore -> {
                    LoadingMoreIndicator(modifier = Modifier.animateItem())
                }
                is RecordDisplayItem.RecordGroup -> {
                    val isTopGroup = index == firstGroupIndex
                    val prevRenderItem = if (index > 0) items[index - 1] else null

                    RecordListGroupCard(
                        groupRecords = item.records,
                        isLight = item.isLight,
                        isTopGroup = isTopGroup,
                        showCollapse = showCollapse,
                        showSwipe = showSwipe,
                        showHeaders = showHeaders,
                        weightUnit = weightUnit,
                        expandedRecordIds = expandedRecordIds,
                        timestampTokens = timestampTokens,
                        showTimestamp = onShowTimestamp,
                        onUpdateRequest = onUpdateRequest,
                        onDeleteRequest = onDeleteRequest,
                        onDeleteGroupRequest = onDeleteGroupRequest,
                        prevRenderItem = prevRenderItem,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun OlderRecordsSeparator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = globalLocalization.labelOlderRecords,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RecordDateHeader(
    date: Long,
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val currentLocale = globalLocalization.appLocale
    val isToday = remember(date, globalUiState.currentDayKey) { android.text.format.DateUtils.isToday(date) }
    val isYesterday = remember(date, globalUiState.currentDayKey) {
        android.text.format.DateUtils.isToday(date + android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    val dateString = remember(date, currentLocale) {
        globalLocalization.formatDateShort(date)
    }

    val dayName = remember(date, currentLocale, isToday, isYesterday) {
        if (isToday) globalLocalization.labelToday
        else if (isYesterday) globalLocalization.labelYesterday
        else globalLocalization.formatDayName(date)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = dateString,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
