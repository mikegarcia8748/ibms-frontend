package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the sysadmin control panel.
 *
 * Kept beside [DashboardScreen] rather than inside it: the screen is long
 * enough already, and a dozen preview functions between the reader and the
 * composables they exercise is a dozen functions of noise. Nothing here ships —
 * it draws [DashboardContent] against [DashboardSampleData].
 */

@Preview(name = "Dashboard — directory", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardDirectoryWidePreview() {
    AppTheme {
        DashboardContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Dashboard — stores", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardStoresWidePreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — accounts", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardAccountsWidePreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Accounts),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — directory", group = "MobileApp")
@Composable
private fun DashboardDirectoryPreview() {
    AppTheme {
        DashboardContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Dashboard — stores", group = "MobileApp")
@Composable
private fun DashboardStoresPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — empty states", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardEmptyPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                // Nobody in the directory, and a branch query that matches nothing.
                users = emptyList(),
                branchQuery = "zzzz",
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import, no file", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportEmptyPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(isBulkImportOpen = true),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import, file chosen", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportChosenPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import summary", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportSummaryPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
                bulkImportSummary = DashboardSampleData.bulkImportSummary,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — add user", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardAddUserPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                userAdmin = UserAdminUIState(
                    isAddOpen = true,
                    form = NewUserForm(
                        username = "rlim",
                        employeeNumber = "010007422",
                        role = Role.SECRETARY,
                        firstName = "Rosario",
                        middleInitial = "D",
                        lastName = "Lim",
                    ),
                ),
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — password issued", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardCredentialIssuedPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                userAdmin = UserAdminUIState(
                    isAddOpen = true,
                    issued = DashboardSampleData.issuedCredential,
                ),
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — reset confirm", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardResetPasswordPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                userAdmin = UserAdminUIState(
                    resetTarget = DashboardSampleData.users.first(),
                ),
            ),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = DashboardUIState(
    userName = "Michael Garcia",
    userRole = "sysadmin",
    users = DashboardSampleData.users,
    activeProviders = DashboardSampleData.activeProviders,
    branches = DashboardSampleData.branches,
    accounts = DashboardSampleData.accounts,
)

private fun previewCallback() = DashboardCallback(
    onTabSelect = {},
    onBranchQueryChange = {},
    onBranchLetterSelect = {},
    onBranchProviderSelect = {},
    onAccountQueryChange = {},
    onAccountProviderSelect = {},
    onBulkUploadClick = {},
    onBulkImportFilePicked = {},
    onBulkImportStart = {},
    onBulkUploadDismiss = {},
    onAddUserClick = {},
    onNewUserUsernameChange = {},
    onNewUserEmployeeNumberChange = {},
    onNewUserRoleChange = {},
    onAddUserSubmit = {},
    onAddUserDismiss = {},
    onResetPasswordClick = {},
    onResetPasswordConfirm = {},
    onResetPasswordDismiss = {},
    onRetryLoad = {},
    onUserQueryChange = {},
    onNewUserFirstNameChange = {},
    onNewUserMiddleInitialChange = {},
    onNewUserLastNameChange = {},
    onChangeRoleClick = {},
    onRoleSelectionChange = {},
    onChangeRoleConfirm = {},
    onChangeRoleDismiss = {},
    onUserStatusToggleClick = {},
    onUserStatusConfirm = {},
    onUserStatusDismiss = {},
    onLogoutClick = {},
)
