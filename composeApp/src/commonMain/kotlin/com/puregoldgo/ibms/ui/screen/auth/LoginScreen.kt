package com.puregoldgo.ibms.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login screen composable — handles events and delegates to pure UI content.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onLoginSuccess()
                is LoginUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LoginContent(
            uiState = uiState,
            callback = LoginCallback(
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::onLogin,
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Pure UI content — no ViewModel dependency.
 */
@Composable
private fun LoginContent(
    uiState: LoginUIState,
    callback: LoginCallback,
    modifier: Modifier = Modifier,
) {
    Scaffold {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "IBMS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(Dimensions.spacingXs))
            Text(
                text = "ISP Billing Management System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimensions.spacingXxl))

            OutlinedTextField(
                modifier = Modifier,
                value = uiState.username,
                onValueChange = callback.onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                enabled = !uiState.isLoading,
            )
            Spacer(Modifier.height(Dimensions.spacingLg))

            OutlinedTextField(
                modifier = Modifier,
                value = uiState.password,
                onValueChange = callback.onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(Dimensions.spacingSm))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(Dimensions.spacingXl))

            Button(
                modifier = Modifier,
                onClick = callback.onLoginClick,
                enabled = !uiState.isLoading && uiState.username.isNotBlank() && uiState.password.isNotBlank(),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(Dimensions.progressIndicatorSmall),
                        strokeWidth = Dimensions.progressStrokeWidth,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Log In")
                }
            }
        }
    }
}

@Preview
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
