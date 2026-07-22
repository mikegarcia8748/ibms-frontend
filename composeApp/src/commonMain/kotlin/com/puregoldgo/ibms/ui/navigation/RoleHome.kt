package com.puregoldgo.ibms.ui.navigation

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason

/**
 * Where a role lands after signing in.
 *
 * The single answer to that question, stated once. It used to live inline in
 * `AppNavigation` with an `else` branch that sent finance, manager and payables
 * to the provider list — a placeholder that read as a home screen, and one that
 * would have quietly absorbed every role added after it.
 *
 * There is deliberately no `else` here. A new [Role] constant stops this file
 * compiling until somebody decides where its holder goes, which is the point:
 * that decision should be made in a code review, not discovered in production by
 * the first person to hold the role.
 *
 * This picks a landing screen and nothing more. Every console it leads to is
 * still subject to the server's own role checks — routing is a convenience, not
 * access control.
 */
fun homeRouteFor(role: Role?): Route = when (role) {
    Role.SYSADMIN -> Route.SysadminDashboard
    Role.SECRETARY -> Route.SecretaryDashboard

    // Payables is being retired: it is no longer offered when provisioning a
    // user, and the accounts it can still create belong to the finance side of
    // the ledger. Existing holders land here until the backend drops the value.
    // See the note on `Role.PAYABLES`.
    Role.FINANCE, Role.PAYABLES -> Route.FinanceDashboard

    Role.MANAGER -> Route.ManagerDashboard

    // No role, or one this build has never heard of. Both mean "signed in with
    // nothing to use", and the no-access screen says so — where the old
    // fallback would have drawn a console behind a wall of 403s.
    Role.PENDING, null -> Route.NoAccess(NoAccessReason.PendingRole)
}
