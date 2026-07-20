package com.puregoldgo.core.network

/**
 * The authentication failures the UI actually behaves differently about.
 *
 * "Wrong password" and "your temporary password expired" are both 401s, and
 * "account locked" and "role not permitted" are both 403s — but each pair needs
 * a different screen, so status alone cannot drive the UI.
 *
 * The backend does classify these (`DomainError.code`), but its error envelope
 * drops the code before it reaches the wire, leaving `{result, status, message}`.
 * Until that changes, the classification is reconstructed here from status plus
 * the message, in exactly one place — so no ViewModel ever matches on prose.
 */
enum class AuthFailure {
    /** Wrong username or password. Uniform by design: no account enumeration. */
    InvalidCredentials,

    /** The temporary password hashed correctly but is past its TTL. */
    TempPasswordExpired,

    /** Too many failed attempts; the account is barred for a cooling-off period. */
    AccountLocked,

    /** The account was deactivated by a sysadmin. */
    AccountInactive,

    /** The chosen password does not satisfy the policy. */
    WeakPassword,

    /** The chosen password matches the one being replaced. */
    PasswordReused,

    /** The change-password challenge expired or was rejected. */
    ChallengeExpired,

    /** The challenge was already redeemed — the password is set. */
    PasswordAlreadySet,

    /** The stored refresh token is spent or revoked; a fresh sign-in is required. */
    SessionExpired,

    /** Too many requests from this client. */
    RateLimited,

    /** The request never reached the backend. */
    Offline,

    /** The backend answered, but with a fault of its own. */
    Server,

    Unknown,
}

/** A failure plus the text to put in front of the user. */
data class AuthError(
    val failure: AuthFailure,
    val message: String,
)

/**
 * Maps a failed auth call to an [AuthError].
 *
 * Some backend messages are already written for end users — the password policy
 * spells out every unmet rule, and the lockout states its duration — so those
 * are passed through verbatim rather than replaced with something vaguer. The
 * rest get copy written here.
 */
object AuthErrorMapper {

    fun map(status: Int, serverMessage: String?): AuthError {
        val raw = serverMessage?.trim().orEmpty()
        val message = raw.lowercase()

        val failure = when {
            status == 0 -> AuthFailure.Offline
            status == 429 -> AuthFailure.RateLimited
            status >= 500 -> AuthFailure.Server

            status == 401 && message.contains("temporary password") -> AuthFailure.TempPasswordExpired
            status == 401 && message.contains("refresh token") -> AuthFailure.SessionExpired
            status == 401 && message.contains("sign in again") -> AuthFailure.SessionExpired
            status == 401 && message.contains("password-change challenge") -> AuthFailure.ChallengeExpired
            status == 401 -> AuthFailure.InvalidCredentials

            status == 403 && message.contains("locked") -> AuthFailure.AccountLocked
            status == 403 && message.contains("deactivated") -> AuthFailure.AccountInactive

            status == 409 && message.contains("already been set") -> AuthFailure.PasswordAlreadySet
            status == 409 -> AuthFailure.PasswordAlreadySet

            status == 400 && message.contains("must differ") -> AuthFailure.PasswordReused
            status == 400 && message.startsWith("password must") -> AuthFailure.WeakPassword

            else -> AuthFailure.Unknown
        }

        return AuthError(failure, displayMessage(failure, raw))
    }

    private fun displayMessage(failure: AuthFailure, serverMessage: String): String = when (failure) {
        // The server's own wording is the better one here — it enumerates the
        // unmet rules, or names the lockout duration.
        AuthFailure.WeakPassword,
        AuthFailure.PasswordReused,
        AuthFailure.AccountLocked,
        AuthFailure.AccountInactive,
        AuthFailure.TempPasswordExpired,
        -> serverMessage.ifBlank { messageFor(failure) }

        else -> messageFor(failure)
    }

    /** The app's own wording for a failure, used when there is no server text to prefer. */
    fun messageFor(failure: AuthFailure): String = when (failure) {
        AuthFailure.InvalidCredentials -> "Incorrect username or password."
        AuthFailure.TempPasswordExpired ->
            "Your temporary password has expired. Ask a sysadmin to issue a new one."
        AuthFailure.AccountLocked -> "Too many failed attempts. Try again later."
        AuthFailure.AccountInactive -> "This account has been deactivated. Contact a sysadmin."
        AuthFailure.WeakPassword -> "That password does not meet the requirements."
        AuthFailure.PasswordReused -> "Your new password must differ from your current one."
        AuthFailure.ChallengeExpired ->
            "That took too long. Sign in again with your temporary password."
        AuthFailure.PasswordAlreadySet -> "Your password is already set. Please sign in."
        AuthFailure.SessionExpired -> "Your session expired. Please sign in again."
        AuthFailure.RateLimited -> "Too many attempts. Wait a moment and try again."
        AuthFailure.Offline -> "Can't reach the server. Check your connection and try again."
        AuthFailure.Server -> "The server had a problem. Please try again."
        AuthFailure.Unknown -> "Something went wrong. Please try again."
    }
}
