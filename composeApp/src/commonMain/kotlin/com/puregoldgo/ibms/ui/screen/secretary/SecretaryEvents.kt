package com.puregoldgo.ibms.ui.screen.secretary

/** One-shot navigation signals from the secretary console. */
sealed class SecretaryUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : SecretaryUiEvent()
}
