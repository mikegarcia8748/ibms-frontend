package com.puregoldgo.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Every secret this app exchanges travels under a *compound* key —
 * `temporaryPassword`, `newPassword`, `challengeToken`, `refreshToken`. An
 * earlier version of the masker anchored each sensitive word to the opening
 * quote of the key, so all four went to the log in the clear. These cases name
 * the real field names off the wire; if one is ever missed again, it breaks
 * here rather than in a log file.
 */
class PayloadMaskerTest {

    private val mask = "***MASKED***"

    @Test
    fun tc01_should_mask_a_provisioned_temporary_password() {
        val masked = PayloadMasker.mask(
            """{"user":{"username":"jdoe"},"temporaryPassword":"Xy7!kQ2mNp","status":"201"}""",
        )

        assertFalse(masked!!.contains("Xy7!kQ2mNp"))
        assertTrue(masked.contains(""""temporaryPassword":"$mask""""))
        // Only the secret goes; the surrounding payload has to stay readable or
        // the log stops being worth keeping.
        assertTrue(masked.contains(""""username":"jdoe""""))
    }

    @Test
    fun tc02_should_mask_a_change_password_body() {
        val masked = PayloadMasker.mask("""{"newPassword":"Sup3rSecret!"}""")

        assertEquals("""{"newPassword":"$mask"}""", masked)
    }

    @Test
    fun tc03_should_mask_a_challenge_token() {
        val masked = PayloadMasker.mask("""{"challengeToken":"eyJhbGciOi","expiresInSeconds":900}""")

        assertFalse(masked!!.contains("eyJhbGciOi"))
        assertTrue(masked.contains(""""challengeToken":"$mask""""))
    }

    @Test
    fun tc04_should_mask_a_whole_session() {
        val masked = PayloadMasker.mask(
            """{"accessToken":"aaa.bbb.ccc","refreshToken":"rt-123","tokenType":"Bearer"}""",
        )

        assertFalse(masked!!.contains("aaa.bbb.ccc"))
        assertFalse(masked.contains("rt-123"))
        // `tokenType` is a compound ending in "Type", not in a sensitive word,
        // so it is left alone — it says nothing secret and aids debugging.
        assertTrue(masked.contains(""""tokenType":"Bearer""""))
    }

    @Test
    fun tc05_should_mask_regardless_of_spacing_or_case() {
        val masked = PayloadMasker.mask("""{"Password" : "hunter2"}""")

        assertFalse(masked!!.contains("hunter2"))
    }

    @Test
    fun tc06_should_pass_through_a_payload_with_nothing_to_hide() {
        val payload = """{"id":"usr-1","role":"sysadmin"}"""

        assertEquals(payload, PayloadMasker.mask(payload))
    }

    @Test
    fun tc07_should_pass_through_null_and_blank() {
        assertEquals(null, PayloadMasker.mask(null))
        assertEquals("", PayloadMasker.mask(""))
    }
}
