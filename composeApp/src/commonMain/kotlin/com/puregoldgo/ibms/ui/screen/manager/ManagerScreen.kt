package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.component.ConsoleHeader
import com.puregoldgo.ibms.ui.component.ConsoleScaffold
import com.puregoldgo.ibms.ui.component.SegmentedTabRow
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchCallback
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchDialog
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_tab_activity
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_tab_branches
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_tab_overview
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_tab_topsheets
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The oversight console — the period's spend, the topsheets behind it, and the
 * audit trail, behind one segmented switch.
 *
 * Read-only throughout: the role holds no write endpoint in the contract, so
 * this console reports and never edits.
 *
 * Runs on sample data this pass; see [ManagerSampleData] for why.
 */
@Composable
fun ManagerScreen(
    onSignedOut: () -> Unit,
    viewModel: ManagerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ManagerUiEvent.NavigateToLogin -> onSignedOut()
                is ManagerUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    ManagerContent(
        uiState = uiState,
        callback = ManagerCallback(
            onTabSelect = viewModel::onTabSelect,
            onTopSheetQueryChange = viewModel::onTopSheetQueryChange,
            onActivityQueryChange = viewModel::onActivityQueryChange,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onRegisterBranchClick = viewModel::onRegisterBranchClick,
            onRegisterBranchStoreTypeChange = viewModel::onRegisterBranchStoreTypeChange,
            onRegisterBranchCodeChange = viewModel::onRegisterBranchCodeChange,
            onRegisterBranchNameChange = viewModel::onRegisterBranchNameChange,
            onRegisterBranchRegionChange = viewModel::onRegisterBranchRegionChange,
            onRegisterBranchProvinceChange = viewModel::onRegisterBranchProvinceChange,
            onRegisterBranchCityChange = viewModel::onRegisterBranchCityChange,
            onRegisterBranchBarangayChange = viewModel::onRegisterBranchBarangayChange,
            onRegisterBranchPostalCodeChange = viewModel::onRegisterBranchPostalCodeChange,
            onRegisterBranchSubmit = viewModel::onRegisterBranchSubmit,
            onRegisterBranchDismiss = viewModel::onRegisterBranchDismiss,
            onRetryLoad = viewModel::loadPanel,
            onLogoutClick = viewModel::onLogout,
        ),
        snackbarHostState = snackbarHostState,
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so `ManagerScreenPreview.kt` can draw it.
 */
@Composable
internal fun ManagerContent(
    uiState: ManagerUIState,
    callback: ManagerCallback,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    ConsoleScaffold(
        userName = uiState.userName,
        userRole = uiState.userRole,
        onLogoutClick = callback.onLogoutClick,
        snackbarHostState = snackbarHostState,
    ) { layout ->
        val isCompact = layout.isCompact

        ConsoleHeader(
            title = stringResource(Res.string.manager_title),
            subtitle = stringResource(Res.string.manager_subtitle),
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        SegmentedTabRow(
            tabs = listOf(
                ManagerTab.Overview to stringResource(Res.string.manager_tab_overview),
                ManagerTab.TopSheets to stringResource(Res.string.manager_tab_topsheets),
                ManagerTab.Branches to stringResource(Res.string.manager_tab_branches),
                ManagerTab.Activity to stringResource(Res.string.manager_tab_activity),
            ),
            selected = uiState.selectedTab,
            onSelect = callback.onTabSelect,
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        when (uiState.selectedTab) {
            ManagerTab.Overview -> OverviewTab(uiState, callback, isCompact)
            ManagerTab.TopSheets -> TopSheetsTab(uiState, callback, isCompact)
            ManagerTab.Branches -> BranchesTab(uiState, callback, isCompact)
            ManagerTab.Activity -> ActivityTab(uiState, callback, isCompact)
        }

        uiState.registerBranchForm?.let { form ->
            RegisterBranchDialog(
                form = form,
                callback = RegisterBranchCallback(
                    onStoreTypeChange = callback.onRegisterBranchStoreTypeChange,
                    onBranchCodeChange = callback.onRegisterBranchCodeChange,
                    onBranchNameChange = callback.onRegisterBranchNameChange,
                    onRegionChange = callback.onRegisterBranchRegionChange,
                    onProvinceChange = callback.onRegisterBranchProvinceChange,
                    onCityChange = callback.onRegisterBranchCityChange,
                    onBarangayChange = callback.onRegisterBranchBarangayChange,
                    onPostalCodeChange = callback.onRegisterBranchPostalCodeChange,
                    onSubmit = callback.onRegisterBranchSubmit,
                    onDismiss = callback.onRegisterBranchDismiss,
                ),
                isCompact = isCompact,
            )
        }
    }
}
