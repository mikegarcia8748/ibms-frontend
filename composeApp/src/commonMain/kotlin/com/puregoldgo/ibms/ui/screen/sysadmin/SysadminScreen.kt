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
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.AddUserDialog
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.ChangeRoleDialog
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryCallback
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryTab
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryUIState
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryViewModel
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.ResetPasswordDialog
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.UserStatusDialog
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.AccountsTab
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.BulkImportDialog
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryCallback
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryUIState
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryViewModel
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.StoresTab
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
 *
 * Three ViewModels rather than one: the shell here, the staff directory, and the
 * two registries that share a fetch. Each owns its own state and its own
 * callbacks, so a panel can be exercised — in a test or in a preview — without
 * the other two.
 */
@Composable
fun SysadminScreen(
    onSignedOut: () -> Unit,
    viewModel: SysadminViewModel = koinViewModel(),
    directoryViewModel: DirectoryViewModel = koinViewModel(),
    registryViewModel: RegistryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val directoryState by directoryViewModel.uiState.collectAsStateWithLifecycle()
    val registryState by registryViewModel.uiState.collectAsStateWithLifecycle()

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
            onLogoutClick = viewModel::onLogout,
        ),
        directoryState = directoryState,
        directoryCallback = DirectoryCallback(
            onUserQueryChange = directoryViewModel::onUserQueryChange,
            onAddUserClick = directoryViewModel::onAddUserClick,
            onNewUserFirstNameChange = directoryViewModel::onNewUserFirstNameChange,
            onNewUserMiddleInitialChange = directoryViewModel::onNewUserMiddleInitialChange,
            onNewUserLastNameChange = directoryViewModel::onNewUserLastNameChange,
            onNewUserUsernameChange = directoryViewModel::onNewUserUsernameChange,
            onNewUserEmployeeNumberChange = directoryViewModel::onNewUserEmployeeNumberChange,
            onNewUserRoleChange = directoryViewModel::onNewUserRoleChange,
            onAddUserSubmit = directoryViewModel::onAddUserSubmit,
            onAddUserDismiss = directoryViewModel::onAddUserDismiss,
            onResetPasswordClick = directoryViewModel::onResetPasswordClick,
            onResetPasswordConfirm = directoryViewModel::onResetPasswordConfirm,
            onResetPasswordDismiss = directoryViewModel::onResetPasswordDismiss,
            onChangeRoleClick = directoryViewModel::onChangeRoleClick,
            onRoleSelectionChange = directoryViewModel::onRoleSelectionChange,
            onChangeRoleConfirm = directoryViewModel::onChangeRoleConfirm,
            onChangeRoleDismiss = directoryViewModel::onChangeRoleDismiss,
            onUserStatusToggleClick = directoryViewModel::onUserStatusToggleClick,
            onUserStatusConfirm = directoryViewModel::onUserStatusConfirm,
            onUserStatusDismiss = directoryViewModel::onUserStatusDismiss,
            onRetryLoad = { directoryViewModel.loadPanel() },
        ),
        registryState = registryState,
        registryCallback = RegistryCallback(
            onBranchQueryChange = registryViewModel::onBranchQueryChange,
            onBranchLetterSelect = registryViewModel::onBranchLetterSelect,
            onBranchProviderSelect = registryViewModel::onBranchProviderSelect,
            onAccountQueryChange = registryViewModel::onAccountQueryChange,
            onAccountProviderSelect = registryViewModel::onAccountProviderSelect,
            onBulkUploadClick = registryViewModel::onBulkUploadClick,
            onBulkImportFilePicked = registryViewModel::onBulkImportFilePicked,
            onBulkImportStart = registryViewModel::onBulkImportStart,
            onBulkUploadDismiss = registryViewModel::onBulkUploadDismiss,
            onRetryLoad = { registryViewModel.loadPanel() },
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * Takes each panel's state and callbacks separately rather than one bundle of
 * everything: that is what lets a preview draw the directory against sample
 * users while the registries stay empty, and it is why the tabs below can only
 * be handed the actions they can actually perform.
 *
 * `internal` rather than private so [SysadminScreenPreview.kt] can draw it.
 */
@Composable
internal fun SysadminContent(
    uiState: SysadminUIState,
    callback: SysadminCallback,
    directoryState: DirectoryUIState,
    directoryCallback: DirectoryCallback,
    registryState: RegistryUIState,
    registryCallback: RegistryCallback,
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
            // The one console-level action, and it belongs to the registries —
            // an import creates stores, accounts and providers.
            BulkUploadButton(modifier = modifier, onClick = registryCallback.onBulkUploadClick)
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
            SysadminTab.Directory -> DirectoryTab(directoryState, directoryCallback, isCompact)
            SysadminTab.Stores -> StoresTab(registryState, registryCallback, isCompact)
            SysadminTab.Accounts -> AccountsTab(registryState, registryCallback, isCompact)
        }

        if (registryState.isBulkImportOpen) {
            BulkImportDialog(
                uiState = registryState,
                onFilePicked = registryCallback.onBulkImportFilePicked,
                onStartImport = registryCallback.onBulkImportStart,
                onDismiss = registryCallback.onBulkUploadDismiss,
            )
        }

        // Mutually exclusive by construction: opening any of them resets the
        // whole user-admin block, so a credential on screen always belongs to
        // whichever one is showing.
        val userAdmin = directoryState.userAdmin
        when {
            userAdmin.isAddOpen ->
                AddUserDialog(uiState = userAdmin, callback = directoryCallback)

            userAdmin.resetTarget != null ->
                ResetPasswordDialog(uiState = userAdmin, callback = directoryCallback)

            userAdmin.roleTarget != null ->
                ChangeRoleDialog(uiState = userAdmin, callback = directoryCallback)

            userAdmin.statusTarget != null ->
                UserStatusDialog(uiState = userAdmin, callback = directoryCallback)
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
