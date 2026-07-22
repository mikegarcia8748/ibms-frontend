package com.puregoldgo.ibms.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.AuthFailure
import com.puregoldgo.core.network.DomainException
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.SessionStore
import com.puregoldgo.ibms.shared.domain.hasOperationalAccess
import com.puregoldgo.ibms.shared.domain.usecase.RefreshSessionUseCase
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import com.puregoldgo.ibms.ui.screen.access.noAccessReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Decides where a launch lands, before anything else is drawn.
 *
 * The access token only ever lives in memory, so every cold start begins
 * unauthenticated even when the user never signed out. If a refresh token was
 * persisted, it is spent here for a fresh pair — and the response carries the
 * user profile, so a resumed session needs no second call to learn who is back.
 *
 * Doing this on a splash rather than on the sign-in screen is what keeps a
 * returning user from seeing a login form flash past on every launch.
 */
class SplashViewModel(
    private val refreshSession: RefreshSessionUseCase,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SplashUiEvent>(replay = 1)
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Resolving)
    val uiState = _uiState.asStateFlow()

    init {
        resume()
    }

    /** Re-runs the session resume after a [SplashUiState.Unreachable] outage. */
    fun retry() {
        _uiState.value = SplashUiState.Resolving
        resume()
    }

    private fun resume() {
        viewModelScope.launch {
            if (!SessionStore.hasPersistedSession()) {
                _uiEvent.emit(SplashUiEvent.NavigateToLogin)
                return@launch
            }

            refreshSession().collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit

                    is Resource.Success -> {
                        val user = resource.data?.user
                        val destination = when {
                            resource.data?.session == null || user == null ->
                                SplashUiEvent.NavigateToLogin

                            user.hasOperationalAccess() -> SplashUiEvent.NavigateToHome
                            else -> SplashUiEvent.NavigateToNoAccess(user.noAccessReason())
                        }
                        _uiEvent.emit(destination)
                    }

                    is Resource.Error if resource.isOffline() -> {
                        // The server never answered, so the refresh token is not
                        // spent — it just could not be presented. Keep the session
                        // and let the user retry once the backend is back.
                        _uiState.value = SplashUiState.Unreachable
                    }

                    // A refresh token that no longer works is ordinary: it
                    // expired, a sysadmin reset the password, or the session was
                    // revoked. Sign-in is the answer, not an error screen.
                    is Resource.Failed, is Resource.Error -> {
                        SessionStore.clear()
                        _uiEvent.emit(SplashUiEvent.NavigateToLogin)
                    }
                }
            }
        }
    }

    /** True when the failure was a dropped connection rather than a rejection. */
    private fun Resource.Error<*>.isOffline(): Boolean =
        (error as? DomainException.AuthException)?.failure == AuthFailure.Offline
}

/** Where the launch goes once the session question is settled. */
sealed class SplashUiEvent {
    data object NavigateToHome : SplashUiEvent()
    data class NavigateToNoAccess(val reason: NoAccessReason) : SplashUiEvent()
    data object NavigateToLogin : SplashUiEvent()
}

/** What the splash draws while it works, versus when the server can't be reached. */
sealed class SplashUiState {
    data object Resolving : SplashUiState()
    data object Unreachable : SplashUiState()
}
