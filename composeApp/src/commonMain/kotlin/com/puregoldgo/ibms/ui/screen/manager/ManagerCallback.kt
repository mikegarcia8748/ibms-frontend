package com.puregoldgo.ibms.ui.screen.manager

import com.puregoldgo.ibms.shared.model.StoreType

/**
 * Everything the oversight console can do, bundled so the content composables
 * stay free of the ViewModel and remain previewable.
 */
data class ManagerCallback(
    val onTabSelect: (ManagerTab) -> Unit,

    // Topsheet and activity filters.
    val onTopSheetQueryChange: (String) -> Unit,
    val onActivityQueryChange: (String) -> Unit,

    // Branch filters.
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,

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

    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
