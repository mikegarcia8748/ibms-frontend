package com.puregoldgo.ibms.ui.screen.dashboard

import com.puregoldgo.ibms.platform.file.PickedFile

/**
 * Everything the control panel can do, bundled so the content composables stay
 * free of the ViewModel and remain previewable.
 */
data class DashboardCallback(
    val onTabSelect: (DashboardTab) -> Unit,
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,
    val onBranchProviderSelect: (String?) -> Unit,
    val onAccountQueryChange: (String) -> Unit,
    val onAccountProviderSelect: (String?) -> Unit,
    val onBulkUploadClick: () -> Unit,
    val onBulkImportFilePicked: (PickedFile) -> Unit,
    val onBulkImportStart: () -> Unit,
    val onBulkUploadDismiss: () -> Unit,
    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
