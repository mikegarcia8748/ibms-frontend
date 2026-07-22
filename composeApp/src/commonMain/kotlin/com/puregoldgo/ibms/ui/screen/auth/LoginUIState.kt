package com.puregoldgo.ibms.ui.screen.auth

import com.puregoldgo.core.network.AuthFailure

/**
 * Immutable UI state for the Login screen.
 *
 * [failure] is kept alongside [errorMessage] because not every rejection is the
 * same shape of problem: a typo belongs next to the field, while an expired
 * temporary password or a locked account is a standing condition the user cannot
 * type their way out of and needs to read as such.
 */
data class LoginUIState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val failure: AuthFailure? = null,
    val isAuthenticated: Boolean = false,
    val serverUnavailable: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isLoading && username.isNotBlank() && password.isNotBlank()

    /**
     * Failures no amount of retyping will fix — the account needs a sysadmin, or
     * time. Shown as a notice rather than as field feedback.
     */
    val isStandingCondition: Boolean
        get() = failure in setOf(
            AuthFailure.TempPasswordExpired,
            AuthFailure.AccountLocked,
            AuthFailure.AccountInactive,
            AuthFailure.RateLimited,
        )

    /** Only the temporary-password case has a specific next step worth suggesting. */
    val showsSysadminHint: Boolean
        get() = failure == AuthFailure.TempPasswordExpired
}
