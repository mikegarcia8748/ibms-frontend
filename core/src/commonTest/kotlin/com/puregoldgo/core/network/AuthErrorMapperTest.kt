package com.puregoldgo.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The backend classifies its failures but drops the code before the wire, so the
 * app reconstructs the classification from status plus message. These cases use
 * the backend's real wording (ibms-backend `AuthUseCases.kt`) — if it changes,
 * this is where it should break, rather than in front of a user.
 */
class AuthErrorMapperTest {

    // region 401 — three different problems behind one status

    @Test
    fun tc01_should_read_a_plain_401_as_invalid_credentials() {
        val error = AuthErrorMapper.map(401, "invalid username or password")

        assertEquals(AuthFailure.InvalidCredentials, error.failure)
        // Uniform wording: the response must not hint at whether the account exists.
        assertEquals("Incorrect username or password.", error.message)
    }

    @Test
    fun tc02_should_recognise_an_expired_temporary_password() {
        val error = AuthErrorMapper.map(
            401,
            "your temporary password has expired — ask a sysadmin to issue a new one",
        )

        assertEquals(AuthFailure.TempPasswordExpired, error.failure)
        // The server's wording already names the next step; keep it.
        assertTrue(error.message.contains("sysadmin"))
    }

    @Test
    fun tc03_should_recognise_a_dead_refresh_token() {
        val error = AuthErrorMapper.map(401, "refresh token is invalid or expired")

        assertEquals(AuthFailure.SessionExpired, error.failure)
    }

    @Test
    fun tc04_should_recognise_a_forced_reauthentication() {
        val error = AuthErrorMapper.map(401, "please sign in again")

        assertEquals(AuthFailure.SessionExpired, error.failure)
    }

    @Test
    fun tc05_should_recognise_a_rejected_challenge() {
        val error = AuthErrorMapper.map(401, "a valid password-change challenge is required")

        assertEquals(AuthFailure.ChallengeExpired, error.failure)
    }

    // endregion

    // region 403 — locked vs deactivated

    @Test
    fun tc06_should_recognise_a_locked_account_and_keep_its_duration() {
        val error = AuthErrorMapper.map(
            403,
            "too many failed attempts — this account is locked for 15 minutes",
        )

        assertEquals(AuthFailure.AccountLocked, error.failure)
        // The duration only exists in the server's text, so it must survive.
        assertTrue(error.message.contains("15 minutes"))
    }

    @Test
    fun tc07_should_recognise_a_deactivated_account() {
        val error = AuthErrorMapper.map(403, "this account has been deactivated — contact a sysadmin")

        assertEquals(AuthFailure.AccountInactive, error.failure)
    }

    // endregion

    // region Password change

    @Test
    fun tc08_should_recognise_a_weak_password_and_keep_the_unmet_rules() {
        val message = "password must be at least 12 characters, contain a digit"
        val error = AuthErrorMapper.map(400, message)

        assertEquals(AuthFailure.WeakPassword, error.failure)
        assertEquals(message, error.message)
    }

    @Test
    fun tc09_should_recognise_a_reused_password() {
        val error = AuthErrorMapper.map(400, "new password must differ from your current one")

        assertEquals(AuthFailure.PasswordReused, error.failure)
    }

    @Test
    fun tc10_should_recognise_an_already_redeemed_challenge() {
        val error = AuthErrorMapper.map(409, "password has already been set")

        assertEquals(AuthFailure.PasswordAlreadySet, error.failure)
    }

    // endregion

    // region Transport

    @Test
    fun tc11_should_read_a_missing_status_as_offline() {
        // No status means the request never got an answer — not a rejection.
        val error = AuthErrorMapper.map(0, "Failed to connect")

        assertEquals(AuthFailure.Offline, error.failure)
        assertTrue(error.message.contains("Can't reach the server"))
    }

    @Test
    fun tc12_should_recognise_throttling() {
        assertEquals(AuthFailure.RateLimited, AuthErrorMapper.map(429, "too many requests").failure)
    }

    @Test
    fun tc13_should_recognise_a_server_fault() {
        assertEquals(AuthFailure.Server, AuthErrorMapper.map(500, "internal server error").failure)
    }

    @Test
    fun tc14_should_fall_back_to_unknown_for_an_unmapped_status() {
        val error = AuthErrorMapper.map(418, "")

        assertEquals(AuthFailure.Unknown, error.failure)
        assertTrue(error.message.isNotBlank())
    }

    @Test
    fun tc15_should_never_return_a_blank_message_even_without_server_text() {
        AuthFailure.entries.forEach { failure ->
            assertTrue(AuthErrorMapper.messageFor(failure).isNotBlank(), "blank for $failure")
        }
    }

    // endregion
}
