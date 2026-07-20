package com.puregoldgo.ibms.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.AuthFailure
import com.puregoldgo.core.network.DomainException
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.data.auth.ChallengeHolder
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.domain.hasOperationalAccess
import com.puregoldgo.ibms.shared.domain.usecase.CompletePasswordChangeUseCase
import com.puregoldgo.ibms.shared.validation.PasswordPolicy
import com.puregoldgo.ibms.ui.screen.access.noAccessReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * MVI ViewModel for the set-password screen — step three of the provisioned
 * account flow, and the step that actually creates the session.
 *
 * The challenge is read from [ChallengeHolder] rather than passed in as a
 * navigation argument, because routes are serialized into saved state and a
 * bearer credential has no business being written there.
 */
class SetPasswordViewModel(
    private val challengeHolder: ChallengeHolder,
    private val completePasswordChange: CompletePasswordChangeUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(SetPasswordUIState())
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<SetPasswordUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        val pending = challengeHolder.current()
        if (pending == null) {
            // Arrived without a live challenge — a restored back stack, or a
            // window that ran out while the screen was being built.
            _uiState.update { it.copy(terminal = SetPasswordUIState.TerminalState.ChallengeExpired) }
        } else {
            _uiState.update {
                it.copy(
                    displayName = pending.user.name,
                    username = pending.user.username,
                    rules = PasswordPolicy.evaluate(password = "", username = pending.user.username),
                )
            }
            startCountdown()
        }
    }

    // region Public API

    fun onNewPasswordChange(value: String) {
        _uiState.update { state ->
            state.copy(
                newPassword = value,
                rules = PasswordPolicy.evaluate(value, state.username),
                strength = PasswordPolicy.strength(value),
                errorMessage = null,
                confirmError = confirmErrorFor(value, state.confirmPassword),
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = value,
                errorMessage = null,
                confirmError = confirmErrorFor(state.newPassword, value),
            )
        }
    }

    fun onToggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onSubmit() {
        val state = _uiState.value
        // The endpoint is not idempotent — a second redemption of the same
        // challenge is a 409, not a no-op — so an in-flight call blocks another.
        if (state.isLoading || state.isSuccess || state.terminal != null) return

        val challenge = challengeHolder.current()
        if (challenge == null) {
            expire(SetPasswordUIState.TerminalState.ChallengeExpired)
            return
        }

        viewModelScope.launch {
            completePasswordChange(
                challengeToken = challenge.challengeToken,
                username = state.username,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword,
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                    is Resource.Success -> onPasswordSet(resource)

                    is Resource.Failed ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }

                    is Resource.Error -> onError(resource.error)
                }
            }
        }
    }

    fun onCancel() {
        challengeHolder.clear()
        viewModelScope.launch { _uiEvent.emit(SetPasswordUiEvent.NavigateToLogin) }
    }

    // endregion

    // region Internals

    private suspend fun onPasswordSet(resource: Resource.Success<LoginResponse>) {
        val response = resource.data
        val user = response?.user
        // Same invariant as sign-in: a session is what makes this authenticated,
        // not the presence of a user in the payload.
        if (response?.session == null || user == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Your password was not set. Please try again.",
                )
            }
            return
        }

        // Spent: it cannot be redeemed twice, and holding it any longer only
        // widens the window in which it could leak.
        challengeHolder.clear()
        _uiState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }

        // A beat on the confirmation so the jump into the app does not read as a
        // glitch on a fast connection.
        delay(SUCCESS_DWELL)
        _uiEvent.emit(
            if (user.hasOperationalAccess()) {
                SetPasswordUiEvent.NavigateToHome
            } else {
                SetPasswordUiEvent.NavigateToNoAccess(user.noAccessReason())
            },
        )
    }

    private fun onError(error: Throwable?) {
        val authError = error as? DomainException.AuthException
        when (authError?.failure) {
            AuthFailure.ChallengeExpired -> expire(SetPasswordUIState.TerminalState.ChallengeExpired)
            AuthFailure.PasswordAlreadySet -> expire(SetPasswordUIState.TerminalState.PasswordAlreadySet)
            else -> _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = authError?.message ?: error?.message,
                )
            }
        }
    }

    private fun expire(terminal: SetPasswordUIState.TerminalState) {
        challengeHolder.clear()
        _uiState.update {
            it.copy(
                isLoading = false,
                terminal = terminal,
                remainingSeconds = 0,
                errorMessage = null,
                // Nothing typed here can be submitted any more; do not leave a
                // password sitting in memory behind a dead form.
                newPassword = "",
                confirmPassword = "",
            )
        }
    }

    /**
     * Ticks the remaining time from the monotonic deadline rather than counting
     * down a number, so a suspended app resumes with the truth instead of the
     * time it had left when it went away.
     */
    private fun startCountdown() {
        viewModelScope.launch {
            while (isActive) {
                val pending = challengeHolder.current()
                if (pending == null) {
                    if (_uiState.value.terminal == null && !_uiState.value.isSuccess) {
                        expire(SetPasswordUIState.TerminalState.ChallengeExpired)
                    }
                    return@launch
                }
                _uiState.update { it.copy(remainingSeconds = pending.remaining.inWholeSeconds) }
                delay(1.seconds)
            }
        }
    }

    private fun confirmErrorFor(password: String, confirmation: String): String? = when {
        confirmation.isEmpty() -> null
        password == confirmation -> null
        else -> "Passwords do not match"
    }

    private companion object {
        /** Long enough to read the confirmation, short enough not to feel like a wait. */
        val SUCCESS_DWELL = 700.milliseconds
    }

    // endregion
}
