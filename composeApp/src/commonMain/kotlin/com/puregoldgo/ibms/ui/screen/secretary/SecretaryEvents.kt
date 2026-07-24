package com.puregoldgo.ibms.ui.screen.secretary

/** One-shot signals from the secretary console. */
sealed class SecretaryUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : SecretaryUiEvent()

    /** A message to show briefly, usually a validation or server error. */
    data class ShowSnackbar(val message: String) : SecretaryUiEvent()
}
