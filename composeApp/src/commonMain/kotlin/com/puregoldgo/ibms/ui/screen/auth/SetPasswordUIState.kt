package com.puregoldgo.ibms.ui.screen.auth

import com.puregoldgo.ibms.shared.validation.PasswordPolicy

/**
 * Immutable UI state for the set-password screen.
 *
 * [terminal] is the important field: an expired challenge or an
 * already-redeemed one cannot be recovered by editing the form, so the screen
 * stops being a form at that point and becomes a single way back to sign-in.
 */
data class SetPasswordUIState(
    val displayName: String = "",
    val username: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val rules: List<PasswordPolicy.Rule> = emptyList(),
    val strength: PasswordPolicy.Strength = PasswordPolicy.Strength.None,
    val remainingSeconds: Long = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val confirmError: String? = null,
    val terminal: TerminalState? = null,
) {
    /**
     * The server is the authority on the policy; this only decides whether it is
     * worth spending a round trip yet.
     */
    val canSubmit: Boolean
        get() = !isLoading &&
            !isSuccess &&
            terminal == null &&
            newPassword.isNotEmpty() &&
            rules.all { it.satisfied } &&
            newPassword == confirmPassword

    val isExpiringSoon: Boolean
        get() = terminal == null && remainingSeconds in 1..EXPIRY_WARNING_SECONDS

    /** Reasons the screen can no longer be completed. */
    enum class TerminalState {
        /** The ten-minute window ran out, or the backend rejected the challenge. */
        ChallengeExpired,

        /** The challenge was already redeemed — the password is set, so sign in. */
        PasswordAlreadySet,
    }

    companion object {
        const val EXPIRY_WARNING_SECONDS = 60L
    }
}
