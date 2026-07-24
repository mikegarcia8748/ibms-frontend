package com.puregoldgo.ibms.ui.screen.secretary

/** One-shot signals from the secretary console. */
sealed class SecretaryUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : SecretaryUiEvent()

    /** A billing-history row was tapped — navigate to the full-screen detail. */
    data class NavigateToTopSheetDetail(val topSheetId: String) : SecretaryUiEvent()

    /** Show a transient message for validation or server errors. */
    data class ShowSnackbar(val message: String) : SecretaryUiEvent()

    /**
     * The selected store already has an active account.
     * The UI should show a confirmation dialog; if the user confirms,
     * [SecretaryCallback.onAddAccountSubmitConfirmed] is called.
     */
    data class ShowStoreHasAccountConfirmation(val storeName: String) : SecretaryUiEvent()

    /** A message to show briefly, usually a validation or server error. */
    data class ShowSnackbar(val message: String) : SecretaryUiEvent()
}
