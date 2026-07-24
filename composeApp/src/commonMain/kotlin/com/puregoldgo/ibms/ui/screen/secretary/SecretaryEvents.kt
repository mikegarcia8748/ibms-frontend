package com.puregoldgo.ibms.ui.screen.secretary

/** One-shot navigation signals from the secretary console. */
sealed class SecretaryUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : SecretaryUiEvent()

    /** A billing-history row was tapped — navigate to the full-screen detail. */
    data class NavigateToTopSheetDetail(val topSheetId: String) : SecretaryUiEvent()
}
