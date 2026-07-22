package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import com.puregoldgo.ibms.shared.model.Role

/**
 * Everything the directory panel can do, bundled so its composables stay free of
 * the ViewModel and remain previewable.
 */
data class DirectoryCallback(
    val onUserQueryChange: (String) -> Unit,

    // Add user.
    val onAddUserClick: () -> Unit,
    val onNewUserFirstNameChange: (String) -> Unit,
    val onNewUserMiddleInitialChange: (String) -> Unit,
    val onNewUserLastNameChange: (String) -> Unit,
    val onNewUserUsernameChange: (String) -> Unit,
    val onNewUserEmployeeNumberChange: (String) -> Unit,
    val onNewUserRoleChange: (Role) -> Unit,
    val onAddUserSubmit: () -> Unit,
    val onAddUserDismiss: () -> Unit,

    // Reset password.
    val onResetPasswordClick: (DirectoryUser) -> Unit,
    val onResetPasswordConfirm: () -> Unit,
    val onResetPasswordDismiss: () -> Unit,

    // Change role.
    val onChangeRoleClick: (DirectoryUser) -> Unit,
    val onRoleSelectionChange: (Role) -> Unit,
    val onChangeRoleConfirm: () -> Unit,
    val onChangeRoleDismiss: () -> Unit,

    // Activate / deactivate.
    val onUserStatusToggleClick: (DirectoryUser) -> Unit,
    val onUserStatusConfirm: () -> Unit,
    val onUserStatusDismiss: () -> Unit,

    val onRetryLoad: () -> Unit,
)
