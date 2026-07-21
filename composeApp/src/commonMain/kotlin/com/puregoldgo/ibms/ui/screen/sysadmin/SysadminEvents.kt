package com.puregoldgo.ibms.ui.screen.sysadmin

/** One-shot navigation signals from the control panel. */
sealed class SysadminUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : SysadminUiEvent()
}
