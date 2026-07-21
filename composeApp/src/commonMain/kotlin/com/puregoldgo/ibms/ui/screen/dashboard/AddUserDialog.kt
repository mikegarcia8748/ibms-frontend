package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.puregoldgo.ibms.shared.validation.Validation
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogBanner
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.RoleDropdown
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_employee_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_employee_number_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_first_name
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_last_name
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_middle_initial
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_note
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_role
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_submitting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_username
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user_username_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_copied
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_copy
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_created_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_done
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_expires
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_password
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_reset_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_username
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_warning_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_credential_warning_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_reset_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_reset_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_reset_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_reset_confirming
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_reset_title
import org.jetbrains.compose.resources.stringResource

/**
 * Provisions a user, then hands over the temporary password it generated.
 *
 * One dialog with two phases rather than two dialogs, for the same reason the
 * bulk importer is one: filling the form and reading the credential it produced
 * are two views of a single task, and moving to another surface would separate
 * the password from the name it belongs to.
 *
 * The second phase is the *only* place that password is ever readable — see
 * [TemporaryPasswordPanel].
 */
@Composable
internal fun AddUserDialog(
    uiState: UserAdminUIState,
    callback: DashboardCallback,
) {
    val issued = uiState.issued

    AppDialog(
        onDismissRequest = callback.onAddUserDismiss,
        // A click beside the dialog must not be able to take the temporary
        // password with it — it cannot be looked up again — nor orphan a
        // request that is still going to create the account.
        dismissOnClickOutside = issued == null && !uiState.isSubmitting,
        dismissOnBackPress = issued == null && !uiState.isSubmitting,
    ) {
        AppDialogHeader(
            title = stringResource(Res.string.dashboard_add_user_title),
            icon = AppIcons.PersonAdd,
            onClose = callback.onAddUserDismiss,
            closeDescription = stringResource(Res.string.dashboard_add_user_close),
            // Closing mid-submit would orphan a request that is still going to
            // create the account — and take its password with it.
            closeEnabled = !uiState.isSubmitting,
        )

        Column(
            modifier = Modifier
                .heightIn(max = Dimensions.viewHeight480)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.viewPadding24),
        ) {
            if (issued != null) {
                TemporaryPasswordPanel(issued)
            } else {
                NewUserFormBody(uiState, callback)
            }
        }

        AppDialogFooter {
            if (issued != null) {
                Button(
                    onClick = callback.onAddUserDismiss,
                    shape = RoundedCornerShape(Dimensions.viewRadius8),
                ) {
                    Text(stringResource(Res.string.dashboard_credential_done))
                }
                return@AppDialogFooter
            }

            OutlinedButton(
                onClick = callback.onAddUserDismiss,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.dashboard_add_user_cancel))
            }

            Button(
                onClick = callback.onAddUserSubmit,
                enabled = uiState.canSubmit,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        strokeWidth = Dimensions.viewStroke2,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.viewSize18),
                    )
                }
                Spacer(Modifier.width(Dimensions.viewPadding8))
                Text(
                    stringResource(
                        if (uiState.isSubmitting) {
                            Res.string.dashboard_add_user_submitting
                        } else {
                            Res.string.dashboard_add_user_submit
                        },
                    ),
                )
            }
        }
    }
}

/**
 * The name in three parts, username, employee number and role.
 *
 * The name is split because the backend keeps a column for each part and this
 * form used to leave all three null, filling only the composed display name. The
 * directory still shows that composed name — [Validation.composeFullName] builds
 * it — so nothing here is asked for twice.
 *
 * Employee number is required. It is the identifier the rest of the company
 * files people under, and there is no endpoint to add one afterwards.
 */
