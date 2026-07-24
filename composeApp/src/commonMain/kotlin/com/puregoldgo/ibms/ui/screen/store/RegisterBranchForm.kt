package com.puregoldgo.ibms.ui.screen.store

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.model.StoreType

/**
 * The register-branch dialog's form state.
 *
 * Kept separate from the dashboard UI states so the dialog can move between
 * consoles without dragging role-specific state with it.
 */
@Immutable
data class RegisterBranchForm(
    val storeType: StoreType = StoreType.PUREGOLD,
    val branchCode: String = "",
    val branchName: String = "",
    val region: String = "",
    val province: String = "",
    val city: String = "",
    val barangay: String = "",
    val postalCode: String = "",
    val isSaving: Boolean = false,
    val branchCodeError: String? = null,
    val branchNameError: String? = null,
) {
    /** Both required fields must be present before the submit button is enabled. */
    val canSubmit: Boolean
        get() = branchCode.isNotBlank() && branchName.isNotBlank() && !isSaving
}
