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
import com.puregoldgo.ibms.ui.screen.secretary.compile.CompileCallback
import com.puregoldgo.ibms.ui.screen.secretary.compile.CompileTopSheetTab
import com.puregoldgo.ibms.ui.screen.secretary.compile.CompileUIState
import com.puregoldgo.ibms.ui.screen.secretary.compile.CompileViewModel
import com.puregoldgo.ibms.ui.screen.secretary.compile.NoOpCompileCallback
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
    onNavigateToTopSheetDetail: (String) -> Unit,
    viewModel: SecretaryViewModel = koinViewModel(),
    compileViewModel: CompileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val compileState by compileViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SecretaryUiEvent.NavigateToLogin -> onSignedOut()
                is SecretaryUiEvent.NavigateToTopSheetDetail -> onNavigateToTopSheetDetail(event.topSheetId)
            }
        }
    }

    SecretaryContent(
        uiState = uiState,
        compileState = compileState,
        compileCallback = CompileCallback(
            onPreviousMonth = compileViewModel::onPreviousMonth,
            onNextMonth = compileViewModel::onNextMonth,
            onProviderSelect = compileViewModel::onProviderSelect,
            onQueryChange = compileViewModel::onQueryChange,
            onLetterSelect = compileViewModel::onLetterSelect,
            onCompileClick = compileViewModel::onCompileClick,
            onRetryLoad = compileViewModel::loadContext,
            onRfpChange = compileViewModel::onRfpChange,
            onRfpCommit = compileViewModel::onRfpCommit,
            onRfpRangeStartChange = compileViewModel::onRfpRangeStartChange,
            onRfpRangeEndChange = compileViewModel::onRfpRangeEndChange,
            onAssignRfpClick = compileViewModel::onAssignRfpClick,
            onRemoveLine = compileViewModel::onRemoveLine,
            onRetryLines = compileViewModel::onRetryLines,
            onBackToReview = compileViewModel::onBackToReview,
            onConfirmClick = compileViewModel::onConfirmClick,
            onLinesQueryChange = compileViewModel::onLinesQueryChange,
            onLinesLetterSelect = compileViewModel::onLinesLetterSelect,
            onLinesSortSelect = compileViewModel::onLinesSortSelect,
            onStartNew = compileViewModel::onStartNew,
            onResumeDraftClick = compileViewModel::onResumeDraftClick,
            onResumeDraft = compileViewModel::onResumeDraft,
            onResumeDraftDismiss = compileViewModel::onResumeDraftDismiss,
        ),
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
            onTopSheetClick = viewModel::onTopSheetClick,
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
            onBranchClick = viewModel::onBranchClick,
            onStoreDetailDismiss = viewModel::onStoreDetailDismiss,
            onAccountClick = viewModel::onAccountClick,
            onAccountDetailDismiss = viewModel::onAccountDetailDismiss,
            onDeactivateAccountClick = viewModel::onDeactivateAccountClick,
            onDeactivateAccountDismiss = viewModel::onDeactivateAccountDismiss,
            onDeactivateAccountFilePicked = viewModel::onDeactivateAccountFilePicked,
            onDeactivateAccountConfirm = viewModel::onDeactivateAccountConfirm,
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
    compileState: CompileUIState = CompileUIState(),
    compileCallback: CompileCallback = NoOpCompileCallback,
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
            SecretaryTab.CompileTopSheet -> CompileTopSheetTab(compileState, compileCallback, isCompact)
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
        // Ordered before the store/account modals so those overlay on top: an
        // account row inside this dialog opens AccountDetailsDialog above it.
        uiState.storeDetail?.let { detail ->
            StoreDetailsDialog(detail = detail, callback = callback)
        }
        uiState.accountDetail?.let { detail ->
            AccountDetailsDialog(
                detail = detail,
                onDismiss = callback.onAccountDetailDismiss,
                onDeactivateClick = callback.onDeactivateAccountClick,
            )
        }
        uiState.deactivateAccount?.let { form ->
            DeactivateAccountDialog(form = form, callback = callback)
        }
    }
}
