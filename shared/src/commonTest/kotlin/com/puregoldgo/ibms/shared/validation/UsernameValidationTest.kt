package com.puregoldgo.ibms.shared.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Mirrors the backend's `UsernamePolicy` and the `users_username_format` CHECK.
 * If those three ever disagree, an admin fills in a form that the server then
 * refuses — so this is the copy that has to break first.
 */
class UsernameValidationTest {

    @Test
    fun tc01_should_reject_a_blank_username() {
        assertEquals("Username is required", Validation.validateUsername(""))
        assertEquals("Username is required", Validation.validateUsername("   "))
    }

    @Test
    fun tc02_should_reject_one_shorter_than_three_characters() {
        assertNotNull(Validation.validateUsername("jd"))
    }

    @Test
    fun tc03_should_reject_one_longer_than_thirty_two_characters() {
        assertNotNull(Validation.validateUsername("j".repeat(33)))
    }

    @Test
    fun tc04_should_reject_illegal_characters() {
        assertNotNull(Validation.validateUsername("jane doe"))
        assertNotNull(Validation.validateUsername("jane@doe"))
        assertNotNull(Validation.validateUsername("jane+doe"))
    }

    @Test
    fun tc05_should_accept_the_punctuation_the_backend_allows() {
        assertNull(Validation.validateUsername("m.garcia"))
        assertNull(Validation.validateUsername("m_garcia"))
        assertNull(Validation.validateUsername("m-garcia-2"))
    }

    @Test
    fun tc06_should_accept_both_ends_of_the_length_range() {
        assertNull(Validation.validateUsername("abc"))
        assertNull(Validation.validateUsername("a".repeat(32)))
    }

    @Test
    fun tc07_should_validate_the_normalised_form() {
        // The column is CITEXT and the backend lowercases on the way in, so
        // uppercase input is valid — it is simply the same account.
        assertNull(Validation.validateUsername("M.Garcia"))
        assertNull(Validation.validateUsername("  mgarcia  "))
    }

    @Test
    fun tc08_should_normalise_by_trimming_and_lowercasing() {
        assertEquals("m.garcia", Validation.normalizeUsername("  M.Garcia  "))
    }
}
