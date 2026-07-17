package com.puregoldgo.ibms.ui.screen.auth

/**
 * One-time UI events emitted by [LoginViewModel].
 */
sealed class LoginUiEvent {
    data object NavigateToHome : LoginUiEvent()
    data class ShowError(val message: String) : LoginUiEvent()
}
