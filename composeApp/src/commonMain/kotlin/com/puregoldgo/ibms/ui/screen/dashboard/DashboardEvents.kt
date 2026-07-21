package com.puregoldgo.ibms.ui.screen.dashboard

/** One-shot navigation signals from the control panel. */
sealed class DashboardUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : DashboardUiEvent()
}
