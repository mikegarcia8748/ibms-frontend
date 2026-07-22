package com.puregoldgo.ibms.ui.screen.finance

/**
 * Everything the finance console can do, bundled so the content composables stay
 * free of the ViewModel and remain previewable.
 */
data class FinanceCallback(
    val onTabSelect: (FinanceTab) -> Unit,

    // Approvals.
    val onApprovalQueryChange: (String) -> Unit,
    val onApprovalProviderSelect: (String?) -> Unit,
    val onApproveClick: (FinanceTopSheetRow) -> Unit,

    // Payments.
    val onPaymentQueryChange: (String) -> Unit,
    val onPaymentProviderSelect: (String?) -> Unit,
    val onPayClick: (FinanceTopSheetRow) -> Unit,

    /** Downloads `GET /exports/topsheet/{id}.xlsx` — the one export finance owns. */
    val onExportClick: (FinanceTopSheetRow) -> Unit,

    val onActionConfirm: () -> Unit,
    val onActionDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
