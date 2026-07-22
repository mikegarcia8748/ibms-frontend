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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.core.network.AuthFailure
import com.puregoldgo.ibms.ui.component.PasswordField
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import com.puregoldgo.ibms.ui.screen.error.ServerUnavailableContent
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_button
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_contact_sysadmin
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_footer
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_menu_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_password_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_top_bar_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_username_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login screen composable — handles events and delegates to pure UI content.
 *
 * A successful call is not the same as a session: a temporary password answers
 * with a challenge instead, which is what [onPasswordChangeRequired] is for. The
 * token behind that route never passes through here — see [LoginUiEvent].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNoAccess: (NoAccessReason) -> Unit,
    onPasswordChangeRequired: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onLoginSuccess()
                is LoginUiEvent.NavigateToNoAccess -> onNoAccess(event.reason)
                is LoginUiEvent.NavigateToPasswordChange -> onPasswordChangeRequired()
                is LoginUiEvent.ShowError -> { }
            }
        }
    }

    if (uiState.serverUnavailable) {
        ServerUnavailableContent(onRetry = viewModel::onRetry)
        return
    }

    LoginContent(
        uiState = uiState,
        callback = LoginCallback(
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onLoginClick = viewModel::onLogin,
        )
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    uiState: LoginUIState,
    callback: LoginCallback
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.login_top_bar_title)) },
                navigationIcon = {
                    Image(
                        modifier = Modifier
                            .size(Dimensions.viewSize24),
                        painter = painterResource(Res.drawable.img_puregold_logo),
                        contentDescription = stringResource(Res.string.login_menu_content_description)
                    )
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val isCompact = maxWidth <= Dimensions.viewWidth600
            val contentPadding = if (isCompact) {
                Dimensions.viewPadding16
            } else {
                Dimensions.viewPadding24
            }
            val cardPadding = if (isCompact) {
                Dimensions.viewPadding16
            } else {
                Dimensions.viewPadding24
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_puregold_logo),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(Dimensions.viewSize80),
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))

                Text(
                    text = stringResource(Res.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding8))

                Text(
                    text = stringResource(Res.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding32))

                LoginCard(
                    uiState = uiState,
                    callback = callback,
                    cardPadding = cardPadding,
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
                )

                Spacer(Modifier.height(Dimensions.viewPadding48))

                Text(
                    text = stringResource(Res.string.login_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))
            }
        }
    }
}

@Composable
private fun LoginCard(
    uiState: LoginUIState,
    callback: LoginCallback,
    cardPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val usernameFocus = remember { FocusRequester() }

    // Land in the first field rather than making every sign-in start with a tap.
    // Guarded: on some targets the requester is not attached on the first frame.
    LaunchedEffect(Unit) {
        runCatching { usernameFocus.requestFocus() }
    }

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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocus),
                    value = uiState.username,
                    onValueChange = callback.onUsernameChange,
                    label = { Text(stringResource(Res.string.login_username_label)) },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    isError = uiState.failure == AuthFailure.InvalidCredentials,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    ),
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))

                PasswordField(
                    value = uiState.password,
                    onValueChange = callback.onPasswordChange,
                    label = stringResource(Res.string.login_password_label),
                    isVisible = uiState.isPasswordVisible,
                    onVisibilityToggle = callback.onTogglePasswordVisibility,
                    enabled = !uiState.isLoading,
                    isError = uiState.failure == AuthFailure.InvalidCredentials,
                    imeAction = ImeAction.Done,
                    onImeAction = { if (uiState.canSubmit) callback.onLoginClick() },
                )

                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                    LoginNotice(uiState)
                }

                Spacer(Modifier.height(Dimensions.viewPadding24))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.viewHeight48),
                    onClick = callback.onLoginClick,
                    enabled = uiState.canSubmit,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(Dimensions.viewSize20),
                            strokeWidth = Dimensions.viewStroke2,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(Res.string.login_button))
                    }
                }
            }
        }
    }
}

/**
 * One place for every rejection.
 *
 * A wrong password reads as field feedback; an expired temporary password, a
 * locked account or a deactivated one is a standing condition, so it is stated
 * plainly and — where there is one — followed by the next step. The wording for
 * a bad credential stays uniform whether or not the account exists.
 */
@Composable
private fun LoginNotice(uiState: LoginUIState) {
    val message = uiState.errorMessage ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            color = if (uiState.isStandingCondition) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        )

        if (uiState.showsSysadminHint) {
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = stringResource(Res.string.login_contact_sysadmin),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(
    name = "Login",
    group = "WebApp",
    device = Devices.DESKTOP,
)
@Composable
private fun LoginContentWidePreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginContent(
                uiState = LoginUIState(),
                callback = previewCallback(),
            )
        }
    }
}

@Preview(
    name = "Login",
    group = "MobileApp"
)
@Composable
private fun LoginContentPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUIState(),
            callback = previewCallback(),
        )
    }
}

@Preview(
    name = "Login — temporary password expired",
    group = "MobileApp",
)
@Composable
private fun LoginContentExpiredPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUIState(
                username = "mikepg",
                failure = AuthFailure.TempPasswordExpired,
                errorMessage = "Your temporary password has expired.",
            ),
            callback = previewCallback(),
        )
    }
}

private fun previewCallback() = LoginCallback(
    onUsernameChange = {},
    onPasswordChange = {},
    onTogglePasswordVisibility = {},
    onLoginClick = {},
)
