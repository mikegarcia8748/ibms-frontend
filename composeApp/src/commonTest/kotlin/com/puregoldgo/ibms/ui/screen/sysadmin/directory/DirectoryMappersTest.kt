package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** What a directory row keeps from the API user behind it. */
class DirectoryMappersTest {

    // region UserProfile -> DirectoryUser

    @Test
    fun tc51_should_carry_the_fields_the_row_draws() {
        val row = UserProfile(
            id = "usr-1",
            username = "rlim",
            name = "Rosario D Lim",
            employeeNumber = "010007422",
            role = Role.PENDING,
            status = UserStatus.ACTIVE,
            mustChangePassword = true,
        ).toRow()

        assertEquals("rlim", row.username)
        assertEquals("010007422", row.employeeNumber)
        assertEquals(Role.PENDING, row.role)
        assertTrue(row.isActive)
        // Drives the TEMPORARY chip, and is the only trace a temporary password
        // leaves once the dialog that showed it is gone.
        assertTrue(row.mustChangePassword)
    }

    @Test
    fun tc52_should_mark_a_deactivated_user_inactive() {
        val row = UserProfile(
            id = "usr-1",
            username = "garciaga",
            name = "Gilbert Arciaga",
            role = Role.SECRETARY,
            status = UserStatus.INACTIVE,
            mustChangePassword = false,
        ).toRow()

        assertFalse(row.isActive)
    }

    @Test
    fun tc53_should_fall_back_to_the_username_for_a_blank_name() {
        val row = UserProfile(
            id = "usr-1",
            username = "jdoe",
            name = "",
            role = Role.PENDING,
            mustChangePassword = false,
        ).toRow()

        assertEquals("J", row.initial)
    }

    // endregion
}
