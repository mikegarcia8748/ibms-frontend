package com.puregoldgo.ibms.ui.screen.dashboard

import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.model.Role

/**
 * Everything the control panel can do, bundled so the content composables stay
 * free of the ViewModel and remain previewable.
 */
data class DashboardCallback(
    val onTabSelect: (DashboardTab) -> Unit,
    val onUserQueryChange: (String) -> Unit,
    val onBranchQueryChange: (String) -> Unit,
    val onBranchLetterSelect: (Char) -> Unit,
    val onBranchProviderSelect: (String?) -> Unit,
    val onAccountQueryChange: (String) -> Unit,
    val onAccountProviderSelect: (String?) -> Unit,
    val onBulkUploadClick: () -> Unit,
    val onBulkImportFilePicked: (PickedFile) -> Unit,
    val onBulkImportStart: () -> Unit,
    val onBulkUploadDismiss: () -> Unit,

    // User administration.
    val onAddUserClick: () -> Unit,
    val onNewUserFirstNameChange: (String) -> Unit,
    val onNewUserMiddleInitialChange: (String) -> Unit,
    val onNewUserLastNameChange: (String) -> Unit,
    val onNewUserUsernameChange: (String) -> Unit,
    val onNewUserEmployeeNumberChange: (String) -> Unit,
    val onNewUserRoleChange: (Role) -> Unit,
    val onAddUserSubmit: () -> Unit,
    val onAddUserDismiss: () -> Unit,
    val onResetPasswordClick: (DirectoryUser) -> Unit,
    val onResetPasswordConfirm: () -> Unit,
    val onResetPasswordDismiss: () -> Unit,
    val onChangeRoleClick: (DirectoryUser) -> Unit,
    val onRoleSelectionChange: (Role) -> Unit,
    val onChangeRoleConfirm: () -> Unit,
    val onChangeRoleDismiss: () -> Unit,
    val onUserStatusToggleClick: (DirectoryUser) -> Unit,
    val onUserStatusConfirm: () -> Unit,
    val onUserStatusDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