@Composable
private fun NewUserFormBody(
    uiState: UserAdminUIState,
    callback: DashboardCallback,
) {
    val form = uiState.form
    val enabled = !uiState.isSubmitting

    // Validated as typed rather than on submit, because neither rule is
    // guessable ("3-32 characters: letters, digits, dot, underscore or hyphen")
    // and finding one out from a rejected round trip is a worse way to learn it.
    // Blank is not an error yet — an untouched field has not failed anything.
    val usernameError = form.usernameError
    val employeeNumberError = form.employeeNumberError

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
        OutlinedTextField(
            value = form.firstName,
            onValueChange = callback.onNewUserFirstNameChange,
            label = { Text(stringResource(Res.string.dashboard_add_user_first_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        )

        // Last name takes the room; the initial is one character and a box sized
        // for a name beside it would read as a field someone forgot to fill.
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12)) {
            OutlinedTextField(
                value = form.lastName,
                onValueChange = callback.onNewUserLastNameChange,
                label = { Text(stringResource(Res.string.dashboard_add_user_last_name)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            )

            OutlinedTextField(
                value = form.middleInitial,
                onValueChange = callback.onNewUserMiddleInitialChange,
                label = { Text(stringResource(Res.string.dashboard_add_user_middle_initial)) },
                modifier = Modifier.width(Dimensions.viewWidth96),
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            )
        }

        OutlinedTextField(
            value = form.username,
            onValueChange = callback.onNewUserUsernameChange,
            label = { Text(stringResource(Res.string.dashboard_add_user_username)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            isError = usernameError != null,
            supportingText = {
                Text(
                    text = usernameError
                        ?: stringResource(Res.string.dashboard_add_user_username_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (usernameError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
            ),
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        )

        OutlinedTextField(
            value = form.employeeNumber,
            onValueChange = callback.onNewUserEmployeeNumberChange,
            label = { Text(stringResource(Res.string.dashboard_add_user_employee_number)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            isError = employeeNumberError != null,
            supportingText = {
                Text(
                    text = employeeNumberError
                        ?: stringResource(Res.string.dashboard_add_user_employee_number_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (employeeNumberError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
            // The number is digits but is stored as text — `010005529` keeps its
            // leading zero — so this only asks for the numeric keypad.
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        )

        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            val roleLabel = stringResource(Res.string.dashboard_add_user_role)
            Text(
                text = roleLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RoleDropdown(
                selected = form.role,
                onSelect = callback.onNewUserRoleChange,
                label = roleLabel,
                enabled = enabled,
            )
        }

        if (uiState.formError != null) {
            // The backend's own words — "username 'jdoe' is already taken" names
            // the field to go change; house copy would not.
            AppDialogBanner(
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Text(text = uiState.formError, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            AppDialogBanner(
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_add_user_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/**
 * The temporary password, in the one moment it exists in readable form.
 *
 * Shown in the clear rather than behind a reveal toggle: this panel exists to be
 * read and copied, and masking a value the admin has been sent here to transcribe
 * would be ceremony without security — it is on screen either way.
 *
 * The warning is not decoration. The backend keeps only a bcrypt hash, so an
 * admin who dismisses this believing they can look the password up again has
 * locked the new user out until someone issues a reset.
 */
@Composable
private fun TemporaryPasswordPanel(credential: IssuedCredential) {
    // `LocalClipboardManager` is deprecated in favour of `LocalClipboard`, but
    // that one takes a `ClipEntry` — an `expect class` with no common factory in
    // Compose Multiplatform 1.11. Writing plain text to the clipboard from
    // common code would mean an expect/actual across four source sets for one
    // button, so the deprecated call stands until the newer API grows a common
    // text constructor.
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var copied by remember(credential.temporaryPassword) { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12)) {
        Text(
            text = stringResource(
                if (credential.isNewUser) {
                    Res.string.dashboard_credential_created_title
                } else {
                    Res.string.dashboard_credential_reset_title
                },
                credential.name,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        CredentialField(
            label = stringResource(Res.string.dashboard_credential_username),
            value = credential.username,
        )

        CredentialField(
            label = stringResource(Res.string.dashboard_credential_password),
            value = credential.temporaryPassword,
            trailing = {
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(credential.temporaryPassword))
                        copied = true
                    },
                ) {
                    Icon(
                        imageVector = AppIcons.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.viewSize18),
                    )
                    Spacer(Modifier.width(Dimensions.viewPadding8))
                    Text(
                        stringResource(
                            if (copied) {
                                Res.string.dashboard_credential_copied
                            } else {
                                Res.string.dashboard_credential_copy
                            },
                        ),
                    )
                }
            },
        )

        Text(
            text = stringResource(Res.string.dashboard_credential_expires, credential.expiresAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AppDialogBanner(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            Text(
                text = stringResource(Res.string.dashboard_credential_warning_title),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.dashboard_credential_warning_body),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * One labelled, read-only credential value.
 *
 * Monospace, as the accounts table already does for account numbers: these get
 * transcribed by hand into a chat message or read down a phone, and a generated
 * password is full of the characters a proportional face makes ambiguous.
 */
@Composable
private fun CredentialField(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        trailingIcon = trailing,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
    )
}

/**
 * Confirms re-issuing a temporary password, then shows the one it issued.
 *
 * A confirmation step because this is not additive: the user's current password
 * stops working the instant it runs, and every session they hold is revoked
 * server-side. Clicking a menu item should not silently sign someone out.
 */
@Composable
internal fun ResetPasswordDialog(
    uiState: UserAdminUIState,
    callback: DashboardCallback,
) {
    val target = uiState.resetTarget ?: return
    val issued = uiState.issued

    AppDialog(
        onDismissRequest = callback.onResetPasswordDismiss,
        // Same reason as the add-user dialog: the issued password exists only
        // here, and a reset already in flight is not cancellable.
        dismissOnClickOutside = issued == null && !uiState.isSubmitting,
        dismissOnBackPress = issued == null && !uiState.isSubmitting,
    ) {
        AppDialogHeader(
            title = stringResource(Res.string.dashboard_reset_title),
            icon = AppIcons.Key,
            onClose = callback.onResetPasswordDismiss,
            closeDescription = stringResource(Res.string.dashboard_add_user_close),
            closeEnabled = !uiState.isSubmitting,
        )

        Column(
            modifier = Modifier
                .heightIn(max = Dimensions.viewHeight480)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.viewPadding24),
        ) {
            if (issued != null) {
                TemporaryPasswordPanel(issued)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12)) {
                    Text(
                        text = stringResource(Res.string.dashboard_reset_body, target.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (uiState.formError != null) {
                        AppDialogBanner(
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        ) {
                            Text(
                                text = uiState.formError,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }

        AppDialogFooter {
            if (issued != null) {
                Button(
                    onClick = callback.onResetPasswordDismiss,
                    shape = RoundedCornerShape(Dimensions.viewRadius8),
                ) {
                    Text(stringResource(Res.string.dashboard_credential_done))
                }
                return@AppDialogFooter
            }

            OutlinedButton(
                onClick = callback.onResetPasswordDismiss,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.dashboard_reset_cancel))
            }

            Button(
                onClick = callback.onResetPasswordConfirm,
                enabled = !uiState.isSubmitting,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        strokeWidth = Dimensions.viewStroke2,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(Dimensions.viewPadding8))
                }
                Text(
                    stringResource(
                        if (uiState.isSubmitting) {
                            Res.string.dashboard_reset_confirming
                        } else {
                            Res.string.dashboard_reset_confirm
                        },
                    ),
                )
            }
        }
    }
}
