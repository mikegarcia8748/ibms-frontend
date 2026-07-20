package com.puregoldgo.ibms.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.shared.validation.PasswordPolicy
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import com.puregoldgo.ibms.ui.component.PasswordField
import com.puregoldgo.ibms.ui.component.PasswordRequirements
import com.puregoldgo.ibms.ui.component.PasswordStrengthBar
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_account
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_confirm_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_expiring_soon
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_new_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_start_over
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_submitting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_success
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_time_remaining
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.set_password_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Step three of the provisioned-account flow: the holder of a temporary password
 * chooses their own, and only then does a session exist.
 *
 * The screen deliberately offers no way around itself — no app chrome, no back
 * entry into the product — because at this point the caller is still
 * unauthenticated, however much the response looked like a login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPasswordScreen(
    onPasswordSet: () -> Unit,
    onNoAccess: (NoAccessReason) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: SetPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SetPasswordUiEvent.NavigateToHome -> onPasswordSet()
                is SetPasswordUiEvent.NavigateToNoAccess -> onNoAccess(event.reason)
                is SetPasswordUiEvent.NavigateToLogin -> onBackToLogin()
            }
        }
    }

    SetPasswordContent(
        uiState = uiState,
        callback = SetPasswordCallback(
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onToggleNewPasswordVisibility = viewModel::onToggleNewPasswordVisibility,
            onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
            onSubmit = viewModel::onSubmit,
            onCancel = viewModel::onCancel,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetPasswordContent(
    uiState: SetPasswordUIState,
    callback: SetPasswordCallback,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.set_password_top_bar_title)) })
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val isCompact = maxWidth <= Dimensions.viewWidth600
            val contentPadding = if (isCompact) Dimensions.viewPadding16 else Dimensions.viewPadding24

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(Modifier.height(Dimensions.viewPadding24))

                Image(
                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.img_puregold_logo),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(Dimensions.viewSize80),
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))

                Text(
                    text = stringResource(Res.string.set_password_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding8))

                Text(
                    text = stringResource(Res.string.set_password_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding24))

                SetPasswordCard(
                    uiState = uiState,
                    callback = callback,
                    cardPadding = contentPadding,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding32))
            }
        }
    }
}

@Composable
private fun SetPasswordCard(
    uiState: SetPasswordUIState,
    callback: SetPasswordCallback,
    cardPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius24),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimensions.viewElevation2,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight4)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
            ) {
                if (uiState.terminal != null) {
                    TerminalNotice(uiState.terminal, callback.onCancel)
                    return@Column
                }

                AccountLine(uiState)
                Spacer(Modifier.height(Dimensions.viewPadding16))
                CountdownLine(uiState)
                Spacer(Modifier.height(Dimensions.viewPadding16))

                PasswordForm(uiState, callback)
            }
        }
    }
}

