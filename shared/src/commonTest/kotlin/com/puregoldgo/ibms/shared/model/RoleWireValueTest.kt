package com.puregoldgo.ibms.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The two directions a role travels between `:core`'s stored string and the enum.
 *
 * Worth a test because `:core` keeps the signed-in role as a raw wire value —
 * it sits below this module and cannot see [Role] — so every routing decision
 * in the app goes through [roleFromWire], and a constant whose `@SerialName`
 * drifted from its name would silently stop matching.
 */
class RoleWireValueTest {

    @Test
    fun tc01_should_round_trip_every_constant() {
        Role.entries.forEach { role ->
            assertEquals(role, roleFromWire(role.wireValue), "round trip failed for $role")
        }
    }

    @Test
    fun tc02_should_use_the_lowercase_wire_names_the_backend_emits() {
        assertEquals("sysadmin", Role.SYSADMIN.wireValue)
        assertEquals("secretary", Role.SECRETARY.wireValue)
        assertEquals("finance", Role.FINANCE.wireValue)
        assertEquals("manager", Role.MANAGER.wireValue)
        assertEquals("pending", Role.PENDING.wireValue)
    }

    @Test
    fun tc03_should_return_null_for_a_value_this_build_does_not_know() {
        // A role the backend added without this client. Guessing one for it
        // would be the one guess that matters.
        assertNull(roleFromWire("auditor"))
        assertNull(roleFromWire(""))
        assertNull(roleFromWire(null))
    }

    @Test
    fun tc04_should_not_match_on_case() {
        // The wire value is lowercase; a JWT that carried "SYSADMIN" is not one
        // this backend issued.
        assertNull(roleFromWire("SYSADMIN"))
    }
}
