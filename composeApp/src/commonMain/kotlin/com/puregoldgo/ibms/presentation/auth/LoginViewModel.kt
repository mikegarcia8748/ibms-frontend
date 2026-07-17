package com.puregoldgo.ibms.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.usecase.LoginUseCase
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
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            loginUseCase(
                username = state.username.trim(),
                password = state.password,
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(isLoading = false, isAuthenticated = true)
                        }
                        _uiEvent.emit(LoginUiEvent.NavigateToHome)
                    }
                    is Resource.Failed -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                        _uiEvent.emit(LoginUiEvent.ShowError(resource.message ?: "An unexpected error occurred"))
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.error?.message)
                        }
                        _uiEvent.emit(LoginUiEvent.ShowError(resource.error?.message ?: "An unexpected error occurred"))
                    }
                }
            }
        }
    }

    // endregion
}
