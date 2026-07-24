package com.puregoldgo.ibms.ui.screen.store

import com.puregoldgo.ibms.shared.model.StoreType

/**
 * Bundles every UI action the [RegisterBranchDialog] can emit.
 *
 * A single callback object keeps the dialog's signature small and makes the
 * composable reusable across the secretary, sysadmin and manager consoles.
 */
class RegisterBranchCallback(
    val onStoreTypeChange: (StoreType) -> Unit,
    val onBranchCodeChange: (String) -> Unit,
    val onBranchNameChange: (String) -> Unit,
    val onRegionChange: (String) -> Unit,
    val onProvinceChange: (String) -> Unit,
    val onCityChange: (String) -> Unit,
    val onBarangayChange: (String) -> Unit,
    val onPostalCodeChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onDismiss: () -> Unit,
)
