package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.ibms.platform.file.PickedFile

/**
 * Everything the secretary console can do, bundled so the content composables
 * stay free of the ViewModel and remain previewable.
 *
 * Deliberately a plain class rather than a data class: storing lambdas in a
 * data class invites unwanted equals/hashCode behaviour on recompositions.
 */
class SecretaryCallback(
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
    val onTopSheetClick: (String) -> Unit,

    // Add branch.
    val onAddBranchClick: () -> Unit,
    val onNewBranchCodeChange: (String) -> Unit,
    val onNewBranchNameChange: (String) -> Unit,
    val onNewBranchCityChange: (String) -> Unit,
    val onNewBranchProviderChange: (String?) -> Unit,
    val onAddBranchSubmit: () -> Unit,
    val onAddBranchDismiss: () -> Unit,

    // Add account.
    val onAddAccountClick: () -> Unit,
    val onNewAccountNumberChange: (String) -> Unit,
    val onNewAccountStoreChange: (String?) -> Unit,
    val onNewAccountProviderChange: (String?) -> Unit,
    val onNewAccountRateChange: (String) -> Unit,
    val onNewAccountInstallationDateChange: (String) -> Unit,
    val onNewAccountCircuitIdChange: (String) -> Unit,
    val onNewAccountBillingPeriodChange: (String) -> Unit,
    val onNewAccountPlanChange: (String) -> Unit,
    val onNewAccountProofPicked: (PickedFile) -> Unit,
    val onNewAccountProofRemove: (String) -> Unit,
    val onAddAccountSubmit: () -> Unit,
    val onAddAccountSubmitConfirmed: () -> Unit,
    val onAddAccountDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,

    // Detail modals.
    val onBranchClick: (String) -> Unit,
    val onStoreDetailDismiss: () -> Unit,
    val onAccountClick: (String) -> Unit,
    val onAccountDetailDismiss: () -> Unit,

    // Deactivate account.
    val onDeactivateAccountClick: () -> Unit,
    val onDeactivateAccountDismiss: () -> Unit,
    val onDeactivateAccountFilePicked: (PickedFile) -> Unit,
    val onDeactivateAccountConfirm: () -> Unit,
)
