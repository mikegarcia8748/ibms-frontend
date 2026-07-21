package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the sysadmin control panel.
 *
 * Kept beside [SysadminScreen] rather than inside it: the screen is long
 * enough already, and a dozen preview functions between the reader and the
 * composables they exercise is a dozen functions of noise. Nothing here ships —
 * it draws [SysadminContent] against [SysadminSampleData].
 */

@Preview(name = "Sysadmin — directory", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminDirectoryWidePreview() {
    AppTheme {
        SysadminContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Sysadmin — stores", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminStoresWidePreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(selectedTab = SysadminTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — accounts", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminAccountsWidePreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(selectedTab = SysadminTab.Accounts),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — directory", group = "MobileApp")
@Composable
private fun SysadminDirectoryPreview() {
    AppTheme {
        SysadminContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Sysadmin — stores", group = "MobileApp")
@Composable
private fun SysadminStoresPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(selectedTab = SysadminTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — empty states", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminEmptyPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(
                // Nobody in the directory, and a branch query that matches nothing.
                users = emptyList(),
                branchQuery = "zzzz",
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — import, no file", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportEmptyPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(isBulkImportOpen = true),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — import, file chosen", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportChosenPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — import summary", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportSummaryPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
                bulkImportSummary = SysadminSampleData.bulkImportSummary,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — add user", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminAddUserPreview() {
    AppTheme {
        SysadminContent(
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

@Preview(name = "Sysadmin — password issued", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminCredentialIssuedPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(
                userAdmin = UserAdminUIState(
                    isAddOpen = true,
                    issued = SysadminSampleData.issuedCredential,
                ),
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Sysadmin — reset confirm", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminResetPasswordPreview() {
    AppTheme {
        SysadminContent(
            uiState = previewState().copy(
                userAdmin = UserAdminUIState(
                    resetTarget = SysadminSampleData.users.first(),
                ),
            ),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = SysadminUIState(
    userName = "Michael Garcia",
    userRole = "sysadmin",
    users = SysadminSampleData.users,
    activeProviders = SysadminSampleData.activeProviders,
    branches = SysadminSampleData.branches,
    accounts = SysadminSampleData.accounts,
)

private fun previewCallback() = SysadminCallback(
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
