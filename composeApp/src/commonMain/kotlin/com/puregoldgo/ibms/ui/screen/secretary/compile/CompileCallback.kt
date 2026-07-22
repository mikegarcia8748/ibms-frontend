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
    val onRemoveLine: (lineId: String) -> Unit,
    val onRetryLines: () -> Unit,
    val onBackToReview: () -> Unit,
    val onConfirmClick: () -> Unit,

    // Result.
    val onStartNew: () -> Unit,
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
    onRemoveLine = {},
    onRetryLines = {},
    onBackToReview = {},
    onConfirmClick = {},
    onStartNew = {},
)
