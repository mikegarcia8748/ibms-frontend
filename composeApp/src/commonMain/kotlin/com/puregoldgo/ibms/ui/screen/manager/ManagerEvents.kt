package com.puregoldgo.ibms.ui.screen.manager

/** One-shot signals from the oversight console. */
sealed class ManagerUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : ManagerUiEvent()

    /** A message to show briefly, usually a validation or server error. */
    data class ShowSnackbar(val message: String) : ManagerUiEvent()
}
