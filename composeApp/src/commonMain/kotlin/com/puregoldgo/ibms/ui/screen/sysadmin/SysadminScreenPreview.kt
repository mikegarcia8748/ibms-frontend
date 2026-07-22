package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryCallback
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryUIState
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.NewUserForm
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.UserAdminUIState
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryCallback
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryUIState
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the sysadmin control panel.
 *
 * Kept beside [SysadminScreen] rather than inside it: the screen is long enough
 * already, and a dozen preview functions between the reader and the composables
 * they exercise is a dozen functions of noise. Nothing here ships — it draws
 * [SysadminContent] against [SysadminSampleData].
 *
 * Each panel's state is built separately, which is the point of the split: a
 * preview can now put the directory in one state while the registries stay in
 * another, because they no longer share a `loadError` or a loading flag.
 */

@Preview(name = "Sysadmin — directory", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminDirectoryWidePreview() {
    AppTheme { PreviewContent() }
}

@Preview(name = "Sysadmin — stores", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminStoresWidePreview() {
    AppTheme { PreviewContent(shell = previewShell().copy(selectedTab = SysadminTab.Stores)) }
}

@Preview(name = "Sysadmin — accounts", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminAccountsWidePreview() {
    AppTheme { PreviewContent(shell = previewShell().copy(selectedTab = SysadminTab.Accounts)) }
}

@Preview(name = "Sysadmin — directory", group = "MobileApp")
@Composable
private fun SysadminDirectoryPreview() {
    AppTheme { PreviewContent() }
}

@Preview(name = "Sysadmin — stores", group = "MobileApp")
@Composable
private fun SysadminStoresPreview() {
    AppTheme { PreviewContent(shell = previewShell().copy(selectedTab = SysadminTab.Stores)) }
}

@Preview(name = "Sysadmin — empty states", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminEmptyPreview() {
    AppTheme {
        PreviewContent(directory = DirectoryUIState(), registry = RegistryUIState())
    }
}

@Preview(name = "Sysadmin — import, no file", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportEmptyPreview() {
    AppTheme {
        PreviewContent(registry = previewRegistry().copy(isBulkImportOpen = true))
    }
}

@Preview(name = "Sysadmin — import, file chosen", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportChosenPreview() {
    AppTheme {
        PreviewContent(
            registry = previewRegistry().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "isp-master-list-june.xlsx",
                bulkImportFileSize = 348_160,
            ),
        )
    }
}

@Preview(name = "Sysadmin — import summary", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminBulkImportSummaryPreview() {
    AppTheme {
        PreviewContent(
            registry = previewRegistry().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "isp-master-list-june.xlsx",
                bulkImportFileSize = 348_160,
                bulkImportSummary = SysadminSampleData.bulkImportSummary,
            ),
        )
    }
}

@Preview(name = "Sysadmin — add user", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminAddUserPreview() {
    AppTheme {
        PreviewContent(
            directory = previewDirectory().copy(
                userAdmin = UserAdminUIState(
                    isAddOpen = true,
                    form = NewUserForm(
                        firstName = "Rosario",
                        middleInitial = "D",
                        lastName = "Lim",
                        username = "rlim",
                        employeeNumber = "010007422",
                    ),
                ),
            ),
        )
    }
}

@Preview(name = "Sysadmin — password issued", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminCredentialIssuedPreview() {
    AppTheme {
        PreviewContent(
            directory = previewDirectory().copy(
                userAdmin = UserAdminUIState(
                    isAddOpen = true,
                    issued = SysadminSampleData.issuedCredential,
                ),
            ),
        )
    }
}

@Preview(name = "Sysadmin — reset confirm", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SysadminResetPasswordPreview() {
    AppTheme {
        PreviewContent(
            directory = previewDirectory().copy(
                userAdmin = UserAdminUIState(resetTarget = SysadminSampleData.users.first()),
            ),
        )
    }
}

/** The whole console against sample data, with any one panel's state swapped in. */
@Composable
private fun PreviewContent(
    shell: SysadminUIState = previewShell(),
    directory: DirectoryUIState = previewDirectory(),
    registry: RegistryUIState = previewRegistry(),
) {
    SysadminContent(
        uiState = shell,
        callback = previewCallback(),
        directoryState = directory,
        directoryCallback = previewDirectoryCallback(),
        registryState = registry,
        registryCallback = previewRegistryCallback(),
    )
}

private fun previewShell() = SysadminUIState(
    userName = "Michael Garcia",
    userRole = "sysadmin",
)

private fun previewDirectory() = DirectoryUIState(
    currentUserId = SysadminSampleData.SIGNED_IN_USER_ID,
    users = SysadminSampleData.users,
    activeProviders = SysadminSampleData.activeProviders,
)

private fun previewRegistry() = RegistryUIState(
    activeProviders = SysadminSampleData.activeProviders,
    branches = SysadminSampleData.branches,
    accounts = SysadminSampleData.accounts,
)

private fun previewCallback() = SysadminCallback(
    onTabSelect = {},
    onLogoutClick = {},
)

private fun previewDirectoryCallback() = DirectoryCallback(
    onUserQueryChange = {},
    onAddUserClick = {},
    onNewUserFirstNameChange = {},
    onNewUserMiddleInitialChange = {},
    onNewUserLastNameChange = {},
    onNewUserUsernameChange = {},
    onNewUserEmployeeNumberChange = {},
    onNewUserRoleChange = {},
    onAddUserSubmit = {},
    onAddUserDismiss = {},
    onResetPasswordClick = {},
    onResetPasswordConfirm = {},
    onResetPasswordDismiss = {},
    onChangeRoleClick = {},
    onRoleSelectionChange = {},
    onChangeRoleConfirm = {},
    onChangeRoleDismiss = {},
    onUserStatusToggleClick = {},
    onUserStatusConfirm = {},
    onUserStatusDismiss = {},
    onRetryLoad = {},
)

private fun previewRegistryCallback() = RegistryCallback(
    onBranchQueryChange = {},
    onBranchLetterSelect = {},
    onBranchProviderSelect = {},
    onAccountQueryChange = {},
    onAccountProviderSelect = {},
    onBulkUploadClick = {},
    onBulkImportFilePicked = {},
    onBulkImportStart = {},
    onBulkUploadDismiss = {},
    onRetryLoad = {},
)
