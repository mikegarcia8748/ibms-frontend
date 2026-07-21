package com.puregoldgo.ibms.ui.screen.manager

/** One-shot navigation signals from the oversight console. */
sealed class ManagerUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : ManagerUiEvent()
}
