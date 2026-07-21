package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import com.puregoldgo.ibms.platform.file.PickedFile

/**
 * Everything the registry panels can do, bundled so their composables stay free
 * of the ViewModel and remain previewable.
 */
data class RegistryCallback(
    // Branch filters.
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,
    val onBranchProviderSelect: (String?) -> Unit,

    // Account filters.
    val onAccountQueryChange: (String) -> Unit,
    val onAccountProviderSelect: (String?) -> Unit,

    // Bulk import. Opened from the console header, which is why the shell takes
    // this one lambda from here rather than owning it.
    val onBulkUploadClick: () -> Unit,
    val onBulkImportFilePicked: (PickedFile) -> Unit,
    val onBulkImportStart: () -> Unit,
    val onBulkUploadDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
)
