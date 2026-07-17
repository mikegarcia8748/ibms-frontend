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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_button
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onLoginSuccess()
                is LoginUiEvent.ShowError -> { }
            }
        }
    }

    LoginContent(
        uiState = uiState,
        callback = LoginCallback(
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onLoginClick = viewModel::onLogin,
        )
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 */
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
private fun MenuIcon(
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = Dimensions.viewPadding4)
            .semantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(Dimensions.viewWidth24)
                .height(Dimensions.viewHeight2)
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Box(
            modifier = Modifier
                .width(Dimensions.viewWidth24)
                .height(Dimensions.viewHeight2)
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Box(
            modifier = Modifier
                .width(Dimensions.viewWidth24)
                .height(Dimensions.viewHeight2)
                .background(MaterialTheme.colorScheme.onSurface),
        )
    }
}

@Composable
private fun LoginCard(
    uiState: LoginUIState,
    callback: LoginCallback,
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.username,
                    onValueChange = callback.onUsernameChange,
                    label = { Text(stringResource(Res.string.login_username_label)) },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.password,
                    onValueChange = callback.onPasswordChange,
                    label = { Text(stringResource(Res.string.login_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isLoading,
                )

                if (uiState.errorMessage != null) {
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(Dimensions.viewPadding24))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.viewHeight48),
                    onClick = callback.onLoginClick,
                    enabled = !uiState.isLoading && uiState.username.isNotBlank() && uiState.password.isNotBlank(),
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

@Preview(
    name = "Login",
    group = "MobileApp"
)
@Composable
private fun LoginContentPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUIState(),
            callback = LoginCallback(
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {},
            ),
        )
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
                callback = LoginCallback(
                    onUsernameChange = {},
                    onPasswordChange = {},
                    onLoginClick = {},
                ),
            )
        }
    }
}
