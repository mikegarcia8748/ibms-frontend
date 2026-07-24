package com.puregoldgo.ibms.ui.screen.secretary

/**
 * Everything the topsheet detail screen can do, bundled so the content
 * composable stays free of the ViewModel and remains previewable.
 */
data class TopSheetDetailCallback(
    val onBackClick: () -> Unit,
    val onLineQueryChange: (String) -> Unit,
    val onLineSortSelect: (TopSheetLineSortKey) -> Unit,
    val onLineSortDirectionToggle: () -> Unit,
    val onAccountClick: (String) -> Unit,
    val onAccountDetailDismiss: () -> Unit,
    val onRetryLines: () -> Unit,
)
