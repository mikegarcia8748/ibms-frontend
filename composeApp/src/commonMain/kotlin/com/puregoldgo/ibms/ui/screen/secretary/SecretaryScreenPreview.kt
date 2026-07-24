package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.shared.model.StoreType
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the secretary console.
 *
 * Kept beside [SecretaryScreen] rather than inside it: the screen is long
 * enough already, and a stack of preview functions between the reader and the
 * composables they exercise is noise. Nothing here ships — it draws
 * [SecretaryContent] against [SecretarySampleData].
 */

@Preview(name = "Secretary — compile topsheet", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryCompileWidePreview() {
    AppTheme {
        SecretaryContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Secretary — branches", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryBranchesWidePreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.BranchLocations),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — accounts", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryAccountsWidePreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.IspAccounts),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — billing history", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryBillingWidePreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.BillingHistory),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — archive", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryArchiveWidePreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.Archive),
            callback = previewCallback(),
        )
    }
}

/**
 * The panel doing its job — the sample deliberately strands one account, so this
 * is the populated case.
 */
@Preview(name = "Secretary — floating, found", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryFloatingFoundPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.FloatingAccounts),
            callback = previewCallback(),
        )
    }
}

/**
 * The all-clear.
 *
 * Reached by reopening the closed branch rather than by deleting the account:
 * that is the state the list is actually derived from, so this proves the
 * derivation empties instead of proving an empty list draws.
 */
@Preview(name = "Secretary — floating, clear", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryFloatingClearPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.FloatingAccounts,
                branches = SecretarySampleData.branches.map { branch ->
                    if (branch.isClosed) {
                        branch.copy(status = BranchRecordStatus.Active, closureReason = null)
                    } else {
                        branch
                    }
                },
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — empty states", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryEmptyPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.BranchLocations,
                // A query that matches nothing, rather than an empty list: the
                // "no branches" line and a failed load look identical otherwise.
                branchQuery = "zzzz",
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — loading", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryLoadingPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.IspAccounts,
                isLoading = true,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — load failed", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryErrorPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.IspAccounts,
                loadError = "The account list could not be loaded.",
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — add branch", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryAddBranchPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.BranchLocations,
                registerBranchForm = RegisterBranchForm(
                    storeType = StoreType.PUREGOLD,
                    branchCode = "8231",
                    branchName = "BALIBAGO",
                    city = "Sta. Rosa Laguna",
                ),
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — add account", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryAddAccountPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.IspAccounts,
                addAccount = NewAccountForm(
                    accountNumber = "48575443CE0311B2",
                    storeId = "br-malolos",
                    providerId = SecretarySampleData.PROVIDER_GLOBE,
                    monthlyRate = "2,000.00",
                ),
            ),
            callback = previewCallback(),
        )
    }
}

/** Empty form: the submit is disabled here, which is the state worth checking. */
@Preview(name = "Secretary — add branch, blank", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SecretaryAddBranchBlankPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(
                selectedTab = SecretaryTab.BranchLocations,
                registerBranchForm = RegisterBranchForm(),
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — branches", group = "MobileApp")
@Composable
private fun SecretaryBranchesPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.BranchLocations),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — accounts", group = "MobileApp")
@Composable
private fun SecretaryAccountsPreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.IspAccounts),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Secretary — archive", group = "MobileApp")
@Composable
private fun SecretaryArchivePreview() {
    AppTheme {
        SecretaryContent(
            uiState = previewState().copy(selectedTab = SecretaryTab.Archive),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = SecretaryUIState(
    userName = "Michael Garcia",
    userRole = "secretary",
    providers = SecretarySampleData.providers,
    branches = SecretarySampleData.branches,
    accounts = SecretarySampleData.accounts,
    topSheets = SecretarySampleData.topSheets,
)

private fun previewCallback() = SecretaryCallback(
    onTabSelect = {},
    onBranchQueryChange = {},
    onBranchLetterSelect = {},
    onBranchProviderSelect = {},
    onAccountQueryChange = {},
    onAccountLetterSelect = {},
    onAccountProviderSelect = {},
    onAccountStatusSelect = {},
    onExportAccounts = {},
    onInvoiceQueryChange = {},
    onRegisterBranchClick = {},
    onRegisterBranchStoreTypeChange = {},
    onRegisterBranchCodeChange = {},
    onRegisterBranchNameChange = {},
    onRegisterBranchRegionChange = {},
    onRegisterBranchProvinceChange = {},
    onRegisterBranchCityChange = {},
    onRegisterBranchBarangayChange = {},
    onRegisterBranchPostalCodeChange = {},
    onRegisterBranchSubmit = {},
    onRegisterBranchDismiss = {},
    onAddAccountClick = {},
    onNewAccountNumberChange = {},
    onNewAccountStoreChange = {},
    onNewAccountProviderChange = {},
    onNewAccountRateChange = {},
    onAddAccountSubmit = {},
    onAddAccountDismiss = {},
    onRetryLoad = {},
    onLogoutClick = {},
)
