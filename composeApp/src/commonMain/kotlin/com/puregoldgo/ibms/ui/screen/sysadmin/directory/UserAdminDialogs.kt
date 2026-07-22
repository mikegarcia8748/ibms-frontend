package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogBanner
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.RoleDropdown
import com.puregoldgo.ibms.ui.component.roleLabel
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_add_user_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_confirming
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_field
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_pending_note
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_role_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_deactivate_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_deactivate_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_deactivate_confirming
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_deactivate_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_reactivate_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_reactivate_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_reactivate_confirming
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_status_reactivate_title
import org.jetbrains.compose.resources.stringResource

/**
 * The two row actions that change what an account *is*, each behind a
 * confirmation.
 *
 * Both used to happen inline: a role from a dropdown on the row, a deactivation
 * straight off a menu item. Neither is reversible from the user's side — one
 * hands out or takes away every permission they have, the other locks them out
 * — and on a list where every row carries the same controls, the one that gets
 * hit is whichever the pointer was over.
 */

/**
 * Changes a user's role.
 *
 * The rejection the backend can raise here — "cannot demote the last sysadmin"
 * — is rendered in this dialog rather than under the list, because it is an
 * answer about *this* user and it arrives after the list may have scrolled.
 */
@Composable
internal fun ChangeRoleDialog(
    uiState: UserAdminUIState,
    callback: DirectoryCallback,
) {
    val target = uiState.roleTarget ?: return
    val selected = uiState.pendingRole ?: target.role

    AppDialog(
        onDismissRequest = callback.onChangeRoleDismiss,
        dismissOnClickOutside = !uiState.isSubmitting,
        dismissOnBackPress = !uiState.isSubmitting,
    ) {
        AppDialogHeader(
            title = stringResource(Res.string.sysadmin_role_title),
            icon = AppIcons.Group,
            onClose = callback.onChangeRoleDismiss,
            closeDescription = stringResource(Res.string.sysadmin_add_user_close),
            closeEnabled = !uiState.isSubmitting,
        )

        Column(
            modifier = Modifier
                .heightIn(max = Dimensions.viewHeight480)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.viewPadding24),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
        ) {
            Text(
                text = stringResource(
                    Res.string.sysadmin_role_body,
                    target.name,
                    roleLabel(target.role),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                val fieldLabel = stringResource(Res.string.sysadmin_role_field)
                Text(
                    text = fieldLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoleDropdown(
                    selected = selected,
                    onSelect = callback.onRoleSelectionChange,
                    label = fieldLabel,
                    enabled = !uiState.isSubmitting,
                )
            }

            when {
                uiState.formError != null -> AppDialogBanner(
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(text = uiState.formError, style = MaterialTheme.typography.bodySmall)
                }

                // Choosing "No role yet" is a legitimate act — it is how access
                // is parked without deactivating the account — but it reads like
                // a neutral option in the list, so it says what it costs.
                selected == Role.PENDING -> AppDialogBanner(
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Text(
                        text = stringResource(Res.string.sysadmin_role_pending_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        AppDialogFooter {
            OutlinedButton(
                onClick = callback.onChangeRoleDismiss,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.sysadmin_role_cancel))
            }

            Button(
                onClick = callback.onChangeRoleConfirm,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                SubmitSpinner(uiState.isSubmitting)
                Text(
                    stringResource(
                        if (uiState.isSubmitting) {
                            Res.string.sysadmin_role_confirming
                        } else {
                            Res.string.sysadmin_role_confirm
                        },
                    ),
                )
            }
        }
    }
}

/**
 * Confirms switching an account off, or back on.
 *
 * Deactivating is the destructive direction and gets the error-toned button;
 * reactivating is additive and gets the ordinary one. The rows this cannot be
 * reached from — your own, and the last sysadmin — are refused in
 * [DirectoryUIState.deactivateBlockedReason] before this ever opens.
 */
@Composable
internal fun UserStatusDialog(
    uiState: UserAdminUIState,
    callback: DirectoryCallback,
) {
    val target = uiState.statusTarget ?: return
    val isDeactivating = target.isActive

    AppDialog(
        onDismissRequest = callback.onUserStatusDismiss,
        dismissOnClickOutside = !uiState.isSubmitting,
        dismissOnBackPress = !uiState.isSubmitting,
    ) {
        AppDialogHeader(
            title = stringResource(
                if (isDeactivating) {
                    Res.string.sysadmin_status_deactivate_title
                } else {
                    Res.string.sysadmin_status_reactivate_title
                },
            ),
            icon = if (isDeactivating) AppIcons.Warning else AppIcons.CheckCircle,
            onClose = callback.onUserStatusDismiss,
            closeDescription = stringResource(Res.string.sysadmin_add_user_close),
            closeEnabled = !uiState.isSubmitting,
        )

        Column(
            modifier = Modifier
                .heightIn(max = Dimensions.viewHeight480)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.viewPadding24),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Text(
                text = stringResource(
                    if (isDeactivating) {
                        Res.string.sysadmin_status_deactivate_body
                    } else {
                        Res.string.sysadmin_status_reactivate_body
                    },
                    target.name,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (uiState.formError != null) {
                AppDialogBanner(
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(text = uiState.formError, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        AppDialogFooter {
            OutlinedButton(
                onClick = callback.onUserStatusDismiss,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.sysadmin_status_cancel))
            }

            Button(
                onClick = callback.onUserStatusConfirm,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
                colors = if (isDeactivating) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                SubmitSpinner(uiState.isSubmitting)
                Text(
                    stringResource(
                        when {
                            isDeactivating && uiState.isSubmitting ->
                                Res.string.sysadmin_status_deactivate_confirming

                            isDeactivating -> Res.string.sysadmin_status_deactivate_confirm

                            uiState.isSubmitting ->
                                Res.string.sysadmin_status_reactivate_confirming

                            else -> Res.string.sysadmin_status_reactivate_confirm
                        },
                    ),
                )
            }
        }
    }
}

/** The in-flight indicator these footers share, and the gap after it. */
@Composable
private fun SubmitSpinner(isSubmitting: Boolean) {
    if (!isSubmitting) return
    CircularProgressIndicator(
        modifier = Modifier.size(Dimensions.viewSize18),
        strokeWidth = Dimensions.viewStroke2,
        color = MaterialTheme.colorScheme.onPrimary,
    )
    Spacer(Modifier.width(Dimensions.viewPadding8))
}
