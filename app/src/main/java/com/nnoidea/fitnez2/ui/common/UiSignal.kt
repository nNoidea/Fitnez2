package com.nnoidea.fitnez2.ui.common

import com.nnoidea.fitnez2.data.entities.Record

sealed interface UiSignal {
    data object ScrollToTop : UiSignal
    data class ScrollToRecord(val recordId: String) : UiSignal
    data class RecordInserted(val recordId: String) : UiSignal
    data class RecordUpdated(val record: Record) : UiSignal
    data class RecordDeleted(val recordId: String) : UiSignal
    data object DatabaseSeeded : UiSignal
}
