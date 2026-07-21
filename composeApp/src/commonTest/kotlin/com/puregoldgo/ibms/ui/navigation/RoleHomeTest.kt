package com.puregoldgo.ibms.ui.navigation

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Where each role lands.
 *
 * The regression guard for a role added later: [homeRouteFor] has no `else`, so
 * a new constant already breaks the build — but nothing would stop someone
 * silently mapping it to the wrong console, and [tc01] is what catches that.
 */
class RoleHomeTest {

    @Test
    fun tc01_should_give_every_role_a_home() {
        // No role may fall through to a placeholder the way finance, manager and
        // payables once fell through to the provider list.
        Role.entries.forEach { role ->
            val home = homeRouteFor(role)
            assertTrue(
                home is Route.SysadminDashboard ||
                    home is Route.SecretaryDashboard ||
                    home is Route.FinanceDashboard ||
                    home is Route.ManagerDashboard ||
                    home is Route.NoAccess,
                "no home for $role",
            )
        }
    }

    @Test
    fun tc02_should_send_each_working_role_to_its_own_console() {
        assertEquals(Route.SysadminDashboard, homeRouteFor(Role.SYSADMIN))
        assertEquals(Route.SecretaryDashboard, homeRouteFor(Role.SECRETARY))
        assertEquals(Route.FinanceDashboard, homeRouteFor(Role.FINANCE))
        assertEquals(Route.ManagerDashboard, homeRouteFor(Role.MANAGER))
    }

    @Test
    fun tc03_should_send_a_retired_payables_account_to_finance() {
        // The role is no longer handed out, but existing holders still sign in.
        assertEquals(Route.FinanceDashboard, homeRouteFor(Role.PAYABLES))
    }

    @Test
    fun tc04_should_send_an_account_with_no_role_to_the_no_access_screen() {
        assertEquals(Route.NoAccess(NoAccessReason.PendingRole), homeRouteFor(Role.PENDING))
    }

    @Test
    fun tc05_should_send_an_unrecognised_role_to_the_no_access_screen() {
        // A role this build has never heard of is one the backend added without
        // it. Anything else would draw a console behind a wall of 403s.
        assertEquals(Route.NoAccess(NoAccessReason.PendingRole), homeRouteFor(null))
    }
}
