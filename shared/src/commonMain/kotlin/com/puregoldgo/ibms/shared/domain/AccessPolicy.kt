package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus

/**
 * Whether a signed-in account can actually use the app.
 *
 * Holding a session is not the same as having access. `POST /users` defaults a
 * new account to the `pending` role, so someone can legitimately complete the
 * whole temporary-password flow and still have no permissions; and an access
 * token issued before a deactivation stays valid until it expires. Both land on
 * the same "nothing for you here yet" screen rather than on an empty dashboard
 * or a wall of 403s.
 */
fun UserProfile.hasOperationalAccess(): Boolean =
    status == UserStatus.ACTIVE && role != Role.PENDING
