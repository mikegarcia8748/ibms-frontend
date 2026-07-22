package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.component.ConsoleHeader
import com.puregoldgo.ibms.ui.component.ConsoleScaffold
import com.puregoldgo.ibms.ui.component.SegmentedTabRow
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_archive
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_billing
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_branches
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_compile
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_floating
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The secretary console — topsheet compilation, the branch and ISP account
 * registries, billing history, and the two panels that catch what the others
 * leave behind.
 *
 * Runs on sample data this pass; see [SecretarySampleData] for why.
 */
@Composable
fun SecretaryScreen(
    onSignedOut: () -> Unit,
    viewModel: SecretaryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SecretaryUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    SecretaryContent(
        uiState = uiState,
        callback = SecretaryCallback(
            onTabSelect = viewModel::onTabSelect,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onBranchProviderSelect = viewModel::onBranchProviderSelect,
            onAccountQueryChange = viewModel::onAccountQueryChange,
            onAccountLetterSelect = viewModel::onAccountLetterSelect,
            onAccountProviderSelect = viewModel::onAccountProviderSelect,
            onAccountStatusSelect = viewModel::onAccountStatusSelect,
            onExportAccounts = viewModel::onExportAccounts,
            onInvoiceQueryChange = viewModel::onInvoiceQueryChange,
            onAddBranchClick = viewModel::onAddBranchClick,
            onNewBranchCodeChange = viewModel::onNewBranchCodeChange,
            onNewBranchNameChange = viewModel::onNewBranchNameChange,
            onNewBranchCityChange = viewModel::onNewBranchCityChange,
            onNewBranchProviderChange = viewModel::onNewBranchProviderChange,
            onAddBranchSubmit = viewModel::onAddBranchSubmit,
            onAddBranchDismiss = viewModel::onAddBranchDismiss,
            onAddAccountClick = viewModel::onAddAccountClick,
            onNewAccountNumberChange = viewModel::onNewAccountNumberChange,
            onNewAccountStoreChange = viewModel::onNewAccountStoreChange,
            onNewAccountProviderChange = viewModel::onNewAccountProviderChange,
            onNewAccountRateChange = viewModel::onNewAccountRateChange,
            onAddAccountSubmit = viewModel::onAddAccountSubmit,
            onAddAccountDismiss = viewModel::onAddAccountDismiss,
            onRetryLoad = viewModel::loadPanel,
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so `SecretaryScreenPreview.kt` can draw it.
 */
@Composable
internal fun SecretaryContent(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
) {
    ConsoleScaffold(
        userName = uiState.userName,
        userRole = uiState.userRole,
        onLogoutClick = callback.onLogoutClick,
    ) { layout ->
        val isCompact = layout.isCompact

        ConsoleHeader(
            title = stringResource(Res.string.secretary_title),
            subtitle = stringResource(Res.string.secretary_subtitle),
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        SegmentedTabRow(
            tabs = listOf(
                SecretaryTab.CompileTopSheet to
                    stringResource(Res.string.secretary_tab_compile),
                SecretaryTab.BranchLocations to
                    stringResource(Res.string.secretary_tab_branches),
                SecretaryTab.IspAccounts to
                    stringResource(Res.string.secretary_tab_accounts),
                SecretaryTab.BillingHistory to
                    stringResource(Res.string.secretary_tab_billing),
                SecretaryTab.Archive to
                    stringResource(Res.string.secretary_tab_archive),
                SecretaryTab.FloatingAccounts to
                    stringResource(Res.string.secretary_tab_floating),
            ),
            selected = uiState.selectedTab,
            onSelect = callback.onTabSelect,
            // Six labels fit only at the full capped width; anything narrower
            // has to scroll, well before the body needs its compact layout.
            isCompact = layout.isTabRowCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        when (uiState.selectedTab) {
            SecretaryTab.CompileTopSheet -> CompileTopSheetTab()
            SecretaryTab.BranchLocations -> BranchLocationsTab(uiState, callback, isCompact)
            SecretaryTab.IspAccounts -> IspAccountsTab(uiState, callback, isCompact)
            SecretaryTab.BillingHistory -> BillingHistoryTab(uiState, callback, isCompact)
            SecretaryTab.Archive -> ArchiveTab(uiState, isCompact)
            SecretaryTab.FloatingAccounts -> FloatingAccountsTab(uiState)
        }

        // Mutually exclusive by construction: the ViewModel clears one when it
        // opens the other, so only ever one form is on screen.
        uiState.addBranch?.let { form ->
            AddBranchDialog(
                form = form,
                canSubmit = uiState.canSubmitBranch,
                providers = uiState.activeProviders,
                callback = callback,
            )
        }
        uiState.addAccount?.let { form ->
            AddAccountDialog(
                form = form,
                canSubmit = uiState.canSubmitAccount,
                branches = uiState.visibleBranches,
                providers = uiState.activeProviders,
                callback = callback,
            )
        }
    }
}