@Composable
private fun AccountLine(uiState: SetPasswordUIState) {
    if (uiState.username.isBlank()) return
    Text(
        // Naming the account removes the "wait, whose password am I setting?"
        // pause — this screen is reached straight after a sysadmin handover.
        text = stringResource(Res.string.set_password_account, uiState.displayName, uiState.username),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CountdownLine(uiState: SetPasswordUIState) {
    if (uiState.remainingSeconds <= 0) return
    val formatted = formatDuration(uiState.remainingSeconds)
    Text(
        text = if (uiState.isExpiringSoon) {
            stringResource(Res.string.set_password_expiring_soon, formatted)
        } else {
            stringResource(Res.string.set_password_time_remaining, formatted)
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (uiState.isExpiringSoon) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        // Polite, and only once it actually matters: announcing every tick would
        // make the screen unusable with a screen reader.
        modifier = if (uiState.isExpiringSoon) {
            Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        } else {
            Modifier
        },
    )
}

@Composable
private fun PasswordForm(
    uiState: SetPasswordUIState,
    callback: SetPasswordCallback,
) {
    val focusManager = LocalFocusManager.current
    val newPasswordFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { newPasswordFocus.requestFocus() }
    }

    PasswordField(
        value = uiState.newPassword,
        onValueChange = callback.onNewPasswordChange,
        label = stringResource(Res.string.set_password_new_label),
        isVisible = uiState.isNewPasswordVisible,
        onVisibilityToggle = callback.onToggleNewPasswordVisibility,
        enabled = !uiState.isLoading && !uiState.isSuccess,
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
        modifier = Modifier.focusRequester(newPasswordFocus),
    )

    Spacer(Modifier.height(Dimensions.viewPadding12))

    PasswordStrengthBar(strength = uiState.strength)

    Spacer(Modifier.height(Dimensions.viewPadding12))

    PasswordRequirements(rules = uiState.rules)

    Spacer(Modifier.height(Dimensions.viewPadding16))

    PasswordField(
        value = uiState.confirmPassword,
        onValueChange = callback.onConfirmPasswordChange,
        label = stringResource(Res.string.set_password_confirm_label),
        isVisible = uiState.isConfirmPasswordVisible,
        onVisibilityToggle = callback.onToggleConfirmPasswordVisibility,
        enabled = !uiState.isLoading && !uiState.isSuccess,
        isError = uiState.confirmError != null,
        supportingText = uiState.confirmError,
        imeAction = ImeAction.Done,
        onImeAction = { if (uiState.canSubmit) callback.onSubmit() },
    )

    if (uiState.errorMessage != null) {
        Spacer(Modifier.height(Dimensions.viewPadding8))
        Text(
            text = uiState.errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        )
    }

    if (uiState.isSuccess) {
        Spacer(Modifier.height(Dimensions.viewPadding8))
        Text(
            text = stringResource(Res.string.set_password_success),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }

    Spacer(Modifier.height(Dimensions.viewPadding24))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.viewHeight48),
        onClick = callback.onSubmit,
        enabled = uiState.canSubmit,
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.viewSize20),
                strokeWidth = Dimensions.viewStroke2,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            uiState.isSuccess -> Text(stringResource(Res.string.set_password_submitting))
            else -> Text(stringResource(Res.string.set_password_submit))
        }
    }

    Spacer(Modifier.height(Dimensions.viewPadding8))

    TextButton(
        onClick = callback.onCancel,
        enabled = !uiState.isLoading && !uiState.isSuccess,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(Res.string.set_password_cancel))
    }
}

/**
 * What is left when the form can no longer be submitted: one explanation and one
 * way out. Editing the fields cannot rescue either case, so the fields go away.
 */
@Composable
private fun TerminalNotice(
    terminal: SetPasswordUIState.TerminalState,
    onBackToLogin: () -> Unit,
) {
    val message = when (terminal) {
        SetPasswordUIState.TerminalState.ChallengeExpired ->
            com.puregoldgo.core.network.AuthErrorMapper.messageFor(
                com.puregoldgo.core.network.AuthFailure.ChallengeExpired,
            )

        SetPasswordUIState.TerminalState.PasswordAlreadySet ->
            com.puregoldgo.core.network.AuthErrorMapper.messageFor(
                com.puregoldgo.core.network.AuthFailure.PasswordAlreadySet,
            )
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
    )

    Spacer(Modifier.height(Dimensions.viewPadding24))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.viewHeight48),
        onClick = onBackToLogin,
    ) {
        Text(stringResource(Res.string.set_password_start_over))
    }
}

/** mm:ss for the countdown. */
private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:" + seconds.toString().padStart(2, '0')
}

@Preview(
    name = "Set password",
    group = "WebApp",
    device = Devices.DESKTOP,
)
@Composable
private fun SetPasswordContentWidePreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SetPasswordContent(
                uiState = previewState(),
                callback = previewCallback(),
            )
        }
    }
}

@Preview(
    name = "Set password",
    group = "MobileApp",
)
@Composable
private fun SetPasswordContentPreview() {
    AppTheme {
        SetPasswordContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(
    name = "Set password — challenge expired",
    group = "MobileApp",
)
@Composable
private fun SetPasswordExpiredPreview() {
    AppTheme {
        SetPasswordContent(
            uiState = previewState().copy(
                terminal = SetPasswordUIState.TerminalState.ChallengeExpired,
            ),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = SetPasswordUIState(
    displayName = "Michael P. Garcia",
    username = "mikepg",
    newPassword = "Chosen-Passw0",
    rules = PasswordPolicy.evaluate("Chosen-Passw0", "mikepg"),
    strength = PasswordPolicy.strength("Chosen-Passw0"),
    remainingSeconds = 541,
)

private fun previewCallback() = SetPasswordCallback(
    onNewPasswordChange = {},
    onConfirmPasswordChange = {},
    onToggleNewPasswordVisibility = {},
    onToggleConfirmPasswordVisibility = {},
    onSubmit = {},
    onCancel = {},
)
