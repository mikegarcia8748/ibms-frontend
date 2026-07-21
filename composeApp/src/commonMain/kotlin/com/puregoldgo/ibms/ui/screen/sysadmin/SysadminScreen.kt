package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_directory
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_stores
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The sysadmin control panel — delegations, branch locations and the ISP
 * accounts database, behind one segmented switch.
 */
@Composable
fun SysadminScreen(
    onSignedOut: () -> Unit,
    viewModel: SysadminViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SysadminUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    SysadminContent(
        uiState = uiState,
        callback = SysadminCallback(
            onTabSelect = viewModel::onTabSelect,
            onUserQueryChange = viewModel::onUserQueryChange,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onBranchProviderSelect = viewModel::onBranchProviderSelect,
            onAccountQueryChange = viewModel::onAccountQueryChange,
            onAccountProviderSelect = viewModel::onAccountProviderSelect,
            onBulkUploadClick = viewModel::onBulkUploadClick,
            onBulkImportFilePicked = viewModel::onBulkImportFilePicked,
            onBulkImportStart = viewModel::onBulkImportStart,
            onBulkUploadDismiss = viewModel::onBulkUploadDismiss,
            onAddUserClick = viewModel::onAddUserClick,
            onNewUserFirstNameChange = viewModel::onNewUserFirstNameChange,
            onNewUserMiddleInitialChange = viewModel::onNewUserMiddleInitialChange,
            onNewUserLastNameChange = viewModel::onNewUserLastNameChange,
            onNewUserUsernameChange = viewModel::onNewUserUsernameChange,
            onNewUserEmployeeNumberChange = viewModel::onNewUserEmployeeNumberChange,
            onNewUserRoleChange = viewModel::onNewUserRoleChange,
            onAddUserSubmit = viewModel::onAddUserSubmit,
            onAddUserDismiss = viewModel::onAddUserDismiss,
            onResetPasswordClick = viewModel::onResetPasswordClick,
            onResetPasswordConfirm = viewModel::onResetPasswordConfirm,
            onResetPasswordDismiss = viewModel::onResetPasswordDismiss,
            onChangeRoleClick = viewModel::onChangeRoleClick,
            onRoleSelectionChange = viewModel::onRoleSelectionChange,
            onChangeRoleConfirm = viewModel::onChangeRoleConfirm,
            onChangeRoleDismiss = viewModel::onChangeRoleDismiss,
            onUserStatusToggleClick = viewModel::onUserStatusToggleClick,
            onUserStatusConfirm = viewModel::onUserStatusConfirm,
            onUserStatusDismiss = viewModel::onUserStatusDismiss,
            onRetryLoad = { viewModel.loadPanel() },
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so [SysadminScreenPreview.kt] can draw it.
 */
@Composable
internal fun SysadminContent(
    uiState: SysadminUIState,
    callback: SysadminCallback,
) {
    ConsoleScaffold(
        userName = uiState.userName,
        userRole = uiState.userRole,
        onLogoutClick = callback.onLogoutClick,
    ) { layout ->
        val isCompact = layout.isCompact

        ConsoleHeader(
            title = stringResource(Res.string.sysadmin_title),
            subtitle = stringResource(Res.string.sysadmin_subtitle),
            isCompact = isCompact,
        ) { modifier ->
            BulkUploadButton(modifier = modifier, onClick = callback.onBulkUploadClick)
        }

        Spacer(Modifier.height(Dimensions.viewPadding24))

        SegmentedTabRow(
            tabs = listOf(
                SysadminTab.Directory to stringResource(Res.string.sysadmin_tab_directory),
                SysadminTab.Stores to stringResource(Res.string.sysadmin_tab_stores),
                SysadminTab.Accounts to stringResource(Res.string.sysadmin_tab_accounts),
            ),
            selected = uiState.selectedTab,
            onSelect = callback.onTabSelect,
            isCompact = isCompact,
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        when (uiState.selectedTab) {
            SysadminTab.Directory -> DirectoryTab(uiState, callback, isCompact)
            SysadminTab.Stores -> StoresTab(uiState, callback, isCompact)
            SysadminTab.Accounts -> AccountsTab(uiState, callback, isCompact)
        }

        if (uiState.isBulkImportOpen) {
            BulkImportDialog(
                uiState = uiState,
                onFilePicked = callback.onBulkImportFilePicked,
                onStartImport = callback.onBulkImportStart,
                onDismiss = callback.onBulkUploadDismiss,
            )
        }

        // Mutually exclusive by construction: opening any of them resets the
        // whole user-admin block, so a credential on screen always belongs to
        // whichever one is showing.
        val userAdmin = uiState.userAdmin
        when {
            userAdmin.isAddOpen ->
                AddUserDialog(uiState = userAdmin, callback = callback)

            userAdmin.resetTarget != null ->
                ResetPasswordDialog(uiState = userAdmin, callback = callback)

            userAdmin.roleTarget != null ->
                ChangeRoleDialog(uiState = userAdmin, callback = callback)

            userAdmin.statusTarget != null ->
                UserStatusDialog(uiState = userAdmin, callback = callback)
        }
    }
}

/** The control panel's one header action. */
@Composable
private fun BulkUploadButton(
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Dimensions.viewHeight48),
        shape = RoundedCornerShape(Dimensions.viewRadius8),
    ) {
        Icon(
            imageVector = AppIcons.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize18),
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(stringResource(Res.string.sysadmin_bulk_upload))
    }
}
