package com.puregoldgo.ibms.ui.screen.finance

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ConsoleHeader
import com.puregoldgo.ibms.ui.component.ConsoleScaffold
import com.puregoldgo.ibms.ui.component.SegmentedTabRow
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_approvals_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_approvals_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_approvals_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_approvals_variance
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_approve
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_pay
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_payments_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_payments_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_payments_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_tab_approvals
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_tab_payments
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_tab_schedule
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The finance console — the approval queue, the payment queue, and each ISP's
 * standing payment day, behind one segmented switch.
 *
 * Runs on sample data this pass; see [FinanceSampleData] for why.
 */
@Composable
fun FinanceScreen(
    onSignedOut: () -> Unit,
    viewModel: FinanceViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FinanceUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    FinanceContent(
        uiState = uiState,
        callback = FinanceCallback(
            onTabSelect = viewModel::onTabSelect,
            onApprovalQueryChange = viewModel::onApprovalQueryChange,
            onApprovalProviderSelect = viewModel::onApprovalProviderSelect,
            onApproveClick = viewModel::onApproveClick,
            onPaymentQueryChange = viewModel::onPaymentQueryChange,
            onPaymentProviderSelect = viewModel::onPaymentProviderSelect,
            onPayClick = viewModel::onPayClick,
            onExportClick = viewModel::onExportClick,
            onActionConfirm = viewModel::onActionConfirm,
            onActionDismiss = viewModel::onActionDismiss,
            onRetryLoad = viewModel::loadPanel,
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so `FinanceScreenPreview.kt` can draw it.
 */
@Composable
internal fun FinanceContent(
    uiState: FinanceUIState,
    callback: FinanceCallback,
) {
    ConsoleScaffold(
        userName = uiState.userName,
        userRole = uiState.userRole,
        onLogoutClick = callback.onLogoutClick,
    ) { layout ->
        val isCompact = layout.isCompact

        ConsoleHeader(
            title = stringResource(Res.string.finance_title),
            subtitle = stringResource(Res.string.finance_subtitle),
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        SegmentedTabRow(
            tabs = listOf(
                FinanceTab.Approvals to stringResource(Res.string.finance_tab_approvals),
                FinanceTab.Payments to stringResource(Res.string.finance_tab_payments),
                FinanceTab.PaymentSchedule to stringResource(Res.string.finance_tab_schedule),
            ),
            selected = uiState.selectedTab,
            onSelect = callback.onTabSelect,
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        when (uiState.selectedTab) {
            FinanceTab.Approvals -> TopSheetQueue(
                uiState = uiState,
                callback = callback,
                isCompact = isCompact,
                sheets = uiState.visibleApprovals,
                title = Res.string.finance_approvals_title,
                searchHint = Res.string.finance_approvals_search_hint,
                emptyMessage = Res.string.finance_approvals_empty,
                query = uiState.approvalQuery,
                onQueryChange = callback.onApprovalQueryChange,
                providerId = uiState.approvalProviderId,
                onProviderSelect = callback.onApprovalProviderSelect,
                actionIcon = AppIcons.CheckCircle,
                actionLabel = Res.string.finance_approve,
                onAction = callback.onApproveClick,
                // The number that decides whether this queue can be cleared in
                // one sitting. Led with rather than left to be counted by eye.
                note = if (uiState.varianceCount > 0) {
                    { VarianceNote(count = uiState.varianceCount) }
                } else {
                    null
                },
            )

            FinanceTab.Payments -> TopSheetQueue(
                uiState = uiState,
                callback = callback,
                isCompact = isCompact,
                sheets = uiState.visiblePayments,
                title = Res.string.finance_payments_title,
                searchHint = Res.string.finance_payments_search_hint,
                emptyMessage = Res.string.finance_payments_empty,
                query = uiState.paymentQuery,
                onQueryChange = callback.onPaymentQueryChange,
                providerId = uiState.paymentProviderId,
                onProviderSelect = callback.onPaymentProviderSelect,
                actionIcon = AppIcons.CheckCircle,
                actionLabel = Res.string.finance_pay,
                onAction = callback.onPayClick,
                // Export is offered here and not on approvals: the spreadsheet
                // is what gets sent to the vendor with the payment.
                onExport = callback.onExportClick,
            )

            FinanceTab.PaymentSchedule -> PaymentScheduleTab(uiState, callback, isCompact)
        }

        uiState.pendingAction?.let { action ->
            ConfirmActionDialog(
                action = action,
                canConfirm = uiState.canConfirmAction,
                isSubmitting = uiState.isSubmitting,
                error = uiState.actionError,
                callback = callback,
            )
        }
    }
}

@Composable
private fun VarianceNote(count: Int) {
    Text(
        text = stringResource(Res.string.finance_approvals_variance, count),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
