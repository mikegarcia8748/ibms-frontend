package com.puregoldgo.ibms.ui.screen.secretary.compile

/**
 * Everything the Compile TopSheet panel can do, bundled so the content
 * composables stay free of the ViewModel and remain previewable.
 */
data class CompileCallback(
    // Review.
    val onPreviousMonth: () -> Unit,
    val onNextMonth: () -> Unit,
    val onProviderSelect: (String?) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onLetterSelect: (Char) -> Unit,
    val onCompileClick: () -> Unit,
    val onRetryLoad: () -> Unit,

    // RFP entry.
    val onRfpChange: (lineId: String, value: String) -> Unit,
    val onRfpCommit: (lineId: String) -> Unit,
    val onRfpRangeStartChange: (String) -> Unit,
    val onRfpRangeEndChange: (String) -> Unit,
    val onAssignRfpClick: () -> Unit,
    val onRemoveLine: (lineId: String) -> Unit,
    val onRetryLines: () -> Unit,
    val onBackToReview: () -> Unit,
    val onConfirmClick: () -> Unit,

    // Drafted-account list filters (RFP entry).
    val onLinesQueryChange: (String) -> Unit,
    val onLinesLetterSelect: (Char) -> Unit,
    val onLinesSortSelect: (LineSortOrder) -> Unit,

    // Result.
    val onStartNew: () -> Unit,

    // Resume draft — reopen a DRAFT topsheet left unfinished.
    val onResumeDraftClick: () -> Unit,
    val onResumeDraft: (topSheetId: String) -> Unit,
    val onResumeDraftDismiss: () -> Unit,
)

/** A do-nothing callback, for previews and as the content composable's default. */
val NoOpCompileCallback = CompileCallback(
    onPreviousMonth = {},
    onNextMonth = {},
    onProviderSelect = {},
    onQueryChange = {},
    onLetterSelect = {},
    onCompileClick = {},
    onRetryLoad = {},
    onRfpChange = { _, _ -> },
    onRfpCommit = {},
    onRfpRangeStartChange = {},
    onRfpRangeEndChange = {},
    onAssignRfpClick = {},
    onRemoveLine = {},
    onRetryLines = {},
    onBackToReview = {},
    onConfirmClick = {},
    onLinesQueryChange = {},
    onLinesLetterSelect = {},
    onLinesSortSelect = {},
    onStartNew = {},
    onResumeDraftClick = {},
    onResumeDraft = {},
    onResumeDraftDismiss = {},
)
