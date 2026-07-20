package com.puregoldgo.ibms.ui.screen.auth

import com.puregoldgo.ibms.ui.screen.access.NoAccessReason

/**
 * One-time UI events emitted by [LoginViewModel].
 */
sealed class LoginUiEvent {
    data object NavigateToHome : LoginUiEvent()

    /** Authenticated, but the account has no role yet — or has been deactivated. */
    data class NavigateToNoAccess(val reason: NoAccessReason) : LoginUiEvent()

    /**
     * The credentials were a valid *temporary* password. The caller is NOT
     * authenticated — it holds only a single-use challenge, which must be
     * redeemed at `POST /auth/password/change` to obtain a session.
     *
     * The challenge travels no further than `ChallengeHolder`: this event carries
     * no token, because navigation state is serialized and a bearer credential
     * has no business being written to disk.
     */
    data object NavigateToPasswordChange : LoginUiEvent()

    data class ShowError(val message: String) : LoginUiEvent()
}
