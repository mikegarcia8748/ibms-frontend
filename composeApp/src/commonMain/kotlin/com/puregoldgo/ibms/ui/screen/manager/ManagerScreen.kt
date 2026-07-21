package com.puregoldgo.ibms.ui.screen.manager

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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_tab_activity
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

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ManagerUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    ManagerContent(
        uiState = uiState,
        callback = ManagerCallback(
            onTabSelect = viewModel::onTabSelect,
            onTopSheetQueryChange = viewModel::onTopSheetQueryChange,
            onActivityQueryChange = viewModel::onActivityQueryChange,
            onRetryLoad = viewModel::loadPanel,
            onLogoutClick = viewModel::onLogout,
        ),
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
) {
    ConsoleScaffold(
        userName = uiState.userName,
        userRole = uiState.userRole,
        onLogoutClick = callback.onLogoutClick,
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
            ManagerTab.Activity -> ActivityTab(uiState, callback, isCompact)
        }
    }
}
