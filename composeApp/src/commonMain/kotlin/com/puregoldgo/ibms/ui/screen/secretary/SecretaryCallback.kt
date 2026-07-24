package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.ibms.shared.model.StoreType

/**
 * Everything the secretary console can do, bundled so the content composables
 * stay free of the ViewModel and remain previewable.
 */
data class SecretaryCallback(
    val onTabSelect: (SecretaryTab) -> Unit,

    // Branch locations.
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,
    val onBranchProviderSelect: (String?) -> Unit,

    // ISP accounts.
    val onAccountQueryChange: (String) -> Unit,
    val onAccountLetterSelect: (Char) -> Unit,
    val onAccountProviderSelect: (String?) -> Unit,
    val onAccountStatusSelect: (AccountRecordStatus?) -> Unit,
    val onExportAccounts: () -> Unit,

    // Billing history.
    val onInvoiceQueryChange: (String) -> Unit,

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

    // Add account.
    val onAddAccountClick: () -> Unit,
    val onNewAccountNumberChange: (String) -> Unit,
    val onNewAccountStoreChange: (String?) -> Unit,
    val onNewAccountProviderChange: (String?) -> Unit,
    val onNewAccountRateChange: (String) -> Unit,
    val onAddAccountSubmit: () -> Unit,
    val onAddAccountDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
