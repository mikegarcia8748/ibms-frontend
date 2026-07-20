package com.puregoldgo.ibms.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the client-side policy to the backend's `PasswordPolicy`. If these two
 * drift, the screen either accepts something the server will reject or blocks
 * something it would have taken — both of which land on the user.
 */
class PasswordPolicyTest {

    private fun unmet(password: String, username: String = "mikepg") =
        PasswordPolicy.evaluate(password, username).filterNot { it.satisfied }.map { it.id }

    // region Boundaries

    @Test
    fun tc01_should_reject_password_one_character_below_minimum() {
        val password = "Passw0rdAbc" // 11 characters
        assertEquals(11, password.length)
        assertTrue(PasswordPolicy.RuleId.MinLength in unmet(password))
    }

    @Test
    fun tc02_should_accept_password_exactly_at_minimum() {
        val password = "Passw0rdAbcd" // 12 characters
        assertEquals(PasswordPolicy.MIN_LENGTH, password.length)
        assertTrue(PasswordPolicy.isSatisfiedBy(password, "mikepg"))
    }

    @Test
    fun tc03_should_accept_password_exactly_at_maximum() {
        val password = "A1" + "a".repeat(PasswordPolicy.MAX_LENGTH - 2)
        assertEquals(PasswordPolicy.MAX_LENGTH, password.length)
        assertTrue(PasswordPolicy.isSatisfiedBy(password, "mikepg"))
    }

    @Test
    fun tc04_should_reject_password_one_character_above_maximum() {
        // bcrypt truncates past 72 bytes, so anything longer is only pretending
        // to be stronger.
        val password = "A1" + "a".repeat(PasswordPolicy.MAX_LENGTH - 1)
        assertEquals(PasswordPolicy.MAX_LENGTH + 1, password.length)
        assertTrue(PasswordPolicy.RuleId.MaxLength in unmet(password))
    }

    // endregion

    // region Character classes

    @Test
    fun tc05_should_reject_password_without_uppercase() {
        assertTrue(PasswordPolicy.RuleId.Uppercase in unmet("passw0rdabcd"))
    }

    @Test
    fun tc06_should_reject_password_without_lowercase() {
        assertTrue(PasswordPolicy.RuleId.Lowercase in unmet("PASSW0RDABCD"))
    }

    @Test
    fun tc07_should_reject_password_without_digit() {
        assertTrue(PasswordPolicy.RuleId.Digit in unmet("PasswordAbcd"))
    }

    @Test
    fun tc08_should_reject_password_containing_whitespace() {
        assertTrue(PasswordPolicy.RuleId.NoSpaces in unmet("Passw0rd Abcd"))
    }

    // endregion

    // region Username containment

    @Test
    fun tc09_should_reject_password_containing_username() {
        assertTrue(PasswordPolicy.RuleId.NotUsername in unmet("Mikepg-Passw0rd"))
    }

    @Test
    fun tc10_should_reject_password_containing_username_in_any_case() {
        assertTrue(PasswordPolicy.RuleId.NotUsername in unmet("MIKEPG-Passw0rd"))
    }

    @Test
    fun tc11_should_ignore_username_rule_when_username_is_blank() {
        assertTrue(PasswordPolicy.RuleId.NotUsername !in unmet("Chosen-Passw0rd", username = ""))
    }

    // endregion

    // region Validation messages

    @Test
    fun tc12_should_return_null_for_a_valid_password() {
        assertNull(Validation.validateNewPassword("Chosen-Passw0rd", "mikepg"))
    }

    @Test
    fun tc13_should_describe_every_unmet_rule_in_one_message() {
        val message = Validation.validateNewPassword("short", "mikepg")
        // One trip, every reason — not one rejection per attempt.
        assertTrue(message!!.contains("at least 12 characters"), message)
        assertTrue(message.contains("uppercase"), message)
        assertTrue(message.contains("digit"), message)
    }

    @Test
    fun tc14_should_require_a_non_blank_password() {
        assertEquals("New password is required", Validation.validateNewPassword("   ", "mikepg"))
    }

    @Test
    fun tc15_should_reject_mismatched_confirmation() {
        assertEquals(
            "Passwords do not match",
            Validation.validatePasswordConfirmation("Chosen-Passw0rd", "Chosen-Passw0rE"),
        )
    }

    @Test
    fun tc16_should_accept_matching_confirmation() {
        assertNull(Validation.validatePasswordConfirmation("Chosen-Passw0rd", "Chosen-Passw0rd"))
    }

    // endregion

    // region Strength

    @Test
    fun tc17_should_report_no_strength_for_empty_input() {
        assertEquals(PasswordPolicy.Strength.None, PasswordPolicy.strength(""))
    }

    @Test
    fun tc18_should_rate_a_short_password_weak() {
        assertEquals(PasswordPolicy.Strength.Weak, PasswordPolicy.strength("Ab1"))
    }

    @Test
    fun tc19_should_rate_a_long_varied_password_strong() {
        assertEquals(PasswordPolicy.Strength.Strong, PasswordPolicy.strength("Colossal-Squid-42-Ink!"))
    }

    @Test
    fun tc20_should_discount_a_password_that_is_mostly_one_character() {
        // Long, but nearly all the same character: not as strong as it looks.
        val repeated = PasswordPolicy.strength("Aaaaaaaaaaaaaaaa1")
        assertFalse(repeated == PasswordPolicy.Strength.Strong, "was $repeated")
    }

    // endregion
}
