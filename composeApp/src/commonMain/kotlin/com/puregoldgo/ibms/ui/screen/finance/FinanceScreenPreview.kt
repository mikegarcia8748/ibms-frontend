package com.puregoldgo.ibms.ui.screen.finance

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the finance console.
 *
 * Kept beside [FinanceScreen] rather than inside it, the convention both other
 * consoles follow. Nothing here ships — it draws [FinanceContent] against
 * [FinanceSampleData].
 */

@Preview(name = "Finance — approvals", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun FinanceApprovalsWidePreview() {
    AppTheme {
        FinanceContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Finance — payments", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun FinancePaymentsWidePreview() {
    AppTheme {
        FinanceContent(
            uiState = previewState().copy(selectedTab = FinanceTab.Payments),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Finance — payment schedule", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun FinanceScheduleWidePreview() {
    AppTheme {
        FinanceContent(
            uiState = previewState().copy(selectedTab = FinanceTab.PaymentSchedule),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Finance — approve confirmation", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun FinanceApproveConfirmPreview() {
    AppTheme {
        FinanceContent(
            uiState = previewState().let { state ->
                state.copy(
                    pendingAction = TopSheetAction(
                        sheet = state.visibleApprovals.first(),
                        kind = TopSheetAction.Kind.Approve,
                    ),
                )
            },
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Finance — empty queue", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun FinanceEmptyPreview() {
    AppTheme {
        FinanceContent(
            uiState = FinanceUIState(userName = "Michael Garcia", userRole = "finance"),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Finance — approvals", group = "MobileApp")
@Composable
private fun FinanceApprovalsPreview() {
    AppTheme {
        FinanceContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Finance — payment schedule", group = "MobileApp")
@Composable
private fun FinanceSchedulePreview() {
    AppTheme {
        FinanceContent(
            uiState = previewState().copy(selectedTab = FinanceTab.PaymentSchedule),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = FinanceUIState(
    userName = "Michael Garcia",
    userRole = "finance",
    providers = FinanceSampleData.providers,
    topSheets = FinanceSampleData.topSheets,
)

private fun previewCallback() = FinanceCallback(
    onTabSelect = {},
    onApprovalQueryChange = {},
    onApprovalProviderSelect = {},
    onApproveClick = {},
    onPaymentQueryChange = {},
    onPaymentProviderSelect = {},
    onPayClick = {},
    onExportClick = {},
    onActionConfirm = {},
    onActionDismiss = {},
    onRetryLoad = {},
    onLogoutClick = {},
)
