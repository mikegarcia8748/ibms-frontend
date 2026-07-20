package com.puregoldgo.ibms.ui.screen.auth

import com.puregoldgo.ibms.ui.screen.access.NoAccessReason

/**
 * One-time UI events emitted by [SetPasswordViewModel].
 */
sealed class SetPasswordUiEvent {

    /**
     * The password was set and the response carried a session — the user is now
     * authenticated, without having to sign in a second time.
     */
    data object NavigateToHome : SetPasswordUiEvent()

    /**
     * A session exists but the account cannot use the app yet: freshly
     * provisioned accounts default to the `pending` role, and a deactivated one
     * can still hold a valid token until it expires.
     */
    data class NavigateToNoAccess(val reason: NoAccessReason) : SetPasswordUiEvent()

    /** The challenge is gone. The only way forward is signing in again. */
    data object NavigateToLogin : SetPasswordUiEvent()
}
