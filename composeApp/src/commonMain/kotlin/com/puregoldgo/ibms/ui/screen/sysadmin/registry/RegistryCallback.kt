package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.model.StoreType

/**
 * Everything the registry panels can do, bundled so their composables stay free
 * of the ViewModel and remain previewable.
 */
data class RegistryCallback(
    // Branch filters.
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,
    val onBranchProviderSelect: (String?) -> Unit,

    // Register branch.
    val onRegisterBranchClick: () -> Unit,
    val onRegisterBranchStoreTypeChange: (StoreType) -> Unit,
    val onRegisterBranchCodeChange: (String) -> Unit,
    val onRegisterBranchNameChange: (String) -> Unit,
    val onRegisterBranchRegionChange: (String) -> Unit,
    val onRegisterBranchProvinceChange: (String) -> Unit,
    val onRegisterBranchCityChange: (String) -> Unit,
    val onRegisterBranchBarangayChange: (String) -> Unit,
    val onRegisterBranchPostalCodeChange: (String) -> Unit,
    val onRegisterBranchSubmit: () -> Unit,
    val onRegisterBranchDismiss: () -> Unit,

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
