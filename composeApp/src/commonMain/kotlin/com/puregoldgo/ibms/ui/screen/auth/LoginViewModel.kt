package com.puregoldgo.ibms.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.AuthFailure
import com.puregoldgo.core.network.DomainException
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.data.auth.ChallengeHolder
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.domain.hasOperationalAccess
import com.puregoldgo.ibms.shared.domain.usecase.LoginUseCase
import com.puregoldgo.ibms.ui.screen.access.noAccessReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the Login screen.
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val challengeHolder: ChallengeHolder,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    // region Public API

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null, failure = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, failure = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLogin() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            loginUseCase(
                username = state.username.trim(),
                password = state.password,
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null, failure = null) }
                    }

                    is Resource.Success -> onLoginResponse(resource.data)

                    is Resource.Failed -> fail(resource.message)

                    is Resource.Error -> {
                        val authError = resource.error as? DomainException.AuthException
                        if (authError?.failure == AuthFailure.Offline) {
                            // The server never answered — the password was not
                            // rejected, so keep it and let a retry re-submit once
                            // the backend is reachable again.
                            _uiState.update { it.copy(isLoading = false, serverUnavailable = true) }
                        } else {
                            fail(authError?.message ?: resource.error?.message, authError)
                        }
                    }
                }
            }
        }
    }

    /** Dismisses the "server unavailable" takeover and re-submits the sign-in. */
    fun onRetry() {
        _uiState.update { it.copy(serverUnavailable = false) }
        onLogin()
    }

    // endregion

    // region Internals

    private suspend fun onLoginResponse(response: LoginResponse?) {
        // Authenticated ⟺ session != null. `user` is populated on BOTH outcomes,
        // so branching on it would let a holder of a temporary password walk
        // straight into the app.
        val user = response?.user
        val session = response?.session

        if (session != null && user != null) {
            _uiState.update { it.copy(isLoading = false, isAuthenticated = true, password = "") }
            _uiEvent.emit(
                if (user.hasOperationalAccess()) {
                    LoginUiEvent.NavigateToHome
                } else {
                    LoginUiEvent.NavigateToNoAccess(user.noAccessReason())
                },
            )
            return
        }

        val challenge = response?.passwordChange
        if (challenge != null && user != null) {
            // Memory only, and only until it is redeemed. The temporary password
            // is cleared with it: it is spent, and the next screen has no use
            // for it.
            challengeHolder.put(user, challenge)
            _uiState.update { it.copy(isLoading = false, password = "") }
            _uiEvent.emit(LoginUiEvent.NavigateToPasswordChange)
            return
        }

        // Neither a session nor a challenge — a contract violation. Fail closed
        // rather than guess which of the two the backend meant.
        fail("Sign-in could not be completed. Please try again.")
    }

    private suspend fun fail(message: String?, error: DomainException.AuthException? = null) {
        val text = message ?: "An unexpected error occurred"
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = text,
                failure = error?.failure,
                // Keep the username — it will be needed again — but never leave a
                // rejected password sitting in the field.
                password = "",
            )
        }
        _uiEvent.emit(LoginUiEvent.ShowError(text))
    }

    // endregion
}
